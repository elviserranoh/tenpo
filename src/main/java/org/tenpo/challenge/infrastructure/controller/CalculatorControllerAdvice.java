package org.tenpo.challenge.infrastructure.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.tenpo.challenge.application.Create.ResultOperationResponse;
import org.tenpo.challenge.domain.exception.BusinessValidationException;
import org.tenpo.challenge.domain.model.OperationStatus;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class CalculatorControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessValidationException.class)
    public final Mono<ResponseEntity<ResultOperationResponse>> handleBusinessValidationException(BusinessValidationException ex) {

        ResultOperationResponse response = new ResultOperationResponse(
                ex.getStatus(),
                null,
                ex.getMessage(),
                ex.getEndpoint(),
                ex.getParameters()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public final Mono<ResponseEntity<ResultOperationResponse>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ResultOperationResponse response = new ResultOperationResponse(
                OperationStatus.ERROR,
                null,
                ex.getMessage(),
                null,
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(Exception.class)
    public final Mono<ResponseEntity<ResultOperationResponse>> handleAnyException(Exception ex) {
        ResultOperationResponse response = new ResultOperationResponse(
                OperationStatus.ERROR,
                null,
                ex.getMessage(),
                null,
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

}
