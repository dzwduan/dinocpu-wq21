// Logic to calculate the next pc

package dinocpu.components

import chisel3._
import chisel3.util._
import dinocpu.components.Branch_Op._

case object Branch_Op{
  val BEQ  = 0.U
  val BNE  = 1.U
  val BLT  = 4.U 
  val BGE  = 5.U
  val BLTU = 6.U
  val BGEU = 7.U
}

/**
 * Next PC unit. This takes various inputs and outputs the next address of the next instruction.
 *
 * Input: branch         true if executing a branch instruction
 * Input: jal            true if executing a jal
 * Input: jalr           true if executing a jalr
 * Input: inputx         first input
 * Input: inputy         second input
 * Input: funct3         the funct3 from the instruction
 * Input: pc             the *current* program counter for this instruction
 * Input: imm            the sign-extended immediate
 *
 * Output: nextpc        the address of the next instruction
 * Output: taken         true if the next pc is not pc+4
 *
 */
class NextPC extends Module {
  val io = IO(new Bundle {
    val branch  = Input(Bool())
    val jal     = Input(Bool())
    val jalr    = Input(Bool())
    val inputx  = Input(UInt(32.W))
    val inputy  = Input(UInt(32.W))
    val funct3  = Input(UInt(3.W))
    val pc      = Input(UInt(32.W))
    val imm     = Input(UInt(32.W))

    val nextpc  = Output(UInt(32.W))
    val taken   = Output(Bool())
  })

  //io.nextpc := io.pc + 4.U
  //io.taken  := false.B

  // Your code goes here part 0
  // case branch
  when(io.branch) {
    io.taken := MuxLookup(io.funct3,0.U,Array(
      BEQ  ->  (io.inputx === io.inputy),
      BNE  ->  (io.inputx =/= io.inputy),
      BLT  ->  (io.inputx.asSInt  <   io.inputy.asSInt).asUInt,
      BGE  ->  (io.inputx.asSInt  >=  io.inputy.asSInt).asUInt,
      BLTU ->  (io.inputx.asUInt  <  io.inputy.asUInt),
      BGEU ->  (io.inputx.asUInt  >  io.inputy.asUInt)
    ))

    io.nextpc := Mux(io.taken, io.pc+io.imm,io.pc+4.U)
  }.elsewhen(io.jal){
    io.taken  := true.B 
    io.nextpc := io.pc + io.imm
  }.elsewhen(io.jalr){
    io.taken  := true.B 
    io.nextpc := io.inputx + io.imm
  }.otherwise{
    io.taken  := false.B 
    io.nextpc := io.pc + 4.U
  }

}
