package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Scenario 4: Ramp-up Test - Gradual load increase
 * Tests system scalability and identifies performance degradation points
 */
class RampUpSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val browse = exec(http("Browse Products")
    .get("/api/inventory/products")
    .check(status.is(200)))

  val createOrder = exec(http("Create Order")
    .post("/api/orders")
    .body(StringBody("""{"userId": 1, "items": [{"productId": 1, "quantity": 1, "unitPrice": 99.99}]}"""))
    .check(status.is(200)))

  val scn = scenario("Gradual Ramp-Up")
    .randomSwitch(
      70.0 -> browse,
      30.0 -> createOrder
    )

  setUp(
    scn.inject(
      rampUsers(10) during (30 seconds),
      rampUsers(50) during (60 seconds),
      rampUsers(100) during (60 seconds),
      rampUsers(200) during (60 seconds),
      constantUsersPerSec(50) during (120 seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile2.lt(4000)
    )
}
