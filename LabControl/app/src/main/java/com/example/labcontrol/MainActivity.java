package com.example.labcontrol;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final int SERVER_PORT = 41007;

    TextView currentSongText;
    TextView responseTextView;
    LinearLayout serverCheckboxContainer;
    Button echoButton, restartButton, shutdownButton,
            restoreButton, wakeButton, selectAllButton, playlistButton;
    ToggleButton musicToggleButton;

    private MusicService musicService;
    private boolean isMusicBound = false;
    private String padding = "\t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t";

    private ServerStatusService serverStatusService;
    private boolean isServerStatusBound = false;
    private Map<String, Boolean> serverConnectionStatus = new HashMap<>();

    static final Map<String, String> ipToMacMap =
            Stream.of(new String[][] {
                            { "192.168.88.2",  "50:81:40:2B:91:8D" },   // PRPC01
                            { "192.168.88.3",  "50:81:40:2B:7C:78" },   // PRPC02
                            { "192.168.88.4",  "50:81:40:2B:78:DD" },   // PRPC03
                            { "192.168.88.5",  "50:81:40:2B:7B:3D" },   // PRPC04
                            { "192.168.88.6",  "50:81:40:2B:79:91" },   // PRPC05
                            { "192.168.88.7",  "C8:5A:CF:0F:76:3D" },   // PRPC06
                            { "192.168.88.8",  "C8:5A:CF:0D:71:24" },   // PRPC07
                            { "192.168.88.9",  "C8:5A:CF:0F:B3:FF" },   // PRPC08
                            { "192.168.88.10", "C8:5A:CF:0E:2C:C4" },   // PRPC09
                            { "192.168.88.11", "C8:5A:CF:0F:7C:D0" },   // PRPC10
                            { "192.168.88.12", "C8:5A:CF:0D:71:3A" },   // PRPC11
                            { "192.168.88.13", "C8:5A:CF:0F:EE:01" },   // PRPC12
                            { "192.168.88.14", "C8:5A:CF:0E:1D:88" },   // PRPC13
                            { "192.168.88.15", "C8:5A:CF:0F:F0:1E" },   // PRPC14
                            { "192.168.88.16", "50:81:40:2B:7D:A4" },   // PRPC15
                            { "192.168.88.17", "C8:5A:CF:0E:2C:78" },   // PRPC16
                            { "192.168.88.18", "50:81:40:2B:87:F4" },   // PRPC17
                            { "192.168.88.19", "C8:5A:CF:0F:EC:11" },   // PRPC18
                            { "192.168.88.20", "C8:5A:CF:0F:7C:1F" },   // PRPC19
                            { "192.168.88.21", "C8:5A:CF:0D:71:2C" },   // PRPC20
                            { "192.168.88.22", "C8:5A:CF:0D:70:95" },   // PRPC21
                            { "192.168.88.23", "50:81:40:2B:5F:D0" },   // PRPC22
                            { "192.168.88.24", "50:81:40:2B:7A:0B" },   // PRPC23
                            { "192.168.88.25", "50:81:40:2B:8F:D3" },   // PRPC24
                            { "192.168.88.26", "50:81:40:2B:72:E0" },   // PRPC25
                            { "192.168.88.27", "50:81:40:2B:7A:74" },   // PRPC26
                            { "192.168.88.28", "C8:5A:CF:0F:7C:D4" }    // PRPC27DESK
                    })
                    .collect(Collectors.toMap(data -> data[0], data -> data[1],
                            (e1, e2) -> e1, LinkedHashMap::new));

    static final Map<String, String> ipToNameMap = Stream.of(new String[][] {
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
    }).collect(Collectors.toMap(data -> data[0], data -> data[1],
            (e1, e2) -> e1, LinkedHashMap::new));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.mainLayout);
        root.setAlpha(0f);
        root.animate().alpha(1f).setDuration(2000).start();

        currentSongText = findViewById(R.id.currentSongText);
        currentSongText.setSelected(true);

        responseTextView       = findViewById(R.id.responseTextView);
        serverCheckboxContainer = findViewById(R.id.serverCheckboxContainer);

        echoButton      = findViewById(R.id.echoButton);
        restartButton   = findViewById(R.id.restartButton);
        shutdownButton  = findViewById(R.id.shutdownButton);
        restoreButton   = findViewById(R.id.restoreButton);
        wakeButton      = findViewById(R.id.wolButton);
        selectAllButton = findViewById(R.id.selectAllButton);
        musicToggleButton     = findViewById(R.id.musicToggleButton);
        playlistButton  = findViewById(R.id.playlistButton);

        echoButton.setOnClickListener(this);
        restartButton.setOnClickListener(this);
        shutdownButton.setOnClickListener(this);
        restoreButton.setOnClickListener(this);
        wakeButton.setOnClickListener(this);
        selectAllButton.setOnClickListener(this);
        musicToggleButton.setOnClickListener(this);
        playlistButton.setOnClickListener(this);

        responseTextView.setMovementMethod(new ScrollingMovementMethod());
        populateCheckboxes();

        Intent startMusic = new Intent(this, MusicService.class);
        startService(startMusic);

        bindService(startMusic, musicConnection, Context.BIND_AUTO_CREATE);

        Intent serverIntent = new Intent(this, ServerStatusService.class);
        bindService(serverIntent, statusConnection, Context.BIND_AUTO_CREATE);

        for (String ip : ipToNameMap.keySet()) {
            serverConnectionStatus.put(ip, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMusicBound) {
            String track = musicService.getCurrentTrackName();
            currentSongText.setText(padding + "Now Playing: " + track + padding);
            musicToggleButton.setChecked(musicService.isPlaying());
        }
    }

    private void populateCheckboxes() {
        for (Map.Entry<String, String> entry : ipToNameMap.entrySet()) {
            CheckBox cb = new CheckBox(this);
            cb.setText(entry.getValue() + " (" + entry.getKey() + ")");
            cb.setTextColor(RandomColor.getRandomColor());
            cb.setTag(entry.getKey());
            cb.setChecked(false);
            serverCheckboxContainer.addView(cb);
        }
    }

    private final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isMusicBound = true;

            musicToggleButton.setChecked(musicService.isPlaying());

            String track = musicService.getCurrentTrackName();
            currentSongText.setText(padding + "Now Playing: " + track + padding);
            currentSongText.setSelected(true);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMusicBound = false;
        }
    };

    private final ServiceConnection statusConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerStatusService.LocalBinder binder = (ServerStatusService.LocalBinder) service;
            serverStatusService = binder.getService();
            isServerStatusBound = true;

            serverStatusService.setCallback((ip, isConnected, os) -> runOnUiThread(() -> {
                for (int i = 0; i < serverCheckboxContainer.getChildCount(); i++) {
                    serverConnectionStatus.put(ip, isConnected);
                    CheckBox cb = (CheckBox) serverCheckboxContainer.getChildAt(i);
                    String cbIp = cb.getTag().toString();
                    if (cbIp.equals(ip)) {
                        String label = ipToNameMap.get(ip) + " (" + ip + ")" + "  OS: " + os;
                        cb.setText(label + (isConnected ? " ✅" : " ❌"));
                    }
                }
            }));
            serverStatusService.startPeriodicChecks(
                    new ArrayList<>(ipToNameMap.keySet()), SERVER_PORT);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServerStatusBound = false;
        }
    };

    @Override
    public void onClick(View v) {
        if (v == echoButton || v == restartButton || v == shutdownButton || v == restoreButton) {
            String command = "";
            if (v == echoButton) command = "echo";
            if (v == restartButton) command = "restart";
            if (v == shutdownButton) command = "shutdown";
            if (v == restoreButton) command = "restore";

            responseTextView.setText("");
            boolean anyServerSelected = false;

            for (int i = 0; i < serverCheckboxContainer.getChildCount(); i++) {
                CheckBox cb = (CheckBox) serverCheckboxContainer.getChildAt(i);
                if (cb.isChecked() && cb.isEnabled()) {
                    anyServerSelected = true;
                    String ip = cb.getTag().toString();
                    new ServerCommandThread(this, ip, command).start();
                }
            }

            if (!anyServerSelected) {
                showMessage("ℹ️ No servers selected to execute " + command);
            }
            return;
        }

        if (v == selectAllButton) {
            for (int i = 0; i < serverCheckboxContainer.getChildCount(); i++) {
                CheckBox cb = (CheckBox) serverCheckboxContainer.getChildAt(i);
                cb.setChecked(!cb.isChecked());
            }
            return;
        }

        if (v == wakeButton) {
            responseTextView.setText("");
            boolean anySelected = false;

            for (int i = 0; i < serverCheckboxContainer.getChildCount(); i++) {
                CheckBox cb = (CheckBox) serverCheckboxContainer.getChildAt(i);
                if (cb.isChecked() && cb.isEnabled()) {
                    anySelected = true;
                    String ip = cb.getTag().toString();
                    String serverName = ipToNameMap.get(ip);

                    if (serverConnectionStatus.getOrDefault(ip, false)) {
                        showMessage("⚠️ " + serverName + " is already connected!");
                        continue;
                    }

                    String mac = ipToMacMap.get(ip);
                    if (mac == null) {
                        showMessage("❌ No MAC found for " + serverName + " (" + ip + ")");
                        continue;
                    }

                    try {
                        WakeOnLan.sendWolPacket(mac);
                        showMessage("✅ Sent WOL to " + serverName + " (" + ip + ")");
                    } catch (Exception e) {
                        showMessage("❌ Failed WOL to " + serverName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            if (!anySelected) {
                showMessage("ℹ️ No servers selected for Wake-on-LAN");
            }
            return;
        }

        if (v == musicToggleButton) {
            if (isMusicBound) {
                musicService.pauseOrResume();
                musicToggleButton.setChecked(musicService.isPlaying());
            }
            return;
        }

        if (v == playlistButton) {
            Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
            startActivity(intent);
            return;
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
    protected void onDestroy() {
        super.onDestroy();

        if (isMusicBound) {
            unbindService(musicConnection);
            isMusicBound = false;
        }
        if (isServerStatusBound) {
            serverStatusService.stopPeriodicChecks();
            unbindService(statusConnection);
            isServerStatusBound = false;
        }
    }
}