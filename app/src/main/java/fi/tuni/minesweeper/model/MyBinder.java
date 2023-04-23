package fi.tuni.minesweeper.model;

import android.os.Binder;


public class MyBinder extends Binder {
    //the binded soundplayer object
    private SoundPlayer soundPlayer;

    /**
     * Gets a soundPlayer object upon creations
     * @param soundPlayer
     */
    public MyBinder(SoundPlayer soundPlayer) {
        this.soundPlayer = soundPlayer;
    }

    /**
     * returns a ServiceConnection a SoundPlayer object
     * @return
     */
    public SoundPlayer getSoundPlayer() {
        return this.soundPlayer;
    }
}
