package io.pusteblume.loadbalancer.balancer.strategy

import io.pusteblume.loadbalancer.models.{ Provider, ProviderState }

trait BalancingStrategy {
  def getNextProvider(providers: List[ProviderState]): Option[Provider]
}
