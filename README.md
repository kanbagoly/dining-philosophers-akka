# dining-philosophers-akka
Solving the Dining philosophers problem 
(see: [Wikipedia](https://en.wikipedia.org/wiki/Dining_philosophers_problem "Dining philosophers problem"))
with Akka's actors

## solution
The philosophers avoid deadlocks by picking up the forks in ordered way
(the lower-numbered fork first).

### todos
 ☐ Provide test for Philosopher  
 ☐ Make Philosopher cleaner  
 ☐ Provide test for Table  