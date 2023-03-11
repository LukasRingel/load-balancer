package de.lukasringel.loadbalancer.server.netty.pipeline

import de.lukasringel.loadbalancer.server.application.console.Console
import de.lukasringel.loadbalancer.server.netty.minecraft.Action
import de.lukasringel.loadbalancer.server.netty.minecraft.Pipeline
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.net.InetSocketAddress

class ServerHandshakeDecoder(private val upstreamHandler: ServerUpstreamHandler, private val clientAddress: InetSocketAddress) :
  MessageToMessageDecoder<ByteBuf>() {

  private var protocolState: Action = Action.HANDSHAKE

  override fun decode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: MutableList<Any>?) {
    val copy = msg!!.copy()
    Pipeline.readVarInt(msg)
    val packetId: Int = Pipeline.readVarInt(msg)
    if (packetId != 0) {
      msg.skipBytes(msg.readableBytes())
      return
    }
    if (protocolState !== Action.HANDSHAKE) {
      println("Not handshake")
      out!!.add(copy.retain())
      ctx!!.channel().pipeline().remove(this)
      println("state is ${protocolState.name}")
      if (protocolState === Action.STATUS) {
        return
      }
      println("handling login")
      handleLogin(Pipeline.readString(msg, 16))
      return
    }

    val protocolVersion: Int = Pipeline.readVarInt(msg)
    val rawHostname: String = Pipeline.readString(msg, 255).lowercase()

    val port = msg.readUnsignedShort()
    val nextState: Int = Pipeline.readVarInt(msg)
    if (nextState <= 0 || nextState > 2) {
      msg.skipBytes(msg.readableBytes())
      return
    }
    protocolState = Action.getById(nextState.toByte())
    if (!upstreamHandler.createDownstreamClient(clientAddress)) {
      msg.skipBytes(msg.readableBytes())
      return
    }
    val readableBytes = msg.readableBytes()
    var name: String? = null
    if (protocolState === Action.LOGIN && readableBytes != 0) {
      try {
        name = "lvkaas"
      } catch (e: Exception) {
        println("Error while reading name: ${e.message}")
      }
    }
    val newHandshake = Unpooled.buffer()
    Pipeline.writeVarInt(packetId, newHandshake)
    Pipeline.writeVarInt(protocolVersion, newHandshake)

    Pipeline.writeString(rawHostname, newHandshake)
    newHandshake.writeShort(port)
    Pipeline.writeVarInt(nextState, newHandshake)

    val bodyLen = newHandshake.readableBytes()
    val headerLen: Int = getVarIntSize(bodyLen)

    val buffer = Unpooled.buffer(headerLen + bodyLen)
    Pipeline.writeVarInt(bodyLen, buffer)

    buffer.writeBytes(newHandshake)
    if (protocolState === Action.LOGIN && name != null) {
      Pipeline.writeString(name, buffer)
    } else if (protocolState === Action.STATUS && readableBytes != 0) {
      buffer.writeBytes(byteArrayOf(0x01, 0x00))
    }

    buffer.resetReaderIndex()
    out!!.add(buffer.retain())

    if (readableBytes != 0) {
      ctx!!.channel().pipeline().remove(this)
    }

    if (protocolState === Action.LOGIN && name != null) {
      handleLogin(name)
    }
  }

  private fun handleLogin(name: String) {
    Console.log("Player logged in: $name")
  }

  private fun getVarIntSize(paramInt: Int): Int {
    if (paramInt and -0x80 == 0) {
      return 1
    }
    if (paramInt and -0x4000 == 0) {
      return 2
    }
    if (paramInt and -0x200000 == 0) {
      return 3
    }
    return if (paramInt and -0x10000000 == 0) {
      4
    } else 5
  }
}