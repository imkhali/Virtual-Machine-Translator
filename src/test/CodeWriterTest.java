package test;

import Model.CodeWriter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CodeWriterTest {
    private CodeWriter testCodeWriter;
    private final String testFilePath = "..\\StackArithmetic\\SimpleAdd\\SimpleAdd.asm";

    @Before
    public void setup() {
        testCodeWriter = new CodeWriter(testFilePath);
    }

    @Test
    public void testConstructor() {
        assertNotNull(testCodeWriter);
        assertEquals(testFilePath, testCodeWriter.getAsmFilePath());
    }

    @Test
    public void testWriteArithmetic() {
        // TODO
    }

    @Test
    public void testWritePushPop() {
        // TODO
    }

}
