# API Documentation

## Base URL
`http://localhost:8080`

## Swagger UI
Access the interactive API docs at: `http://localhost:8080/swagger-ui.html`

## Endpoints

### Orders (`/api/v1/orders`)

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| POST | `/api/v1/orders` | Create a new order | 201 Created |
| GET | `/api/v1/orders` | Get all orders | 200 OK |
| GET | `/api/v1/orders/{id}` | Get order by ID | 200 OK / 404 |
| GET | `/api/v1/orders/status/{status}` | Get orders by status | 200 OK |
| GET | `/api/v1/orders/customer/{email}` | Get orders by customer email | 200 OK |
| GET | `/api/v1/orders/date-range?startDate=...&endDate=...` | Get orders by date range | 200 OK |
| PATCH | `/api/v1/orders/{id}/status` | Update order status | 200 OK / 400 / 404 |
| GET | `/api/v1/orders/summary/counts` | Get order count summary by status | 200 OK |
| DELETE | `/api/v1/orders/{id}/cancel` | Cancel an order | 204 No Content / 400 / 404 |

### Customers (`/api/v1/customers`)

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | `/api/v1/customers/{email}/orders` | Get paginated orders for a customer | 200 OK / 404 |
| GET | `/api/v1/customers/{email}/summary` | Get order statistics for a customer | 200 OK / 404 |
| GET | `/api/v1/customers/{email}/exists` | Check if customer has any orders | 200 OK |

### Health (`/api/health`)

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | `/api/health` | Service health check (verifies DB connectivity) | 200 OK |

## Order Statuses
`PENDING` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED`

An order can be `CANCELLED` from `PENDING`, `CONFIRMED`, or `PROCESSING` states only.

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2026-03-06T10:30:00",
  "status": 404,
  "message": "Order not found with id: 999",
  "details": "uri=/api/v1/orders/999"
}
```

| HTTP Status | Description |
|-------------|-------------|
| 400 | Invalid input, invalid order state transition, or invalid email format |
| 404 | Order, customer, or resource not found |
| 405 | HTTP method not supported for the endpoint |
| 500 | Unexpected server error |
