//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel:6.5.0"
//> using plugin "org.chipsalliance:::chisel-plugin:6.5.0"
//> using options "-unchecked", "-deprecation", "-language:reflectiveCalls", "-feature", "-Xcheckinit", "-Xfatal-warnings", "-Ywarn-dead-code", "-Ymacro-annotations"
package tech.rocksavage.chiselware.SPI

import _root_.circt.stage.ChiselStage
import _root_.circt.stage.FirtoolOption
// third-party imports
import chisel3._

object Main extends App {

  // ######### Getting Setup #########
  // setting file output directory
  var output = System.getProperty("output")
  if (output == null) output = "out"
  val outputDir = s"$output/generated"

  val myParams = BaseParams(
    dataWidth = 32,
    addrWidth = 32
  )
  // if output dir does not exist, make path
  val javaOutputDir = new java.io.File(outputDir)
  if (!javaOutputDir.exists) javaOutputDir.mkdirs

  // ######### Set Up Top Module HERE #########
  val top_name = "SPI.sv"
  val verilog = ChiselStage.emitSystemVerilog(
    new SPI(myParams, 1000000, SPIMode.Mode0, SPIRole.Slave),
    firtoolOpts = Array(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays",
      "--disable-all-randomization",
      "--strip-debug-info",
      "--split-verilog",
      s"-o=generated/"
    )
  )
  // ##########################################

  // print verilog to console
  println(verilog)

  // write verilog to file
  val writer =
    new java.io.PrintWriter(new java.io.File(s"$outputDir/$top_name"))
  writer.write(verilog)

  // close writer
  writer.close()

  // exit
  System.exit(0)
}
