package org.ajantis.vmbix.service

import akka.actor.{ActorLogging, Actor}
import org.ajantis.vmbix.service.VISphereServiceProtocol._
import org.ajantis.vmbix.vsphere.{VISphereConnection, VISphereConnCredentials}
import io.netty.channel.{ChannelHandlerContext, ChannelFutureListener}

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
class VISphereServiceWorker(credentials: VISphereConnCredentials) extends Actor with ActorLogging{
  
  val connection = new VISphereConnection(credentials)
  
  def receive = {
    case GetHostStatus(h, ch) => sendResponse(ch, connection.getHostStatus(h).toString)
    case HostCPUUsed(h, ch) => sendResponse(ch, connection.getHostCpuUsed(h).getOrElse(0).toString)
    case HostCPUTotal(h, ch) => sendResponse(ch, connection.getHostCpuTotal(h).getOrElse(0).toString)
    case HostCPUCores(h, ch) => sendResponse(ch, connection.getHostCpuCores(h).getOrElse(0).toString)
    case HostMemUsed(h, ch) => sendResponse(ch, connection.getHostMemUsed(h).getOrElse(0).toString)
    case HostMemTotal(h, ch) => sendResponse(ch, connection.getHostMemTotal(h).getOrElse(0).toString)
    case HostMemStatsPrivate(h, ch) => sendResponse(ch, connection.getHostVmsStatsPrivate(h).getOrElse(0).toString)
    case HostMemStatsShared(h, ch) => sendResponse(ch, connection.getHostVmsStatsShared(h).getOrElse(0).toString)
    case HostMemStatsSwapped(h, ch) => sendResponse(ch, connection.getHostVmsStatsSwapped(h).getOrElse(0).toString)
    case HostMemStatsCompressed(h, ch) => sendResponse(ch, connection.getHostVmsStatsCompressed(h).getOrElse(0).toString)
    case HostMemStatsOverhCons(h, ch) => sendResponse(ch, connection.getHostVmsStatsOverhCons(h).getOrElse(0).toString)
    case HostMemStatsConsumed(h, ch) => sendResponse(ch, connection.getHostVmsStatsConsumed(h).getOrElse(0).toString)
    case HostMemStatsBalooned(h, ch) => sendResponse(ch, connection.getHostVmsStatsBalooned(h).getOrElse(0).toString)
    case HostMemStatsActive(h, ch) => sendResponse(ch, connection.getHostVmsStatsActive(h).getOrElse(0).toString)
    case VmCPUUsed(vm, ch) => sendResponse(ch, connection.getVmCpuUsed(vm).getOrElse(0).toString)
    case VmCPUTotal(vm, ch) => sendResponse(ch, connection.getVmCpuTotal(vm).getOrElse(0).toString)
    case VmMemPrivate(vm, ch) => sendResponse(ch, connection.getVmMemPrivate(vm).getOrElse(0).toString)
    case VmMemShared(vm, ch) => sendResponse(ch, connection.getVmMemShared(vm).getOrElse(0).toString)
    case VmMemSwapped(vm, ch) => sendResponse(ch, connection.getVmMemSwapped(vm).getOrElse(0).toString)
    case VmMemCompressed(vm, ch) => sendResponse(ch, connection.getVmMemCompressed(vm).getOrElse(0).toString)
    case VmMemOverheadConsumed(vm, ch) => sendResponse(ch, connection.getVmMemOverheadConsumed(vm).getOrElse(0).toString)
    case VmMemConsumed(vm, ch) => sendResponse(ch, connection.getVmMemConsumed(vm).getOrElse(0).toString)
    case VmMemBallooned(vm, ch) => sendResponse(ch, connection.getVmMemBalooned(vm).getOrElse(0).toString)
    case VmMemActive(vm, ch) => sendResponse(ch, connection.getVmMemActive(vm).getOrElse(0).toString)
    case VmMemSize(vm, ch) => sendResponse(ch, connection.getVmMemSize(vm).getOrElse(0).toString)
    case DatastoreFree(ds, ch) => sendResponse(ch, connection.datastoreSizeFree(ds).getOrElse(0).toString)
    case DatastoreTotal(ds, ch) => sendResponse(ch, connection.datastoreSizeTotal(ds).getOrElse(0).toString)
  }
  
  private def sendResponse(channel: ChannelHandlerContext, response: String){
    channel.write(response).addListener(ChannelFutureListener.CLOSE)
  }
  
}
