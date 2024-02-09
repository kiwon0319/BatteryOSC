package com.example.batteryosc.util;

import static android.app.PendingIntent.getActivity;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class PrefsManager extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public PrefsManager(String t_prefsName, int t_context){
        prefs = getSharedPreferences(t_prefsName, t_context);
        editor = prefs.edit();
    }

    public SharedPreferences.Editor getEditor(){
        return editor;
    }
}
