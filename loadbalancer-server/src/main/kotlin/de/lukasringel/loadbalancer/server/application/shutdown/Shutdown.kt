package de.lukasringel.loadbalancer.server.application.shutdown

object Shutdown {
  /**
   * This list contains all registered shutdown hooks
   */
  private val shutdownHooks = mutableListOf<ShutdownHook>()

  /**
   * This method registers a new shutdown hook
   * by creating a new [ShutdownHook] instance
   * with the given [hook]
   *
   * @param hook - the hook to register
   */
  fun registerShutdownHook(hook: () -> Unit) {
    shutdownHooks.add(ShutdownHook(hook))
  }

  /**
   * This method runs all registered shutdown hooks
   */
  fun shutdown() {
    shutdownHooks.forEach(ShutdownHook::run)
  }
}