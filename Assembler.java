import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
/**
 * C6461 Two-Pass Assembler
 *
 * This assembler implements a classic two-pass design for the C6461 architecture.
 * Pass 1: Scans source.txt to build the symbol table and determine memory layout.
 * Pass 2: Re-reads source.txt, resolves all label references, encodes each
 *         instruction into a 16-bit machine word and writes both output files.
 *
 * The assembler converts C6461 assembly language instructions into 16-bit
 * machine code representations in octal format, following the C6461 instruction
 * set architecture specification.
 *
 * Supported Instructions : LDR, LDA, LDX, JZ, HLT
 * Supported Directives   : LOC, DATA, End:
 *
 * 16-bit Machine Word Layout:
 *   Bits 15-10 : Opcode (6 bits)
 *   Bits  9-8  : R  — general-purpose register (0-3)
 *   Bits  7-6  : IX — index register (0-3)
 *   Bit   5    : I  — indirect addressing flag
 *   Bits  4-0  : Address / immediate value (5 bits, max 31)
 *
 * Input:  source.txt  — assembly source file (hardcoded filename)
 * Output: listing.txt — octal address, machine code and original source line
 *         load.txt    — octal address and machine code only (simulator input)
 *
 * Requirements: JDK 21 or higher
 *
 * Team 3 - C6461 Computer Architecture Project
 */

public class Assembler {

    // --- State ---
    // Symbol table: maps label names to their memory addresses (built in Pass 1)
    private final Map<String, Integer> sym = new HashMap<>();
        
    // Location counter: tracks the current memory address during assembly
    private int loc = 0;

    // --- Entry point ---
    public static void main(String[] args) {
        String src = "source.txt";
        String lst = "listing.txt";
        String load = "load.txt";

        Assembler a = new Assembler();
        try {
            a.firstPass(src);
            a.secondPass(src, lst, load);
            System.out.println("Assembly completed successfully.");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // --- Pass 1: build symbol table + compute locations ---
    public void firstPass(String sourceFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String raw;
            while ((raw = br.readLine()) != null) {
                String line = raw.trim();
                if (isBlankOrComment(line)) continue;

                LineParts lp = peelLabel(line); // extract label and remainder of the line
                if (lp.label != null) {
                    sym.put(lp.label, loc);
                }
                if (lp.rest == null) continue; // label-only line

                Token t = tokenizeHead(lp.rest);
                if (t == null) continue;

                if (eqi(t.op, "LOC")) {
                    loc = parseIntFirst(t.tail);
                } else if (isWordAllocatingOp(t.op)) {
                    loc++;
                }
            }
        }
    }

    // --- Pass 2: Instruction Translation : emit listing + load ---
    public void secondPass(String sourceFile, String listingFile, String loadFile) throws IOException {
        loc = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile));
             PrintWriter listing = new PrintWriter(new BufferedWriter(new FileWriter(listingFile)));
             PrintWriter load = new PrintWriter(new BufferedWriter(new FileWriter(loadFile)))) {

            String raw;
            while ((raw = br.readLine()) != null) {
                String trimmed = raw.trim();

                // keep comments/blank lines in listing
                if (isBlankOrComment(trimmed)) {
                    listing.println(trimmed);
                    continue;
                }

                // If line starts with a label, print label line as-is (matching your behavior),
                // and then continue (your code does this).
                if (looksLikeLabelOnlyOrLabelStart(trimmed)) {
                    // If it's "Label:" only, just print it.
                    if (trimmed.endsWith(":") && trimmed.indexOf(' ') < 0 && trimmed.indexOf('\t') < 0) {
                        listing.println(trimmed);
                        continue;
                    }
                    // If it begins with label + more tokens, your code prints labels "as is"
                    // only when parts[0] endsWith ":" after split; it then continues.
                    // To match that same visible behavior, we do the same:
                    String first = tokenizeHead(trimmed) != null ? tokenizeHead(trimmed).op : "";
                    if (first.endsWith(":")) {
                        listing.println(trimmed);
                        continue;
                    }
                }

                try {
                    Token t = tokenizeHead(trimmed);
                    if (t == null) continue;

                    if (eqi(t.op, "LOC")) {
                        loc = parseIntFirst(t.tail);
                        listing.println(" " + trimmed); // leading space for LOC lines
                        continue;
                    }

                    if (eqi(t.op, "DATA")) {
                        int value = resolveDataValue(t.tail);
                        emitWord(listing, load, loc, value, trimmed);
                        loc++;
                        continue;
                    }

                    // Your code checks parts[0].equalsIgnoreCase("End:")
                    if (eqi(t.op, "End:")) {
                        emitWord(listing, load, loc, 0, trimmed);
                        // (your code doesn't increment loc here)
                        continue;
                    }

                    if (isInstruction(t.op)) {
                        int word = encode(t.op, t.tail == null ? "" : t.tail);
                        emitWord(listing, load, loc, word, trimmed);
                        loc++;
                        continue;
                    }

                    System.err.println("Error: Unknown instruction " + t.op);

                } catch (NumberFormatException nfe) {
                    System.err.println("Error processing line: " + trimmed);
                    System.err.println("Error details: " + nfe.getMessage());
                }
            }
        }
    }

    // --- Emit helpers ---
    // Format: %06o %06o (6-digit octal) per C6461 specification.
    private static void emitWord(PrintWriter listing, PrintWriter load, int address, int word, String originalLine) {
        listing.printf("%06o %06o %s%n", address, word, originalLine);
        load.printf("%06o %06o%n", address, word);
    }

    // --- Encoding (same behavior, different code style) ---
    // Instruction Encoding
    // Assembles a 16-bit machine word from the mnemonic and operands.
    //
    //   16-bit layout:
    //   Bits 15-10 : Opcode (6 bits)
    //   Bits  9-8  : R  — general-purpose register (0-3)
    //   Bits  7-6  : IX — index register (0-3)
    //   Bit   5    : I  — indirect addressing flag
    //   Bits  4-0  : Address / immediate value (5 bits, max 31)
    private int encode(String opcode, String operandText) {
        String ops = stripComment(operandText);
        String[] parts = splitCommaArgs(ops);

        switch (opcode.toUpperCase(Locale.ROOT)) {
            case "LDX": { // Load Index Register: LDX x,address
                int r = parse(parts, 0);
                int addr = parse(parts, 1);
                return 0x102000 | (r << 9) | addr;
            }
            case "LDR": { // Load Register: LDR r,x,address
                int r = parse(parts, 0);
                int ix = parse(parts, 1);
                int addr = parse(parts, 2);
                int w = 0x000000 | (r << 13) | (ix << 9) | addr;
                if (parts.length > 3) w |= 0x000400; // indirect bit
                return w;
            }
            case "LDA": { // Load Address: LDA r,x,address
                int r = parse(parts, 0);
                int ix = parse(parts, 1);
                int addr = parse(parts, 2);
                return 0x006000 | (r << 13) | (ix << 9) | addr;
            }
            case "JZ": { // Jump if Zero: JZ r,x,address
                int r = parse(parts, 0);
                int ix = parse(parts, 1);
                int addr = parse(parts, 2);
                return 0x020000 | (r << 9) | (ix << 5) | addr;
            }
            case "HLT":  // Halt instruction has no operands
                return 0x000000;
            default:
                return 0;
        }
    }

    // --- Parsing helpers ---
    private static final Pattern WS = Pattern.compile("\\s+");

//returns true if the line is empty or comment starts with ;
    private static boolean isBlankOrComment(String line) {
        return line == null || line.isEmpty() || line.startsWith(";");
    }
//removes everything from first ; to end of line
    private static String stripComment(String s) {
        if (s == null) return "";
        int i = s.indexOf(';');
        return (i >= 0 ? s.substring(0, i) : s).trim();
    }
//splits an operand string on commas, stripping inline comments first
    private static String[] splitCommaArgs(String s) {
        String cleaned = stripComment(s);
        if (cleaned.isEmpty()) return new String[0];
        String[] arr = cleaned.split(",");
        for (int i = 0; i < arr.length; i++) arr[i] = arr[i].trim();
        return arr;
    }
//parses the integer at parts[idx]
    private static int parse(String[] parts, int idx) {
        if (idx >= parts.length) return 0;
        return Integer.parseInt(parts[idx].trim());
    }
// parses the first whitespace-delimited integer token from tail
    private static int parseIntFirst(String tail) {
        if (tail == null) throw new NumberFormatException("Missing operand");
        String[] p = WS.split(tail.trim());
        return Integer.parseInt(p[0]);
    }
//resolves a data operand to an integer value
    private int resolveDataValue(String tail) {
        String[] p = WS.split(stripComment(tail));
        if (p.length == 0) return 0;

        String token = p[0];
        Integer known = sym.get(token);
        if (known != null) return known;

        return Integer.parseInt(token);
    }

    private static boolean eqi(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }
//returns true if the operation is one of the 5 supported C6461 instruction mnemonics
    private static boolean isInstruction(String op) {
        if (op == null) return false;
        switch (op.toUpperCase(Locale.ROOT)) {
            case "HLT":
            case "LDX":
            case "LDR":
            case "LDA":
            case "JZ":
                return true;
            default:
                return false;
        }
    }
    //returns true for operations that consume one memory word
    private static boolean isWordAllocatingOp(String op) {
        return isInstruction(op) || eqi(op, "DATA");
    }
//returns true if the first token of the line ends with :
    private static boolean looksLikeLabelOnlyOrLabelStart(String line) {
        // quick heuristic: first token ends with ":"
        String[] p = WS.split(line, 2);
        return p.length > 0 && p[0].endsWith(":");
    }
//Splits line into its leading token and the remainder
    private static Token tokenizeHead(String line) {
        if (line == null) return null;
        String[] p = WS.split(line.trim(), 2);
        if (p.length == 0 || p[0].isEmpty()) return null;
        String head = p[0];
        String tail = (p.length > 1) ? p[1] : null;
        return new Token(head, tail);
    }
 // return label=null if no label is present
    private static LineParts peelLabel(String line) {
        Token t = tokenizeHead(line);
        if (t == null) return new LineParts(null, null);

        if (t.op.endsWith(":")) {
            String label = t.op.substring(0, t.op.length() - 1);
            String rest = t.tail;
            if (rest == null || rest.trim().isEmpty()) {
                return new LineParts(label, null);
            }
            return new LineParts(label, rest.trim());
        }
        return new LineParts(null, line);
    }

    // --- Tiny structs ---
    private static final class Token {
        final String op;
        final String tail;
        Token(String op, String tail) { this.op = op; this.tail = tail; }
    }
    // Holds label extracted from the line and rest of the line after it
    private static final class LineParts {
        final String label;
        final String rest;
        LineParts(String label, String rest) { this.label = label; this.rest = rest; }
    }
}
