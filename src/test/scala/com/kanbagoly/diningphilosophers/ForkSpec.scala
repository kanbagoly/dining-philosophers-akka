package com.kanbagoly.diningphilosophers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.kanbagoly.diningphilosophers.Fork.{PickUp, Response}
import org.scalatest.wordspec.AnyWordSpecLike

class ForkSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Fork actor" must {
    "picked up" when {
      "it is free" in {
        val pickUpProbe = createTestProbe[Response]()
        val fork = spawn(Fork())

        fork ! PickUp(pickUpProbe.ref)
        val response = pickUpProbe.receiveMessage()
        response.successful should ===(true)
      }
    }
    "not picked up" when {
      "it is in use" in {
        val pickUpProbe = createTestProbe[Response]()
        val fork = spawn(Fork())

        fork ! PickUp(pickUpProbe.ref)
        pickUpProbe.expectMessage(Response(successful = true))

        fork ! PickUp(pickUpProbe.ref)
        val response = pickUpProbe.receiveMessage()
        response.successful should ===(false)
      }
    }
  }
}