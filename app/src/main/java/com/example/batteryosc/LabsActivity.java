package com.example.batteryosc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LabsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labs);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
