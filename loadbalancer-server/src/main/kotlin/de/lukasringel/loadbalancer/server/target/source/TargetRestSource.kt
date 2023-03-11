package de.lukasringel.loadbalancer.server.target.source

import de.lukasringel.loadbalancer.server.target.Target
import de.lukasringel.loadbalancer.server.target.TargetRegistry
import de.lukasringel.loadbalancer.server.target.TargetUnregisterCause
import spark.Spark.*

object TargetRestSource {
  fun registerRoutes(registry: TargetRegistry) {
    post("/targets/register") { request, _ ->
      {
        registry.registerTarget(
          Target(
            request.queryParams("name"),
            request.queryParams("address"),
            request.queryParams("port").toInt()
          )
        )
      }
    }
    delete("/targets/unregister") { request, _ ->
      {
        registry.unregisterTarget(
          request.queryParams("name"),
          TargetUnregisterCause.REQUESTED
        )
      }
    }
    put("/targets/heartbeat") { request, _ ->
      {
        registry.registeredTargets()
          .find { it.name() == request.queryParams("name") }
          ?.updateHeartbeat()
      }
    }
  }
}