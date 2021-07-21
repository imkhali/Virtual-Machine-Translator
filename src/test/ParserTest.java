package test;

import model.CommandType;
import model.Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.Assert.*;

public class ParserTest {
    private Parser testParser;
    private final String testFilePath = "..\\StackArithmetic\\SimpleAdd\\SimpleAdd.vm";
    ArrayList<String> commandsFromFile;

    @Before
    public void setup() {
        testParser = new Parser(testFilePath);
    }

    @Test
    public void testConstructor() {
        assertNotNull(testParser);
        assertEquals(testFilePath, testParser.getVmFilePath());
        assertNull(testParser.getCurrentCommandString());
    }

    @Test
    public void testHasMoreCommands() {
        fillCommandsFromFileList();

        for (int i = 0; i < commandsFromFile.size(); i++) {
            assertTrue(testParser.hasMoreCommands());
            testParser.advance();
        }
        assertFalse(testParser.hasMoreCommands());
    }

    @Test
    public void testAdvance() {
        fillCommandsFromFileList();

        // starts as null
        assertNull(testParser.getCurrentCommandString());

        for (int i = 0; i < commandsFromFile.size(); i++) {
            testParser.advance();
            assertEquals(commandsFromFile.get(i), testParser.getCurrentCommandString());
        }
    }

    @Test
    public void testCurrentCommandType() {
        fillCommandsFromFileList();

        for (String command: commandsFromFile) {
            testParser.advance();
            String commandType = command.split(" ")[0];
            switch (commandType) {
                case "push":
                    assertEquals(CommandType.C_PUSH, testParser.getCurrentCommandType());
                    break;
                case "pop":
                    assertEquals(CommandType.C_POP, testParser.getCurrentCommandType());
                    break;
                case "add":
                case "neg":
                case "eq":
                case "lt":
                    assertEquals(CommandType.C_ARITHMETIC, testParser.getCurrentCommandType());
                default:
                    break;
            }
        }
    }


    @Test
    public void testArg1() {
        fillCommandsFromFileList();

        for (String command: commandsFromFile) {
            testParser.advance();
            String commandType = command.split(" ")[0];
            String arg1FromList;
            switch (commandType) {
                case "push":
                case "pop":
                    arg1FromList = command.split(" ")[1];
                    assertEquals(arg1FromList, testParser.getCurrentCommandArg1());
                    break;
                case "add":
                case "neg":
                case "eq":
                case "lt":
                    arg1FromList = command.strip();
                    assertEquals(arg1FromList, commandType);
                default:
                    break;
            }
        }
    }

    @Test
    public void testArg2() {
        fillCommandsFromFileList();

        for (String command: commandsFromFile) {
            testParser.advance();
            String commandType = command.split(" ")[0];
            Integer arg2FromList;
            switch (commandType) {
                case "push":
                case "pop":
                    arg2FromList = Integer.parseInt(command.split(" ")[2]);
                    assertEquals(arg2FromList, testParser.getCurrentCommandArg2());
                    break;
                default:
                    assertNull(testParser.getCurrentCommandArg2());
                    break;
            }
        }
    }
    private void fillCommandsFromFileList() {
        commandsFromFile = new ArrayList<>();
        File testFile = new File(testFilePath);
        try {
            Scanner testFileScanner = new Scanner(testFile);
            while(testFileScanner.hasNextLine()) {
                String currentCommand = testFileScanner.nextLine().strip();
                if (currentCommand.length() == 0 || currentCommand.startsWith("//")) continue;
                commandsFromFile.add(currentCommand);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
