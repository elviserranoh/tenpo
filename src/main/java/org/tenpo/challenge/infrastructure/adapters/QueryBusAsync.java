package org.tenpo.challenge.infrastructure.adapters;

import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.tenpo.challenge.domain.ports.in.bus.command.Command;
import org.tenpo.challenge.domain.ports.in.bus.command.CommandHandler;
import org.tenpo.challenge.domain.ports.in.bus.query.Query;
import org.tenpo.challenge.domain.ports.in.bus.query.QueryBus;
import org.tenpo.challenge.domain.ports.in.bus.query.QueryHandler;
import org.tenpo.challenge.domain.ports.in.bus.query.Response;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Objects;

@Component
@SuppressWarnings("unchecked")
public class QueryBusAsync implements QueryBus {

    private final ApplicationContext applicationContext;

    public QueryBusAsync(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <R extends Response, Q extends Query<R>> Mono<R> ask(Q query) {

        if (Objects.isNull(query)) {
            throw new IllegalArgumentException("Query cannot be null");
        }

        QueryHandler<Q, R> handler = firstHandlerForQuery(query);

        if (Objects.isNull(handler)) {
            return Mono.error(new IllegalArgumentException(String.format("QueryHandler no registered to %s", query.getClass().getName())));
        }

        // Ejecuta el handler de forma asincrona usando un Scheduler de Reactor
        // handler.handle(Query) devuelve Mono<Void>
        // .subscribeOn() asegura que la ejecucion del handle ocurrra en un hilo del Scheduler especificado.
        return (Mono<R>) handler.handle(query)
                // Usar Schedulers.boundedElastic() es una buena opcion por defecto para tareas
                // que podrian ser I/O bound o bloquear ligeramente, ya que gestiona un pool
                // de hilos elastico pero limitado. Evita bloquear el Event Loop
                .subscribeOn(Schedulers.boundedElastic());

    }

    private <Q extends Query<R>, R extends Response> QueryHandler<Q, R> firstHandlerForQuery(Q query) {
        ResolvableType handlerType = ResolvableType.forClassWithGenerics(
                QueryHandler.class,
                ResolvableType.forClass(query.getClass()),
                ResolvableType.forClass(query.getResponseType())
        );

        String[] beanNames = applicationContext.getBeanNamesForType(handlerType);

        if (beanNames.length == 0) {
            return null;
        }

        if (beanNames.length > 1) {
            System.err.println("Se ha encontrado varios queryhandler con la misma query: " + query.getClass().getName() + ". se utilizara solo la primera coincidencia: " + Arrays.toString(beanNames));
        }

        return (QueryHandler<Q, R>) applicationContext.getBean(beanNames[0]);
    }


}
