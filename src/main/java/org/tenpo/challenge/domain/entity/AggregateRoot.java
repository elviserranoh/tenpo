package org.tenpo.challenge.domain.entity;

import org.tenpo.challenge.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot {

    private final List<DomainEvent> uncommittedEvents;

    public AggregateRoot() {
        this.uncommittedEvents = new ArrayList<>();
    }

    public void record(DomainEvent event) {
        this.uncommittedEvents.add(event);
    }

    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }

    public void markChangesAsCommitted() {
        this.uncommittedEvents.clear();
    }

}
