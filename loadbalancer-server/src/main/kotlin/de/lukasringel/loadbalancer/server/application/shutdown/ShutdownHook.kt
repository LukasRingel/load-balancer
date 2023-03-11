package de.lukasringel.loadbalancer.server.application.shutdown

class ShutdownHook(private val hook: Runnable) {

  /**
   * This boolean indicates if the hook has already been run
   */
  private var ran = false

  /**
   * This method runs the hook if it has not been run yet
   */
  fun run() {
    // We don't want to run the hook twice
    if (ran) {
      return
    }

    // Run the hook and set the ran boolean to true
    hook.run()
    ran = true
  }
}