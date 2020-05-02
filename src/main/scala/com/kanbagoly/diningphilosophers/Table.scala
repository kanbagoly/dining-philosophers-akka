package com.kanbagoly.diningphilosophers

import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors

object Table {
  def apply(numberOfPhilosophers: Int): Behavior[Nothing] = {
    require(numberOfPhilosophers >= 2, "at least two philosophers need for the simulation")
    Behaviors.setup[Nothing] { context =>
      context.log.info("Eating philosophers simulation started")
      (1 to numberOfPhilosophers).foreach { id =>
        val philosopher = context.spawn(Philosopher(), s"philosopher-$id")
        context.watch(philosopher)
      }
      behavior(numberOfPhilosophers)
    }
  }

  private def behavior(numberOfPhilosophers: Int): Behavior[Nothing] =
    Behaviors.receiveSignal[Nothing] {
      case (context, PostStop) =>
        context.log.info("Eating philosophers simulation stopped")
        Behaviors.same
      case (context, t@Terminated(_)) =>
        context.log.info("Actor {} terminated", t.ref)
        if (numberOfPhilosophers > 1) behavior(numberOfPhilosophers - 1) else Behaviors.stopped
    }
}