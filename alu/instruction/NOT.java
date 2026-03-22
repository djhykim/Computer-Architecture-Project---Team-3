package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class NOT extends AbstractInstruction {

    int r;

    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));

        int value = ~registers.getRnByNum(r);
        registers.setRnByNum(r, value);

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "NOT executed";
    }
}