package com.example.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class splashScreen extends AppCompatActivity {
    private IntroPref introPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        introPref = new IntroPref(this);
        if (!introPref.isFirstTimeLaunch())
        {
            launchHomeScreen();
            finish();
        } else {
            Intent intent = new Intent(this, on_boarding_activity.class);
            startActivity(intent);
            finish();
        }
    }

    private void launchHomeScreen() {
        introPref.setIsFirstTimeLaunch(false);
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
        finish();
    }
}