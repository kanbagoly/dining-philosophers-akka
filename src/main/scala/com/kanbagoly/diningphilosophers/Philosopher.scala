package com.kanbagoly.diningphilosophers

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration._
import scala.util.Random


object Philosopher {

  sealed trait Command
  final case object Eat extends Command
  final case object Rest extends Command

  def apply(leftFork: ActorRef[Fork.Command],
            rightFork: ActorRef[Fork.Command],
            numberOfTimesToEat: Int = 5): Behavior[Command] = {
  Behaviors.withTimers(timers => behavior(timers, leftFork, rightFork, numberOfTimesToEat)
  )}

  private def behavior(timers: TimerScheduler[Command],
                       leftFork: ActorRef[Fork.Command],
                       rightFork: ActorRef[Fork.Command],
                       numberOfBites: Int): Behavior[Command] = {
    Behaviors.receive {
      case (context, Eat) if numberOfBites > 1 =>
        context.log.info("Philosopher {} is eating. {} bites left.", context.self.path.name, numberOfBites - 1)
        timers.startSingleTimer(Rest, Random.nextInt(500).milliseconds)
        behavior(timers, leftFork, rightFork, numberOfBites - 1)
      case (context, Eat) =>
        context.log.info("Philosopher {} finished eating. {} bites left.", context.self.path.name)
        Behaviors.stopped
      case (context, Rest) =>
        context.log.info("Philosopher {} is resting.", context.self.path.name)
        timers.startSingleTimer(Eat, Random.nextInt(500).milliseconds)
        Behaviors.same
    }
  }

  private val NameCounter = new AtomicInteger(1)

  def nextName(): String = s"philosopher-${NameCounter.getAndIncrement()}"

}
