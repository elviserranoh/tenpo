package org.tenpo.challenge.application.Record;

import org.springframework.stereotype.Component;
import org.tenpo.challenge.domain.ports.in.bus.query.QueryHandler;
import reactor.core.publisher.Mono;

@Component
public class HistoryRecordQueryHandler implements QueryHandler<RecordQuery, PaginatedResponse> {

    private final HistoryRecord service;

    public HistoryRecordQueryHandler(HistoryRecord service) {
        this.service = service;
    }

    @Override
    public Mono<PaginatedResponse> handle(RecordQuery query) {
        return service.findAllPaginated(query);
    }

}
