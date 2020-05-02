package com.kanbagoly.diningphilosophers

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Philosopher {

  sealed trait Command
  final case object StartEating extends Command

  def apply(): Behavior[Command] =
    Behaviors.stopped

}
