package org.tenpo.challenge.application.Create;

import org.tenpo.challenge.domain.ports.in.bus.command.Command;

import java.math.BigDecimal;

public record SumCalculatePercentageCommand(
        String endpoint,
        BigDecimal firstNumber,
        BigDecimal secondNumber) implements Command<ResultOperationResponse> {

    @Override
    public Class<ResultOperationResponse> getResponseType() {
        return ResultOperationResponse.class;
    }

}
