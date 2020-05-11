package com.kanbagoly.diningphilosophers

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
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
  private final case object PutDownForks extends Command

  private case class Setup(context: ActorContext[Philosopher.Command], timers: TimerScheduler[Command])

  def apply(leftFork: ActorRef[Fork.Command],
            rightFork: ActorRef[Fork.Command],
            numberOfTimesToEat: Int = 5): Behavior[Command] =
    Behaviors.setup(context =>
      Behaviors.withTimers(timers =>
        behavior(context, timers, orderForks(leftFork, rightFork), numberOfTimesToEat)
      )
    )

  private def behavior(context: ActorContext[Philosopher.Command],
                       timers: TimerScheduler[Command],
                       forks: List[ActorRef[Fork.Command]],
                       numberOfBites: Int): Behavior[Command] =
    Behaviors.receiveMessage {
      case Eat =>
        acquireForks(context, timers, forks)
        Behaviors.same
      case PutDownForks if numberOfBites == 0 =>
        forks.foreach(_ ! Fork.PutDown)
        context.log.info("Philosopher {} finished eating.", context.self.path.name)
        Behaviors.stopped
      case PutDownForks =>
        forks.foreach(_ ! Fork.PutDown)
        context.log.info("Philosopher {} finished eating. {} bites left.", context.self.path.name, numberOfBites)
        context.self ! Rest
        behavior(context, timers, forks, numberOfBites - 1)
      case Rest =>
        context.log.info("Philosopher {} is resting.", context.self.path.name)
        timers.startSingleTimer(Eat, Random.nextInt(500).milliseconds)
        Behaviors.same
    }

  private def acquireForks(context: ActorContext[Philosopher.Command],
                           timers: TimerScheduler[Command],
                           forks: List[ActorRef[Fork.Command]]): Unit = {
    def acquireForks(notAcquired: List[ActorRef[Fork.Command]], acquired: List[ActorRef[Fork.Command]]): Unit =
      notAcquired match {
        case Nil =>
          context.log.info("Philosopher {} started to eat.", context.self.path.name)
          timers.startSingleTimer(PutDownForks, Random.nextInt(500).milliseconds)
        case fork :: notAcquiredForks =>
          implicit val timeout: Timeout = 3.seconds
          implicit val system: ActorSystem[_] = context.system
          val result: Future[Fork.Response] = fork ? Fork.PickUp
          implicit val ec: ExecutionContextExecutor = context.system.executionContext
          result.onComplete {
            case Success(Successful) => acquireForks(notAcquiredForks, fork :: acquired)
            case Success(Unsuccessful) =>
              acquired.foreach(_ ! Fork.PutDown)
              context.self ! Rest
            case Failure(ex) =>
              context.log.info("Houston, we have a problem: {} from {}", ex, context.self.path.name)
          }
      }
    acquireForks(forks, Nil)
  }

  private def orderForks(forks: ActorRef[Fork.Command]*): List[ActorRef[Fork.Command]] =
    forks.sortBy(_.path.name).toList

  private val nameCounter = new AtomicInteger(1)

  def nextName(): String = s"philosopher-${nameCounter.getAndIncrement()}"

}
