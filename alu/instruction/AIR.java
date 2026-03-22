package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class AIR extends AbstractInstruction {

    int r;
    int immed;
    // AIR: Add Immediate to Register
// r = destination register (bits 6-7)
// immed = immediate value (bits 11-15)
// Operation: R[r] = R[r] + immed
// PC increments by 1 after execution
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        immed = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        if (immed != 0) {
            registers.setRnByNum(r, registers.getRnByNum(r) + immed);
        }

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "AIR executed";
    }
}