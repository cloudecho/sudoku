

package com.github.cloudecho.sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class Gui extends JFrame {
    private static final long serialVersionUID = 1L;
    public static final int UNIT_SIZE = 56;
    public static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, UNIT_SIZE / 2);
    public static final Font FONT2 = new Font(Font.MONOSPACED, Font.PLAIN, 3 * UNIT_SIZE / 10);

    private MPanel matrix;
    private JComponent main;

    private final JButton btnPuzzle = new JButton(LABEL_PUZZLE);
    private final JButton btnRestart = new JButton(LABEL_RESTART);
    private final JButton btnReset = new JButton(LABEL_RESET);
    private final ButtonGroup buttonGroupLevel = new ButtonGroup();

    public Gui(Sudoku sudoku) {
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.init(sudoku);
        this.addListeners(sudoku);
    }

    private void init(Sudoku sudoku) {
        this.setTitle("Sudoku : github.com/cloudecho/sudoku");
        this.matrix = new MPanel(sudoku.model.row, sudoku.model.col);

        JRadioButton btnEasy = new JRadioButton("EASY");
        JRadioButton btnNormal = new JRadioButton("NORMAL");
        JRadioButton btnHard = new JRadioButton("HARD");

        btnEasy.setActionCommand(String.valueOf(Sudoku.EASY));
        btnNormal.setActionCommand(String.valueOf(Sudoku.NORMAL));
        btnHard.setActionCommand(String.valueOf(Sudoku.HARD));

        buttonGroupLevel.add(btnEasy);
        buttonGroupLevel.add(btnNormal);
        buttonGroupLevel.add(btnHard);
        btnEasy.setSelected(true);

        btnEasy.setFont(FONT2);
        btnNormal.setFont(FONT2);
        btnHard.setFont(FONT2);
        btnPuzzle.setFont(FONT2);
        btnReset.setFont(FONT2);
        btnRestart.setFont(FONT2);

        main = new JPanel();
        main.setLayout(new BorderLayout());
        this.getContentPane().add(main);

        Box north = Box.createHorizontalBox();
        north.add(btnEasy);
        north.add(btnNormal);
        north.add(btnHard);
        north.add(Box.createHorizontalGlue());
        north.add(btnPuzzle);
        north.add(btnReset);
        north.add(btnRestart);

        main.add(north, BorderLayout.NORTH);
        main.add(matrix, BorderLayout.CENTER);
    }

    private void paint(Graphics g, Model model, Point currGrid) {
        paintBorder(g);
        selectGrid(g, currGrid.y, currGrid.x);

        for (int i = 0; i < matrix.row; ++i) {
            for (int j = 0; j < matrix.col; ++j) {
                byte n = model.get(i, j);
                if (n > 0) {
                    matrix.setText(g, i, j,
                            String.valueOf(Colors.originalDigit(n)),
                            Colors.ofDigit(n));
                }
            }
        }
    }

    void repaintGui() {
        matrix.repaint();
    }

    private void selectGrid(Graphics g, int r, int c) {
        matrix.paint(g, r, c, Colors.SELECTED_COLOR);
    }

    float getSelectedLevel() {
        String ac = buttonGroupLevel.getSelection().getActionCommand();
        return Float.parseFloat(ac);
    }

    private void paintBorder(Graphics g) {
        ((Graphics2D) g).setStroke(new BasicStroke(3));
        g.setColor(Colors.BORDER_COLOR);

        // for each subgrid
        for (int i = 0; i < matrix.row / 3; i++) {
            for (int j = 0; j < matrix.col / 3; j++) {
                g.drawRect(3 * j * Gui.UNIT_SIZE, 3 * i * Gui.UNIT_SIZE, 3 * Gui.UNIT_SIZE, 3 * Gui.UNIT_SIZE);
            }
        }
    }

    void stateChanged(int newState) {
        switch (newState) {
            case Sudoku.STATE_SOLVED:
                btnReset.setText(LABEL_SOLVED);
                break;
            case Sudoku.STATE_SOLVING:
                btnReset.setText(LABEL_RESET);
                break;
        }
    }

    private static final String LABEL_PUZZLE = "PUZZLE";
    private static final String LABEL_DONE = "DONE";
    private static final String LABEL_SOLVED = "SOLVED";
    private static final String LABEL_RESET = "RESET";
    private static final String LABEL_RESTART = "RESTART";

    private void addListeners(final Sudoku sudoku) {
        this.btnPuzzle.addActionListener((actionEvent) -> {
            if (LABEL_PUZZLE.equals(btnPuzzle.getText())) {
                sudoku.startPuzzle();
                btnPuzzle.setText(LABEL_DONE);
                btnReset.setEnabled(false);
                btnRestart.setEnabled(false);
            } else {
                sudoku.endPuzzle();
                btnPuzzle.setText(LABEL_PUZZLE);
                btnReset.setEnabled(true);
                btnRestart.setEnabled(true);
            }
        });

        this.btnReset.addActionListener((actionEvent) -> {
            if (LABEL_RESET.equals(btnReset.getText())) {
                sudoku.reset();
            }
        });

        this.btnRestart.addActionListener((actionEvent) -> sudoku.restart());

        this.matrix.addComponentPaintListener((g) -> paint(g, sudoku.model, sudoku.currGrid));

        this.main.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();
                switch (keyCode) {
                    case 37:
                    case 65:
                    case 226:
                        sudoku.gridMove(Moving.LEFT);
                        break;
                    case 38:
                    case 87:
                    case 224:
                        sudoku.gridMove(Moving.UP);
                        break;
                    case 39:
                    case 68:
                    case 227:
                        sudoku.gridMove(Moving.RIGHT);
                        break;
                    case 40:
                    case 83:
                    case 225:
                        sudoku.gridMove(Moving.DOWN);
                        break;
                    case '1':
                    case '1' + 48:
                    case '2':
                    case '2' + 48:
                    case '3':
                    case '3' + 48:
                    case '4':
                    case '4' + 48:
                    case '5':
                    case '5' + 48:
                    case '6':
                    case '6' + 48:
                    case '7':
                    case '7' + 48:
                    case '8':
                    case '8' + 48:
                    case '9':
                    case '9' + 48:
                        sudoku.inputDigit(keyCode);
                        break;
                    case 8:
                    case 127:
                        sudoku.clearDigit();
                        break;
                    default:
                        System.out.println("keyCode=" + keyCode);
                }
            }
        });

        new Thread(new FocusRequesting(main)).start();
    }

    private static class FocusRequesting implements Runnable {
        JComponent component;

        private FocusRequesting(JComponent component) {
            this.component = component;
        }

        public void run() {
            while (true) {
                if (!component.isFocusOwner()) {
                    component.requestFocusInWindow();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
