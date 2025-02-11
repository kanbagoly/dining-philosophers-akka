package com.kanbagoly.diningphilosophers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.kanbagoly.diningphilosophers.Fork.Response.{Successful, Unsuccessful}
import com.kanbagoly.diningphilosophers.Fork.{PickUp, PutDown, Response}
import org.scalatest.wordspec.AnyWordSpecLike

// TODO: Try BehaviorsTestKit
class ForkSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Fork actor" must {
    "able to picked up" when {
      "it is free" in {
        val pickUpProbe = createTestProbe[Response]()
        val fork = spawn(Fork())

        fork ! PickUp(pickUpProbe.ref)
        val response = pickUpProbe.receiveMessage()
        response should ===(Successful)
      }
    }
    "not be able to picked up" when {
      "it is in use" in {
        val pickUpProbe = createTestProbe[Response]()
        val fork = spawn(Fork())

        fork ! PickUp(pickUpProbe.ref)
        pickUpProbe.expectMessage(Successful)

        fork ! PickUp(pickUpProbe.ref)
        val response = pickUpProbe.receiveMessage()
        response should ===(Unsuccessful)
      }
    }
    "able to put down and picked up again" when {
      "it is in use" in {
        val pickUpProbe = createTestProbe[Response]()
        val putDownProbe = createTestProbe[Response]()
        val fork = spawn(Fork())

        fork ! PickUp(pickUpProbe.ref)
        pickUpProbe.expectMessage(Successful)

        fork ! PutDown

        fork ! PickUp(pickUpProbe.ref)
        pickUpProbe.expectMessage(Successful)
      }
    }
  }
}