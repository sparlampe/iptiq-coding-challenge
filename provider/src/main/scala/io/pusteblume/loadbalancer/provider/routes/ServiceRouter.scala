package io.pusteblume.loadbalancer.provider.routes

import akka.http.scaladsl.server.Directives.{ complete, get, path }
import akka.http.scaladsl.server.Route
import io.jvm.uuid.UUID

object ServiceRouter {
  def routes(id: UUID): Route =
    path("get") {
      get {
        complete(id.toString)
      }
    }
}
