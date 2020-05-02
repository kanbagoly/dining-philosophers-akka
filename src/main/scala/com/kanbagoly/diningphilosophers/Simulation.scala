package com.kanbagoly.diningphilosophers

import akka.actor.typed.ActorSystem

object Simulation {

  def main(args: Array[String]): Unit = {
    ActorSystem[Nothing](Table(2), "eating-philosophers-system")
  }

}
