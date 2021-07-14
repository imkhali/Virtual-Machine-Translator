package Model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.getProperty;

public class CodeWriter {
    private String asmFilePath;
    private List<String> bufferCommands;

    public CodeWriter(String asmFile) {
        this.asmFilePath = asmFile;
        this.bufferCommands = new LinkedList<>();
    }

    public String getAsmFilePath() {
        return asmFilePath;
    }

    // EFFECTS: write to asmFile the assembly code that implements the given Arithmetic/Logical command
    public void writeArithmetic(String command) {
        bufferCommands.add("dummy for now" + getProperty("line.separator"));
    }

    private void add(Integer arg1, Integer arg2) {

    }

    // EFFECTS: write to asmFile the assembly code that implements the given push/pop command
    public void writePushPop(CommandType commandType, String segment, int index) {
        switch (commandType) {
            case C_PUSH:
                writePush(segment, index);
                break;
            case C_POP:
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
            case "argument":
            case "this":
            case "that":
                writePushLocal(segment, index);
                break;
            case "static":
                writePushStatic(index);
                break;
            case "temp":
                writePushTemp(index);
                break;
            case "pointer":
                writePushPointer(index);
                break;
            default:
                throw new RuntimeException("Got wrong command: expected push or pop");
        }
    }

    private void writePop(String segment, int index) {
        switch (segment) {
            case "local":
            case "argument":
            case "this":
            case "that":
                writePopLocal(segment, index);
                break;
            case "static":
                writePopStatic(index);
                break;
            case "temp":
                writePopTemp(index);
                break;
            case "pointer":
                writePopPointer(index);
                break;
            default:
                throw new RuntimeException("Got wrong command: expected push or pop");
        }
    }

    private void writePushConstant(int index) {
        bufferCommands.add("@" + index);
        bufferCommands.add("D=A");
        bufferCommands.add("@SP");
        bufferCommands.add("A=M");
        bufferCommands.add("M=D");
        bufferCommands.add("@SP");
        bufferCommands.add("M=M+1");
    }

    private void writePushLocal(String segment,int index) {
        Map<String, String> constants = Stream.of(new String[][] {
                {"local", "LCL"},
                {"argument", "ARG"},
                {"this","THIS"},
                {"that","THAT"},
        }).collect(Collectors.toMap(data -> data[0], data->data[1]));
        bufferCommands.add("@" + constants.get(segment));
        bufferCommands.add("D=A");
        bufferCommands.add("@" + index);
        bufferCommands.add("A=D+A");
        bufferCommands.add("A=M");
        bufferCommands.add("D=M");
        bufferCommands.add("@SP");
        bufferCommands.add("A=M");
        bufferCommands.add("M=D");
        bufferCommands.add("@SP");
        bufferCommands.add("M=M+1");
    }

    private void writePopLocal(String segment, int index) {
        Map<String, String> constants = Stream.of(new String[][] {
                {"local", "LCL"},
                {"argument", "ARG"},
                {"this","THIS"},
                {"that","THAT"},
        }).collect(Collectors.toMap(data -> data[0], data->data[1]));
        bufferCommands.add("@" + constants.get(segment));
        bufferCommands.add("D=A");
        bufferCommands.add("@" + index);
        bufferCommands.add("D=D+A");
        bufferCommands.add("@tmp");
        bufferCommands.add("M=D");
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("@tmp");
        bufferCommands.add("M=D");
    }

    private void writePushStatic(int index) {
        bufferCommands.add("@Foo." + index);
        bufferCommands.add("D=M");
        bufferCommands.add("@SP");
        bufferCommands.add("A=M");
        bufferCommands.add("M=D");
        bufferCommands.add("@SP");
        bufferCommands.add("M=M+1");
    }

    private void writePopStatic(int index) {
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("@Foo." + index);
        bufferCommands.add("M=D");
    }

    private void writePushTemp(int index) {
        bufferCommands.add("@" + index);
        bufferCommands.add("D=A");
        bufferCommands.add("@R5");
        bufferCommands.add("A=D+A");
        bufferCommands.add("D=M");
        bufferCommands.add("@SP");
        bufferCommands.add("A=M");
        bufferCommands.add("M=D");
        bufferCommands.add("@SP");
        bufferCommands.add("M=M+1");
    }

    private void writePopTemp(int index) {
        bufferCommands.add("@" + index);
        bufferCommands.add("D=A");
        bufferCommands.add("@R5");
        bufferCommands.add("D=D+A");
        bufferCommands.add("@tmp");
        bufferCommands.add("M=D");
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        bufferCommands.add("@tmp");
        bufferCommands.add("M=D");
    }

    private void writePushPointer(int index) {
        if (index == 0) {
            bufferCommands.add("@THIS");
        } else if (index == 1) {
            bufferCommands.add("@THAT");
        } else {
            throw new RuntimeException("Expected 0 or 1 only, got: " + index);
        }
        bufferCommands.add("D=M");
        bufferCommands.add("@SP");
        bufferCommands.add("A=M");
        bufferCommands.add("M=D");
        bufferCommands.add("@SP");
        bufferCommands.add("M=M+1");
    }

    private void writePopPointer(int index) {
        bufferCommands.add("@SP");
        bufferCommands.add("AM=M-1");
        bufferCommands.add("D=M");
        if (index == 0) {
            bufferCommands.add("@THIS");
        } else if (index == 1) {
            bufferCommands.add("@THAT");
        } else {
            throw new RuntimeException("Expected 0 or 1 only, got: " + index);
        }
        bufferCommands.add("M=D");
    }

    private void writeToAsmFile() {
        try {
            FileWriter asmFile = new FileWriter(this.asmFilePath);
            for (String asmStatement: this.bufferCommands)
                asmFile.write(asmStatement + getProperty("line.separator"));
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
