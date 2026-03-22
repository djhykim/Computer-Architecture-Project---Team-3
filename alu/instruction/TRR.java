package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class TRR extends AbstractInstruction {

    int r;
    int rx;
    // TRR: Test Register to Register
// r = first register, rx = second register
// Operation: if R[r] == R[rx], set CC bit 3 (EQUALORNOT) = 1
//            else clear CC bit 3 = 0
// Used by JCC instruction to branch on equality
    @Override
public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

    r  = StringUtil.binaryToDecimal(instruction.substring(6, 8));
    rx = StringUtil.binaryToDecimal(instruction.substring(8, 10));

    if (registers.getRnByNum(r) == registers.getRnByNum(rx)) {
        registers.setCCElementByBit(3, true);   // bit 3 = EQUALORNOT
    } else {
        registers.setCCElementByBit(3, false);  // clear bit 3
    }

    registers.increasePCByOne();
}

    @Override
    public String getExecuteMessage() {
        return "TRR executed";
    }
}