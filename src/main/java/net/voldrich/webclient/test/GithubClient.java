package net.voldrich.webclient.test;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.LongAdder;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import com.google.common.util.concurrent.RateLimiter;
import net.voldrich.webclient.test.dto.GithubError;
import net.voldrich.webclient.test.dto.User;
import net.voldrich.webclient.test.dto.UserDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.retry.Retry;

/**
 * Uses Spring Web client
 */
public class GithubClient {

    private static final Logger logger = LoggerFactory.getLogger(GithubClient.class);

    public static final String GITHUB_URL = "https://api.github.com";

    public static final String USER_DETAIL_URL = "/users/{name}";

    public static final String CONTRIBUTORS_URL = "/repos/{owner}/{repo}/contributors";

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Used DateFormat by this connector and github
     */
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    public static final String AGENT_NAME = "Github Spring WebClient connector";

    public static final MediaType VND_GITHUB_V3 = MediaType.valueOf("application/vnd.github.v3+json");

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final String CONTEXT_REQUEST_START = "REQUEST_START";

    public static final Scheduler RATE_LIMIT_SCHEDULER = Schedulers.newSingle("RateLimitThread", true);

    private final GithubClientConfiguration config;

    private final WebClient client;

    private final UriBuilderFactory builderFactory;

    private final LongAdder numberOfRequests = new LongAdder();

    @Nullable
    private RateLimiter rateLimiter;

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public enum Paging {
        PARALEL_CONCAT_MAP,
        PARALEL_FLAT_MAP,
        RECURSIVE,
        EXPAND,
    }

    public GithubClient(GithubClientConfiguration config) {
        this.config = config;
        this.builderFactory = new DefaultUriBuilderFactory(GITHUB_URL);
        if (config.getRatePerSecond() > 0) {
            this.rateLimiter = RateLimiter.create(config.getRatePerSecond());
        }
        this.client = WebClient.builder()
                .filter(ExchangeFilterFunctions.basicAuthentication("token ", config.getAccessToken()))
                .filter(userAgent())
                .filter(loggingFilter())
                .baseUrl(GITHUB_URL)
                .build();
    }

    private ExchangeFilterFunction userAgent() {
        return (clientRequest, exchangeFunction) -> {
            ClientRequest newRequest = ClientRequest
                    .from(clientRequest)
                    .header("User-Agent", AGENT_NAME)
                    .build();
            return exchangeFunction.exchange(newRequest);
        };
    }

    private ExchangeFilterFunction loggingFilter() {
        return (clientRequest, exchangeFunction) -> {
            numberOfRequests.increment();
            return exchangeFunction.exchange(clientRequest)
                    .zipWith(Mono.subscriberContext()) // this must placed before subscriberContext, otherwise it
                    // would be called in invalid order
                    .doOnSubscribe(subscription -> logger.info("{} started: {}", clientRequest.method(),
                            clientRequest.url().toString()))
                    .subscriberContext(context -> context.put(CONTEXT_REQUEST_START, new Date()))
                    .doOnNext(tuple -> {
                        Date startTime = tuple.getT2().get(CONTEXT_REQUEST_START);
                        Date endTime = new Date();
                        long delta = endTime.getTime() - startTime.getTime();
                        logger.info("{} finished {}: {} in {} ms",
                                clientRequest.method(),
                                tuple.getT1().statusCode().toString(),
                                clientRequest.url().toString(),
                                delta
                        );
                    }).map(Tuple2::getT1);
        };
    }

    public Flux<UserDetail> loadContributorSinglePage() {
        return loadContributors()
                .flatMap(user -> loadUserDetail(user.getLogin()));
    }

    public Flux<UserDetail> loadContributorsPaged(Paging pagingType) {
        URI pageUri = urlBuilder(CONTRIBUTORS_URL)
                .queryParam("per_page", DEFAULT_PAGE_SIZE)
                .build(config.getOwner(), config.getRepository());
        return performPageableRequest(pagingType, pageUri, 100)
                .log()
                .doOnComplete(() -> logger.info("Total Request Count: {}", numberOfRequests.longValue()));
    }

    public Flux<UserDetail> performPageableRequest(Paging pagingType, URI uri, int pagesLimit) {
        switch (pagingType) {
            case PARALEL_FLAT_MAP:
                return performPageableRequestParallelFlatMap(uri, User.class, pagesLimit)
                        .flatMap(user -> loadUserDetail(user.getLogin()));
            case RECURSIVE:
                return performPageableRequestRecursive(uri, User.class, pagesLimit)
                        .flatMap(user -> loadUserDetail(user.getLogin()));
            case EXPAND:
                return performPageableRequestExpand(uri, User.class)
                        .flatMap(user -> loadUserDetail(user.getLogin()));
            case PARALEL_CONCAT_MAP:
                return performPageableRequestParallelConcatMap(uri, User.class, pagesLimit)
                        .concatMap(user -> loadUserDetail(user.getLogin()));
        }
        return Flux.empty();
    }


    protected Flux<UserDetail> loadUserDetail(String name) {
        return getRequest(urlBuilder(USER_DETAIL_URL).build(name), UserDetail.class);
    }

    protected Flux<User> loadContributors() {
        URI uri = urlBuilder(CONTRIBUTORS_URL)
                .queryParam("per_page", 100)
                .build(config.getOwner(), config.getRepository());
        return getRequest(uri, User.class);
    }

    private UriBuilder urlBuilder(String urlPath) {
        return builderFactory.builder().path(urlPath);
    }

    /**
     * Creates a standard GET request with all requested headers and executes it.
     * Checks whether response is ok.
     */
    private <T> Mono<GithubResponseWrapper<T>> getRequestWrapped(URI uri, Class<T> clazz) {
        return getRequest(uri)
                .map(response -> new GithubResponseWrapper<T>(response, clazz));
    }

    private <T> Flux<T> getRequest(URI uri, Class<T> clazz) {
        return getRequest(uri)
                .flatMapMany(response -> response.bodyToFlux(clazz));
    }

    private Mono<ClientResponse> getRequest(URI uri) {
        Mono<ClientResponse> requestMono = this.client
                .get()
                .uri(uri)
                .accept(VND_GITHUB_V3)
                .exchange()
                .flatMap(this::checkResponse)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)).filter(throwable -> {
                    logger.warn("Request {} failed {}", uri, throwable.toString());
                    return throwable instanceof IOException;
                }));

        return limitRateRequest(requestMono, uri);
    }

    private Mono<ClientResponse> limitRateRequest(Mono<ClientResponse> requestMono, URI uri) {
        if (rateLimiter != null) {
            return Mono.<ClientResponse>create(monoSink -> {
                rateLimiter.acquire();
                logger.debug("Subscribing to request {}", uri);
                requestMono.subscribe(
                        clientResponse -> monoSink.success(clientResponse),
                        throwable -> monoSink.error(throwable));
            }).subscribeOn(RATE_LIMIT_SCHEDULER);
        } else {
            return requestMono;
        }
    }

    private Mono<ClientResponse> checkResponse(ClientResponse response) {
        if (!response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(GithubError.class)
                    .flatMap(errorResponse -> Mono.error(new GithubClientException(
                            "HTTP request failed with status: " + response.statusCode() +
                                    ": " + errorResponse.getMessage())));
        } else {
            return Mono.just(response);
        }
    }

    private <T> Flux<T> performPageableRequestRecursive(URI uri, Class<T> clazz, int pagesLimit) {
        return getRequestWrapped(uri, clazz)
                .flatMapMany(responseWrapper -> responseWrapper.getData()
                        .concatWith(pagesLimit == 0 ? Flux.empty() : responseWrapper.getNextLink()
                                .concatMap(link -> performPageableRequestRecursive(
                                        link.getUri(), clazz, pagesLimit - 1))));
    }

    /* flatMap never finishes when using rate limiter, concatMap finishes just fine.*/
    private <T> Flux<T> performPageableRequestParallelFlatMap(URI uri, Class<T> clazz, int pageLimit) {
        return getRequestWrapped(uri, clazz)
                .flatMapMany(responseWrapper -> responseWrapper.getData()
                        .mergeWith(responseWrapper.getAllPageUri(pageLimit, builderFactory)
                                .flatMap(pageLink -> getRequest(pageLink, clazz))));
    }

    private <T> Flux<T> performPageableRequestParallelConcatMap(URI uri, Class<T> clazz, int pageLimit) {
        return getRequestWrapped(uri, clazz)
                .flatMapMany(responseWrapper -> responseWrapper.getData()
                        .concatWith(responseWrapper.getAllPageUri(pageLimit, builderFactory)
                                .concatMap(pageLink -> getRequest(pageLink, clazz))));
    }

    private <T> Flux<T> performPageableRequestExpand(URI uri, Class<T> clazz) {
        return getRequestWrapped(uri, clazz)
                .expand(responseWrapper -> responseWrapper
                        .getNextLink()
                        .flatMap(link -> getRequestWrapped(link.getUri(), clazz)))
                .flatMap(GithubResponseWrapper::getData);
    }

}
