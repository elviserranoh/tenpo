package org.tenpo.challenge.domain.ports.in.bus.query;

import reactor.core.publisher.Mono;

public interface QueryBus {
    <R extends Response, Q extends Query<R>> Mono<R> ask(Q query);
}

