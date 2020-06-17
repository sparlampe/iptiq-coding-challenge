package io.pusteblume.loadbalancer.balancer.strategy

import io.pusteblume.loadbalancer.models.{ Provider, ProviderState }

import scala.util.Random

trait BalancingStrategy {
  def getNextProvider(providers: List[ProviderState]): Option[Provider]
}

class RandomBalancingStrategy extends BalancingStrategy {
  override def getNextProvider(providers: List[ProviderState]): Option[Provider] =
    providers.filter(_.isActive) match {
      case Nil                                          => None
      case viableProviders if viableProviders.size == 1 => Some(viableProviders(0).providerInfo)
      case viableProviders                              => Some(viableProviders(Random.between(1, viableProviders.size) - 1).providerInfo)
    }
}

class RoundRobinBalancingStrategy extends BalancingStrategy {
  var lastProvider: Option[Provider] = None

  def findNextViableProvider(providers: List[ProviderState], startAt: Int, analyzed: Int): Option[Provider] = {
    val providerCount = providers.size
    analyzed match {
      case `providerCount` => None
      case _ => {
        val provider = providers(startAt % providerCount)
        if (provider.isActive) {
          Some(provider.providerInfo)
        } else {
          findNextViableProvider(providers, startAt + 1, analyzed + 1)
        }
      }
    }
  }

  override def getNextProvider(providers: List[ProviderState]): Option[Provider] = {
    lastProvider = lastProvider match {
      case None => findNextViableProvider(providers, 0, 0)
      case Some(provider) =>
        providers.zipWithIndex.find(p => p._1.providerInfo.id == provider.id) match {
          case None                => findNextViableProvider(providers, 0, 0)
          case Some((_, position)) => findNextViableProvider(providers, position + 1, 0)
        }
    }
    lastProvider
  }
}
