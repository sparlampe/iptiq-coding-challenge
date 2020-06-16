package io.pusteblume.loadbalancer.balancer.routes

import java.net.InetAddress

import akka.http.scaladsl.model.headers.`Remote-Address`
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, RemoteAddress, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.pusteblume.loadbalancer.models.{ Provider, ProviderRegistrationInfo, ProviderState }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.pusteblume.loadbalancer.models.ProviderJson._
import spray.json._

import scala.concurrent.Future

class RegistrationRouterTest extends AnyWordSpec with Matchers with ScalatestRouteTest {

  val registerProviderSuccess: Provider => Future[String]         = _ => Future.successful("success")
  val retrieveProvidersSuccess: () => Future[List[ProviderState]] = () => Future.successful(List[ProviderState]())

  "The service" should {
    "return CREATED if registration successful" in {
      val info = ProviderRegistrationInfo("someId", 5000, 10).toJson.toString()
      Post("/provider", HttpEntity(ContentTypes.`application/json`, info))
        .withHeaders(`Remote-Address`(RemoteAddress(InetAddress.getByName("192.168.3.12")))) ~>
      RegistrationRouter.routes(registerProviderSuccess, retrieveProvidersSuccess) ~> check {
        status should ===(StatusCodes.Created)
      }
    }

    "return OK if retrieval is successful" in {
      Get("/provider") ~>
      RegistrationRouter.routes(registerProviderSuccess, retrieveProvidersSuccess) ~> check {
        status should ===(StatusCodes.OK)
      }
    }
  }
}
