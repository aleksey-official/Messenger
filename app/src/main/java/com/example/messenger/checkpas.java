package com.example.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;

import java.util.concurrent.Executor;

public class checkpas extends AppCompatActivity {
    Button one, two, three, four, five, six, seven, eight, nine, zero;
    ImageView delpasscode, oneview, twoview, threeview, fourview;
    Integer pasone, pastwo, pasthree, pasfour;
    TextView notification;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpas);
        preferenceManager = new PreferenceManager(getApplicationContext());
        one = (Button) findViewById(R.id.one);
        two = (Button) findViewById(R.id.two);
        three = (Button) findViewById(R.id.three);
        four = (Button) findViewById(R.id.four);
        five = (Button) findViewById(R.id.five);
        six = (Button) findViewById(R.id.six);
        seven = (Button) findViewById(R.id.seven);
        eight = (Button) findViewById(R.id.eight);
        nine = (Button) findViewById(R.id.nine);
        zero = (Button) findViewById(R.id.zero);
        oneview = (ImageView) findViewById(R.id.oneview);
        twoview = (ImageView) findViewById(R.id.twoview);
        threeview = (ImageView) findViewById(R.id.threeview);
        fourview = (ImageView) findViewById(R.id.fourview);
        notification = (TextView) findViewById(R.id.notification);
        if (preferenceManager.getBoolean(Constants.FINGER) == true){
            Executor executor = ContextCompat.getMainExecutor(checkpas.this);
            final BiometricPrompt biometricPrompt = new BiometricPrompt(checkpas.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                }
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Intent intent = new Intent(checkpas.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                }
            });
            final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Вход по отпечатку пальца")
                    .setDescription("Приложите палец к сканеру отпечатка пальца").setNegativeButtonText("Отмена").build();
            biometricPrompt.authenticate(promptInfo);
        }
        delpasscode = (ImageView) findViewById(R.id.delpasscode);
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 1;
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 1;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasthree == null){
                    pasthree = 1;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 1;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (pasone != null && pastwo != null && pasthree != null && pasfour != null){
                        if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                            notification.setTextColor(Color.GREEN);
                            notification.setText("Успешный вход");
                            Intent intent = new Intent(checkpas.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            notification.setTextColor(Color.RED);
                            notification.setText("Неверный пароль");
                            pasone = null;
                            pastwo = null;
                            pasthree = null;
                            pasfour = null;
                            oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                            twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                            threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                            fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        }
                    }
                }
            }
        });
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 2;
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 2;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasthree == null){
                    pasthree = 2;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 2;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 3;
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 3;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasthree == null){
                    pasthree = 3;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 3;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 4;
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 4;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasthree == null){
                    pasthree = 4;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 4;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 5;
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 5;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasthree == null){
                    pasthree = 5;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 5;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 6;
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 6;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasthree == null){
                    pasthree = 6;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 6;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 7;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 7;
                } else if (pasthree == null){
                    pasthree = 7;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 7;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 8;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 8;
                } else if (pasthree == null){
                    pasthree = 8;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 8;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 9;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 9;
                } else if (pasthree == null){
                    pasthree = 9;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 9;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasone == null){
                    pasone = 0;
                    oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pastwo == null){
                    pastwo = 0;
                    twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasthree == null){
                    pasthree = 0;
                    threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                } else if (pasfour == null){
                    pasfour = 0;
                    fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspointadd));
                    if (preferenceManager.getString(Constants.PASSCODE).equals(String.valueOf("" + pasone +  pastwo + pasthree + pasfour))){
                        notification.setTextColor(Color.GREEN);
                        notification.setText("Успешный вход");
                        Intent intent = new Intent(checkpas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        notification.setTextColor(Color.RED);
                        notification.setText("Неверный пароль");
                        pasone = null;
                        pastwo = null;
                        pasthree = null;
                        pasfour = null;
                        oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                        fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                    }
                }
            }
        });
        delpasscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasone = null;
                pastwo = null;
                pasthree = null;
                pasfour = null;
                oneview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                twoview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                threeview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
                fourview.setBackgroundDrawable(getResources().getDrawable(R.drawable.paspoint));
            }
        });
    }
}