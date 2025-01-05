package com.example.messenger.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.R;
import com.example.messenger.databinding.ActivityChatBinding;
import com.example.messenger.databinding.ItemContainerUserBinding;
import com.example.messenger.listeners.UserListeners;
import com.example.messenger.models.User;
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private List<User> users;
    private final UserListeners userListeners;
    private PreferenceManager preferenceManager;
    private Context context;

    public UserAdapter(List<User> users, UserListeners userListeners, Context context) {
        this.users = users;
        this.userListeners = userListeners;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void filterList(ArrayList<User> filteredlist) {
        users = filteredlist;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user){
            preferenceManager = new PreferenceManager(context);
            if (preferenceManager.getBoolean(Constants.THEME)){
                binding.textName.setTextColor(context.getResources().getColorStateList(R.color.white));
            }
            binding.textName.setText(user.name);
            binding.textRecentMessage.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(view -> userListeners.onUserClicked(user, getPosition()));
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
