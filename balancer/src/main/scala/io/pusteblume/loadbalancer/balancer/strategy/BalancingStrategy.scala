package io.pusteblume.loadbalancer.balancer.strategy

import io.pusteblume.loadbalancer.models.{ Provider, ProviderState }

import scala.util.Random

trait BalancingStrategy {
  def getNextProvider(providers: List[ProviderState]): Option[Provider]
}

class RandomBalancingStrategy extends BalancingStrategy {
  override def getNextProvider(providers: List[ProviderState]): Option[Provider] =
    providers match {
      case Nil                                          => None
      case viableProviders if viableProviders.size == 1 => Some(viableProviders(0).providerInfo)
      case viableProviders                              => Some(viableProviders(Random.between(1, viableProviders.size) - 1).providerInfo)
    }
}
