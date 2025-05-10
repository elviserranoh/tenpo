package org.tenpo.challenge.domain.ports.in.bus.query;

public interface Query<R extends Response> {
    Class<R> getResponseType();
}
