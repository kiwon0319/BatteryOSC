package com.example.batteryosc.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

public class SensorUtil{
    private final String TAG = "trace";
    private SensorManager sm;
    public SensorUtil(Context ctx){
        sm = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
    }

    public List<Sensor> get_sensor_list(){
        List<Sensor> str = sm.getSensorList(Sensor.TYPE_ALL);
        Log.d(TAG, "get_sensor_list: " + str.toString());
        return str;
    }
}
