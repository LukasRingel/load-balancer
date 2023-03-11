package de.lukasringel.loadbalancer.server.netty.minecraft

enum class Action(private val id: Byte) {

  HANDSHAKE(0),
  STATUS(1),
  LOGIN(2);

  fun getId(): Byte {
    return id
  }

  companion object {
    fun getById(id: Byte): Action {
      return values().first { it.id == id }
    }
  }
}