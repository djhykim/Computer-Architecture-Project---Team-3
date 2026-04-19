package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class LDX extends AbstractInstruction {

    int r;
    int ix;
    int address;
    int i;

    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {
        // 41: LDX -> Load Index Register from Memory

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ix = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        // For LDX/STX in your assembler, IX field names the target X register,
        // not an index register to add into the address.
        int effectiveAddress = address;

        if (i == 1) {
            effectiveAddress = mcu.fetchFromCache(effectiveAddress);
        }

        int value = mcu.fetchFromCache(effectiveAddress);

        // choose the correct target index register
        int targetReg = (r == 0 && ix != 0) ? ix : r;
        registers.setXnByNum(targetReg, value);

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "LDX " + r + ", " + ix + ", " + address + ", " + i;
    }
}