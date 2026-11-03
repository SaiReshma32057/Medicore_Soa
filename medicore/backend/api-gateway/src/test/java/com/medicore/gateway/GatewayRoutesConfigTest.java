package com.medicore.gateway;

import com.medicore.gateway.config.GatewayRoutesConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayRoutesConfigTest {
    @Test
    void gatewayUsesDiscoveryUrisForEveryDomainService() {
        var context = new org.springframework.context.support.StaticApplicationContext();
        context.getBeanFactory().registerSingleton("pathRoutePredicateFactory", new PathRoutePredicateFactory());
        var builder = new RouteLocatorBuilder(context);
        var locator = new GatewayRoutesConfig().routes(builder);

        var routes = locator.getRoutes().collectList().block();
        assertEquals(9, routes.size());
        assertTrue(routes.stream().allMatch(route -> route.getUri().toString().startsWith("lb://")));
        assertEquals("lb://user-service", find(routes, "user-service").getUri().toString());
        assertEquals("lb://notification-service", find(routes, "notification-service").getUri().toString());
    }

    private Route find(java.util.List<Route> routes, String id) {
        return routes.stream().filter(route -> route.getId().equals(id)).findFirst().orElseThrow();
    }
}
