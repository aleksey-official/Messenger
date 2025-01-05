package com.example.messenger;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messenger.models.User;
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Profile extends AppCompatActivity {
    ImageButton backBtn;
    LinearLayout signout, supportBtn, date, addpas, delpas, addtouchpas, deltouchpas, recreatepas, delacc, supportbtn, worksend;
    ImageView profileImage;
    TextView statusText, nameText, emailText, namesettingText, phone, darkText, textstatus, textname, textphone, textemail, usernameText;
    SwitchCompat darkTheme;
    private User user;
    private PreferenceManager preferenceManager;
    private DocumentReference documentReference;
    private FirebaseAuth mAuth;
    String encodedImage;
    ConstraintLayout profileLayout;
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        preferenceManager = new PreferenceManager(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        backBtn = (ImageButton) findViewById(R.id.back);
        statusText = (TextView) findViewById(R.id.status);
        nameText = (TextView) findViewById(R.id.name);
        emailText = (TextView) findViewById(R.id.emailText);
        phone = (TextView) findViewById(R.id.phone);
        textstatus = (TextView) findViewById(R.id.status);
        textname = (TextView) findViewById(R.id.name);
        textphone = (TextView) findViewById(R.id.phone);
        textemail = (TextView) findViewById(R.id.emailText);
        usernameText = (TextView) findViewById(R.id.usernameText);
        date = (LinearLayout) findViewById(R.id.date);
        addpas = (LinearLayout) findViewById(R.id.addpas);
        delpas = (LinearLayout) findViewById(R.id.delpas);
        addtouchpas = (LinearLayout) findViewById(R.id.addtouchpas);
        deltouchpas = (LinearLayout) findViewById(R.id.deltouchpas);
        recreatepas = (LinearLayout) findViewById(R.id.recreatepas);
        delacc = (LinearLayout) findViewById(R.id.delacc);
        supportbtn = (LinearLayout) findViewById(R.id.sendsupporttextbtn);
        worksend = (LinearLayout) findViewById(R.id.worksend);
        worksend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.worklayout));
                EditText name = (EditText) dialog.findViewById(R.id.accpas);
                EditText surname = (EditText) dialog.findViewById(R.id.accpas2);
                EditText email = (EditText) dialog.findViewById(R.id.nameText2);
                EditText text = (EditText) dialog.findViewById(R.id.birthday2);
                Button file = (Button) dialog.findViewById(R.id.statusbtn2);
                Button sendBtn = (Button) dialog.findViewById(R.id.statusbtn);
                ImageView load = (ImageView) dialog.findViewById(R.id.imageView8);
                file.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        pickFile.launch(intent);
                        load.setBackgroundDrawable(getResources().getDrawable(R.drawable.doneicon));
                    }
                });
                name.setText(nameText.getText().toString());
                email.setText(emailText.getText().toString());
                sendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (name.getText().toString().trim().isEmpty()) {
                            name.setError("Введите имя");
                        } else if (surname.getText().toString().trim().isEmpty()) {
                            surname.setError("Введите фамилию");
                        } else if (email.getText().toString().trim().isEmpty()) {
                            email.setError("Введите почту");
                        } else if (text.getText().toString().trim().isEmpty()) {
                            text.setError("Введите дополнительную информацию");
                        } else {
                            sendBtn.setEnabled(false);
                            sendBtn.setText("Отправляется...");
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("message/rfc822");
                            i.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.EMAIL_DEVELOPER});
                            i.putExtra(Intent.EXTRA_SUBJECT, "Резюме");
                            i.putExtra(Intent.EXTRA_TEXT, "Имя пользователя: " + name.getText().toString() + "\nПочта пользователя: " + email.getText().toString() + "\nДополнительная информация: " + text.getText().toString());
                            try {
                                startActivity(Intent.createChooser(i, "Отправить Резюме..."));
                                dialog.dismiss();
                            } catch (android.content.ActivityNotFoundException ex) {
                                dialog.dismiss();
                                final Dialog dialog = new Dialog(Profile.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView((R.layout.uncorrectlayout));
                                TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                                correctText.setText("На этом устройстве не устоновлены почтовые клиенты");
                                Button save = (Button) dialog.findViewById(R.id.statusbtn);
                                save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                dialog.getWindow().setGravity(Gravity.CENTER);
                            }
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        supportbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.supportlayout));
                EditText name = (EditText) dialog.findViewById(R.id.accpas);
                EditText email = (EditText) dialog.findViewById(R.id.nameText2);
                EditText text = (EditText) dialog.findViewById(R.id.birthday2);
                Button sendBtn = (Button) dialog.findViewById(R.id.statusbtn);
                name.setText(nameText.getText().toString());
                email.setText(emailText.getText().toString());
                sendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (name.getText().toString().trim().isEmpty()){
                            name.setError("Введите имя");
                        } else if (email.getText().toString().trim().isEmpty()){
                            email.setError("Введите почту");
                        } else if (text.getText().toString().trim().isEmpty()){
                            text.setError("Введите сообщение");
                        } else {
                            sendBtn.setEnabled(false);
                            sendBtn.setText("Отправляется...");
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("message/rfc822");
                            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{Constants.EMAIL_DEVELOPER});
                            i.putExtra(Intent.EXTRA_SUBJECT, "Обращение в службу поддержки");
                            i.putExtra(Intent.EXTRA_TEXT   , "Имя пользователя: " + name.getText().toString() +"\nПочта пользователя: " + email.getText().toString() + "\nСообщение: " + text.getText().toString());
                            try {
                                startActivity(Intent.createChooser(i, "Отправить обращение..."));
                                dialog.dismiss();
                            } catch (android.content.ActivityNotFoundException ex) {
                                dialog.dismiss();
                                final Dialog dialog = new Dialog(Profile.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView((R.layout.uncorrectlayout));
                                TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                                correctText.setText("На этом устройстве не устоновлены почтовые клиенты");
                                Button save = (Button) dialog.findViewById(R.id.statusbtn);
                                save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                dialog.getWindow().setGravity(Gravity.CENTER);
                            }
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        delacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.delacclayout));
                EditText accpas = (EditText) dialog.findViewById(R.id.accpas);
                Button delacc = (Button) dialog.findViewById(R.id.statusbtn);
                delacc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (accpas.getText().toString().trim().isEmpty()){
                            accpas.setError("Введите пароль от аккаунта");
                        } else {
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(emailText.getText().toString(), accpas.getText().toString());
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            final Dialog dialog = new Dialog(Profile.this);
                                                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                                            dialog.setContentView((R.layout.delaccdonelayout));
                                                            dialog.setCancelable(false);
                                                            dialog.setCanceledOnTouchOutside(false);
                                                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                                                            DocumentReference documentReference =
                                                                    database.collection(Constants.KEY_COLLECTION_USERS).document(
                                                                            preferenceManager.getString(Constants.KEY_USER_ID)
                                                                    );
                                                            HashMap<String, Object> updates = new HashMap<>();
                                                            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                                                            documentReference.update(updates)
                                                                    .addOnSuccessListener(unused -> {
                                                                        preferenceManager.clear();
                                                                    })
                                                                    .addOnFailureListener(e -> showToast("Неудалось выйти из аккаунта"));
                                                            final Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent = new Intent(Profile.this, login.class);
                                                                    startActivity(intent);
                                                                }
                                                            }, 3000);
                                                            dialog.show();
                                                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                                            dialog.getWindow().setGravity(Gravity.CENTER);
                                                        }
                                                    }
                                                });
                                            } else {
                                                accpas.setError("Неверный пароль");
                                                accpas.setText("");
                                            }
                                        }
                                    });
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        recreatepas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.addpasscode));
                EditText lastpas, newpas, confurmnewpas;
                lastpas = (EditText) dialog.findViewById(R.id.accpas);
                newpas = (EditText) dialog.findViewById(R.id.nameText2);
                confurmnewpas = (EditText) dialog.findViewById(R.id.birthday2);
                Button save;
                save = (Button) dialog.findViewById(R.id.statusbtn);
                if (preferenceManager.getString(Constants.PASSCODE) == null){
                    lastpas.setVisibility(View.GONE);
                }
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (preferenceManager.getString(Constants.PASSCODE) == null){
                            if (newpas.getText().toString().trim().isEmpty()){
                                newpas.setError("Введи пароль");
                            } else if (confurmnewpas.getText().toString().trim().isEmpty()){
                                confurmnewpas.setError("Повтори пароль");
                            } else if (newpas.length() < 4 || newpas.length() > 4){
                                newpas.setError("Пароль должен содержать 4 символа");
                            } else if (!newpas.getText().toString().equals(confurmnewpas.getText().toString())){
                                newpas.setError("Пароли не совпадают");
                                confurmnewpas.setError("Пароли не совпадают");
                                newpas.setText("");
                                confurmnewpas.setText("");
                            } else{
                                dialog.dismiss();
                                preferenceManager.putString(Constants.PASSCODE, newpas.getText().toString());
                                final Dialog dialog = new Dialog(Profile.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView((R.layout.correctlayout));
                                TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                                correctText.setText("Пароль успешно создан");
                                Button save = (Button) dialog.findViewById(R.id.statusbtn);
                                save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                dialog.getWindow().setGravity(Gravity.CENTER);
                            }
                        } else {
                            if (lastpas.getText().toString().trim().isEmpty()){
                                lastpas.setError("Введи старый пароль");
                            } else if (newpas.getText().toString().trim().isEmpty()){
                                newpas.setError("Введи пароль");
                            } else if (confurmnewpas.getText().toString().trim().isEmpty()){
                                confurmnewpas.setError("Повтори пароль");
                            } else if (newpas.length() < 4 || newpas.length() > 4){
                                newpas.setError("Пароль должен содержать 4 символа");
                            } else if (!newpas.getText().toString().equals(confurmnewpas.getText().toString())){
                                newpas.setError("Пароли не совпадают");
                                confurmnewpas.setError("Пароли не совпадают");
                                newpas.setText("");
                                confurmnewpas.setText("");
                            } else{
                                if (preferenceManager.getString(Constants.PASSCODE).equals(lastpas.getText().toString())){
                                    dialog.dismiss();
                                    preferenceManager.putString(Constants.PASSCODE, newpas.getText().toString());
                                    final Dialog dialog = new Dialog(Profile.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setContentView((R.layout.correctlayout));
                                    TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                                    correctText.setText("Пароль успешно создан");
                                    Button save = (Button) dialog.findViewById(R.id.statusbtn);
                                    save.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.show();
                                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                    dialog.getWindow().setGravity(Gravity.CENTER);
                                } else {
                                    lastpas.setText("");
                                    lastpas.setError("Неверный пароль");
                                }
                            }
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        deltouchpas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferenceManager.getBoolean(Constants.FINGER) == true){
                    final Dialog dialog = new Dialog(Profile.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView((R.layout.correctlayout));
                    TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                    preferenceManager.putBoolean(Constants.FINGER, false);
                    correctText.setText("Отпечаток пальца успешно удалён");
                    Button save = (Button) dialog.findViewById(R.id.statusbtn);
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                } else {
                    final Dialog dialog = new Dialog(Profile.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView((R.layout.uncorrectlayout));
                    TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                    preferenceManager.putBoolean(Constants.FINGER, false);
                    correctText.setText("Вы не добавили вход по отпечатку пальца");
                    Button save = (Button) dialog.findViewById(R.id.statusbtn);
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                }
            }
        });
        addtouchpas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferenceManager.getString(Constants.PASSCODE) != null){
                    BiometricManager biometricManager = androidx.biometric.BiometricManager.from(Profile.this);
                    switch (biometricManager.canAuthenticate()) {

                        case BiometricManager.BIOMETRIC_SUCCESS:
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            final Dialog dialog = new Dialog(Profile.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView((R.layout.uncorrectlayout));
                            TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                            correctText.setText("Это устройство не поддерживает вход по отпечатку пальца");
                            Button save = (Button) dialog.findViewById(R.id.statusbtn);
                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setGravity(Gravity.CENTER);
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            final Dialog dialog1 = new Dialog(Profile.this);
                            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog1.setContentView((R.layout.uncorrectlayout));
                            TextView correctText1 = (TextView) dialog1.findViewById(R.id.textinfo);
                            correctText1.setText("Вход по отпечатку пальца в данный момент недоступен на этом устройстве");
                            Button save1 = (Button) dialog1.findViewById(R.id.statusbtn);
                            save1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog1.dismiss();
                                }
                            });
                            dialog1.show();
                            dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialog1.getWindow().setGravity(Gravity.CENTER);
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            final Dialog dialognew = new Dialog(Profile.this);
                            dialognew.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialognew.setContentView((R.layout.uncorrectlayout));
                            TextView correctTextnew = (TextView) dialognew.findViewById(R.id.textinfo);
                            correctTextnew.setText("На этом устройстве не сохранён отпечаток пальца, пожалуйста, проверьте настройки безопасности");
                            Button savenew = (Button) dialognew.findViewById(R.id.statusbtn);
                            savenew.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialognew.dismiss();
                                }
                            });
                            dialognew.show();
                            dialognew.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialognew.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialognew.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialognew.getWindow().setGravity(Gravity.CENTER);
                            break;
                    }
                    Executor executor = ContextCompat.getMainExecutor(Profile.this);
                    final BiometricPrompt biometricPrompt = new BiometricPrompt(Profile.this, executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                        }
                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            final Dialog dialog = new Dialog(Profile.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView((R.layout.correctlayout));
                            TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                            correctText.setText("Отпечаток успешно добавлен");
                            preferenceManager.putBoolean(Constants.FINGER, true);
                            Button save = (Button) dialog.findViewById(R.id.statusbtn);
                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setGravity(Gravity.CENTER);
                        }
                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                        }
                    });
                    final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Добавление отпечатка пальца")
                            .setDescription("Приложите палец к сканеру отпечатка пальца").setNegativeButtonText("Отмена").build();
                    biometricPrompt.authenticate(promptInfo);
                } else {
                    final Dialog dialog = new Dialog(Profile.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView((R.layout.uncorrectlayout));
                    TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                    correctText.setText("Для добавления входа по отпечатку пальца, для начала нужно добавить пароль");
                    Button save = (Button) dialog.findViewById(R.id.statusbtn);
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                }
            }
        });
        addpas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.addpasscode));
                EditText lastpas, newpas, confurmnewpas;
                lastpas = (EditText) dialog.findViewById(R.id.accpas);
                newpas = (EditText) dialog.findViewById(R.id.nameText2);
                confurmnewpas = (EditText) dialog.findViewById(R.id.birthday2);
                Button save;
                save = (Button) dialog.findViewById(R.id.statusbtn);
                if (preferenceManager.getString(Constants.PASSCODE) == null){
                    lastpas.setVisibility(View.GONE);
                }
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (preferenceManager.getString(Constants.PASSCODE) == null){
                            if (newpas.getText().toString().trim().isEmpty()){
                                newpas.setError("Введи пароль");
                            } else if (confurmnewpas.getText().toString().trim().isEmpty()){
                                confurmnewpas.setError("Повтори пароль");
                            } else if (newpas.length() < 4 || newpas.length() > 4){
                                newpas.setError("Пароль должен содержать 4 символа");
                            } else if (!newpas.getText().toString().equals(confurmnewpas.getText().toString())){
                                newpas.setError("Пароли не совпадают");
                                confurmnewpas.setError("Пароли не совпадают");
                                newpas.setText("");
                                confurmnewpas.setText("");
                            } else{
                                dialog.dismiss();
                                preferenceManager.putString(Constants.PASSCODE, newpas.getText().toString());
                                final Dialog dialog = new Dialog(Profile.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView((R.layout.correctlayout));
                                TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                                correctText.setText("Пароль успешно создан");
                                Button save = (Button) dialog.findViewById(R.id.statusbtn);
                                save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                dialog.getWindow().setGravity(Gravity.CENTER);
                            }
                        } else {
                            if (lastpas.getText().toString().trim().isEmpty()){
                                lastpas.setError("Введи старый пароль");
                            } else if (newpas.getText().toString().trim().isEmpty()){
                                newpas.setError("Введи пароль");
                            } else if (confurmnewpas.getText().toString().trim().isEmpty()){
                                confurmnewpas.setError("Повтори пароль");
                            } else if (!newpas.getText().toString().equals(confurmnewpas.getText().toString())){
                                newpas.setError("Пароли не совпадают");
                                confurmnewpas.setError("Пароли не совпадают");
                                newpas.setText("");
                                confurmnewpas.setText("");
                            } else{
                                if (preferenceManager.getString(Constants.PASSCODE).equals(lastpas.getText().toString())){
                                    dialog.dismiss();
                                    preferenceManager.putString(Constants.PASSCODE, newpas.getText().toString());
                                    final Dialog dialog = new Dialog(Profile.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setContentView((R.layout.correctlayout));
                                    TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                                    correctText.setText("Пароль успешно создан");
                                    Button save = (Button) dialog.findViewById(R.id.statusbtn);
                                    save.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.show();
                                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                    dialog.getWindow().setGravity(Gravity.CENTER);
                                } else {
                                    lastpas.setText("");
                                    lastpas.setError("Неверный пароль");
                                }
                            }
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        delpas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferenceManager.getString(Constants.PASSCODE) != null){
                    final Dialog dialog = new Dialog(Profile.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView((R.layout.delpasscode));
                    EditText lastpas;
                    lastpas = (EditText) dialog.findViewById(R.id.accpas);
                    Button save;
                    save = (Button) dialog.findViewById(R.id.statusbtn);
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (lastpas.getText().toString().trim().isEmpty()){
                                lastpas.setError("Введи пароль");
                            } else if (!lastpas.getText().toString().equals(preferenceManager.getString(Constants.PASSCODE))) {
                                lastpas.setError("Неверный пароль");
                                lastpas.setText("");
                            } else {
                                preferenceManager.putString(Constants.PASSCODE, null);
                                dialog.dismiss();
                                final Dialog dialog = new Dialog(Profile.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView((R.layout.correctlayout));
                                TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                                correctText.setText("Пароль успешно удалён");
                                Button save = (Button) dialog.findViewById(R.id.statusbtn);
                                save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                dialog.getWindow().setGravity(Gravity.CENTER);
                            }
                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                } else {
                    final Dialog dialog = new Dialog(Profile.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView((R.layout.uncorrectlayout));
                    TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                    correctText.setText("Вы не создали пароль");
                    Button save = (Button) dialog.findViewById(R.id.statusbtn);
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                }
            }
        });
        profileLayout = (ConstraintLayout) findViewById(R.id.profileLayout);
        darkTheme = (SwitchCompat) findViewById(R.id.textinput_helper_text);
        darkTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (darkTheme.isChecked()) {

                }
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.changebirhtday));
                ConstraintLayout changestatuslayout = dialog.findViewById(R.id.chagestatuslayout);
                EditText status = dialog.findViewById(R.id.status);
                TextView textsettingstatus = dialog.findViewById(R.id.textsettingstatus);
                TextView textstatussetting = dialog.findViewById(R.id.textstatussetting);
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID));
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            status.setText(documentSnapshot.getString(Constants.BIRTHDAY));
                        }
                    }
                });
                status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Calendar calendar = Calendar.getInstance ();
                        mYear = calendar.get ( Calendar.YEAR );
                        mMonth = calendar.get ( Calendar.MONTH );
                        mDay = calendar.get ( Calendar.DAY_OF_MONTH );
                        DatePickerDialog datePickerDialog = new DatePickerDialog ( Profile.this, new DatePickerDialog.OnDateSetListener () {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                status.setText ( dayOfMonth + "." + (month + 1) + "." + year );
                            }
                        }, mYear, mMonth, mDay );
                        datePickerDialog.show();
                    }
                });
                status.setText(preferenceManager.getString(Constants.STATUS));
                Button changenameBtn = dialog.findViewById(R.id.statusbtn);
                if (preferenceManager.getBoolean(Constants.THEME)){
                    changestatuslayout.setBackgroundColor(getResources().getColor(R.color.black));
                    textsettingstatus.setTextColor(getResources().getColor(R.color.white));
                    textstatussetting.setTextColor(getResources().getColor(R.color.white));
                    status.setBackgroundColor(getResources().getColor(R.color.black));
                    status.setTextColor(getResources().getColor(R.color.white));
                    status.setHintTextColor(getResources().getColor(R.color.white));
                    status.setBackground(getResources().getDrawable(R.drawable.textboxbgdark));
                }
                changenameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!status.getText().toString().trim().isEmpty()){
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID));
                            documentReference.update(Constants.BIRTHDAY, status.getText().toString());
                            preferenceManager.putString(Constants.BIRTHDAY, status.getText().toString());
                            loadUserData();
                            dialog.dismiss();
                            final Dialog dialog = new Dialog(Profile.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView((R.layout.correctlayout));
                            TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                            correctText.setText("День рождения успешно сохранён");
                            Button save = (Button) dialog.findViewById(R.id.statusbtn);
                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setGravity(Gravity.CENTER);
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        namesettingText = (TextView) findViewById(R.id.name);
        signout = (LinearLayout) findViewById(R.id.exo_icon);
        profileImage = (ImageView) findViewById(R.id.profilePhoto);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
        loadUserData();
        nameText.setText(preferenceManager.getString(Constants.KEY_NAME));
        usernameText.setText(preferenceManager.getString(Constants.KEY_NAME));
        emailText.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        statusText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.changestatuslayout));
                ConstraintLayout changestatuslayout = dialog.findViewById(R.id.chagestatuslayout);
                EditText status = dialog.findViewById(R.id.status);
                TextView textsettingstatus = dialog.findViewById(R.id.textsettingstatus);
                TextView textstatussetting = dialog.findViewById(R.id.textstatussetting);
                status.setText(preferenceManager.getString(Constants.STATUS));
                Button changenameBtn = dialog.findViewById(R.id.statusbtn);
                if (preferenceManager.getBoolean(Constants.THEME)){
                    changestatuslayout.setBackgroundColor(getResources().getColor(R.color.black));
                    textsettingstatus.setTextColor(getResources().getColor(R.color.white));
                    textstatussetting.setTextColor(getResources().getColor(R.color.white));
                    status.setBackgroundColor(getResources().getColor(R.color.black));
                    status.setTextColor(getResources().getColor(R.color.white));
                    status.setHintTextColor(getResources().getColor(R.color.white));
                    status.setBackground(getResources().getDrawable(R.drawable.textboxbgdark));
                }
                changenameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (status.getText().toString().trim().isEmpty()){
                            status.setError("Введи статус");
                        } else {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID));
                            documentReference.update(Constants.STATUS, status.getText().toString());
                            preferenceManager.putString(Constants.STATUS, status.getText().toString());
                            loadUserData();
                            dialog.dismiss();
                            final Dialog dialog = new Dialog(Profile.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView((R.layout.correctlayout));
                            TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                            correctText.setText("Статус успешно сохранён");
                            Button save = (Button) dialog.findViewById(R.id.statusbtn);
                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setGravity(Gravity.CENTER);
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        nameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.changenamelayout));
                EditText name = dialog.findViewById(R.id.status);
                name.setText(preferenceManager.getString(Constants.KEY_NAME));
                ConstraintLayout changestatuslayout = dialog.findViewById(R.id.changenamelayout);
                TextView textsettingstatus = dialog.findViewById(R.id.textsettingstatus);
                TextView textstatussetting = dialog.findViewById(R.id.textstatussetting);
                Button changenameBtn = dialog.findViewById(R.id.statusbtn);
                if (preferenceManager.getBoolean(Constants.THEME)){
                    changestatuslayout.setBackgroundColor(getResources().getColor(R.color.black));
                    textsettingstatus.setTextColor(getResources().getColor(R.color.white));
                    textstatussetting.setTextColor(getResources().getColor(R.color.white));
                    name.setBackgroundColor(getResources().getColor(R.color.black));
                    name.setTextColor(getResources().getColor(R.color.white));
                    name.setHintTextColor(getResources().getColor(R.color.white));
                    name.setBackground(getResources().getDrawable(R.drawable.textboxbgdark));
                }
                changenameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (name.getText().toString().trim().isEmpty()){
                            name.setError("Введи имя");
                        } else {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID));
                            documentReference.update(Constants.KEY_NAME, name.getText().toString());
                            preferenceManager.putString(Constants.KEY_NAME, name.getText().toString());
                            loadUserData();
                            dialog.dismiss();
                            final Dialog dialog = new Dialog(Profile.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView((R.layout.correctlayout));
                            TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                            correctText.setText("Имя успешно сохранено");
                            Button save = (Button) dialog.findViewById(R.id.statusbtn);
                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setGravity(Gravity.CENTER);
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        backBtn.setOnClickListener(view -> onBackPressed());
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    private void  showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signOut()
    {
        Intent intent = new Intent(getApplicationContext(), login.class);
        if (preferenceManager.getBoolean(Constants.THEME)){
            intent.putExtra("theme", "dark");
        }
        showToast("Выход из аккаунта...");
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean("theme", preferenceManager.getBoolean(Constants.THEME));
        editor.apply();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(intent);
                })
                .addOnFailureListener(e -> showToast("Неудалось выйти из аккаунта"));
    }

    private void loadUserData()
    {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        profileImage.setImageBitmap(bitmap);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        nameText.setText(preferenceManager.getString(Constants.KEY_NAME));
        namesettingText.setText(preferenceManager.getString(Constants.KEY_NAME));
        emailText.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        statusText.setText(preferenceManager.getString(Constants.STATUS));
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    phone.setText(documentSnapshot.getString(Constants.KEY_USER_PHONE));
                    statusText.setText(documentSnapshot.getString(Constants.STATUS));
                    nameText.setText(documentSnapshot.getString(Constants.KEY_NAME));
                }
            }
        });
    }

    private String encodeImage(Bitmap bitmap)
    {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
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
                            profileImage.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID));
                            documentReference.update(Constants.KEY_IMAGE, encodedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri fileUri = result.getData().getData();
                    }
                }
            }
    );
}