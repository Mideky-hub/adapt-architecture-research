# E-Commerce Layered Architecture

Traditional 3-tier layered architecture implementation for the ADAPT research case study.

## Architecture Overview

This is a monolithic Spring Boot application with traditional layered architecture:

### Layers
- **Controller Layer**: REST API endpoints
- **Service Layer**: Business logic
- **Repository Layer**: Data access

### Services
1. **User Service**: User management and authentication
2. **Order Service**: Order processing and management
3. **Inventory Service**: Product and stock management
4. **Payment Service**: Payment processing
5. **Billing Service**: Invoice generation
6. **Notification Service**: Email/SMS notifications

### Key Characteristics
- Monolithic deployment
- Shared database (PostgreSQL)
- Synchronous blocking calls
- Tight coupling between layers
- Compile-time dependencies

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL 15**
- **Maven**
- **Docker & Docker Compose**
- **Prometheus & Grafana** (monitoring)

## Prerequisites

- Docker and Docker Compose
- Java 17 (for local development)
- Maven 3.9+ (for local development)

## Quick Start

### Using Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up --build

# Start in detached mode
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Local Development

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

## API Endpoints

### User Service
- `POST /api/users/register` - Register new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `GET /api/users` - Get all users

### Order Service
- `POST /api/orders` - Create new order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{userId}` - Get orders by user
- `GET /api/orders` - Get all orders

### Inventory Service
- `GET /api/inventory/products/{id}` - Get product by ID
- `GET /api/inventory/products` - Get all products
- `GET /api/inventory/products/available` - Get available products
- `GET /api/inventory/products/{id}/check-availability?quantity={qty}` - Check availability

### Payment Service
- `POST /api/payments` - Process payment
- `GET /api/payments/{id}` - Get payment by ID
- `GET /api/payments/order/{orderId}` - Get payment by order

### Billing Service
- `POST /api/billing/invoices/generate/{orderId}` - Generate invoice
- `GET /api/billing/invoices/{id}` - Get invoice by ID
- `GET /api/billing/invoices/order/{orderId}` - Get invoice by order
- `PUT /api/billing/invoices/{id}/mark-paid` - Mark invoice as paid

### Notification Service
- `POST /api/notifications?userId={id}&type={type}&subject={subject}&message={msg}` - Send notification
- `GET /api/notifications/user/{userId}` - Get user notifications
- `GET /api/notifications/user/{userId}/unread` - Get unread notifications
- `PUT /api/notifications/{id}/mark-read` - Mark as read

## Monitoring

- **Application**: http://localhost:8080
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **Prometheus UI**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## Database

- **Host**: localhost
- **Port**: 5432
- **Database**: ecommerce_layered
- **Username**: postgres
- **Password**: postgres

## Sample Data

The application includes sample data for testing:
- 3 users (john_doe, jane_smith, bob_wilson)
- 10 products (Laptop, Mouse, Keyboard, etc.)

## Testing Order Creation

```bash
# Create an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 2, "quantity": 2}
    ]
  }'
```

## Architecture Issues (for Research)

This layered architecture demonstrates several anti-patterns:

1. **Tight Coupling**: Services directly depend on each other
2. **Shared Database**: All services access the same database
3. **Synchronous Blocking**: All calls are synchronous
4. **Cross-Domain Access**: BillingService directly accesses UserRepository
5. **Scalability Limits**: Cannot scale individual services
6. **Single Point of Failure**: Monolithic deployment

These issues will be addressed in the ADAPT microservices architecture.
