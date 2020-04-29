package com.kanbagoly.diningphilosophers

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.kanbagoly.diningphilosophers.Fork.{Command, PickUp, PutDown, Response}

// TODO: Need Id (maybe just externally)
object Fork {

  sealed trait Command
  final case class PickUp(replyTo: ActorRef[Response]) extends Command
  final case class PutDown(replyTo: ActorRef[Response]) extends Command

  final case class Response(successful: Boolean)

  def apply(): Behavior[Command] = Behaviors.setup(new Fork(_))

}

class Fork(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  private var used: Boolean = false

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case PickUp(replyTo) =>
      context.log.info("Pick up request from {}!", replyTo.ref)
      replyTo ! Response(!used)
      used = true
      Behaviors.same
    case PutDown(replyTo) =>
      context.log.info("Put down request from {}!", replyTo.ref)
      replyTo ! Response(used)
      used = false
      Behaviors.same
  }

}
