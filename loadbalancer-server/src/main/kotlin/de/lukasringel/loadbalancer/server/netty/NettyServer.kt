package de.lukasringel.loadbalancer.server.netty

import de.lukasringel.loadbalancer.server.application.console.Console
import de.lukasringel.loadbalancer.server.application.shutdown.Shutdown
import de.lukasringel.loadbalancer.server.target.TargetRegistry
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

class NettyServer(private val configuration: NettyServerConfiguration, private val registry: TargetRegistry) {

  /**
   * We use these two groups to handle the connections
   *
   * The boss group handles the incoming connections
   * The worker group handles the incoming requests
   */
  private var bossGroup: EventLoopGroup? = null
  private var workerGroup: EventLoopGroup? = null

  init {
    // register a shutdown hook
    Shutdown.registerShutdownHook { stop() }
  }

  /**
   * This method starts the netty server
   * It creates the groups and the bootstrap
   * It binds the server to the port and waits for connections
   */
  fun start() {
    Console.log("Starting netty server on port ${configuration.port()}")

    // create the groups
    bossGroup = NioEventLoopGroup(1)
    workerGroup = NioEventLoopGroup(configuration.workerThreads())

    // create the bootstrap
    val bootstrap = ServerBootstrap()
    bootstrap.group(bossGroup, workerGroup)
      // we use the NioServerSocketChannel
      .channel(NioServerSocketChannel::class.java)
      // no delay between the requests
      .option(ChannelOption.TCP_NODELAY, true)
      // number of connections that can be queued
      .option(ChannelOption.SO_BACKLOG, 128)
      // use the clients address
      .option(ChannelOption.SO_REUSEADDR, true)
      // no delay between the requests
      .childOption(ChannelOption.TCP_NODELAY, true)
      // we don't want to use auto read because we want to handle the requests manually
      .childOption(ChannelOption.AUTO_READ, false)
      // timeout to 4000ms to prevent the clients from disconnecting to fast
      //.childOption(ChannelOption.SO_TIMEOUT, 4000)
      // we use our own child handler
      .childHandler(NettyServerChildHandler(registry))

    // bind the server to the port and wait for connections
    val channelFuture = bootstrap.bind(configuration.port()).sync()

    Console.log("Started netty server on port ${configuration.port()}")
    Console.log("Waiting for connections")

    // wait for the server to close
    channelFuture.channel().closeFuture().sync()
  }

  /**
   * This method stops the netty server
   */
  private fun stop() {
    Console.log("Stopping netty server")
    bossGroup?.shutdownGracefully()
    workerGroup?.shutdownGracefully()
    bossGroup = null
    workerGroup = null
    Console.log("Stopped netty server")
  }
}