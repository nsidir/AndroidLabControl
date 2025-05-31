package com.example.labcontrol;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerStatusService extends Service {
    private final IBinder binder = new LocalBinder();
    private ServerStatusCallback callback;
    private Handler handler;
    private Runnable periodicCheck;
    private List<String> ipList;
    private int port;
    private static final long CHECK_INTERVAL_MS = 5000;

    final private ExecutorService executorService = Executors.newFixedThreadPool(27);

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
        for (String ip : ipList) {
            executorService.execute(() -> {
                boolean isConnected = isReachable(ip, port);
                String os = "Unknown";
                if(isConnected) {
                    Socket socket = new Socket();
                    try {
                        socket.connect(new InetSocketAddress(ip, port), 5000);

                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    out.write(("getos").getBytes());
                    out.flush();

                    while ((bytesRead = in.read(buffer)) != -1) {
                        os = new String(buffer, 0, bytesRead);
                    }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (callback != null) {
                    callback.onServerStatusChecked(ip, isConnected, os);
                }
            });
        }
    }


    // InetAddress inet = InetAddress.getByName(ipAddress);
    // inet.isReachable(5000)
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
        executorService.shutdownNow();
        super.onDestroy();
    }

    public interface ServerStatusCallback {
        void onServerStatusChecked(String ip, boolean isConnected, String os);
    }
}