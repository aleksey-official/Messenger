package com.example.messenger;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messenger.adapters.RecentConversionsAdapter;
import com.example.messenger.databinding.ItemContainerRecentConversionBinding;
import com.example.messenger.listeners.ConversionListener;
import com.example.messenger.models.ChatMessage;
import com.example.messenger.models.User;
import com.example.messenger.network.ApiClient;
import com.example.messenger.network.ApiService;
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class resend_activity extends BaseActivity implements ConversionListener {
    ImageView profileImage, noconversationsimage;
    TextView noconversationstext, newsText, chatText;
    EditText searchText;
    ImageButton addBtn;
    ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversion;
    private RecentConversionsAdapter conversionsAdapter;
    private FirebaseFirestore database;
    RecyclerView conversionsRecyclerView;
    User user;
    private String conversionId = null;
    SwipeRefreshLayout refreshLayout;
    static int PERMISSION_CODE = 100;
    private DocumentReference documentReference;
    private Boolean isReceiverAvailable = false;
    private final int MY_PERMISSIONS_REQUEST_WRITE = 100;
    Animation anim;
    ConstraintLayout resend_activity;
    private List<ChatMessage> selectedMessage;
    FloatingActionButton resent;
    private User receivedUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resend);
        selectedMessage = new ArrayList<>();
        receivedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        resent = (FloatingActionButton) findViewById(R.id.resent);
        resent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        preferenceManager = new PreferenceManager(getApplicationContext());
        resend_activity = (ConstraintLayout) findViewById(R.id.resend_activity);
        chatText = (TextView) findViewById(R.id.chatText);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                init();
                getToken();
                listenConversations();
                refreshLayout.setRefreshing(false);
            }
        });
        noconversationsimage = (ImageView) findViewById(R.id.noconversationsimage);
        noconversationstext = (TextView) findViewById(R.id.text);
        progressBar = (ProgressBar) findViewById(R.id.prbar);
        searchText = (EditText) findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
        conversionsRecyclerView = (RecyclerView) findViewById(R.id.conversionsRecyclerView);
        init();
        checkTheme();
        getToken();
        listenConversations();
    }

    public void checkTheme() {
        if (preferenceManager.getBoolean(Constants.THEME)){
            resend_activity.setBackgroundColor(getResources().getColor(R.color.black));
            chatText.setTextColor(getResources().getColor(R.color.white));
            searchText.setHintTextColor(Color.parseColor("#CBCBD1"));
            searchText.setTextColor(Color.parseColor("#CBCBD1"));
            searchText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.searchicondark, 0, 0, 0);
            searchText.setBackgroundDrawable(getResources().getDrawable(R.drawable.searchtextbgdark));
        }
    }

    private void filter(String text){
        ArrayList<ChatMessage> filteredlist = new ArrayList<ChatMessage>();
        for (ChatMessage item : conversion){
            if (item.conversionName.toString().toLowerCase().contains(text.toLowerCase())){
                filteredlist.add(item);
            }
        }
        conversionsAdapter.filterList(filteredlist);
    }

    private void init(){
        conversion = new ArrayList<>();
        conversionsAdapter = new RecentConversionsAdapter(conversion, this, resend_activity.this);
        conversionsRecyclerView.setAdapter(conversionsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVED_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private void  showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void call(String phone){
        if (ContextCompat.checkSelfPermission(resend_activity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(resend_activity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        }
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    if (conversion.size() > 0){
                        noconversationstext.setVisibility(View.GONE);
                        noconversationsimage.setVisibility(View.GONE);
                    }
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receivedId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionPhone = documentChange.getDocument().getString(Constants.KEY_USER_PHONE);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                        chatMessage.getConversionId = documentChange.getDocument().getId();
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionPhone = documentChange.getDocument().getString(Constants.KEY_USER_PHONE);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.getConversionId = documentChange.getDocument().getId();
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversion.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversion.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                        if (conversion.get(i).senderId.equals(senderId) && conversion.get(i).receivedId.equals(receiverId)){
                            conversion.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversion.get(i).dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            if (conversion.size() == 0){
                noconversationstext.setVisibility(View.VISIBLE);
                noconversationsimage.setVisibility(View.VISIBLE);
            } else {
                noconversationstext.setVisibility(View.GONE);
                noconversationsimage.setVisibility(View.GONE);
            }
            Collections.sort(conversion, (obj1, obj2) -> obj2.dataObject.compareTo(obj1.dataObject));
            conversionsAdapter.notifyDataSetChanged();
            conversionsRecyclerView.smoothScrollToPosition(0);
            conversionsRecyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    });

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token)
    {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Неудалось обновить токен"));
    }

    String deletedMove = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onConversionClicked(User user, int position) {
        if (selectedMessage.contains(user)) {
            selectedMessage.remove(conversion.get(position));
            Toast.makeText(this, "Пользователь удалён", Toast.LENGTH_SHORT).show();
        } else {
            selectedMessage.add(conversion.get(position));
            Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_TYPING, false);
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVED_ID, receivedUser.id);
        message.put(Constants.KEY_MESSAGE, getIntent().getSerializableExtra(Constants.RESEND));
        message.put(Constants.SENDER_MESSAGE, getIntent().getSerializableExtra(Constants.RESEND));
        message.put(Constants.KEY_TIMESTAMP, new Date());
        message.put(Constants.READ, false);
        message.put(Constants.DELIVERED, true);
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null){
            updateConversion(String.valueOf(getIntent().getSerializableExtra(Constants.RESEND)));
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVED_ID, receivedUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receivedUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receivedUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, getIntent().getSerializableExtra(Constants.RESEND));
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
                data.put(Constants.KEY_MESSAGE, getIntent().getSerializableExtra(Constants.RESEND));
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            } catch (Exception exception){
                showToast(exception.getMessage());
            }
        }
        Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
    }

    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receivedUser.id
        ).addSnapshotListener(resend_activity.this, ((value, error) -> {
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
            }
        }));
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@org.checkerframework.checker.nullness.qual.NonNull Call<String> call, @org.checkerframework.checker.nullness.qual.NonNull Response<String> response) {
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
            public void onFailure(@org.checkerframework.checker.nullness.qual.NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }
}