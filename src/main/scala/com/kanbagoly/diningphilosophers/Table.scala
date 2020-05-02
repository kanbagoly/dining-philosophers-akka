package com.kanbagoly.diningphilosophers

import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors

object Table {
  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      context.log.info("Eating philosophers simulation started")

      val philosopher = context.spawn(Philosopher(), "philosopher")
      context.watch(philosopher)

      Behaviors.receiveSignal[Nothing] {
        case (_, PostStop) =>
          context.log.info("Eating philosophers simulation stopped")
          Behaviors.same
        case (_, Terminated(`philosopher`)) =>
          Behaviors.stopped
      }
    }
}