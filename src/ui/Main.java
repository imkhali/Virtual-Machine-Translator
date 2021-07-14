package ui;

import Model.CodeWriter;
import Model.CommandType;
import Model.Parser;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: <path to vm file>");
            System.exit(-1);
        }

        String vmFilePath = args[0];
        String asmFilePath= vmFilePath.substring(0, vmFilePath.lastIndexOf('.')) + ".asm";

        Parser parser = new Parser(vmFilePath);
        CodeWriter codeWriter = new CodeWriter(asmFilePath);

        while (parser.hasMoreCommands()) {
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
                default:
                    break;
            }
            parser.advance();
        }
    }
}