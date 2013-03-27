package org.ajantis.vmbix

import service.VISphereActorProtocol._

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
class ZabbixCommandDecoder {

  val pStatusR = "^ZBXD[^a-zA-Z]*status\\[(.+),used\\]".r        // :checks host status (not vm)
  val pHostCpuUsedR = "^ZBXD[^a-zA-Z]*esx\\.cpu\\.load\\[(.+),used\\]".r
  val pHostCpuTotalR = "^ZBXD[^a-zA-Z]*esx\\.cpu\\.load\\[(.+),total\\]".r
  val pHostCpuCoresR = "^ZBXD[^a-zA-Z]*esx\\.cpu\\.load\\[(.+),cores\\]".r
  val pHostMemUsedR = "^ZBXD[^a-zA-Z]*esx\\.memory\\[(.+),used\\]".r
  val pHostMemTotalR = "^ZBXD[^a-zA-Z]*esx\\.memory\\[(.+),total\\]".r                            //
  val pHostMemStatsPrivateR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[(.+),private\\]".r               // :this is a heavy check. Counts average private          memory usage in % for all powered on vms.
  val pHostMemStatsSharedR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[(.+),shared\\]".r                 // :this is a heavy check. Counts average shared           memory usage in % for all powered on vms.
  val pHostMemStatsSwappedR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[(.+),swapped\\]".r               // :this is a heavy check. Counts average swapped          memory usage in % for all powered on vms.
  val pHostMemStatsCompressedR= "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[(.+),compressed\\]".r          // :this is a heavy check. Counts average compressed       memory usage in % for all powered on vms.
  val pHostMemStatsOverhConsR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[(.+),overheadConsumed\\]".r    // :this is a heavy check. Counts average overheadConsumed memory usage in % for all powered on vms.
  val pHostMemStatsConsumedR = "^^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[(.+),consumed\\]".r             // :this is a heavy check. Counts average consumed         memory usage in % for all powered on vms.
  val pHostMemStatsBaloonedR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[(.+),balooned\\]".r             // :this is a heavy check. Counts average balooned         memory usage in % for all powered on vms.
  val pHostMemStatsActiveR = "^ZBXD[^a-zA-Z]*esx\\.vms.memory\\[(.+),active\\]".r                 // :this is a heavy check. Counts average active           memory usage in % for all powered on vms.
  val pVmCpuUsedR = "^ZBXD[^a-zA-Z]*vm\\.cpu\\.load\\[(.+),used\\]".r
  val pVmCpuTotalR = "^ZBXD[^a-zA-Z]*vm\\.cpu\\.load\\[(.+),total\\]".r
  val pVmMemPrivateR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),private\\]".r
  val pVmMemSharedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),shared\\]".r
  val pVmMemSwappedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),swapped\\]".r
  val pVmMemCompressedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),compressed\\]".r
  val pVmMemOverheadConsumedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),overheadConsumed\\]".r
  val pVmMemConsumedR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),consumed\\]".r
  val pVmMemBaloonedR= "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),balooned\\]".r
  val pVmMemActiveR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),active\\]".r
  val pVmMemSizeR = "^ZBXD[^a-zA-Z]*vm\\.memory\\[(.+),total\\]".r
  val pDatastoreFreeR = "^ZBXD[^a-zA-Z]*datastore\\.size\\[(.+),free\\]".r
  val pDatastoreTotalR = "^ZBXD[^a-zA-Z]*datastore\\.size\\[(.+),total\\]".r

  @throws(classOf[ZbxCommandDecodeException])
  def decode(cmdBody: String): VISphereActorCmd = {

    cmdBody match {
      case pStatusR(host) => GetHostStatus(host)
      case pHostCpuUsedR(host) => HostCPUUsed(host)
      case pHostCpuTotalR(id, host) => HostCPUTotal(host)
      case pHostCpuCoresR(id, host) => HostCPUCores(host)
      case pHostMemUsedR(id, host) => HostMemUsed(host)
      case pHostMemTotalR(id, host) => HostMemTotal(host)
      case pHostMemStatsPrivateR(id, host) => HostMemStatsPrivate(host)
      case pHostMemStatsSharedR(id, host) => HostMemStatsShared(host)
      case pHostMemStatsSwappedR(id, host) => HostMemStatsSwapped(host)
      case pHostMemStatsCompressedR(id, host) => HostMemStatsCompressed(host)
      case pHostMemStatsOverhConsR(id, host) => HostMemStatsOverhCons(host)
      case pHostMemStatsConsumedR(id, host) => HostMemStatsConsumed(host)
      case pHostMemStatsBaloonedR(id, host) => HostMemStatsBalooned(host)
      case pHostMemStatsActiveR(id, host) => HostMemStatsActive(host)
      case pVmCpuUsedR(id, vm) => VmCPUUsed(vm)
      case pVmCpuTotalR(id, vm) => VmCPUTotal(vm)
      case pVmMemPrivateR(id, vm) => VmMemPrivate(vm)
      case pVmMemSharedR(id, vm) => VmMemShared(vm)
      case pVmMemSwappedR(id, vm) => VmMemSwapped(vm)
      case pVmMemCompressedR(id, vm) => VmMemCompressed(vm)
      case pVmMemOverheadConsumedR(id, vm) => VmMemOverheadConsumed(vm)
      case pVmMemConsumedR(id, vm) => VmMemConsumed(vm)
      case pVmMemBaloonedR(id, vm) => VmMemBallooned(vm)
      case pVmMemActiveR(id, vm) => VmMemActive(vm)
      case pVmMemSizeR(id, vm) => VmMemSize(vm)
      case pDatastoreTotalR(id, ds) => DatastoreFree(ds)
      case pDatastoreTotalR(id, ds) => DatastoreTotal(ds)
      case _ =>
        throw new ZbxCommandDecodeException(s"Command ${cmdBody} is not supported", "ZBX_NOTSUPPORTED\n")
    }
  }

}

class ZbxCommandDecodeException(msg: String, val response: String) extends Exception(msg)
