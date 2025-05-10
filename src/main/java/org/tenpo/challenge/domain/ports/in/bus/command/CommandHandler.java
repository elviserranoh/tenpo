package org.tenpo.challenge.domain.ports.in.bus.command;

import org.tenpo.challenge.domain.ports.in.bus.query.Response;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command<R>, R extends Response> {
    Mono<R> handle(T command);
    Class<T> getCommandClass();
}
