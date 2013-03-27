package org.ajantis.vmbix.service

import akka.actor._
import org.ajantis.vmbix.{ZbxCommandDecodeException, ZabbixCommandDecoder}
import io.netty.channel.{ChannelHandlerContext, ChannelFutureListener}
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.routing.FromConfig
import org.ajantis.vmbix.Session
import org.ajantis.vmbix.vsphere.VISphereConnCredentials
import scala.Some
import akka.actor.OneForOneStrategy
;
/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
class VISphereService(url: String, user: String, password: String) {
  val system = ActorSystem("akkaSystem")
  val actor = system.actorOf(Props(creator = new VISphereServiceRouter(new VISphereConnCredentials(url, user, password))), name = "vSphereService")
}

class VISphereServiceRouter(credentials: VISphereConnCredentials) extends Actor with ActorLogging{
  private val decoder = new ZabbixCommandDecoder

  val workersRouted =
    context.actorOf(Props(creator = new VISphereServiceWorker(credentials)).withRouter(FromConfig()), name = "vSphereServiceWorkerRoutedActor")

  override def supervisorStrategy() = OneForOneStrategy(){
    case _: ActorInitializationException => Escalate
    case _: Exception => Restart
  }

  def receive = {
    case Session(ctx, query) => {     
      val cmd = try {
        Some(decoder.decode(query, ctx))
      } catch {
        case e: ZbxCommandDecodeException => {
          log.error(s"Error while decoding command query: ${query}")
          val future = ctx.write(e.response)
          future.addListener(ChannelFutureListener.CLOSE)
          None
        }
      }
      cmd.foreach { c =>
        workersRouted forward c
      }
    }
  }
}

object VISphereServiceProtocol {
  abstract sealed class VISphereActorCmd(channel: ChannelHandlerContext)

  case class GetHostStatus(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostCPUUsed(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostCPUTotal(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostCPUCores(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemUsed(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemTotal(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemStatsPrivate(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemStatsShared(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemStatsSwapped(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemStatsCompressed(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemStatsOverhCons(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemStatsConsumed(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemStatsBalooned(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class HostMemStatsActive(host: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmCPUUsed(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmCPUTotal(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemPrivate(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemShared(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemSwapped(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemCompressed(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemOverheadConsumed(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemConsumed(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemBallooned(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemActive(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class VmMemSize(vm: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class DatastoreFree(ds: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)
  case class DatastoreTotal(ds: String, ch: ChannelHandlerContext) extends VISphereActorCmd(ch)

}