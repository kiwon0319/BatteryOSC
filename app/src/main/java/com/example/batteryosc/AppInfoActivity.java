package com.example.batteryosc;

import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import dev.oneuiproject.oneui.layout.AppInfoLayout;
import dev.oneuiproject.oneui.layout.ToolbarLayout;

public class AppInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app_info);

        AppInfoLayout appInfoLayout = (AppInfoLayout) findViewById(R.id.layout_appInfo);
        appInfoLayout.setStatus(AppInfoLayout.NOT_UPDATEABLE);
        appInfoLayout.setExpandable(false);
    }
}
