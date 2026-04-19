package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class IN extends AbstractInstruction {

    int r;
    int devid;

    @Override
    public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

        r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
        devid = StringUtil.binaryToDecimal(instruction.substring(11, 16));

        String buffer = mcu.getKeyboardBuffer();

        if (buffer == null || buffer.isEmpty()) {
            // No input available: do not modify PC here. Leave PC unchanged
            // so the CPU will re-execute the IN instruction (stall) until
            // input arrives. Previous implementation adjusted PC to
            // compensate for framework-level increments; instructions
            // now manage PC themselves, so decrementing causes a toggle.
            return;
        }

        int input = (int) buffer.charAt(0);
        mcu.setKeyboardBuffer(buffer.substring(1));
        registers.setRnByNum(r, input);
        registers.increasePCByOne();
    }

    @Override
    public String getExecuteMessage() {
        return "IN executed";
    }
}