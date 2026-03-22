package main;

import cpu.Registers;
import memory.MCU;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        Registers registers = new Registers();
        MCU mcu = new MCU();
        CPU cpu = new CPU(registers, mcu);

        registers.setX1(32);

        Scanner sc = new Scanner(System.in);

        int[] values = new int[20];

        System.out.println("Enter 20 numbers:");

        // Input + store in memory
        for (int i = 0; i < 20; i++) {
            values[i] = sc.nextInt();
            mcu.storeIntoMemory(40 + i, values[i]);
        }

        System.out.println("\nStored values:");
        for (int i = 0; i < 20; i++) {
            System.out.print(values[i] + " ");
        }

        System.out.println("\n\nEnter target value:");
        int target = sc.nextInt();

        // Load first value using CPU
        int inst = Integer.parseInt("0000010001001000", 2); // LDR R0,1,8
        mcu.storeIntoMemory(10, inst);

        registers.setPC(10);
        cpu.step(); // R0 = first value

        int bestValue = registers.getR0();
        int bestDiff = Math.abs(bestValue - target);

        // Process remaining values
        for (int i = 1; i < 20; i++) {

            // Update address using X1
            registers.setX1(32 + i);

            mcu.storeIntoMemory(11, inst); // reuse LDR
            registers.setPC(11);
            cpu.step();

            int value = registers.getR0();
            int diff = Math.abs(value - target);

            if (diff < bestDiff) {
                bestDiff = diff;
                bestValue = value;
            }
        }

        boolean exactFound = false;
for (int i = 0; i < 20; i++) {
    if (values[i] == target) {
        exactFound = true;
        break;
    }
}

System.out.println("\nTarget: " + target);
if (exactFound) {
    System.out.println("Found exact match: " + target);
} else {
    System.out.println("Closest value: " + bestValue);
}
    }
}