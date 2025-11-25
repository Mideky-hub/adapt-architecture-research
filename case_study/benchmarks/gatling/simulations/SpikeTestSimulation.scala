package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Scenario 5: Spike Test - Sudden traffic surge
 * Tests system resilience to sudden spikes (e.g., flash sales, Black Friday)
 */
class SpikeTestSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val scn = scenario("Traffic Spike")
    .exec(http("Get Products")
      .get("/api/inventory/products")
      .check(status.is(200)))
    .pause(500 milliseconds)
    .exec(http("Create Order")
      .post("/api/orders")
      .body(StringBody("""{"userId": 1, "items": [{"productId": 1, "quantity": 1, "unitPrice": 99.99}]}"""))
      .check(status.in(200, 500, 503))) // Accept circuit breaker responses

  setUp(
    scn.inject(
      constantUsersPerSec(10) during (30 seconds), // Normal load
      nothingFor(5 seconds),
      atOnceUsers(500), // SPIKE!
      constantUsersPerSec(50) during (60 seconds), // Sustained high load
      nothingFor(10 seconds),
      constantUsersPerSec(10) during (30 seconds) // Recovery
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile4.lt(10000)
    )
}
