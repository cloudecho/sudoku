

package com.github.cloudecho.sudoku;

import javax.swing.*;
import java.awt.*;

public class Sudoku {
    public static final int ROW = 9;
    public static final int COL = 9;

    public static final float EASY = 0.25f;
    public static final float NORMAL = 0.50f;
    public static final float HARD = 0.75f;

    public static final int STATE_SOLVING = 1;
    public static final int STATE_SOLVED = 2;
    public static final int STATE_PUZZLING = 3;

    Model model = new Model(ROW, COL);
    private final Gui gui = new Gui(this);
    final Point currGrid = new Point();

    private volatile int state = STATE_SOLVING;

    public Sudoku() {
        this.init(EASY);
        this.gui.pack();
        this.gui.setVisible(true);
    }

    private void init(float level) {
        changeState(STATE_SOLVING);
        this.model.random();
        System.out.println(this.model);

        this.model.hideDigits(level);
        this.computeCurrGrid();
    }

    void restart() {
        this.init(gui.getSelectedLevel());
        gui.repaintGui();
    }

    void reset() {
        for (int i = 0; i < model.row; i++) {
            for (int j = 0; j < model.col; j++) {
                if (model.get(i, j) > Model.MAX_NUM) {
                    model.set(i, j, (byte) 0);
                }
            }
        }
        gui.repaintGui();
    }

    private void computeCurrGrid() {
        for (int i = 0; i < model.row; i++) {
            for (int j = 0; j < model.col; j++) {
                if (model.get(i, j) < 0) {
                    this.currGrid.move(j, i);
                    return;
                }
            }
        }
    }

    void gridMove(int direction) {
        int x = currGrid.x;
        int y = currGrid.y;

        switch (direction) {
            case Moving.UP:
                y--;
                break;
            case Moving.DOWN:
                y++;
                break;
            case Moving.LEFT:
                x--;
                break;
            case Moving.RIGHT:
                x++;
                break;
        }

        if (x < 0) {
            x = model.col - 1;
        } else if (x > model.col - 1) {
            x = 0;
        } else if (y < 0) {
            y = model.row - 1;
        } else if (y > model.row - 1) {
            y = 0;
        }

        currGrid.move(x, y);
        gui.repaintGui();
    }

    private void changeState(int state) {
        this.state = state;
        gui.stateChanged(state);
    }

    private boolean solved() {
        return STATE_SOLVED == state;
    }

    void inputDigit(int keycode) {
        if (solved()) {
            System.out.println("solved, could not input digit");
            return;
        }

        if (!canEdit(currGrid)) {
            return;
        }

        if (keycode > '9') {
            keycode -= 48;
        }
        byte digit = (byte) (keycode - '0');
        boolean ok = checkInput(digit);
        byte n = Colors.withColor(digit, Colors.indexOf(!ok ?
                Colors.ERROR_TEXT_COLOR :
                puzzling() ? Colors.DEFAULT_TEXT_COLOR : Colors.INPUT_TEXT_COLOR));
        model.set(currGrid.y, currGrid.x, n);
        gui.repaintGui();

        // check resolved
        if (ok && model.isSolved()) {
            changeState(STATE_SOLVED);
        }
    }

    void clearDigit() {
        if (solved()) {
            System.out.println("solved, could not clear digit");
            return;
        }

        if (!canEdit(currGrid)) {
            return;
        }
        model.set(currGrid.y, currGrid.x, (byte) 0);
        gui.repaintGui();
    }

    private boolean canEdit(Point pos) {
        if (puzzling()) {
            return true;
        }
        byte n = model.get(pos.y, pos.x);
        return n <= 0 || n > Model.MAX_NUM;
    }

    private boolean puzzling() {
        return STATE_PUZZLING == this.state;
    }

    void startPuzzle() {
        model.reset();
        changeState(STATE_PUZZLING);
        gui.repaintGui();
    }

    void endPuzzle() {
        changeState(STATE_SOLVING);
    }

    /**
     * Return true if OK.
     */
    private boolean checkInput(byte digit) {
        // check subgrid
        final int i0 = currGrid.y / 3 * 3;
        final int j0 = currGrid.x / 3 * 3;
        for (int u = 0; u < 3; u++) {
            for (int v = 0; v < 3; v++) {
                final int i = i0 + u;
                final int j = j0 + v;
                if (i == currGrid.y && j == currGrid.x) {
                    continue;
                }
                if (Colors.originalDigit(model.get(i, j)) == digit) {
                    return false;
                }
            }
        }

        // check row
        for (int j = 0; j < model.col; j++) {
            if (j == currGrid.x) {
                continue;
            }
            if (Colors.originalDigit(model.get(currGrid.y, j)) == digit) {
                return false;
            }
        }

        // check column
        for (int i = 0; i < model.row; i++) {
            if (i == currGrid.y) {
                continue;
            }
            if (Colors.originalDigit(model.get(i, currGrid.x)) == digit) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Sudoku();
    }
}
