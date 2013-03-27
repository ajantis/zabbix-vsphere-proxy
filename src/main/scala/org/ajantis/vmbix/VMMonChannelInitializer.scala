package org.ajantis.vmbix

import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import io.netty.buffer.BufType
import io.netty.channel.socket.SocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer}
import service.VISphereService
import io.netty.handler.timeout.{IdleStateEvent, IdleStateHandler}
import io.netty.handler.codec.{Delimiters, DelimiterBasedFrameDecoder}

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
class VMMonChannelInitializer(service: VISphereService) extends ChannelInitializer[SocketChannel] {

  private final val DECODER: StringDecoder = new StringDecoder()
  private final val ENCODER: StringEncoder = new StringEncoder(BufType.BYTE)

  def initChannel(ch: SocketChannel) {
    val pipeline = ch.pipeline

    pipeline.addLast("timeout", new IdleStateHandler(0, 110, 0){
      override def channelIdle(ctx: ChannelHandlerContext, edx: IdleStateEvent) {
        ctx.channel.close
      }
    })

    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter:_*))
    pipeline.addLast("decoder", DECODER)
    pipeline.addLast("encoder", ENCODER)
    pipeline.addLast("handler", new VIMonServerHandler(service))
  }

}