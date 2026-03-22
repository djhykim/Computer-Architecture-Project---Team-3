package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class RFS extends AbstractInstruction {

    int immed;
            // RFS: Return from Subroutine
// immed = return value to place in R0
// Operation: R0 = immed (return value for caller)
//            PC = R3 (return to address saved by JSR)
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        immed = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        registers.setR0(immed);
        registers.setPC(registers.getR3());
    }

    @Override
    public String getExecuteMessage() {
        return "RFS executed";
    }
}