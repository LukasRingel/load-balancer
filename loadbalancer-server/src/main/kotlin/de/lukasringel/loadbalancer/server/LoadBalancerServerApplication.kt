package de.lukasringel.loadbalancer.server

import de.lukasringel.loadbalancer.server.application.console.Console
import de.lukasringel.loadbalancer.server.application.console.commands.HelpCommand
import de.lukasringel.loadbalancer.server.application.console.commands.StopCommand
import de.lukasringel.loadbalancer.server.application.console.commands.TargetsCommand
import de.lukasringel.loadbalancer.server.application.shutdown.Shutdown
import de.lukasringel.loadbalancer.server.netty.NettyServer
import de.lukasringel.loadbalancer.server.netty.NettyServerConfiguration
import de.lukasringel.loadbalancer.server.target.TargetRegistry
import de.lukasringel.loadbalancer.server.target.source.TargetRestSource

fun main(args: Array<String>) {
  val registry = TargetRegistry()
  val configuration = NettyServerConfiguration(25565, 16)
  val server = NettyServer(configuration, registry)

  Runtime.getRuntime().addShutdownHook(Thread {
    Shutdown.shutdown()
  })

  TargetRestSource.registerRoutes(registry)

  Console.registerCommand(HelpCommand())
  Console.registerCommand(StopCommand())
  Console.registerCommand(TargetsCommand(registry))

  server.start()
}