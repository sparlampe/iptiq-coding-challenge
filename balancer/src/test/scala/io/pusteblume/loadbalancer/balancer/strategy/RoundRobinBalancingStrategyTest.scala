package io.pusteblume.loadbalancer.balancer.strategy

import io.pusteblume.loadbalancer.models.{ Provider, ProviderState }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RoundRobinBalancingStrategyTest extends AnyWordSpec with Matchers {

  "The strategy" should {
    "return the next provider " in {
      val balancingStrategy = new RoundRobinBalancingStrategy
      val providers =
        List(ProviderState(Provider("someId1", "someIp", 2, 1), true), ProviderState(Provider("someId2", "someIp", 2, 1), true))
      balancingStrategy.getNextProvider(providers).get.id shouldEqual "someId1"
      balancingStrategy.getNextProvider(providers).get.id shouldEqual "someId2"
      balancingStrategy.getNextProvider(providers).get.id shouldEqual "someId1"
    }
    "skips inactive provider" in {
      val balancingStrategy = new RoundRobinBalancingStrategy
      val providers =
        List(ProviderState(Provider("someId1", "someIp", 2, 1), true), ProviderState(Provider("someId2", "someIp", 2, 1), false))
      balancingStrategy.getNextProvider(providers).get.id shouldEqual "someId1"
      balancingStrategy.getNextProvider(providers).get.id shouldEqual "someId1"
    }
  }
}
