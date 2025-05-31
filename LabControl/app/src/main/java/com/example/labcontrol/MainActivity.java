package com.example.labcontrol;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MediaPlayer mediaPlayer;

    private int getRandomColor() {
        Random random = new Random();

        int r, g, b;

        // only bright colors
        do {
            r = random.nextInt(256);
            g = random.nextInt(256);
            b = random.nextInt(256);
        } while ((r * 0.299 + g * 0.587 + b * 0.114) < 186);

        return Color.rgb(r, g, b);
    }


    final int SERVER_PORT = 41007;

    TextView responseTextView;
    LinearLayout serverCheckboxContainer;
    Button echoButton, restartButton, shutdownButton, restoreButton;

    private ServerStatusService serverStatusService;
    private boolean isBound = false;

    static final Map<String, String> ipToNameMap = Stream.of(new String[][] {
            { "100.110.22.5", "Test" },
            { "192.168.88.2", "PRPC01" },
            { "192.168.88.3", "PRPC02" },
            { "192.168.88.4", "PRPC03" },
            { "192.168.88.5", "PRPC04" },
            { "192.168.88.6", "PRPC05" },
            { "192.168.88.7", "PRPC06" },
            { "192.168.88.8", "PRPC07" },
            { "192.168.88.9", "PRPC08" },
            { "192.168.88.10", "PRPC09" },
            { "192.168.88.11", "PRPC10" },
            { "192.168.88.12", "PRPC11" },
            { "192.168.88.13", "PRPC12" },
            { "192.168.88.14", "PRPC13" },
            { "192.168.88.15", "PRPC14" },
            { "192.168.88.16", "PRPC15" },
            { "192.168.88.17", "PRPC16" },
            { "192.168.88.18", "PRPC17" },
            { "192.168.88.19", "PRPC18" },
            { "192.168.88.20", "PRPC19" },
            { "192.168.88.21", "PRPC20" },
            { "192.168.88.22", "PRPC21" },
            { "192.168.88.23", "PRPC22" },
            { "192.168.88.24", "PRPC23" },
            { "192.168.88.25", "PRPC24" },
            { "192.168.88.26", "PRPC25" },
            { "192.168.88.27", "PRPC26" },
            { "192.168.88.28", "PRPC27DESK" }
    }).collect(Collectors.toMap(data -> data[0], data -> data[1], (e1, e2) -> e1, LinkedHashMap::new));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.mainLayout);
        root.setAlpha(0f);
        root.animate().alpha(1f).setDuration(2000).start();

        responseTextView = findViewById(R.id.responseTextView);
        serverCheckboxContainer = findViewById(R.id.serverCheckboxContainer);

        echoButton = findViewById(R.id.echoButton);
        restartButton = findViewById(R.id.restartButton);
        shutdownButton = findViewById(R.id.shutdownButton);
        restoreButton = findViewById(R.id.restoreButton);

        echoButton.setOnClickListener(this);
        restartButton.setOnClickListener(this);
        shutdownButton.setOnClickListener(this);
        restoreButton.setOnClickListener(this);

        responseTextView.setMovementMethod(new ScrollingMovementMethod());

        populateCheckboxes();

        Intent intent = new Intent(this, ServerStatusService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void populateCheckboxes() {
        for (Map.Entry<String, String> entry : ipToNameMap.entrySet()) {
            CheckBox cb = new CheckBox(this);
            cb.setText(entry.getValue() + " (" + entry.getKey() + ")");
            cb.setTextColor(getRandomColor());
            cb.setTag(entry.getKey());
            cb.setChecked(false);
            serverCheckboxContainer.addView(cb);
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerStatusService.LocalBinder binder = (ServerStatusService.LocalBinder) service;
            serverStatusService = binder.getService();
            isBound = true;

            serverStatusService.setCallback((ip, isConnected, os) -> runOnUiThread(() -> {
                for (int i = 0; i < serverCheckboxContainer.getChildCount(); i++) {
                    CheckBox cb = (CheckBox) serverCheckboxContainer.getChildAt(i);
                    String cbIp = cb.getTag().toString();
                    if (cbIp.equals(ip)) {
                        //cb.setEnabled(isConnected);
                        String label = ipToNameMap.get(ip) + " (" + ip + ")" + " OS: " + os;
                        cb.setText(label + (isConnected ? " ✅" : " ❌"));
                    }
                }
            }));

            serverStatusService.startPeriodicChecks(new ArrayList<>(ipToNameMap.keySet()), SERVER_PORT);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


    @Override
    public void onClick(View v) {
        String command = "";
        if (v == echoButton) command = "echo";
        if (v == restartButton) command = "restart";
        if (v == shutdownButton) command = "shutdown";
        if (v == restoreButton) command = "restore";

        responseTextView.setText("");

        for (int i = 0; i < serverCheckboxContainer.getChildCount(); i++) {
            CheckBox cb = (CheckBox) serverCheckboxContainer.getChildAt(i);
            if (cb.isChecked() && cb.isEnabled()) {
                String ip = cb.getTag().toString();
                new ServerCommandThread(this, ip, command).start();
            }
        }
    }

    public void showMessage(final String msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("Message", msg);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String content = msg.getData().getString("Message");
            responseTextView.append(content + "\n");
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isBound) {
            serverStatusService.stopPeriodicChecks();
            unbindService(connection);
            isBound = false;
        }
    }
}
