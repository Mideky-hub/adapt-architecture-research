package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Scenario 1: Baseline - Simple user browsing
 * Tests basic read operations (GET requests) to measure baseline performance
 */
class BaselineSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080") // API Gateway
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val scn = scenario("Baseline Browsing")
    .exec(http("Get All Products")
      .get("/api/inventory/products")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Get Available Products")
      .get("/api/inventory/products/available")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Get Product by ID")
      .get("/api/inventory/products/1")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Get User Info")
      .get("/api/users/1")
      .check(status.is(200)))

  setUp(
    scn.inject(
      rampUsers(50) during (30 seconds),
      constantUsersPerSec(10) during (60 seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(5000),
      global.successfulRequests.percent.gt(95)
    )
}
