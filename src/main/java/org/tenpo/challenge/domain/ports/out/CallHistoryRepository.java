package org.tenpo.challenge.domain.ports.out;

import org.tenpo.challenge.application.Record.PaginatedResponse;
import org.tenpo.challenge.domain.entity.History;
import org.tenpo.challenge.domain.entity.SumPercentageCalculation;
import org.tenpo.challenge.domain.event.HistoryRecordedEvent;
import org.tenpo.challenge.domain.ports.in.bus.query.Response;
import org.tenpo.challenge.infrastructure.repository.entity.CallHistoryEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CallHistoryRepository {
    Mono<History> save(History aggregate);
    Mono<PaginatedResponse> findAllPaginated(int page, int size);
}
