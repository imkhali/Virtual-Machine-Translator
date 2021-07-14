package Model;

import java.util.List;

public class CodeWriter {
    private String asmFile;
    private List<String> bufferCommands;

    public CodeWriter(String asmFile) {
        this.asmFile = asmFile;
    }

    public String getAsmFile() {
        return asmFile;
    }

    // EFFECTS: write to asmFile the assembly code that implements the given Arithmetic/Logical command
    public void writeArithmetic(String command) {;}

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

    private void writePop(String segment, int index) {
        switch (segment) {
            case "constant":
                writePushConstant(index);
                break;

        }
    }

    private void writePush(String segment, int index) {
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

}
