package com.medicore.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/api/users/**", "/api/auth/**").uri("lb://user-service"))
            .route("medicine-service", r -> r.path("/api/medicines/**").uri("lb://medicine-service"))
            .route("inventory-service", r -> r.path("/api/inventory/**").uri("lb://inventory-service"))
            .route("order-service", r -> r.path("/api/orders/**").uri("lb://order-service"))
            .route("prescription-service", r -> r.path("/api/prescriptions/**").uri("lb://prescription-service"))
            .route("payment-service", r -> r.path("/api/payments/**").uri("lb://payment-service"))
            .route("fulfillment-service", r -> r.path("/api/fulfillment/**").uri("lb://fulfillment-service"))
            .route("delivery-service", r -> r.path("/api/deliveries/**").uri("lb://delivery-service"))
            .route("notification-service", r -> r.path("/api/notifications/**").uri("lb://notification-service"))
            .build();
    }
}
