package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.EffectiveAddress;
import util.MachineFaultException;
import util.StringUtil;

public class JZ extends AbstractInstruction {

    int r;
    int ix;
    int i;
    int address;
    // JZ: Jump if Zero
// r = register to test (bits 6-7)
// EA = target address to jump to
// Operation: if R[r] == 0, PC = EA
//            else PC = PC + 1 (no jump)
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ix = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        int effectiveAddress = EffectiveAddress.calculateEA(ix, address, i, mcu, registers);

        if (registers.getRnByNum(r) == 0) {
            registers.setPC(effectiveAddress);
        } else {
            registers.increasePCByOne();
        }
    }

    @Override
    public String getExecuteMessage() {
        return "JZ executed";
    }
}