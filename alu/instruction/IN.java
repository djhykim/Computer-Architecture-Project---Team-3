package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

import java.util.Scanner;

public class IN extends AbstractInstruction {

    int r;
    int devid;

    static Scanner scanner = new Scanner(System.in);

    @Override
public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

    r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
    devid = StringUtil.binaryToDecimal(instruction.substring(11, 16));

    // Read from keyboard buffer set by GUI
    String buffer = mcu.getKeyboardBuffer();
    int input = 0;
    if (buffer != null && !buffer.isEmpty()) {
        input = (int) buffer.charAt(0);
        // Remove the character we just read
        mcu.setKeyboardBuffer(buffer.substring(1));
    }

    registers.setRnByNum(r, input);
    registers.increasePCByOne();
}

    @Override
    public String getExecuteMessage() {
        return "IN executed";
    }
}