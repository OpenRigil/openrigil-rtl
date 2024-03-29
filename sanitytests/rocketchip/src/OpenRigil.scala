package openrigil

import chisel3._
import chisel3.util._

import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.rocket._
import freechips.rocketchip.tile._
import freechips.rocketchip.util._

import org.chipsalliance.rocketchip.blocks.devices.usb._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.spi._
import org.chipsalliance.rocketchip.blocks.devices.montgomery._

import os._

// it is required that frequency be multiple of 12000000
// for USB
object Const {
  val frequency = 60000000
}

class OpenRigilConfig extends Config ((site, here, up) => {
  // Tile parameters
  case PgLevels => 2 /* Sv32, actually useless */
  case XLen => 32
  case MaxHartIdBits => log2Up(site(TilesLocated(InSubsystem)).map(_.tileParams.hartId).max+1)
  // rocket tile
  case RocketTilesKey => List(RocketTileParams(
      core = RocketCoreParams(
        useCryptoNIST = true,
        useVM = false,
        fpu = None,
        mulDiv = Some(MulDivParams(mulUnroll = 8))),
      btb = None,
      dcache = Some(DCacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 1024, // 64Kb scratchpad
        nWays = 1,
        nTLBSets = 1,
        nTLBWays = 4,
        nMSHRs = 0,
        blockBytes = site(CacheBlockBytes),
        scratch = Some(0x80000000L))),
      icache = Some(ICacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 64,
        nWays = 1,
        nTLBSets = 1,
        nTLBWays = 4,
        blockBytes = site(CacheBlockBytes)))))
  case RocketCrossingKey => List(RocketCrossingParams(
    crossingType = SynchronousCrossing(),
    master = TileMasterPortParams()
  ))
  // Interconnect parameters
  case SystemBusKey => SystemBusParams(
    beatBytes = site(XLen)/8,
    blockBytes = site(CacheBlockBytes))
  case ControlBusKey => PeripheryBusParams(
    beatBytes = site(XLen)/8,
    blockBytes = site(CacheBlockBytes),
    errorDevice = Some(BuiltInErrorDeviceParams(
      errorParams = DevNullParams(List(AddressSet(0x3000, 0xfff)), maxAtomic=site(XLen)/8, maxTransfer=4096))))
  case PeripheryBusKey => PeripheryBusParams(
    beatBytes = site(XLen)/8,
    blockBytes = site(CacheBlockBytes),
    dtsFrequency = Some(Const.frequency)) // 60MHz in our case
  case MemoryBusKey => MemoryBusParams(
    beatBytes = site(XLen)/8,
    blockBytes = site(CacheBlockBytes))
  case FrontBusKey => FrontBusParams(
    beatBytes = site(XLen)/8,
    blockBytes = site(CacheBlockBytes))
  // IncoherentBusTopology
  case TLNetworkTopologyLocated(InSubsystem) => List(
    JustOneBusTopologyParams(sbus = site(SystemBusKey)),
    HierarchicalBusTopologyParams(
      pbus = site(PeripheryBusKey),
      fbus = site(FrontBusKey),
      cbus = site(ControlBusKey),
      xTypes = SubsystemCrossingParams(
        sbusToCbusXType = site(SbusToCbusXTypeKey),
        cbusToPbusXType = site(CbusToPbusXTypeKey),
        fbusToSbusXType = site(FbusToSbusXTypeKey)),
      driveClocksFromSBus = site(DriveClocksFromSBus)))
  // ROM
  case BootROMLocated(InSubsystem) =>
    Some(BootROMParams(contentFileName = {
      val tmp = os.temp.dir()
      val elf = tmp / "bootrom.elf"
      val bin = tmp / "bootrom.bin"
      val img = tmp / "bootrom.img"
      // format: off
      proc(
        "clang",
        "--target=riscv64", "-march=rv64gc",
        "-mno-relax",
        "-static",
        "-nostdlib",
        "-Wl,--no-gc-sections",
        "-fuse-ld=lld", s"-T${sanitytests.rocketchip.resource("linker.ld")}",
        s"${sanitytests.rocketchip.resource("bootrom.S")}",
        "-o", elf
      ).call()
      proc(
        "llvm-objcopy",
        "-O", "binary",
        elf,
        bin
      ).call()
      proc(
        "dd",
        s"if=$bin",
        s"of=$img",
        "bs=128",
        "count=1"
      ).call()
      // format: on
      img.toString()
    }, hang = 0x10000))
  case MaskROMLocated(InSubsystem) => Seq(MaskROMParams(address = 0x100000, name="usbbootrom", depth=288 * 1024 / 4, width=32))
  // periphery
  case PeripheryUSBKey => Some(USBParams(baseAddress = 0x5000, txEpNum = 5, sampleRate = Const.frequency / 12000000))
  case PeripheryUARTKey => Seq(UARTParams(address = 0x6000, nTxEntries = 64))
  case PeripherySPIFlashKey => Seq(SPIFlashParams(rAddress = 0x7000, fAddress = 0x60000000))
  case MontgomeryKey => Some(MontgomeryParams(
    baseAddress = 0x2000,
    width = 32,
    //block = 8, // 8 * 32 = 256, so 256 bit mmm
    block = 64, // 64 * 32 = 2048, so 2048 bit mmm
    //block = 128, // 128 * 32 = 4096, so 4096 bit mmm
    freqDiv = 1,
    addPipe = 1))
  // Additional device Parameters
  case ClockGateModelFile => Some("/vsrc/EICG_wrapper.v")
  case SubsystemExternalResetVectorKey => false
  case DebugModuleKey => None // disable debug module for simplicity
  case CLINTKey => Some(CLINTParams())
  case PLICKey => Some(PLICParams())
  case TilesLocated(InSubsystem) => 
    LegacyTileFieldHelper(site(RocketTilesKey), site(RocketCrossingKey), RocketTileAttachParams.apply _)
  case DTSModel => "freechips,rocketchip-unknown"
  case DTSCompat => Nil
  case DTSTimebase => BigInt(1000000) // 1MHz
})

/** Example Top with periphery devices and ports, and a Rocket subsystem */
class OpenRigilSystem(implicit p: Parameters) extends RocketSubsystem
    with HasAsyncExtInterrupts
    with CanHaveMasterAXI4MemPort
    with CanHaveMasterAXI4MMIOPort
    with CanHaveSlaveAXI4Port
    with org.chipsalliance.rocketchip.blocks.devices.usb.CanHavePeripheryUSB
    with sifive.blocks.devices.uart.HasPeripheryUART
    with sifive.blocks.devices.spi.HasPeripherySPIFlash
    with org.chipsalliance.rocketchip.blocks.devices.montgomery.CanHavePeripheryMontgomery
{
  // optionally add ROM devices
  // Note that setting BootROMLocated will override the reset_vector for all tiles
  val bootROM  = p(BootROMLocated(location)).map { BootROM.attach(_, this, CBUS) }
  val maskROMs = p(MaskROMLocated(location)).map { MaskROM.attach(_, this, CBUS) }

  override lazy val module = new OpenRigilSystemModuleImp(this)
}

class OpenRigilSystemModuleImp[+L <: OpenRigilSystem](_outer: L) extends RocketSubsystemModuleImp(_outer)
    with HasRTCModuleImp
    with HasExtInterruptsModuleImp
    with DontTouch
    with org.chipsalliance.rocketchip.blocks.devices.usb.CanHavePeripheryUSBModuleImp
    with sifive.blocks.devices.uart.HasPeripheryUARTModuleImp
    with sifive.blocks.devices.spi.HasPeripherySPIFlashModuleImp


class OpenRigilTestHarness()(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
    val usb_rx = Input(UInt(2.W))
    val uart_0_rxd = Input(UInt(1.W))
  })

  val ldut = LazyModule(new OpenRigilSystem)
  val dut = Module(ldut.module)

  // Allow the debug ndreset to reset the dut, but not until the initial reset has completed
  dut.reset := (reset.asBool | dut.debug.map { debug => AsyncResetReg(debug.ndreset) }.getOrElse(false.B)).asBool

  dut.dontTouchPorts()
  dut.tieOffInterrupts()
  ldut.l2_frontend_bus_axi4.foreach(_.tieoff)
  io.success := 0.U

  dut.usb match {
    case Some(usb) => {
      usb.rx := io.usb_rx
      Some(usb)
    }
    case None => None
  }

  dut.uart.map(sifive.blocks.devices.uart.UART.loopback(_))

  dut.qspi.map { port =>
    port.dq.foreach { dq => dq.i := false.B }
  }
}
