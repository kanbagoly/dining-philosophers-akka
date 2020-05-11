package com.kanbagoly.diningphilosophers

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import com.kanbagoly.diningphilosophers.Fork.Response.{Successful, Unsuccessful}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Random, Success}


object Philosopher {

  sealed trait Command
  final case object Eat extends Command
  final case object Rest extends Command
  private final case object PutForksDown extends Command

  def apply(leftFork: ActorRef[Fork.Command],
            rightFork: ActorRef[Fork.Command],
            numberOfTimesToEat: Int = 5): Behavior[Command] = {
    val orderedForks = orderForks(leftFork, rightFork)
    Behaviors.withTimers(timers => behavior(timers, orderedForks, numberOfTimesToEat)
  )}

  private def behavior(timers: TimerScheduler[Command],
                       forks: List[ActorRef[Fork.Command]],
                       numberOfBites: Int): Behavior[Command] = {
    Behaviors.receive {
      case (context, Eat) =>
        def tryToAcquireForks(notAcquired: List[ActorRef[Fork.Command]], acquired: List[ActorRef[Fork.Command]]): Unit =
          notAcquired match {
            case Nil =>
              context.log.info("Philosopher {} started to eat.", context.self.path.name)
              timers.startSingleTimer(PutForksDown, Random.nextInt(500).milliseconds)
            case fork::notAcquiredForks =>
              implicit val timeout: Timeout = 3.seconds
              implicit val system: ActorSystem[_] = context.system
              val result: Future[Fork.Response] = fork ? Fork.PickUp
              implicit val ec: ExecutionContextExecutor = context.system.executionContext
              result.onComplete {
                case Success(Successful) => tryToAcquireForks(notAcquiredForks, fork :: acquired)
                case Success(Unsuccessful) =>
                  acquired.foreach(_ ! Fork.PutDown)
                  context.self ! Rest
                case Failure(ex) =>
                  context.log.info("Boo! {} from {}", ex, context.self.path.name) // TODO: Do something
              }
          }
        tryToAcquireForks(forks, Nil)
        Behaviors.same
      case (context, PutForksDown) =>
        forks.foreach(_ ! Fork.PutDown)
        if (numberOfBites == 0) {
          context.log.info("Philosopher {} finished eating.", context.self.path.name)
          Behaviors.stopped
        } else {
          context.log.info("Philosopher {} finished eating. {} bites left.", context.self.path.name, numberOfBites)
          context.self ! Rest
          behavior(timers, forks, numberOfBites - 1)
        }
      case (context, Rest) =>
        context.log.info("Philosopher {} is resting.", context.self.path.name)
        timers.startSingleTimer(Eat, Random.nextInt(500).milliseconds)
        Behaviors.same
    }
  }

  private def orderForks(forks: ActorRef[Fork.Command]*): List[ActorRef[Fork.Command]] =
    forks.sortBy(_.path.name).toList

  private val NameCounter = new AtomicInteger(1)

  def nextName(): String = s"philosopher-${NameCounter.getAndIncrement()}"

}
