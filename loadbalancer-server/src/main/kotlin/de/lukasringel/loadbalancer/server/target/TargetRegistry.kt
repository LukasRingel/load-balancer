package de.lukasringel.loadbalancer.server.target

import de.lukasringel.loadbalancer.server.application.console.Console
import de.lukasringel.loadbalancer.server.target.strategy.RoundRobin
import de.lukasringel.loadbalancer.server.target.strategy.Strategy

class TargetRegistry {
  /**
   * This list contains all registered targets
   */
  private val registeredTargets = mutableListOf<Target>()

  /**
   * This strategy is used to find a target
   */
  private var strategy: Strategy = RoundRobin(this)

  /**
   * This method returns all registered targets
   *
   * @return - all registered targets
   */
  fun registeredTargets(): List<Target> {
    return registeredTargets
  }

  /**
   * This method registers a new target
   *
   * @param target - the target to register
   */
  fun registerTarget(target: Target) {
    strategy.targetsUpdated()
    registeredTargets.add(target)
    Console.log("Registered new target ${target.name()} on socket ${target.address()}:${target.port()}")
  }

  /**
   * This method unregisters a new target
   *
   * @param target - the target to unregister
   * @param reason - the reason why the target got unregistered
   */
  fun unregisterTarget(target: Target, reason: TargetUnregisterCause) {
    strategy.targetsUpdated()
    registeredTargets.remove(target)
    Console.log("Unregistered target ${target.name()} with reason ${reason.name.lowercase()}")
  }

  /**
   * This method unregisters a new target
   *
   * @param target - the name of the target
   * @param reason - the reason why the target got unregistered
   */
  fun unregisterTarget(target: String, reason: TargetUnregisterCause) {
    strategy.targetsUpdated()
    registeredTargets.removeIf { it.name() == target }
    Console.log("Unregistered target $target with reason ${reason.name.lowercase()}")
  }

  fun nextTarget(): Target {
    return strategy.findAddress()
  }
}