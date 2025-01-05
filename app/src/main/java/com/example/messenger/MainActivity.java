package com.example.messenger;

import androidx.annotation.NonNull;
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
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends BaseActivity implements ConversionListener {
    ImageView profileImage, noconversationsimage;
    TextView noconversationstext, newsText, chatText, textchat;
    EditText searchText;
    ImageButton addBtn;
    ProgressBar progressBar;
    Button startchatBtn;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversion;
    private RecentConversionsAdapter conversionsAdapter;
    private FirebaseFirestore database;
    RecyclerView conversionsRecyclerView;
    User user;
    SwipeRefreshLayout refreshLayout;
    static int PERMISSION_CODE = 100;
    private DocumentReference documentReference;
    private final int MY_PERMISSIONS_REQUEST_WRITE = 100;
    Animation anim;
    ConstraintLayout mainactivitylayout;
    private List<ChatMessage> selectedMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedMessage = new ArrayList<>();
        preferenceManager = new PreferenceManager(getApplicationContext());
        mainactivitylayout = (ConstraintLayout) findViewById(R.id.mainactivitylayout);
        chatText = (TextView) findViewById(R.id.chatText);
        startchatBtn = (Button) findViewById(R.id.startchatBtn);
        textchat = (TextView) findViewById(R.id.textchat);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ItemTouchHelper helper = new ItemTouchHelper(callback);
                helper.attachToRecyclerView(conversionsRecyclerView);
                init();
                loadUserData();
                getToken();
                listenConversations();
                refreshLayout.setRefreshing(false);
            }
        });
        database = FirebaseFirestore.getInstance();
        documentReference = database.collection("version")
                .document("version");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.getString("version").equals("1.1")) {

                    } else {
                        final Dialog dialog = new Dialog(MainActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView((R.layout.updatelayout));
                        Button updateBtn = dialog.findViewById(R.id.updatebtn);
                        updateBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE);
                                } else {
                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                    StrictMode.setThreadPolicy(policy);
                                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                                    progressDialog.setMessage("Загрузка новой версии приложения");
                                    progressDialog.setCancelable(false);
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.show();
                                    DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                    Uri uri = Uri.parse("https://drive.google.com/uc?export=download&id=1ZppU8J9_bZhvjLVHHid-LgVDqrItRAFR");
                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setTitle("Новая версия приложения");
                                    request.setDescription("Загрузка новой версии приложения");
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setVisibleInDownloadsUi(false);
                                    downloadmanager.enqueue(request);
                                    progressDialog.dismiss();
                                }
                            }
                        });
                        dialog.show();
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        dialog.getWindow().setGravity(Gravity.BOTTOM);
                    }
                }
            }
        });
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 103);
        }
        newsText = (TextView) findViewById(R.id.newsText);
        database = FirebaseFirestore.getInstance();
        documentReference = database.collection("news")
                .document("news");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getString("news").equals("")) {
                        newsText.setVisibility(View.GONE);
                    } else {
                        anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.textviewanim);
                        newsText.setVisibility(View.VISIBLE);
                        newsText.setText("   " + task.getResult().getString("news"));
                        newsText.startAnimation(anim);
                    }
                }
            }
        });
        profileImage = (ImageView) findViewById(R.id.profileImage);
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
        addBtn = (ImageButton) findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
            }
        });
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(conversionsRecyclerView);
        init();
        checkTheme();
        loadUserData();
        getToken();
        listenConversations();
        if (getIntent().getSerializableExtra(Constants.RESEND) != null){
            if (getIntent().getSerializableExtra(Constants.RESEND).equals("resend")){
                chatText.setText("Отправить...");

            }

        }
        startchatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
    }

    public void checkTheme() {
        if (preferenceManager.getBoolean(Constants.THEME)){
            mainactivitylayout.setBackgroundColor(getResources().getColor(R.color.black));
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
        conversionsAdapter = new RecentConversionsAdapter(conversion, this, MainActivity.this);
        conversionsRecyclerView.setAdapter(conversionsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void loadUserData()
    {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        profileImage.setImageBitmap(bitmap);
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
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
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
            FirebaseFirestore databaseUser;
            DocumentReference documentReferenceUser;
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    if (conversion.size() > 0){
                        noconversationstext.setVisibility(View.GONE);
                        noconversationsimage.setVisibility(View.GONE);
                        textchat.setVisibility(View.GONE);
                        startchatBtn.setVisibility(View.GONE);
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
                textchat.setVisibility(View.VISIBLE);
                startchatBtn.setVisibility(View.VISIBLE);
            } else {
                noconversationstext.setVisibility(View.GONE);
                noconversationsimage.setVisibility(View.GONE);
                textchat.setVisibility(View.GONE);
                startchatBtn.setVisibility(View.GONE);
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
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        intent.putExtra(Constants.KEY_RECEIVED_ID, conversion.get(position).conversionId);
        intent.putExtra(Constants.CONVERSION_ID, conversion.get(position).receivedId);
        intent.putExtra(Constants.KEY_RECEIVER_IMAGE, user.image);
        intent.putExtra(Constants.KEY_NAME, user.name);
        intent.putExtra(Constants.KEY_EMAIL, user.email);
        startActivity(intent);
    }
    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Удалить чат?");
            builder.setMessage("Чат удалится со всех устройств");
            builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String id = conversion.get(viewHolder.getPosition()).receivedId;
                    database = FirebaseFirestore.getInstance();
                    documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                            .document(conversion.get(viewHolder.getPosition()).getConversionId);
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Query query = db.collection(Constants.KEY_COLLECTION_CHAT)
                                        .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                                        .whereEqualTo(Constants.KEY_RECEIVED_ID, id);
                                query.get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                WriteBatch batch = db.batch();
                                                for (QueryDocumentSnapshot document : task1.getResult()) {
                                                    batch.delete(document.getReference());
                                                }
                                                batch.commit()
                                                        .addOnCompleteListener(batchTask -> {
                                                            if (batchTask.isSuccessful()) {
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }
                    });
                    documentReference.delete();
                    conversion.remove(viewHolder.getPosition());
                    conversionsAdapter.notifyItemRemoved(viewHolder.getPosition());
                }
            });
            builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    conversionsAdapter.notifyDataSetChanged();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,float dX, float dY,int actionState, boolean isCurrentlyActive){

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red))
                    .addActionIcon(R.drawable.dellisticon)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}