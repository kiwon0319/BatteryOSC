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
    static final String KEY_LABS_SEND_BLUETOOTH_BATTERY = "labs_sendBtBattery";

    //Preferences and editor
    private SharedPreferences mprefs;
    private SharedPreferences.Editor editor;

    //Preferences values
    public String prmt_ipAddr,prmt_port, prmtNm_batteryLevel, prmtNm_isCharge, prmtNm_prmtPath;
    public boolean setting_showPrmtPath;
    public boolean setting_sendBtBattery;


    @Override
    public void onCreate(){
        super.onCreate();

        init();
    }

    public void init(){
        mprefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        editor = mprefs.edit();

        prmt_ipAddr = mprefs.getString(KEY_IP, "ip");
        prmt_port = mprefs.getString(KEY_PORT, "port");
        prmtNm_prmtPath = mprefs.getString(KEY_PRMT_PATH, "/avatar/parameters/");
        prmtNm_batteryLevel = mprefs.getString(KEY_PRMT_BATTERYLEVEL, "battery_level");
        prmtNm_isCharge = mprefs.getString(KEY_PRMT_ISCHARGING, "is_charging");
        setting_showPrmtPath = mprefs.getBoolean(KEY_LABS_SHOW_PRAMETER_PATH, false);
        setting_sendBtBattery = mprefs.getBoolean(KEY_LABS_SEND_BLUETOOTH_BATTERY, false);
    }

    public void updatePrefs(){
        editor.putString(KEY_IP, prmt_ipAddr);
        editor.putString(KEY_PORT, prmt_port);
        editor.putString(KEY_PRMT_PATH, prmtNm_prmtPath);
        editor.putString(KEY_PRMT_BATTERYLEVEL, prmtNm_batteryLevel);
        editor.putString(KEY_PRMT_ISCHARGING, prmtNm_isCharge);
        editor.putBoolean(KEY_LABS_SHOW_PRAMETER_PATH, setting_showPrmtPath);
        editor.putBoolean(KEY_LABS_SEND_BLUETOOTH_BATTERY, setting_sendBtBattery);

        editor.apply();
    }
}
