package org.ajantis.vmbix.service

import akka.actor.{ActorSystem, Actor, Props}
import akka.event.Logging
import org.ajantis.vmbix.vsphere.VISphereConnection
import org.ajantis.vmbix.Session
import org.ajantis.vmbix.vsphere.VISphereConnCredentials
import io.netty.channel.ChannelFutureListener
import org.ajantis.vmbix.service.VISphereActorProtocol._
;
/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
class VISphereManager(url: String, user: String, password: String) {
  val system = ActorSystem("akkaSystem")
  val actor = system.actorOf(Props(creator = new VISphereActor(new VISphereConnCredentials(url, user, password))))
}

class VISphereActor(credentials: VISphereConnCredentials) extends Actor{

  private val logger = Logging(context.system, this)
  private val connection = new VISphereConnection(credentials)

  def receive = {
    case Session(ctx, command) => {
      logger.debug("We got a session request. Sending an answer...")

      val result = command match {
        case GetHostStatus(h) => connection.getHostStatus(h).toString
        case HostCPUUsed(h) => connection.getHostCpuUsed(h).getOrElse(0).toString
        case HostCPUTotal(h) => connection.getHostCpuTotal(h).getOrElse(0).toString
        case HostCPUCores(h) => connection.getHostCpuCores(h).getOrElse(0).toString
        case HostMemUsed(h) => connection.getHostMemUsed(h).getOrElse(0).toString
        case HostMemTotal(h) => connection.getHostMemTotal(h).getOrElse(0).toString
        case HostMemStatsPrivate(h) => connection.getHostVmsStatsPrivate(h).getOrElse(0).toString
        case HostMemStatsShared(h) => connection.getHostVmsStatsShared(h).getOrElse(0).toString
        case HostMemStatsSwapped(h) => connection.getHostVmsStatsSwapped(h).getOrElse(0).toString
        case HostMemStatsCompressed(h) => connection.getHostVmsStatsCompressed(h).getOrElse(0).toString
        case HostMemStatsOverhCons(h) => connection.getHostVmsStatsOverhCons(h).getOrElse(0).toString
        case HostMemStatsConsumed(h) => connection.getHostVmsStatsConsumed(h).getOrElse(0).toString
        case HostMemStatsBalooned(h) => connection.getHostVmsStatsBalooned(h).getOrElse(0).toString
        case HostMemStatsActive(h) => connection.getHostVmsStatsActive(h).getOrElse(0).toString
        case VmCPUUsed(vm) => connection.getVmCpuUsed(vm).getOrElse(0).toString
        case VmCPUTotal(vm) => connection.getVmCpuTotal(vm).getOrElse(0).toString
        case VmMemPrivate(vm) => connection.getVmMemPrivate(vm).getOrElse(0).toString
        case VmMemShared(vm) => connection.getVmMemShared(vm).getOrElse(0).toString
        case VmMemSwapped(vm) => connection.getVmMemSwapped(vm).getOrElse(0).toString
        case VmMemCompressed(vm) => connection.getVmMemCompressed(vm).getOrElse(0).toString
        case VmMemOverheadConsumed(vm) => connection.getVmMemOverheadConsumed(vm).getOrElse(0).toString
        case VmMemConsumed(vm) => connection.getVmMemConsumed(vm).getOrElse(0).toString
        case VmMemBallooned(vm) => connection.getVmMemBalooned(vm).getOrElse(0).toString
        case VmMemActive(vm) => connection.getVmMemActive(vm).getOrElse(0).toString
        case VmMemSize(vm) => connection.getVmMemSize(vm).getOrElse(0).toString
        case DatastoreFree(vm) => connection.datastoreSizeFree(vm).getOrElse(0).toString
        case DatastoreTotal(vm) => connection.datastoreSizeTotal(vm).getOrElse(0).toString
        case _ => "N/A"
      }
      logger.debug("Result is: " + result)

      // Close the connection as soon as the error message is sent.
      ctx.channel().write(result).addListener(ChannelFutureListener.CLOSE)
    }

  }
}

object VISphereActorProtocol {
  abstract sealed class VISphereActorCmd

  case class GetHostStatus(host: String) extends VISphereActorCmd
  case class HostCPUUsed(host: String) extends VISphereActorCmd
  case class HostCPUTotal(host: String) extends VISphereActorCmd
  case class HostCPUCores(host: String) extends VISphereActorCmd
  case class HostMemUsed(host: String) extends VISphereActorCmd
  case class HostMemTotal(host: String) extends VISphereActorCmd
  case class HostMemStatsPrivate(host: String) extends VISphereActorCmd
  case class HostMemStatsShared(host: String) extends VISphereActorCmd
  case class HostMemStatsSwapped(host: String) extends VISphereActorCmd
  case class HostMemStatsCompressed(host: String) extends VISphereActorCmd
  case class HostMemStatsOverhCons(host: String) extends VISphereActorCmd
  case class HostMemStatsConsumed(host: String) extends VISphereActorCmd
  case class HostMemStatsBalooned(host: String) extends VISphereActorCmd
  case class HostMemStatsActive(host: String) extends VISphereActorCmd
  case class VmCPUUsed(vm: String) extends VISphereActorCmd
  case class VmCPUTotal(vm: String) extends VISphereActorCmd
  case class VmMemPrivate(vm: String) extends VISphereActorCmd
  case class VmMemShared(vm: String) extends VISphereActorCmd
  case class VmMemSwapped(vm: String) extends VISphereActorCmd
  case class VmMemCompressed(vm: String) extends VISphereActorCmd
  case class VmMemOverheadConsumed(vm: String) extends VISphereActorCmd
  case class VmMemConsumed(vm: String) extends VISphereActorCmd
  case class VmMemBallooned(vm: String) extends VISphereActorCmd
  case class VmMemActive(vm: String) extends VISphereActorCmd
  case class VmMemSize(vm: String) extends VISphereActorCmd
  case class DatastoreFree(ds: String) extends VISphereActorCmd
  case class DatastoreTotal(ds: String) extends VISphereActorCmd

}