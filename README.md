# Order Management Service

A POC microservice for managing orders, demonstrating AI Agentic SDLC Workflows.

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=flat&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8.7+-C71A36?style=flat&logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat&logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2-In--Memory-0000BB?style=flat&logo=databricks&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-59666C?style=flat&logo=hibernate&logoColor=white)
![Swagger](https://img.shields.io/badge/SpringDoc%20OpenAPI-3.0.2-85EA2D?style=flat&logo=swagger&logoColor=black)
![JUnit](https://img.shields.io/badge/JUnit-5-25A162?style=flat&logo=junit5&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-0.8.14-C21325?style=flat&logo=codecov&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.8+-3776AB?style=flat&logo=python&logoColor=white)
![Claude](https://img.shields.io/badge/Claude%20AI-Opus%204.6-D4A574?style=flat&logo=anthropic&logoColor=white)

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Building the Project](#building-the-project)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Endpoints](#api-endpoints)
- [Valid Order Statuses](#valid-order-statuses)
- [Order Status Transitions](#order-status-transitions)
- [Sample Request](#sample-request)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Exception Handling](#exception-handling)
- [Monitoring and Health Checks](#monitoring-and-health-checks)
- [Development](#development)
- [Troubleshooting](#troubleshooting)
- [Hosting & Monitoring](#hosting--monitoring)
- [Contributing](#contributing)
- [License](#license)

## Overview

The Order Management Service is a Spring Boot-based REST API that provides comprehensive order management capabilities. It supports creating, retrieving, and managing orders with various statuses and operations.

## Prerequisites

- Java 25 or higher
- Maven 3.8.7 or higher
- Docker and Docker Compose (for PostgreSQL deployment)
- PostgreSQL 15 (if running without Docker)

## Building the Project

Clone the repository and navigate to the project directory:

```bash
mvn clean package
```

To skip tests during build:

```bash
mvn clean package -DskipTests
```

## Running the Application

### Option 1: H2 In-Memory Database (Development/Testing)

Run the application with H2 in-memory database:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Option 2: PostgreSQL with Docker Compose (Production-like)

Ensure Docker and Docker Compose are installed, then run:

```bash
docker-compose up -d
```

This will:
- Start a PostgreSQL 15 container with proper health checks
- Build and start the Spring Boot application container
- Both services will be networked and ready for communication

To view logs:

```bash
docker-compose logs -f
```

To stop the services:

```bash
docker-compose down
```

To remove volumes (clean database):

```bash
docker-compose down -v
```

## Running Tests

Execute unit and integration tests:

```bash
mvn test
```

Run tests with coverage report:

```bash
mvn test jacoco:report
```

Coverage report will be available at: `target/site/jacoco/index.html`

## API Endpoints

### Orders

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/api/v1/orders` | Create a new order | 201 Created |
| GET | `/api/v1/orders` | Get all orders | 200 OK |
| GET | `/api/v1/orders/{id}` | Get order by ID | 200 OK / 404 Not Found |
| GET | `/api/v1/orders/status/{status}` | Get orders by status | 200 OK |
| GET | `/api/v1/orders/customer/{email}` | Get orders by customer email | 200 OK |
| GET | `/api/v1/orders/date-range?startDate=...&endDate=...` | Get orders by date range | 200 OK |
| PATCH | `/api/v1/orders/{id}/status` | Update order status | 200 OK / 400 / 404 |
| GET | `/api/v1/orders/summary/counts` | Get order count summary by status | 200 OK |
| DELETE | `/api/v1/orders/{id}/cancel` | Cancel an order | 204 No Content / 400 / 404 |

### Customers

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/v1/customers/{email}/orders` | Get paginated orders for a customer | 200 OK / 404 Not Found |
| GET | `/api/v1/customers/{email}/summary` | Get aggregated order statistics for a customer | 200 OK / 404 Not Found |
| GET | `/api/v1/customers/{email}/exists` | Check if a customer has any orders | 200 OK |

### Health

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/health` | Service health check (verifies DB connectivity) | 200 OK |

### Query Parameters — Date Range

The `GET /api/v1/orders/date-range` endpoint accepts the following query parameters:

| Parameter | Required | Format | Example |
|-----------|----------|--------|---------|
| `startDate` | Yes | ISO 8601 date-time | `2026-01-01T00:00:00` |
| `endDate` | Yes | ISO 8601 date-time | `2026-12-31T23:59:59` |

**Example request:**

```bash
curl "http://localhost:8080/api/v1/orders/date-range?startDate=2026-01-01T00:00:00&endDate=2026-12-31T23:59:59"
```

### Query Parameters — Customer Orders

The `GET /api/v1/customers/{email}/orders` endpoint supports pagination and date filtering:

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `page` | No | `0` | Page number (zero-based) |
| `size` | No | `10` | Page size |
| `sortBy` | No | `createdAt` | Field to sort by |
| `sortDir` | No | `desc` | Sort direction (`asc` or `desc`) |
| `fromDate` | No | — | Filter orders created on or after (ISO 8601) |
| `toDate` | No | — | Filter orders created on or before (ISO 8601) |

## Valid Order Statuses

- `PENDING` - Initial state when order is created
- `CONFIRMED` - Order has been confirmed
- `PROCESSING` - Order is being processed
- `SHIPPED` - Order has been shipped
- `DELIVERED` - Order has been delivered
- `CANCELLED` - Order has been cancelled

## Order Status Transitions

Status transition rules:
- `CANCELLED` → no further transitions allowed
- `DELIVERED` → no further transitions allowed
- `PENDING` → cannot transition directly to `SHIPPED` or `DELIVERED`
- All other transitions are allowed

## Sample Request

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "shippingAddress": "123 Main Street, New York, NY 10001",
    "items": [
      {
        "productSku": "SKU001",
        "productName": "Widget",
        "quantity": 2,
        "unitPrice": 29.99
      }
    ]
  }'
```

## API Documentation

Interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON specification:

```
http://localhost:8080/api-docs
```

## Project Structure

```
src/
├── main/
│   ├── java/com/thd/ordermanagement/
│   │   ├── controller/      - REST API endpoints
│   │   ├── service/         - Business logic
│   │   ├── repository/      - Data access layer
│   │   ├── model/           - Entity models
│   │   ├── dto/             - Data transfer objects
│   │   ├── exception/       - Custom exceptions
│   │   └── config/          - Application configuration
│   └── resources/
│       └── application.yml  - Configuration
└── test/
    ├── java/                - Unit and integration tests
    └── resources/
        └── application-test.yml - Test configuration
```

## Configuration

### Application Properties

Main configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:orderdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
  jpa:
    hibernate:
      ddl-auto: update
```

### Database Profiles

- **Default (H2)**: In-memory database for development
- **PostgreSQL**: Use with `--spring.profiles.active=postgres`

Set via Docker Compose environment variable or VM parameter.

## Exception Handling

The API returns appropriate HTTP status codes:

- `400 Bad Request` - Invalid input or invalid order state transition
- `404 Not Found` - Order or resource not found
- `405 Method Not Allowed` - HTTP method not supported for the endpoint
- `500 Internal Server Error` - Unexpected server error

Error responses include a detailed message:

```json
{
  "timestamp": "2026-03-06T10:30:00",
  "status": 404,
  "message": "Order not found with id: 999",
  "details": "uri=/api/v1/orders/999"
}
```

## Monitoring and Health Checks

Application health check (verifies database connectivity):

```
http://localhost:8080/api/health
```

Spring Boot Actuator health endpoint:

```
http://localhost:8080/actuator/health
```

Metrics endpoint:

```
http://localhost:8080/actuator/metrics
```

## Development

### Code Style

The project follows standard Java conventions:
- camelCase for variables and methods
- PascalCase for class names
- UPPER_CASE for constants

### Testing Strategy

- Unit tests use Mockito for mocking dependencies
- Integration tests use @WebMvcTest for controller testing
- H2 in-memory database for test isolation

## Troubleshooting

### Port Already in Use

If port 8080 is in use, specify a different port:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

Or set in `application.yml`:

```yaml
server:
  port: 8081
```

### PostgreSQL Connection Issues

Verify PostgreSQL is running and accessible:

```bash
docker-compose ps
docker-compose logs postgres
```

Check the container health:

```bash
docker-compose exec postgres pg_isready -U order_user
```

### Tests Failing

Ensure H2 is properly configured in `pom.xml` and test profile uses correct settings:

```bash
mvn test -Dspring.profiles.active=test
```

## Hosting & Monitoring

The application is hosted on Render (free tier) with UptimeRobot providing keep-alive pings every 5 minutes.

| Service | Dashboard |
|---------|-----------|
| **Render** (Hosting) | [App Dashboard](https://dashboard.render.com/web/srv-d6ot40aa214c73aojvjg) |
| **UptimeRobot** (Monitoring) | [Monitor Dashboard](https://dashboard.uptimerobot.com/monitors/802530643) |

**Live URL**: https://poc-agentic-sdlc.onrender.com

## Contributing

This is a POC project demonstrating AI Agentic SDLC Workflows. Please follow the existing code structure and conventions when contributing.

## License

Proprietary - The Home Depot
