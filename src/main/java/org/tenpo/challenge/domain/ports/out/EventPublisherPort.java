package org.tenpo.challenge.domain.ports.out;

import org.tenpo.challenge.domain.event.DomainEvent;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventPublisherPort {
    Mono<Void> publish(List<? extends DomainEvent> events);
    Mono<Void> publish(DomainEvent event);
}
