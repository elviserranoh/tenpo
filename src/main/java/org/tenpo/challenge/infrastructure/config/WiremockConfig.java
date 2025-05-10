package org.tenpo.challenge.infrastructure.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiremock.com.github.jknack.handlebars.Handlebars;

import java.net.URL;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Configuration
public class WiremockConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer(@Value("${wiremock.server.port:8881}") int port,
                                         @Value("${wiremock.stubs.path:stubs}") String stubsPath) {

        return new WireMockServer(options()
                .port(port)
                .notifier(new ConsoleNotifier(true))
                .fileSource(getFileSource(stubsPath))
                .extensions(ResponseTemplateTransformer.builder()
                        .handlebars(new Handlebars())
                        .global(true)
                        .build())
        );
    }

    private FileSource getFileSource(String stubs) {
        URL stubsUrl = Thread.currentThread().getContextClassLoader().getResource(stubs);
        String stubsPath = Optional.ofNullable(stubsUrl).map(Object::toString)
                .filter(url -> url.contains("BOOT-INF/classes"))
                .map(s -> "BOOT-INF/classes/stubs")
                .orElse("stubs");
        return new ClasspathFileSource(stubsPath);
    }

}
