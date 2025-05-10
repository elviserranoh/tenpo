package org.tenpo.challenge.infrastructure.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import org.tenpo.challenge.application.Record.PaginatedResponse;
import org.tenpo.challenge.domain.entity.History;
import org.tenpo.challenge.domain.ports.out.CallHistoryRepository;
import org.tenpo.challenge.infrastructure.mappers.CallHistoryMapper;
import org.tenpo.challenge.infrastructure.repository.entity.CallHistoryEntity;
import reactor.core.publisher.Mono;

@Component
public class PostgresCallHistoryRepository implements CallHistoryRepository {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final CallHistoryMapper mapper;

    public PostgresCallHistoryRepository(R2dbcEntityTemplate r2dbcEntityTemplate, CallHistoryMapper mapper) {
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.mapper = mapper;
    }

    @Override
    public Mono<History> save(History aggregate) {
        return this.r2dbcEntityTemplate
                .insert(this.mapper.historyToCallHistoryEntity(aggregate))
                .flatMap(result -> Mono.just(this.mapper.historyEntityToHistoryAggregate(result)));
    }

    @Override
    public Mono<PaginatedResponse> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Criteria criteria = Criteria.empty();
        Query query = Query.query(criteria).with(pageable);
        Query queryConteo = Query.query(criteria);

        return r2dbcEntityTemplate.select(query, CallHistoryEntity.class)
                .collectList()
                .zipWith(r2dbcEntityTemplate.count(queryConteo, CallHistoryEntity.class))
                .map(tupla -> new PaginatedResponse(
                        mapper.listHistoryEntityToHistoryList(tupla.getT1()),
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        tupla.getT2())
                );
    }
}
