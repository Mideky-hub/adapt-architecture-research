#!/bin/bash
# ADAPT Architecture Benchmarking Automation Script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BENCHMARK_DIR="$(dirname "$SCRIPT_DIR")"
RESULTS_DIR="$BENCHMARK_DIR/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "========================================"
echo "ADAPT Architecture Benchmark Suite"
echo "========================================"

# Create results directory
mkdir -p "$RESULTS_DIR/$TIMESTAMP"

# Function to run Gatling simulation
run_gatling() {
    local simulation=$1
    local architecture=$2
    echo "Running $simulation on $architecture architecture..."

    cd "$BENCHMARK_DIR/gatling"
    mvn gatling:test -Dgatling.simulationClass=simulations.$simulation

    # Copy results
    cp -r target/gatling/*/ "$RESULTS_DIR/$TIMESTAMP/${architecture}_${simulation}/"
}

# Function to collect metrics
collect_metrics() {
    local architecture=$1
    echo "Collecting Prometheus metrics for $architecture..."

    curl -s "http://localhost:9090/api/v1/query?query=process_cpu_seconds_total" > "$RESULTS_DIR/$TIMESTAMP/${architecture}_cpu.json"
    curl -s "http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes" > "$RESULTS_DIR/$TIMESTAMP/${architecture}_memory.json"
    curl -s "http://localhost:9090/api/v1/query?query=http_server_requests_seconds_count" > "$RESULTS_DIR/$TIMESTAMP/${architecture}_requests.json"
}

# Benchmark ADAPT Architecture
echo ""
echo "=== Testing ADAPT Microservices Architecture ==="
echo ""

# Ensure services are running
docker-compose -f ../../micro_adapt/docker-compose.yml up -d
sleep 30 # Wait for services to start

run_gatling "BaselineSimulation" "adapt"
sleep 10
run_gatling "OrderFlowSimulation" "adapt"
sleep 10
run_gatling "ConcurrentOrdersSimulation" "adapt"
sleep 10
run_gatling "RampUpSimulation" "adapt"
sleep 10
run_gatling "SpikeTestSimulation" "adapt"

collect_metrics "adapt"

# Stop ADAPT services
docker-compose -f ../../micro_adapt/docker-compose.yml down

echo ""
echo "=== Testing Layered Architecture ==="
echo ""

# Start layered architecture
docker-compose -f ../../layered/docker-compose.yml up -d
sleep 30

run_gatling "BaselineSimulation" "layered"
sleep 10
run_gatling "OrderFlowSimulation" "layered"
sleep 10
run_gatling "ConcurrentOrdersSimulation" "layered"

collect_metrics "layered"

# Stop layered services
docker-compose -f ../../layered/docker-compose.yml down

echo ""
echo "========================================"
echo "Benchmarks Complete!"
echo "Results saved to: $RESULTS_DIR/$TIMESTAMP"
echo "========================================"
