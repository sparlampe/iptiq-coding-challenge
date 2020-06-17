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

class RoundRobinBalancingStrategy extends BalancingStrategy {
  var lastProvider: Option[Provider] = None

  override def getNextProvider(providers: List[ProviderState]): Option[Provider] = {
    lastProvider = lastProvider match {
      case None => providers.headOption.map(_.providerInfo)
      case Some(provider) =>
        providers.zipWithIndex.find(p => p._1.providerInfo.id == provider.id) match {
          case None                => providers.headOption.map(_.providerInfo)
          case Some((_, position)) => Some(providers((position + 1) % providers.size).providerInfo)
        }
    }
    lastProvider
  }
}
