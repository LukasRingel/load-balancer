package de.lukasringel.loadbalancer.server.application.console.commands

import de.lukasringel.loadbalancer.server.application.console.Console
import de.lukasringel.loadbalancer.server.application.console.command.Command
import de.lukasringel.loadbalancer.server.application.console.command.CommandDescription
import de.lukasringel.loadbalancer.server.target.TargetRegistry

@CommandDescription("targets", "Shows all registered targets")
class TargetsCommand(private val registry: TargetRegistry) : Command {
  override fun execute(args: Array<String>) {
    if (registry.registeredTargets().isEmpty()) {
      Console.log("No targets registered")
      return
    }

    registry.registeredTargets().forEach(action = { target ->
      Console.log("Name: ${target.name()} | Address: ${target.address()} | Port: ${target.port()}")
    })
  }
}