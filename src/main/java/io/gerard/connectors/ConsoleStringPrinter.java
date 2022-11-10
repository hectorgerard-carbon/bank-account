package io.gerard.connectors;

import io.gerard.services.StringPrinter;

public class ConsoleStringPrinter implements StringPrinter {
    @Override
    public void print(String value) {
        System.out.println(value);
    }
}
