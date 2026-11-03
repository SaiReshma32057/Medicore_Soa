# Medicore Backend Implementation Plan

**Date**: 2026-09-22  
**Scope**: `backend/` only  
**Execution status**: Plan only; implementation and Maven validation are explicitly deferred.

## Summary

Build 11 independent Spring Boot Maven applications under `backend/`: `eureka-server`, `api-gateway`, `user-service`, `medicine-service`, `inventory-service`, `order-service`, `prescription-service`, `payment-service`, `fulfillment-service`, `delivery-service`, and `notification-service`.

Each project is independently buildable and testable, uses Java 17 and Spring Boot 3.x, contains its own `pom.xml`, application class, `src/main/resources/application.properties`, and tests, and has no dependency on a multi-module parent build. Service discovery uses Eureka, edge routing uses Spring Cloud Gateway, synchronous service calls use OpenFeign where required, persistence uses Spring Data JPA with MySQL, request validation uses Jakarta Bean Validation, and `user-service` owns JWT/BCrypt authentication.

The implementation must produce real compiling Java code for every file in the manifest below. No frontend file may be created or modified. No JPA entity may reference an entity from another service; cross-service identifiers remain scalar IDs and service calls use HTTP/Feign.

## Requirements

- **REQ-001**: Create exactly 11 independent Spring Boot Maven projects under `backend/` with the listed service names.
- **REQ-002**: Every project has its own `pom.xml`, Java 17 configuration, Spring Boot 3.x dependencies, application class, `src/main/resources/application.properties`, and tests.
- **REQ-003**: Configure Eureka server and Eureka clients; configure Gateway routes and service discovery through the gateway.
- **REQ-004**: Use OpenFeign for required synchronous calls, especially `order-service` to `inventory-service`; use service names rather than hardcoded hostnames.
- **REQ-005**: Use Spring Data JPA and MySQL for persistence services with no cross-service JPA relationships.
- **REQ-006**: Use the exact JDBC URL `jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3308}/medicoree_db?createDatabaseIfNotExist=true&serverTimezone=UTC`, externalize `MYSQL_PASSWORD`, and never hardcode a password.
- **REQ-007**: Implement `user-service` authentication with BCrypt password hashing, JWT issuance/verification, validation, and protected endpoint behavior.
- **REQ-008**: Implement inventory mutations transactionally with pessimistic locking, prevent negative stock, and return HTTP 409 for insufficient stock.
- **REQ-009**: Implement order-to-inventory communication through Feign and preserve failure/409 semantics.
- **REQ-010**: Implement meaningful domain APIs for medicine, inventory, orders, prescriptions, payments, fulfillment, delivery, notifications, users, gateway, and discovery rather than empty scaffolds.
- **REQ-011**: Add service tests that compile and exercise core behavior, including authentication, validation, inventory locking/insufficient stock, and order Feign behavior with mocks.
- **REQ-012**: Add `backend/README.md` documenting architecture, environment variables, run/test commands, service ports, and API examples.
- **REQ-013**: Run Maven compile and tests for every service, repair implementation errors, and record validation results.
- **REQ-014**: Do not modify anything outside `backend/`.

## Technical Context

**Language/Version**: Java 17  
**Framework**: Spring Boot 3.x; Spring Cloud release train compatible with the selected Boot 3.x version  
**Build**: 11 independent Maven builds; no root parent POM required  
**Dependencies**: Spring Web, Actuator, Eureka Server/Client, Spring Cloud Gateway, OpenFeign, Spring Data JPA, MySQL Connector/J, Jakarta Validation, Spring Security, JJWT, BCrypt through Spring Security Crypto, and Spring Boot Test  
**Storage**: MySQL database `medicoree_db`; each persistence service uses service-owned tables and scalar foreign identifiers only  
**Configuration**: `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_PASSWORD`, JWT secret and expiry, Eureka URL, service ports, and downstream service settings are externalized with safe local defaults only where requested  
**Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc where appropriate; database-sensitive tests should use mocks or a clearly isolated test profile so every service remains independently runnable  
**Target Platform**: Local Maven execution first; service discovery and downstream integration are designed for Docker or local JVM processes

## Constitution Check

- **Backend-only scope**: Pass. All implementation paths are below `backend/`; frontend remains untouched.
- **Independent deployability**: Pass. Every service owns its POM, source tree, resources, tests, and runtime configuration.
- **Security by default**: Pass. Passwords are BCrypt-hashed, JWT settings are externalized, passwords are not committed, and protected APIs validate bearer tokens.
- **Service ownership boundaries**: Pass. Entities never use cross-service JPA relationships; remote references are IDs and Feign contracts.
- **Operational correctness**: Pass. Inventory uses transactional pessimistic locking and explicit 409 conflict semantics.
- **Evidence before completion**: Pass. Completion requires compile and test commands for all 11 services plus a backend scope check.

## Applied Guidelines

- Spring Boot 3.x: use Jakarta packages (`jakarta.persistence`, `jakarta.validation`), constructor injection, layered controller/service/repository boundaries, and `@SpringBootApplication` entry points.
- Spring Cloud: keep the Spring Cloud BOM compatible with the chosen Boot 3.x release; use Eureka service IDs for discovery and Gateway route predicates for edge routing.
- Feign: define typed client interfaces and DTOs; do not pass JPA entities across service boundaries.
- Persistence: use service-local entities and repositories; use explicit transaction boundaries and locking annotations for stock mutation.
- Security: use a password encoder rather than storing raw passwords; validate JWT claims and return 401/403 consistently.
- Testing: test the public behavior at the controller/service boundary and mock remote calls; do not make the full validation gate depend on a running frontend.

## Service Topology

| Service | Package root | Port | Primary responsibility | Required integrations |
|---|---|---:|---|---|
| `eureka-server` | `com.medicore.eureka` | 8761 | Service registry | Eureka Server |
| `api-gateway` | `com.medicore.gateway` | 8080 | Edge routing and auth propagation | Eureka Client, Gateway |
| `user-service` | `com.medicore.user` | 8081 | Users, registration, login, JWT | JPA, MySQL, Security |
| `medicine-service` | `com.medicore.medicine` | 8082 | Medicine catalog | JPA, MySQL, validation |
| `inventory-service` | `com.medicore.inventory` | 8083 | Stock and reservation mutations | JPA, MySQL, transactions |
| `order-service` | `com.medicore.order` | 8084 | Order creation and status | JPA, MySQL, Feign to inventory |
| `prescription-service` | `com.medicore.prescription` | 8085 | Prescription records and validation | JPA, MySQL, validation |
| `payment-service` | `com.medicore.payment` | 8086 | Payment intent/status records | JPA, MySQL, validation |
| `fulfillment-service` | `com.medicore.fulfillment` | 8087 | Fulfillment lifecycle | JPA, MySQL, validation |
| `delivery-service` | `com.medicore.delivery` | 8088 | Delivery tracking/status | JPA, MySQL, validation |
| `notification-service` | `com.medicore.notification` | 8089 | Notification records and dispatch state | JPA, MySQL, validation |

## Implementation Steps

### Step 1: [Cross-cutting] Establish independent Maven project skeletons

**Requirements**: REQ-001, REQ-002, REQ-006, REQ-014

Create the 11 directories and give each an independent POM with Java 17, Spring Boot 3.x, a compatible Spring Cloud BOM, compiler settings, test dependencies, and only the dependencies used by that service. Add one application class and one `src/main/resources/application.properties` per service. Use the exact MySQL URL wherever JPA/MySQL is used; externalize `MYSQL_PASSWORD` as `${MYSQL_PASSWORD}` and never add a literal password. Configure distinct ports and Eureka URLs through environment/property placeholders.

### Step 2: [Cross-cutting] Implement discovery and edge routing

**Requirements**: REQ-003, REQ-010

Implement `eureka-server` with `@EnableEurekaServer`. Implement `api-gateway` with discovery locator or explicit routes for all domain services, route predicates, default filters for correlation/auth header handling, and health configuration. Keep gateway routes based on Eureka service IDs, not fixed machine addresses.

### Step 3: [User service] Implement identity, registration, and JWT security

**Requirements**: REQ-005, REQ-007, REQ-010, REQ-011

Implement the user entity/repository, validated registration and login DTOs, BCrypt hashing, JWT service, bearer authentication filter, security configuration, auth controller, and user controller. Return safe response DTOs without password hashes. Add unit and MVC tests for successful registration/login, duplicate email, invalid input, invalid token, and protected access.

### Step 4: [Catalog and prescription services] Implement medicine and prescription APIs

**Requirements**: REQ-005, REQ-010, REQ-011

Implement CRUD-style medicine catalog operations with validation and service-owned persistence. Implement prescription creation, retrieval, status updates, and required scalar user/order/medicine IDs without entity relationships to other services. Add focused controller/service tests for valid and invalid requests.

### Step 5: [Inventory service] Implement locked stock operations

**Requirements**: REQ-005, REQ-008, REQ-011

Implement stock entities, a repository query using `@Lock(LockModeType.PESSIMISTIC_WRITE)`, transactional reserve/release/adjust operations, validation, and a domain exception mapped to HTTP 409 when available quantity is insufficient. Ensure every mutation rejects negative quantities and leaves stock unchanged on failure. Add tests that assert successful reservation, insufficient stock conflict, invalid quantity, and rollback/error behavior.

### Step 6: [Order service] Implement order lifecycle and inventory Feign integration

**Requirements**: REQ-004, REQ-005, REQ-009, REQ-010, REQ-011

Implement order and order-line persistence with scalar medicine IDs, validated create requests, an `InventoryClient` Feign interface, reservation request/response DTOs, transaction/error handling, and controller endpoints. The service must call inventory through Feign using the Eureka service ID and translate inventory 409 responses without creating an order that cannot reserve stock. Add Mockito/Feign-boundary tests for success, insufficient stock, downstream failure, validation, and order lookup.

### Step 7: [Payment, fulfillment, delivery, and notification services] Implement operational lifecycle APIs

**Requirements**: REQ-005, REQ-010, REQ-011

Implement service-owned entities, repositories, validated request/response DTOs, services, controllers, and exception handling for payment state, fulfillment state, delivery tracking, and notification state. Use scalar IDs for order/user references. APIs must have real state transitions and persistence behavior, not empty methods. Add tests for creation, retrieval, valid transitions, invalid transitions, and validation failures.

### Step 8: [Cross-cutting] Add resources, error handling, and documentation

**Requirements**: REQ-006, REQ-010, REQ-012, REQ-014

Add consistent JSON error responses, validation error handling, actuator health settings, logging-safe configuration, and application properties to every service. Add `backend/README.md` with architecture, service table, environment variables, startup order, independent Maven commands, API examples, JWT usage, MySQL requirements, and inventory 409 semantics. Do not document or add frontend changes.

### Step 9: [Cross-cutting] Execute the complete validation matrix

**Requirements**: REQ-013, REQ-014

From each service directory, run `mvn -q test` (which includes compilation) and, where needed for explicit evidence, `mvn -q compile` followed by `mvn -q test`. Repair only backend implementation/build/test errors. Confirm all 11 services pass. Run a final scope check proving changed/created paths are all under `backend/` and verify no password literal exists in source or properties.

## Required Project File Manifests

The following file manifests are mandatory. Additional small support files are allowed only when required for a compiling implementation; removing a listed file is not allowed.

### `eureka-server`

```text
backend/eureka-server/pom.xml
backend/eureka-server/src/main/java/com/medicore/eureka/EurekaServerApplication.java
backend/eureka-server/src/main/resources/application.properties
backend/eureka-server/src/test/java/com/medicore/eureka/EurekaServerApplicationTests.java
```

### `api-gateway`

```text
backend/api-gateway/pom.xml
backend/api-gateway/src/main/java/com/medicore/gateway/ApiGatewayApplication.java
backend/api-gateway/src/main/java/com/medicore/gateway/config/GatewayRoutesConfig.java
backend/api-gateway/src/main/java/com/medicore/gateway/filter/CorrelationIdFilter.java
backend/api-gateway/src/main/resources/application.properties
backend/api-gateway/src/test/java/com/medicore/gateway/ApiGatewayApplicationTests.java
```

### `user-service`

```text
backend/user-service/pom.xml
backend/user-service/src/main/java/com/medicore/user/UserServiceApplication.java
backend/user-service/src/main/java/com/medicore/user/config/SecurityConfig.java
backend/user-service/src/main/java/com/medicore/user/controller/AuthController.java
backend/user-service/src/main/java/com/medicore/user/controller/UserController.java
backend/user-service/src/main/java/com/medicore/user/dto/LoginRequest.java
backend/user-service/src/main/java/com/medicore/user/dto/RegisterRequest.java
backend/user-service/src/main/java/com/medicore/user/dto/UserResponse.java
backend/user-service/src/main/java/com/medicore/user/entity/User.java
backend/user-service/src/main/java/com/medicore/user/exception/GlobalExceptionHandler.java
backend/user-service/src/main/java/com/medicore/user/repository/UserRepository.java
backend/user-service/src/main/java/com/medicore/user/security/JwtAuthenticationFilter.java
backend/user-service/src/main/java/com/medicore/user/security/JwtService.java
backend/user-service/src/main/java/com/medicore/user/service/UserService.java
backend/user-service/src/main/resources/application.properties
backend/user-service/src/test/java/com/medicore/user/UserServiceApplicationTests.java
backend/user-service/src/test/java/com/medicore/user/service/UserServiceTest.java
backend/user-service/src/test/java/com/medicore/user/controller/AuthControllerTest.java
```

### `medicine-service`

```text
backend/medicine-service/pom.xml
backend/medicine-service/src/main/java/com/medicore/medicine/MedicineServiceApplication.java
backend/medicine-service/src/main/java/com/medicore/medicine/controller/MedicineController.java
backend/medicine-service/src/main/java/com/medicore/medicine/dto/MedicineRequest.java
backend/medicine-service/src/main/java/com/medicore/medicine/dto/MedicineResponse.java
backend/medicine-service/src/main/java/com/medicore/medicine/entity/Medicine.java
backend/medicine-service/src/main/java/com/medicore/medicine/exception/GlobalExceptionHandler.java
backend/medicine-service/src/main/java/com/medicore/medicine/repository/MedicineRepository.java
backend/medicine-service/src/main/java/com/medicore/medicine/service/MedicineService.java
backend/medicine-service/src/main/resources/application.properties
backend/medicine-service/src/test/java/com/medicore/medicine/MedicineServiceApplicationTests.java
backend/medicine-service/src/test/java/com/medicore/medicine/service/MedicineServiceTest.java
```

### `inventory-service`

```text
backend/inventory-service/pom.xml
backend/inventory-service/src/main/java/com/medicore/inventory/InventoryServiceApplication.java
backend/inventory-service/src/main/java/com/medicore/inventory/controller/InventoryController.java
backend/inventory-service/src/main/java/com/medicore/inventory/dto/InventoryRequest.java
backend/inventory-service/src/main/java/com/medicore/inventory/dto/InventoryResponse.java
backend/inventory-service/src/main/java/com/medicore/inventory/entity/InventoryItem.java
backend/inventory-service/src/main/java/com/medicore/inventory/exception/InsufficientStockException.java
backend/inventory-service/src/main/java/com/medicore/inventory/exception/GlobalExceptionHandler.java
backend/inventory-service/src/main/java/com/medicore/inventory/repository/InventoryRepository.java
backend/inventory-service/src/main/java/com/medicore/inventory/service/InventoryService.java
backend/inventory-service/src/main/resources/application.properties
backend/inventory-service/src/test/java/com/medicore/inventory/InventoryServiceApplicationTests.java
backend/inventory-service/src/test/java/com/medicore/inventory/service/InventoryServiceTest.java
```

### `order-service`

```text
backend/order-service/pom.xml
backend/order-service/src/main/java/com/medicore/order/OrderServiceApplication.java
backend/order-service/src/main/java/com/medicore/order/client/InventoryClient.java
backend/order-service/src/main/java/com/medicore/order/controller/OrderController.java
backend/order-service/src/main/java/com/medicore/order/dto/InventoryReservationRequest.java
backend/order-service/src/main/java/com/medicore/order/dto/InventoryReservationResponse.java
backend/order-service/src/main/java/com/medicore/order/dto/OrderItemRequest.java
backend/order-service/src/main/java/com/medicore/order/dto/OrderRequest.java
backend/order-service/src/main/java/com/medicore/order/dto/OrderResponse.java
backend/order-service/src/main/java/com/medicore/order/entity/Order.java
backend/order-service/src/main/java/com/medicore/order/entity/OrderItem.java
backend/order-service/src/main/java/com/medicore/order/exception/GlobalExceptionHandler.java
backend/order-service/src/main/java/com/medicore/order/repository/OrderRepository.java
backend/order-service/src/main/java/com/medicore/order/service/OrderService.java
backend/order-service/src/main/resources/application.properties
backend/order-service/src/test/java/com/medicore/order/OrderServiceApplicationTests.java
backend/order-service/src/test/java/com/medicore/order/service/OrderServiceTest.java
```

### `prescription-service`

```text
backend/prescription-service/pom.xml
backend/prescription-service/src/main/java/com/medicore/prescription/PrescriptionServiceApplication.java
backend/prescription-service/src/main/java/com/medicore/prescription/controller/PrescriptionController.java
backend/prescription-service/src/main/java/com/medicore/prescription/dto/PrescriptionRequest.java
backend/prescription-service/src/main/java/com/medicore/prescription/dto/PrescriptionResponse.java
backend/prescription-service/src/main/java/com/medicore/prescription/entity/Prescription.java
backend/prescription-service/src/main/java/com/medicore/prescription/exception/GlobalExceptionHandler.java
backend/prescription-service/src/main/java/com/medicore/prescription/repository/PrescriptionRepository.java
backend/prescription-service/src/main/java/com/medicore/prescription/service/PrescriptionService.java
backend/prescription-service/src/main/resources/application.properties
backend/prescription-service/src/test/java/com/medicore/prescription/PrescriptionServiceApplicationTests.java
backend/prescription-service/src/test/java/com/medicore/prescription/service/PrescriptionServiceTest.java
```

### `payment-service`

```text
backend/payment-service/pom.xml
backend/payment-service/src/main/java/com/medicore/payment/PaymentServiceApplication.java
backend/payment-service/src/main/java/com/medicore/payment/controller/PaymentController.java
backend/payment-service/src/main/java/com/medicore/payment/dto/PaymentRequest.java
backend/payment-service/src/main/java/com/medicore/payment/dto/PaymentResponse.java
backend/payment-service/src/main/java/com/medicore/payment/entity/Payment.java
backend/payment-service/src/main/java/com/medicore/payment/exception/GlobalExceptionHandler.java
backend/payment-service/src/main/java/com/medicore/payment/repository/PaymentRepository.java
backend/payment-service/src/main/java/com/medicore/payment/service/PaymentService.java
backend/payment-service/src/main/resources/application.properties
backend/payment-service/src/test/java/com/medicore/payment/PaymentServiceApplicationTests.java
backend/payment-service/src/test/java/com/medicore/payment/service/PaymentServiceTest.java
```

### `fulfillment-service`

```text
backend/fulfillment-service/pom.xml
backend/fulfillment-service/src/main/java/com/medicore/fulfillment/FulfillmentServiceApplication.java
backend/fulfillment-service/src/main/java/com/medicore/fulfillment/controller/FulfillmentController.java
backend/fulfillment-service/src/main/java/com/medicore/fulfillment/dto/FulfillmentRequest.java
backend/fulfillment-service/src/main/java/com/medicore/fulfillment/dto/FulfillmentResponse.java
backend/fulfillment-service/src/main/java/com/medicore/fulfillment/entity/Fulfillment.java
backend/fulfillment-service/src/main/java/com/medicore/fulfillment/exception/GlobalExceptionHandler.java
backend/fulfillment-service/src/main/java/com/medicore/fulfillment/repository/FulfillmentRepository.java
backend/fulfillment-service/src/main/java/com/medicore/fulfillment/service/FulfillmentService.java
backend/fulfillment-service/src/main/resources/application.properties
backend/fulfillment-service/src/test/java/com/medicore/fulfillment/FulfillmentServiceApplicationTests.java
backend/fulfillment-service/src/test/java/com/medicore/fulfillment/service/FulfillmentServiceTest.java
```

### `delivery-service`

```text
backend/delivery-service/pom.xml
backend/delivery-service/src/main/java/com/medicore/delivery/DeliveryServiceApplication.java
backend/delivery-service/src/main/java/com/medicore/delivery/controller/DeliveryController.java
backend/delivery-service/src/main/java/com/medicore/delivery/dto/DeliveryRequest.java
backend/delivery-service/src/main/java/com/medicore/delivery/dto/DeliveryResponse.java
backend/delivery-service/src/main/java/com/medicore/delivery/entity/Delivery.java
backend/delivery-service/src/main/java/com/medicore/delivery/exception/GlobalExceptionHandler.java
backend/delivery-service/src/main/java/com/medicore/delivery/repository/DeliveryRepository.java
backend/delivery-service/src/main/java/com/medicore/delivery/service/DeliveryService.java
backend/delivery-service/src/main/resources/application.properties
backend/delivery-service/src/test/java/com/medicore/delivery/DeliveryServiceApplicationTests.java
backend/delivery-service/src/test/java/com/medicore/delivery/service/DeliveryServiceTest.java
```

### `notification-service`

```text
backend/notification-service/pom.xml
backend/notification-service/src/main/java/com/medicore/notification/NotificationServiceApplication.java
backend/notification-service/src/main/java/com/medicore/notification/controller/NotificationController.java
backend/notification-service/src/main/java/com/medicore/notification/dto/NotificationRequest.java
backend/notification-service/src/main/java/com/medicore/notification/dto/NotificationResponse.java
backend/notification-service/src/main/java/com/medicore/notification/entity/Notification.java
backend/notification-service/src/main/java/com/medicore/notification/exception/GlobalExceptionHandler.java
backend/notification-service/src/main/java/com/medicore/notification/repository/NotificationRepository.java
backend/notification-service/src/main/java/com/medicore/notification/service/NotificationService.java
backend/notification-service/src/main/resources/application.properties
backend/notification-service/src/test/java/com/medicore/notification/NotificationServiceApplicationTests.java
backend/notification-service/src/test/java/com/medicore/notification/service/NotificationServiceTest.java
```

## Execution Tasks

### Phase 1: Setup

- [ ] T001 [Plan:1.1] Create only the 11 service directories under `backend/` and initialize each independent `pom.xml` with Java 17, Spring Boot 3.x, compatible Spring Cloud BOM, compiler plugin, and test dependencies.
- [ ] T002 [P] [Plan:1.1] Add each service's application class, `src/main/resources/application.properties`, and smoke test using the exact package and file manifest in this plan.
- [ ] T003 [P] [Plan:1.1,8.1] Add service-specific ports, Eureka URL properties, datasource URL `jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3308}/medicoree_db?createDatabaseIfNotExist=true&serverTimezone=UTC`, `${MYSQL_PASSWORD}`, and no hardcoded password to every persistence service.

### Phase 2: Foundational Platform

- [ ] T004 [Plan:2.1] Implement `backend/eureka-server/src/main/java/com/medicore/eureka/EurekaServerApplication.java` with Eureka Server enablement and registry properties.
- [ ] T005 [Plan:2.1] Implement `backend/api-gateway/src/main/java/com/medicore/gateway/config/GatewayRoutesConfig.java` with Eureka-backed routes for all domain services.
- [ ] T006 [Plan:2.1] Implement `backend/api-gateway/src/main/java/com/medicore/gateway/filter/CorrelationIdFilter.java` and gateway application configuration without modifying any frontend path.

### Phase 3: Identity and Access

- [ ] T007 [Plan:3.1] Implement `backend/user-service/src/main/java/com/medicore/user/entity/User.java`, `repository/UserRepository.java`, and validated DTOs for registration, login, and safe user responses.
- [ ] T008 [Plan:3.1] Implement `backend/user-service/src/main/java/com/medicore/user/security/JwtService.java`, `security/JwtAuthenticationFilter.java`, and `config/SecurityConfig.java` with BCrypt, externalized JWT settings, and protected route behavior.
- [ ] T009 [Plan:3.1] Implement `backend/user-service/src/main/java/com/medicore/user/service/UserService.java`, `controller/AuthController.java`, `controller/UserController.java`, and `exception/GlobalExceptionHandler.java` with real registration/login/profile behavior.
- [ ] T010 [Plan:3.1] Add `UserServiceTest.java` and `AuthControllerTest.java` covering BCrypt persistence, duplicate users, login tokens, validation, invalid tokens, and protected access.

### Phase 4: Catalog, Prescription, and Inventory

- [ ] T011 [P] [Plan:4.1] Implement the complete medicine manifest under `backend/medicine-service/` with validated CRUD behavior, persistence, error mapping, and focused service tests.
- [ ] T012 [P] [Plan:4.1] Implement the complete prescription manifest under `backend/prescription-service/` with scalar user/order/medicine IDs, validated lifecycle behavior, and focused service tests.
- [ ] T013 [Plan:5.1] Implement `backend/inventory-service/src/main/java/com/medicore/inventory/entity/InventoryItem.java` and `repository/InventoryRepository.java` with a pessimistic write lock query.
- [ ] T014 [Plan:5.1] Implement `backend/inventory-service/src/main/java/com/medicore/inventory/service/InventoryService.java`, controller, DTOs, and exceptions with transactional reserve/release/adjust operations, nonnegative stock invariants, and HTTP 409 insufficient-stock handling.
- [ ] T015 [Plan:5.1] Add inventory tests proving successful mutation, pessimistic-lock query usage, invalid quantities, insufficient-stock 409 mapping, and no negative/partial mutation on failure.

### Phase 5: Orders and Operational Services

- [ ] T016 [Plan:6.1] Implement order entities, DTOs, repository, service, controller, exception handler, and `InventoryClient.java` under `backend/order-service/` with Eureka service ID Feign configuration.
- [ ] T017 [Plan:6.1] Add order tests mocking the Feign client for successful reservation, inventory 409 propagation, downstream failure, validation, and persistence behavior.
- [ ] T018 [P] [Plan:7.1] Implement the complete payment manifest under `backend/payment-service/` with validated payment state transitions, persistence, controller, errors, and tests.
- [ ] T019 [P] [Plan:7.1] Implement the complete fulfillment manifest under `backend/fulfillment-service/` with validated fulfillment state transitions, persistence, controller, errors, and tests.
- [ ] T020 [P] [Plan:7.1] Implement the complete delivery manifest under `backend/delivery-service/` with tracking/status transitions, persistence, controller, errors, and tests.
- [ ] T021 [P] [Plan:7.1] Implement the complete notification manifest under `backend/notification-service/` with notification state, persistence, controller, errors, and tests.

### Phase 6: Documentation and Cross-Cutting Quality

- [ ] T022 [Plan:8.1] Add consistent validation/error response handling, actuator health properties, and logging-safe configuration to all listed services without duplicating cross-service JPA relationships.
- [ ] T023 [Plan:8.1] Write `backend/README.md` with architecture, service ports, startup order, env vars, MySQL setup, independent run/test commands, JWT request example, medicine/order/inventory API examples, and 409 semantics.
- [ ] T024 [Plan:9.1] Run `mvn -q compile` and `mvn -q test` from each of the 11 service directories; repair backend-only compile and test errors until all services pass.
- [ ] T025 [Plan:9.1] Perform final backend scope and security checks: verify all created/modified files are below `backend/`, all mandatory manifest files exist, the exact JDBC URL is present, and no hardcoded password exists.

## Validation Matrix

Run these commands independently; do not substitute a root reactor build because independence is a requirement.

```powershell
$services = @(
  "eureka-server", "api-gateway", "user-service", "medicine-service",
  "inventory-service", "order-service", "prescription-service", "payment-service",
  "fulfillment-service", "delivery-service", "notification-service"
)
foreach ($service in $services) {
  Push-Location "backend/$service"
  mvn -q compile
  mvn -q test
  Pop-Location
}
```

Acceptance requires:

1. All 11 `compile` commands exit successfully.
2. All 11 `test` commands exit successfully.
3. `InventoryServiceTest` proves insufficient stock maps to 409 and never produces a negative quantity.
4. `OrderServiceTest` proves the inventory call is made through the Feign interface and handles 409/downstream errors.
5. User security tests prove BCrypt is used and JWT-protected behavior works.
6. Every mandatory path in the file manifests exists and contains compiling implementation code.
7. No path outside `backend/` is created or modified.
8. No password literal is present in any backend source or properties file.

## Requirement Mapping

| REQ ID | Description | Plan Items | Implementation Evidence |
|---|---|---|---|
| REQ-001 | 11 independent services | 1.1 | 11 service directories and POMs |
| REQ-002 | Java 17, Boot 3.x, app/resources/tests per project | 1.1 | Each manifest's POM, application class, properties, and smoke test |
| REQ-003 | Eureka and Gateway | 2.1 | Eureka application, gateway routes, Eureka properties |
| REQ-004 | Feign and service discovery calls | 2.1, 6.1 | `InventoryClient.java`, Eureka service IDs, gateway discovery routes |
| REQ-005 | JPA/MySQL and service-local persistence | 1.1, 3.1, 4.1, 5.1, 6.1, 7.1 | Entities/repositories per service; scalar remote IDs |
| REQ-006 | Exact JDBC URL and external password | 1.1, 8.1 | Every persistence service's `application.properties`; password scan |
| REQ-007 | JWT/BCrypt user security | 3.1 | `SecurityConfig.java`, `JwtService.java`, filter, auth tests |
| REQ-008 | Transactional pessimistic inventory and 409 | 5.1 | Locked repository query, transactional service, exception handler, tests |
| REQ-009 | Order to inventory through Feign | 6.1 | `InventoryClient.java`, order service/error tests |
| REQ-010 | Real implementations across all domains | 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1 | Required Java file manifests and non-empty APIs |
| REQ-011 | Focused tests | 3.1, 4.1, 5.1, 6.1, 7.1, 9.1 | Per-service tests plus security/inventory/order scenarios |
| REQ-012 | Backend README | 8.1 | `backend/README.md` |
| REQ-013 | Compile/test and repair | 9.1 | Per-service Maven validation matrix and recorded results |
| REQ-014 | Backend-only changes | 1.1, 8.1, 9.1 | Scope check over created/modified paths |

## Explicit Non-Goals

- Do not implement or modify frontend code.
- Do not create a root multi-module Maven reactor that replaces independent service builds.
- Do not add cross-service JPA entity relationships or shared database entities.
- Do not execute implementation or Maven commands as part of plan generation.
