package org.tenpo.challenge.application.Record;

import org.tenpo.challenge.domain.ports.in.bus.query.Query;

public record RecordQuery(int page, int size) implements Query<PaginatedResponse> {
    @Override
    public Class<PaginatedResponse> getResponseType() {
        return PaginatedResponse.class;
    }
}
