package io.pusteblume.loadbalancer.balancer.actors

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import io.pusteblume.loadbalancer.balancer.actors.ProviderBookKeeper._
import io.pusteblume.loadbalancer.models.{ Provider, ProviderState }

import scala.collection.mutable

class ProviderBookKeeper(maxProviders: Int) extends Actor with LazyLogging {
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  val providers: mutable.Map[String, ProviderState] = mutable.Map[String, ProviderState]()

  def receive: Receive = {
    case RegisterProvider(provider) =>
      providers.size match {
        case `maxProviders` =>
          logger.warn(s"Cannot register provider ${provider.id}, max capacity reached.")
          sender ! MaxCapacityReached(provider.id: String)
        case _ =>
          providers += (provider.id -> ProviderState(provider))
          logger.info(s"Registered ${provider.id}.")
          sender ! Registered(provider.id: String)
      }
    case GetProviders =>
      sender ! RegisteredProviders(providers.values.toList)
  }
}

object ProviderBookKeeper {
  final case class RegisterProvider(provider: Provider)
  final case class Registered(providerId: String)
  final case class MaxCapacityReached(providerId: String)
  final case object GetProviders
  final case class RegisteredProviders(providers: List[ProviderState])
}
