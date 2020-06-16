package io.pusteblume.loadbalancer.provider.routes

import io.jvm.uuid.UUID
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ServiceRouterTest extends AnyWordSpec with Matchers with ScalatestRouteTest {

  val rnd: UUID = UUID.random
  "The service" should {

    "return the instance id when invoked on 'get' route" in {
      Get("/get") ~> ServiceRouter.routes(rnd) ~> check {
        responseAs[String] shouldEqual rnd.toString
      }
    }
  }
}
