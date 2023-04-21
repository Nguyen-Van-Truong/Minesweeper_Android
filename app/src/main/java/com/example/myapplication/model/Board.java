package com.example.myapplication.model;

public class Board {
    private Cell[][] cells;

    public Board(int numRows, int numCols) {
        cells = new Cell[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    public int getWidth() {
        return cells[0].length;
    }

    public int getHeight() {
        return cells.length;
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
}
