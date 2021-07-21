package ui;

import model.CodeWriter;
import model.CommandType;
import model.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: <path to vm file>");
            System.exit(-1);
        }

        File vmFile = new File(args[0]);

        String asmFilePath;
        List<String> vmFiles = new ArrayList<>();

        boolean writeInit = false;
        if (vmFile.isFile()) {
            asmFilePath = vmFile.getAbsolutePath().replace(".vm", ".asm");
            vmFiles.add(vmFile.getPath());
        } else if (vmFile.isDirectory()) {
            asmFilePath = vmFile.getAbsolutePath() + "\\" + vmFile.getName() + ".asm";
            vmFiles = getVMFiles(vmFile.getPath());
            writeInit = true;
        } else {
            throw new RuntimeException("I/O Error, please enter a valid file or directory name");
        }

        CodeWriter codeWriter = new CodeWriter(asmFilePath);
        if (writeInit) {
            codeWriter.writeInit();
        }
        for (String vmFilePath : vmFiles) {
            Parser parser = new Parser(vmFilePath);
            codeWriter.setFileName(vmFilePath);

            while (parser.hasMoreCommands()) {
                parser.advance();
                switch (parser.getCurrentCommandType()) {
                    case C_ARITHMETIC:
                        codeWriter.writeArithmetic(parser.getCurrentCommandArg1());
                        break;
                    case C_PUSH:
                        codeWriter.writePushPop(CommandType.C_PUSH, parser.getCurrentCommandArg1(), parser.getCurrentCommandArg2());
                        break;
                    case C_POP:
                        codeWriter.writePushPop(CommandType.C_POP, parser.getCurrentCommandArg1(), parser.getCurrentCommandArg2());
                        break;
                    case C_LABEL:
                        codeWriter.writeLabel(parser.getCurrentCommandArg1());
                        break;
                    case C_GOTO:
                        codeWriter.writeGoto(parser.getCurrentCommandArg1());
                        break;
                    case C_IF:
                        codeWriter.writeIF(parser.getCurrentCommandArg1()); // if first stack pop is true (previous command)
                        break;
                    case C_CALL:
                        codeWriter.writeCall(parser.getCurrentCommandArg1(), parser.getCurrentCommandArg2());
                        break;
                    case C_FUNCTION:
                        codeWriter.writeFunction(parser.getCurrentCommandArg1(), parser.getCurrentCommandArg2());
                        break;
                    case C_RETURN:
                        codeWriter.writeReturn();
                        break;
                    default:
                        throw new RuntimeException("Got wrong command type");
                }
            }
        }
        codeWriter.close();
    }

    private static List<String> getVMFiles(String path) {
        File dir = new File(path);
        return Arrays.stream(Objects.requireNonNull(dir.list()))
                .filter(f -> f.endsWith(".vm")).map(f -> dir.getAbsolutePath() + "\\" + f).collect(Collectors.toList());
    }
}