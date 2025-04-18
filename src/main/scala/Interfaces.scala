package tech.rocksavage.chiselware.SPI

import chisel3._
import chisel3.util._
import java.io.{File, PrintWriter}
import _root_.circt.stage.ChiselStage

class ApbInterface(p: BaseParams) extends Bundle {
  //val PCLK = Input(Clock())
  //val PRESETn = Input(AsyncReset())
  val PSEL = Input(Bool()) // Peripheral select
  val PENABLE = Input(Bool()) // Enable signal
  val PWRITE = Input(Bool()) // Read/Write signal
  val PADDR = Input(UInt(p.addrWidth.W)) // Address
  val PWDATA = Input(UInt(p.dataWidth.W)) // Write data
  val PRDATA = Output(UInt(p.dataWidth.W)) // Read data
  val PREADY = Output(Bool()) // Ready signal
  val PSLVERR = Output(Bool()) // Slave error signal
}
/*
class MyApbModule(p: BaseParams) extends RawModule {
  val io = IO(new ApbInterface(p))

  // Reset logic: Invert PRESETn for Chisel's reset type
  val reset_n = (!io.PRESETn.asBool).asAsyncReset

  // Example register with APB interface
  val reg = withClockAndReset(io.PCLK, reset_n)(RegInit(0.U(p.PDATA_WIDTH.W)))

  // Increment logic (example logic for the register)
  when(io.PSEL && io.PENABLE && io.PWRITE) {
    reg := io.PWDATA
  }

  // Outputs
  io.PRDATA := reg
  io.PREADY := true.B
  io.PSLVERR := false.B
}
  */

class MasterInterface() extends Bundle {
  val miso = Input(Bool())
  val mosi = Output(UInt(1.W))
  val sclk = Output(Bool())
  val cs = Output(Bool())
}

class SlaveInterface() extends Bundle {
  val miso = Output(Bool())
  val mosi = Input(UInt(1.W))
  val sclk = Input(Bool())
  val cs = Input(Bool())
}

class SPIInterface(p: BaseParams) extends Bundle {
  val apb = new ApbInterface(p)
  val master = new MasterInterface
  val slave = new SlaveInterface
}