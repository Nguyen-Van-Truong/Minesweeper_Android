package com.example.myapplication.model;

public class Cell {
    private int row;
    private int col;
    private boolean isMine;
    private int numAdjacentMines;
    private int color;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public int getNumAdjacentMines() {
        return numAdjacentMines;
    }

    public void setNumAdjacentMines(int numAdjacentMines) {
        this.numAdjacentMines = numAdjacentMines;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
