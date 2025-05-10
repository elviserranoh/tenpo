package org.tenpo.challenge.infrastructure.adapters.in.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import org.tenpo.challenge.domain.event.HistoryRecordedEvent;
import org.tenpo.challenge.domain.ports.in.bus.event.KafkaConsumerPort;
import org.tenpo.challenge.domain.ports.out.CallHistoryRepository;
import org.tenpo.challenge.infrastructure.mappers.CallHistoryMapper;
import org.tenpo.challenge.infrastructure.util.JsonUtil;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Objects;

@Slf4j
@Component
public class kafkaEventConsumerAdapter implements KafkaConsumerPort {

    private final ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate;
    private Disposable subscription;

    private final JsonUtil jsonUtil;
    private final CallHistoryRepository repository;
    private final CallHistoryMapper mapper;

    public kafkaEventConsumerAdapter(ReactiveKafkaConsumerTemplate<String, String> kafkaConsumerTemplate,
                                     JsonUtil jsonUtil,
                                     CallHistoryRepository repository,
                                     CallHistoryMapper mapper) {
        this.kafkaConsumerTemplate = kafkaConsumerTemplate;
        this.jsonUtil = jsonUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @PostConstruct
    public void initConsumer() {
        this.subscription = receive()
                .subscribe(
                        record -> {
                            log.info("Mensaje recibido: key={}, value={}, offset={}", record.key(), record.value(), record.offset());

                            HistoryRecordedEvent historyCreatedEvent = jsonUtil.jsonToObject(record.value(), HistoryRecordedEvent.class);

                            log.info("Mensaje: {}", historyCreatedEvent);

                            if (Objects.nonNull(historyCreatedEvent)) {
                                repository.save(this.mapper.historyCreatedEventToAggregate(historyCreatedEvent))
                                        .doOnSuccess(saved -> log.info("Historial guardado exitosamente: {}", saved))
                                        .doOnError(error -> log.error("Error al guardar historial", error))
                                        .subscribe();
                            }

                            record.receiverOffset().acknowledge(); // confirmación manual
                        },
                        error -> log.error("Error al consumir el mensaje de kafka", error),
                        () -> log.info("Flujo de consumo de kafka completado (esto usualmente no sucede a menos que el Flux termine)"));
    }

    public Flux<ReceiverRecord<String, String>> receive() {
        return kafkaConsumerTemplate
                .receive()
                .doOnNext(record -> {
                    log.info("Procesando registro: offset {}", record.offset());
                })
                .doOnError(error -> {
                    log.error("Error en el flujo de recepcion: ", error);
                });
    }

    @PreDestroy
    public void destroyConsumer() {
        if (this.subscription != null && !this.subscription.isDisposed()) {
            log.info("Deteniendo consumidor de kafka...");
            this.subscription.dispose();
            log.info("Consumidor de kafka detenido...");
        }
    }
}
