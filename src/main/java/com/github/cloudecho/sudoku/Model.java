package com.github.cloudecho.sudoku;

/**
 * Model factory.
 */
public class Model {
    public static final int MAX_NUM = 9;

    private static final byte MARK_ROW = -1;
    private static final byte MARK_COL = -2;
    private static final byte MARK_SUBGRID = -4;

    final int row;
    final int col;
    private final byte[][] m;

    public Model(int row, int col) {
        this.row = row;
        this.col = col;
        this.m = new byte[row][col];
    }

    public void random() {
        for (int i = 1; ; i++) {
            if (random0()) {
                System.out.println("success at trial " + i);
                break;
            }
        }
    }

    private boolean random0() {
        for (int x = 0; x < row / 3; x++) {
            for (int y = 0; y < col / 3; y++) {
                if (!subgrid(x, y)) {
                    return false;
                }
            }
        }
        // success
        return true;
    }

    public byte get(int r, int c) {
        return this.m[r][c];
    }

    public void update(int r, int c, byte val) {
        this.m[r][c] = val;
    }

    private boolean subgrid(final int x, final int y) {
        final int i0 = 3 * x;
        final int j0 = 3 * y;
        byte[] a = arr1to9();

        // for each number in the subgrid
        for (int k = 0; k < MAX_NUM; k++) {
            final int i1 = k / 3;
            final int j1 = k % 3;

            if (!markRow(i0, j0, a, i1, j1)) {
                return false;
            }

            if (!markColumn(i0, j0, a, i1, j1)) {
                return false;
            }

            int w = randomIndex(unmarkedNum(a));
            boolean found = false;

            for (int i = 0; i < a.length; i++) {
                if (a[i] < 0) {
                    continue;
                }
                if (w == 0) {
                    m[i0 + i1][j0 + j1] = a[i];
                    // mark deleted
                    a[i] = MARK_SUBGRID;
                    found = true;
                    break;
                }
                w--;
            }

            if (!found) { // fail
                return false;
            }
        }

        // success
        return true;
    }

    private boolean markRow(final int i0, final int j0, final byte[] a, final int i1, final int j1) {
        if (j1 > 0) {
            return true;
        }

        // unmark previous row
        for (int i = 0; i < a.length; i++) {
            if (a[i] < 0 && a[i] > MARK_SUBGRID) {
                a[i] = (byte) (i + 1);
            }
        }

        // mark delete (row)
        for (int j = 0; j < j0; j++) {
            byte n = m[i0 + i1][j];
            if (n == 0) { // fail
                return false;
            }
            if (a[n - 1] > 0) {
                a[n - 1] = MARK_ROW;
            }
        }

        return true;
    }

    private boolean markColumn(final int i0, final int j0, final byte[] a, final int i1, final int j1) {
        // unmark previous column
        for (int i = 0; i < a.length; i++) {
            if (MARK_COL == a[i]) {
                a[i] = (byte) (i + 1);
            }
        }

        for (int i = 0; i < i0; i++) {
            byte n = m[i][j0 + j1];
            if (n == 0) { // fail
                return false;
            }
            if (a[n - 1] > 0) {
                a[n - 1] = MARK_COL;
            }
        }

        return true;
    }

    private static final int MAX_HIDES_IN_SUBGRID = 7;

    public void hideDigits(float probability) {
        final int maxHides = maxHides(probability);
        int hides = 0;

        for (int x = 0; x < this.row / 3; x++) {
            for (int y = 0; y < this.col / 3; y++) {
                // for each subgrid
                int hidesInSubgrid = 0;
                for (int k = 0; k < Model.MAX_NUM; k++) {
                    if (Math.random() < probability) {
                        final int i = 3 * x + k / 3;
                        final int j = 3 * y + k % 3;
                        this.m[i][j] = (byte) (-this.m[i][j]);

                        if (++hides >= maxHides) {
                            return;
                        }
                        if (++hidesInSubgrid >= MAX_HIDES_IN_SUBGRID) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private int maxHides(float probability) {
        return (int) Math.ceil(row * col * probability);
    }

    private static final byte[] ARR_1_TO_9 = new byte[MAX_NUM];

    static {
        for (int i = 0; i < MAX_NUM; i++) {
            ARR_1_TO_9[i] = (byte) (i + 1);
        }
    }

    private byte[] arr1to9() {
        return ARR_1_TO_9.clone();
    }

    private int unmarkedNum(byte[] a) {
        int r = 0;
        for (int b : a) {
            if (b > 0) {
                r++;
            }
        }
        return r;
    }

    private int randomIndex(int n) {
        return (int) (Math.random() * n);
    }

    public boolean solved() {
        // check each subgrid
        for (int x = 0; x < row / 3; x++) {
            for (int y = 0; y < col / 3; y++) {
                // for each number in the subgrid
                byte[] a = arr1to9();
                for (int k = 0; k < MAX_NUM; k++) {
                    final int i = 3 * x + k / 3;
                    final int j = 3 * y + k % 3;
                    byte n = Colors.originalDigit(this.m[i][j]);
                    if (n <= 0) {
                        return false;
                    }
                    a[n - 1] = MARK_SUBGRID;
                }
                if (unmarkedNum(a) > 0) {
                    return false;
                }
            }
        }

        // check each row
        for (int i = 0; i < row; i++) {
            byte[] a = arr1to9();
            for (int j = 0; j < col; j++) {
                byte n = Colors.originalDigit(this.m[i][j]);
                a[n - 1] = MARK_ROW;
            }
            if (unmarkedNum(a) > 0) {
                return false;
            }
        }

        // check each column
        for (int j = 0; j < col; j++) {
            byte[] a = arr1to9();
            for (int i = 0; i < row; i++) {
                byte n = Colors.originalDigit(this.m[i][j]);
                a[n - 1] = MARK_COL;
            }
            if (unmarkedNum(a) > 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("{ sudoku ");
        b.append(row).append('x').append(col).append('\n');

        for (int i = 0; i < row; i++) {
            if (i > 0 && i % 3 == 0) {
                b.append("- - - + - - - + - - -\n");
            }
            for (int j = 0; j < col; j++) {
                if (j > 0 && j % 3 == 0) {
                    b.append("| ");
                }
                b.append(m[i][j]).append(' ');
            }
            b.append('\n');
        }

        return b.append('}').toString();
    }
}
