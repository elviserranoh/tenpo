package org.tenpo.challenge.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tenpo.challenge.domain.exception.BusinessValidationException;
import org.tenpo.challenge.domain.model.OperationStatus;
import org.tenpo.challenge.domain.vo.HistoryUrl;
import org.tenpo.challenge.domain.vo.OperandValueObject;
import org.tenpo.challenge.domain.vo.PercentageRateValue;
import org.tenpo.challenge.domain.vo.SumPercentageCalculationId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@Builder
public class SumPercentageCalculation extends AggregateRoot {

    private static final Logger log = LoggerFactory.getLogger(SumPercentageCalculation.class);

    private final SumPercentageCalculationId id;
    private final OperandValueObject firstOperand;
    private final OperandValueObject secondOperand;
    private PercentageRateValue appliedRate;
    private OperandValueObject result;
    private final HistoryUrl url;

    public SumPercentageCalculation(SumPercentageCalculationId id, OperandValueObject firstOperand, OperandValueObject secondOperand, HistoryUrl url) {

        if (id == null || firstOperand == null || secondOperand == null || url == null) {
            throw new IllegalArgumentException("Los parametros de creacion no pueden ser nulos.");
        }

        this.id = id;
        this.firstOperand = firstOperand;
        this.secondOperand = secondOperand;
        this.url = url;

        log.info("Agregado SumPercentageCalculation creado con ID: {}", this.id.value());
    }

    public void completeCalculationWithPercentage(PercentageRateValue externalPercentage) {

        if (externalPercentage == null) {
            log.error("El porcentaje externo no puede ser nulo para completar el calculo. ID: {}", this.id.value());
            throw new BusinessValidationException(
                    this.url().value(),
                    OperationStatus.ERROR,
                    "El porcentaje externo no puede ser nulo para completar el calculo.",
                    Map.of("firstNumber", this.firstOperand(), "secondNumber", this.secondOperand()),
                    Instant.now());
        }

        log.debug("Aplicando porcentaje externo {} al calculo {}", externalPercentage.getValue(), this.id.value());

        this.applyRateAndCalculate(externalPercentage);

        if (this.result == null || this.appliedRate == null) {
            log.error("El resultado o el porcentaje no se establecieron despues del calculo para el ID: {}", this.id.value());
            throw new BusinessValidationException(
                    this.url().value(),
                    OperationStatus.ERROR,
                    String.format("El resultado o el porcentaje no se establecieron despues del calculo para el ID: %s", this.id.value()),
                    Map.of("firstNumber", this.firstOperand(), "secondNumber", this.secondOperand()),
                    Instant.now());
        }

//        HistoryRecordedEvent event = new HistoryRecordedEvent(
//                this.id.value(),
//                this.url.value(),
//                Map.of("firstNumber", this.firstOperand(), "secondNumber", this.secondOperand()),
//                this.appliedRate.getValue(),
//                this.result().getValue().toString()
//        );
//
//        this.record(event);
//
//        log.info("Evento HistoryCreatedEvent registrado para el calculo ID: {}", this.id.value());

    }

    private void applyRateAndCalculate(PercentageRateValue rowRate) {
        BigDecimal percentageMultiplier = rowRate.getValue()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN)
                .add(BigDecimal.ONE);

        this.appliedRate = new PercentageRateValue(percentageMultiplier);

        BigDecimal sumOfOperands = this.firstOperand.getValue().add(this.secondOperand().getValue());
        BigDecimal calculatedResult = sumOfOperands
                .multiply(this.appliedRate().getValue())
                .setScale(2, RoundingMode.HALF_EVEN);

        this.result = new OperandValueObject(calculatedResult);
        log.debug("Calculo realizado para ID: {}. Suma: {}, Porcentaje: {}, Resultado: {}",
                this.id.value(), sumOfOperands, this.appliedRate.getValue(), this.result.getValue());
    }

    public SumPercentageCalculationId id() {
        return id;
    }

    public OperandValueObject firstOperand() {
        return firstOperand;
    }

    public OperandValueObject secondOperand() {
        return secondOperand;
    }

    public PercentageRateValue appliedRate() {
        return appliedRate;
    }

    public OperandValueObject result() {
        return result;
    }

    public HistoryUrl url() {
        return url;
    }
}
