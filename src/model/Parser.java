package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * VM code parser
 * TODO: Refactor to make command a separate class with fields: string, type, arg1, arg2
 */
public class Parser {
    public static final String INLINE_COMMENT_PATTERN = "\\s*//.*";
    private final String vmFilePath;
    private List<String> vmCommands;

    private String currentCommandString = null;
    private CommandType currentCommandType = null;
    private String currentCommandArg1 = null;
    private Integer currentCommandArg2 = null;

    public Parser(String vmFilePath) {
        this.vmFilePath = vmFilePath;
        this.vmCommands = new LinkedList<>();
        this.readCommandsFromFile();
    }

    private void readCommandsFromFile() {
        try {
            File vmFile = new File(this.vmFilePath);
            Scanner vmFileScanner = new Scanner(vmFile);
            while(vmFileScanner.hasNextLine()) {
                String currentCommand = vmFileScanner.nextLine().strip();
                if (currentCommand.length() == 0 || currentCommand.startsWith("//")) continue;
                currentCommand = currentCommand.replaceFirst(INLINE_COMMENT_PATTERN, ""); // inline comments
                vmCommands.add(currentCommand);
            }
        } catch (FileNotFoundException e) {
            System.out.println("I/O Error " + e);
            e.printStackTrace();
        }
    }

    public String getVmFilePath() {
        return vmFilePath;
    }

    public String getCurrentCommandString() {
        return currentCommandString;
    }

    // EFFECTS: return true if there are more VM commands in the inputFile
    public boolean hasMoreCommands() {
        return (vmCommands.size() > 0);
    }

    // REQUIRES: vmFileHasMoreCommands must be true
    // MODIFIES: this
    // EFFECTS: assign currentCommand the next command from vmFile
    public void advance() {
        if (!hasMoreCommands()) {
            throw new RuntimeException("No more VM commands!");
        }
        this.currentCommandString = vmCommands.remove(0);
        parse();
    }

    // REQUIRES: currentCommand is not NULL
    // MODIFIES: this
    // EFFECTS: parses the currentCommand into its constituent parts
    private void parse() {
        if (currentCommandString != null) {
            String[] tokens = currentCommandString.split(" "); // TODO: assume single space between tokens, relax.
            switch (tokens[0]) {
                case "push" -> {
                    currentCommandType = CommandType.C_PUSH;
                    currentCommandArg1 = tokens[1];
                    currentCommandArg2 = Integer.parseInt(tokens[2]);
                }
                case "pop" -> {
                    currentCommandType = CommandType.C_POP;
                    currentCommandArg1 = tokens[1];
                    currentCommandArg2 = Integer.parseInt(tokens[2]);
                }
                case "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not" -> {
                    currentCommandType = CommandType.C_ARITHMETIC;
                    currentCommandArg1 = tokens[0];
                    currentCommandArg2 = null;
                }
                case "label" -> {
                    currentCommandType = CommandType.C_LABEL;
                    currentCommandArg1 = tokens[1];
                    currentCommandArg2 = null;
                }
                case "goto" -> {
                    currentCommandType = CommandType.C_GOTO;
                    currentCommandArg1 = tokens[1];
                    currentCommandArg2 = null;
                }
                case "if-goto" -> {
                    currentCommandType = CommandType.C_IF;
                    currentCommandArg1 = tokens[1];
                    currentCommandArg2 = null;
                }
                case "call" -> {
                    currentCommandType = CommandType.C_CALL;
                    currentCommandArg1 = tokens[1];
                    currentCommandArg2 = Integer.parseInt(tokens[2]);
                }
                case "function" -> {
                    currentCommandType = CommandType.C_FUNCTION;
                    currentCommandArg1 = tokens[1];
                    currentCommandArg2 = Integer.parseInt(tokens[2]);
                }
                case "return" -> {
                    currentCommandType = CommandType.C_RETURN;
                    currentCommandArg1 = null;
                    currentCommandArg2 = null;
                }
                default -> throw new RuntimeException("\"" + currentCommandString + "\" is not a valid VM command!");
            }
        }
    }

    // REQUIRES: currentCommand is not NULL
    // EFFECTS: return the constant representing the type of the current VM command
    public CommandType getCurrentCommandType() {
        return this.currentCommandType;
    }

    // REQUIRES: currentCommand is neither NULL nor of type C_RETURN
    // EFFECTS: returns the first argument of the current VM command
    public String getCurrentCommandArg1() {
        return this.currentCommandArg1;
    }

    // REQUIRES: currentCommand is not NULL and if currentCommand of type
    //           C_PUSH, C_POP, C_FUNCTION, C_CALL
    // EFFECTS: returns the second argument of the current VM command if any
    public Integer getCurrentCommandArg2() {
        return this.currentCommandArg2;
    }

}
