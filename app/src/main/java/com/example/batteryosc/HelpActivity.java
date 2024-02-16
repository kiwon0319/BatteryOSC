package com.example.batteryosc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.oneuiproject.oneui.layout.ToolbarLayout;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpandable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
