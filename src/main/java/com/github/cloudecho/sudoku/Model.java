package com.github.cloudecho.sudoku;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
            this.reset();
            if (random0()) {
                Log.info("success at trial #" + i);
                break;
            }
        }
    }

    private boolean random0() {
        final byte[][] sub = new byte[3][3];
        for (int x = 0; x < row / 3; x++) {
            for (int y = 0; y < col / 3; y++) {
                this.copySubgrid(x, y, sub);
                boolean ok = false;
                // try more times
                for (int z = 0; z < 3 * MAX_NUM; z++) {
                    ok = subgrid(x, y);
                    if (ok) {
                        break;
                    }
                    // reset subgrid
                    this.restoreSubgrid(x, y, sub);
                }
                if (!ok) {
                    return false;
                }
            }
        }
        // success
        return true;
    }

    private void copySubgrid(int x, int y, byte[][] dest) {
        final int i0 = 3 * x;
        final int j0 = 3 * y;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                dest[i][j] = this.m[i0 + i][j0 + j];
            }
        }
    }

    private void restoreSubgrid(int x, int y, byte[][] src) {
        final int i0 = 3 * x;
        final int j0 = 3 * y;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.m[i0 + i][j0 + j] = src[i][j];
            }
        }
    }

    public byte get(int r, int c) {
        return this.m[r][c];
    }

    public void set(int r, int c, byte val) {
        this.m[r][c] = val;
    }

    private boolean subgrid(final int x, final int y) {
        final int i0 = 3 * x;
        final int j0 = 3 * y;
        byte[] a = arr1to9();
        markSubgrid(i0, j0, a);

        // for each number in the subgrid
        for (int k = 0; k < MAX_NUM; k++) {
            final int i1 = k / 3;
            final int j1 = k % 3;

            markRow(i0, j0, a, i1, j1);
            markColumn(i0, j0, a, i1, j1);

            if (this.m[i0 + i1][j0 + j1] > 0) {
                continue;
            }

            final int n = unmarkedNum(a);
            if (0 == n) {
                return false;
            }
            int w = randomIndex(n);
            boolean found = false;

            for (int i = 0; i < a.length; i++) {
                if (a[i] < 0) {
                    continue;
                }
                if (w == 0) {
                    this.m[i0 + i1][j0 + j1] = a[i];
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

    private void markSubgrid(final int i0, final int j0, final byte[] a) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                byte n = this.m[i0 + i][j0 + j];
                if (n > 0) {
                    a[n - 1] = MARK_SUBGRID;
                }
            }
        }
    }

    private void markRow(final int i0, final int j0, final byte[] a, final int i1, final int j1) {
        if (j1 > 0) {
            return;
        }

        // unmark previous row
        for (int i = 0; i < a.length; i++) {
            if (a[i] < 0 && a[i] > MARK_SUBGRID) {
                a[i] = (byte) (i + 1);
            }
        }

        // mark delete (row)
        for (int j = 0; j < col; j++) {
            byte n = m[i0 + i1][j];
            if (n > 0 && a[n - 1] > 0) {
                a[n - 1] = MARK_ROW;
            }
        }
    }

    private void markColumn(final int i0, final int j0, final byte[] a, final int i1, final int j1) {
        // unmark previous column
        for (int i = 0; i < a.length; i++) {
            if (MARK_COL == a[i]) {
                a[i] = (byte) (i + 1);
            }
        }

        for (int i = 0; i < row; i++) {
            byte n = m[i][j0 + j1];
            if (n > 0 && a[n - 1] > 0) {
                a[n - 1] = MARK_COL;
            }
        }
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

    public void reset() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                m[i][j] = 0;
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
        return ThreadLocalRandom.current().nextInt(n);
    }

    public boolean isSolved() {
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

    static final String KEY_SOLVING_THREADS = "SOLVING_THREADS";
    static final String DEFAULT_SOLVING_THREADS = "2";

    public void solve() {
        final int n = Integer.parseInt(System.getProperty(KEY_SOLVING_THREADS, DEFAULT_SOLVING_THREADS));
        final AtomicBoolean solved = new AtomicBoolean(false);
        final AtomicLong trial = new AtomicLong(1);
        final CountDownLatch latch = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            Thread t = new Thread(new RandomSolver(this, solved, trial, latch), "sudoku-solver-" + i);
            t.setDaemon(true);
            t.start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("thread " + Thread.currentThread().getName() + " interrupted", e);
        }
    }

    private static class RandomSolver implements Runnable {
        private final Model model;
        private final AtomicBoolean solved;
        private final AtomicLong trial;
        private final CountDownLatch latch;

        public RandomSolver(Model model, AtomicBoolean solved, AtomicLong trial, CountDownLatch latch) {
            this.model = model;
            this.solved = solved;
            this.trial = trial;
            this.latch = latch;
        }

        public void run() {
            final Model c = model.modelClone();
            for (long i = trial.get(); !solved.get(); i = trial.incrementAndGet()) {
                if (0 == i % 1_000_000) {
                    Log.info("trial #" + i);
                }
                if (c.random0()) {
                    // find an answer
                    synchronized (model) {
                        Log.info("success at trial #" + i);
                        solved.set(true);
                        c.copym(model);
                    }
                    break;
                }
                // reset
                model.copym(c);
            }

            Log.info("done");
            latch.countDown();
        }
    }

    private void copym(Model dest) {
        for (int i = 0; i < row; i++) {
            System.arraycopy(this.m[i], 0, dest.m[i], 0, this.m[i].length);
        }
    }

    public Model modelClone() {
        Model c = new Model(row, col);
        this.copym(c);
        return c;
    }

    public static void main(String[] args) {
        Model model = new Model(9, 9);
        // subgrid 0
        model.set(0, 0, (byte) 8);
        model.set(1, 2, (byte) 3);
        model.set(2, 1, (byte) 7);
        // subgrid 1
        model.set(1, 3, (byte) 6);
        model.set(2, 4, (byte) 9);
        // subgrid 2
        model.set(2, 6, (byte) 2);
        // subgrid 3
        model.set(3, 1, (byte) 5);
        // subgrid 4
        model.set(3, 5, (byte) 7);
        model.set(4, 4, (byte) 4);
        model.set(4, 5, (byte) 5);
        model.set(5, 3, (byte) 1);
        // subgrid 5
        model.set(4, 6, (byte) 7);
        model.set(5, 7, (byte) 3);
        // subgrid 6
        model.set(6, 2, (byte) 1);
        model.set(7, 2, (byte) 8);
        model.set(8, 1, (byte) 9);
        // subgrid 7
        model.set(7, 3, (byte) 5);
        // subgrid 8
        model.set(6, 7, (byte) 6);
        model.set(6, 8, (byte) 8);
        model.set(7, 7, (byte) 1);
        model.set(8, 6, (byte) 4);

        // 8 0 0 | 0 0 0 | 0 0 0
        // 0 0 3 | 6 0 0 | 0 0 0
        // 0 7 0 | 0 9 0 | 2 0 0
        // - - - + - - - + - - -
        // 0 5 0 | 0 0 7 | 0 0 0
        // 0 0 0 | 0 4 5 | 7 0 0
        // 0 0 0 | 1 0 0 | 0 3 0
        // - - - + - - - + - - -
        // 0 0 1 | 0 0 0 | 0 6 8
        // 0 0 8 | 5 0 0 | 0 1 0
        // 0 9 0 | 0 0 0 | 4 0 0
        Log.info(model);

        model.solve();

        // 8 1 2 | 7 5 3 | 6 4 9
        // 9 4 3 | 6 8 2 | 1 7 5
        // 6 7 5 | 4 9 1 | 2 8 3
        // - - - + - - - + - - -
        // 1 5 4 | 2 3 7 | 8 9 6
        // 3 6 9 | 8 4 5 | 7 2 1 
        // 2 8 7 | 1 6 9 | 5 3 4
        // - - - + - - - + - - -
        // 5 2 1 | 9 7 4 | 3 6 8
        // 4 3 8 | 5 2 6 | 9 1 7
        // 7 9 6 | 3 1 8 | 4 5 2
        Log.info(model);
        Log.info("solved:", model.isSolved());
    }
}
