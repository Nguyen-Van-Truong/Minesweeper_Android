package fi.tuni.minesweeper.model;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class Timer extends IntentService {

    public Timer() {
        super("Timer");
        LocalBroadcastManager.getInstance(Timer.this).registerReceiver(broadcastReceiver, new IntentFilter("STOP"));
    }

    private int time;
    private boolean running = false;
    @Override
    protected void onHandleIntent(Intent intent) {
        running = true;
        System.out.println("Timer started");
        for (time = 0; time < 1000; time++) {
            try {
                if(running) {
                    updateTimer(time);
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * UpdateTimer Sends local broadcasts to the game activity
     * @param time
     */
    private void updateTimer(int time) {
        Intent i = new Intent();
        i.putExtra("time", time);
        i.setAction("TIMER");

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Broadcast received, stopping timer");
            running = false;
        }
    };

}
