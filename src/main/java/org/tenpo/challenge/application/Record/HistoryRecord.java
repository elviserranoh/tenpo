package org.tenpo.challenge.application.Record;

import org.springframework.stereotype.Service;
import org.tenpo.challenge.domain.entity.History;
import org.tenpo.challenge.domain.ports.out.CallHistoryRepository;
import reactor.core.publisher.Mono;

@Service
public class HistoryRecord {

    private final CallHistoryRepository repository;

    public HistoryRecord(CallHistoryRepository repository) {
        this.repository = repository;
    }

    public Mono<PaginatedResponse> findAllPaginated(RecordQuery query) {
        return repository.findAllPaginated(query.page(), query.size());
    }
}
