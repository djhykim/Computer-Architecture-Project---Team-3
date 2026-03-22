package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;
import util.StringUtil;

public class OUT extends AbstractInstruction {

    int r;
    int devid;

    @Override
public void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException {

    r = StringUtil.binaryToDecimal(instruction.substring(6, 8));
    devid = StringUtil.binaryToDecimal(instruction.substring(11, 16));

    // Send to printer buffer so GUI can display it
    char c = (char) registers.getRnByNum(r);
    String current = mcu.getPrinterBuffer();
    if (current == null) current = "";
    mcu.setPrinterBuffer(current + c);

    registers.increasePCByOne();
}

    @Override
    public String getExecuteMessage() {
        return "OUT executed";
    }
}