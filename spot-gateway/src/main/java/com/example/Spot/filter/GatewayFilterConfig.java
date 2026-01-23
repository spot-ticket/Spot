package com.example.Spot.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class GatewayFilterConfig {

    @Bean
    public GlobalFilter requestIdFilter() {
        return (exchange, chain) -> {
            String rid = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
            if (rid == null || rid.isBlank()) {
                rid = UUID.randomUUID().toString();
            }
            final String finalRequestId = rid;

            var mutated = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-Request-Id", finalRequestId)
                            .build())
                    .build();

            return chain.filter(mutated)
                    .then(Mono.fromRunnable(() ->
                            mutated.getResponse().getHeaders().set("X-Request-Id", finalRequestId)
                    ));
        };
    }
}