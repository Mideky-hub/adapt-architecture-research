package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Scenario 2: Order Creation Flow - End-to-end event chain
 * Tests the complete event-driven flow: Order → Inventory → Payment → Billing → Notification
 */
class OrderFlowSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val orderRequest = StringBody("""{
    "userId": 1,
    "items": [
      {"productId": 1, "quantity": 2, "unitPrice": 999.99},
      {"productId": 2, "quantity": 1, "unitPrice": 29.99}
    ]
  }""")

  val scn = scenario("Complete Order Flow")
    .exec(http("Create Order")
      .post("/api/orders")
      .body(orderRequest)
      .check(status.is(200))
      .check(jsonPath("$.id").saveAs("orderId")))
    .pause(2) // Wait for event processing
    .exec(http("Check Order Status")
      .get("/api/orders/${orderId}")
      .check(status.is(200))
      .check(jsonPath("$.status").exists))
    .pause(3) // Wait for payment processing
    .exec(http("Check Payment")
      .get("/api/payments/order/${orderId}")
      .check(status.in(200, 404))) // May not exist if payment failed
    .pause(2) // Wait for billing
    .exec(http("Check Invoice")
      .get("/api/invoices/order/${orderId}")
      .check(status.in(200, 404))) // May not exist if order/payment failed

  setUp(
    scn.inject(
      rampUsers(20) during (30 seconds),
      constantUsersPerSec(5) during (60 seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.mean.lt(3000),
      global.successfulRequests.percent.gt(90)
    )
}
