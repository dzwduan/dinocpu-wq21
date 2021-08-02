// Control logic for the processor

package dinocpu.components

import chisel3._
import chisel3.util.{BitPat, ListLookup}

/**
 * Main control logic for our simple processor
 *
 * Input: opcode:     Opcode from instruction
 *
 * Output: itype         true if we're working on an itype instruction
 * Output: aluop         true for R-type and I-type, false otherwise
 * Output: xsrc          source for the first ALU/nextpc input (0 is readdata1, 1 is pc)
 * Output: ysrc          source for the second ALU/nextpc input (0 is readdata2 and 1 is immediate) 注意零号寄存器
 * Output: branch        true if branch
 * Output: jal           true if a jal
 * Output: jalr          true if a jalr instruction
 * Output: plus4         true if ALU should add 4 to inputx
 * Output: resultselect  false for result from alu, true for immediate
 * Output: memop         00 if not using memory, 10 if reading, and 11 if writing
 * Output: toreg         false for result from execute, true for data from memory
 * Output: regwrite      true if writing to the register file   //PC也算寄存器
 * Output: validinst     True if the instruction we're decoding is valid
 *
 * For more information, see section 4.4 of Patterson and Hennessy.
 * This follows figure 4.22 somewhat.
 */

 // i          T T F T F F F F F  0 F T T
 //load        F F F T F F F F F 10 T T T
 //store       F F F T F F F F F 11 F F T
 //branch      F F F F T F F F F  0 F F T
//lui          F F F F F T F F T 0  F T T
//auipc        F F T T F T F F F 0  F T T
 //jal         F F T F F T F T F 0  F T T 
 //jalr        F F T T F F T T F 0  F T T //? jalr_ysrc === imm ?




class Control extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))

    val itype        = Output(Bool())
    val aluop        = Output(Bool())
    val xsrc         = Output(Bool())
    val ysrc         = Output(Bool())
    val branch       = Output(Bool())
    val jal          = Output(Bool())
    val jalr         = Output(Bool())
    val plus4        = Output(Bool())
    val resultselect = Output(Bool())
    val memop        = Output(UInt(2.W))
    val toreg        = Output(Bool())
    val regwrite     = Output(Bool())
    val validinst    = Output(Bool())
  })

  val signals =
    ListLookup(io.opcode,
      /*default*/           List(false.B, false.B, false.B, false.B, false.B, false.B, false.B, false.B, false.B,      0.U,   false.B, false.B,  false.B),

      Array(              /*     itype,   aluop,   xsrc,    ysrc,    branch,  jal,     jalr     plus4,   resultselect, memop, toreg,   regwrite, validinst */
      // R-format
      BitPat("b0110011") -> List(false.B, true.B,  false.B, false.B, false.B, false.B, false.B, false.B, false.B,      0.U,   false.B, false.B,   true.B),

      // Your code coes here for Lab 2.
      // Remember to make sure to have commas at the end of each line
      // I-type without jal and jalr
      BitPat("b0010011") -> List(true.B,  true.B,   false.B,  true.B, false.B, false.B, false.B, false.B, false.B,     0.U,   false.B, true.B,   true.B),
      //jal   pc += sext(offset)
      BitPat("b1101111") -> List(false.B, false.B,  true.B,  false.B, false.B,  true.B, false.B, true.B, false.B,      0.U,   false.B, true.B,  true.B),
      //jalr  pc =(x[rs1]+sext(offset))
      BitPat("b1100111") -> List(false.B, false.B,  true.B, true.B, false.B,  false.B, true.B,  true.B,  false.B,      0.U,    false.B, true.B,  true.B),
      //branch
      BitPat("b1100011") -> List(false.B, false.B,  false.B, false.B, true.B,  false.B, false.B, false.B, false.B,     0.U,   false.B, false.B,  true.B),
      //load  x[rd] = sext(M[x[rs1] + sext(offset)][31:0])
      BitPat("b0000011") -> List(false.B, false.B,  false.B, true.B, false.B, false.B,  false.B, false.B, false.B,     2.U,   true.B, true.B,   true.B),
      //store M[x[rs1] + sext(offset) = x[rs2][31: 0]
      BitPat("b0100011") -> List(false.B, false.B,  false.B, true.B, false.B, false.B,  false.B, false.B, false.B,     3.U,   false.B, false.B,   true.B),
      //lui   x[rd] = sext(immediate[31:12] << 12)
      BitPat("b0110111") -> List(false.B, false.B,  false.B, false.B, false.B, true.B, false.B, false.B, true.B,       0.U,   false.B, true.B,   true.B),
      //auipc x[rd] = pc + sext(immediate[31:12] << 12)
      BitPat("b0010111") -> List(false.B, false.B,  true.B,  true.B, false.B, true.B,  false.B, false.B, false.B,      0.U,   false.B, true.B,   true.B),
      ) // Array
    ) // ListLookup

  io.itype        := signals(0)
  io.aluop        := signals(1)
  io.xsrc         := signals(2)
  io.ysrc         := signals(3)
  io.branch       := signals(4)
  io.jal          := signals(5)
  io.jalr         := signals(6)
  io.plus4        := signals(7)
  io.resultselect := signals(8)
  io.memop        := signals(9)
  io.toreg        := signals(10)
  io.regwrite     := signals(11)
  io.validinst    := signals(12)
}
