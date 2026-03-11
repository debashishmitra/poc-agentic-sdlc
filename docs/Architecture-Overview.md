# Architecture Overview

## THD Order Management Service

A Spring Boot 4.0.x REST microservice following a standard layered architecture.

```
┌───────────────────────────────────────┐
│              API Layer                │
│  OrderController    /api/v1/orders/** │
│  CustomerController /api/v1/customers/**│
│  HealthController   /api/health       │
├───────────────────────────────────────┤
│           Service Layer               │
│  OrderService / OrderServiceImpl      │
│  CustomerOrderService                 │
│  HealthService                        │
│  Business logic, validation, mapping  │
├───────────────────────────────────────┤
│         Repository Layer              │
│         (OrderRepository)             │
│         Spring Data JPA               │
├───────────────────────────────────────┤
│          Database Layer               │
│    PostgreSQL (prod) / H2 (dev)       │
└───────────────────────────────────────┘
```

## Tech Stack
- **Framework:** Spring Boot 4.0.3
- **Language:** Java 25
- **Database:** PostgreSQL / H2
- **API Docs:** SpringDoc OpenAPI 3.0.2
- **Testing:** JUnit 5, Mockito, Spring Boot Test
- **Build:** Maven, Docker
- **CI/CD:** GitHub Actions (planned)
