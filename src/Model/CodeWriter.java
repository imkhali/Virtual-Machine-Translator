package Model;

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
    private final List<String> bufferCommands;

    public CodeWriter(String asmFile) {
        this.asmFilePath = asmFile;
        this.bufferCommands = new LinkedList<>();
    }

    public String getAsmFilePath() {
        return asmFilePath;
    }

    // EFFECTS: write to asmFile the assembly code that implements the given push/pop command
    public void writePushPop(CommandType commandType, String segment, int index) {
        switch (commandType) {
            case C_PUSH:
                bufferCommands.add("// push " + segment + " " + index);
                writePush(segment, index);
                break;
            case C_POP:
                bufferCommands.add("// pop " + segment + " " + index);
                writePop(segment, index);
                break;
            default:
                throw new RuntimeException("Got wrong command: expected push or pop");
        }
    }

    private void writePush(String segment, int index) {
        switch (segment) {
            case "constant":
                writePushConstant(index);
                break;
            case "local":
                writePushSegment("@LCL", index);
                break;
            case "argument":
                writePushSegment("@ARG", index);
                break;
            case "this":
                writePushSegment("@THIS", index);
                break;
            case "that":
                writePushSegment("@THAT", index);
                break;
            case "static":
                writePushOtherSegment("@Foo." + index);
                break;
            case "temp":
                writePushOtherSegment("@R" + (index + TEMP));
                break;
            case "pointer":
                writePushOtherSegment("@R" + (index + THIS));
                break;
            default:
                throw new RuntimeException("Got wrong command: expected push or pop");
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
        bufferCommands.add(address);
        bufferCommands.add("D=M");
        pushFromD();
    }

    private void writePop(String segment, int index) {
        switch (segment) {
            case "local":
                writePopSegment("@LCL", index);
                break;
            case "argument":
                writePopSegment("@ARG", index);
                break;
            case "this":
                writePopSegment("@THIS", index);
                break;
            case "that":
                writePopSegment("@THAT", index);
                break;
            case "static":
                writePopOtherSegment("@Foo." + index);
                break;
            case "temp":
                writePopOtherSegment("@R" + (index + TEMP));
                break;
            case "pointer":
                writePopOtherSegment("@R" + (index + THIS));
                break;
            default:
                throw new RuntimeException("Got wrong command: expected push or pop");
        }
    }

    private void writePopSegment(String segmentBaseAddress, int index) {
        bufferCommands.add("@" + segmentBaseAddress);
        bufferCommands.add("D=M");
        bufferCommands.add("@" + index);
        bufferCommands.add("D=D+A");
        bufferCommands.add("@tmp");
        bufferCommands.add("M=D");
        popToD();
        bufferCommands.add("@tmp");
        bufferCommands.add("A=M");
        bufferCommands.add("M=D");
    }

    private void writePopOtherSegment(String address) {
        popToD();
        bufferCommands.add(address);
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
            case "add":
                sAdd();
                break;
            case "sub":
                sSub();
                break;
            case "neg":
                sNeg();
                break;
            case "and":
                sAnd();
                break;
            case "or":
                sOr();
                break;
            case "not":
                sNot();
                break;
            case "gt":
                sGt();
                break;
            case "lt":
                sLt();
                break;
            case "eq":
                sEq();
                break;
            default:
                throw new RuntimeException("Got wrong arithmetic function");

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
        bufferCommands.add("M=D-M");
    }

    private void sNeg() {
        bufferCommands.add("@SP");
        bufferCommands.add("A=M-1");
        bufferCommands.add("M=-M");
    }

    private void sEq() {
        String firstLabel = getNextLabel();
        String secondLabel = getNextLabel();
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("A=A-1");
        bufferCommands.add("D=M-D");
        bufferCommands.add("@" + firstLabel);
        bufferCommands.add("D;JEQ");
        pushFromDTrueOrFalse(firstLabel, secondLabel);
    }

    private void sLt() {
        String firstLabel = getNextLabel();
        String secondLabel = getNextLabel();
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("A=A-1");
        bufferCommands.add("D=M-D");
        bufferCommands.add("@" + firstLabel);
        bufferCommands.add("D;JLT");
        pushFromDTrueOrFalse(firstLabel, secondLabel);
    }

    private void sGt() {
        String firstLabel = getNextLabel();
        String secondLabel = getNextLabel();
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("A=A-1");
        bufferCommands.add("D=M-D");
        bufferCommands.add("@" + firstLabel);
        bufferCommands.add("D;JGT");
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
        return "Label" + (currentLabelIndex++);
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
}
