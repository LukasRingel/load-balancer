package de.lukasringel.loadbalancer.server.target.strategy

import de.lukasringel.loadbalancer.server.target.Target
import de.lukasringel.loadbalancer.server.target.TargetRegistry
import java.util.concurrent.atomic.AtomicInteger

class RoundRobin(private val targetRegistry: TargetRegistry) : Strategy {
  /**
   * We use a AtomicInteger since we access the value from multiple threads
   */
  private val currentTarget = AtomicInteger(0)

  /**
   * This method will return a target from the registry in a round-robin fashion
   * It will return the first target, then the second and so on
   * If we reach the end of the list, we will start from the beginning again
   */
  override fun findAddress(): Target {
    val targets = targetRegistry.registeredTargets()
    return targets[currentTarget.getAndIncrement() % targets.size]
  }

  /**
   * Reset the current target to 0 if the target list has been updated (e.g. a new target has been added)
   * This will ensure that we start from the beginning again and don't skip any targets in the list
   */
  override fun targetsUpdated() {
    currentTarget.set(0)
  }
}