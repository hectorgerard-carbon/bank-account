package io.gerard.connectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class ConsolePrinterShould {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();


    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void print() {

        ConsoleStringPrinter sut = new ConsoleStringPrinter();

        final var expected = """
                Your account : f910cf03-e534-4d9d-a473-94ebe3d2cae3
                	your last balance is : 3320.0
                Operations :\s
                Date 					Type 		Amount\s
                2022-10-29T21:30:18		WITHDRAW	30.0
                2022-10-15T12:05   		DEPOSIT		1200.0
                2022-10-06T17:10:31		DEPOSIT		50.0
                2022-10-02T09:59:59		DEPOSIT		100.0""";

        sut.print(expected);

        final var actual = outputStreamCaptor.toString();

        Assertions.assertEquals(expected + System.lineSeparator(), actual);
    }

}
