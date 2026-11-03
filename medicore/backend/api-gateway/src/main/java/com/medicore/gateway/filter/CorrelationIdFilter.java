package com.medicore.gateway.filter;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    public static final String HEADER = "X-Correlation-Id";
    @Override public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String id = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        final String correlationId = id;
        var request = exchange.getRequest().mutate().header(HEADER, correlationId).build();
        var updated = exchange.mutate().request(request).build();
        updated.getResponse().beforeCommit(() -> {
            updated.getResponse().getHeaders().set(HEADER, correlationId);
            return Mono.empty();
        });
        return chain.filter(updated);
    }
    @Override public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
