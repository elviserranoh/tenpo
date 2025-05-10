package org.tenpo.challenge.domain.ports.in.bus.command;

import org.tenpo.challenge.domain.ports.in.bus.query.Response;

public interface Command<R extends Response> {
    Class<R> getResponseType();
}
