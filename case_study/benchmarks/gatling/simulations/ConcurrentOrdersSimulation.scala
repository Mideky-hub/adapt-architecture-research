package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Scenario 3: Concurrent Order Processing - Stress test
 * Tests system behavior under high concurrent order creation load
 */
class ConcurrentOrdersSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val orderRequest = StringBody("""{
    "userId": #{userId},
    "items": [
      {"productId": #{productId}, "quantity": #{quantity}, "unitPrice": 99.99}
    ]
  }""")

  val feeder = Iterator.continually(Map(
    "userId" -> (1 + scala.util.Random.nextInt(10)),
    "productId" -> (1 + scala.util.Random.nextInt(10)),
    "quantity" -> (1 + scala.util.Random.nextInt(5))
  ))

  val scn = scenario("Concurrent Orders")
    .feed(feeder)
    .exec(http("Create Order")
      .post("/api/orders")
      .body(orderRequest)
      .check(status.is(200)))
    .pause(500 milliseconds, 1 second)

  setUp(
    scn.inject(
      rampUsers(100) during (30 seconds),
      constantUsersPerSec(20) during (120 seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile3.lt(5000),
      global.successfulRequests.percent.gt(85)
    )
}
