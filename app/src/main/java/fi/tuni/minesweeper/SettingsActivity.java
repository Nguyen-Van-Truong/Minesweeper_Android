package fi.tuni.minesweeper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import fi.tuni.minesweeper.model.MyBinder;
import fi.tuni.minesweeper.model.ScoreDatabase;
import fi.tuni.minesweeper.model.SoundPlayer;


public class SettingsActivity extends AppCompatActivity {

    Activity messenger;
    private ServiceConnection connectService;
    SharedPreferences settings;
    private static final String SETTINGS = "UserSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        messenger = this;
        connectService = new SettingsActivity.MyConnection();
        settings = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
    }

    private SoundPlayer soundService;
    private boolean soundBound = false;
    private static ScoreDatabase scoreDatabase;
    private boolean soundStatus;
    private Switch soundSwitch;
    private boolean vibrationStatus;
    private Switch vibrationSwitch;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SoundPlayer.class);
        bindService(intent, connectService, Context.BIND_AUTO_CREATE);

        scoreDatabase = Room.databaseBuilder(getApplicationContext(), ScoreDatabase.class, "scoredb").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        soundStatus = settings.getBoolean("sound", true);
        vibrationStatus = settings.getBoolean("vibration", true);

        soundSwitch = findViewById(R.id.soundsStatus);
        vibrationSwitch = findViewById(R.id.vibrationStatus);

        soundSwitch.setChecked(soundStatus);
        vibrationSwitch.setChecked(vibrationStatus);
    }

    SharedPreferences.Editor editor;

    @Override
    protected void onPause() {
        editor = settings.edit();
        editor.putBoolean("sound", soundStatus);
        editor.putBoolean("vibration", vibrationStatus);
        editor.commit();

        toaster("Cài đặt lưu thành công.");
        super.onPause();
    }

    /**
     * Nguyễn Văn Trường
     */
    public void clicked(View v) {
        System.out.println(v.getId());
        switch (v.getId()) {
            case R.id.soundsStatus:
                soundStatus = !soundStatus;
                soundService.toggleSound(soundStatus);
                if (soundStatus) {
                    toaster("Âm thanh đã bật");
                } else if (!soundStatus) {
                    toaster("Âm thanh đã tắt");
                }

                playSound(R.raw.click);
                break;
            case R.id.vibrationStatus:
                vibrationStatus = !vibrationStatus;
                playSound(R.raw.click);

                if (vibrationStatus) {
                    toaster("Rung đã bật");
                } else if (!vibrationStatus) {
                    toaster("Rung đã tắt");
                }
                break;
        }
    }

    public void toaster(String text) {
        Toast.makeText(messenger, text, Toast.LENGTH_LONG).show();
    }

    private void playSound(int audioId) {
        if (soundBound) {
            soundService.playSound(audioId);
        }
    }

    class MyConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
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