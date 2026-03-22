package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.EffectiveAddress;
import util.MachineFaultException;
import util.StringUtil;

public class SOB extends AbstractInstruction {

    int r;
    int ix;
    int i;
    int address;
    // SOB: Subtract One and Branch
// r = register to decrement
// EA = target address to jump to if result > 0
// Operation: R[r] = R[r] - 1
//            if R[r] > 0, PC = EA (loop back)
//            else PC = PC + 1 (exit loop)
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ix = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        int effectiveAddress = EffectiveAddress.calculateEA(ix, address, i, mcu, registers);

        int value = registers.getRnByNum(r) - 1;
        registers.setRnByNum(r, value);

        if (value > 0) {
            registers.setPC(effectiveAddress);
        } else {
            registers.increasePCByOne();
        }
    }

    @Override
    public String getExecuteMessage() {
        return "SOB executed";
    }
}