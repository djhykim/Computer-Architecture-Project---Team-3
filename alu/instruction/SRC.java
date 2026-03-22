package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class SRC extends AbstractInstruction {

    int r;
    int count;
    int lr;
    int al;
    // SRC: Shift Register by Count
// r = register to shift (bits 6-7)
// al = arithmetic/logical flag (bit 8): 0=arithmetic, 1=logical
// lr = left/right flag (bit 9): 0=right, 1=left
// count = number of positions to shift (bits 12-15)
// Arithmetic right shift preserves sign bit
// Logical right shift fills with zeros
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r     = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        al    = StringUtil.binaryToDecimal(instruction.substring(8, 9));   // A/L
        lr    = StringUtil.binaryToDecimal(instruction.substring(9, 10));  // L/R
        count = StringUtil.binaryToDecimal(instruction.substring(12, 16)); // count

        int value = registers.getRnByNum(r);

        if (count != 0) {
            if (lr == 1) { // left
                value = value << count;
            } else { // right
                if (al == 1) { // logical
                    value = value >>> count;
                } else { // arithmetic
                    value = value >> count;
                }
            }
        }

        registers.setRnByNum(r, value);
        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "SRC executed";
    }
}