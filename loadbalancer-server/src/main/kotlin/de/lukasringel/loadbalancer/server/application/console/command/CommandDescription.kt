package de.lukasringel.loadbalancer.server.application.console.command

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandDescription(val name: String, val description: String)
