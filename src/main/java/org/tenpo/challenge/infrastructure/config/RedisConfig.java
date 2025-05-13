package org.tenpo.challenge.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.tenpo.challenge.infrastructure.adapters.out.external.dto.DynamicPercentageResponse;

@Configuration
public class RedisConfig {


    @Value("${spring.data.redis.host:redis}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public ReactiveRedisTemplate<String, DynamicPercentageResponse> reactiveRedisTemplateForDynamicPercentage(ReactiveRedisConnectionFactory factory) {

        // Jackson2JsonRedisSerializar convierte tu objeto DynamicPercentage a JSON y viceversa
        Jackson2JsonRedisSerializer<DynamicPercentageResponse> serializer =
                new Jackson2JsonRedisSerializer<>(DynamicPercentageResponse.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, DynamicPercentageResponse> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, DynamicPercentageResponse> context = builder.value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);

    }

}
