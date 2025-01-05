package com.example.messenger;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.messenger.adapters.UserAdapter;
import com.example.messenger.listeners.UserListeners;
import com.example.messenger.models.User;
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListeners {
    ProgressBar progressBar;
    RecyclerView usersRecyclerView;
    TextView textErrorMessage, newchatText;
    ImageButton backBtn;
    EditText searchText;
    private PreferenceManager preferenceManager;
    private UserAdapter userAdapter;
    private List<User> users;
    ConstraintLayout userlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        preferenceManager = new PreferenceManager(getApplicationContext());
        userlayout = (ConstraintLayout) findViewById(R.id.userlayout);
        getUsers();
        progressBar = (ProgressBar) findViewById(R.id.progBar);
        textErrorMessage = (TextView) findViewById(R.id.textErrorMessage);
        newchatText = (TextView) findViewById(R.id.newchatText);
        usersRecyclerView = (RecyclerView) findViewById(R.id.conversionsRecyclerView);
        searchText = (EditText) findViewById(R.id.searchtextNew);
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
        backBtn = (ImageButton) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        checktheme();
    }

    private void checktheme() {
        if (preferenceManager.getBoolean(Constants.THEME)){
            userlayout.setBackgroundColor(getResources().getColor(R.color.black));
            newchatText.setTextColor(getResources().getColor(R.color.white));
            searchText.setHintTextColor(Color.parseColor("#CBCBD1"));
            searchText.setTextColor(Color.parseColor("#CBCBD1"));
            searchText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.searchicondark, 0, 0, 0);
            searchText.setBackgroundDrawable(getResources().getDrawable(R.drawable.searchtextbgdark));
        }
    }

    private void filter(String text){
        ArrayList<User> filteredlist = new ArrayList<>();
        for (User item : users){
            if (item.email.toString().toLowerCase().contains(text.toLowerCase())){
                filteredlist.add(item);
            }
        }
        userAdapter.filterList(filteredlist);
    }

    private void getUsers()
    {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null)
                    {
                        users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                        {
                            if (currentUserId.equals(queryDocumentSnapshot.getId()))
                            {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {

                            userAdapter = new UserAdapter(users, this, this);
                            usersRecyclerView.setAdapter(userAdapter);
                            usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            showErrorMessage();
                        }
                    }
                    else
                    {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        textErrorMessage.setText(String.format("%s", "Нет доступных пользователей"));
        textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            progressBar.setVisibility(View.VISIBLE);
        } else
        {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onUserClicked(User user, int position) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        intent.putExtra(Constants.KEY_EMAIL, user.email);
        intent.putExtra(Constants.KEY_NAME, user.name);
        intent.putExtra(Constants.CONVERSION_ID, users.get(position).id);
        startActivity(intent);
        finish();
    }
}