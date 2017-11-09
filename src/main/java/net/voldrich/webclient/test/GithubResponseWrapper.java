package net.voldrich.webclient.test;

import java.net.URI;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriBuilderFactory;

import reactor.core.publisher.Flux;

public class GithubResponseWrapper<T> {

    private static final Pattern LAST_PAGE_PATTERN = Pattern.compile("[\\?&]page=([0-9]*)");

    private ClientResponse response;

    private ClientResponse.Headers headers;

    private Class<T> clazz;

    public GithubResponseWrapper(ClientResponse clientResponse, Class<T> clazz) {
        this.response = clientResponse;
        this.headers = clientResponse.headers();
        this.clazz = clazz;
    }

    public Flux<T> getData() {
        return response.bodyToFlux(clazz);
    }

    public Set<Link> getLinks() {
        return Link.parseLinks(headers.header(HttpHeaders.LINK));
    }

    public Flux<Link> getLinkWithRelName(String name) {
        return Flux.fromIterable(getLinks())
                .filter(link -> link.getRel().equalsIgnoreCase(name));
    }

    /**
     * Derives URI's of all pages from last page URI by generating a sequence of URI's
     * with page numbers from 1 to last page number.
     */
    public Flux<URI> getAllPageUri(int pagesLimit, UriBuilderFactory builderFactory) {
        return getLinkWithRelName("last").flatMap(link -> {
            String last = link.getUri().toString();
            Matcher m = LAST_PAGE_PATTERN.matcher(last);
            if (m.find()) {
                String paramWithValue = m.group(0);
                int lastPageNumber = Integer.parseInt(m.group(1));
                String pageTemplate = last.replace(paramWithValue,
                        paramWithValue.replace(String.valueOf(lastPageNumber), "{page}"));

                return Flux.range(1, Math.min(lastPageNumber, pagesLimit))
                        .map(pageNumber -> builderFactory.uriString(pageTemplate).build(pageNumber));
            } else {
                throw new RuntimeException("Failed to parse last page link");
            }
        });
    }
}
