package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class DVD extends AbstractInstruction {

    int rx;
    int ry;
    // DVD: Divide Register by Register
// rx = dividend register (must be R0 or R2)
// ry = divisor register (must be R0 or R2)
// Operation: R[rx] = quotient, R[rx+1] = remainder
// Sets CC bit 2 (DIVZERO) if denominator is 0
    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        rx = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        ry = StringUtil.binaryToDecimal(instruction.substring(8, 10));

        if (!((rx == 0 || rx == 2) && (ry == 0 || ry == 2))) {
            throw new RuntimeException("DVD requires rx and ry to be 0 or 2");
        }

        int numerator = registers.getRnByNum(rx);
        int denominator = registers.getRnByNum(ry);

        if (denominator == 0) {
             registers.setCCElementByBit(2, true);  // set DIVZERO flag
             registers.increasePCByOne();
            return;
        }

        int quotient = numerator / denominator;
        int remainder = numerator % denominator;

        registers.setRnByNum(rx, quotient);
        registers.setRnByNum(rx + 1, remainder);

        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "DVD executed";
    }
}