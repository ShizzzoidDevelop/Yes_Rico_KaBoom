package com.yesricokaboom.core;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Core minefield logic - classic Minesweeper rules
 */
public class Minefield {
    public enum CellState {
        CLOSED, OPEN, FLAGGED, QUESTION
    }
    
    public static class Cell {
        public boolean isMine;
        public CellState state;
        public int adjacentMines;
        
        public Cell() {
            this.isMine = false;
            this.state = CellState.CLOSED;
            this.adjacentMines = 0;
        }
    }
    
    private final int rows;
    private final int cols;
    private final int totalMines;
    private final Cell[][] cells;
    private boolean firstClickDone;
    private boolean gameOver;
    private boolean gameWon;
    private int revealedCells;
    
    public Minefield(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = Math.min(mines, rows * cols - 1);
        this.cells = new Cell[rows][cols];
        initializeCells();
    }
    
    private void initializeCells() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell();
            }
        }
        firstClickDone = false;
        gameOver = false;
        gameWon = false;
        revealedCells = 0;
    }
    
    public void reset() {
        initializeCells();
    }
    
    public Cell getCell(int row, int col) {
        if (!isValid(row, col)) return null;
        return cells[row][col];
    }
    
    public boolean isValid(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public boolean isGameWon() {
        return gameWon;
    }
    
    public boolean isFirstClickDone() {
        return firstClickDone;
    }
    
    public int getRemainingMines() {
        int flagged = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].state == CellState.FLAGGED) {
                    flagged++;
                }
            }
        }
        return totalMines - flagged;
    }
    
    public void revealCell(int row, int col) {
        if (!isValid(row, col) || gameOver) return;
        
        Cell cell = cells[row][col];
        if (cell.state != CellState.CLOSED) return;
        
        // First click safety - generate mines avoiding this cell and neighbors
        if (!firstClickDone) {
            generateMinesAvoiding(row, col);
            firstClickDone = true;
        }
        
        if (cell.isMine) {
            gameOver = true;
            revealAllMines();
            return;
        }
        
        // Flood fill for empty cells
        floodFill(row, col);
        checkWinCondition();
    }
    
    public void toggleFlag(int row, int col) {
        if (!isValid(row, col) || gameOver) return;
        
        Cell cell = cells[row][col];
        if (cell.state == CellState.CLOSED) {
            cell.state = CellState.FLAGGED;
        } else if (cell.state == CellState.FLAGGED) {
            cell.state = CellState.CLOSED;
        }
    }
    
    public void toggleQuestion(int row, int col) {
        if (!isValid(row, col) || gameOver) return;
        
        Cell cell = cells[row][col];
        if (cell.state == CellState.FLAGGED) {
            cell.state = CellState.QUESTION;
        } else if (cell.state == CellState.QUESTION) {
            cell.state = CellState.CLOSED;
        }
    }
    
    public void chord(int row, int col) {
        if (!isValid(row, col) || gameOver) return;
        
        Cell cell = cells[row][col];
        if (cell.state != CellState.OPEN || cell.adjacentMines == 0) return;
        
        // Count flags around this cell
        int flagCount = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr, nc = col + dc;
                if (isValid(nr, nc) && cells[nr][nc].state == CellState.FLAGGED) {
                    flagCount++;
                }
            }
        }
        
        // If flag count matches adjacent mines, reveal unflagged neighbors
        if (flagCount == cell.adjacentMines) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = row + dr, nc = col + dc;
                    if (isValid(nr, nc) && cells[nr][nc].state == CellState.CLOSED) {
                        revealCell(nr, nc);
                    }
                }
            }
        }
    }
    
    private void generateMinesAvoiding(int safeRow, int safeCol) {
        int placed = 0;
        while (placed < totalMines) {
            int r = MathUtils.random(0, rows - 1);
            int c = MathUtils.random(0, cols - 1);
            
            // Avoid safe cell and its neighbors
            if ((r == safeRow && c == safeCol) || 
                (Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1) ||
                cells[r][c].isMine) {
                continue;
            }
            
            cells[r][c].isMine = true;
            placed++;
        }
        
        // Calculate adjacent mine counts
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine) {
                    cells[r][c].adjacentMines = -1;
                    continue;
                }
                
                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc = c + dc;
                        if (isValid(nr, nc) && cells[nr][nc].isMine) {
                            count++;
                        }
                    }
                }
                cells[r][c].adjacentMines = count;
            }
        }
    }
    
    private void floodFill(int startRow, int startCol) {
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startRow, startCol});
        
        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int r = pos[0], c = pos[1];
            
            if (!isValid(r, c) || cells[r][c].state != CellState.CLOSED) continue;
            
            cells[r][c].state = CellState.OPEN;
            revealedCells++;
            
            // If this cell has adjacent mines, don't flood further
            if (cells[r][c].adjacentMines > 0) continue;
            
            // Add all neighbors to queue
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = r + dr, nc = c + dc;
                    if (isValid(nr, nc) && !cells[nr][nc].isMine) {
                        queue.add(new int[]{nr, nc});
                    }
                }
            }
        }
    }
    
    private void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine) {
                    cells[r][c].state = CellState.OPEN;
                }
            }
        }
    }
    
    private void checkWinCondition() {
        int totalCells = rows * cols;
        if (revealedCells == totalCells - totalMines) {
            gameWon = true;
            gameOver = true;
        }
    }
    
    // Getters
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getTotalMines() { return totalMines; }
}

