package com.kanbagoly.diningphilosophers

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}

object Philosopher {

  sealed trait Command
  final case object Eat extends Command
  final case object Rest extends Command

  def apply(leftFork: ActorRef[Fork.Command],
            rightFork: ActorRef[Fork.Command],
            numberOfTimesToEat: Int = 5): Behavior[Command] = {
  Behaviors.withTimers(timers => behavior(timers, leftFork, rightFork, numberOfTimesToEat)
  )}

  private def behavior(factory: TimerScheduler[Command],
                       leftFork: ActorRef[Fork.Command],
                       rightFork: ActorRef[Fork.Command],
                       numberOfTimesToEat: Int): Behavior[Command] = {
    Behaviors.stopped
  }

  private val NameCounter = new AtomicInteger(1)

  def nextName(): String = s"philosopher-${NameCounter.getAndIncrement()}"

}
