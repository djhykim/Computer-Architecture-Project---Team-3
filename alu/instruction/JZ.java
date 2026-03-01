package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class JZ extends AbstractInstruction {

    int r, ix, address, i;

    @Override
    public void execute(String instruction, Registers registers, MCU mcu)
            throws MachineFaultException {

        r       = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ix      = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i       = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        // Calculate EA manually (don't use ix for indexing — ix IS the index reg)
        int ea = address;
        if (ix != 0) {
            ea = address + registers.getXnByNum(ix);
        }
        // Indirect
        if (i == 1) {
            registers.setMAR(ea);
            registers.setMBR(mcu.fetchFromCache(ea));
            ea = registers.getMBR();
        }

        // If R[r] == 0, jump — do NOT increment PC
        if (registers.getRnByNum(r) == 0) {
            registers.setPC(ea);
            registers.increasePCByOne();
        } else {
            registers.increasePCByOne(); // no jump
        }
    }

    @Override
    public String getExecuteMessage() {
        return "JZ " + r + ", " + ix + ", " + address + ", " + i;
    }
}