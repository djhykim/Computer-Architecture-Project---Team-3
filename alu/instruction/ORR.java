package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class ORR extends AbstractInstruction {

    int r;
    int rx;

    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        rx = StringUtil.binaryToDecimal(instruction.substring(8, 10));

        int value = registers.getRnByNum(r) | registers.getRnByNum(rx);
        registers.setRnByNum(r, value);

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "ORR executed";
    }
}