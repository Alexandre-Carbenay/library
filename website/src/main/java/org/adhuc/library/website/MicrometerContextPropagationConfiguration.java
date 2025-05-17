package org.adhuc.library.website;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

import java.util.concurrent.Executors;

/**
 * This configuration ensures that the Resilience4j circuit breaker and Spring WebClient use the micrometer tracing
 * context to enhance the requests to APIs.
 */
@Configuration
class MicrometerContextPropagationConfiguration {

    MicrometerContextPropagationConfiguration(Resilience4JCircuitBreakerFactory circuitBreakerFactory) {
        circuitBreakerFactory.configureExecutorService(
                ContextExecutorService.wrap(Executors.newCachedThreadPool(), ContextSnapshotFactory.builder().build())
        );
    }

    @PostConstruct
    void init() {
        Hooks.enableAutomaticContextPropagation();
    }

}
