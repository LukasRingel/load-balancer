package de.lukasringel.loadbalancer.server.target

enum class TargetUnregisterCause {
  TIMEOUT,
  REQUESTED,
  UNKNOWN
}