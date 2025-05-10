package org.tenpo.challenge.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.tenpo.challenge.application.Create.OperationRequest;
import org.tenpo.challenge.application.Create.ResultOperationResponse;
import org.tenpo.challenge.application.Create.SumCalculatePercentageCommand;
import org.tenpo.challenge.application.Record.PaginatedResponse;
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

    @Operation(summary = "Calcular la suma de los dos operadores", description = "La funcion principal de este endpoint es sumar los dos operadores y aplicarle el porcentaje que se obtiene en segundo plano, adicional de forma asincrona se registra el resultado sea error o exito para llevar un control")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calculo realizado satisfactoriamente",
                    content = @Content(schema = @Schema(implementation = ResultOperationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ResultOperationResponse.class))),
    })
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

    @Operation(summary = "Solicitar el historial de las operaciones paginados", description = "Servir historial de llamadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitar historial de llamadas",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class)))
    })
    @GetMapping
    public Mono<ResponseEntity<?>> findAll(@RequestParam(name = "page") Integer page,
                                           @RequestParam(name = "size") Integer size) {

        RecordQuery query = new RecordQuery(page, size);
        
        return ask(query)
                .map(ResponseEntity::ok);
    }

}
