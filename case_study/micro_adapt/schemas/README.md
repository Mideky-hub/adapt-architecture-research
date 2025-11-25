# Event Schemas for ADAPT Microservices

This directory contains Avro schema definitions for all events exchanged between microservices.

## ADAPT Principle: Transparency through Contracts

All inter-service communication uses well-defined event contracts registered in Schema Registry.

## Event Catalog

### User Events
- `user.registered.v1` - Emitted when a new user registers
- `user.updated.v1` - Emitted when user profile is updated

### Inventory Events
- `inventory.reserved.v1` - Emitted when stock is reserved for an order
- `inventory.released.v1` - Emitted when reserved stock is released
- `inventory.depleted.v1` - Emitted when product stock runs low

### Order Events
- `order.created.v1` - Emitted when order is created
- `order.confirmed.v1` - Emitted when order is confirmed (payment successful)
- `order.failed.v1` - Emitted when order fails
- `order.cancelled.v1` - Emitted when order is cancelled

### Payment Events
- `payment.initiated.v1` - Emitted when payment processing starts
- `payment.completed.v1` - Emitted when payment succeeds
- `payment.failed.v1` - Emitted when payment fails

### Billing Events
- `invoice.generated.v1` - Emitted when invoice is created
- `invoice.sent.v1` - Emitted when invoice is sent to customer

### Notification Events
- `notification.send.v1` - Command to send notification
- `notification.sent.v1` - Emitted when notification is successfully sent

## Event Flow Example: Order Creation

```
1. Client → Order Service: POST /api/orders
2. Order Service → Kafka: order.created.v1
3. Inventory Service (listens) → Reserves stock
4. Inventory Service → Kafka: inventory.reserved.v1
5. Payment Service (listens) → Processes payment
6. Payment Service → Kafka: payment.completed.v1
7. Order Service (listens) → Updates order status
8. Order Service → Kafka: order.confirmed.v1
9. Billing Service (listens) → Generates invoice
10. Billing Service → Kafka: invoice.generated.v1
11. Notification Service (listens) → Sends email
12. Notification Service → Kafka: notification.sent.v1
```

## Key ADAPT Principles Demonstrated

1. **Asynchronous**: All service communication via Kafka events
2. **Domain-Cohesive**: Each service owns its domain and database
3. **Abstraction with Purpose**: Events abstract implementation details
4. **Piloted through Configuration**: Topic names, retry policies configurable
5. **Transparency through Contracts**: Avro schemas enforce contracts
