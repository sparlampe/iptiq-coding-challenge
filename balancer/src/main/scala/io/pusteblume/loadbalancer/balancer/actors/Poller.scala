package io.pusteblume.loadbalancer.balancer.actors

import akka.actor.{ Actor, ActorRef, Timers }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.pattern.pipe
import io.pusteblume.loadbalancer.balancer.actors.Poller.{
  HeartBeat,
  RegisterProviderForPolling,
  SkippedHeartBeat,
  Tick,
  TickKey,
  UnregisterProviderFromPolling
}
import io.pusteblume.loadbalancer.models.Provider

import scala.concurrent.duration._

class Poller(accountantRef: ActorRef, pollingIntervalSec: Int) extends Actor with Timers {

  var providers = Map[String, Provider]()

  timers.startTimerAtFixedRate(TickKey, Tick, pollingIntervalSec.second)

  def receive = {
    case RegisterProviderForPolling(provider) =>
      providers += (provider.id -> provider)
    case UnregisterProviderFromPolling(providerId: String) =>
      providers -= providerId
    case Tick =>
      implicit val system           = context.system
      implicit val executionContext = system.dispatcher
      for (provider <- providers.values)
        Http()
          .singleRequest(HttpRequest(uri = s"http://${provider.ip}:${provider.port}/check"))
          .map(_ => HeartBeat(provider.id))
          .recover(_ => SkippedHeartBeat(provider.id))
          .pipeTo(accountantRef)
  }
}

object Poller {
  private final case object TickKey
  private final case object Tick
  final case class RegisterProviderForPolling(provider: Provider)
  final case class UnregisterProviderFromPolling(providerId: String)
  final case class HeartBeat(providerId: String)
  final case class SkippedHeartBeat(providerId: String)
}
