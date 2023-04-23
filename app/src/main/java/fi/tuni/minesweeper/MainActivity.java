package fi.tuni.minesweeper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fi.tuni.minesweeper.model.MyBinder;
import fi.tuni.minesweeper.model.SoundPlayer;


public class MainActivity extends AppCompatActivity {

    Activity messenger;
    private ServiceConnection connectService;

    /**
     * upon creation animates the play button, title and prepares the soundService
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messenger = this;
        buttonAnimator();
        titleAnimator();
        connectService = new MyConnection();
    }

    // SoundPlayer connection related variables
    private SoundPlayer soundService;
    private boolean soundBound = false;

    /**
     * Binds the SoundPlayer upon application start
     */
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SoundPlayer.class);
        bindService(intent, connectService, Context.BIND_AUTO_CREATE);
    }

    /**
     * Assings and starts the button animation on the main screen
     */
    public void buttonAnimator() {
        Button btn = findViewById(R.id.playButton);
        Animation buttonAnimation =
                AnimationUtils.loadAnimation(this, R.anim.button_bounce);
        btn.startAnimation(buttonAnimation);
    }

    /**
     * Assings and starts the Title animation on the main screen
     */
    public void titleAnimator() {
        ImageView title = findViewById(R.id.title);
        Animation buttonAnimation =
                AnimationUtils.loadAnimation(this, R.anim.title_tilt);
        title.startAnimation(buttonAnimation);

    }

    /**
     * Lê Tuấn Cảnh
     * Lựa chọn button sử dụng switch case nếu lựa chọn button xem thành tích sau đó đưa đến màn hình HighScoreActivity
     *
     * @param v Clicked View
     */
    public void clicked(View v) {
        Intent intent;
        playSound(R.raw.click);
        switch(v.getId()) {
            case(R.id.playButton):
                System.out.println("play");
                intent = new Intent(this, LevelSelectionActivity.class);
                startActivity(intent);
                break;
            case(R.id.highScoreButton):
                System.out.println("High Scores");
                intent = new Intent(this, HighScoreActivity.class);
                startActivity(intent);
                break;
            case(R.id.settingsButton):
                System.out.println("Settings");
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case(R.id.exitsButton):
                System.out.println("exit");
                finish();
                break;
        }
    }

    /**
     *
     * @param v Clicked view
     */
    public void titleClick(View v) {
        toaster("welcome!");
    }

    /**
     *
     * @param text
     */
    public void toaster(String text) {
        Toast.makeText(messenger, text, Toast.LENGTH_LONG).show();
    }

    /**
     *
     *
     */
    private void playSound(int audioId) {
        if(soundBound) {
            soundService.playSound(audioId);
        }
    }

    /**
     * MyConnection maintains the connection between SoundPlayer and this activity
     */
    class MyConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // After bound to SoundPlayer, cast the IBinder and get SoundService instance
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
