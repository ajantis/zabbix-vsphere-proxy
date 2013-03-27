package org.ajantis.vmbix.vsphere

import com.vmware.vim25._
import com.vmware.vim25.mo.util.MorUtil
import exception.VISphereConnectionException
import mo._
import org.slf4j.LoggerFactory
import java.net.URL

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
class VISphereConnection(credentials: VISphereConnCredentials) {
  private val logger = LoggerFactory.getLogger(classOf[VISphereConnection])

  private val serviceInstance: ServiceInstance = connect(credentials)
  private val inventoryNavigator: InventoryNavigator = new InventoryNavigator(serviceInstance.getRootFolder)

  private val VirtualMachine = "VirtualMachine"
  private val HostSystem = "HostSystem"
  private val poweredOn = "poweredOn"
  private val Datastore = "Datastore"
  
  private def getManagedEntityByName(vmName: String, meType: String): Option[ManagedEntity] = {
    Option(inventoryNavigator.searchManagedEntity(meType, vmName))
  }

  private def getHostMHZ(host: HostSystem): Option[Int] = {
    val hls: HostListSummary = host.getSummary
    val hosthwi: HostHardwareSummary = hls.getHardware
    Option(hosthwi.getCpuMhz)
  }

  def getHostStatus(hostName: String): Int = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host match {
      case Some(h: HostSystem) => {
        val hscs: HostSystemConnectionState = h.getRuntime.getConnectionState

        hscs.name match {
          case "connected" => 0
          case "disconnected" => 1
          case _ => 2
        }
      }
      case _ => 3
    }
  }

  def getHostCpuUsed(hostName: String): Option[Int] = {
    val host = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.flatMap { h =>
      val hostQuickStats: HostListSummaryQuickStats = h.getSummary.getQuickStats
      Option(hostQuickStats.getOverallCpuUsage)
    }
  }

  def getHostCpuTotal(hostName: String): Option[Int] = {
    val host = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])
    host.flatMap(getHostMHZ _)
  }

  def getHostCpuCores(hostName: String): Option[Short] = {
    val host = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])
    host.flatMap { h =>
      val hls: HostListSummary = h.getSummary
      val hosthwi: HostHardwareSummary = hls.getHardware
      Option(hosthwi.getNumCpuCores)
    }
  }

  def getHostMemUsed(hostName: String): Option[Int] = {
    val host = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])
    host.flatMap { h =>
      val hostQuickStats: HostListSummaryQuickStats = h.getSummary.getQuickStats
      Option(hostQuickStats.getOverallMemoryUsage)
    }
  }

  def getHostMemTotal(hostName: String): Option[Long] = {
    val host = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])
    host.flatMap { h =>
      val hosthwi: HostHardwareSummary = h.getSummary.getHardware
      Option(hosthwi.getMemorySize)
    }
  }

  def getHostVmsStatsPrivate(hostName: String): Option[Int] = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.map{ h: HostSystem =>
      val activeVMStats = h.getVms.filter(_.getRuntime.getPowerState.name == poweredOn).map{ v =>
        val summary = v.getSummary
        summary.getQuickStats.getSharedMemory * 100 / summary.getConfig.getMemorySizeMB
      }

      if (activeVMStats.isEmpty) 0
      else activeVMStats.foldLeft(0)(_ + _) / activeVMStats.size
    }
  }

  def getHostVmsStatsShared(hostName: String): Option[Int] = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.map{ h: HostSystem =>
      val activeVMStats = h.getVms.filter(_.getRuntime.getPowerState.name == poweredOn).map{ v =>
        val summary = v.getSummary
        (summary.getQuickStats.getSharedMemory / 1024) * 100 / summary.getConfig.getMemorySizeMB
      }

      if (activeVMStats.isEmpty) 0
      else activeVMStats.foldLeft(0)(_ + _) / activeVMStats.size
    }
  }

  def getHostVmsStatsSwapped(hostName: String): Option[Int] = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.map{ h: HostSystem =>
      val activeVMStats = h.getVms.filter(_.getRuntime.getPowerState.name == poweredOn).map{ v =>
        val summary = v.getSummary
        (summary.getQuickStats.getSwappedMemory / 1024) * 100 / summary.getConfig.getMemorySizeMB
      }

      if (activeVMStats.isEmpty) 0
      else activeVMStats.foldLeft(0)(_ + _) / activeVMStats.size
    }
  }

  def getHostVmsStatsCompressed(hostName: String): Option[Int] = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.map{ h: HostSystem =>
      val activeVMStats = h.getVms.filter(_.getRuntime.getPowerState.name == poweredOn).map{ v =>
        val summary = v.getSummary
        (summary.getQuickStats.getConsumedOverheadMemory / 1024) * 100 / summary.getConfig.getMemorySizeMB
      }

      if (activeVMStats.isEmpty) 0
      else activeVMStats.foldLeft(0)(_ + _) / activeVMStats.size
    }
  }

  def getHostVmsStatsOverhCons(hostName: String): Option[Int] = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.map{ h: HostSystem =>
      val activeVMStats = h.getVms.filter(_.getRuntime.getPowerState.name == poweredOn).map{ v =>
        val summary = v.getSummary
        summary.getQuickStats.getConsumedOverheadMemory * 100 / summary.getConfig.getMemorySizeMB
      }

      if (activeVMStats.isEmpty) 0
      else activeVMStats.foldLeft(0)(_ + _) / activeVMStats.size
    }
  }

  def getHostVmsStatsConsumed(hostName: String): Option[Int] = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.map{ h: HostSystem =>
      val activeVMStats = h.getVms.filter(_.getRuntime.getPowerState.name == poweredOn).map{ v =>
        val summary = v.getSummary
        summary.getQuickStats.getHostMemoryUsage * 100 / summary.getConfig.getMemorySizeMB
      }

      if (activeVMStats.isEmpty) 0
      else activeVMStats.foldLeft(0)(_ + _) / activeVMStats.size
    }
  }

  def getHostVmsStatsBalooned(hostName: String): Option[Int] = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.map{ h: HostSystem =>
      val activeVMStats = h.getVms.filter(_.getRuntime.getPowerState.name == poweredOn).map{ v =>
        val summary = v.getSummary
        summary.getQuickStats.getBalloonedMemory * 100 / summary.getConfig.getMemorySizeMB
      }

      if (activeVMStats.isEmpty) 0
      else activeVMStats.foldLeft(0)(_ + _) / activeVMStats.size
    }
  }

  def getHostVmsStatsActive(hostName: String): Option[Int] = {
    val host: Option[HostSystem] = getManagedEntityByName(hostName, HostSystem).map(_.asInstanceOf[HostSystem])

    host.map{ h: HostSystem =>
      val activeVMStats = h.getVms.filter(_.getRuntime.getPowerState.name == poweredOn).map{ v =>
        val summary = v.getSummary
        summary.getQuickStats.getGuestMemoryUsage * 100 / summary.getConfig.getMemorySizeMB
      }

      if (activeVMStats.isEmpty) 0
      else activeVMStats.foldLeft(0)(_ + _) / activeVMStats.size
    }
  }

  def getVmCpuUsed(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getOverallCpuUsage)
  }

  def getVmCpuTotal(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.flatMap( v => Option(v.getRuntime.getHost).flatMap{ h =>
      val hostSystem = MorUtil.createExactManagedEntity(serviceInstance.getServerConnection, h).asInstanceOf[HostSystem]
      getHostMHZ(hostSystem)
    })
  }

  def getVmMemPrivate(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getPrivateMemory)
  }

  def getVmMemShared(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getSharedMemory)
  }

  def getVmMemSwapped(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getSwappedMemory)
  }

  def getVmMemCompressed(vmName: String): Option[Long] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getCompressedMemory)
  }

  def getVmMemOverheadConsumed(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getConsumedOverheadMemory)
  }

  def getVmMemConsumed(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getHostMemoryUsage)
  }

  def getVmMemBalooned(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getBalloonedMemory)
  }

  def getVmMemActive(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getQuickStats.getGuestMemoryUsage)
  }

  def getVmMemSize(vmName: String): Option[Int] = {
    val vm: Option[VirtualMachine] = getManagedEntityByName(vmName, VirtualMachine).map(_.asInstanceOf[VirtualMachine])
    vm.map(_.getSummary.getConfig.getMemorySizeMB)
  }

  def datastoreSizeFree(dsName: String): Option[Long] = {
    getDataStore(dsName).flatMap(d => Option(d.getSummary)).map(_.getFreeSpace)
  }

  def datastoreSizeTotal(dsName: String): Option[Long] = {
    getDataStore(dsName).flatMap(d => Option(d.getSummary)).map(_.getCapacity)
  }

  private def connect(credentials: VISphereConnCredentials): ServiceInstance = {
    val si = new ServiceInstance(new URL(credentials.url), credentials.user, credentials.password, true)
    if (si == null) {
      throw new VISphereConnectionException(s"Connection to vSphere instance at ${credentials.url} is failed", null)
    }
    si
  }

  private def getDataStore(dsName: String): Option[Datastore] = {
    val ds: Option[Datastore] = getManagedEntityByName(dsName, Datastore).map(_.asInstanceOf[Datastore])
    if (ds.isEmpty)
      logger.error("Datastore named {} is not found", dsName)
    ds
  }
}

case class VISphereConnCredentials(url: String, user: String, password: String)


