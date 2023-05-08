package fi.tuni.minesweeper.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Score.class}, version = 4, exportSchema = false)
public abstract class ScoreDatabase extends RoomDatabase {
    public abstract ScoreDao scoreDao();
}
