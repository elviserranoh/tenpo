package org.tenpo.challenge.infrastructure.adapters.out.external;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.tenpo.challenge.domain.vo.DynamicPercentage;
import org.tenpo.challenge.infrastructure.adapters.out.external.dto.DynamicPercentageResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

@SpringBootTest
class PercentageServiceProviderAdapterTest {

    @Value("${spring.kafka.topic.history.created}")
    private static String topicName;

    private final static String DYNAMIC_PERCENTAGE_CACHE_KEY = "tenpo:percentage:dynamic";

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort()) // <-- Clave aquí
            .build();

    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("tenpodb")
            .withUsername("root")
            .withPassword("root");

    @Container
    @ServiceConnection
    static KafkaContainer kafkaContainer = new KafkaContainer("apache/kafka-native:3.8.0");

    @Bean
    public NewTopic historyTestTopic() {
        return new NewTopic(topicName, 1, (short) 1);
    }


    @Autowired
    private WebClient.Builder webClient;

    @Autowired
    private PercentageServiceProviderAdapter serviceProvider;

    @Autowired
    private ReactiveRedisTemplate<String, DynamicPercentageResponse> reactiveRedisTemplateMock;

    @Test
    void whenFetchDynamicPercentageThenReturnDynamicPercentageResponseWithStatus200() {

        Mono<DynamicPercentage> result = serviceProvider.fetchDynamicPercentage();

        StepVerifier.create(result).expectNextMatches(s -> {
            return Objects.nonNull(s.getValue());
        }).verifyComplete();

        Mono<DynamicPercentageResponse> redisResult = reactiveRedisTemplateMock.opsForValue().get(DYNAMIC_PERCENTAGE_CACHE_KEY);
        StepVerifier.create(redisResult).expectNextMatches(r -> {
            return Objects.nonNull(r.rate());
        }).verifyComplete();

    }

    @Test
    void whenFetchDynamicPercentageThenThrowExceptionWithStatus503() {

        String httpStatus = String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value());

        Mono<DynamicPercentage> result = serviceProvider.fetchDynamicPercentage(httpStatus);

        StepVerifier.create(result).expectNextMatches(s -> {
            return Objects.nonNull(s.getValue());
        }).verifyComplete();

        Mono<DynamicPercentageResponse> redisResult = reactiveRedisTemplateMock.opsForValue().get(DYNAMIC_PERCENTAGE_CACHE_KEY);
        StepVerifier.create(redisResult).expectNextMatches(r -> {
            return Objects.nonNull(r.rate());
        }).verifyComplete();

    }
}