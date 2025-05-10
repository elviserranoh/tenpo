package org.tenpo.challenge.application.Create;

import org.tenpo.challenge.domain.model.OperationStatus;
import org.tenpo.challenge.domain.ports.in.bus.query.Response;

import java.math.BigDecimal;
import java.util.Map;

public record ResultOperationResponse(
        OperationStatus status,
        BigDecimal result,
        String message,
        String endpoint,
        Map<String, Object> parameters
) implements Response {
}
