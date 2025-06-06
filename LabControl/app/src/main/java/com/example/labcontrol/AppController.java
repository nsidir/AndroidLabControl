package com.example.labcontrol;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class AppController extends Application
        implements Application.ActivityLifecycleCallbacks {

    private int startedActivities = 0;  // >0 means app in foreground

    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override public void onActivityStarted(Activity activity) {
        if (startedActivities == 0) {
            MusicServiceHolder.resumeIfNeeded();
        }
        startedActivities++;
    }

    @Override public void onActivityStopped(Activity activity) {
        startedActivities--;
        if (startedActivities == 0) {
            MusicServiceHolder.pauseIfPlaying();
        }
    }

    @Override public void onActivityCreated(Activity a, Bundle b) {}
    @Override public void onActivityResumed(Activity a) {}
    @Override public void onActivityPaused(Activity a) {}
    @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
    @Override public void onActivityDestroyed(Activity a) {}
}
