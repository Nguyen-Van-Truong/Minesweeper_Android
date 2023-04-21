package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.myapplication.model.Board;
import com.example.myapplication.model.BoardAdapter;

public class BoardActivity extends AppCompatActivity {
    private Board board;
    private RecyclerView grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        getSupportActionBar().hide();

        board = new Board(20, 15);
        GridLayoutManager layoutManager = new GridLayoutManager(this, board.getWidth());
        BoardAdapter adapter = new BoardAdapter(board);

        grid = findViewById(R.id.board_grid);
        grid.setLayoutManager(layoutManager);


        grid.setAdapter(adapter);


    }
}

