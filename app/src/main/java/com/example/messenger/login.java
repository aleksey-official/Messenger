package com.example.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messenger.databinding.LoginBinding;
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class login extends AppCompatActivity {
    private LoginBinding binding;
    EditText emailText, pasText;
    TextView forgotpas, regBtn, welcome, text, textpas;
    Button logBtn;
    ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    ConstraintLayout loginlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null)
            {
                if (preferenceManager.getString(Constants.PASSCODE) != null) {
                    Intent intent = new Intent(login.this, checkpas.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        emailText = (EditText) findViewById(R.id.emailText);
        pasText = (EditText) findViewById(R.id.pasText);
        forgotpas = (TextView) findViewById(R.id.forgotPas);
        forgotpas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailText.getText().toString().equals("")){
                    Toast.makeText(login.this, "Введи почту и мы отправим письмо с подтверждением", Toast.LENGTH_SHORT).show();
                } else if (Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString()).matches()){
                    mAuth.sendPasswordResetEmail(emailText.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    emailText.setText(null);
                                    Toast.makeText(login.this, "Письмо отправлено на почту", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    emailText.setText(null);
                                    Toast.makeText(login.this, "Произошла ошибка. Повтори попытку", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        regBtn = (TextView) findViewById(R.id.logBtn);
        logBtn = (Button) findViewById(R.id.regBtn);
        progressBar = (ProgressBar) findViewById(R.id.progBar);
        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidSignInDetails())
                {
                    signIn();
                }
            }
        });
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this, phonereg.class);
                startActivity(intent);
            }
        });
       checktheme();
    }

    private void signIn()
    {
        loading(true);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(emailText.getText().toString(), pasText.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .whereEqualTo(Constants.KEY_EMAIL, emailText.getText().toString())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
                                    {
                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                        preferenceManager.putString(Constants.KEY_EMAIL, emailText.getText().toString());
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }else
                                    {
                                        loading(false);
                                        emailText.setText("");
                                        pasText.setText("");
                                        showToast("Неверная почта или пароль");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading(false);
                        emailText.setText("");
                        pasText.setText("");
                        showToast("Неверная почта или пароль");
                    }
                });
    }

    private void checktheme() {
        String theme = null;
        if (getIntent().getSerializableExtra("theme") != null){
            theme = (String) getIntent().getSerializableExtra("theme");
            preferenceManager.putString("usertheme", theme);
        }
        if (preferenceManager.getString("username") != null && preferenceManager.getString("usertheme").equals("dark")){
            welcome = (TextView) findViewById(R.id.welcome);
            text = (TextView) findViewById(R.id.text);
            textpas = (TextView) findViewById(R.id.textpas);
            loginlayout = (ConstraintLayout) findViewById(R.id.loginlayout);
            loginlayout.setBackgroundColor(getResources().getColor(R.color.black));
            welcome.setTextColor(getResources().getColor(R.color.white));
            text.setTextColor(getResources().getColor(R.color.white));
            textpas.setTextColor(getResources().getColor(R.color.white));
            emailText.setBackground(getResources().getDrawable(R.drawable.textboxbgdark));
            emailText.setTextColor(getResources().getColor(R.color.white));
            emailText.setHintTextColor(getResources().getColor(R.color.white));
            pasText.setBackground(getResources().getDrawable(R.drawable.textboxbgdark));
            pasText.setTextColor(getResources().getColor(R.color.white));
            pasText.setHintTextColor(getResources().getColor(R.color.white));
        }
    }

    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            logBtn.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else
        {
            logBtn.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void  showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails()
    {
        if (emailText.getText().toString().trim().isEmpty())
        {
            showToast("Введи почту");
            emailText.setError("Введи почту");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString()).matches())
        {
            showToast("Введи верный адрес почты");
            emailText.setError("Введи верный адрес почты");
            return false;
        }else if (pasText.getText().toString().trim().isEmpty()) {
            showToast("Введи пароль");
            pasText.setError("Введи пароль");
            return false;
        }else {
            return true;
        }
    }
}