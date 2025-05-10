package org.tenpo.challenge.infrastructure.repository.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Table("call_history")
public class CallHistoryEntity {

    @Id
    private UUID id;
    private String endpoint;
    private String parameters;
    private String messageOrError;

}
