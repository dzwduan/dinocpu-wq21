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
  registers.io  := DontCare
  aluControl.io := DontCare
  alu.io        := DontCare
  immGen.io     := DontCare
  nextpc.io     := DontCare
  io.dmem       := DontCare

  //FETCH
  io.imem.address := pc
  io.imem.valid := true.B

  val instruction = io.imem.instruction


  val reg = registers.io 

  control.io.opcode := instruction(6,0)


 
  reg.readreg1:= instruction(19,15)
  reg.readreg2:= instruction(24,20)
  reg.writereg:= instruction(11,7)
  reg.wen     := (control.io.regwrite)&&(reg.writereg=/=0.U)
  
  aluControl.io.itype := control.io.itype 
  aluControl.io.aluop := control.io.aluop
  aluControl.io.funct3 := instruction(14,12)
  aluControl.io.funct7 := instruction(31,25)

  immGen.io.instruction := instruction
 
  //contect input of nextpc
  nextpc.io.branch := control.io.branch
  nextpc.io.jal    := control.io.jal
  nextpc.io.jalr   := control.io.jalr
  nextpc.io.inputx := reg.readdata1
  nextpc.io.inputy := reg.readdata2
  nextpc.io.funct3 := instruction(14,12)
  nextpc.io.pc     := pc
  nextpc.io.imm    := immGen.io.sextImm

  alu.io.operation := aluControl.io.operation
  alu.io.inputx    := Mux(control.io.xsrc,pc,reg.readdata1)

  when(control.io.plus4 === false.B) {
    alu.io.inputy := Mux(control.io.ysrc,immGen.io.sextImm,reg.readdata2)
  }.otherwise {
    alu.io.inputy := 4.U
  }

  val result = Wire(UInt())
  when(control.io.resultselect) {
    result := immGen.io.sextImm
  }.otherwise{
    result := alu.io.result
  }


  when(control.io.toreg ===false.B){
    reg.writedata := result 
  }//TODO
  

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
