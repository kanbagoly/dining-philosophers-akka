package com.kanbagoly.diningphilosophers

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object Fork {

  sealed trait Command
  final case class PickUp(replyTo: ActorRef[Response]) extends Command
  final case class PutDown(replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  object Response {
    def apply(successful: Boolean): Response =
      if (successful) Successful else Unsuccessful
    case object Successful extends Response
    case object Unsuccessful extends Response
  }

  def apply(): Behavior[Command] = Free

  private val Free: Behavior[Command] = behavior(used = false)
  private val Used: Behavior[Command] = behavior(used = true)

  private def behavior(used: Boolean): Behavior[Command] = Behaviors.receive {
    case (context, PickUp(replyTo)) =>
      context.log.info("Pick up request from {}!", replyTo.ref)
      replyTo ! Response(!used)
      Used
    case (context, PutDown(replyTo)) =>
      context.log.info("Put down request from {}!", replyTo.ref)
      replyTo ! Response(used)
      Free
  }

}
