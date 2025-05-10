package org.tenpo.challenge.domain.event;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class HistoryRecordedEvent extends DomainEvent {

    private final UUID aggregateId;
    private final String endpoint;
    private final Map<String, Object> parameters;
    private final BigDecimal percentage;
    private final String messageOrError;

    public HistoryRecordedEvent(UUID aggregateId, String endpoint, Map<String, Object> parameters, BigDecimal percentage, String messageOrError) {
        this.aggregateId = aggregateId;
        this.endpoint = endpoint;
        this.parameters = parameters;
        this.percentage = percentage;
        this.messageOrError = messageOrError;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public String getMessageOrError() {
        return messageOrError;
    }

    @Override
    public String toString() {
        return "HistoryRecordedEvent{" +
                "aggregateId='" + aggregateId + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", parameters=" + parameters +
                ", percentage=" + percentage +
                ", messageOrError='" + messageOrError + '\'' +
                '}';
    }
}
