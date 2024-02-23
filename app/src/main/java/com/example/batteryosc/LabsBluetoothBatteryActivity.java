package com.example.batteryosc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SeslSwitchBar;
import androidx.appcompat.widget.SwitchCompat;

import dev.oneuiproject.oneui.layout.ToolbarLayout;

public class LabsBluetoothBatteryActivity extends AppCompatActivity {
    static final String PREFS = "BatOSC";
    static final String KEY_LABS_SEND_BLUETOOTH_BATTERY = "labs_sendBtBattery";
    private SharedPreferences mprefs;
    private SharedPreferences.Editor editor;

    boolean setting_sendBtBattery;

    ToolbarLayout toolbarLayout;
    SeslSwitchBar switchBar;

    private void init(){
        if (setting_sendBtBattery){
            switchBar.setChecked(true);
        }else {
            switchBar.setChecked(false);
        }

        switchBar.addOnSwitchChangeListener(new SeslSwitchBar.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
                if (isChecked){
                    setting_sendBtBattery = true;
                }else{
                    setting_sendBtBattery = false;
                }
                editor.putBoolean(KEY_LABS_SEND_BLUETOOTH_BATTERY, setting_sendBtBattery);
                editor.apply();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluetoothbattery);

        toolbarLayout = (ToolbarLayout)findViewById(R.id.toolbar_layout);
        toolbarLayout.setNavigationButtonAsBack();

        mprefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        editor = mprefs.edit();
        setting_sendBtBattery = mprefs.getBoolean(KEY_LABS_SEND_BLUETOOTH_BATTERY, false);

        switchBar = (SeslSwitchBar) findViewById(R.id.switchBar);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_send_bluetooth_battery, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.find) {
            Intent intent = new Intent(LabsBluetoothBatteryActivity.this, AppInfoActivity.class);
            startActivity(intent);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
    //</툴바 메뉴>
}
