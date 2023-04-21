package com.example.myapplication.model;

import android.view.*;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {
    private Board board;

    public BoardAdapter(Board board) {
        this.board = board;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Cell cell = board.getCell(position / board.getWidth(), position % board.getWidth());
        holder.itemView.setBackgroundColor(cell.getColor());
    }

    @Override
    public int getItemCount() {
        return board.getWidth() * board.getHeight();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}