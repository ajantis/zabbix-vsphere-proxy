package org.ajantis.vmbix

import java.net.InetSocketAddress
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel._
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.{IdleStateEvent, IdleStateHandler}
import io.netty.handler.codec.http._
import io.netty.channel.ChannelHandler.Sharable
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import nio.NioEventLoopGroup
import org.slf4j.LoggerFactory
import service.VISphereActorProtocol.VISphereActorCmd
import service.VISphereManager

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
object VIMonitoringServer {
  private val logger = LoggerFactory.getLogger("VIMonitoringServer")

  def main(args: Array[String]) {

    val addr = new InetSocketAddress("0.0.0.0", 8080)
    val srv = new ServerBootstrap()
    val viSphereManager = new VISphereManager("https://localhost/sdk", "user", "password")

    try {

      srv.group(new NioEventLoopGroup(), new NioEventLoopGroup())
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new VMMonChannelInitializer(viSphereManager))

      logger.info("Listening on {}:{}", addr.getAddress.getHostAddress, addr.getPort)

      srv.bind(addr.getPort).sync().channel().closeFuture().sync()
    } finally {
      srv.shutdown()
    }
  }
}

case class Session(ctx: ChannelHandlerContext, cmd: VISphereActorCmd){
  val created = new java.util.Date
}

@Sharable
class VIMonServerHandler(service: VISphereManager) extends ChannelInboundMessageHandlerAdapter[String]{

  private val logger = LoggerFactory.getLogger(classOf[VIMonServerHandler])
  private val decoder = new ZabbixCommandDecoder

  override def exceptionCaught(ctx: ChannelHandlerContext, t: Throwable){
    logger.error("Exception during context handling " + t.getMessage, t)
    ctx.close()
  }

  override def messageReceived(ctx: ChannelHandlerContext, query: String) {
    try {
      val cmd = decoder.decode(query)
      service.actor ! Session(ctx, cmd)

    } catch {
      case e: ZbxCommandDecodeException => {
        val future = ctx.write(e.response)
        future.addListener(ChannelFutureListener.CLOSE)
      }
    }
  }
}