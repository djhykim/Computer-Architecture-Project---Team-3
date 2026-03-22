package alu.instruction;

import cpu.Registers;
import memory.MCU;

public class HLT extends AbstractInstruction {

    public void execute(String instruction, Registers registers, MCU mcu) {
    System.out.println("Program halted.");
    registers.increasePCByOne();  // move PC forward so run loop stops
}

    @Override
    public String getExecuteMessage() {
        return "HLT executed";
    }
}