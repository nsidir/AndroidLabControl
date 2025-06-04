package com.example.labcontrol;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;


public class MusicService extends Service {

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPaused = false;

    private String currentTrackName = "";

    public static final Map<Integer, String> idToNameMap = new HashMap<>();
    static {
        idToNameMap.put(R.raw.song1, "Vangelis – Spiral");
        idToNameMap.put(R.raw.song2, "Daft Punk – Around the World");
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Immediately start song1 (Spiral) by default:
        mediaPlayer = MediaPlayer.create(this, R.raw.song1);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        isPaused = false;

        currentTrackName = idToNameMap.get(R.raw.song1);
    }

    public void play(int rawResId) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPaused = false;
        }
        mediaPlayer = MediaPlayer.create(this, rawResId);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        isPaused = false;

        if (idToNameMap.containsKey(rawResId)) {
            currentTrackName = idToNameMap.get(rawResId);
        } else {
            currentTrackName = "Unknown Track";
        }
    }

    public void pauseOrResume() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        } else if (isPaused) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPaused = false;
        }
    }

    public String getCurrentTrackName() {
        return currentTrackName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
