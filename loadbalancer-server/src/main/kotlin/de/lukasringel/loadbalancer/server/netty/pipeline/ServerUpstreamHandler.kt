package de.lukasringel.loadbalancer.server.netty.pipeline

import de.lukasringel.loadbalancer.server.application.console.Console
import de.lukasringel.loadbalancer.server.target.Target
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.timeout.ReadTimeoutHandler
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class ServerUpstreamHandler(private val target: Target) : SimpleChannelInboundHandler<ByteBuf>() {

  private var upstreamChannel: Channel? = null
  private var downstreamChannel: Channel? = null

  private var downstreamHandler: ClientDownstreamHandler? = null

  private var downstreamChannelActive = false
  private var waiting: List<ByteBuf?> = ArrayList()

  override fun messageReceived(ctx: ChannelHandlerContext?, msg: ByteBuf?) {
    if (!downstreamChannelActive) {
      waiting += msg
      ctx?.channel()?.read()
      return
    }
    downstreamChannel?.writeAndFlush(msg?.retain())?.addListener { future ->
      if (future.isSuccess) {
        ctx?.channel()?.read()
      } else {
        ctx?.channel()?.close()
      }
    }
  }

  override fun channelActive(ctx: ChannelHandlerContext?) {
    upstreamChannel = ctx?.channel()
    upstreamChannel?.read()
  }

  override fun channelInactive(ctx: ChannelHandlerContext?) {
    if (downstreamChannel == null) {
      return
    }

    if (downstreamChannel?.isActive == true) {
      downstreamChannel?.writeAndFlush(Unpooled.EMPTY_BUFFER)
        ?.addListener(ChannelFutureListener.CLOSE)
    }
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
    if (ctx?.channel()?.isActive == false) {
      return
    }

    if (downstreamChannel?.isActive == false) {
      return
    }

    downstreamChannel?.writeAndFlush(Unpooled.EMPTY_BUFFER)
      ?.addListener(ChannelFutureListener.CLOSE)
  }

  fun createDownstreamClient(clientHost: InetSocketAddress) : Boolean {
    upstreamChannel.let { upstreamChannel ->

      val bootstrap = Bootstrap()
      bootstrap.group(upstreamChannel?.eventLoop())
        .channel(upstreamChannel?.javaClass)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .handler(object : ChannelInitializer<Channel>() {
          override fun initChannel(ch: Channel?) {

            val channelConfig = ch?.config()

            try {
              channelConfig?.setOption(ChannelOption.IP_TOS, 0x18)
            } catch (ignored: Exception) {
            }

            channelConfig?.allocator = ByteBufAllocator.DEFAULT
            channelConfig?.writeBufferHighWaterMark = 2 shl 20
            channelConfig?.writeBufferLowWaterMark = 2 shl 18

            downstreamHandler = ClientDownstreamHandler(upstreamChannel)

            ch?.pipeline()?.addLast("timeout_handler", ReadTimeoutHandler(-1, TimeUnit.MILLISECONDS))
            ch?.pipeline()?.addLast("handler", downstreamHandler)
          }
        })

      val channelFuture = bootstrap.connect(target.address(), target.port())
      downstreamChannel = channelFuture.channel()
      channelFuture.addListener { future ->
        if (future.isSuccess) {
          downstreamChannelActive = true
          waiting.forEach { byteBuf ->
            downstreamChannel?.writeAndFlush(byteBuf)
          }
          Console.log("Redirected client $clientHost to ${target.address()}:${target.port()}")
        } else {
          upstreamChannel?.close()
          Console.error("Failed to connect client $clientHost to ${target.address()}:${target.port()}")
        }
      }
    }

    return true
  }
}