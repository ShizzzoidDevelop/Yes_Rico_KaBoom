package com.yesricokaboom.game;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayDeque;
import java.util.Queue;

public class Board {
    public static class Cell {
        public boolean mine;
        public boolean revealed;
        public boolean flagged;
        public int adjacentMines;
    }

    private final int width;
    private final int height;
    private final int mineCount;
    private final Cell[][] cells;
    private boolean gameOver;
    private boolean victory;
    private boolean firstClickDone;

    public Board(int width, int height, int mineCount) {
        this.width = width;
        this.height = height;
        this.mineCount = Math.min(mineCount, width * height - 1);
        this.cells = new Cell[width][height];
        reset();
    }

    public void reset() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Cell c = new Cell();
                c.mine = false;
                c.revealed = false;
                c.flagged = false;
                c.adjacentMines = 0;
                cells[x][y] = c;
            }
        }
        gameOver = false;
        victory = false;
        firstClickDone = false;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public boolean isGameOver() {
        return gameOver || victory;
    }
    
    public boolean hasMineExploded() {
        return gameOver && !victory;
    }

    public void toggleFlag(int x, int y) {
        if (isGameOver()) return;
        Cell c = cells[x][y];
        if (c.revealed) return;
        c.flagged = !c.flagged;
    }

    public void reveal(int x, int y) {
        if (isGameOver()) return;
        Cell c = cells[x][y];
        if (c.flagged || c.revealed) return;

        // На первый клик гарантируем отсутствие мины и соседних мин вокруг
        if (!firstClickDone) {
            firstClickDone = true;
            placeMinesAvoiding(x, y);
            computeAdjacencies();
        }

        if (c.mine) {
            c.revealed = true;
            gameOver = true;
            revealAllMines();
            return;
        }

        floodReveal(x, y);
        checkVictory();
    }

    private void revealAllMines() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (cells[i][j].mine) {
                    cells[i][j].revealed = true;
                }
            }
        }
    }

    private void placeMinesAvoiding(int safeX, int safeY) {
        int placed = 0;
        while (placed < mineCount) {
            int x = MathUtils.random(0, width - 1);
            int y = MathUtils.random(0, height - 1);
            if ((x == safeX && y == safeY) || cells[x][y].mine) continue;
            // избегаем ближайшего кольца вокруг первой клетки, чтобы дать шанс на пустую область
            if (Math.abs(x - safeX) <= 1 && Math.abs(y - safeY) <= 1) continue;
            cells[x][y].mine = true;
            placed++;
        }
    }

    private void computeAdjacencies() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].mine) {
                    cells[x][y].adjacentMines = -1;
                    continue;
                }
                int count = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (isInside(nx, ny) && cells[nx][ny].mine) count++;
                    }
                }
                cells[x][y].adjacentMines = count;
            }
        }
    }

    private void floodReveal(int sx, int sy) {
        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{sx, sy});
        while (!q.isEmpty()) {
            int[] p = q.remove();
            int x = p[0], y = p[1];
            Cell c = cells[x][y];
            if (c.revealed || c.flagged) continue;
            c.revealed = true;
            if (c.adjacentMines > 0) continue;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx, ny = y + dy;
                    if (isInside(nx, ny)) {
                        Cell nc = cells[nx][ny];
                        if (!nc.revealed && !nc.mine) q.add(new int[]{nx, ny});
                    }
                }
            }
        }
    }

    private void checkVictory() {
        int hidden = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!cells[x][y].revealed && !cells[x][y].mine) hidden++;
            }
        }
        if (hidden == 0) {
            victory = true;
            // авто-раскрытие всех мин для наглядности
            revealAllMines();
        }
    }
}


