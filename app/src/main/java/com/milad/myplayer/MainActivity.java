package com.milad.myplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.milad.myplayer.databinding.AudioListBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //<editor-fold desc="variable">
    private static final String TAG = "tag";
    private AudioListBinding mBinding;
    private ArrayList<Song> songs;
    private audioStore store;
    private ConstraintLayout bottom_sheet;
    private BottomSheetBehavior sheetBehavior;

    public static final String Broadcast_PLAY_NEW_AUDIO = "PlayNewAudio";
    private MediaPlayerService player;
    boolean serviceBound = false;
    public MyHandlers handlers;
    int rotationAngle = 0;
    //</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.audio_list);
        handlers = new MyHandlers();
        mBinding.bottomSheet.setHandlers(handlers);

        initBottomSheet();
        initAudio();
        initRecyclerView();
    }

    //<editor-fold desc="init">
    private void initBottomSheet() {
        bottom_sheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        rotateExpandIcon(0);
                        break;
                    }
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        rotateExpandIcon(180);
                        // TODO: 2/13/20_4:27 PM -> change bottom sheet top view
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    private void initAudio() {
        findSong song = new findSong();
        songs = song.find(getApplicationContext());
        store = audioStore.getInstance(getApplicationContext());
        store.storeAudio(songs);
    }

    private void initRecyclerView() {
        if (songs.size() > 0) {
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            RecyclerView_Adapter adapter = new RecyclerView_Adapter(songs, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    playAudio(index);
                }
            }));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                });
            }
        }
    }
    //</editor-fold>

    void rotateExpandIcon(int radius) {
        rotationAngle = radius == 0 ? 180 : 0;  //toggle

        ImageView image = findViewById(R.id.expand);
        image.animate().rotation(rotationAngle).setDuration(200).start();
    }

    private void playAudio(int index) {
        if (!serviceBound) {
            store.storeAudioIndex(index);
            Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            store.storeAudioIndex(index);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    //<editor-fold desc="InstanceState">
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
    //</editor-fold>

    //<editor-fold desc="ServiceConnection">
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
    //</editor-fold>

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    //<editor-fold desc="Player Click Listener">
    public class MyHandlers implements playerOnClick {
        @Override
        public void play() {
            Log.d(TAG, "play: ");
        }

        @Override
        public void nextSong() {

        }

        @Override
        public void previousSong() {

        }

        @Override
        public void star() {

        }

        @Override
        public void listItem() {

        }

        @Override
        public void showBottomSheet() {
            if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }
    //</editor-fold>
}
