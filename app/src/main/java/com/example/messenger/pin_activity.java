package com.example.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;

public class pin_activity extends AppCompatActivity {
    Button add, skip;
    EditText one, two, three, four;
    private PreferenceManager preferenceManager;
    TextView infotext;
    String pas = "", confPas = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        add = (Button) findViewById(R.id.addpas);
        skip = (Button) findViewById(R.id.skippas);
        one = (EditText) findViewById(R.id.pasText2);
        two = (EditText) findViewById(R.id.pasText3);
        three = (EditText) findViewById(R.id.pasText4);
        four = (EditText) findViewById(R.id.pasText5);
        infotext = (TextView) findViewById(R.id.infotext);
        if (getIntent().getSerializableExtra("passcode") != null){
            if (getIntent().getSerializableExtra("passcode").equals("false")){
                infotext.setText("Пароли не совпали. Повторите попытку");
            }
        }
        preferenceManager = new PreferenceManager(getApplicationContext());
        one.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                two.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        two.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                three.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        three.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                four.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!one.getText().toString().trim().isEmpty() && !two.getText().toString().trim().isEmpty() && !three.getText().toString().trim().isEmpty() && !four.getText().toString().trim().isEmpty()){
                    preferenceManager.putString(Constants.PASSCODE, one.getText().toString() + two.getText().toString() + three.getText().toString() + four.getText().toString());
                    Intent intent = new Intent(pin_activity.this, touchactivity.class);
                    startActivity(intent);
                }
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(pin_activity.this, touchactivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}