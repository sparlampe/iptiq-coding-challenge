package io.pusteblume.loadbalancer.balancer.actors

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit, TestProbe }
import io.pusteblume.loadbalancer.balancer.actors.Poller.{ RegisterProviderForPolling, SkippedHeartBeat }
import scala.concurrent.duration._
import io.pusteblume.loadbalancer.models.{ Provider }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PollerTest()
    extends TestKit(ActorSystem("PollerTest"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

  "Poller actor" ignore {
    "send notification about skipped heartbeat" in {
      val probe  = TestProbe()
      val poller = system.actorOf(Props(new Poller(probe.ref, 1)))
      poller ! RegisterProviderForPolling(Provider("someId", "localhost", 5000, 10))
      probe.expectMsgPF(10.second) {
        case SkippedHeartBeat("someId") => ()
      }
    }
  }
}
