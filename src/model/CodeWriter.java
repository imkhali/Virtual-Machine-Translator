package model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.getProperty;

public class CodeWriter {
    public static final String LINE_SEPARATOR = getProperty("line.separator");

    public static final int THIS = 3;
    public static final int TEMP = 5;

    public static int currentLabelIndex = 0;

    private final String asmFilePath;

    private String currentVMFilePath;
    private String fileBaseName;
    private String labelPrefix = "";

    private final List<String> bufferCommands;

    public CodeWriter(String asmFile) {
        this.asmFilePath = asmFile;
        this.bufferCommands = new LinkedList<>();
    }

    public String getAsmFilePath() {
        return asmFilePath;
    }

    // the current vm File we are working on
    public void setFileName(String currentVMFilePath) {
        this.currentVMFilePath = currentVMFilePath;
        this.fileBaseName = extractBaseName();
    }

    private String extractBaseName() {
        return currentVMFilePath.replaceAll("^.*[/\\\\]", "").split("\\.")[0];
    }

    private void writeToAsmFile() {
        try {
            FileWriter asmFile = new FileWriter(this.asmFilePath);
            for (String asmStatement : this.bufferCommands)
                asmFile.write(asmStatement + LINE_SEPARATOR);
            asmFile.close();
            System.out.println("Successfully wrote to the file. " + asmFilePath);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public void close() {
        writeToAsmFile();
    }

    // write the assembly instructions for bootstrap code
    public void writeInit() {
        /* *SP = 256 */
        bufferCommands.add("@256");
        bufferCommands.add("D=A");
        bufferCommands.add("@SP");
        bufferCommands.add("M=D");

        /* call Sys.init */
        writeCall("Sys.init", 0);
    }

    // EFFECTS: write to asmFile the assembly code that implements the given push/pop command
    public void writePushPop(CommandType commandType, String segment, int index) {
        switch (commandType) {
            case C_PUSH -> {
                bufferCommands.add("// push " + segment + " " + index);
                writePush(segment, index);
            }
            case C_POP -> {
                bufferCommands.add("// pop " + segment + " " + index);
                writePop(segment, index);
            }
            default -> throw new RuntimeException("Got wrong command: expected push or pop");
        }
    }

    private void writePush(String segment, int index) {
        switch (segment) {
            case "constant" -> writePushConstant(index);
            case "local" -> writePushSegment("LCL", index);
            case "argument" -> writePushSegment("ARG", index);
            case "this" -> writePushSegment("THIS", index);
            case "that" -> writePushSegment("THAT", index);
            case "static" -> writePushOtherSegment(fileBaseName + "." + index);
            case "temp" -> writePushOtherSegment("R" + (index + TEMP));
            case "pointer" -> writePushOtherSegment("R" + (index + THIS));
            default -> throw new RuntimeException("Got wrong command: expected push or pop");
        }
    }

    private void writePushConstant(int index) {
        bufferCommands.add("@" + index);
        bufferCommands.add("D=A");
        pushFromD();
    }

    private void pushFromD() {
        bufferCommands.add("@SP");
        bufferCommands.add("M=M+1");
        bufferCommands.add("A=M-1");
        bufferCommands.add("M=D");
    }

    private void writePushSegment(String segmentBaseAddress, int index) {
        bufferCommands.add("@" + segmentBaseAddress);
        bufferCommands.add("D=M");
        bufferCommands.add("@" + index);
        bufferCommands.add("A=D+A");
        bufferCommands.add("D=M");
        pushFromD();
    }

    private void writePushOtherSegment(String address) {
        bufferCommands.add("@" + address);
        bufferCommands.add("D=M");
        pushFromD();
    }

    private void writePop(String segment, int index) {
        switch (segment) {
            case "local" -> writePopSegment("LCL", index);
            case "argument" -> writePopSegment("ARG", index);
            case "this" -> writePopSegment("THIS", index);
            case "that" -> writePopSegment("THAT", index);
            case "static" -> writePopOtherSegment(fileBaseName + "." + index);
            case "temp" -> writePopOtherSegment("R" + (index + TEMP));
            case "pointer" -> writePopOtherSegment("R" + (index + THIS));
            default -> throw new RuntimeException("Got wrong command: expected push or pop");
        }
    }

    private void writePopSegment(String segmentBaseAddress, int index) {
        bufferCommands.add("@" + segmentBaseAddress);
        bufferCommands.add("D=M");
        bufferCommands.add("@" + index);
        bufferCommands.add("D=D+A");
        bufferCommands.add("@R13");
        bufferCommands.add("M=D");
        popToD();
        bufferCommands.add("@R13");
        bufferCommands.add("A=M");
        bufferCommands.add("M=D");
    }

    private void writePopOtherSegment(String address) {
        popToD();
        bufferCommands.add("@" + address);
        bufferCommands.add("M=D");
    }

    private void popToD() {
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
    }

    // EFFECTS: write to asmFile the assembly code that implements the given Arithmetic/Logical command
    public void writeArithmetic(String command) {
        bufferCommands.add("// " + command);
        switch (command) {
            case "add" -> sAdd();
            case "sub" -> sSub();
            case "neg" -> sNeg();
            case "and" -> sAnd();
            case "or" -> sOr();
            case "not" -> sNot();
            case "gt" -> sGt();
            case "lt" -> sLt();
            case "eq" -> sEq();
            default -> throw new RuntimeException("Got wrong arithmetic function");
        }
    }

    private void sAnd() {
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("A=A-1");
        bufferCommands.add("M=D&M");
    }

    private void sOr() {
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=!M");
        bufferCommands.add("A=A-1");
        bufferCommands.add("M=!M");
        bufferCommands.add("M=D&M");
        bufferCommands.add("M=!M");
    }

    private void sNot() {
        bufferCommands.add("@SP");
        bufferCommands.add("A=M-1");
        bufferCommands.add("M=!M");
    }

    private void sAdd() {
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("A=A-1");
        bufferCommands.add("M=D+M");
    }

    private void sSub() {
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("A=A-1");
        bufferCommands.add("M=M-D");
    }

    private void sNeg() {
        bufferCommands.add("@SP");
        bufferCommands.add("A=M-1");
        bufferCommands.add("M=-M");
    }

    private void sEq() {
        jumpIfCondition("JEQ");
    }

    private void sLt() {
        jumpIfCondition("JLT");
    }

    private void sGt() {
        jumpIfCondition("JGT");
    }

    private void jumpIfCondition(String jump) {
        String firstLabel = getNextLabel();
        String secondLabel = getNextLabel();
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("A=A-1");
        bufferCommands.add("D=M-D");
        bufferCommands.add("@" + firstLabel);
        bufferCommands.add("D;" + jump);
        pushFromDTrueOrFalse(firstLabel, secondLabel);
    }

    private void pushFromDTrueOrFalse(String firstLabel, String secondLabel) {
        bufferCommands.add("@SP");                 // branch false
        bufferCommands.add("A=M-1");
        bufferCommands.add("M=0");
        bufferCommands.add("@" + secondLabel);
        bufferCommands.add("0;JMP");
        bufferCommands.add("(" + firstLabel + ")"); // branch true
        bufferCommands.add("@SP");
        bufferCommands.add("A=M-1");
        bufferCommands.add("M=-1");
        bufferCommands.add("(" + secondLabel + ")");
    }

    private String getNextLabel() {
        return labelPrefix + "Label" + (currentLabelIndex++);
    }

    public void writeGoto(String label) {
        bufferCommands.add("@" + label);
        bufferCommands.add("0;JMP");
    }

    public void writeLabel(String label) {
        bufferCommands.add("// label " + label); // TODO: same as above
        bufferCommands.add("(" + label + ")");
    }

    public void writeIF(String label) {
        bufferCommands.add("// if-goto " + label); // TODO: same as above
        popToD();
        bufferCommands.add("@" + label);
        bufferCommands.add("D;JNE");
    }

    public void writeCall(String functionName, Integer nArgs) {

        bufferCommands.add("// call " + functionName);

        /**
         * Pushing the caller state
         */
        /* push returnAddress */
        String returnAddress = getNextLabel();
        bufferCommands.add("@" + returnAddress);
        bufferCommands.add("D=A");
        pushFromD();

        /* save segment address */
        saveSegmentAddress("LCL");
        saveSegmentAddress("ARG");
        saveSegmentAddress("THIS");
        saveSegmentAddress("THAT");

        /**
         * Setting up for the callee
         */
        /* reposition ARG */
        bufferCommands.add("@SP");
        bufferCommands.add("D=M");
        bufferCommands.add("@" + (5 + nArgs));
        bufferCommands.add("D=D-A");
        bufferCommands.add("@ARG");
        bufferCommands.add("M=D");

        /* reposition LCL */
        bufferCommands.add("@SP");
        bufferCommands.add("D=M");
        bufferCommands.add("@LCL");
        bufferCommands.add("M=D");

        /**
         * jumping to functionName
         */
        /* goto functionName */
        writeGoto(functionName);

        /* Declares a label for the return address */
        bufferCommands.add("(" + returnAddress + ")");
    }

    private void saveSegmentAddress(String segmentBaseAddress) {
        bufferCommands.add("@" + segmentBaseAddress);
        bufferCommands.add("D=M");
        pushFromD();
    }

    public void writeFunction(String functionName, Integer nVars) {
        labelPrefix = functionName + "$";
        bufferCommands.add("// function " + functionName + " " + nVars);

        /* Entry point for the function */
        bufferCommands.add("(" + functionName + ")");

        /* Initialize local variables as zeros */
        bufferCommands.add("D=0");
        for (int i = 0; i < nVars; i++) {
            pushFromD();
        }
    }

    public void writeReturn() {
        bufferCommands.add("// return");

        /* R13 = LCL */
        bufferCommands.add("@LCL");
        bufferCommands.add("D=M");
        bufferCommands.add("@R13");
        bufferCommands.add("M=D");

        /* R14 = *(endFrame - 5)  - caller state is 4 segments + retAddress */
        bufferCommands.add("@5");
        bufferCommands.add("A=D-A");
        bufferCommands.add("D=M");
        bufferCommands.add("@R14");
        bufferCommands.add("M=D");

        /* *ARG = pop()  - argument 0 is replaced with return value */
        popToD();
        bufferCommands.add("@ARG");
        bufferCommands.add("A=M");
        bufferCommands.add("M=D");

        /**
         * reposition the caller's segments (state)
         */
        /* SP = ARG + 1 */
        bufferCommands.add("@ARG");
        bufferCommands.add("D=M+1");
        bufferCommands.add("@SP");
        bufferCommands.add("M=D");

        /* THAT = *(endFrame - 1) */
        bufferCommands.add("@R13");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("@THAT");
        bufferCommands.add("M=D");

        /* THIS = *(endFrame - 2) */
        bufferCommands.add("@R13");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("@THIS");
        bufferCommands.add("M=D");

        /* ARG = *(endFrame - 3) */
        bufferCommands.add("@R13");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("@ARG");
        bufferCommands.add("M=D");

        /* LCL = *(endFrame - 4) */
        bufferCommands.add("@R13");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("@LCL");
        bufferCommands.add("M=D");

        /* goto retAddress */
        bufferCommands.add("@R14");
        bufferCommands.add("A=M");
        bufferCommands.add("0;JMP");
        this.labelPrefix = "";
    }
}
