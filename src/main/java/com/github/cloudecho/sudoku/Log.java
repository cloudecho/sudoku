package com.github.cloudecho.sudoku;

import java.util.logging.Logger;

class Log {
    private static final Logger LOGGER = Logger.getLogger("sudoku");

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %3$s %4$s %5$s%6$s%n");
    }

    static void debug(Object... msg) {
        LOGGER.fine(toString(msg));
    }

    static void info(Object... msg) {
        LOGGER.info(toString(msg));
    }

    static void warn(Object... msg) {
        LOGGER.warning(toString(msg));
    }

    static void error(Object... msg) {
        LOGGER.severe(toString(msg));
    }

    private static String toString(Object... msg) {
        StringBuilder b = new StringBuilder(Thread.currentThread().getName());
        for (Object m : msg) {
            b.append(' ').append(m);
        }
        return b.toString();
    }
}
