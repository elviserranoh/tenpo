package org.tenpo.challenge.domain.ports.in.bus.query;

import reactor.core.publisher.Mono;

public interface QueryHandler<Q extends Query<R>, R extends Response> {
    Mono<R> handle(Q query);
}
