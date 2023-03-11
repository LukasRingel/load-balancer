package de.lukasringel.loadbalancer.server.target

class Target(private val name: String, private val address: String, private val port: Int) {

  /**
   * This is the last time we received a heartbeat from this target
   */
  private var lastHeartbeat: Long = 0

  /**
   * This method returns the name of the target
   */
  fun name(): String {
    return name
  }

  /**
   * This method returns the address of the target
   */
  fun address(): String {
    return address
  }

  /**
   * This method returns the port of the target
   */
  fun port(): Int {
    return port
  }

  /**
   * This method returns the last time we received a heartbeat from this target
   */
  fun lastHeartbeat(): Long {
    return lastHeartbeat
  }

  /**
   * This method updates the last heartbeat time
   */
  fun updateHeartbeat() {
    lastHeartbeat = System.currentTimeMillis()
  }

  /**
   * This method returns the hostname of the target
   * We use this to connect to the target
   */
  fun hostname(): String {
    return "$address:$port"
  }
}