package com.example.labcontrol;

public final class MusicServiceHolder {

    private static MusicService service;

    static void set(MusicService s)      { service = s; }
    static void clear()                  { service = null; }

    public static void pauseIfPlaying() {
        if (service != null) service.onAppBackground();
    }
    public static void resumeIfNeeded() {
        if (service != null) service.onAppForeground();
    }
}
