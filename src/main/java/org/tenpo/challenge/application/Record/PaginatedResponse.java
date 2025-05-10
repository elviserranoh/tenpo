package org.tenpo.challenge.application.Record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tenpo.challenge.domain.entity.History;
import org.tenpo.challenge.domain.ports.in.bus.query.Response;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse implements Response {
    private List<? extends History> content;
    private int page;
    private int size;
    private long totalElements;
}
