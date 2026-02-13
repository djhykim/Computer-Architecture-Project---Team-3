package front;

// Standard imports
import java.io.*;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.awt.Color;
import util.Const;
import java.util.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.BiConsumer;

// Custom imports from the rest of the project
import cpu.Registers;
import util.StringUtil;
import memory.Test;
import alu.instruction.AbstractInstruction;
import util.MachineFaultException;
import memory.MCU;

public class FrontPanel
{
    private int enableStatus;
    JFrame mainFrame;
    private JPanel regPanel, indexRegPanel, statusRegPanel, generalRegPanel, reg0Panel, reg1Panel, reg2Panel, reg3Panel, index1Panel, index2Panel, index3Panel;
    private JLabel labelR, labelR1, labelR2, labelR3, labelX, labelX1, labelX2;
    int programCounter;
    private JTextField textFieldReg0, textFieldReg1, textFieldReg2, textFieldReg3, textFieldIndex1, textFieldIndex2, textFieldIndex3;
    private JButton btnLoadReg0, btnLoadReg1, btnLoadReg2, btnLoadReg3, btnLoadIndex1, btnLoadIndex2, btnLoadIndex3;
    private JPanel marPanel, mbrPanel, msrPanel, irPanel, pcPanel, ccPanel, mfrPanel;
    private JLabel labelMAR, labelMBR, labelMSR, labelIR, labelPC, labelCC, labelMFR;
    private JTextField textFieldMAR, textFieldMBR, textFieldMSR, textFieldIR, textFieldPC, textFieldCC, textFieldMFR;
    private JButton btnLoadMAR, btnLoadMBR, btnLoadMSR, btnLoadIR, btnLoadPC, btnLoadCC, btnLoadMFR;
    private JButton btnSingleStep, btnInitialProgramLoad, btnReset, btnHalt, btnStore, btnRun;

    private Registers cpuRegisters;
    private MCU memoryControlUnit;
    private JTextArea consoleOutput;
    private JScrollPane scrollPaneOutput;

    private JPanel addressPanel, valuePanel, buttonPanel, printerPanel, cachePanel, operationPanel, programPanel1, programPanel2;
    private JLabel labelPrinter, labelCache, generalPurposeReg, indexReg, addressLabel;
    private JTextArea consoleInput;
    private JTable cacheTable;
    private JScrollPane scrollPaneCache;
    private JPanel keyboardPanel;
    private JButton btnRunSingleStep;
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

        (this.btnInitialProgramLoad = new JButton("IPL")).setBounds(308, 650, 75, 48);
        this.btnInitialProgramLoad.setBackground(Color.BLACK);

        (this.btnSingleStep = new JButton("SS")).setBounds(208, 650, 75, 48);
        this.btnSingleStep.setBackground(Color.BLACK);

        layoutPanels();
        addButtons();
        addCache();
        addTabbedPane();
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

    private void configureFrame() {
        mainFrame.setTitle("TEAM 5 - CSCI 6461 COMPUTER SIMULATOR");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void layoutPanels() {
        mainFrame.getContentPane().setLayout(new BorderLayout());

        // Create a left panel for registers
        JPanel leftPanel = createPanel(BACKGROUND_COLOR);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        // Add all register panels to the left panel
        leftPanel.add(createSectionPanel("GENERAL PURPOSE REGISTERS", reg0Panel, reg1Panel, reg2Panel, reg3Panel));
        leftPanel.add(createSectionPanel("INDEX REGISTERS", index1Panel, index2Panel, index3Panel));
        leftPanel.add(createSectionPanel("ADDRESS", marPanel, mbrPanel, mfrPanel, ccPanel));
        leftPanel.add(createSectionPanel("OTHER REGISTERS", msrPanel, irPanel, pcPanel));

        // Create a scroll pane for the left panel
        JScrollPane leftScrollPane = new JScrollPane(leftPanel);

        // Create a console panel
        JPanel consolePanel = new JPanel(new BorderLayout());
        this.consoleOutput = new JTextArea();
        this.consoleOutput.setEditable(true);
        JScrollPane scrollPaneConsole = new JScrollPane(this.consoleOutput);
        consolePanel.add(scrollPaneConsole, BorderLayout.CENTER);

        // Add the left panel (registers) to the left side and the console panel to the right side
        mainFrame.getContentPane().add(leftScrollPane, BorderLayout.WEST); // Registers on the left
        mainFrame.getContentPane().add(consolePanel, BorderLayout.EAST); // Console on the right
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

    private void addTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel consolePanel = new JPanel(new BorderLayout());
        this.consoleOutput = new JTextArea();
        this.consoleOutput.setEditable(true);
        JScrollPane scrollPaneConsole = new JScrollPane(this.consoleOutput);
        consolePanel.add(scrollPaneConsole, BorderLayout.CENTER);

        consoleOutput.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            int caretPosition = consoleOutput.getCaretPosition();
                            int lineOffset = consoleOutput.getLineStartOffset(consoleOutput.getLineOfOffset(caretPosition));
                            if (lineOffset == caretPosition && (caretPosition == 0 || consoleOutput.getText().charAt(caretPosition - 1) == '\n')) {
                                consoleOutput.getDocument().insertString(caretPosition, "> ", null);
                            }
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });

        tabbedPane.addTab("Console", null, consolePanel, "Console Output/Input");

        mainFrame.add(tabbedPane, BorderLayout.CENTER);
    }

    private void addButtons() {
        JPanel buttonPanel = createPanel(BACKGROUND_COLOR);
        buttonPanel.setLayout(new FlowLayout());

        this.btnInitialProgramLoad = createButton("IPL", BUTTON_COLOR);
        this.btnRun = createButton("Run", BUTTON_COLOR);
        this.btnSingleStep = createButton("SS", BUTTON_COLOR);
        this.btnReset = createButton("Reset", BUTTON_COLOR);
        this.btnHalt = createButton("Halt", BUTTON_COLOR);
        this.btnStore = createButton("Store", BUTTON_COLOR);

        buttonPanel.add(this.btnInitialProgramLoad);
        buttonPanel.add(this.btnRun);
        buttonPanel.add(this.btnSingleStep);
        buttonPanel.add(this.btnReset);
        buttonPanel.add(this.btnHalt);
        buttonPanel.add(this.btnStore);

        mainFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        this.btnSingleStep.setEnabled(false);
        this.btnRun.setEnabled(false);
        // Add mouse listener for the Store button
        this.btnStore.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Store value from the currently selected text field into the corresponding register
                if (currentTextField != null) {
                    String valueStr = currentTextField.getText(); // Get value from the selected text field
                    try {
                        int value = Integer.parseInt(valueStr, 8); // Convert from octal to decimal
                        switch (currentTextField.getName()) {
                            case "R0":
                                cpuRegisters.setR0(value);
                                break;
                            case "R1":
                                cpuRegisters.setR1(value);
                                break;
                            case "R2":
                                cpuRegisters.setR2(value);
                                break;
                            case "R3":
                                cpuRegisters.setR3(value);
                                break;
                            case "X1":
                                cpuRegisters.setX1(value);
                                break;
                            case "X2":
                                cpuRegisters.setX2(value);
                                break;
                            case "X3":
                                cpuRegisters.setX3(value);
                                break;
                            case "MAR":
                                cpuRegisters.setMAR(value);
                                break;
                            case "MBR":
                                cpuRegisters.setMBR(value);
                                break;
                            case "MSR":
                                cpuRegisters.setMSR(value);
                                break;
                            case "IR":
                                cpuRegisters.setIR(value);
                                break;
                            case "PC":
                                cpuRegisters.setPC(value);
                                break;
                            case "CC":
                                cpuRegisters.setCC(value);
                                break;
                            case "MFR":
                                cpuRegisters.setMFR(value);
                                break;
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

    }


    private void addCache() {
        (this.cachePanel = new JPanel()).setBounds(808, 418, 254, 147);
        this.labelCache = new JLabel("Cache");

        this.labelCache.setForeground(new Color(204, 255, 204));

        this.scrollPaneCache = new JScrollPane();
        (this.cacheTable = new JTable(16, 2)).setEnabled(false);

        this.scrollPaneCache.setViewportView(this.cacheTable);
        this.cacheTable.setModel(new DefaultTableModel(
                new Object[][]{new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2], new Object[2]},
                new String[]{"Tag", "Data"}));
        this.cachePanel.setLayout(new BoxLayout(this.cachePanel, BoxLayout.Y_AXIS));
        this.cachePanel.add(this.labelCache);
        this.cachePanel.add(this.scrollPaneCache);
        this.cachePanel.setBackground(new Color(60, 80, 65));
    }

    private JButton createButton(String label, Color bgColor) {
        JButton button = new JButton(label);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(BUTTON_DIMENSION);
        return button;
    }

    public FrontPanel() {
        initComponents(); // Initialize GUI components.
        addListeners(); // Attach listeners to the components.
        this.programCounter = 0;

        instructionMap = new HashMap<>();
        addressList = new ArrayList<>();

        this.inputQueueMap = new HashMap<>();

        initCPU(); // Initialize CPU-related objects and structures.

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

    private void initCPU() {
        this.cpuRegisters = new Registers(); // Initialize the registers.
        this.memoryControlUnit = new MCU(); // Initialize the memory control unit.
        this.cpuRegisters.setPC(addressList.isEmpty() ? 10 : addressList.get(0));
        this.programCounter = 0;
    }

    private MouseAdapter createRegisterMouseListener(JTextField textField, BiConsumer<Registers, Integer> registerSetter) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentTextField = textField;
                try {
                    String valueStr = textField.getText();
                    int value = Integer.parseInt(valueStr, 8); // Try parsing as octal
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
        this.btnSingleStep.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (FrontPanel.this.programCounter < FrontPanel.instructionMap.size()) {
                    final int value = Integer.parseInt(FrontPanel.instructionMap.get(FrontPanel.addressList.get(FrontPanel.this.programCounter)), 2);
                    FrontPanel.this.memoryControlUnit.storeIntoCache(FrontPanel.this.cpuRegisters.getPC(), value);
                    FrontPanel.this.cpuRegisters.setMAR(Integer.parseInt(Integer.toOctalString(FrontPanel.this.cpuRegisters.getPC()), 8));
                    FrontPanel.this.cpuRegisters.setMBR(Integer.parseInt(Integer.toOctalString(FrontPanel.this.memoryControlUnit.fetchFromCache(FrontPanel.this.cpuRegisters.getMAR()))));
                    FrontPanel.this.cpuRegisters.setIR(FrontPanel.this.cpuRegisters.getMBR());
                    final String ins = FrontPanel.this.cpuRegisters.getBinaryStringIr();
                    FrontPanel.this.runInstruction(ins, FrontPanel.this.cpuRegisters, FrontPanel.this.memoryControlUnit);
                    FrontPanel.this.printConsole("PC: " + FrontPanel.this.cpuRegisters.getPC() + ", instruction: " + ins);
                    FrontPanel.this.refreshRegistersPanel();
                    final FrontPanel this$0 = FrontPanel.this;
                    ++this$0.programCounter;
                } else {
                    System.out.println("End of program");
                }
            }
        });

        this.btnRun.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                while (FrontPanel.this.programCounter < FrontPanel.instructionMap.size()) {
                    final int value = Integer.parseInt(FrontPanel.instructionMap.get(FrontPanel.addressList.get(FrontPanel.this.programCounter)), 2);
                    FrontPanel.this.memoryControlUnit.storeIntoCache(FrontPanel.this.cpuRegisters.getPC(), value);
                    FrontPanel.this.cpuRegisters.setMAR(Integer.parseInt(Integer.toOctalString(FrontPanel.this.cpuRegisters.getPC()), 8));
                    FrontPanel.this.cpuRegisters.setMBR(Integer.parseInt(Integer.toOctalString(FrontPanel.this.memoryControlUnit.fetchFromCache(FrontPanel.this.cpuRegisters.getMAR()))));
                    FrontPanel.this.cpuRegisters.setIR(FrontPanel.this.cpuRegisters.getMBR());
                    final String ins = FrontPanel.this.cpuRegisters.getBinaryStringIr();
                    FrontPanel.this.runInstruction(ins, FrontPanel.this.cpuRegisters, FrontPanel.this.memoryControlUnit);
                    FrontPanel.this.printConsole("PC: " + FrontPanel.this.cpuRegisters.getPC() + ", instruction: " + ins);
                    FrontPanel.this.refreshRegistersPanel();
                    final FrontPanel this$0 = FrontPanel.this;
                    ++this$0.programCounter;
                }
            }
        });

        this.btnReset.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                clearAll(); // Method to clear all fields and output
            }
        });

        this.btnInitialProgramLoad.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (!FrontPanel.this.btnInitialProgramLoad.isEnabled()) {
                    return; // Exit the method if the button is disabled
                }

                FrontPanel.this.initCPU();
                try {
                    FrontPanel.this.memoryControlUnit.loadProgramFile("input.txt");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                FrontPanel.this.btnInitialProgramLoad.setEnabled(false);

                if (FrontPanel.this.enableStatus == 0) {
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.regPanel, true);
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.statusRegPanel, true);
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.generalRegPanel, true);
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.indexRegPanel, true);
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.programPanel1, true);
                    FrontPanel.this.setEnableForPanel(FrontPanel.this.programPanel2, true);
                    FrontPanel.this.btnSingleStep.setEnabled(true);
                    FrontPanel.this.btnRun.setEnabled(true);
                    FrontPanel.access$41(FrontPanel.this, 1);
                }

                FrontPanel.this.cpuRegisters.setPC(addressList.get(instructionIndex));
                FrontPanel.this.refreshRegistersPanel();
                FrontPanel.this.printConsole("IPL complete!");
            }
        });
    }

    private void updateTextBoxes() {
        String[] values = {
                Integer.toString(cpuRegisters.getR0()),
                Integer.toString(cpuRegisters.getR1()),
                Integer.toString(cpuRegisters.getR2()),
                Integer.toString(cpuRegisters.getR3()),
                Integer.toString(cpuRegisters.getX1()),
                Integer.toString(cpuRegisters.getX2()),
                Integer.toString(cpuRegisters.getX3()),
                Integer.toString(cpuRegisters.getMAR()),
                Integer.toString(cpuRegisters.getMBR()),
                Integer.toString(cpuRegisters.getMSR()),
                Integer.toString(cpuRegisters.getIR()),
                Integer.toString(cpuRegisters.getPC()),
                Integer.toString(cpuRegisters.getMFR()),
                Integer.toString(cpuRegisters.getCC())
        };
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
        if (this.memoryControlUnit.getPrinterBuffer() != null) {
            this.consoleOutput.append(this.memoryControlUnit.getPrinterBuffer());
            this.memoryControlUnit.setPrinterBuffer("");
        }
        if (this.memoryControlUnit.getKeyboardBuffer() != null) {
            this.consoleInput.setText(this.memoryControlUnit.getKeyboardBuffer());
        }
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
                int regVal = 0;

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
        for (Component c : Arrays.asList(reg0Panel, reg1Panel, reg2Panel, reg3Panel, index1Panel, index2Panel, index3Panel, marPanel, mbrPanel, msrPanel, irPanel, pcPanel, mfrPanel, ccPanel)) {
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
            System.out.println("PC: " + registers.getPC() + ", instruction: " + instruction);

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
}