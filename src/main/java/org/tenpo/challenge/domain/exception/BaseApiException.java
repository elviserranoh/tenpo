package org.tenpo.challenge.domain.exception;

import org.tenpo.challenge.domain.model.OperationStatus;

import java.time.Instant;
import java.util.Map;

public class BaseApiException extends RuntimeException {

    private String endpoint;
    private OperationStatus status;
    private Map<String, Object> parameters;
    private Instant timestamp;

    public BaseApiException(String endpoint, OperationStatus status, String message, Map<String, Object> parameters, Instant timestamp) {
        super(message);
        this.endpoint = endpoint;
        this.parameters = parameters;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "BaseApiException{" +
                "endpoint='" + endpoint + '\'' +
                "message='" + super.getMessage() + '\'' +
                ", status=" + status +
                ", parameters=" + parameters +
                ", timestamp=" + timestamp +
                '}';
    }
}
