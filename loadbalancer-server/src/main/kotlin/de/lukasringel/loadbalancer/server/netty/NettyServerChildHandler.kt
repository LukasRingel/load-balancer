package de.lukasringel.loadbalancer.server.netty

import de.lukasringel.loadbalancer.server.netty.pipeline.ServerHandshakeDecoder
import de.lukasringel.loadbalancer.server.netty.pipeline.ServerUpstreamHandler
import de.lukasringel.loadbalancer.server.target.TargetRegistry
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.SocketChannelConfig
import io.netty.handler.timeout.ReadTimeoutHandler
import java.util.concurrent.TimeUnit


class NettyServerChildHandler(private val registry: TargetRegistry) : ChannelInitializer<SocketChannel>() {
  override fun initChannel(ch: SocketChannel) {
    // Modify the channel config
    modifyChannelConfig(ch.config())

    val target = registry.nextTarget()

    val serverUpstreamHandler = ServerUpstreamHandler(target)
    ch.pipeline()?.addLast("handshake_decoder", ServerHandshakeDecoder(serverUpstreamHandler, ch.remoteAddress()))
    ch.pipeline()?.addLast("upstream_channel", serverUpstreamHandler)
    ch.pipeline()?.addLast("timeout_handler", ReadTimeoutHandler(-1, TimeUnit.SECONDS))
  }

  /**
   * This method modifies the channel config
   *
   * It sets the IP type of service to 0x18 (low delay)
   * It sets the allocator to the default one
   * It sets the write buffer watermarks to 256kb and 1mb
   *
   * @param channelConfig - the channel config to modify
   */
  private fun modifyChannelConfig(channelConfig: SocketChannelConfig) {
    // Set the IP type of service to 0x18 (low delay)
    channelConfig.setOption(ChannelOption.IP_TOS, 0x18)

    // Set the allocator to the default one
    channelConfig.allocator = ByteBufAllocator.DEFAULT

    // Set the write buffer watermarks to 256kb and 1mb
    channelConfig.writeBufferHighWaterMark = 2 shl 20
    channelConfig.writeBufferLowWaterMark = 2 shl 18
  }
}