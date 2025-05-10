package org.tenpo.challenge.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredOn;

    public DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
    }

    public UUID eventId() {
        return eventId;
    }

    public LocalDateTime occurredOn() {
        return occurredOn;
    }

}
