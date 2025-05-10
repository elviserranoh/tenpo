package org.tenpo.challenge.infrastructure.mappers;

import org.springframework.stereotype.Component;
import org.tenpo.challenge.domain.entity.History;
import org.tenpo.challenge.domain.event.HistoryRecordedEvent;
import org.tenpo.challenge.infrastructure.repository.entity.CallHistoryEntity;
import org.tenpo.challenge.infrastructure.util.JsonUtil;

import java.util.List;


@Component
public class CallHistoryMapper {

    private final JsonUtil jsonUtil;

    public CallHistoryMapper(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    public History historyCreatedEventToAggregate(HistoryRecordedEvent event) {
        return History.builder()
                .id(event.getAggregateId())
                .endpoint(event.getEndpoint())
                .messageOrError(event.getMessageOrError())
                .parameters(event.getParameters())
                .build();
    }

    public CallHistoryEntity historyToCallHistoryEntity(History aggregate) {
        return CallHistoryEntity.builder()
                .id(aggregate.getId())
                .messageOrError(aggregate.getMessageOrError())
                .endpoint(aggregate.getEndpoint())
                .parameters(jsonUtil.objectToJson(aggregate.getParameters()))
                .build();
    }

    public List<History> listHistoryEntityToHistoryList(List<CallHistoryEntity> callHistoryEntities) {
        return callHistoryEntities.stream().map(this::historyEntityToHistoryAggregate).toList();
    }

    public History historyEntityToHistoryAggregate(CallHistoryEntity entity) {
        return History.builder()
                .id(entity.getId())
                .messageOrError(entity.getMessageOrError())
                .endpoint(entity.getEndpoint())
                .parameters(jsonUtil.jsonToMap(entity.getParameters()))
                .build();
    }
}
