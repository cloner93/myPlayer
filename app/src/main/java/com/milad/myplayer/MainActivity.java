package com.milad.myplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.milad.myplayer.databinding.ViewPlayerBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String Broadcast_PLAY_NEW_AUDIO = "PlayNewAudio";
    private MediaPlayerService player;
    boolean serviceBound = false;

    audioStore store;

    ViewPlayerBinding mBinding;
    ArrayList<Song> songs;
    int cTime = 0;
    SeekBar seekBar;
    TextView songCurrentDurationLabel, songTotalDurationLabel;
    ImageView playPause;

    Utilities utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_player);
        mBinding = DataBindingUtil.setContentView(this, R.layout.view_player);
        seekBar = mBinding.seekBar;
        playPause = mBinding.playPause;
        songCurrentDurationLabel = mBinding.currentDuration;
        songTotalDurationLabel = mBinding.totalDuration;

        store = audioStore.getInstance(getApplicationContext());
        loadAudio();

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(3);
            }
        });
    }

    private void playAudio(int index) {
        if (!serviceBound) {
            store.storeAudioIndex(index);
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            store.storeAudioIndex(index);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void loadAudio() {
        findSong song = new findSong();
        songs = song.find(getApplicationContext());

        store.storeAudio(songs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }
}
