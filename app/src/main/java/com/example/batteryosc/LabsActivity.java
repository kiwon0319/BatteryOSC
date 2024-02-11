package com.example.batteryosc;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SeslToggleSwitch;

public class LabsActivity extends AppCompatActivity {

    static final String PREFS = "BatOSC";
    static final String KEY_LABS_SHOW_PRAMETER_PATH = "labs_prmtPath";

    CardView card_prmtPath;

    private SharedPreferences mprefs;
    private SharedPreferences.Editor editor;

    boolean setting_showPrmtPath;

    //<커스텀 함수>
    private void init(){
        card_prmtPath = (CardView) findViewById(R.id.card_prmtPath);
        SeslToggleSwitch toggleSwitch = (SeslToggleSwitch) card_prmtPath.findViewById(R.id.sesl_cardview_switch);


        if(setting_showPrmtPath){
            toggleSwitch.setChecked(true);
        }else{
            toggleSwitch.setChecked(false);
        }
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
