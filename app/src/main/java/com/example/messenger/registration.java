package com.example.messenger;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

public class registration extends AppCompatActivity {

    EditText nameText, emailText, pasText, confpasText, userstatus, birthday;
    TextView logBtn, photoText, welcome, textnameusername, text, phonetextuser, textpas, textconfpas, startphonetext;
    Button regBtn;
    ImageView imageProfile, back;
    ProgressBar progressBar;
    String encodedImage;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    ConstraintLayout registrationlayout;
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        preferenceManager = new PreferenceManager(getApplicationContext());
        nameText = (EditText) findViewById(R.id.nameText);
        emailText = (EditText) findViewById(R.id.emailText);
        pasText = (EditText) findViewById(R.id.pasText);
        confpasText = (EditText) findViewById(R.id.confpasText);
        userstatus = (EditText) findViewById(R.id.userstatus);
        birthday = (EditText) findViewById(R.id.birthday);
        photoText = (TextView) findViewById(R.id.phototext);
        regBtn = (Button) findViewById(R.id.regBtn);
        imageProfile = (ImageView) findViewById(R.id.imageProfile);
        back = (ImageView) findViewById(R.id.backBtnclick2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progBar);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidSignUpDetails())
                {
                    signUp();
                }
            }
        });
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance ();
                mYear = calendar.get ( Calendar.YEAR );
                mMonth = calendar.get ( Calendar.MONTH );
                mDay = calendar.get ( Calendar.DAY_OF_MONTH );
                DatePickerDialog datePickerDialog = new DatePickerDialog ( registration.this, new DatePickerDialog.OnDateSetListener () {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        birthday.setText ( dayOfMonth + "." + (month + 1) + "." + year );
                    }
                }, mYear, mMonth, mDay );
                datePickerDialog.show();
            }
        });
        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
    }
    private void  showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void  signUp()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, nameText.getText().toString());
        user.put(Constants.KEY_EMAIL, emailText.getText().toString());
        user.put(Constants.BIRTHDAY, birthday.getText().toString());
        user.put(Constants.STATUS, userstatus.getText().toString());
        user.put(Constants.KEY_USER_PHONE, getIntent().getSerializableExtra("phone").toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    mAuth.createUserWithEmailAndPassword(emailText.getText().toString(), pasText.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    loading(false);
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                    preferenceManager.putString(Constants.STATUS, userstatus.getText().toString());
                                    preferenceManager.putString(Constants.BIRTHDAY, birthday.getText().toString());
                                    preferenceManager.putString(Constants.KEY_NAME, nameText.getText().toString());
                                    preferenceManager.putString(Constants.KEY_USER_PHONE, String.valueOf(getIntent().getSerializableExtra("phone").toString()));
                                    preferenceManager.putString(Constants.KEY_EMAIL, emailText.getText().toString());
                                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                                    Intent intent = new Intent(getApplicationContext(), pin_activity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loading(false);
                                    Toast.makeText(registration.this, "Произошла ошибка. Повтори попытку", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(exception -> {

                });
    }

    private String encodeImage(Bitmap bitmap)
    {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            imageProfile.setImageBitmap(bitmap);
                            photoText.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails()
    {
        if (encodedImage == null)
        {
            showToast("Выбери фото профиля");
            return false;
        }else if (nameText.getText().toString().trim().isEmpty())
        {
            showToast("Введи имя");
            nameText.setError("Введи имя");
            return false;
        }else if (emailText.getText().toString().trim().isEmpty())
        {
            showToast("Введи почту");
            emailText.setError("Введи почту");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString()).matches())
        {
            showToast("Введи верный адрес почты");
            emailText.setError("Введи верный адрес почты");
            return false;
        } else if (pasText.getText().toString().trim().isEmpty()) {
            showToast("Введи пароль");
            pasText.setError("Введи пароль");
            return false;
        } else if (confpasText.getText().toString().trim().isEmpty()) {
            showToast("Повтори пароль");
            confpasText.setError("Повтори пароль");
            return false;
        } else if (userstatus.getText().toString().trim().isEmpty()) {
            showToast("Введи статус");
            userstatus.setError("Введи статус");
            return false;
        } else if (birthday.getText().toString().trim().isEmpty()) {
            showToast("Введи день рождения");
            userstatus.setError("Введи день рождения");
            return false;
        } else if (!pasText.getText().toString().equals(confpasText.getText().toString())) {
            showToast("Пароли не совпадают");
            pasText.setText("");
            confpasText.setText("");
            pasText.setError("");
            confpasText.setError("");
            return false;
        } else if (pasText.length() < 6){
            pasText.setError("Пароль должен содержать не менее 6 символов");
            return false;
        } else {
            return true;
        }
    }
    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            regBtn.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else
        {
            regBtn.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}