package org.ajantis.vmbix

import service.VISphereServiceProtocol._
import io.netty.channel.ChannelHandlerContext

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
class ZabbixCommandDecoder {

  val pStatusR = "^ZBXD[^a-zA-Z]*status\\[([^,]+),used\\]".r                                         // :checks host status (not vm)
  val pHostCpuUsedR = "^ZBXD[^a-zA-Z]*esx\\.cpu\\.load\\[([^,]+),used\\]".r
  val pHostCpuTotalR = "^ZBXD[^a-zA-Z]*esx\\.cpu\\.load\\[([^,]+),total\\]".r
  val pHostCpuCoresR = "^ZBXD[^a-zA-Z]*esx\\.cpu\\.load\\[([^,]+),cores\\]".r
  val pHostMemUsedR = "^ZBXD[^a-zA-Z]*esx\\.memory\\[([^,]+),used\\]".r
  val pHostMemTotalR = "^ZBXD[^a-zA-Z]*esx\\.memory\\[([^,]+),total\\]".r
  val pHostMemStatsPrivateR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[([^,]+),private\\]".r               // :this is a heavy check. Counts average private          memory usage in % for all powered on vms.
  val pHostMemStatsSharedR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[([^,]+),shared\\]".r                 // :this is a heavy check. Counts average shared           memory usage in % for all powered on vms.
  val pHostMemStatsSwappedR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[([^,]+),swapped\\]".r               // :this is a heavy check. Counts average swapped          memory usage in % for all powered on vms.
  val pHostMemStatsCompressedR= "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[([^,]+),compressed\\]".r          // :this is a heavy check. Counts average compressed       memory usage in % for all powered on vms.
  val pHostMemStatsOverhConsR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[([^,]+),overheadConsumed\\]".r    // :this is a heavy check. Counts average overheadConsumed memory usage in % for all powered on vms.
  val pHostMemStatsConsumedR = "^^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[([^,]+),consumed\\]".r            // :this is a heavy check. Counts average consumed         memory usage in % for all powered on vms.
  val pHostMemStatsBaloonedR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[([^,]+),balooned\\]".r             // :this is a heavy check. Counts average ballooned        memory usage in % for all powered on vms.
  val pHostMemStatsActiveR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[([^,]+),active\\]".r                 // :this is a heavy check. Counts average active           memory usage in % for all powered on vms.
  val pVmCpuUsedR = "^ZBXD[^a-zA-Z]*vm\\.cpu\\.load\\[([^,]+),used\\]".r
  val pVmCpuTotalR = "^ZBXD[^a-zA-Z]*vm\\.cpu\\.load\\[([^,]+),total\\]".r
  val pVmMemPrivateR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),private\\]".r
  val pVmMemSharedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),shared\\]".r
  val pVmMemSwappedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),swapped\\]".r
  val pVmMemCompressedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),compressed\\]".r
  val pVmMemOverheadConsumedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),overheadConsumed\\]".r
  val pVmMemConsumedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),consumed\\]".r
  val pVmMemBaloonedR= "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),balooned\\]".r
  val pVmMemActiveR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),active\\]".r
  val pVmMemSizeR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[([^,]+),total\\]".r
  val pDatastoreFreeR = "^ZBXD[^a-zA-Z]*datastore\\.size\\[([^,]+),free\\]".r
  val pDatastoreTotalR = "^ZBXD[^a-zA-Z]*datastore\\.size\\[([^,]+),total\\]".r

  @throws(classOf[ZbxCommandDecodeException])
  def decode(cmdBody: String, ch: ChannelHandlerContext): VISphereActorCmd = {

    cmdBody match {
      case pStatusR(host) => GetHostStatus(host, ch)
      case pHostCpuUsedR(host) => HostCPUUsed(host, ch)
      case pHostCpuTotalR(host) => HostCPUTotal(host, ch)
      case pHostCpuCoresR(host) => HostCPUCores(host, ch)
      case pHostMemUsedR(host) => HostMemUsed(host, ch)
      case pHostMemTotalR(host) => HostMemTotal(host, ch)
      case pHostMemStatsPrivateR(host) => HostMemStatsPrivate(host, ch)
      case pHostMemStatsSharedR(host) => HostMemStatsShared(host, ch)
      case pHostMemStatsSwappedR(host) => HostMemStatsSwapped(host, ch)
      case pHostMemStatsCompressedR(host) => HostMemStatsCompressed(host, ch)
      case pHostMemStatsOverhConsR(host) => HostMemStatsOverhCons(host, ch)
      case pHostMemStatsConsumedR(host) => HostMemStatsConsumed(host, ch)
      case pHostMemStatsBaloonedR(host) => HostMemStatsBalooned(host, ch)
      case pHostMemStatsActiveR(host) => HostMemStatsActive(host, ch)
      case pVmCpuUsedR(vm) => VmCPUUsed(vm, ch)
      case pVmCpuTotalR(vm) => VmCPUTotal(vm, ch)
      case pVmMemPrivateR(vm) => VmMemPrivate(vm, ch)
      case pVmMemSharedR(vm) => VmMemShared(vm, ch)
      case pVmMemSwappedR(vm) => VmMemSwapped(vm, ch)
      case pVmMemCompressedR(vm) => VmMemCompressed(vm, ch)
      case pVmMemOverheadConsumedR(vm) => VmMemOverheadConsumed(vm, ch)
      case pVmMemConsumedR(vm) => VmMemConsumed(vm, ch)
      case pVmMemBaloonedR(vm) => VmMemBallooned(vm, ch)
      case pVmMemActiveR(vm) => VmMemActive(vm, ch)
      case pVmMemSizeR(vm) => VmMemSize(vm, ch)
      case pDatastoreTotalR(ds) => DatastoreTotal(ds, ch)
      case pDatastoreFreeR(ds) => DatastoreFree(ds, ch)
      case _ =>
        throw new ZbxCommandDecodeException(s"Command ${cmdBody} is not supported", "ZBX_NOTSUPPORTED\n")
    }
  }

}

class ZbxCommandDecodeException(msg: String, val response: String) extends Exception(msg)
