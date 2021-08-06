package ui;

import model.CodeWriter;
import model.CommandType;
import model.Parser;

import java.io.File;
import java.util.Objects;

public class Main {
    private static Parser parser;
    private static CodeWriter codeWriter;

    public static final String inFileExt = ".vm";
    public static final String outFileExt = ".asm";
    private static final String fileSeparator = System.getProperty("file.separator");

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: <path to vm file>");
            System.exit(-1);
        }

        File vmFile = new File(args[0]);
        String asmFilePath;
        if (vmFile.isFile()) {
            asmFilePath = vmFile.getAbsolutePath().replace(inFileExt, outFileExt);
            codeWriter = new CodeWriter(asmFilePath);
            handleFile(vmFile.getPath());
        } else if (vmFile.isDirectory()) {
            asmFilePath = vmFile.getAbsolutePath() + fileSeparator + vmFile.getName() + outFileExt;
            codeWriter = new CodeWriter(asmFilePath);
            handleDirectory(vmFile.getPath());
        } else {
            throw new RuntimeException("I/O Error, please enter a valid file or directory name");
        }
    }

    private static void handleDirectory(String path) {
        codeWriter.writeInit();
        File inDirectory = new File(path);
        for (String f : Objects.requireNonNull(inDirectory.list())) {
            if (f.endsWith(inFileExt)) {
                String inFilePath = inDirectory + fileSeparator + f;
                handleFile(inFilePath);
            }
        }
    }

    private static void handleFile(String path) {
        parser = new Parser(path);
        codeWriter.setFileName(path);

        while (parser.hasMoreCommands()) {
            parser.advance();
            switch (parser.getCurrentCommandType()) {
                case C_ARITHMETIC -> codeWriter.writeArithmetic(parser.getCurrentCommandArg1());
                case C_PUSH -> codeWriter.writePushPop(CommandType.C_PUSH, parser.getCurrentCommandArg1(), parser.getCurrentCommandArg2());
                case C_POP -> codeWriter.writePushPop(CommandType.C_POP, parser.getCurrentCommandArg1(), parser.getCurrentCommandArg2());
                case C_LABEL -> codeWriter.writeLabel(parser.getCurrentCommandArg1());
                case C_GOTO -> codeWriter.writeGoto(parser.getCurrentCommandArg1());
                case C_IF -> codeWriter.writeIF(parser.getCurrentCommandArg1()); // if first stack pop is true (previous command)
                case C_CALL -> codeWriter.writeCall(parser.getCurrentCommandArg1(), parser.getCurrentCommandArg2());
                case C_FUNCTION -> codeWriter.writeFunction(parser.getCurrentCommandArg1(), parser.getCurrentCommandArg2());
                case C_RETURN -> codeWriter.writeReturn();
                default -> throw new RuntimeException("Got wrong command type");
            }
        }
        codeWriter.close();
    }
}