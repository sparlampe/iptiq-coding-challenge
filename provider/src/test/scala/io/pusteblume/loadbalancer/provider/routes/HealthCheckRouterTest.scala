package io.pusteblume.loadbalancer.provider.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HealthCheckRouterTest extends AnyWordSpec with Matchers with ScalatestRouteTest {
  "The service" should {

    "return ok status" in {
      Get("/check") ~> HealthCheckRouter.routes~> check {
        status should ===(StatusCodes.OK)
      }
    }
  }
}
