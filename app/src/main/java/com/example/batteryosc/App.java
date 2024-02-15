package com.example.batteryosc;

import android.app.Application;
import android.content.SharedPreferences;

public class App extends Application {
    //Preferences name
    static final String PREFS = "BatOSC";

    //Preferences key
    static final String KEY_PORT = "port";
    static final String KEY_IP = "ip";
    static final String KEY_PRMT_BATTERYLEVEL = "batteryLevel";
    static final String KEY_PRMT_ISCHARGING = "isCharging";
    static final String KEY_PRMT_PATH = "prmtPath";
    static final String KEY_LABS_SHOW_PRAMETER_PATH = "labs_prmtPath";

    //Preferences and editor
    private SharedPreferences mprefs;
    private SharedPreferences.Editor editor;

    //Preferences values
    private String prmt_ipAddr,prmt_port, prmtNm_batteryLevel, prmtNm_isCharge, prmtNm_prmtPath;
    private boolean setting_showPrmtPath;


    @Override
    public void onCreate(){
        super.onCreate();
    }

    private void updatePrefs(){
        editor.putString(KEY_IP, prmt_ipAddr);
        editor.putString(KEY_PORT, prmt_port);
        editor.putString(KEY_PRMT_PATH, prmtNm_prmtPath);
        editor.putString(KEY_PRMT_BATTERYLEVEL, prmtNm_batteryLevel);
        editor.putString(KEY_PRMT_ISCHARGING, prmtNm_isCharge);

        editor.apply();
    }
}
