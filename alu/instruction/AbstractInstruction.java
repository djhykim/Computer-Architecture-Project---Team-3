package alu.instruction;

import cpu.Registers;
import memory.MCU;
import util.MachineFaultException;

/**
 * Base class for all instruction implementations in the system.
 * It defines the core framework for executing instructions and retrieving their execution messages.
 */
public abstract class AbstractInstruction {

    /**
     * Executes the instruction with the given parameters.
     * Implementers should define the specific behavior of the instruction,
     * including any changes to registers, memory, and potential faults.
     *
     * @param instruction The binary string representation of the instruction.
     * @param registers The CPU registers available for the instruction.
     * @param mcu The memory control unit, for accessing memory.
     * @throws MachineFaultException If an unrecoverable error occurs during execution.
     */
    public abstract void execute(String instruction, Registers registers, MCU mcu) throws MachineFaultException;

    /**
     * Returns a descriptive message about the instruction's execution.
     * Implementers are responsible for providing a meaningful message that
     * describes the effect of the instruction (e.g., "LDR 3,0,31" or "Load register 3 with the contents of the memory location 31").
     *
     * @return A string describing the execution of the instruction.
     */
    public abstract String getExecuteMessage();
}
