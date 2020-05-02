package com.kanbagoly.diningphilosophers

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed._

object Table {
  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => new Table(context))
}

class Table(context: ActorContext[Nothing]) extends AbstractBehavior[Nothing](context) {
  context.log.info("Eating philosophers simulation started")

  private val philosopher: ActorRef[Nothing] = context.spawn(Philosopher(), "philosopher")
  context.watch(philosopher)

  override def onMessage(msg: Nothing): Behavior[Nothing] = Behaviors.unhandled

  override def onSignal: PartialFunction[Signal, Behavior[Nothing]] = {
    case PostStop =>
      context.log.info("Eating philosophers simulation stopped")
      this
    case Terminated(`philosopher`) =>
      Behaviors.stopped
  }
}