package de.lukasringel.loadbalancer.server.netty.pipeline

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ClientDownstreamHandler(private val upstreamChannel: Channel?) : SimpleChannelInboundHandler<ByteBuf>() {

  override fun messageReceived(ctx: ChannelHandlerContext?, msg: ByteBuf?) {
    upstreamChannel?.writeAndFlush(msg?.retain())?.addListener { future ->
      if (future.isSuccess) {
        ctx?.channel()?.read()
      } else {
        ctx?.channel()?.close()
      }
    }
  }

  override fun channelActive(ctx: ChannelHandlerContext?) {
    ctx?.read()
    ctx?.write(Unpooled.EMPTY_BUFFER)
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
    if (ctx?.channel()?.isActive == false) {
      return
    }
    ctx?.channel()?.writeAndFlush(Unpooled.EMPTY_BUFFER)
      ?.addListener(ChannelFutureListener.CLOSE)
  }

  override fun channelInactive(ctx: ChannelHandlerContext?) {
    if (upstreamChannel == null) {
      return
    }

    if (upstreamChannel.isActive) {
      upstreamChannel.writeAndFlush(Unpooled.EMPTY_BUFFER)
        ?.addListener(ChannelFutureListener.CLOSE)
    }
  }
}