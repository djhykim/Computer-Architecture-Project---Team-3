package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class STX extends AbstractInstruction {

    int r;
    int ix;
    int address;
    int i;

    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {
        // 42: STX -> Store Index Register to Memory

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ix = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        // For LDX/STX in your assembler, IX field names the X register,
        // not an address-indexing register.
        int effectiveAddress = address;

        if (i == 1) {
            effectiveAddress = mcu.fetchFromCache(effectiveAddress);
        }

        int sourceReg = (r == 0 && ix != 0) ? ix : r;

        registers.setMAR(effectiveAddress);
        registers.setMBR(registers.getXnByNum(sourceReg));
        mcu.storeIntoCache(registers.getMAR(), registers.getMBR());

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "STX " + r + ", " + ix + ", " + address + ", " + i;
    }
}