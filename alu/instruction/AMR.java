package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.EffectiveAddress;
import util.MachineFaultException;
import util.StringUtil;

public class AMR extends AbstractInstruction {

    int r;
    int ix;
    int i;
    int address;
    // AMR: Add Memory to Register
// r = destination register (bits 6-7)
// EA = effective address calculated from ix, i, address fields
// Operation: R[r] = R[r] + Memory[EA]
// PC increments by 1 after execution
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ix = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        int effectiveAddress = EffectiveAddress.calculateEA(ix, address, i, mcu, registers);

        int value = registers.getRnByNum(r) + mcu.fetchFromCache(effectiveAddress);
        registers.setRnByNum(r, value);

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "AMR executed";
    }
}