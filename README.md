# THD Order Management Service

A POC microservice for managing orders in The Home Depot ecosystem, demonstrating AI Agentic SDLC Workflows.

## Overview

The THD Order Management Service is a Spring Boot-based REST API that provides comprehensive order management capabilities. It supports creating, retrieving, and managing orders with various statuses and operations.

## Prerequisites

- Java 11 or higher
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

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/api/v1/orders` | Create a new order | 201 Created |
| GET | `/api/v1/orders` | Get all orders | 200 OK |
| GET | `/api/v1/orders/{id}` | Get order by ID | 200 OK / 404 Not Found |
| GET | `/api/v1/orders/status/{status}` | Get orders by status | 200 OK |
| PATCH | `/api/v1/orders/{id}/status` | Update order status | 200 OK / 400 / 404 |
| DELETE | `/api/v1/orders/{id}/cancel` | Cancel an order | 204 No Content / 400 / 404 |

## Valid Order Statuses

- `PENDING` - Initial state when order is created
- `CONFIRMED` - Order has been confirmed
- `PROCESSING` - Order is being processed
- `SHIPPED` - Order has been shipped
- `DELIVERED` - Order has been delivered
- `CANCELLED` - Order has been cancelled

## Order Status Transitions

Valid status transitions:
- `PENDING` → `CONFIRMED`, `PROCESSING`, `CANCELLED`
- `CONFIRMED` → `PROCESSING`, `SHIPPED`, `CANCELLED`
- `PROCESSING` → `SHIPPED`, `CANCELLED`
- `SHIPPED` → `DELIVERED`
- `DELIVERED` → (no transitions allowed)
- `CANCELLED` → (no transitions allowed)

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
http://localhost:8080/v3/api-docs
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
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Database Profiles

- **Default (H2)**: In-memory database for development
- **PostgreSQL**: Use with `--spring.profiles.active=postgres`

Set via Docker Compose environment variable or VM parameter.

## Exception Handling

The API returns appropriate HTTP status codes:

- `400 Bad Request` - Invalid input or invalid order state transition
- `404 Not Found` - Order not found
- `500 Internal Server Error` - Unexpected server error

Error responses include a detailed message:

```json
{
  "timestamp": "2026-03-06T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 999"
}
```

## Monitoring and Health Checks

Health endpoint:

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

## Contributing

This is a POC project demonstrating AI Agentic SDLC Workflows. Please follow the existing code structure and conventions when contributing.

## License

Proprietary - The Home Depot
