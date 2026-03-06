# API Documentation

## Base URL
`http://localhost:8080/api/v1/orders`

## Swagger UI
Access the interactive API docs at: `http://localhost:8080/swagger-ui.html`

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/orders` | Create a new order |
| GET | `/api/v1/orders` | Get all orders |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| GET | `/api/v1/orders/status/{status}` | Get orders by status |
| PATCH | `/api/v1/orders/{id}/status` | Update order status |
| DELETE | `/api/v1/orders/{id}/cancel` | Cancel an order |

## Order Statuses
`PENDING` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED`

An order can be `CANCELLED` from `PENDING`, `CONFIRMED`, or `PROCESSING` states only.
