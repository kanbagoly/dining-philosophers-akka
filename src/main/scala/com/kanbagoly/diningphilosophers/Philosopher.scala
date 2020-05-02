package com.kanbagoly.diningphilosophers

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Philosopher {

  sealed trait Command
  final case object StartEating extends Command

  def apply(leftFork: ActorRef[Fork.Command], rightFork: ActorRef[Fork.Command]): Behavior[Command] =
    Behaviors.stopped

  private val NameCounter = new AtomicInteger(1)

  def nextName(): String = s"philosopher-${NameCounter.getAndIncrement()}"

}
