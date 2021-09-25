package com.github.cloudecho.sudoku;

import java.awt.*;

public class Colors {
    public static final Color BG_COLOR = new Color(0xe7, 0xe7, 0xe7);
    public static final Color BORDER_COLOR = Color.LIGHT_GRAY;
    public static final Color SELECTED_COLOR = new Color(192, 190, 91, 128);

    public static final Color DEFAULT_TEXT_COLOR = Color.DARK_GRAY;
    public static final Color INPUT_TEXT_COLOR = new Color(82, 173, 173);
    public static final Color ERROR_TEXT_COLOR = new Color(213, 120, 120);

    private static final Color[] COLORS = new Color[]{
            DEFAULT_TEXT_COLOR,
            INPUT_TEXT_COLOR,
            ERROR_TEXT_COLOR
    };

    private Colors() {
    }

    public static Color of(byte id) {
        return COLORS[id];
    }

    public static byte indexOf(Color color) {
        for (byte i = 0; i < COLORS.length; i++) {
            if (COLORS[i].equals(color)) {
                return i;
            }
        }
        return -1; // not found
    }

    private static final int DIGIT_BITS = 4;

    static byte withColor(byte digit, byte colorId) {
        return (byte) (colorId << DIGIT_BITS | digit);
    }

    private static byte indexOf(byte digitWithColor) {
        if (digitWithColor <= 0) {
            return 0;
        }

        return (byte) (digitWithColor >> DIGIT_BITS);
    }

    static byte originalDigit(byte digitWithColor) {
        if (digitWithColor <= 0) {
            return digitWithColor;
        }

        return (byte) (0x0f & digitWithColor);
    }

    static Color ofDigit(byte digitWithColor) {
        return of(indexOf(digitWithColor));
    }
}
