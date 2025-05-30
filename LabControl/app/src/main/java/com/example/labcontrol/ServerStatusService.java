package com.example.labcontrol;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class ServerStatusService extends Service {
    private final IBinder binder = new LocalBinder();
    private ServerStatusCallback callback;
    private Handler handler;
    private Runnable periodicCheck;
    private List<String> ipList;
    private int port;
    private static final long CHECK_INTERVAL_MS = 5000;

    public class LocalBinder extends Binder {
        public ServerStatusService getService() {
            return ServerStatusService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        periodicCheck = new Runnable() {
            @Override
            public void run() {
                if (ipList != null) {
                    checkServers(ipList, port);
                }
                handler.postDelayed(this, CHECK_INTERVAL_MS);
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallback(ServerStatusCallback callback) {
        this.callback = callback;
    }

    public void startPeriodicChecks(List<String> ipList, int port) {
        this.ipList = ipList;
        this.port = port;
        handler.post(periodicCheck);
    }

    public void stopPeriodicChecks() {
        handler.removeCallbacks(periodicCheck);
    }

    public void checkServers(final List<String> ipList, final int port) {
        new Thread(() -> {
            for (String ip : ipList) {
                boolean isConnected = isReachable(ip, port);
                if (callback != null) {
                    callback.onServerStatusChecked(ip, isConnected);
                }
            }
        }).start();
    }

    private boolean isReachable(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 1500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        stopPeriodicChecks();
        super.onDestroy();
    }

    public interface ServerStatusCallback {
        void onServerStatusChecked(String ip, boolean isConnected);
    }
}