# Architecture Overview

## THD Order Management Service

A Spring Boot 2.7.x REST microservice following a standard layered architecture.

```
┌─────────────────────────────────────────┐
│              API Layer                   │
│         (OrderController)               │
│         /api/v1/orders/**               │
├─────────────────────────────────────────┤
│           Service Layer                  │
│    (OrderService / OrderServiceImpl)     │
│    Business logic, validation, mapping   │
├─────────────────────────────────────────┤
│         Repository Layer                 │
│         (OrderRepository)               │
│         Spring Data JPA                  │
├─────────────────────────────────────────┤
│          Database Layer                  │
│    PostgreSQL (prod) / H2 (dev)         │
└─────────────────────────────────────────┘
```

## Tech Stack
- **Framework:** Spring Boot 2.7.18
- **Language:** Java 11
- **Database:** PostgreSQL 15 / H2
- **API Docs:** SpringDoc OpenAPI 1.7
- **Testing:** JUnit 5, Mockito, Spring Boot Test
- **Build:** Maven, Docker
- **CI/CD:** GitHub Actions (planned)
