package io.pusteblume.loadbalancer.balancer.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ as, complete, entity, extractClientIP, onComplete, pathPrefix, post }
import akka.http.scaladsl.server.Route
import io.pusteblume.loadbalancer.models.{ Provider, ProviderRegistrationInfo, ProviderState }

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scala.compat.java8.OptionConverters._
import akka.http.scaladsl.server.Directives._
import io.pusteblume.loadbalancer.models.ProviderJson._

object RegistrationRouter {
  def routes(registerProvider: Provider => Future[String],
             retrieveProviders: () => Future[List[ProviderState]]): Route =
    pathPrefix("provider") {
      concat(
        post {
          extractClientIP(ip => {
            ip.getAddress().asScala match {
              case Some(addr) =>
                entity(as[ProviderRegistrationInfo]) { provider =>
                  onComplete(
                    registerProvider(Provider(provider.id, addr.getHostAddress, provider.port, provider.maxCapacity))) {
                    case Success(msg) => complete(StatusCodes.Created   -> msg)
                    case Failure(e)   => complete(StatusCodes.Forbidden -> e.getMessage)
                  }
                }
              case None => complete(StatusCodes.Forbidden -> "Cannot register provider without ip")
            }
          })
        },
        get {
          onComplete(retrieveProviders()) {
            case Success(providers) =>
              complete(StatusCodes.OK -> s"Currently registered providers ${providers.toString()}")
            case Failure(e) =>
              complete(StatusCodes.InternalServerError -> s"Could not retrieve providers ${e.getMessage}")
          }
        }
      )
    }
}
