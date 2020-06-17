package io.pusteblume.loadbalancer.provider

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpMethods, HttpRequest }
import akka.http.scaladsl.server.Route
import com.typesafe.config.{ Config, ConfigFactory }
import com.typesafe.scalalogging.LazyLogging
import io.jvm.uuid.UUID
import io.pusteblume.loadbalancer.models.ProviderRegistrationInfo
import io.pusteblume.loadbalancer.provider.routes.ServiceRouter
import io.pusteblume.loadbalancer.models.ProviderJson._
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Failure, Success }

object Main extends App with LazyLogging {
  private implicit val system: ActorSystem                        = ActorSystem("main-actor-system")
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  lazy val providerId: UUID      = UUID.random
  val config: Config             = ConfigFactory.load()
  val port: Int                  = config.getInt("app.http.port")
  val lbRegisterEndpoint: String = config.getString("app.http.lbhost")
  val maxConcurrentRequests: Int = config.getInt("app.max-concurrent-requests")
  val allRoutes: Route           = ServiceRouter.routes(providerId)

  Http()
    .bindAndHandle(allRoutes, "0.0.0.0", port)
    .onComplete[Any]({
      case Failure(exception) =>
        logger.error(exception.getMessage)
        system.terminate
      case Success(serverBinding) =>
        logger.info(
          s"Bound with id $providerId to ${serverBinding.localAddress.getAddress.getHostName} :${serverBinding.localAddress.getPort}")
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = lbRegisterEndpoint,
          entity =
            HttpEntity(ContentTypes.`application/json`,
                       ProviderRegistrationInfo(providerId.toString, port, maxConcurrentRequests).toJson.toString())
        )
        Http().singleRequest(request).onComplete {
          case Success(m) => logger.info(m.toString())
          case Failure(e) => logger.error(e.getMessage)
        }
    })
}
