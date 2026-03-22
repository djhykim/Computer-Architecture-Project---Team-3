package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.EffectiveAddress;
import util.MachineFaultException;
import util.StringUtil;

public class JNE extends AbstractInstruction {

    int r;
    int ix;
    int i;
    int address;

    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ix = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        int effectiveAddress = EffectiveAddress.calculateEA(ix, address, i, mcu, registers);

        if (registers.getRnByNum(r) != 0) {
            registers.setPC(effectiveAddress);
        } else {
            registers.increasePCByOne();
        }
    }

    @Override
    public String getExecuteMessage() {
        return "JNE executed";
    }
}