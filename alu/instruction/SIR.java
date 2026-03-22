package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class SIR extends AbstractInstruction {

    int r;
    int immed;

    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        immed = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        if (immed != 0) {
            registers.setRnByNum(r, registers.getRnByNum(r) - immed);
        }

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "SIR executed";
    }
}