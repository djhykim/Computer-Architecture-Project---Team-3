import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * CSCI 6461 / C6461 Assembler
 *
 * - Two-pass assembler
 * - Input numbers (LOC/Data/operands) are DECIMAL
 * - Output addresses + words are OCTAL
 * - Instruction words are STRICTLY 16-bit
 *
 * Source format:
 *   [Label:]  OP  operands   ; comment
 *
 * Directives:
 *   LOC n
 *   DATA n|label
 */
public class Assembler {

    private final Map<String, Integer> sym = new HashMap<>();
    private int loc = 0;

    public static void main(String[] args) {
        String src = "source.txt";
        String lst = "listing.txt";
        String load = "load.txt";

        if (args.length >= 1) src = args[0];
        if (args.length >= 2) lst = args[1];
        if (args.length >= 3) load = args[2];

        Assembler a = new Assembler();
        try {
            a.firstPass(src);
            a.secondPass(src, lst, load);
            System.out.println("Assembly completed successfully.");
            System.out.println("Listing: " + lst);
            System.out.println("Load:    " + load);
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        } catch (RuntimeException re) {
            System.err.println("Assembly Error: " + re.getMessage());
        }
    }

    public void firstPass(String sourceFile) throws IOException {
        sym.clear();
        loc = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String raw;
            while ((raw = br.readLine()) != null) {
                ParsedLine pl = parseLine(raw);
                if (pl.kind == LineKind.BLANK_OR_COMMENT) continue;

                if (pl.label != null) {
                    if (sym.containsKey(pl.label)) {
                        throw new IllegalArgumentException("Duplicate label: " + pl.label);
                    }
                    sym.put(pl.label, loc);
                }

                if (pl.kind == LineKind.DIRECTIVE_LOC) {
                    loc = parseDecimal(pl.operands.get(0));
                } else if (pl.kind == LineKind.DIRECTIVE_DATA || pl.kind == LineKind.INSTRUCTION) {
                    loc++;
                }
            }
        }
    }

    public void secondPass(String sourceFile, String listingFile, String loadFile) throws IOException {
        loc = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile));
             PrintWriter listing = new PrintWriter(new BufferedWriter(new FileWriter(listingFile)));
             PrintWriter load = new PrintWriter(new BufferedWriter(new FileWriter(loadFile)))) {

            String raw;
            while ((raw = br.readLine()) != null) {
                ParsedLine pl = parseLine(raw);

                if (pl.kind == LineKind.BLANK_OR_COMMENT) {
                    listing.println(raw.trim());
                    continue;
                }

                if (pl.kind == LineKind.DIRECTIVE_LOC) {
                    loc = parseDecimal(pl.operands.get(0));
                    listing.println(raw.trim());
                    continue;
                }

                if (pl.kind == LineKind.LABEL_ONLY) {
                    listing.println(raw.trim());
                    continue;
                }

                if (pl.kind == LineKind.DIRECTIVE_DATA) {
                    int value = resolveDataValue(pl.operands.get(0));
                    emitWord(listing, load, loc, value, raw.trim());
                    loc++;
                    continue;
                }

                if (pl.kind == LineKind.INSTRUCTION) {
                    int word = encode(pl.op, pl.operands);
                    emitWord(listing, load, loc, word, raw.trim());
                    loc++;
                }
            }
        }
    }

    private static void emitWord(PrintWriter listing, PrintWriter load, int address, int word, String originalLine) {
        int word16 = word & 0xFFFF;
        listing.printf("%06o %06o %s%n", address & 0xFFFF, word16, originalLine);
        load.printf("%06o %06o%n", address & 0xFFFF, word16);
    }

    private int encode(String mnemonic, List<String> operands) {
        String op = mnemonic.toUpperCase(Locale.ROOT);

        switch (op) {
            case "HLT":
                return 0;

            case "LDR": { // 01 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("01"), r, ix, i, addr);
            }
            case "STR": { // 02 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("02"), r, ix, i, addr);
            }
            case "LDA": { // 03 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("03"), r, ix, i, addr);
            }
            case "AMR": { // 04 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("04"), r, ix, i, addr);
            }
            case "SMR": { // 05 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("05"), r, ix, i, addr);
            }
            case "AIR": { // 06 octal
                Req.reqOperands(op, operands, 2, 2);
                int r = dec(operands.get(0));
                int immed = dec(operands.get(1));
                return packLS(oct("06"), r, 0, 0, immed);
            }
            case "SIR": { // 07 octal
                Req.reqOperands(op, operands, 2, 2);
                int r = dec(operands.get(0));
                int immed = dec(operands.get(1));
                return packLS(oct("07"), r, 0, 0, immed);
            }

            case "JZ": { // 10 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("10"), r, ix, i, addr);
            }
            case "JNE": { // 11 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("11"), r, ix, i, addr);
            }
            case "JCC": { // 12 octal
                Req.reqOperands(op, operands, 3, 4);
                int cc = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("12"), cc, ix, i, addr);
            }
            case "JMA": { // 13 octal
                Req.reqOperands(op, operands, 2, 3);
                int ix = dec(operands.get(0));
                int addr = resolveOperandValue(operands.get(1));
                int i = (operands.size() == 3) ? parseIndirect(operands.get(2)) : 0;
                return packLS(oct("13"), 0, ix, i, addr);
            }
            case "JSR": { // 14 octal
                Req.reqOperands(op, operands, 2, 3);
                int ix = dec(operands.get(0));
                int addr = resolveOperandValue(operands.get(1));
                int i = (operands.size() == 3) ? parseIndirect(operands.get(2)) : 0;
                return packLS(oct("14"), 0, ix, i, addr);
            }
            case "RFS": { // 15 octal
                Req.reqOperands(op, operands, 1, 1);
                int immed = dec(operands.get(0));
                return packLS(oct("15"), 0, 0, 0, immed);
            }
            case "SOB": { // 16 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("16"), r, ix, i, addr);
            }
            case "JGE": { // 17 octal
                Req.reqOperands(op, operands, 3, 4);
                int r = dec(operands.get(0));
                int ix = dec(operands.get(1));
                int addr = resolveOperandValue(operands.get(2));
                int i = (operands.size() == 4) ? parseIndirect(operands.get(3)) : 0;
                return packLS(oct("17"), r, ix, i, addr);
            }

            case "TRR": { // 22 octal
                Req.reqOperands(op, operands, 2, 2);
                int r = dec(operands.get(0));
                int rx = dec(operands.get(1));
                return ((oct("22") & 0x3F) << 10)
                     | ((r & 0x03) << 8)
                     | ((rx & 0x03) << 6);
            }

            case "LDX": { // 41 octal
                Req.reqOperands(op, operands, 2, 3);
                int x = dec(operands.get(0));
                int addr = resolveOperandValue(operands.get(1));
                int i = (operands.size() == 3) ? parseIndirect(operands.get(2)) : 0;
                return packLS(oct("41"), 0, x, i, addr);
            }
            case "STX": { // 42 octal
                Req.reqOperands(op, operands, 2, 3);
                int x = dec(operands.get(0));
                int addr = resolveOperandValue(operands.get(1));
                int i = (operands.size() == 3) ? parseIndirect(operands.get(2)) : 0;
                return packLS(oct("42"), 0, x, i, addr);
            }

            case "IN": { // 61 octal
                Req.reqOperands(op, operands, 2, 2);
                int r = dec(operands.get(0));
                int devid = dec(operands.get(1));
                return packLS(oct("61"), r, 0, 0, devid);
            }
            case "OUT": { // 62 octal
                Req.reqOperands(op, operands, 2, 2);
                int r = dec(operands.get(0));
                int devid = dec(operands.get(1));
                return packLS(oct("62"), r, 0, 0, devid);
            }

            default:
                throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
        }
    }

    private static int packLS(int opcode6, int r2, int ix2, int i1, int addr5) {
        if (opcode6 < 0 || opcode6 > 0x3F) throw new IllegalArgumentException("Bad opcode: " + opcode6);
        if (r2 < 0 || r2 > 3) throw new IllegalArgumentException("Bad R/cc: " + r2);
        if (ix2 < 0 || ix2 > 3) throw new IllegalArgumentException("Bad IX: " + ix2);
        if (i1 < 0 || i1 > 1) throw new IllegalArgumentException("Bad I: " + i1);
        if (addr5 < 0 || addr5 > 31) throw new IllegalArgumentException("Bad Address/Immed: " + addr5);

        return ((opcode6 & 0x3F) << 10)
             | ((r2 & 0x03) << 8)
             | ((ix2 & 0x03) << 6)
             | ((i1 & 0x01) << 5)
             | (addr5 & 0x1F);
    }

    private enum LineKind { BLANK_OR_COMMENT, LABEL_ONLY, DIRECTIVE_LOC, DIRECTIVE_DATA, INSTRUCTION }

    private static final Pattern WS = Pattern.compile("\\s+");

    private static final class ParsedLine {
        final LineKind kind;
        final String label;
        final String op;
        final List<String> operands;

        ParsedLine(LineKind kind, String label, String op, List<String> operands) {
            this.kind = kind;
            this.label = label;
            this.op = op;
            this.operands = operands;
        }
    }

    private static ParsedLine parseLine(String rawLine) {
        if (rawLine == null) return new ParsedLine(LineKind.BLANK_OR_COMMENT, null, null, List.of());

        String noComment = stripComment(rawLine).trim();
        if (noComment.isEmpty()) return new ParsedLine(LineKind.BLANK_OR_COMMENT, null, null, List.of());

        String label = null;
        String rest = noComment;

        String[] headSplit = WS.split(rest, 2);
        if (headSplit.length > 0 && headSplit[0].endsWith(":")) {
            label = headSplit[0].substring(0, headSplit[0].length() - 1);
            rest = (headSplit.length == 2) ? headSplit[1].trim() : "";
            if (rest.isEmpty()) {
                return new ParsedLine(LineKind.LABEL_ONLY, label, null, List.of());
            }
        }

        String[] opSplit = WS.split(rest, 2);
        String op = opSplit[0].trim();
        String tail = (opSplit.length == 2) ? opSplit[1].trim() : "";

        List<String> operands = splitOperands(tail);

        if (eqi(op, "LOC")) {
            Req.reqOperands("LOC", operands, 1, 1);
            return new ParsedLine(LineKind.DIRECTIVE_LOC, label, op, operands);
        }
        if (eqi(op, "DATA")) {
            Req.reqOperands("DATA", operands, 1, 1);
            return new ParsedLine(LineKind.DIRECTIVE_DATA, label, op, operands);
        }

        return new ParsedLine(LineKind.INSTRUCTION, label, op, operands);
    }

    private static String stripComment(String s) {
        int i = s.indexOf(';');
        return (i >= 0) ? s.substring(0, i) : s;
    }

    private static List<String> splitOperands(String tail) {
        if (tail == null) return new ArrayList<>();
        String t = tail.trim();
        if (t.isEmpty()) return new ArrayList<>();

        String[] arr = t.split(",");
        List<String> out = new ArrayList<>(arr.length);
        for (String a : arr) {
            String tok = a.trim();
            if (!tok.isEmpty()) out.add(tok);
        }
        return out;
    }

    private int resolveDataValue(String token) {
        String t = token.trim();
        Integer known = sym.get(t);
        if (known != null) return known;
        return parseDecimal(t);
    }

    private int resolveOperandValue(String token) {
        String t = token.trim();
        Integer known = sym.get(t);
        if (known != null) return known;
        return parseDecimal(t);
    }

    private static int parseIndirect(String tok) {
        if (tok == null) return 0;
        String t = tok.trim();
        if (t.equalsIgnoreCase("I")) return 1;
        if (t.equals("1")) return 1;
        return 0;
    }

    private static int parseDecimal(String tok) {
        return Integer.parseInt(tok.trim());
    }

    private static int dec(String tok) {
        return parseDecimal(tok);
    }

    private static int oct(String octalLiteral) {
        return Integer.parseInt(octalLiteral, 8);
    }

    private static boolean eqi(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private static final class Req {
        static void reqOperands(String op, List<String> operands, int min, int max) {
            int n = operands == null ? 0 : operands.size();
            if (n < min || n > max) {
                throw new IllegalArgumentException(op + " expects " + min + (min == max ? "" : (".." + max))
                        + " operand(s), got " + n + " -> " + operands);
            }
        }
    }
}