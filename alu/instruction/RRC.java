package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class RRC extends AbstractInstruction {

    int r;
    int count;
    int lr;
    // RRC: Rotate Register by Count
// r = register to rotate (bits 6-7)
// lr = left/right flag (bit 9): 0=right, 1=left
// count = number of positions to rotate (bits 12-15)
// Bits shifted out reenter from the other end (circular shift)
// Result masked to 16 bits
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r     = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        lr    = StringUtil.binaryToDecimal(instruction.substring(9, 10));   // L/R
        count = StringUtil.binaryToDecimal(instruction.substring(12, 16)); // count

        int value = registers.getRnByNum(r);

        count = count % 16;

        if (count != 0) {
            if (lr == 1) { // left rotate
                value = (value << count) | (value >>> (16 - count));
            } else { // right rotate
                value = (value >>> count) | (value << (16 - count));
            }
        }

        value = value & 0xFFFF;
        registers.setRnByNum(r, value);

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "RRC executed";
    }
}