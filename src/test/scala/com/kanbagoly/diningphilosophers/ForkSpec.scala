package com.kanbagoly.diningphilosophers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.kanbagoly.diningphilosophers.Fork.{PickUp, PutDown, Response}
import org.scalatest.wordspec.AnyWordSpecLike

class ForkSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Fork actor" must {
    "able to picked up" when {
      "it is free" in {
        val pickUpProbe = createTestProbe[Response]()
        val fork = spawn(Fork())

        fork ! PickUp(pickUpProbe.ref)
        val response = pickUpProbe.receiveMessage()
        response.successful should ===(true)
      }
    }
    "not be able to picked up" when {
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
    "able to put down" when {
      "it is in use" in {
        val pickUpProbe = createTestProbe[Response]()
        val putDownProbe = createTestProbe[Response]()
        val fork = spawn(Fork())

        fork ! PickUp(pickUpProbe.ref)
        pickUpProbe.expectMessage(Response(successful = true))

        fork ! PutDown(putDownProbe.ref)
        val response = putDownProbe.receiveMessage()
        response.successful should ===(true)
      }
    }
    "not be able to put down" when {
      "it is free" in {
        val putDownProbe = createTestProbe[Response]()
        val fork = spawn(Fork())

        fork ! PutDown(putDownProbe.ref)
        val response = putDownProbe.receiveMessage()
        response.successful should ===(false)
      }
    }
  }
}