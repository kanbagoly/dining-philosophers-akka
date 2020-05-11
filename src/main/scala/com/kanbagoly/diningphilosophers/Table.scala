package com.kanbagoly.diningphilosophers

import akka.actor.typed._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.kanbagoly.diningphilosophers.Philosopher.Eat

object Table {

  def apply(numberOfPhilosophers: Int): Behavior[Nothing] = {
    require(numberOfPhilosophers >= 2, "at least two philosophers need for the simulation")
    Behaviors.setup[Nothing] { context =>
      context.log.info("Eating philosophers simulation started")
      createActors(numberOfPhilosophers, context)
      behavior(numberOfPhilosophers)
    }
  }

  private def createActors(numberOfPhilosophers: Int, context: ActorContext[Nothing]): Unit = {
    val forks = List.fill(numberOfPhilosophers)(context.spawn(Fork(), Fork.nextName()))
    val forkPairs = forks zip forks.tail :+ forks.head
    forkPairs.foreach { case (leftFork, rightFork) =>
      val philosopher = context.spawn(Philosopher(leftFork, rightFork), Philosopher.nextName())
      context.watch(philosopher)
      philosopher ! Eat
    }
  }

  private def behavior(numberOfPhilosophers: Int): Behavior[Nothing] =
    Behaviors.receiveSignal[Nothing] {
      case (context, actor@Terminated(_)) =>
        context.log.info("Actor {} terminated", actor.ref.path.name)
        if (numberOfPhilosophers > 1) behavior(numberOfPhilosophers - 1) else Behaviors.stopped
      case (context, PostStop) =>
        context.log.info("Eating philosophers simulation stopped")
        Behaviors.same
    }

}