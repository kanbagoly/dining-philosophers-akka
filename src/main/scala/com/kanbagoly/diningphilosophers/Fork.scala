package com.kanbagoly.diningphilosophers

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object Fork {

  sealed trait Command
  final case class PickUp(replyTo: ActorRef[Response]) extends Command
  final case object PutDown extends Command

  sealed trait Response
  object Response {
    def apply(successful: Boolean): Response =
      if (successful) Successful else Unsuccessful
    final case object Successful extends Response
    final case object Unsuccessful extends Response
  }

  def apply(): Behavior[Command] = Free

  private val Free: Behavior[Command] = behavior(used = false)
  private val Used: Behavior[Command] = behavior(used = true)

  private def behavior(used: Boolean): Behavior[Command] = Behaviors.receive {
    case (context, PickUp(replyTo)) =>
      context.log.info("Pick up request from {}!", replyTo.path.name)
      replyTo ! Response(!used)
      Used
    case (context, PutDown) =>
      context.log.info("Put down request received!")
      Free
  }

  private val nameCounter = new AtomicInteger(1)

  def nextName(): String = s"fork-${nameCounter.getAndIncrement()}"

}
