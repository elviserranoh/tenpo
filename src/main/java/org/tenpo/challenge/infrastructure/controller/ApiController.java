package org.tenpo.challenge.infrastructure.controller;

import org.tenpo.challenge.domain.ports.in.bus.command.Command;
import org.tenpo.challenge.domain.ports.in.bus.command.CommandBus;
import org.tenpo.challenge.domain.ports.in.bus.query.Query;
import org.tenpo.challenge.domain.ports.in.bus.query.QueryBus;
import org.tenpo.challenge.domain.ports.in.bus.query.Response;
import reactor.core.publisher.Mono;

public abstract class ApiController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ApiController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    public Mono<? extends Response> dispatch(Command command) {
        return commandBus.dispatch(command);
    }

    public Mono<Response> ask(Query query) {
        return queryBus.ask(query);
    }
}
