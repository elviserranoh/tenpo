package org.tenpo.challenge.infrastructure.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.tenpo.challenge.application.Create.OperationRequest;
import org.tenpo.challenge.application.Create.SumCalculatePercentageCommand;
import org.tenpo.challenge.application.Record.RecordQuery;
import org.tenpo.challenge.domain.ports.in.bus.command.CommandBus;
import org.tenpo.challenge.domain.ports.in.bus.query.QueryBus;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "calculation")
public class CalculatorController extends ApiController {

    public CalculatorController(CommandBus commandBus, QueryBus queryBus) {
        super(commandBus, queryBus);
    }

    @PostMapping
    public Mono<ResponseEntity<?>> calculatePercentage(@RequestBody OperationRequest request, ServerWebExchange exchange) {

        SumCalculatePercentageCommand command = new SumCalculatePercentageCommand(
                exchange.getRequest().getPath().value(),
                request.firstNumber(),
                request.secondNumber()
        );

        return dispatch(command)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<?>> findAll(@RequestParam(name = "page") Integer page,
                                           @RequestParam(name = "size") Integer size) {

        RecordQuery query = new RecordQuery(page, size);
        
        return ask(query)
                .map(ResponseEntity::ok);
    }

}
