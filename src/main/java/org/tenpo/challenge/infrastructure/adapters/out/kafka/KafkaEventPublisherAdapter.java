package org.tenpo.challenge.infrastructure.adapters.out.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import org.tenpo.challenge.domain.event.DomainEvent;
import org.tenpo.challenge.domain.event.HistoryRecordedEvent;
import org.tenpo.challenge.domain.ports.out.EventPublisherPort;
import org.tenpo.challenge.infrastructure.util.JsonUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.List;

@Slf4j
@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;
    private final JsonUtil jsonUtil;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.topic.history.created}")
    private String historyCreatedTopic;


    public KafkaEventPublisherAdapter(ReactiveKafkaProducerTemplate<String, String> kafkaTemplate, JsonUtil jsonUtil) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public Mono<Void> publish(List<? extends DomainEvent> events) {

        if(events == null || events.isEmpty()) {
            log.error("No hay eventos para publicar");
            return Mono.empty();
        }
        log.info("Publicando {} eventos", events.size());
        return Flux.fromIterable(events)
                .flatMap(this::sendEventToKafka)
                .doOnError(e -> log.error("Error al publicar uno o mas eventos", e))
                .then(); // then() espera a que todos los Monos completen
    }

    @Override
    public Mono<Void> publish(DomainEvent event) {
        if(event == null) {
            log.error("No hay evento para publicar");
            return Mono.empty();
        }
        log.info("Publicando evento: {} ", event);
        return Mono.just(event)
                .flatMap(this::sendEventToKafka)
                .doOnError(e -> log.error("Error al publicar el evento: {}", event.getClass().getSimpleName(), e))
                .then();
    }

    private <E extends DomainEvent> Mono<SenderResult<Void>> sendEventToKafka(E event) {

        String topic = determineTopicForEvent(event);
        if(topic == null) {
            log.error("No se pudo determinar el topico para el evento: {}. El evento no sera publicado", event.getClass().getSimpleName());
            return Mono.empty();
        }

        String eventPayload = jsonUtil.objectToJson(event);
        String eventKey = extractEventKey(event); //

        log.info("Publicando evento {} con clave: {}, al topic: {}", event.getClass().getCanonicalName(), eventKey, topic);

        try {
            return kafkaTemplate.send(topic, eventKey, eventPayload)
                    .doOnSuccess(sendResult -> log.info("Evento {} enviado exitosamente a Kafka. Topico: {}, Particion: {}, Offset: {}",
                            event.getClass().getSimpleName(),
                            sendResult.recordMetadata().topic(),
                            sendResult.recordMetadata().partition(),
                            sendResult.recordMetadata().offset()))
                    .doOnError(error -> log.error("Error al enviar evento {} al topico {}", event.getClass().getSimpleName(), topic, error));

        } catch (Exception e) {
            log.error("Error de serializacion JSON para el evento {}", event.getClass().getSimpleName(), e);
            return Mono.error(e);
        }
    }

    private String determineTopicForEvent(DomainEvent event) {
        if(event instanceof HistoryRecordedEvent) {
            return historyCreatedTopic;
        }
        return null; // O un topic por defecto para eventos no mapeados
    }

    private <E extends DomainEvent> String extractEventKey(E event) {
        if(event instanceof HistoryRecordedEvent) {
            return ((HistoryRecordedEvent) event).getAggregateId().toString();
        }
        return null;
    }
}
