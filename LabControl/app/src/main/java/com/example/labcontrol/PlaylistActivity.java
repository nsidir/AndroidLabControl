package com.example.labcontrol;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaylistActivity extends AppCompatActivity {

    private ListView playlistListView;
    private TextView titleText;
    private Button backButton;

    private ArrayList<Integer> rawResourceIds;
    private ArrayList<String> rawResourceNames;
    private ArrayList<String> displayNames;

    private static Map<Integer, String> displayMap = new HashMap<>();

    private MusicService musicService;
    private boolean isMusicBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        displayMap = MusicService.idToNameMap;

        playlistListView   = findViewById(R.id.playlistListView);
        titleText          = findViewById(R.id.titleText);
        backButton = findViewById(R.id.backButton);

        titleText.setSelected(true);

        rawResourceIds   = new ArrayList<>();
        rawResourceNames = new ArrayList<>();
        displayNames     = new ArrayList<>();

        Field[] rawFields = R.raw.class.getFields();
        for (Field f : rawFields) {
            try {
                int resId = f.getInt(null);
                String rawName = f.getName();
                rawResourceIds.add(resId);
                rawResourceNames.add(rawName);

                if (displayMap.containsKey(resId)) {
                    displayNames.add(displayMap.get(resId));
                } else {
                    displayNames.add(rawName);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item,
                R.id.text1,
                displayNames
        ){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                TextView tv = row.findViewById(R.id.text1);
                tv.setTextColor(RandomColor.getRandomColor());
                return row;
            }
        };
        playlistListView.setAdapter(adapter);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);

        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                if (!isMusicBound) return;

                int chosenResId = rawResourceIds.get(position);
                musicService.play(chosenResId);

                String nowPlaying = displayNames.get(position);
                titleText.setText(nowPlaying);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // pop back to MainActivity
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isMusicBound) {
            unbindService(musicConnection);
            isMusicBound = false;
        }
    }

    private final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isMusicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMusicBound = false;
        }
    };
}