package net.voldrich.webclient.test;

import java.time.Duration;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.WorkQueueProcessor;
import reactor.util.function.Tuple2;

public class RateLimiter {

    private WorkQueueProcessor<Long> requestTicketQueue;

    private Disposable ticketGenerator;

    private Duration duration;

    public RateLimiter(Duration duration) {
        this.duration = duration;
        this.requestTicketQueue = WorkQueueProcessor.<Long>builder()
                .name("RequestTicketQueue")
                .bufferSize(1)
                .build();

        this.ticketGenerator = Flux
                .interval(duration)
                .subscribe(requestTicketQueue::onNext);
    }

    public <T> Mono<T> limitRate(Mono<T> request) {
        return request.zipWith(requestTicketQueue.next()).map(Tuple2::getT1);
    }
}
