package tech.rocksavage.chiselware.SPI

import chisel3._
import chisel3.util._

// SPI Mode Enum
object SPIMode extends ChiselEnum {
  val Mode0, Mode1, Mode2, Mode3 = Value
}

// SPI Master/Slave Enum
object SPIRole extends ChiselEnum {
  val Master, Slave = Value
}

class SPIBundle(role: SPIRole.Type) extends Bundle {
  val miso = role match {
    case SPIRole.Master => Input(Bool())  // Master reads from MISO
    case SPIRole.Slave => Output(Bool())  // Slave drives MISO
  }
  val mosi = role match {
    case SPIRole.Master => Output(Bool()) // Master drives MOSI
    case SPIRole.Slave => Input(Bool())   // Slave reads from MOSI
  }
  val sclk = role match {
    case SPIRole.Master => Output(Bool()) // Master drives SCLK
    case SPIRole.Slave => Input(Bool())   // Slave receives SCLK
  }
  val cs = role match {
    case SPIRole.Master => Output(Bool()) // Master drives CS
    case SPIRole.Slave => Input(Bool())   // Slave receives CS
  }
}

class SPI(p: BaseParams, clockFreq: Int, spiMode: SPIMode.Type, spiRole: SPIRole.Type) extends Module {
  val io = IO(new Bundle {
    val apb = new ApbInterface(p)  // APB bus side
    val spi = new SPIBundle(spiRole)    // SPI peripheral side depends on role
  })

  // Registers and Flags
  val spiReg = RegInit(0.U(p.dataWidth.W)) // Register to store data for transmission
  val shiftCounter = RegInit(0.U((log2Ceil(p.dataWidth)+1).W)) // Counter to track shifting

  // SPI clock and data behavior based on mode
  val sclkReg = RegInit(false.B)
  val cpol = Wire(Bool())  // Clock Polarity
  val cpha = Wire(Bool())  // Clock Phase

  // Set CPOL and CPHA based on spiMode
  spiMode match {
    case SPIMode.Mode0 => { cpol := false.B; cpha := false.B } //Data sampled on rising edge, shifted out on falling edge. CLK Idle = Logic Low
    case SPIMode.Mode1 => { cpol := false.B; cpha := true.B }  //Data sampled on falling edge, shifted out on rising edge. CLK Idle = Logic Low
    case SPIMode.Mode2 => { cpol := true.B; cpha := false.B } //Data sampled on falling edge, shifted out on rising edge. CLK Idle = Logic High
    case SPIMode.Mode3 => { cpol := true.B; cpha := true.B }  //Data sampled on rising edge, shifted out on falling edge. CLK Idle = Logic High
  }

  // Clock generation (only for Master)
  val sclkCounter = RegInit(0.U(log2Ceil(clockFreq).W))
  when(spiRole === SPIRole.Master) {
    when(sclkCounter === (clockFreq / 2 - 1).U) {
      sclkReg := ~sclkReg
      sclkCounter := 0.U
    } .otherwise {
      sclkCounter := sclkCounter + 1.U
    }
  }

  // Set default values for the SPI interface, but only if they are outputs
  spiRole match {
    case SPIRole.Master =>
      // In Master mode, we can drive the output signals
      io.spi.mosi := false.B // Master drives MOSI
      io.spi.sclk := false.B // Master drives SCLK
      io.spi.cs := true.B    // Master drives CS
    case SPIRole.Slave =>
      // In Slave mode, we can drive the appropriate output signals
      io.spi.miso := false.B // Slave drives MISO
      io.spi.sclk := DontCare // SCLK is input, we shouldn't drive it
      io.spi.cs := DontCare   // CS is input, we shouldn't drive it
  }

  // MISO and MOSI behavior based on the role (Master or Slave)
  spiRole match {
    case SPIRole.Master =>
      // Master mode
      io.spi.sclk := sclkReg ^ cpol // Adjust SCLK based on CPOL

      // Load new data from APB to SPI register and reset shift counter
      when(io.apb.PSEL && io.apb.PWRITE && io.apb.PENABLE) {
        spiReg := io.apb.PWDATA
        shiftCounter := (p.dataWidth).U // Start with the MSB
      }

      // Shift data out (MOSI) based on the mode and clock phase (CPHA)
      when(shiftCounter =/= 0.U) {
        when(!cpha) {
          // CPHA = 0 (Mode 0 and Mode 2)
          // Mode 0: Shift on rising edge (CPOL=0)
          // Mode 2: Shift on falling edge (CPOL=1)
          when((!cpol && sclkReg === false.B) || (cpol && sclkReg === false.B)) {
            io.spi.mosi := spiReg(p.dataWidth - 1) // Shift out MSB on MOSI
            spiReg := Cat(spiReg(p.dataWidth - 2, 0), 0.U(1.W)) // Shift left
            shiftCounter := shiftCounter - 1.U // Decrement counter
          }
        } .otherwise {
          // CPHA = 1 (Mode 1 and Mode 3)
          // Mode 1: Shift on falling edge (CPOL=0)
          // Mode 3: Shift on rising edge (CPOL=1)
          when((!cpol && sclkReg === false.B) || (cpol && sclkReg === false.B)) {
            io.spi.mosi := spiReg(p.dataWidth - 1) // Shift out MSB on MOSI
            spiReg := Cat(spiReg(p.dataWidth - 2, 0), 0.U(1.W)) // Shift left
            shiftCounter := shiftCounter - 1.U // Decrement counter
          }
        }
      } .otherwise {
        io.spi.mosi := false.B // Default MOSI output when not shifting
      }

    case SPIRole.Slave =>
      // Initialize MISO as driving the MSB of spiReg
      io.spi.miso := spiReg(p.dataWidth - 1)

      when(io.spi.cs === false.B) { // Slave is selected when CS is low
        // Capture data on appropriate clock edge based on SPI mode
        when(cpol === false.B && cpha === false.B) {
          // Mode 0: Capture data on rising edge of SCLK
          when(io.spi.sclk) {
            spiReg := Cat(spiReg(p.dataWidth - 2, 0), io.spi.mosi) // Shift in MOSI data
            shiftCounter := shiftCounter + 1.U
          }
        } .elsewhen(cpol === false.B && cpha === true.B) {
          // Mode 1: Capture data on falling edge of SCLK
          when(!io.spi.sclk) {
            spiReg := Cat(spiReg(p.dataWidth - 2, 0), io.spi.mosi) // Shift in MOSI data
            shiftCounter := shiftCounter + 1.U
          }
        } .elsewhen(cpol === true.B && cpha === false.B) {
          // Mode 2: Capture data on rising edge of SCLK
          when(io.spi.sclk) {
            spiReg := Cat(spiReg(p.dataWidth - 2, 0), io.spi.mosi) // Shift in MOSI data
            shiftCounter := shiftCounter + 1.U
          }
        } .elsewhen(cpol === true.B && cpha === true.B) {
          // Mode 3: Capture data on falling edge of SCLK
          when(!io.spi.sclk) {
            spiReg := Cat(spiReg(p.dataWidth - 2, 0), io.spi.mosi) // Shift in MOSI data
            shiftCounter := shiftCounter + 1.U
          }
        }

        // Check if a full byte has been received
        when(shiftCounter === (p.dataWidth - 1).U) {
          io.apb.PRDATA := spiReg // Output the full byte to APB
          shiftCounter := 0.U // Reset counter after transmission
        } .otherwise {
          io.apb.PRDATA := 0.U // Default value if not yet full byte
        }
      } .otherwise {
        // Reset logic when CS is high
        shiftCounter := 0.U
        spiReg := 0.U // Optionally reset spiReg when not selected
        io.apb.PRDATA := 0.U // Default value when not selected
      }

  }

  // Output the received data back to APB
  io.apb.PRDATA := spiReg
}