package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class MLT extends AbstractInstruction {

    int rx;
    int ry;
    // MLT: Multiply Register by Register
// rx = multiplicand register (must be R0 or R2)
// ry = multiplier register (must be R0 or R2)
// Operation: 32-bit result stored as R[rx]=high word, R[rx+1]=low word
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        rx = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ry = StringUtil.binaryToDecimal(instruction.substring(8, 10));

        if (!((rx == 0 || rx == 2) && (ry == 0 || ry == 2))) {
            throw new RuntimeException("MLT requires rx and ry to be 0 or 2");
        }

        int a = registers.getRnByNum(rx);
        int b = registers.getRnByNum(ry);

        long result = (long) a * (long) b;

        int high = (int) ((result >> 16) & 0xFFFF);
        int low  = (int) (result & 0xFFFF);

        registers.setRnByNum(rx, high);
        registers.setRnByNum(rx + 1, low);

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "MLT executed";
    }
}