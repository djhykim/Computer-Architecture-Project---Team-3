package main;

import alu.instruction.*;
import cpu.Registers;
import memory.MCU;

public class CPU {
    // Registers hold current CPU state (PC, IR, R0-R3, etc.)
    private Registers registers;
    // MCU handles all memory access, routed through cache 
    private MCU mcu;

    public CPU(Registers registers, MCU mcu) {
        this.registers = registers;
        this.mcu = mcu;
    }
    // step() performs one fetch-decode-execute cycle
    public void step() throws Exception {

        int pc = registers.getPC();
        // FETCH: load instruction from memory into IR
        registers.setMAR(pc);
        registers.setMBR(mcu.fetchFromCache(pc));
        registers.setIR(registers.getMBR());
        // DECODE: convert top 6 bits of IR to opcode number
        String instruction = registers.getBinaryStringIr();
        // EXECUTE: dispatch instruction to ALU or control logic    
        int opcode = Integer.parseInt(instruction.substring(0, 6), 2);
        // EXECUTE: run the instruction
        AbstractInstruction inst = decodeInstruction(opcode);
        inst.execute(instruction, registers, mcu);
    }
     public void run() throws Exception {
        while (true) {
            step();
        }
    }


    private AbstractInstruction decodeInstruction(int opcode) {
     switch (opcode) {
        case 0:  return new HLT();   // octal 00
        case 1:  return new LDR();   // octal 01
        case 2:  return new STR();   // octal 02
        case 3:  return new LDA();   // octal 03
        case 4:  return new AMR();   // octal 04
        case 5:  return new SMR();   // octal 05
        case 6:  return new AIR();   // octal 06
        case 7:  return new SIR();   // octal 07
        case 8:  return new JZ();    // octal 10
        case 9:  return new JNE();   // octal 11
        case 10: return new JCC();   // octal 12
        case 11: return new JMA();   // octal 13
        case 12: return new JSR();   // octal 14
        case 13: return new RFS();   // octal 15
        case 14: return new SOB();   // octal 16
        case 15: return new JGE();   // octal 17
        case 25: return new SRC();   // octal 31
        case 26: return new RRC();   // octal 32
        case 33: return new LDX();   // octal 41
        case 34: return new STX();   // octal 42
        case 49: return new IN();    // octal 61
        case 50: return new OUT();   // octal 62
        case 56: return new MLT();   // octal 70
        case 57: return new DVD();   // octal 71
        case 58: return new TRR();   // octal 72
        case 59: return new AND();   // octal 73
        case 60: return new ORR();   // octal 74
        case 61: return new NOT();   // octal 75
        default: throw new RuntimeException("Unknown opcode: " + opcode);
    }
}
}