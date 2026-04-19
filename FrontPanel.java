package front;

// Standard imports
import java.io.*;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.Color;
import util.Const;
import java.util.*;
import java.awt.Dimension;
import java.util.function.BiConsumer;

// Custom imports
import cpu.Registers;
import memory.Test;
import alu.instruction.AbstractInstruction;
import util.MachineFaultException;
import memory.MCU;

public class FrontPanel {
    private int enableStatus;
    JFrame mainFrame;

    private JPanel regPanel, indexRegPanel, statusRegPanel, generalRegPanel;
    private JPanel reg0Panel, reg1Panel, reg2Panel, reg3Panel;
    private JPanel index1Panel, index2Panel, index3Panel;
    private JPanel marPanel, mbrPanel, msrPanel, irPanel, pcPanel, ccPanel, mfrPanel;

    private JButton btnLoadReg0, btnLoadReg1, btnLoadReg2, btnLoadReg3;
    private JButton btnLoadIndex1, btnLoadIndex2, btnLoadIndex3;
    private JButton btnLoadMAR, btnLoadMBR, btnLoadMSR, btnLoadIR, btnLoadPC, btnLoadCC, btnLoadMFR;
    private JButton btnSingleStep, btnInitialProgramLoad, btnReset, btnHalt, btnStore, btnRun;
    private JButton btnSearchParagraph;

    private Registers cpuRegisters;
    private MCU memoryControlUnit;
    private JTextArea consoleOutput;

    private JPanel cachePanel, programPanel1, programPanel2;
    private JLabel labelCache;
    private JTable cacheTable;
    private JScrollPane scrollPaneCache;

    public static HashMap<Integer, String> instructionMap;
    public static ArrayList<Integer> addressList;
    public static ArrayList<Integer> memoryAddressList;
    private JTextField currentTextField;

    public HashMap<String, Queue<String>> inputQueueMap;
    public static int instructionIndex;
    public static HashMap<String, String> assemblerResults;

    private final HashMap<String, JTextField> textFieldMap = new HashMap<>();

    private static final Color BACKGROUND_COLOR = new Color(144, 238, 144);
    private static final Color TEXT_COLOR = new Color(0, 0, 0);
    private static final Color BUTTON_COLOR = Color.DARK_GRAY;
    private static final Dimension BUTTON_DIMENSION = new Dimension(80, 24);
    private static final Dimension TEXTFIELD_DIMENSION = new Dimension(160, 24);

    public FrontPanel() {
        initComponents();
        addListeners();
        instructionMap = new HashMap<>();
        addressList = new ArrayList<>();
        memoryAddressList = new ArrayList<>();
        inputQueueMap = new HashMap<>();
        initCPU();

        this.btnLoadReg0.addMouseListener(createRegisterMouseListener(textFieldMap.get("R0"), Registers::setR0));
        this.btnLoadReg1.addMouseListener(createRegisterMouseListener(textFieldMap.get("R1"), Registers::setR1));
        this.btnLoadReg2.addMouseListener(createRegisterMouseListener(textFieldMap.get("R2"), Registers::setR2));
        this.btnLoadReg3.addMouseListener(createRegisterMouseListener(textFieldMap.get("R3"), Registers::setR3));
        this.btnLoadIndex1.addMouseListener(createRegisterMouseListener(textFieldMap.get("X1"), Registers::setX1));
        this.btnLoadIndex2.addMouseListener(createRegisterMouseListener(textFieldMap.get("X2"), Registers::setX2));
        this.btnLoadIndex3.addMouseListener(createRegisterMouseListener(textFieldMap.get("X3"), Registers::setX3));
        this.btnLoadMAR.addMouseListener(createRegisterMouseListener(textFieldMap.get("MAR"), Registers::setMAR));
        this.btnLoadMBR.addMouseListener(createRegisterMouseListener(textFieldMap.get("MBR"), Registers::setMBR));
        this.btnLoadMSR.addMouseListener(createRegisterMouseListener(textFieldMap.get("MSR"), Registers::setMSR));
        this.btnLoadMFR.addMouseListener(createRegisterMouseListener(textFieldMap.get("MFR"), Registers::setMFR));
        this.btnLoadPC.addMouseListener(createRegisterMouseListener(textFieldMap.get("PC"), Registers::setPC));
        this.btnLoadIR.addMouseListener(createRegisterMouseListener(textFieldMap.get("IR"), Registers::setIR));
        this.btnLoadCC.addMouseListener(createRegisterMouseListener(textFieldMap.get("CC"), Registers::setCC));
    }

    private void initComponents() {
        this.mainFrame = new JFrame();
        configureFrame();
        this.mainFrame.setSize(new Dimension(1000, 600));

        this.regPanel = createPanel(BACKGROUND_COLOR);
        this.indexRegPanel = createPanel(BACKGROUND_COLOR);
        this.statusRegPanel = createPanel(BACKGROUND_COLOR);
        this.generalRegPanel = createPanel(BACKGROUND_COLOR);

        this.reg0Panel = createRegisterPanel("R0", true);
        this.btnLoadReg0 = (JButton) reg0Panel.getComponent(0);

        this.reg1Panel = createRegisterPanel("R1", true);
        this.btnLoadReg1 = (JButton) reg1Panel.getComponent(0);

        this.reg2Panel = createRegisterPanel("R2", true);
        this.btnLoadReg2 = (JButton) reg2Panel.getComponent(0);

        this.reg3Panel = createRegisterPanel("R3", true);
        this.btnLoadReg3 = (JButton) reg3Panel.getComponent(0);

        this.index1Panel = createRegisterPanel("X1", true);
        this.btnLoadIndex1 = (JButton) index1Panel.getComponent(0);

        this.index2Panel = createRegisterPanel("X2", true);
        this.btnLoadIndex2 = (JButton) index2Panel.getComponent(0);

        this.index3Panel = createRegisterPanel("X3", true);
        this.btnLoadIndex3 = (JButton) index3Panel.getComponent(0);

        this.marPanel = createRegisterPanel("MAR", false);
        this.btnLoadMAR = (JButton) marPanel.getComponent(0);

        this.mbrPanel = createRegisterPanel("MBR", false);
        this.btnLoadMBR = (JButton) mbrPanel.getComponent(0);

        this.msrPanel = createRegisterPanel("MSR", false);
        this.btnLoadMSR = (JButton) msrPanel.getComponent(0);

        this.irPanel = createRegisterPanel("IR", false);
        this.btnLoadIR = (JButton) irPanel.getComponent(0);

        this.pcPanel = createRegisterPanel("PC", true);
        this.btnLoadPC = (JButton) pcPanel.getComponent(0);

        this.mfrPanel = createRegisterPanel("MFR", false);
        this.btnLoadMFR = (JButton) mfrPanel.getComponent(0);

        this.ccPanel = createRegisterPanel("CC", false);
        this.btnLoadCC = (JButton) ccPanel.getComponent(0);

        layoutPanels();
        addButtons();
        addCache();
        addTabbedPane();
    }

    private void configureFrame() {
        mainFrame.setTitle("TEAM 3 - CSCI 6461 COMPUTER SIMULATOR");
        try {
        // Create a 1x1 transparent image to act as a "blank" icon
        java.awt.image.BufferedImage blankIcon = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        mainFrame.setIconImage(blankIcon);
    } catch (Exception e) {
        // Log error if icon fails to load
        System.out.println("Could not change icon.");
    }
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private JPanel createPanel(Color bgColor) {
        JPanel panel = new JPanel();
        panel.setBackground(bgColor);
        return panel;
    }

    private JPanel createRegisterPanel(String label, boolean editable) {
        JPanel panel = createPanel(BACKGROUND_COLOR);
        JLabel lbl = new JLabel(label);
        JTextField textField = new JTextField();
        textField.setEditable(editable);
        textField.setPreferredSize(TEXTFIELD_DIMENSION);
        textField.setBackground(Color.WHITE);
        textField.setForeground(TEXT_COLOR);
        textField.setName(label);

        textFieldMap.put(label, textField);

        JButton button = new JButton("LOAD");
        button.setPreferredSize(BUTTON_DIMENSION);

        panel.add(button);
        panel.add(lbl);
        panel.add(textField);

        return panel;
    }

    private void layoutPanels() {
        mainFrame.getContentPane().setLayout(new BorderLayout());

        JPanel leftPanel = createPanel(BACKGROUND_COLOR);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(createSectionPanel("GENERAL PURPOSE REGISTERS", reg0Panel, reg1Panel, reg2Panel, reg3Panel));
        leftPanel.add(createSectionPanel("INDEX REGISTERS", index1Panel, index2Panel, index3Panel));
        leftPanel.add(createSectionPanel("ADDRESS", marPanel, mbrPanel, mfrPanel, ccPanel));
        leftPanel.add(createSectionPanel("OTHER REGISTERS", msrPanel, irPanel, pcPanel));

        JScrollPane leftScrollPane = new JScrollPane(leftPanel);
        mainFrame.getContentPane().add(leftScrollPane, BorderLayout.WEST);
    }

    private JPanel createSectionPanel(String title, JPanel... panels) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBorder(BorderFactory.createTitledBorder(title));

        for (JPanel panel : panels) {
            sectionPanel.add(panel);
        }
        return sectionPanel;
    }

    private void appendKeyboardChar(char c) {
        String current = this.memoryControlUnit.getKeyboardBuffer();
        if (current == null) current = "";
        this.memoryControlUnit.setKeyboardBuffer(current + c);
    }

    private void addTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel consolePanel = new JPanel(new BorderLayout());
        this.consoleOutput = new JTextArea();
        this.consoleOutput.setEditable(true);
        JScrollPane scrollPaneConsole = new JScrollPane(this.consoleOutput);
        consolePanel.add(scrollPaneConsole, BorderLayout.CENTER);

        consoleOutput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                SwingUtilities.invokeLater(() -> {
                    try {
                        int caretPosition = consoleOutput.getCaretPosition();
                        int lineOffset = consoleOutput.getLineStartOffset(
                                consoleOutput.getLineOfOffset(caretPosition));
                        if (lineOffset == caretPosition &&
                                (caretPosition == 0 ||
                                 consoleOutput.getText().charAt(caretPosition - 1) == '\n')) {
                            consoleOutput.getDocument().insertString(caretPosition, "> ", null);
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                });

                if (c != KeyEvent.CHAR_UNDEFINED) {
                    if (c == '\n' || c == '\r') {
                        appendKeyboardChar('\n');
                    } else if (!Character.isISOControl(c)) {
                        appendKeyboardChar(c);
                    }
                }
            }
        });

        tabbedPane.addTab("Console", null, consolePanel, "Console Output/Input");
        mainFrame.add(tabbedPane, BorderLayout.CENTER);
    }

    private JButton createButton(String label, Color bgColor) {
        JButton button = new JButton(label);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(BUTTON_DIMENSION);
        return button;
    }

    private void addButtons() {
        JPanel buttonPanel = createPanel(BACKGROUND_COLOR);
        buttonPanel.setLayout(new FlowLayout());

        this.btnInitialProgramLoad = createButton("IPL", BUTTON_COLOR);
        this.btnRun = createButton("Run", BUTTON_COLOR);
        this.btnSearchParagraph = createButton("Search", BUTTON_COLOR);
        this.btnSingleStep = createButton("SS", BUTTON_COLOR);
        this.btnReset = createButton("Reset", BUTTON_COLOR);
        this.btnHalt = createButton("Halt", BUTTON_COLOR);
        this.btnStore = createButton("Store", BUTTON_COLOR);

        buttonPanel.add(this.btnInitialProgramLoad);
        buttonPanel.add(this.btnRun);
        buttonPanel.add(this.btnSearchParagraph);
        buttonPanel.add(this.btnSingleStep);
        buttonPanel.add(this.btnReset);
        buttonPanel.add(this.btnHalt);
        buttonPanel.add(this.btnStore);

        mainFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        this.btnSingleStep.setEnabled(false);
        this.btnRun.setEnabled(false);

        this.btnStore.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentTextField != null) {
                    String valueStr = currentTextField.getText();
                    try {
                        int value = Integer.parseInt(valueStr, 8);
                        switch (currentTextField.getName()) {
                            case "R0": cpuRegisters.setR0(value); break;
                            case "R1": cpuRegisters.setR1(value); break;
                            case "R2": cpuRegisters.setR2(value); break;
                            case "R3": cpuRegisters.setR3(value); break;
                            case "X1": cpuRegisters.setX1(value); break;
                            case "X2": cpuRegisters.setX2(value); break;
                            case "X3": cpuRegisters.setX3(value); break;
                            case "MAR": cpuRegisters.setMAR(value); break;
                            case "MBR": cpuRegisters.setMBR(value); break;
                            case "MSR": cpuRegisters.setMSR(value); break;
                            case "IR": cpuRegisters.setIR(value); break;
                            case "PC": cpuRegisters.setPC(value); break;
                            case "CC": cpuRegisters.setCC(value); break;
                            case "MFR": cpuRegisters.setMFR(value); break;
                            default:
                                printConsole("Invalid register selected.");
                                return;
                        }
                        printConsole("Stored value " + value + " in " + currentTextField.getName());
                    } catch (NumberFormatException ex) {
                        printConsole("Invalid input. Please enter a valid octal value.");
                    }
                } else {
                    printConsole("No register selected for storing.");
                }
            }
        });

        this.btnSearchParagraph.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    String paragraph = readParagraphFromMemory(100);
                    if (paragraph.isEmpty()) {
                        printConsole("No paragraph found at address 100.");
                        return;
                    }

                    // Print sentences to console
                    String[] sentences = paragraph.split("(?<=[.!?])\\s+");
                    for (int i = 0; i < sentences.length; i++) {
                        printConsole(String.format("Sentence %d: %s", i + 1, sentences[i].trim()));
                    }

String word = (String) JOptionPane.showInputDialog(mainFrame, "Enter search word:", "Search", JOptionPane.PLAIN_MESSAGE, null, null, "");                    if (word == null || word.trim().isEmpty()) {
                        printConsole("Search cancelled or empty input.");
                        return;
                    }

                    String search = word.trim();
                    boolean foundAny = false;
                    for (int i = 0; i < sentences.length; i++) {
                        String[] words = sentences[i].trim().split("\\s+");
                        for (int j = 0; j < words.length; j++) {
                            String w = words[j].replaceAll("[^A-Za-z0-9]", "");
                            if (w.equalsIgnoreCase(search)) {
                                printConsole(String.format("Found '%s' in sentence %d, word %d", search, i + 1, j + 1));
                                foundAny = true;
                            }
                        }
                    }
                    if (!foundAny) {
                        printConsole(String.format("'%s' not found in paragraph.", search));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    printConsole("Search error: " + ex.getMessage());
                }
            }
        });
    }

    private void addCache() {
        this.cachePanel = new JPanel();
        this.cachePanel.setBounds(808, 418, 254, 147);
        this.labelCache = new JLabel("Cache");
        this.labelCache.setForeground(new Color(204, 255, 204));

        this.scrollPaneCache = new JScrollPane();
        this.cacheTable = new JTable(16, 2);
        this.cacheTable.setEnabled(false);

        this.scrollPaneCache.setViewportView(this.cacheTable);
        this.cacheTable.setModel(new DefaultTableModel(
                new Object[][]{
                        new Object[2], new Object[2], new Object[2], new Object[2],
                        new Object[2], new Object[2], new Object[2], new Object[2],
                        new Object[2], new Object[2], new Object[2], new Object[2],
                        new Object[2], new Object[2], new Object[2], new Object[2]
                },
                new String[]{"Tag", "Data"}));

        this.cachePanel.setLayout(new BoxLayout(this.cachePanel, BoxLayout.Y_AXIS));
        this.cachePanel.add(this.labelCache);
        this.cachePanel.add(this.scrollPaneCache);
        this.cachePanel.setBackground(new Color(60, 80, 65));
    }

    private void initCPU() {
        this.cpuRegisters = new Registers();
        this.memoryControlUnit = new MCU();
        this.cpuRegisters.setPC(addressList.isEmpty() ? 10 : addressList.get(0));
    }

    private MouseAdapter createRegisterMouseListener(JTextField textField, BiConsumer<Registers, Integer> registerSetter) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentTextField = textField;
                try {
                    String valueStr = textField.getText();
                    int value = Integer.parseInt(valueStr, 8);
                    if (valueStr.equals(Integer.toString(value, 8))) {
                        registerSetter.accept(FrontPanel.this.cpuRegisters, value);
                        String message = textField.getName() + " is set to: " + valueStr;
                        System.out.println(message);
                        FrontPanel.this.printConsole(message);
                    } else {
                        String message = "Invalid value for " + textField.getName() + ". Please enter an octal value.";
                        System.out.println(message);
                        FrontPanel.this.printConsole(message);
                    }
                } catch (NumberFormatException ex) {
                    String message = "Invalid value for " + textField.getName() + ". Please enter an octal value.";
                    System.out.println(message);
                    FrontPanel.this.printConsole(message);
                }
            }
        };
    }

    private void addListeners() {
        this.btnRun.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        new Thread(() -> {
            try {
                // If paragraph text is present in memory, enqueue it to the MCU printer buffer
                try {
                    String para = readParagraphFromMemory(100);
                    if (para != null && !para.isEmpty()) {
                        String curr = FrontPanel.this.memoryControlUnit.getPrinterBuffer();
                        if (curr == null) curr = "";
                        FrontPanel.this.memoryControlUnit.setPrinterBuffer(curr + para + "\n");
                    }
                } catch (Exception ignored) {}
                for (int steps = 0; steps < 5000; steps++) {
                    int pc = FrontPanel.this.cpuRegisters.getPC();

                    int word = FrontPanel.this.memoryControlUnit.fetchFromCache(pc);
                    FrontPanel.this.cpuRegisters.setMAR(pc);
                    FrontPanel.this.cpuRegisters.setMBR(word);
                    FrontPanel.this.cpuRegisters.setIR(word);

                    String instruction = String.format("%16s",
                            Integer.toBinaryString(word & 0xFFFF)).replace(' ', '0');

                    SwingUtilities.invokeLater(() ->
                            FrontPanel.this.printConsole(
                                    String.format("PC: %06o, instruction: %s", pc, instruction)));

                    if ((word & 0xFFFF) == 0) {
                        SwingUtilities.invokeLater(() ->
                                FrontPanel.this.printConsole("HLT reached. Stopping."));
                        break;
                    }

                        int pcBeforeInstr = FrontPanel.this.cpuRegisters.getPC();
                        FrontPanel.this.runInstruction(
                            instruction,
                            FrontPanel.this.cpuRegisters,
                            FrontPanel.this.memoryControlUnit
                        );
                        int pcAfterInstr = FrontPanel.this.cpuRegisters.getPC();
                        final int pbb = pcBeforeInstr;
                        final int pab = pcAfterInstr;
                            final int r3val = FrontPanel.this.cpuRegisters.getR3();
                            SwingUtilities.invokeLater(() -> FrontPanel.this.printConsole(
                                String.format("PC before: %06o, PC after: %06o, R3: %06o", pbb, pab, r3val)
                            ));

                    SwingUtilities.invokeLater(() -> {
                        FrontPanel.this.refreshRegistersPanel();
                    });

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        FrontPanel.this.printConsole("Run error: " + ex.getMessage()));
            }
        }).start();
    }
});this.btnSingleStep.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int pc = cpuRegisters.getPC();

            int word = memoryControlUnit.fetchFromCache(pc);
            cpuRegisters.setMAR(pc);
            cpuRegisters.setMBR(word);
            cpuRegisters.setIR(word);

            String instruction = String.format("%16s",
                    Integer.toBinaryString(word & 0xFFFF)).replace(' ', '0');

            printConsole(
                String.format("PC: %06o, instruction: %s", pc, instruction)
            );

            if ((word & 0xFFFF) == 0) {
                printConsole("HLT reached. Stopping.");
                return;
            }

            runInstruction(instruction, cpuRegisters, memoryControlUnit);

            refreshRegistersPanel();

        } catch (Exception ex) {
            ex.printStackTrace();
            printConsole("SS error: " + ex.getMessage());
        }
    }
});

     

        this.btnReset.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                clearAll();
            }
        });

        this.btnInitialProgramLoad.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (!FrontPanel.this.btnInitialProgramLoad.isEnabled()) {
                    return;
                }

                FrontPanel.this.initCPU();

                try {
                        FrontPanel.this.memoryControlUnit.loadProgramFile("load.txt");
                        FrontPanel.this.loadParagraphIntoMemory("paragraph.txt", 100);

                        // Change this if your assembled Program 2 starts somewhere else.
                      // Set PC to program start. Use 10 (decimal) as the standard
                      // entry point so we don't start at addresses 0/1 which contain
                      // support/return instructions that cause a 0/1 toggle loop.
                      FrontPanel.this.cpuRegisters.setPC(10);
                      // Ensure R3 has a sensible return address so RFS doesn't jump to 0
                      FrontPanel.this.cpuRegisters.setR3(FrontPanel.this.cpuRegisters.getPC() + 1);

                        // Paragraph is loaded into memory by loadParagraphIntoMemory.
                        // Do NOT preload the MCU printer buffer here — let the program
                        // running under `Run` fetch from memory and use `OUT` to print.

                } catch (Exception ex) {
                    ex.printStackTrace();
                    FrontPanel.this.printConsole("Failed to load program file: " + ex.getMessage());
                    return;
                }

                FrontPanel.this.btnInitialProgramLoad.setEnabled(false);

                if (FrontPanel.this.enableStatus == 0) {
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.regPanel, true);
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.statusRegPanel, true);
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.generalRegPanel, true);
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.indexRegPanel, true);

                    if (FrontPanel.this.programPanel1 != null) {
                        FrontPanel.this.setEnableForPanel(FrontPanel.this.programPanel1, true);
                    }
                    if (FrontPanel.this.programPanel2 != null) {
                        FrontPanel.this.setEnableForPanel(FrontPanel.this.programPanel2, true);
                    }

                    FrontPanel.this.btnSingleStep.setEnabled(true);
                    FrontPanel.this.btnRun.setEnabled(true);
                    FrontPanel.access$41(FrontPanel.this, 1);
                }

                FrontPanel.this.refreshRegistersPanel();
                FrontPanel.this.printConsole("IPL complete! Program 2 loaded.");
                FrontPanel.this.printConsole("Type the search word in the console when Program 2 requests input.");
            }
        });
    }

    private void refreshCacheTable() {
        int row = 0;
        for (final Test.CacheLine line : this.memoryControlUnit.getCache().getCacheLines()) {
            this.cacheTable.setValueAt(line.getTag(), row, 0);
            this.cacheTable.setValueAt(line.getData(), row, 1);
            row++;
        }
    }

    private void pushConsoleBuffer() {
        if (this.memoryControlUnit.getPrinterBuffer() != null
                && !this.memoryControlUnit.getPrinterBuffer().isEmpty()) {
            this.consoleOutput.append(this.memoryControlUnit.getPrinterBuffer());
            this.memoryControlUnit.setPrinterBuffer("");
        }
    }

    private String readParagraphFromMemory(int startAddress) {
        StringBuilder sb = new StringBuilder();
        int addr = startAddress;
        while (true) {
            int val = this.memoryControlUnit.fetchFromCache(addr);
            if (val == 0) break; // safety
            char c = (char) (val & 0xFF);
            if (c == '#') break;
            sb.append(c);
            addr++;
            if (addr > startAddress + 10000) break; // safety guard
        }
        return sb.toString().trim();
    }

    private void refreshRegistersPanel() {
        refreshPanel(this.reg0Panel);
        refreshPanel(this.reg1Panel);
        refreshPanel(this.reg2Panel);
        refreshPanel(this.reg3Panel);
        refreshPanel(this.index1Panel);
        refreshPanel(this.index2Panel);
        refreshPanel(this.index3Panel);
        refreshPanel(this.marPanel);
        refreshPanel(this.mbrPanel);
        refreshPanel(this.msrPanel);
        refreshPanel(this.irPanel);
        refreshPanel(this.pcPanel);
        refreshPanel(this.ccPanel);
        refreshPanel(this.mfrPanel);
    }

    private void refreshPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTextField txt) {
                String registerName = txt.getName();
                int regVal;

                switch (registerName) {
                    case "MAR":
                        regVal = this.cpuRegisters.getMAR();
                        break;
                    case "MBR":
                        regVal = this.cpuRegisters.getMBR();
                        break;
                    case "IR":
                        regVal = this.cpuRegisters.getIR();
                        break;
                    default:
                        regVal = this.cpuRegisters.getRegistersByName(registerName);
                        break;
                }

                txt.setText(String.valueOf(regVal));
            }
        }
    }

    private void clearAll() {
        for (Component c : Arrays.asList(
                reg0Panel, reg1Panel, reg2Panel, reg3Panel,
                index1Panel, index2Panel, index3Panel,
                marPanel, mbrPanel, msrPanel, irPanel, pcPanel, mfrPanel, ccPanel)) {

            if (c instanceof JPanel) {
                for (Component comp : ((JPanel) c).getComponents()) {
                    if (comp instanceof JTextField) {
                        ((JTextField) comp).setText("");
                    }
                }
            }
        }

        consoleOutput.setText("");
        instructionMap.clear();
        addressList.clear();

        initCPU();
        refreshCacheTable();

        FrontPanel.instructionIndex = 0;
        enableStatus = 0;
        btnSingleStep.setEnabled(false);
        btnRun.setEnabled(false);
        btnInitialProgramLoad.setEnabled(true);
    }

    private void printConsole(final String message) {
        this.consoleOutput.append(message + "\n");
    }

    private void setEnableForPanel(final JPanel panel, final boolean flag) {
        if (panel == null) {
            return;
        }

        for (Component com : panel.getComponents()) {
            if (com instanceof JPanel) {
                setEnableForPanel((JPanel) com, flag);
            } else {
                com.setEnabled(flag);
            }
        }
    }

    private void runInstruction(final String instruction, final Registers registers, final MCU mcu) {
        final String opCode = instruction.substring(0, 6);

        if (!Const.OPCODE.containsKey(opCode)) {
            handleMachineFault(Const.FaultCode.ILL_OPRC.getValue(), Const.FaultCode.ILL_OPRC.getMessage());
            return;
        }

        try {
            String className = "alu.instruction." + Const.OPCODE.get(opCode);
            AbstractInstruction instr = getInstructionInstance(className);
            instr.execute(instruction, registers, mcu);

            pushConsoleBuffer();
            refreshCacheTable();

            System.out.println(instr.getExecuteMessage());

        } catch (ReflectiveOperationException | MachineFaultException e) {
            e.printStackTrace();
            if (e instanceof MachineFaultException) {
                handleMachineFault(((MachineFaultException) e).getFaultCode(), e.getMessage());
            }
        }
    }

    @SuppressWarnings("deprecation")
    private AbstractInstruction getInstructionInstance(String className) throws ReflectiveOperationException {
        return (AbstractInstruction) Class.forName(className).newInstance();
    }

    private void handleMachineFault(final int faultCode, final String message) {
        this.cpuRegisters.setMAR(4);
        this.cpuRegisters.setMBR(this.cpuRegisters.getPC());
        this.memoryControlUnit.storeIntoCache(this.cpuRegisters.getMAR(), this.cpuRegisters.getMBR());

        this.cpuRegisters.setMAR(5);
        this.cpuRegisters.setMBR(this.cpuRegisters.getMSR());
        this.memoryControlUnit.storeIntoCache(this.cpuRegisters.getMAR(), this.cpuRegisters.getMBR());

        this.cpuRegisters.setMFR(faultCode);

        JOptionPane.showMessageDialog(null, message, "Fault Code: " + faultCode, JOptionPane.ERROR_MESSAGE);
        this.cpuRegisters.setPC(this.memoryControlUnit.fetchFromCache(1));
    }

    static void access$41(final FrontPanel frontPanel, final int enableFlag) {
        frontPanel.enableStatus = enableFlag;
    }

    public static void addValue(HashMap<String, Queue<String>> map, String key, String value) {
        if (!map.containsKey(key)) {
            map.put(key, new LinkedList<>());
        }
        map.get(key).add(value);
    }

    public static String binaryToHex(String binary) {
        int val = binaryToDec(binaryStringToBinary(binary));
        return decimalToHex(val);
    }

    public static boolean[] binaryStringToBinary(String s) {
        boolean[] bin = new boolean[s.length()];
        for (int i = 0; i < bin.length; i++) {
            bin[i] = (s.charAt(i) == '1');
        }
        return bin;
    }

    public static String decimalToHex(int val) {
        String hex = Integer.toHexString(val).toUpperCase();
        while (hex.length() < 4) {
            hex = "0" + hex;
        }
        return hex;
    }

    public static int binaryToDec(boolean[] bin) {
        int dec = 0;
        int multiplier = 1;
        for (int i = bin.length - 1; i >= 0; i--) {
            dec += multiplier * (bin[i] ? 1 : 0);
            multiplier *= 2;
        }
        return dec;
    }

    private String octalToBinary(String octalStr) {
        int decimal = Integer.parseInt(octalStr, 8);
        return Integer.toBinaryString(decimal);
    }

    public static String convertBinaryToOctal(String binaryStr) {
        try {
            int decimal = Integer.parseInt(binaryStr, 2);
            return Integer.toOctalString(decimal);
        } catch (NumberFormatException e) {
            System.out.println("Invalid binary input");
            return null;
        }
    }

    public static String toBin(String num, int length) {
        String bin = Integer.toBinaryString(Integer.parseInt(num));
        while (bin.length() < length) {
            bin = "0" + bin;
        }
        return bin;
    }

    private void loadParagraphIntoMemory(String filename, int startAddress) throws IOException {
    StringBuilder sb = new StringBuilder();

    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append(' ');
        }
    }

    String text = sb.toString().trim();

    if (!text.endsWith("#")) {
        text = text + "#";
    }

    int addr = startAddress;
    for (int i = 0; i < text.length(); i++) {
        int ascii = (int) text.charAt(i);
        this.memoryControlUnit.storeIntoCache(addr, ascii);
        addr++;
    }

    this.printConsole("Paragraph loaded at address " + startAddress);
}
}