package org.ajantis.vmbix

import java.net.InetSocketAddress
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel._
import io.netty.channel.ChannelHandler.Sharable
import nio.NioEventLoopGroup
import org.slf4j.LoggerFactory
import service.VISphereService
import scopt.immutable.OptionParser

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
object VIMonitoringServer {
  private val logger = LoggerFactory.getLogger("VIMonitoringServer")
  private val programName = "Zabbix vSphere proxy"
  private val programVersion = "1.0"

  def main(args: Array[String]) {

    val parser = new OptionParser[Config](programName, programVersion) {
      def options = Seq(
        intOpt("P", "port", "port to listen for Zabbix client connections") { (v: Int, c: Config) => c.copy(port = v) },
        opt("s", "serviceurl", "url of vSphere web services endpoint") { (v: String, c: Config) => c.copy(vSphereUrl = v) },
        opt("u", "username", "user with access to vSphere web services") { (v: String, c: Config) => c.copy(vSphereUser = v) },
        opt("p", "password", "user's password") { (v: String, c: Config) => c.copy(vSpherePassword = v) }
      )
    }

    parser.parse(args, Config()) map { config =>
      startService(config)
    } getOrElse {
      // arguments are bad, usage message will have been displayed
      System.exit(1)
    }
  }

  private def startService(config: Config){
    val addr = new InetSocketAddress("0.0.0.0", config.port)
    val srv = new ServerBootstrap()
    val viSphereManager = new VISphereService(config.vSphereUrl, config.vSphereUser, config.vSpherePassword)

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

case class Session(ctx: ChannelHandlerContext, query: String){
  val created = new java.util.Date
}

case class Config(port: Int = 8080, vSphereUrl: String = "https://localhost/sdk", vSphereUser: String = "", vSpherePassword: String = "")

@Sharable
class VIMonServerHandler(service: VISphereService) extends ChannelInboundMessageHandlerAdapter[String]{

  private val logger = LoggerFactory.getLogger(classOf[VIMonServerHandler])

  override def exceptionCaught(ctx: ChannelHandlerContext, t: Throwable){
    logger.error("Exception during context handling " + t.getMessage, t)
    ctx.close()
  }

  override def messageReceived(ctx: ChannelHandlerContext, query: String) {
    service.actor ! Session(ctx, query)
  }
}