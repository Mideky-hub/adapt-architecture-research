# ADAPT Architecture Research - Case Study

Comprehensive implementation and benchmarking of two e-commerce architectures for academic research.

## Project Overview

This case study implements the **same e-commerce system** using two different architectural approaches:

1. **Layered Architecture** (`layered/`) - Traditional 3-tier monolith (baseline)
2. **ADAPT Microservices** (`micro_adapt/`) - Event-driven microservices following ADAPT principles

## ADAPT Principles

- **A**synchronous First - Event-driven communication via Kafka
- **D**omain-Cohesive - Each service owns its complete domain
- **A**bstraction with Purpose - Type-safe contracts via Avro schemas
- **P**iloted through Configuration - Externalized configuration
- **T**ransparency through Contracts - Schema Registry enforces versioning

## Architecture Comparison

### Layered Architecture (Baseline)
- **Type**: Monolithic 3-tier application
- **Database**: Single PostgreSQL database (shared)
- **Communication**: Synchronous REST calls
- **Deployment**: Single unit
- **Services**: All business logic in one codebase
- **Port**: 8080

### ADAPT Microservices
- **Type**: Event-driven microservices
- **Database**: 6 PostgreSQL databases (one per service)
- **Communication**: Async events (Kafka) + REST (API Gateway)
- **Deployment**: 6 independent services
- **Services**: Order, Inventory, Payment, Billing, Notification, User
- **Ports**: 8080 (Gateway), 8081-8086 (Services)

## Services Implemented

| Service | Port | Database | Events Consumed | Events Produced |
|---------|------|----------|----------------|-----------------|
| API Gateway | 8080 | N/A | N/A | N/A |
| Order Service | 8081 | order_db | PaymentCompleted, PaymentFailed | OrderCreated, OrderConfirmed, OrderFailed |
| Inventory Service | 8082 | inventory_db | OrderCreated | InventoryReserved, InventoryFailed |
| Payment Service | 8083 | payment_db | InventoryReserved | PaymentCompleted, PaymentFailed |
| Billing Service | 8084 | billing_db | PaymentCompleted | InvoiceGenerated |
| Notification Service | 8085 | notification_db | OrderCreated, InvoiceGenerated | NotificationSent |
| User Service | 8086 | user_db | N/A | UserRegistered |

## Technology Stack

### Both Architectures
- Java 17
- Spring Boot 3.2.0
- PostgreSQL 15
- Maven 3.9
- Docker & Docker Compose
- Prometheus & Grafana

### ADAPT-Specific
- Apache Kafka 7.5.0
- Confluent Schema Registry
- Apache Avro
- Jaeger (Distributed Tracing)
- Spring Cloud Gateway
- Resilience4J (Circuit Breakers)

## Quick Start

### Run Layered Architecture
```bash
cd layered
docker-compose up -d
# Access at http://localhost:8080
```

### Run ADAPT Microservices
```bash
cd micro_adapt
docker-compose up -d
# API Gateway at http://localhost:8080
# Kafka UI at http://localhost:8090
# Grafana at http://localhost:3000 (admin/admin)
# Jaeger at http://localhost:16686
```

### Run Benchmarks
```bash
cd benchmarks/scripts
chmod +x run-benchmarks.sh
./run-benchmarks.sh
# Results in benchmarks/results/[timestamp]/
```

## Benchmarking Suite

### 5 Gatling Load Test Scenarios

1. **BaselineSimulation** - Simple read operations
2. **OrderFlowSimulation** - Complete end-to-end event chain
3. **ConcurrentOrdersSimulation** - High concurrent load stress test
4. **RampUpSimulation** - Gradual load increase for scalability testing
5. **SpikeTestSimulation** - Sudden traffic surge resilience

### Metrics Collected

**Performance:**
- Response time (mean, P50, P95, P99)
- Throughput (requests/second)
- Success/failure rates
- Resource utilization

**Maintainability:**
- Structural Coupling (SC)
- Weighted Service Interface Count (WSIC)
- Service Interface Data Cohesion (SIDC)

**DORA:**
- Deployment Frequency
- Lead Time for Changes
- Change Failure Rate
- Mean Time to Recovery

## Project Structure

```
case_study/
├── layered/                     # Traditional monolith
│   ├── src/main/java/...
│   ├── docker-compose.yml
│   └── README.md
├── micro_adapt/                 # ADAPT microservices
│   ├── api-gateway/
│   ├── order-service/
│   ├── inventory-service/
│   ├── payment-service/
│   ├── billing-service/
│   ├── notification-service/
│   ├── user-service/
│   ├── config-server/
│   ├── schemas/                 # Avro event schemas
│   ├── monitoring/              # Prometheus & Grafana configs
│   ├── docker-compose.yml
│   └── pom.xml (parent)
└── benchmarks/
    ├── gatling/                 # Load test scenarios
    │   ├── simulations/
    │   └── pom.xml
    ├── scripts/                 # Automation & analysis
    │   ├── run-benchmarks.sh
    │   ├── calculate-metrics.py
    │   └── analyze-results.py
    ├── results/                 # Benchmark outputs
    └── README.md
```

## Research Questions Addressed

1. **RQ1**: Does ADAPT architecture reduce structural coupling?
   - **Measured by**: SC metric calculation

2. **RQ2**: Does event-driven design improve scalability?
   - **Measured by**: Ramp-up and spike test scenarios

3. **RQ3**: Does microservices architecture improve resilience?
   - **Measured by**: Circuit breaker metrics, MTTR

4. **RQ4**: What are the trade-offs in complexity?
   - **Measured by**: WSIC, deployment complexity, development effort

## Sample API Calls

### Create Order (Both Architectures)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {"productId": 1, "quantity": 2, "unitPrice": 999.99}
    ]
  }'
```

### Get Products
```bash
curl http://localhost:8080/api/inventory/products
```

### Check Payment Status (ADAPT)
```bash
curl http://localhost:8080/api/payments/order/{orderId}
```

## Monitoring & Observability

### Prometheus Queries
```promql
# Request rate
rate(http_server_requests_seconds_count[1m])

# Response time P95
histogram_quantile(0.95, http_server_requests_seconds)

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[1m])
```

### Grafana Dashboards
- **ADAPT Overview**: All microservices metrics
- **Kafka Metrics**: Topic lag, consumer groups
- **JVM Metrics**: Heap usage, GC activity
- **Business Metrics**: Orders/sec, success rates

### Jaeger Tracing (ADAPT only)
View distributed traces showing the complete event flow:
`OrderCreated → InventoryReserved → PaymentCompleted → InvoiceGenerated`

## Expected Results

### Performance
| Metric | Layered | ADAPT | Winner |
|--------|---------|-------|--------|
| Simple CRUD | ~50ms | ~80ms | Layered |
| Complex Flow | ~500ms | ~200ms | ADAPT |
| P95 Response Time | 800ms | 300ms | ADAPT |
| Throughput (low load) | 500 req/s | 450 req/s | Layered |
| Throughput (high load) | 400 req/s | 800 req/s | ADAPT |

### Maintainability
| Metric | Layered | ADAPT | Improvement |
|--------|---------|-------|------------|
| Structural Coupling | 0.65 | 0.12 | 81.5% |
| WSIC | 15.0 | 10.3 | 31.3% |
| SIDC | 0.53 | 0.98 | 84.9% |

### DORA Metrics
| Metric | Layered | ADAPT |
|--------|---------|-------|
| Deployment Freq | Weekly | Multiple/day |
| Lead Time | 1-2 days | < 1 hour |
| Change Failure Rate | 15-20% | < 5% |
| MTTR | 2-4 hours | < 30 min |

## Limitations & Trade-offs

### ADAPT Advantages
✓ Independent scalability
✓ Fault isolation
✓ Technology diversity
✓ Faster deployments
✓ Better for large teams

### ADAPT Challenges
✗ Higher operational complexity
✗ Distributed system debugging
✗ Eventual consistency
✗ More infrastructure costs
✗ Steeper learning curve

### When to Use Each

**Layered**: Small teams, simple domains, low traffic, budget constraints

**ADAPT**: Large systems, high scalability needs, multiple teams, cloud-native

## Testing the Event Flow

1. Create an order: `POST /api/orders`
2. Check Kafka UI: See `order.created.v1` event
3. Watch Jaeger: See the cascade of service calls
4. Check final state:
   - Order status: `CONFIRMED`
   - Payment status: `COMPLETED`
   - Invoice: Generated
   - Notification: Sent

## Troubleshooting

### Services won't start
```bash
docker-compose logs -f [service-name]
docker-compose ps
```

### Kafka issues (ADAPT)
```bash
# Check Kafka is running
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# View consumer lag
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --all-groups
```

### Database connection errors
```bash
# Verify databases are ready
docker exec -it db-order psql -U postgres -c "\l"
```

## Contributing

This is research code developed for academic purposes. For questions or collaboration:
- **Author**: [Your Name]
- **Institution**: [Your University]
- **Contact**: [your.email@university.edu]

## License

MIT License - Free for academic and research use

## Citation

```bibtex
@mastersthesis{adapt2025,
  author = {Your Name},
  title = {ADAPT: A Novel Microservices Architecture Pattern for Enhanced Maintainability},
  school = {Your University},
  year = {2025},
  type = {Master's Thesis}
}
```

## Acknowledgments

- Spring Boot & Spring Cloud teams
- Apache Kafka community
- Gatling performance testing framework
- Claude Code for rapid development assistance
