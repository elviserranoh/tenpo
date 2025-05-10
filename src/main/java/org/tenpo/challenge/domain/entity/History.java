package org.tenpo.challenge.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {

    private UUID id;
    private String endpoint;
    private Map<String, Object> parameters;
    private String messageOrError;

}
