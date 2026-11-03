# Medicore Backend

This directory contains 11 independently deployable Spring Boot 3.2 services targeting Java 17. Eureka runs first, the Gateway runs on 8080, and domain services register with Eureka on ports 8081-8089.

## Services

| Service | Port | Responsibility |
|---|---:|---|
| eureka-server | 8761 | Service registry |
| api-gateway | 8080 | Eureka-backed routing |
| user-service | 8081 | Registration, login, JWT |
| medicine-service | 8082 | Medicine catalog |
| inventory-service | 8083 | Locked stock mutations |
| order-service | 8084 | Orders and inventory reservation via Feign |
| prescription-service | 8085 | Prescription lifecycle |
| payment-service | 8086 | Payment lifecycle |
| fulfillment-service | 8087 | Fulfillment lifecycle |
| delivery-service | 8088 | Delivery tracking |
| notification-service | 8089 | Notification dispatch state |

## Configuration

Set `MYSQL_PASSWORD` before starting persistence services. Optional variables include `MYSQL_HOST` (default `localhost`), `MYSQL_PORT` (default `3306`), `MYSQL_USERNAME` (default `root`), `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`, `JWT_SECRET`, and `JWT_EXPIRATION_MS`. Every service uses database `medicoree_db` and the exact JDBC URL configured in its properties.

## Run and test

From each service directory run `mvn -q compile` and `mvn -q test`. Start Eureka before the other services, then start the Gateway and domain services independently with `mvn spring-boot:run`.

## API examples

Register with `POST /api/auth/register` using `{ "email":"user@example.com", "password":"password123", "name":"User" }`, then login at `/api/auth/login` and send `Authorization: Bearer <token>` to protected user endpoints. Create medicines at `POST /api/medicines`; reserve stock at `POST /api/inventory/{medicineId}/reserve`; create orders at `POST /api/orders`.

Insufficient inventory returns HTTP 409 and does not reduce available stock. Cross-service references are scalar IDs; no service shares JPA entities.
