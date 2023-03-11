package de.lukasringel.loadbalancer.server.application.console.commands

import de.lukasringel.loadbalancer.server.application.console.Console
import de.lukasringel.loadbalancer.server.application.console.command.Command
import de.lukasringel.loadbalancer.server.application.console.command.CommandDescription

@CommandDescription("help", "Shows all available commands")
class HelpCommand : Command {
  override fun execute(args: Array<String>) {
    Console.commandHandler()?.getCommands()?.forEach(action = {
      Console.log(
        "${it.value.commandDescription()?.name}: " +
          "${it.value.commandDescription()?.description}"
      )
    })
  }
}