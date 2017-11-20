package net.voldrich.webclient.test;

import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ReactiveTests {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveTests.class);


    @BeforeEach
    void setUp() {
        log("Test Started");
    }

    @AfterEach
    void tearDown() {
        log("Test Finished");
    }

    @Test
    void testSubscribe() {
        Flux.range(1, 10).subscribe(this::log);
    }

    @Test
    void testToIterable() {
        Flux.range(1, 10).toIterable().forEach(this::log);
    }

    @Test
    void testDistinct() {
        Flux.range(1, 10)
                .mergeWith(Flux.range(1, 10))
                .distinct()
                .toIterable().forEach(this::log);
    }

    @Test
    void testToStream() throws InterruptedException {
        try {
            Flux.interval(Duration.ofMillis(100))
                    .map(aLong -> new Random().nextInt(1000))
                    .doOnNext(this::log)
                    .toStream()
                    .forEach(value -> {
                        if (value > 900) {
                            throw new UnsupportedOperationException("NOOO");
                        }
                    });
        } catch (Exception e) {
            log("Exception caught, but the original flux continues on");
            Thread.sleep(1000);
        }
    }

    @Test
    public void testRecursion() throws InterruptedException {
        Function<Integer, Integer> producer = iteration -> {
            if (iteration > 1000) {
                return null;
            }
            return Thread.currentThread().getStackTrace().length;
        };
        recursiveFlux(1, producer)
                .toIterable()
                .forEach(this::logReceived);
    }

    private Flux<String> recursiveFlux(int iteration, Function<Integer, Integer> producer) {
        return Flux.defer(() -> {
            Integer result = producer.apply(iteration);

            if (result == null) {
                return Flux.empty();
            } else {
                return Flux.concat(
                        Flux.just(String.format("%d - stack %d", iteration, result)),
                        recursiveFlux(iteration + 1, producer));
            }
        });
    }

    @Test
    public void testLoop() throws InterruptedException {

        Function<Integer, Mono<Integer>> producer = iteration -> {
            if (iteration > 10000) {
                return Mono.just(0);
            }
            return Mono.just(iteration);
        };

        Flux.create(fluxSink -> {
            for (int i = 1; i < 1000000; i++) {
                Integer result = producer.apply(i).block();
                if (result == 0) {
                    fluxSink.complete();
                } else {
                    fluxSink.next(result);
                }
            }
        }).subscribeOn(Schedulers.single())
                .toIterable()
                .forEach(this::logReceived);
    }

    private void log(Object data) {
        logger.info("{}", data);
    }

    private void logReceived(Object value) {
        logger.info("Received {}, on thread '{}'", value, Thread.currentThread().getName());
    }
}
