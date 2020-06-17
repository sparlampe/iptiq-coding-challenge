package io.pusteblume.loadbalancer.balancer

import akka.pattern.ask
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import io.pusteblume.loadbalancer.balancer.actors.Poller.RegisterProviderForPolling
import io.pusteblume.loadbalancer.balancer.actors.{Poller, ProviderBookKeeper}
import io.pusteblume.loadbalancer.balancer.actors.ProviderBookKeeper.{ActivateProvider, CannotAllocateProvider, DeactivateProvider, GetNextProvider, GetProviders, MaxCapacityReached, NextAvailableProvider, RegisterProvider, Registered, RegisteredProviders}
import io.pusteblume.loadbalancer.balancer.routes.{RegistrationRouter, ServiceRouter}
import io.pusteblume.loadbalancer.balancer.strategy.RoundRobinBalancingStrategy
import io.pusteblume.loadbalancer.models.{Provider, ProviderState}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Main extends App with LazyLogging {
  val config: Config        = ConfigFactory.load()
  val port: Int             = config.getInt("app.http.port")
  val maxProviderCount: Int = config.getInt("app.max-providers")
  val pollingInterval:Int =config.getInt("app.polling-interval-sec")

  implicit val system: ActorSystem                        = ActorSystem("main-actor-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout                           = Timeout(5 seconds)

  lazy val providerBookKeeper: ActorRef =
    system.actorOf(Props(new ProviderBookKeeper(maxProviderCount, new RoundRobinBalancingStrategy)),
                   name = "bookKeeperActor")

  lazy val poller: ActorRef = system.actorOf(Props(new Poller(providerBookKeeper, pollingInterval)),
    name = "pollerActor")
  
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val registerProvider: Provider => Future[String] = provider => {
    providerBookKeeper ? RegisterProvider(provider) flatMap {
      case Registered(id) => 
        poller! RegisterProviderForPolling(provider)
        Future.successful(s"Provider $id registered")
      case MaxCapacityReached(id) =>
        Future.failed[String](new Throwable(s"Provider $id could not be registered, max capacity reached"))
    }
  }
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val retrieveProviders: () => Future[List[ProviderState]] = () => {
    providerBookKeeper ? GetProviders map { case RegisteredProviders(list) => list }
  }
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val setIsActive: (String, Boolean)=>() = (providerId,isActive) => {
    isActive match {
      case true => providerBookKeeper ! ActivateProvider(providerId)
      case false => providerBookKeeper ! DeactivateProvider(providerId)
    }
  }
  val registrationRoutes: Route = RegistrationRouter.routes(registerProvider, retrieveProviders, setIsActive)

  val dispatchToProvider: () => Future[HttpResponse] = () =>
    providerBookKeeper ? GetNextProvider flatMap {
      case NextAvailableProvider(p) =>
        Http()
          .singleRequest(HttpRequest(uri = s"http://${p.ip}:${p.port.toString}/get"))
      case CannotAllocateProvider => Future.failed(new Throwable("Capacity exceeded"))
  }
  val serviceRoutes: Route = ServiceRouter.routes(dispatchToProvider)

  val allRoutes: Route = registrationRoutes ~ serviceRoutes

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
