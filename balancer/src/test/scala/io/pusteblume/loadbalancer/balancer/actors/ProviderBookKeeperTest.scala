package io.pusteblume.loadbalancer.balancer.actors

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import io.pusteblume.loadbalancer.balancer.actors.ProviderBookKeeper.{
  CannotAllocateProvider,
  GetNextProvider,
  GetProviders,
  MaxCapacityReached,
  NextAvailableProvider,
  RegisterProvider,
  Registered,
  RegisteredProviders
}
import io.pusteblume.loadbalancer.balancer.strategy.BalancingStrategy
import io.pusteblume.loadbalancer.models.{ Provider, ProviderState }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ProviderBookKeeperTest()
    extends TestKit(ActorSystem("ProviderBookKeeperTest"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

  val dummyBalancingStrategy: BalancingStrategy = new BalancingStrategy {
    override def getNextProvider(providers: List[ProviderState]): Option[Provider] = None
  }
  "BookKeeper actor" must {

    "send back confirmation if registration success" in {
      val providerBookKeeper = system.actorOf(Props(new ProviderBookKeeper(2, dummyBalancingStrategy)))
      providerBookKeeper ! RegisterProvider(Provider("someId", "someIp", 5000, 10))
      expectMsgPF() {
        case Registered("someId") => ()
      }
    }
    "send back error if max capacity reached" in {
      val providerBookKeeper = system.actorOf(Props(new ProviderBookKeeper(2, dummyBalancingStrategy)))
      ignoreMsg {
        case Registered(_) => true
      }
      providerBookKeeper ! RegisterProvider(Provider("someId1", "someIp", 5000, 10))
      providerBookKeeper ! RegisterProvider(Provider("someId2", "someIp", 5000, 10))
      providerBookKeeper ! RegisterProvider(Provider("someId3", "someIp", 5000, 10))
      expectMsgPF() {
        case MaxCapacityReached("someId3") => ()
      }
    }

    "sends currently registered providers" in {
      val providerBookKeeper = system.actorOf(Props(new ProviderBookKeeper(2, dummyBalancingStrategy)))
      ignoreMsg {
        case Registered(_) => true
      }
      providerBookKeeper ! RegisterProvider(Provider("someId1", "someIp", 5000, 10))
      providerBookKeeper ! RegisterProvider(Provider("someId2", "someIp", 5000, 10))
      providerBookKeeper ! GetProviders
      expectMsgPF() {
        case RegisteredProviders(list) if list.size == 2 => ()
      }
    }
    "send back next available provider" in {
      val dummyBalancingStrategy = new BalancingStrategy {
        override def getNextProvider(providers: List[ProviderState]): Option[Provider] =
          Some(Provider("someId", "someIp", 5000, 10))
      }
      val providerBookKeeper = system.actorOf(Props(new ProviderBookKeeper(2, dummyBalancingStrategy)))
      providerBookKeeper ! GetNextProvider
      expectMsgPF() {
        case NextAvailableProvider(Provider("someId", "someIp", 5000, 10)) => ()
      }
    }
    "send back not provider available if none is available" in {
      val dummyBalancingStrategy = new BalancingStrategy {
        override def getNextProvider(providers: List[ProviderState]): Option[Provider] = None
      }
      val providerBookKeeper = system.actorOf(Props(new ProviderBookKeeper(2, dummyBalancingStrategy)))
      providerBookKeeper ! GetNextProvider
      expectMsgPF() {
        case CannotAllocateProvider => ()
      }
    }
  }
}
