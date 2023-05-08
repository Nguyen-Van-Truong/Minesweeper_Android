package fi.tuni.minesweeper.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;


@Entity(tableName = "scores")
public class Score {
    // automatically incremented primary key
    @PrimaryKey(autoGenerate = true)
    public int sid;

    // Score == Field Clearing Time
    @ColumnInfo
    public int score;

    // Automatically generated value upon game completion
    @ColumnInfo
    public String date;

    // Completed level difficulty
    @ColumnInfo
    public String difficulty;

    /**
     * constructor for score object, assingns score, difficulty and date to the object
     * @param score
     * @param difficulty
     */
    public Score(int score, String difficulty) {
        this.score = score;
        this.difficulty = difficulty;

        Date currentDate = new Date();
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd.MM.yyyy HH:mm ");
        this.date = dateFormat.format(currentDate);
    }

    // Just a bunch of setters and getters
    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

}
