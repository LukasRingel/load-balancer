package de.lukasringel.loadbalancer.server.target.strategy

import de.lukasringel.loadbalancer.server.target.Target

interface Strategy {
  /**
   * Implement this method to find a target
   */
  fun findAddress(): Target

  /**
   * Gets called when the targets get updated in the registry
   */
  fun targetsUpdated()
}