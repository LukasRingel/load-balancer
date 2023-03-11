package de.lukasringel.loadbalancer.server.netty

class NettyServerConfiguration(private val port: Int, private val workerThreads: Int) {

  /**
   * This method returns the port the server should listen on
   */
  fun port(): Int {
    return port
  }

  /**
   * This method returns the amount of worker threads the server should use
   */
  fun workerThreads(): Int {
    return workerThreads
  }
}