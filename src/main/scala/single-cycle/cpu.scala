// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._
import dinocpu.components._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.21
 */
class SingleCycleCPU(implicit val conf: CPUConfig) extends BaseCPU {
  // All of the structures required
  val pc         = dontTouch(RegInit(0.U))
  val control    = Module(new Control())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val nextpc     = Module(new NextPC())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  control.io    := DontCare
  //registers.io  := DontCare
  aluControl.io := DontCare
  //alu.io        := DontCare
  immGen.io     := DontCare
  nextpc.io     := DontCare
  io.dmem       := DontCare

  //FETCH
  io.imem.address := pc
  io.imem.valid := true.B

  val instruction = io.imem.instruction

  //TODO
  nextpc.io.inputx := pc
  nextpc.io.inputy := 4.U

  val reg = registers.io 

  reg.readreg1:= instruction(19,15)
  reg.readreg2:= instruction(24,20)
  reg.writereg:= instruction(11,7)
  reg.wen     := (reg.writereg=/=0.U)

  aluControl.io.funct3 := instruction(14,12)
  aluControl.io.funct7 := instruction(31,25)

  alu.io.operation := aluControl.io.operation
  alu.io.inputx    := reg.readdata1
  alu.io.inputy    := reg.readdata2

  reg.writedata := alu.io.result

  pc := nextpc.io.nextpc
}

/*
 * Object to make it easier to print information about the CPU
 */
object SingleCycleCPUInfo {
  def getModules(): List[String] = {
    List(
      "dmem",
      "imem",
      "control",
      "registers",
      "csr",
      "aluControl",
      "alu",
      "immGen",
      "nextpc"
    )
  }
}
