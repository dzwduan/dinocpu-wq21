// This file contains ALU control logic.

package dinocpu.components

import chisel3._
import chisel3.util._
import dinocpu.components.ALU_OP._

/**
 * The ALU control unit
 *
 * Input:  itype      true if we're working on an itype instruction
 * Input:  aluop      true for R-type and I-type, false otherwise
 * Input:  funct7     the most significant bits of the instruction
 * Input:  funct3     the middle three bits of the instruction (12-14)
 * Output: operation  What we want the ALU to do.
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy.
 * This is loosely based on figure 4.12
 */

case object ALU_OP {
  val XOR  = 0.U 
  val SLTU = 1.U
  val SRL  = 2.U 
  val SRA  = 3.U
  val SUB  = 4.U 
  val OR   = 5.U
  val AND  = 6.U 
  val ADD  = 7.U
  val SLL  = 8.U 
  val SLT  = 9.U
  //val XXX  = "b1111".U
}

class ALUControl extends Module {
  val io = IO(new Bundle {
    val aluop     = Input(Bool())
    val itype     = Input(Bool())
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))
    val operation = Output(UInt(4.W))
  })

  io.operation := ADD
  //Muxcase / ListLookup
  //R-type
  when(io.aluop) {
      when(io.funct7 === 0.U){
        io.operation := MuxLookup(io.funct3,ADD,Array(
        0.U -> ADD,
        1.U -> SLL,
        2.U -> SLT,
        3.U -> SLTU,
        4.U -> XOR,
        5.U -> SRL,
        6.U -> OR,
        7.U -> AND
        ))
      }.elsewhen(io.funct7 === "b0100000".U){
        io.operation := MuxLookup(io.funct3,ADD,Array(
        0.U -> SUB,
        5.U -> SRA
        ))
     }
  }.otherwise{
    io.operation := ADD
  }

  // //没有subi
  // when(io.aluop && io.itype===false.B && io.funct7 === "b0100000".U && io.funct3===0.U) {
  //   io.operation := SUB
  // }

}
