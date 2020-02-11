package com.milad.myplayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class audioStore {
    private static audioStore INSTANCE;
    private Context context;

    private final String STORAGE = "STORAGE";
    private SharedPreferences preferences;

    private audioStore(Context context) {
        this.context = context;
    }

    public static audioStore getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (audioStore.class) {
                if (INSTANCE == null) {
                    INSTANCE = new audioStore(context);
                }
            }
        }
        return INSTANCE;
    }

    //<editor-fold desc="Actions">
    public void storeAudio(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioList", json);
        editor.apply();
    }

    public ArrayList<Song> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = preferences.getString("audioList", null);
        Type type = new TypeToken<ArrayList<Song>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);//return -1 if no data found
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        editor.commit();
    }
    //</editor-fold>
}
