package com.example.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class phonereg extends AppCompatActivity {
    TextView back;
    Button next;
    ImageButton backClick;
    CountryCodePicker countryCodePicker;
    EditText phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonereg);
        back = (TextView) findViewById(R.id.logBtn);
        next = (Button) findViewById(R.id.regBtn);
        backClick = (ImageButton) findViewById(R.id.backBtnclick);
        countryCodePicker = (CountryCodePicker) findViewById(R.id.countryCodePicker);
        phone = (EditText) findViewById(R.id.phoneuser);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        backClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phone.getText().toString().trim().isEmpty()){
                    phone.setError("Введите номер телефона");
                } else if (!Patterns.PHONE.matcher(countryCodePicker.getSelectedCountryCode() + phone.getText().toString()).matches())
                {
                    phone.setError("Введи верный номер телефона");
                } else {
                    Intent intent = new Intent(phonereg.this, registration.class);
                    intent.putExtra("phone", countryCodePicker.getSelectedCountryCodeWithPlus() + phone.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }
}