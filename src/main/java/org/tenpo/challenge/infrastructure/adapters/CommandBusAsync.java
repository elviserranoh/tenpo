package org.tenpo.challenge.infrastructure.adapters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.tenpo.challenge.domain.ports.in.bus.command.Command;
import org.tenpo.challenge.domain.ports.in.bus.command.CommandBus;
import org.tenpo.challenge.domain.ports.in.bus.command.CommandHandler;
import org.tenpo.challenge.domain.ports.in.bus.query.Response;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Component
@SuppressWarnings("unchecked")
public class CommandBusAsync implements CommandBus {

    private final ApplicationContext applicationContext;

    public CommandBusAsync(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <R extends Response, C extends Command<R>> Mono<R> dispatch(C command) {

        if (Objects.isNull(command)) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        CommandHandler<C, R> handler = firstHandlerForCommand(command);

        if (Objects.isNull(handler)) {
            return Mono.error(new IllegalArgumentException(String.format("CommandHandler no registered to %s", command.getClass().getName())));
        }

        // Ejecuta el handler de forma asincrona usando un Scheduler de Reactor
        // handler.handle(command) devuelve Mono<Void>
        // .subscribeOn() asegura que la ejecucion del handle ocurrra en un hilo del Scheduler especificado.
        return (Mono<R>) handler.handle(command)
                // Usar Schedulers.boundedElastic() es una buena opcion por defecto para tareas
                // que podrian ser I/O bound o bloquear ligeramente, ya que gestiona un pool
                // de hilos elastico pero limitado. Evita bloquear el Event Loop
                .subscribeOn(Schedulers.boundedElastic());
    }

    private <C extends Command<R>, R extends Response> CommandHandler<C, R> firstHandlerForCommand(C command) {
        ResolvableType handlerType = ResolvableType.forClassWithGenerics(
                CommandHandler.class,
                ResolvableType.forClass(command.getClass()),
                ResolvableType.forClass(command.getResponseType())
        );

        String[] beanNames = applicationContext.getBeanNamesForType(handlerType);

        if (beanNames.length == 0) {
            return null;
        }

        if (beanNames.length > 1) {
            System.err.println("WARN: Multiple handlers found for command: " + command.getClass().getName() + ". Using the first one found: " + Arrays.toString(beanNames));
        }

        return (CommandHandler<C, R>) applicationContext.getBean(beanNames[0]);
    }

}
