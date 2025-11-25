# ADAPT Architecture Benchmarking Suite

Comprehensive performance and maintainability comparison between traditional layered architecture and ADAPT microservices.

## Overview

This benchmarking suite evaluates two architectural approaches:

1. **Layered Architecture (Baseline)** - Traditional 3-tier monolith
2. **ADAPT Microservices** - Event-driven microservices following ADAPT principles

## Test Scenarios

### 1. Baseline Simulation
- **Purpose**: Measure basic read operation performance
- **Load**: 50 ramp-up users, then 10 users/sec for 60 seconds
- **Endpoints**: Product browsing, user queries
- **Metrics**: Response time, throughput

### 2. Order Flow Simulation
- **Purpose**: Test end-to-end event-driven workflow
- **Flow**: Order → Inventory → Payment → Billing → Notification
- **Load**: 20 ramp-up users, then 5 users/sec for 60 seconds
- **Metrics**: Event processing time, success rate

### 3. Concurrent Orders Simulation
- **Purpose**: Stress test under high concurrent load
- **Load**: 100 ramp-up users, then 20 users/sec for 120 seconds
- **Metrics**: System stability, P95/P99 response times

### 4. Ramp-Up Simulation
- **Purpose**: Identify scalability limits
- **Load**: Gradual increase 10 → 50 → 100 → 200 users
- **Metrics**: Performance degradation points

### 5. Spike Test Simulation
- **Purpose**: Test resilience to sudden traffic spikes
- **Load**: 10 users/sec → 500 at once → 50 users/sec
- **Metrics**: Circuit breaker activation, recovery time

## Running Benchmarks

### Prerequisites
```bash
# Install Maven 3.9+
# Install Docker & Docker Compose
# Install Python 3.8+
# Install required Python packages
pip install pandas numpy scipy
```

### Quick Start

```bash
# 1. Run complete benchmark suite (both architectures)
cd case_study/benchmarks/scripts
chmod +x run-benchmarks.sh
./run-benchmarks.sh
```

### Manual Execution

```bash
# Start ADAPT architecture
cd case_study/micro_adapt
docker-compose up -d

# Run specific Gatling scenario
cd ../benchmarks/gatling
mvn gatling:test -Dgatling.simulationClass=simulations.OrderFlowSimulation

# Stop services
cd ../../micro_adapt
docker-compose down
```

## Metrics Collected

### Performance Metrics (Gatling)
- Mean/Median/P95/P99 response times
- Requests per second (throughput)
- Success/failure rates
- Resource utilization (CPU, memory)

### Maintainability Metrics (Custom Scripts)
- **Structural Coupling (SC)**: Inter-service dependencies
- **WSIC**: Weighted Service Interface Count
- **SIDC**: Service Interface Data Cohesion

### DORA Metrics
- Deployment Frequency
- Lead Time for Changes
- Change Failure Rate
- Mean Time to Recovery (MTTR)

## Analyzing Results

```bash
# Calculate maintainability metrics
python3 scripts/calculate-metrics.py

# Analyze performance results
python3 scripts/analyze-results.py
```

## Expected Results

### Performance
- **ADAPT**: Lower latency for read operations, asynchronous writes
- **Layered**: Faster for simple CRUD, slower for complex workflows

### Scalability
- **ADAPT**: Linear scaling, independent service scaling
- **Layered**: Limited by monolith resources

### Resilience
- **ADAPT**: Circuit breakers, graceful degradation
- **Layered**: Cascading failures

### Maintainability
- **ADAPT**: Low coupling (SC < 0.15), high cohesion (SIDC > 0.95)
- **Layered**: High coupling (SC > 0.60), low cohesion (SIDC < 0.60)

## Directory Structure

```
benchmarks/
├── gatling/
│   ├── pom.xml
│   └── simulations/
│       ├── BaselineSimulation.scala
│       ├── OrderFlowSimulation.scala
│       ├── ConcurrentOrdersSimulation.scala
│       ├── RampUpSimulation.scala
│       └── SpikeTestSimulation.scala
├── scripts/
│   ├── run-benchmarks.sh
│   ├── calculate-metrics.py
│   └── analyze-results.py
└── results/
    └── [timestamp]/
        ├── adapt_*/
        ├── layered_*/
        └── analysis.json
```

## Citation

If you use this benchmarking suite in your research, please cite:

```
@inproceedings{adapt2025,
  title={ADAPT: A Novel Microservices Architecture Pattern for Enhanced Maintainability},
  author={Your Name},
  booktitle={Proceedings of...},
  year={2025}
}
```

## Troubleshooting

### Services won't start
```bash
# Check Docker logs
docker-compose logs -f

# Verify ports are available
lsof -i :8080-8090
```

### Gatling tests fail
```bash
# Check API Gateway is accessible
curl http://localhost:8080/api/inventory/products

# Verify Kafka is running (ADAPT only)
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Out of memory
```bash
# Increase Docker memory (Docker Desktop → Settings → Resources)
# Recommended: 8GB RAM, 4 CPUs
```

## Contributing

This is research code. For questions, contact: [your-email@university.edu]
