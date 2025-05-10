package org.tenpo.challenge.application.Create;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tenpo.challenge.domain.entity.SumPercentageCalculation;
import org.tenpo.challenge.domain.event.HistoryRecordedEvent;
import org.tenpo.challenge.domain.exception.BusinessValidationException;
import org.tenpo.challenge.domain.model.OperationStatus;
import org.tenpo.challenge.domain.ports.in.bus.command.CommandHandler;
import org.tenpo.challenge.domain.ports.out.EventPublisherPort;
import org.tenpo.challenge.domain.ports.out.PercentageBackend;
import org.tenpo.challenge.domain.vo.HistoryUrl;
import org.tenpo.challenge.domain.vo.OperandValueObject;
import org.tenpo.challenge.domain.vo.PercentageRateValue;
import org.tenpo.challenge.domain.vo.SumPercentageCalculationId;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class SumCalculatePercentageCommandHandler implements CommandHandler<SumCalculatePercentageCommand, ResultOperationResponse> {

    private final PercentageBackend percentageBackend;
    private final EventPublisherPort eventPublisher;

    public SumCalculatePercentageCommandHandler(PercentageBackend percentageBackend, EventPublisherPort eventPublisher) {
        this.percentageBackend = percentageBackend;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<ResultOperationResponse> handle(SumCalculatePercentageCommand command) {

        SumPercentageCalculation aggregate = new SumPercentageCalculation(
                new SumPercentageCalculationId(UUID.randomUUID()),
                new OperandValueObject(command.firstNumber()),
                new OperandValueObject(command.secondNumber()),
                new HistoryUrl(command.endpoint())
        );

        return percentageBackend.fetchDynamicPercentage()
                .flatMap(percentageRate -> {

                    log.debug("Porcentaje obtenido del servicio externo: {}",
                            percentageRate.getValue());

                    try {
                        aggregate.completeCalculationWithPercentage(new PercentageRateValue(percentageRate.getValue()));
                    } catch (IllegalStateException | IllegalArgumentException ex) {
                        log.error("Error en la logica del agregado {}: {}", aggregate.id().value(), ex.getMessage());
                        BusinessValidationException exception = new BusinessValidationException(
                                command.endpoint(),
                                OperationStatus.ERROR,
                                "Error en la logica del agregado." + ex.getMessage(),
                                Map.of("firstNumber", command.firstNumber(), "secondNumber", command.secondNumber()),
                                Instant.now());

                        return this.eventPublisher.publish(
                                new HistoryRecordedEvent(
                                        aggregate.id().value(),
                                        aggregate.url().value(),
                                        Map.of("firstNumber", command.firstNumber(), "secondNumber", command.secondNumber()),
                                        aggregate.appliedRate().getValue(),
                                        "Error en la logica del agregado." + ex.getMessage()
                                )
                        ).then(Mono.error(exception));
                    }

                    return Mono.justOrEmpty(aggregate.result())
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("El resultado es nulo despues de un calculo aparentemente exitoso para ID: {}", aggregate.id().value());
                                return Mono.error(new BusinessValidationException(
                                        command.endpoint(),
                                        OperationStatus.ERROR,
                                        "Error interno: el resultado del calculo no esta disponible.",
                                        Map.of("firstNumber", command.firstNumber(), "secondNumber", command.secondNumber()),
                                        Instant.now()));
                            }))
                            .flatMap(result -> {
                                ResultOperationResponse successResponse = new ResultOperationResponse(
                                        OperationStatus.SUCCESS,
                                        aggregate.result().getValue(),
                                        "Calculo realizado con exito.",
                                        command.endpoint(),
                                        Map.of("firstNumber", command.firstNumber(), "secondNumber", command.secondNumber())
                                );

                                log.info("#### ESTE ES EL RESPONSE QUE SE LE ESTA ENVIANDO AL USUARIO: {}",successResponse);

                                return this.eventPublisher.publish(
                                        new HistoryRecordedEvent(
                                                aggregate.id().value(),
                                                aggregate.url().value(),
                                                Map.of("firstNumber", command.firstNumber(), "secondNumber", command.secondNumber()),
                                                aggregate.appliedRate().getValue(),
                                                String.format("Calculo realizado con exito. result: {%s}", aggregate.result().getValue())
                                        )
                                ).thenReturn(successResponse);

//                                return this.eventPublisher.publish(aggregate.getUncommittedEvents())
//                                        .doOnSuccess(uncommittedEvent -> {
//                                            log.info("Eventos publicados exitosamente");
//                                            aggregate.markChangesAsCommitted();
//                                        })
//                                        .doOnError(e -> {
//                                            log.error("Error al publicar eventos");
//                                        }).thenReturn(successResponse);
                            });
                })
                .onErrorResume(BusinessValidationException.class, ex -> Mono.error(new BusinessValidationException(
                        ex.getEndpoint(),
                        OperationStatus.ERROR,
                        ex.getMessage(),
                        Map.of("firstNumber", command.firstNumber(), "secondNumber", command.secondNumber()),
                        ex.getTimestamp())));
    }

    @Override
    public Class<SumCalculatePercentageCommand> getCommandClass() {
        return SumCalculatePercentageCommand.class;
    }

}
