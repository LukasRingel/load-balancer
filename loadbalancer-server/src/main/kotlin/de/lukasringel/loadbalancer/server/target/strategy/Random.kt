package de.lukasringel.loadbalancer.server.target.strategy

import de.lukasringel.loadbalancer.server.target.Target
import de.lukasringel.loadbalancer.server.target.TargetRegistry

class Random(private val registry: TargetRegistry) : Strategy {
  /**
   * This method will return a random target from the registry
   */
  override fun findAddress(): Target {
    // We are using the random extension function from the kotlin stdlib
    return registry.registeredTargets().random()
  }

  override fun targetsUpdated() {
  }
}