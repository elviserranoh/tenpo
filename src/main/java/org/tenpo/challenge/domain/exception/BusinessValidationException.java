package org.tenpo.challenge.domain.exception;

import org.tenpo.challenge.domain.model.OperationStatus;

import java.time.Instant;
import java.util.Map;

public class BusinessValidationException extends BaseApiException{
    public BusinessValidationException(String endpoint, OperationStatus status, String message, Map<String, Object> parameters, Instant timestamp) {
        super(endpoint, status, message, parameters, timestamp);
    }
}
