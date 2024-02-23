package com.example.batteryosc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SeslToggleSwitch;

import com.example.batteryosc.widget.CardView;

public class LabsActivity extends AppCompatActivity {

    static final String PREFS = "BatOSC";
    static final String KEY_LABS_SHOW_PRAMETER_PATH = "labs_prmtPath";
    static final String KEY_LABS_SEND_BLUETOOTH_BATTERY = "labs_sendBtBattery";

    CardView card_prmtPath;
    CardView card_btBattery;

    private SharedPreferences mprefs;
    private SharedPreferences.Editor editor;

    boolean setting_showPrmtPath;
    boolean setting_sendBtBattery;


    //<커스텀 함수>
    private void init(){


        card_prmtPath = (CardView) findViewById(R.id.card_prmtPath);
        SeslToggleSwitch toggleSwitch = (SeslToggleSwitch) card_prmtPath.findViewById(R.id.sesl_cardview_switch);


        if(setting_showPrmtPath){
            toggleSwitch.setChecked(true);
        }else{
            toggleSwitch.setChecked(false);
        }

        card_btBattery = (CardView) findViewById(R.id.card_btBattery);
        SeslToggleSwitch toggle_sendBtbattery = card_btBattery.findViewById(R.id.sesl_cardview_switch);

        if (setting_sendBtBattery){
            toggle_sendBtbattery.setChecked(true);
        }else {
            toggle_sendBtbattery.setChecked(false);
        }

        toggle_sendBtbattery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    setting_sendBtBattery = true;
                }else {
                    setting_sendBtBattery = false;
                }
                editor.putBoolean(KEY_LABS_SEND_BLUETOOTH_BATTERY, setting_sendBtBattery);
                editor.apply();
            }
        });
        card_btBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LabsActivity.this, LabsBluetoothBatteryActivity.class);
                startActivity(intent);
            }
        });
    }

    //</커스텀 함수>

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labs);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        card_prmtPath = (CardView) findViewById(R.id.card_prmtPath);

        //데이터 초기화
        mprefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        editor = mprefs.edit();

        Log.d("trace", "loaded saved data: " + mprefs.getAll());

        setting_showPrmtPath = mprefs.getBoolean(KEY_LABS_SHOW_PRAMETER_PATH, false);
        setting_sendBtBattery = mprefs.getBoolean(KEY_LABS_SEND_BLUETOOTH_BATTERY, false);

        init();

        SeslToggleSwitch toggleSwitch = card_prmtPath.findViewById(R.id.sesl_cardview_switch);
        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editor.putBoolean(KEY_LABS_SHOW_PRAMETER_PATH, true);
                }else{
                    editor.putBoolean(KEY_LABS_SHOW_PRAMETER_PATH, false);
                }
                editor.apply();
            }
        });

    }

}
