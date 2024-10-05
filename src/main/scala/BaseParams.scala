package tech.rocksavage.chiselware.SPI

import chisel3._
import chisel3.util._

case class BaseParams(
    dataWidth: Int = 32,
    addrWidth: Int = 32
)
