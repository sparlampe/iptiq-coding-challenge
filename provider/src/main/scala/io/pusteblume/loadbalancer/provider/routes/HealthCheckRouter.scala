package io.pusteblume.loadbalancer.provider.routes

import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route

object HealthCheckRouter {
  def routes:Route =
    path("check") {
    get {
      complete("Ok")
    }
  }
}
