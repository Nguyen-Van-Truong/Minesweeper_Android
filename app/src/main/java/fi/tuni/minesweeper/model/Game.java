package fi.tuni.minesweeper.model;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import fi.tuni.minesweeper.R;


public class Game extends AppCompatActivity {
    static Activity messenger;
    Vibrator v;

    private int rows;
    private int cols;
    private int mines;
    private String difficulty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirecting back button action to level selection

        setContentView(R.layout.activity_game);
        messenger = this;
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        Intent intent = getIntent();
        rows = intent.getIntExtra("rows", 5);
        cols = intent.getIntExtra("cols", 5);
        mines = intent.getIntExtra("mines", 5);
        difficulty = intent.getStringExtra("difficulty");

        connectService = new MyConnection();
        newGame();
    }

    private ServiceConnection connectService;
    private SoundPlayer soundService;
    private boolean soundBound = false;
    private static ScoreDatabase scoreDatabase;

    SharedPreferences settings;
    private static final String SETTINGS = "UserSettings";
    private boolean vibrationStatus;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SoundPlayer.class);
        bindService(intent, connectService, Context.BIND_AUTO_CREATE);

        scoreDatabase = Room.databaseBuilder(getApplicationContext(),
                ScoreDatabase.class, "scoredb")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        settings = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        vibrationStatus = settings.getBoolean("vibration", true);

    }

    private final int RUNNING = 0;
    private final int WIN = 1;
    private final int LOSE = -1;
    private int gameState;

    private Cell[][] board;
    private int timer = 0;
    private boolean timerStarted = false;

    public void newGame() {
        gameState = RUNNING;
        resetStats();
        System.out.println("Starting a new game");
        board = generateBoard();
        setMines();
        setScene();

        TextView tv = findViewById(R.id.infoBox);
        tv.setText("Clear the field without triggering the mines");
    }


    public void newGame(View v) {

        resetStats();

        TableLayout tl = findViewById(R.id.gameBoard);
        tl.removeAllViews();
        toaster("Resetting current game.");
        if (soundBound) {
            soundService.playSound(R.raw.newgame);
        }

        gameState = RUNNING;
        System.out.println("Starting a new game");
        board = generateBoard();
        setMines();
        setScene();
    }

    private void resetStats() {
        vibrate();
        TextView infobox = findViewById(R.id.infoBox);
        infobox.setText("Clear the field without triggering the mines");
        stopTimer();

        minesFlagged = 0;

        timer = 0;
        timerStarted = false;
        TextView time = findViewById(R.id.timer);
        time.setText(String.format("%03d", timer));
    }

    private int minesFlagged = 0;

    public Cell[][] generateBoard() {
        Cell[][] board = new Cell[cols][rows];
        for(int i = 0; i<rows; i++) {
            for(int j = 0; j<cols; j++) {
                Cell btn = new Cell(this);
                board[i][j] = btn;

                final Cell[][] tempBoard = board;
                final int currentRow = i;
                final int currentCol = j;

                board[i][j].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if(tempBoard[currentRow][currentCol].isClickable()) {
                            if (soundBound) {
                                SoundPlayer.playSound(R.raw.click);
                            }
                            uncoverCell(currentRow, currentCol);
                            if(!timerStarted) {
                                startTimer();
                                timerStarted = true;
                            }


                            if(tempBoard[currentRow][currentCol].hasMine()) {
                                gameState = LOSE;
                                tempBoard[currentRow][currentCol].triggerMine();
                                gameResolve();
                            }


                            if(gameWinCheck()) {
                                gameState = WIN;
                                gameResolve();
                            }
                        }
                    }
                });
                // Nguyễn Trung Kiên : chức năng nhấn giữ ô đó thì đặt cờ
                // 2. Hệ thống nhận sự kiện nhấn giữ
                board[i][j].setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        // 2.Hệ thống kiểm tra xem ô đó đã được bật chưa.
                        // Nếu chưa, chuyển đến bước 3
                        if(tempBoard[currentRow][currentCol].isClickable() ||
                                tempBoard[currentRow][currentCol].isFlagged()) {
                           // 3 .Hệ thống kiểm tra ô đó chưa được đánh dấu cờ và chưa là dấu chấm hỏi
                            if (!tempBoard[currentRow][currentCol].isFlagged() &&
                                    !tempBoard[currentRow][currentCol].isQuestionMarked()) {
                               //4. Hệ thống kiểm tra xem số lượng cờ đã đánh dấu có nhỏ hơn số mìn hay không.
                                if(minesFlagged < mines) {
                                    //5. Dánh dấu ô đó là ô có cờ
                                    tempBoard[currentRow][currentCol].setFlagged(true);
                                    //5.tăng số cờ đánh dấu lên 1
                                    minesFlagged++;
                                    //5. cập nhật lại số cờ trên giao diện
                                    updateMineCountDisplay();
                                    vibrate();
                                }
                            }
                            //Alternative Flow
                            // 1. Nếu ô đó đã được đánh dấu cờ,
                            else if (tempBoard[currentRow][currentCol].isFlagged()) {
                                //1.đánh dấu ô đó là ô đánh dấu dấu chấm hỏi
                                tempBoard[currentRow][currentCol].setQuestionMarked(true);
                               //1. giảm số cờ đánh dấu đi 1
                                minesFlagged--;
                                //1.cập nhật lại hiển thị số cờ đã đánh dấu.
                                updateMineCountDisplay();
                            }
                           // 2.Nếu ô đó đã được đánh dấu dấu chấm hỏi
                            else {
                                //2.đặt lại ô đó thành ô trống bình thường
                                tempBoard[currentRow][currentCol].clearAllIcons();
                                //2.cho phép người chơi bật ô
                                tempBoard[currentRow][currentCol].setClickable(true);
                                //2.cập nhật lại số cờ
                                updateMineCountDisplay();
                            }
                            // cập nhật lại số cờ
                            updateMineCountDisplay();
                        }
                        return true;

                    }
                });
            }
        }

        System.out.println("Board generated");
        return board;
    }
    // hàm cập nhật lại số cờ
    private void updateMineCountDisplay() {
        // số cờ bằng số tổng số cờ trừ cho số cờ đã đặt
        int minesDisplayed = mines - minesFlagged;
        // cập nhật lại hiển thị ra activity_game.xml
        TextView tv = findViewById(R.id.mineCounter);
        tv.setText("" + '\n' + minesDisplayed);
    }

    public void setMines() {
        System.out.println("Adding mines");
        int i = 0;
        while(i < mines) {
            Random rand = new Random();
            int mineRow = rand.nextInt(rows-1);
            int mineCol = rand.nextInt(cols-1);
            if(!board[mineRow][mineCol].hasMine()) {
                board[mineRow][mineCol].plantMine();
                i++;
            }
        }

        updateMineCountDisplay();
        System.out.println("Mines Set");
    }

    private void uncoverCell(int row, int col) {
        if(board[row][col].isClickable() && !board[row][col].isFlagged()) {
            setNumber(row, col);
            System.out.println("Revealed cell at: " + row + " " + col);
            board[row][col].setRevealed();
            if(board[row][col].getMineCount() == 0) {
                uncoverNearbyCells(row, col);
            }
        }
    }


    public void setScene() {
        System.out.println("Adding cells");
        TableLayout tl = findViewById(R.id.gameBoard);
        TableRow tr = new TableRow(this);
        for(int i=0;i<board.length;i++) {
            for(int j = 0;j<board[i].length;j++) {
                tr.addView(board[i][j]);
            }
            tl.addView(tr);
            tr = new TableRow(this);
        }
        System.out.println("Cells added");
    }


    private boolean gameWinCheck() {
        for(int i = 0;i<board.length;i++) {
            for(int j = 0; j<board[i].length;j++) {
                if(!board[i][j].hasMine() && !board[i][j].isRevealed()){
                    System.out.println("found unrevealed cell at : " + i + ',' + j);
                    return false;
                }
            }
        }
        return true;
    }

    private void setNumber(int currentRow, int currentCol) {
        //if the cell is not mine, assign a number
        int surroundingMines = 0;
        if(!board[currentRow][currentCol].hasMine()) {

            // rotating through each adjacent cell
            for(int nextRow = -1; nextRow <= 1; nextRow++) {
                for(int nextCol = -1; nextCol <= 1; nextCol++) {
                    if(insideBounds(currentRow+nextRow,currentCol+nextCol)) {
                        if(board[currentRow+nextRow][currentCol+nextCol].hasMine()) {
                            surroundingMines++;
                        }
                    }
                }
            }
            board[currentRow][currentCol].setMineCount(surroundingMines);
        } else {
            board[currentRow][currentCol].setMineCount(-1);
        }
    }


    private void uncoverNearbyCells(int currentRow, int currentCol) {
        if(!board[currentRow][currentCol].hasMine()) {

            // rotating through each adjacent cell, checking the cells in a 3x3 area
            for(int nextRow = -1; nextRow <= 1; nextRow++) {
                for(int nextCol = -1; nextCol <= 1; nextCol++) {
                    if(insideBounds(currentRow+nextRow,currentCol+nextCol)) {
                        if(!board[currentRow+nextRow][currentCol+nextCol].hasMine() &&
                                !board[currentRow+nextRow][currentCol+nextCol].isRevealed()) {
                            // reveals mineless and unrevealed cells
                            uncoverCell(currentRow+nextRow, currentCol+nextCol);
                        }
                    }
                }
            }

        }
    }

    private boolean insideBounds(int newRow, int newCol) {
        if(rows > newRow  && newRow >= 0) {
            if(cols > newCol && newCol  >= 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // Nguyễn Trung Kiên : hàm cập nhật số  lại cờ
    private void revealMines() {
        for(int i = 0;i<board.length;i++) {
            for(int j = 0; j<board[i].length;j++) {
                if(board[i][j].hasMine()) {
                    board[i][j].setClickable(true);
                    board[i][j].setRevealed();
                }
                if(!board[i][j].isRevealed()) {
                    board[i][j].setCellDisabled(true);
                }
            }
        }
    }


    private void playSound(int audioId) {
        if(soundBound) {
            soundService.playSound(audioId);
        }
    }


    private void vibrate() {
        if(vibrationStatus) {
            v.vibrate(100);
        }
    }


    private void gameResolve() {
        TextView tv = findViewById(R.id.infoBox);
        tv.setText("Restart by clicking the circular image on the top");

        vibrate();
        revealMines();
        stopTimer();
        if(gameState == WIN) {
            if (soundBound) {
                soundService.playSound(R.raw.gratz);
            }
            if(!difficulty.equals("custom")) {
                Game.scoreDatabase.scoreDao().addScore(new Score(timer, difficulty));
            }
            toaster("Well done! The field was cleared in " + timer + " seconds!");
        } else if(gameState == LOSE) {
            if (soundBound) {
                soundService.playSound(R.raw.explosion);
            }
            toaster("A mine blew up. Game Over.");
        } else {
            System.out.println("Why am I running");
        }
    }

    private void startTimer() {
        TextView infobox = findViewById(R.id.infoBox);
        infobox.setText("The numbers describe how many mines are nearby");

        Intent intent = new Intent(this, Timer.class);
        startService(intent);
    }

    private void stopTimer() {
        Intent i = new Intent();
        i.setAction("STOP");

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(gameState != RUNNING) {
                stopTimer();
            }
            TextView tv = (TextView) findViewById(R.id.timer);
            TextView infobox = findViewById(R.id.infoBox);
            timer = intent.getIntExtra("time", 0);
            tv.setText(String.format("%03d", timer));
            if(timer >= 20 && timer < 40) {
                infobox.setText("Hold to place down flags that cannot be dug accidentally");
            } else if(timer >= 40 && timer < 60) {
                infobox.setText("The time is your score \n" +
                        "the faster you finish, the better the score");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(Game.this).registerReceiver(broadcastReceiver, new IntentFilter("TIMER"));

    }


    public void toaster(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    class MyConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            System.out.println("Fetching soundService from binder");
            MyBinder binder = (MyBinder) service;
            soundService = binder.getSoundPlayer();
            soundBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            soundBound = false;
        }
    }
}
