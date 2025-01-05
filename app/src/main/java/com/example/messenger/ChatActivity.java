package com.example.messenger;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.adapters.ChatAdapter;
import com.example.messenger.databinding.ItemContainerSentMessageBinding;
import com.example.messenger.listeners.UserListeners;
import com.example.messenger.models.ChatMessage;
import com.example.messenger.models.User;
import com.example.messenger.network.ApiClient;
import com.example.messenger.network.ApiService;
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChatActivity extends BaseActivity implements UserListeners {

    private User receivedUser;
    ImageButton backBtn, sendBtn, audiocall, videocall, addFile, delbtn, resentBtn, resendinchat, delresendinchat;
    TextView nameText, availabilityText, textsettingstatus, resendmsg;
    ImageView profileImage;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    private Boolean isReceiverTyping = false;
    RecyclerView chatRecyclerView;
    EditText inputMessage;
    ProgressBar progressBar;
    private DocumentReference documentReference;
    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;
    static int PERMISSION_CODE = 100;
    String encodedImage;
    String encodedVideo;
    View headerBackground;
    String encodedFile;
    String SELECT_VIDEO = null;
    ConstraintLayout chatlayout;
    ConstraintLayout sendfilelayout;
    ConstraintLayout aboutuserlayout;
    boolean isImageFitToScreen;
    private Activity mActivity;
    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    private boolean isZoomAndRotate;
    private boolean isOutSide;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    float oldDist = 1f;
    private float xCoOrdinate, yCoOrdinate;
    private final ItemContainerSentMessageBinding binding;
    ItemContainerSentMessageBinding itemContainerSentMessageBinding;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    Uri cameraUri;

    public ChatActivity() {
        binding = itemContainerSentMessageBinding;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        resendmsg = (TextView) findViewById(R.id.resendmsgtext);
        backBtn = (ImageButton) findViewById(R.id.backbtn);
        sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        delbtn = (ImageButton) findViewById(R.id.delBtn);
        resentBtn = (ImageButton) findViewById(R.id.resentBtn);
        resendinchat = (ImageButton) findViewById(R.id.resendinchat);
        delresendinchat = (ImageButton) findViewById(R.id.delresendinchat);
        headerBackground = (View) findViewById(R.id.headerBackground);
        profileImage = (ImageView) findViewById(R.id.photoprofile);
        chatlayout = (ConstraintLayout) findViewById(R.id.chatlayout);
        byte[] bytes = Base64.decode(String.valueOf(getIntent().getSerializableExtra(Constants.KEY_RECEIVER_IMAGE)), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        profileImage.setImageBitmap(bitmap);
        audiocall = (ImageButton) findViewById(R.id.audiocallBtn);
        addFile = (ImageButton) findViewById(R.id.addFile);
        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(ChatActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.sendfilelayout));
                Button sendImage, sendVideo, sendFile, openCamera;
                sendfilelayout = (ConstraintLayout) dialog.findViewById(R.id.sendfilelayout);
                sendImage = (Button) dialog.findViewById(R.id.sendImage);
                sendVideo = (Button) dialog.findViewById(R.id.sendVideo);
                sendFile = (Button) dialog.findViewById(R.id.sendFile);
                textsettingstatus = (TextView) dialog.findViewById(R.id.textsettingstatus);
                openCamera = (Button) dialog.findViewById(R.id.openCamera);
                if (preferenceManager.getBoolean(Constants.THEME)){
                    sendfilelayout.setBackgroundColor(getResources().getColor(R.color.black));
                    textsettingstatus.setTextColor(getResources().getColor(R.color.white));
                    sendImage.setTextColor(getResources().getColor(R.color.white));
                    sendVideo.setTextColor(getResources().getColor(R.color.white));
                    sendFile.setTextColor(getResources().getColor(R.color.white));
                    openCamera.setTextColor(getResources().getColor(R.color.white));
                }
                sendImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        pickImage.launch(intent);
                        dialog.dismiss();
                    }
                });
                sendVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        pickVideo.launch(intent);*/
                        dialog.dismiss();
                        final Dialog dialog = new Dialog(ChatActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView((R.layout.uncorrectlayout));
                        TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                        correctText.setText("В связи с тем, что в бесплатном доступе на сервере можно хранить лишь до 3 Гб файлов, то отправка видео и файлов временно ограничена");
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
                });
                sendFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        //intent.setType("*/*");
                        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        //pickFile.launch(intent);
                        dialog.dismiss();
                        final Dialog dialog = new Dialog(ChatActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView((R.layout.uncorrectlayout));
                        TextView correctText = (TextView) dialog.findViewById(R.id.textinfo);
                        correctText.setText("В связи с тем, что в бесплатном доступе на сервере можно хранить лишь до 3 Гб файлов, то отправка видео и файлов временно ограничена");
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
                });
                openCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkCameraPermission()){
                            pickImageCamera();
                            dialog.dismiss();
                        } else {
                            requestCameraPermission();
                        }
                    }
                });
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progbar);
        inputMessage = (EditText) findViewById(R.id.textMessage);
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID));
                documentReference.update(Constants.KEY_TYPING, true);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (inputMessage.getText().toString().equals("")){
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(preferenceManager.getString(Constants.KEY_USER_ID));
                    documentReference.update(Constants.KEY_TYPING, false);
                }
            }
        });
        Bundle extras = getIntent().getExtras();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputMessage.getText().toString().equals("")){
                    sendMessage();
                }
            }
        });
        loadReceiverDetails();
        nameText = (TextView) findViewById(R.id.nametext);
        nameText.setText(extras.getString(Constants.KEY_NAME));
        nameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(ChatActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView((R.layout.aboutuserlayout));
                ImageView profileimage = dialog.findViewById(R.id.profileimage);
                TextView name = dialog.findViewById(R.id.name);
                TextView nameText = dialog.findViewById(R.id.userName);
                TextView phone = dialog.findViewById(R.id.phone);
                TextView email = dialog.findViewById(R.id.emailText);
                TextView emailuserText = dialog.findViewById(R.id.emailuserText);
                TextView textnameuser = dialog.findViewById(R.id.name);
                TextView phoneText = dialog.findViewById(R.id.phone);
                TextView statustextuser = dialog.findViewById(R.id.status);
                Button audiocall = dialog.findViewById(R.id.audiocallbtn);
                TextView username = dialog.findViewById(R.id.name);
                TextView status = dialog.findViewById(R.id.status);
                TextView clock = dialog.findViewById(R.id.time);
                LinearLayout delchat = dialog.findViewById(R.id.clearchat);
                LinearLayout blockchat = dialog.findViewById(R.id.blockchat);
                delchat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                blockchat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                        builder.setTitle("Заблокировать пользователя?");
                        builder.setMessage(receivedUser.name + " не сможет отправлять Вам сообщения");
                        builder.setPositiveButton("Заблокировать", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseFirestore database = FirebaseFirestore.getInstance();
                                documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                                        .document(preferenceManager.getString(Constants.KEY_USER_ID));
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            if (documentSnapshot.getString(Constants.BLOCKED_USERS) != null){
                                                if (!documentSnapshot.getString(Constants.BLOCKED_USERS).contains(phone.getText())){
                                                    documentReference.update(Constants.BLOCKED_USERS, documentSnapshot.getString(Constants.BLOCKED_USERS) + " " + phone.getText());
                                                }
                                            } else {
                                                documentReference.update(Constants.BLOCKED_USERS, phone.getText());
                                            }
                                        }
                                    }
                                });
                                Toast.makeText(ChatActivity.this, "Пользователь " + receivedUser.name + " заблокирован", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
                ImageButton back = dialog.findViewById(R.id.backClick);
                aboutuserlayout = (ConstraintLayout) dialog.findViewById(R.id.aboutuserlayout);
                if (preferenceManager.getBoolean(Constants.THEME)){
                    aboutuserlayout.setBackgroundColor(getResources().getColor(R.color.black));
                    email.setTextColor(getResources().getColor(R.color.white));
                    phone.setTextColor(getResources().getColor(R.color.white));
                    status.setTextColor(getResources().getColor(R.color.white));
                    username.setTextColor(getResources().getColor(R.color.white));
                    name.setTextColor(getResources().getColor(R.color.white));
                    clock.setTextColor(getResources().getColor(R.color.white));
                    textnameuser.setTextColor(getResources().getColor(R.color.white));
                    phoneText.setTextColor(getResources().getColor(R.color.white));
                    statustextuser.setTextColor(getResources().getColor(R.color.white));
                }
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(receivedUser.id);
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            email.setText(documentSnapshot.getString(Constants.KEY_EMAIL));
                            emailuserText.setText(email.getText().toString());
                            phone.setText(documentSnapshot.getString(Constants.KEY_USER_PHONE));
                            status.setText(documentSnapshot.getString(Constants.STATUS));
                        }
                    }
                });
                profileimage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog photodialog = new Dialog(view.getContext());
                        photodialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        photodialog.setContentView((R.layout.warchfullphoto));
                        ImageView imageView = (ImageView) photodialog.findViewById(R.id.imageview);
                        imageView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                imageView.bringToFront();
                                viewTransformation(view, motionEvent);
                                return true;
                            }
                        });
                        Button back = (Button) photodialog.findViewById(R.id.backImage);
                        back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                photodialog.dismiss();
                            }
                        });
                        photodialog.setCanceledOnTouchOutside(true);
                        ProgressBar progressBar = (ProgressBar) photodialog.findViewById(R.id.progressbar);
                        progressBar.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(getBitmapFromEncodedString(receivedUser.image));
                        progressBar.setVisibility(View.GONE);
                        photodialog.show();
                        photodialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        photodialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        photodialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        photodialog.getWindow().setGravity(Gravity.NO_GRAVITY);
                    }
                });
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                audiocall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
                        } else {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(receivedUser.id);
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        Intent intent = new Intent(Intent.ACTION_CALL);
                                        intent.setData(Uri.parse("tel:" + documentSnapshot.getString(Constants.KEY_USER_PHONE)));
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }
                });
                profileimage.setImageBitmap(getBitmapFromEncodedString(receivedUser.image));
                name.setText(extras.getString(Constants.KEY_NAME));
                nameText.setText(extras.getString(Constants.KEY_NAME));
                username.setText(extras.getString(Constants.KEY_NAME));
                clock.setText(availabilityText.getText().toString());
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        });
        availabilityText = (TextView) findViewById(R.id.availabilityText);
        chatRecyclerView = (RecyclerView) findViewById(R.id.conversionsRecyclerView);
        init();
        listenMessage();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        audiocall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
                } else {
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(receivedUser.id);
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse("tel:" + documentSnapshot.getString(Constants.KEY_USER_PHONE)));
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });
        checktheme();
    }

    private void checktheme() {
        if (preferenceManager.getBoolean(Constants.THEME)){
            chatlayout.setBackgroundColor(getResources().getColor(R.color.black));
            headerBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.headersbgdark));
            nameText.setTextColor(getResources().getColor(R.color.white));
            availabilityText.setTextColor(Color.parseColor("#BFFFFFFF"));
            inputMessage.setTextColor(getResources().getColor(R.color.white));
            inputMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.searchtextbgdark));
        }
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        try {
            chatAdapter = new ChatAdapter(
                    chatMessages,
                    getBitmapFromEncodedString(receivedUser.image),
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    this,
                    this
            );
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_TYPING, false);
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVED_ID, receivedUser.id);
        message.put(Constants.KEY_MESSAGE, inputMessage.getText().toString());
        message.put(Constants.SENDER_MESSAGE, inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        message.put(Constants.READ, false);
        message.put(Constants.DELIVERED, true);
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null){
            updateConversion(inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVED_ID, receivedUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receivedUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receivedUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receivedUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, inputMessage.getText().toString());
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            } catch (Exception exception){
                showToast(exception.getMessage());
            }
        }
        inputMessage.setText(null);
        resendmsg.setText("");
        resendmsg.setVisibility(View.GONE);
        delresendinchat.setVisibility(View.GONE);
        showname();
        chatAdapter.notifyDataSetChanged();
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                return;
                            }
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                } else {
                    showToast("Ошибка: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receivedUser.id
        ).addSnapshotListener(ChatActivity.this, ((value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receivedUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                isReceiverTyping = value.getBoolean(Constants.KEY_TYPING);
                if (receivedUser.image == null){
                    receivedUser.image = value.getString(Constants.KEY_IMAGE);
                    profileImage.setImageBitmap(getBitmapFromEncodedString(receivedUser.image));
                    chatAdapter.setReceivedProfileImage(getBitmapFromEncodedString(receivedUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable){
                availabilityText.setText("Онлайн");
            } else {
                availabilityText.setText("Был-(а) в " + value.getString(Constants.DATE_AVAILABILITY));
            }
            if (isReceiverTyping){
                availabilityText.setText("Печатает...");
            }
        }));
    }

    private void listenMessage(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVED_ID, receivedUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.id)
                .whereEqualTo(Constants.KEY_RECEIVED_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null)
        {
            return;
        }
        if (value != null)
        {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.chatid = documentChange.getDocument().getId();
                    chatMessage.receivedId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.link = documentChange.getDocument().getString(Constants.LINK);
                    chatMessage.senderMessage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                    chatMessage.read = documentChange.getDocument().getBoolean(Constants.READ);
                    chatMessage.delivered = documentChange.getDocument().getBoolean(Constants.DELIVERED);
                    chatMessage.messageid = documentChange.getDocument().getString(Constants.MSG_ID);
                    chatMessage.getConversionId = documentChange.getDocument().getId();
                    chatMessage.senderFile = documentChange.getDocument().getString(Constants.SENDER_FILE);
                    if (documentChange.getDocument().getString(Constants.RECEIVER_FILE) != null){
                        chatMessage.receiverFile = documentChange.getDocument().getString(Constants.RECEIVER_FILE);
                    } else {
                        chatMessage.receiverFile = null;
                    }
                    chatMessage.dataTime = getReadableDataTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dataObject.compareTo(obj2.dataObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            chatRecyclerView.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.GONE);
        if (conversionId == null){
            checkForConversion();
        }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if (encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadReceiverDetails()
    {
        receivedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
    }

    public void resend(String message){
        Intent intent = new Intent(getApplicationContext(), resend_activity.class);
        intent.putExtra(Constants.KEY_MESSAGE, message);
        intent.putExtra(Constants.KEY_USER, receivedUser);
        startActivity(intent);
    }

    private String getReadableDataTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversion(){
        if (chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receivedUser.id
            );
            checkForConversionRemotely(
                    receivedUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVED_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    @Override
    public void onUserClicked(User user, int position) {

    }
    private String encodeImage(Bitmap bitmap, Uri imageUri) {
        int previewWidth = 650;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Отправка фото");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        progressDialog.dismiss();
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVED_ID, receivedUser.id);
        message.put(Constants.DELIVERED, true);
        message.put(Constants.READ, false);
        message.put(Constants.KEY_MESSAGE, "https://firebasestorage.googleapis.com/v0/b/new-messenger-331b1.appspot.com/o/ChatImages" + " " + encodedImage);
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null) {
            updateConversion("Фото");
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVED_ID, receivedUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receivedUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receivedUser.image);
            conversion.put(Constants.DELIVERED, false);
            conversion.put(Constants.READ, false);
            conversion.put(Constants.KEY_LAST_MESSAGE, "Фото");
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receivedUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, "https://firebasestorage.googleapis.com/v0/b/new-messenger-331b1.appspot.com/o/ChatImages" + " " + encodedImage);
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
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
                            final Dialog dialog = new Dialog(ChatActivity.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView((R.layout.preview_send_image));
                            ImageView sendimagephoto = dialog.findViewById(R.id.sendimagephoto);
                            ConstraintLayout aboutlayout = dialog.findViewById(R.id.aboutlayout);
                            TextView senmediatext = dialog.findViewById(R.id.senmediatext);
                            Button sendImagebtn = dialog.findViewById(R.id.sendImagebtn);
                            if (preferenceManager.getBoolean(Constants.THEME)){
                                aboutlayout.setBackgroundColor(getResources().getColor(R.color.black));
                                senmediatext.setTextColor(getResources().getColor(R.color.white));
                            }
                            sendimagephoto.setImageURI(imageUri);
                            sendImagebtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    sendImagebtn.setEnabled(false);
                                    sendImagebtn.setText("Отправляется...");
                                    encodedImage = encodeImage(bitmap, imageUri);
                                    sendImagebtn.setEnabled(true);
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setGravity(Gravity.CENTER);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodingImage(Bitmap bitmap)
    {
        int previewWidth = 300;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private final ActivityResultLauncher<Intent> pickVideo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        int previewWidth = 650;
                        Uri imageUri = result.getData().getData();
                        InputStream inputStream = null;
                        try {
                            inputStream = getContentResolver().openInputStream(imageUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
                        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        encodedVideo = Base64.encodeToString(bytes, Base64.DEFAULT);
                        ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setMessage("Отправка видео");
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        HashMap<String, Object> message = new HashMap<>();
                        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                        message.put(Constants.KEY_RECEIVED_ID, receivedUser.id);
                        message.put(Constants.KEY_MESSAGE, "https://firebasestorage.googleapis.com/v0/b/new-messenger-331b1.appspot.com/o/ChatVideo" + " " + encodedVideo);
                        message.put(Constants.KEY_TIMESTAMP, new Date());
                        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                        if (conversionId != null) {
                            updateConversion(inputMessage.getText().toString());
                        } else {
                            HashMap<String, Object> conversion = new HashMap<>();
                            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                            conversion.put(Constants.KEY_RECEIVED_ID, receivedUser.id);
                            conversion.put(Constants.KEY_RECEIVER_NAME, receivedUser.name);
                            conversion.put(Constants.KEY_RECEIVER_IMAGE, receivedUser.image);
                            conversion.put(Constants.KEY_LAST_MESSAGE, "Видео");
                            conversion.put(Constants.KEY_TIMESTAMP, new Date());
                            addConversion(conversion);
                        }
                        if (!isReceiverAvailable) {
                            try {
                                JSONArray tokens = new JSONArray();
                                tokens.put(receivedUser.token);
                                JSONObject data = new JSONObject();
                                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                                data.put(Constants.KEY_MESSAGE, "https://firebasestorage.googleapis.com/v0/b/new-messenger-331b1.appspot.com/o/ChatVideo" + " " + encodedVideo);
                                JSONObject body = new JSONObject();
                                body.put(Constants.REMOTE_MSG_DATA, data);
                                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                                sendNotification(body.toString());
                            } catch (Exception exception) {
                                showToast(exception.getMessage());
                            }
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
                        Uri imageUri = result.getData().getData();
                    }
                }
            }
    );

    private void viewTransformation(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();

                start.set(event.getX(), event.getY());
                isOutSide = false;
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    mode = ZOOM;
                }

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
                isZoomAndRotate = false;
                if (mode == DRAG) {
                    float x = event.getX();
                    float y = event.getY();
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false;
                        view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            float scale = newDist1 / oldDist * view.getScaleX();
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event);
                            view.setRotation((float) (view.getRotation() + (newRot - d)));
                        }
                    }
                }
                break;
        }
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (int) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void hidename(){
        nameText.setVisibility(View.GONE);
        availabilityText.setVisibility(View.GONE);
        delbtn.setVisibility(View.VISIBLE);
        resentBtn.setVisibility(View.VISIBLE);
        resendinchat.setVisibility(View.VISIBLE);
    }

    public void showname(){
        nameText.setVisibility(View.VISIBLE);
        availabilityText.setVisibility(View.VISIBLE);
        delbtn.setVisibility(View.GONE);
        resentBtn.setVisibility(View.GONE);
        resendinchat.setVisibility(View.GONE);
    }

    public void update(){
        init();
        listenMessage();
    }

    public void hideResend() {
        resentBtn.setVisibility(View.GONE);
        resendinchat.setVisibility(View.GONE);
    }
    private boolean checkCameraPermission(){
        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        return cameraResult;
    }

    private void requestCameraPermission(){
        try {
            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_REQUEST_CODE);
        }catch (Exception e){
            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                try {
                    if (grantResults.length>0){
                        boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        if (cameraAccepted && storageAccepted){
                            pickImageCamera();
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void pickImageCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Загрузка фото");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Загрузка фото для приложения");
        cameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        InputStream inputStream = null;
                        try {
                            inputStream = getContentResolver().openInputStream(cameraUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        final Dialog dialog = new Dialog(ChatActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView((R.layout.preview_send_image));
                        ImageView sendimagephoto = dialog.findViewById(R.id.sendimagephoto);
                        ConstraintLayout aboutlayout = dialog.findViewById(R.id.aboutlayout);
                        TextView senmediatext = dialog.findViewById(R.id.senmediatext);
                        Button sendImagebtn = dialog.findViewById(R.id.sendImagebtn);
                        if (preferenceManager.getBoolean(Constants.THEME)){
                            aboutlayout.setBackgroundColor(getResources().getColor(R.color.black));
                            senmediatext.setTextColor(getResources().getColor(R.color.white));
                        }
                        sendimagephoto.setImageURI(cameraUri);
                        sendImagebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendImagebtn.setEnabled(false);
                                sendImagebtn.setText("Отправляется...");
                                encodedImage = encodeImage(bitmap, cameraUri);
                                sendImagebtn.setEnabled(true);
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
    );
}