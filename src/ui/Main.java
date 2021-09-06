package ui;

import model.CodeWriter;
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
        codeWriter.process(path);
    }
}