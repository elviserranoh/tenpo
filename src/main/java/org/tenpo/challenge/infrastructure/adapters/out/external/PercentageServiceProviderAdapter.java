package org.tenpo.challenge.infrastructure.adapters.out.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tenpo.challenge.domain.exception.BusinessValidationException;
import org.tenpo.challenge.domain.model.OperationStatus;
import org.tenpo.challenge.domain.vo.DynamicPercentage;
import org.tenpo.challenge.domain.ports.out.PercentageBackend;
import org.tenpo.challenge.infrastructure.adapters.out.external.dto.DynamicPercentageResponse;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class PercentageServiceProviderAdapter implements PercentageBackend {

    private final String percentageUrl;
    private String httpStatusCodeMock;

    // Clave constante para el porcentaje en cache
    private final static String DYNAMIC_PERCENTAGE_CACHE_KEY = "tenpo:percentage:dynamic";

    // Duracion de la cache
    private static final Duration DYNAMIC_PERCENTAGE_CACHE_DURATION = Duration.ofMinutes(5);

    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, DynamicPercentageResponse> redisTemplate;

    public PercentageServiceProviderAdapter(WebClient.Builder builder,
                                            @Value("${percentage.service.url}") String percentageUrl,
                                            ReactiveRedisTemplate<String, DynamicPercentageResponse> reactiveRedisTemplate
    ) {
        this.redisTemplate = reactiveRedisTemplate;
        this.webClient = builder.baseUrl(percentageUrl).build();
        this.percentageUrl = percentageUrl;
    }

    @Override
    public Mono<DynamicPercentage> fetchDynamicPercentage() {
        this.setHttpStatusCodeMock(String.valueOf(HttpStatus.OK.value()));
        return getDynamicPercentageMono();
    }

    @Override
    public Mono<DynamicPercentage> fetchDynamicPercentage(String httpStatusCode) {
        this.setHttpStatusCodeMock(httpStatusCode);
        return getDynamicPercentageMono();
    }

    private Mono<DynamicPercentage> getDynamicPercentageMono() {
        return fetchAndCachePercentage()
                .onErrorResume(error -> redisTemplate.opsForValue().get(DYNAMIC_PERCENTAGE_CACHE_KEY)
                        .doOnNext(cachePercentage -> log.info("Obteniendo el porcentaje de la cache, clave: {}, valor: {}", DYNAMIC_PERCENTAGE_CACHE_KEY, cachePercentage))
                        .switchIfEmpty(Mono.error(
                                new BusinessValidationException(
                                        String.format("%s/get/percentages", percentageUrl),
                                        OperationStatus.ERROR,
                                        "Error al intentar buscar el porcentaje en el microservicio externo, ademas no se encontro el porcentaje en la cache: " + error.getMessage(),
                                        Map.of(),
                                        Instant.now())
                        )))
                .flatMap(percentage -> Mono.just(new DynamicPercentage(BigDecimal.valueOf(percentage.rate()))));
    }

    private Mono<DynamicPercentageResponse> fetchAndCachePercentage() {
        return fetchExternalPercentage()
                .flatMap(fetchedPercentage -> {
                    return redisTemplate.opsForValue()
                            .set(DYNAMIC_PERCENTAGE_CACHE_KEY, fetchedPercentage, DYNAMIC_PERCENTAGE_CACHE_DURATION)
                            .doOnError(cacheWriteError ->
                                    log.warn("Ha fallado el intento de escribir en redis: clave {}. Error: {}",
                                            DYNAMIC_PERCENTAGE_CACHE_KEY, cacheWriteError.getMessage(), cacheWriteError)
                            )
                            .thenReturn(fetchedPercentage) // Devuelve el porcentaje obtenido incluso si guarda el guardado en cache
                            .onErrorReturn(fetchedPercentage); // Asegurarse de devolver el valor asi falle
                });
    }

    private Mono<DynamicPercentageResponse> fetchExternalPercentage() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/get/percentages").build())
                .header("X-MOCK", httpStatusCodeMock)
                .retrieve()
                .bodyToMono(DynamicPercentageResponse.class)
                .flatMap(response -> {
                    if (Objects.isNull(response) || Objects.isNull(response.rate())) {
                        log.error("Servicio externo ha respondido null");
                        return Mono.error(new IllegalStateException("Servicio externo ha respondido null"));
                    }
                    log.info("Porcentaje obtenido del servicio externo: {}", response);
                    return Mono.just(response);
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)) // Reintentar 3 veces con backoff exponencial empezando en 1 segundo
                        .filter(throwable ->
                                throwable instanceof WebClientResponseException.ServiceUnavailable |
                                        throwable instanceof WebClientResponseException.GatewayTimeout
                        )
                        .doBeforeRetry(retrySignal -> log.warn("Reintentando... Intento # {}", retrySignal.totalRetriesInARow()))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            // Esta es la excepcion final si todos los reintentos fallan
                            return retrySignal.failure();
                        }))
                .doOnError(error ->
                        log.error("Error al intentar buscar el porcentaje en el microservicio externo: {}", error.getMessage())
                );
    }

    public String getHttpStatusCodeMock() {
        return httpStatusCodeMock;
    }

    public void setHttpStatusCodeMock(String httpStatusCodeMock) {
        this.httpStatusCodeMock = httpStatusCodeMock;
    }
}
