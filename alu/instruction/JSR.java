package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.EffectiveAddress;
import util.MachineFaultException;
import util.StringUtil;

public class JSR extends AbstractInstruction {

    int ix;
    int i;
    int address;
    // JSR: Jump to Subroutine
// EA = address of subroutine to call
// Operation: R3 = PC + 1 (save return address)
//            PC = EA (jump to subroutine)
// Return is done by RFS instruction
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        ix = StringUtil.binaryToDecimal(instruction.substring(8, 10));
        i = StringUtil.binaryToDecimal(instruction.substring(10, 11));
        address = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        int effectiveAddress = EffectiveAddress.calculateEA(ix, address, i, mcu, registers);

        registers.setR3(registers.getPC() + 1);
        registers.setPC(effectiveAddress);
    }

    @Override
    public String getExecuteMessage() {
        return "JSR executed";
    }
}