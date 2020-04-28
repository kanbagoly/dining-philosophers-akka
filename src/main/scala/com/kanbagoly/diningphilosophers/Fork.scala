package com.kanbagoly.diningphilosophers

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

// TODO: Need Id (maybe just externally)
object Fork {

  sealed trait Command
  final case class PickUp(replyTo: ActorRef[Response]) extends Command
  final case class PutDown(replyTo: ActorRef[Response]) extends Command

  final case class Response(successful: Boolean)

  def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case PickUp(replyTo) =>
        context.log.info("Pick up request from {}!", replyTo.ref)
        replyTo ! Response(true)
        Behaviors.same
      case PutDown(replyTo) =>
        context.log.info("Put down request from {}!", replyTo.ref)
        replyTo ! Response(true)
        Behaviors.same
    }
  }

}
