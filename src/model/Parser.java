package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
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
    private final List<String> vmCommands;

    private Command currentCommand;

    private String currentCommandString = null;

    public Parser(String vmFilePath) {
        this.vmFilePath = vmFilePath;
        this.vmCommands = new LinkedList<>();
        this.readCommandsFromFile();
    }

    private void readCommandsFromFile() {
        try {
            File vmFile = new File(this.vmFilePath);
            Scanner vmFileScanner = new Scanner(vmFile);
            while (vmFileScanner.hasNextLine()) {
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
        return currentCommand.string();
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
                case "push" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_PUSH, Arrays.asList(tokens[1], tokens[2]));
                case "pop" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_POP, Arrays.asList(tokens[1], tokens[2]));
                case "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_ARITHMETIC, Arrays.asList(tokens[0], null));
                case "label" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_LABEL, Arrays.asList(tokens[1], null));
                case "goto" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_GOTO, Arrays.asList(tokens[1], null));
                case "if-goto" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_IF, Arrays.asList(tokens[1], null));
                case "call" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_CALL, Arrays.asList(tokens[1], tokens[2]));
                case "function" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_FUNCTION, Arrays.asList(tokens[1], tokens[2]));
                case "return" ->
                        currentCommand = new Command(currentCommandString, CommandType.C_RETURN, Arrays.asList(null, null));
                default -> throw new RuntimeException("\"" + currentCommandString + "\" is not a valid VM command!");
            }
        }
    }

    // REQUIRES: currentCommand is not NULL
    // EFFECTS: return the constant representing the type of the current VM command
    public CommandType getCurrentCommandType() {
        return currentCommand.type();
    }

    // REQUIRES: currentCommand is neither NULL nor of type C_RETURN
    // EFFECTS: returns the first argument of the current VM command
    public String getCurrentCommandArg1() {
        String token = currentCommand.tokens().get(0);
        if (token != null) {
            return token;
        }
        System.out.println(currentCommandString + vmFilePath);
        throw new RuntimeException("parser did not return a first token for current command");
    }

    // REQUIRES: currentCommand is not NULL and if currentCommand of type
    //           C_PUSH, C_POP, C_FUNCTION, C_CALL
    // EFFECTS: returns the second argument of the current VM command if any
    public Integer getCurrentCommandArg2() {
        String token = currentCommand.tokens().get(1);
        if (token != null) {
            return Integer.parseInt(token);
        }
        System.out.println(currentCommandString + vmFilePath);
        throw new RuntimeException("parser did not return a second token for current command");
    }
}
