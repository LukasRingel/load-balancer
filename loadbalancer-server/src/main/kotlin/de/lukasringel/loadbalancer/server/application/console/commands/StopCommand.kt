package de.lukasringel.loadbalancer.server.application.console.commands

import de.lukasringel.loadbalancer.server.application.console.Console
import de.lukasringel.loadbalancer.server.application.console.command.Command
import de.lukasringel.loadbalancer.server.application.console.command.CommandDescription
import de.lukasringel.loadbalancer.server.application.shutdown.Shutdown
import kotlin.system.exitProcess

@CommandDescription("stop", "Stops the server gracefully")
class StopCommand : Command {
  override fun execute(args: Array<String>) {
    Console.log("Going to stop the application...")
    Console.log("Running all shutdown hooks...")
    Shutdown.shutdown()
    Console.log("Shutdown complete!")
    exitProcess(1)
  }
}