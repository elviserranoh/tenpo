package org.tenpo.challenge.domain.ports.out;

import org.tenpo.challenge.domain.vo.DynamicPercentage;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface PercentageBackend {
    Mono<DynamicPercentage> fetchDynamicPercentage();
    Mono<DynamicPercentage> fetchDynamicPercentage(String httpStatusCode);
}
