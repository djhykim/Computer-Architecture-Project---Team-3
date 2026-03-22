package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.EffectiveAddress;
import util.MachineFaultException;
import util.StringUtil;

public class JCC extends AbstractInstruction {

    int cc;
    int ix;
    int i;
    int address;
    // JCC: Jump on Condition Code
// cc = which CC bit to test (bits 6-7): 0=overflow,1=underflow,2=divzero,3=equal
// EA = target address to jump to
// Operation: if CC[cc] == 1, PC = EA
//            else PC = PC + 1 (no jump)
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        cc = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ix = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        int EA = EffectiveAddress.calculateEA(ix, address, i, mcu, registers);

        // Simple simulation: use R0 == 0 as condition
       if (registers.getCCElementByBit(cc)) {
          registers.setPC(EA);
      } else {
        registers.increasePCByOne();
       }
    }

    @Override
    public String getExecuteMessage() {
        return "JCC executed";
    }
}