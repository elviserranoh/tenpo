package org.tenpo.challenge.domain.ports.in.bus.command;

import org.tenpo.challenge.domain.ports.in.bus.query.Response;
import reactor.core.publisher.Mono;

public interface CommandBus {
    <R extends Response, C extends Command<R>> Mono<R> dispatch(C command);
}
