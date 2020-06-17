package io.pusteblume.loadbalancer.balancer.routes

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.{ as, complete, onComplete, _ }
import akka.http.scaladsl.server.Route
import scala.concurrent.Future
import scala.util.{ Failure, Success }

object ServiceRouter {
  def routes(dispatchToProvider: () => Future[HttpResponse]): Route =
    path("get") {
      get {
        onComplete(dispatchToProvider()) {
          case Success(e) => complete(e)
          case Failure(t) => complete(StatusCodes.NotFound -> t.getMessage)
        }
      }
    }
}
