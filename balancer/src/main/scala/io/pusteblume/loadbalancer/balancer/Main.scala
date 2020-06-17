package io.pusteblume.loadbalancer.balancer

import akka.pattern.ask
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import com.typesafe.scalalogging.LazyLogging
import io.pusteblume.loadbalancer.balancer.actors.ProviderBookKeeper
import io.pusteblume.loadbalancer.balancer.actors.ProviderBookKeeper.{
  GetProviders,
  MaxCapacityReached,
  RegisterProvider,
  Registered,
  RegisteredProviders
}
import io.pusteblume.loadbalancer.balancer.routes.RegistrationRouter
import io.pusteblume.loadbalancer.models.{ Provider, ProviderState }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success }

object Main extends App with LazyLogging {
  val config: Config        = ConfigFactory.load()
  val port: Int             = config.getInt("app.http.port")
  val maxProviderCount: Int = config.getInt("app.max-providers")

  implicit val system: ActorSystem                        = ActorSystem("main-actor-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout                           = Timeout(5 seconds)
  lazy val providerBookKeeper: ActorRef =
    system.actorOf(Props(new ProviderBookKeeper(maxProviderCount)), name = "bookKeeperActor")

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val registerProvider: Provider => Future[String] = provider => {
    providerBookKeeper ? RegisterProvider(provider) flatMap {
      case Registered(id) => Future.successful(s"Provider $id registered")
      case MaxCapacityReached(id) =>
        Future.failed[String](new Throwable(s"Provider $id could not be registered, max capacity reached"))
    }
  }
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val retrieveProviders: () => Future[List[ProviderState]] = () => {
    providerBookKeeper ? GetProviders map { case RegisteredProviders(list) => list }
  }

  val registrationRoutes: Route = RegistrationRouter.routes(registerProvider, retrieveProviders)
  val allRoutes: Route          = registrationRoutes
  Http()
    .bindAndHandle(allRoutes, "0.0.0.0", port)
    .onComplete[Any] {
      case Failure(exception) =>
        logger.error(exception.getMessage)
        system.terminate()
      case Success(serverBinding) =>
        logger.info(
          s"Bound to ${serverBinding.localAddress.getAddress.getHostName} :${serverBinding.localAddress.getPort}")
    }
}