package com.example.messenger.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.ChatActivity;
import com.example.messenger.R;
import com.example.messenger.databinding.ItemContainerReceivedMessageBinding;
import com.example.messenger.databinding.ItemContainerSentMessageBinding;
import com.example.messenger.models.ChatMessage;
import com.example.messenger.resend_activity;
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.example.messenger.watchfullvideo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private Bitmap receivedProfileImage;
    private final String senderId;
    boolean isImageFitToScreen;
    private Activity mActivity;
    float[] lastEvent = null;
    float d = 0f;
    VideoView videoView;
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

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    static int PERMISSION_CODE = 105;
    private static int REQUEST_CODE = 100;
    OutputStream outputStream;
    private PreferenceManager preferenceManager;
    ConstraintLayout containerlayout;
    private FirebaseFirestore database;
    private DocumentReference documentReference;
    private Context context;
    private List<ChatMessage> selectedMessage;
    ImageButton delBtn, resentBtn, resendinchat, delresendinchat;
    ImageButton delreceiverBtn;

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receivedProfileImage, String senderId, Activity activity, Context context) {
        this.mActivity = activity;
        this.chatMessages = chatMessages;
        this.receivedProfileImage = receivedProfileImage;
        this.senderId = senderId;
        this.selectedMessage = new ArrayList<>();
        this.context = context;
    }

    public void setReceivedProfileImage(Bitmap bitmap){
        receivedProfileImage = bitmap;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            try {
                ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receivedProfileImage);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
            delBtn = (ImageButton) ((ChatActivity)context).findViewById(R.id.delBtn);
            resendinchat = (ImageButton) ((ChatActivity)context).findViewById(R.id.resendinchat);
            delresendinchat = (ImageButton) ((ChatActivity)context).findViewById(R.id.delresendinchat);
            TextView resendmsg = ((ChatActivity)context).findViewById(R.id.resendmsgtext);
            resendinchat.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    resendmsg.setVisibility(View.VISIBLE);
                    delresendinchat.setVisibility(View.VISIBLE);
                    resendmsg.setText(selectedMessage.get(0).message);
                }
            });
            delresendinchat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delresendinchat = ((ChatActivity)context).findViewById(R.id.delresendinchat);
                    resendmsg.setVisibility(View.GONE);
                    delresendinchat.setVisibility(View.GONE);
                    resendmsg.setText("");
                }
            });
            resentBtn = (ImageButton) ((ChatActivity)context).findViewById(R.id.resentBtn);
            resentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), resend_activity.class);
                    intent.putExtra(Constants.KEY_MESSAGE, selectedMessage.get(0).message);
                    context.startActivity(intent);
                }
            });
            delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Удалить выбранные сообщения?");
                        String[] choice = {"Удалить у себя", "Удалить у всех"};
                        boolean[] checkedItems = {true, false};
                        builder.setMultiChoiceItems(choice, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (checkedItems[1])
                                {
                                    checkedItems[1] = true;
                                } else {
                                    checkedItems[1] = false;
                                }
                            }
                        });
                        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    if (checkedItems[1]){
                                        Toast.makeText(mActivity, selectedMessage.size(), Toast.LENGTH_SHORT).show();
                                        for (int i = 0; i < selectedMessage.size(); i++){
                                            database = FirebaseFirestore.getInstance();
                                            documentReference = database.collection(Constants.KEY_COLLECTION_CHAT)
                                                    .document(selectedMessage.get(i).chatid);
                                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if (documentSnapshot.getString(Constants.KEY_SENDER_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                                                            documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {

                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    } else if (!checkedItems[1]){

                                    }
                                } catch (Exception e){

                                }
                            }
                        });
                        builder.setNegativeButton("Нет", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } catch (Exception e){
                        Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        void setData(ChatMessage chatMessage) throws ParseException {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onLongClick(View view) {
                    selectedMessage.add(chatMessage);
                    binding.view.setVisibility(View.VISIBLE);
                    ((ChatActivity)context).hidename();
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    if (binding.view.getVisibility() == View.VISIBLE){
                        selectedMessage.remove(chatMessage);
                        binding.view.setVisibility(View.GONE);
                        if (selectedMessage.size() == 0){
                            ((ChatActivity)context).showname();
                        }
                        if (selectedMessage.size() == 1){
                            ((ChatActivity)context).hidename();
                        }
                    } else if (selectedMessage.size() > 0) {
                        selectedMessage.add(chatMessage);
                        binding.view.setVisibility(View.VISIBLE);
                    }
                    if (selectedMessage.size() == 0){
                        ((ChatActivity)context).showname();
                    }
                    if (selectedMessage.size() > 1){
                        ((ChatActivity)context).hideResend();
                    }
                }
            });
            boolean zoomOut =  false;
            preferenceManager = new PreferenceManager(mActivity);
            ConstraintLayout.LayoutParams textDataTimeLayoutParams = (ConstraintLayout.LayoutParams) binding.textDataTime.getLayoutParams();
            ConstraintLayout.LayoutParams sendstatusLayoutParams = (ConstraintLayout.LayoutParams) binding.sendstatus.getLayoutParams();
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
            Date date = parseFormat.parse(chatMessage.dataTime.split("-")[1]);
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            documentReference = database.collection(Constants.KEY_COLLECTION_CHAT)
                    .document(chatMessage.chatid);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (value != null && value.exists()) {
                                Boolean read = (Boolean) value.getData().get(Constants.READ);
                                if (read){
                                    binding.sendstatus.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.alldoneicon));
                                }
                            }
                        }
                    });
                }
            });
            if (chatMessage.delivered){
                binding.sendstatus.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.doneicon));
            }
            if (chatMessage.read){
                binding.sendstatus.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.alldoneicon));
            }
            if (preferenceManager.getBoolean(Constants.THEME)){
                binding.containerlayout.setBackgroundColor(mActivity.getResources().getColor(R.color.black));
            }
            if (chatMessage.message.contains("resendMsg")){
                binding.resend.setVisibility(View.VISIBLE);
                binding.resend.setText(chatMessage.message.split("resendMsg:")[1]);
                chatMessage.message = chatMessage.message.split("resendMsg:")[0];
            }
            if (chatMessage.message.contains("https://firebasestorage.googleapis.com/v0/b/new-messenger-331b1.appspot.com/o/ChatImages")){
                textDataTimeLayoutParams.topToBottom = binding.myphotocard.getId();
                sendstatusLayoutParams.bottomToBottom = binding.myphotocard.getId();
                binding.textmessage.setVisibility(View.GONE);
                binding.myphoto.setVisibility(View.VISIBLE);
                binding.myphotocard.setVisibility(View.VISIBLE);
                byte[] bytes = Base64.decode(chatMessage.message.split(" ")[1], Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.myphoto.setImageBitmap(bitmap);
                binding.myimageprogressbar.setVisibility(View.GONE);
                binding.textDataTime.setText(displayFormat.format(date));
                binding.myphoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(view.getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView((R.layout.warchfullphoto));
                        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageview);
                        Button back = (Button) dialog.findViewById(R.id.backImage);
                        back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        imageView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                imageView.bringToFront();
                                viewTransformation(view, motionEvent);
                                return true;
                            }
                        });
                        ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progressbar);
                        progressBar.setVisibility(View.VISIBLE);
                        byte[] bytes = Base64.decode(chatMessage.message.split(" ")[1], Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                        progressBar.setVisibility(View.GONE);
                        dialog.show();
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        dialog.getWindow().setGravity(Gravity.NO_GRAVITY);
                    }
                });
            } else if (chatMessage.message.contains("https://firebasestorage.googleapis.com/v0/b/messenger-45e1b.appspot.com/o/ChatVideo")){
                textDataTimeLayoutParams.topToBottom = binding.myphotocard.getId();
                binding.textmessage.setVisibility(View.GONE);
                binding.myphoto.setVisibility(View.VISIBLE);
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever .setDataSource(chatMessage.message, new HashMap<String, String>());
                Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(1000); //unit in microsecond
                binding.myphoto.setImageBitmap(bmFrame);
                binding.myphotocard.setVisibility(View.VISIBLE);
                binding.myphoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mActivity, watchfullvideo.class);
                        intent.putExtra("source", chatMessage.message);
                        mActivity.startActivity(intent);
                    }
                });
                binding.myimageprogressbar.setVisibility(View.GONE);
                binding.textDataTime.setText(displayFormat.format(date));
            }
            else {
                binding.textDataTime.setText(displayFormat.format(date));
                textDataTimeLayoutParams.topToBottom = binding.textmessage.getId();
                binding.myphoto.setVisibility(View.GONE);
                binding.myphotocard.setVisibility(View.GONE);
                binding.textmessage.setVisibility(View.VISIBLE);
                binding.textmessage.setText(chatMessage.message);
            }
        }
    }

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

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receivedProfileImage) throws ParseException {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onLongClick(View view) {
                    selectedMessage.add(chatMessage);
                    binding.view.setVisibility(View.VISIBLE);
                    ((ChatActivity)context).hidename();
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    if (binding.view.getVisibility() == View.VISIBLE){
                        selectedMessage.remove(chatMessage);
                        binding.view.setVisibility(View.GONE);
                    } else if (selectedMessage.size() > 0) {
                        selectedMessage.add(chatMessage);
                        binding.view.setVisibility(View.VISIBLE);
                    }
                    if (selectedMessage.size() == 0){
                        ((ChatActivity)context).showname();
                    }
                }
            });
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            documentReference = database.collection(Constants.KEY_COLLECTION_CHAT)
                    .document(chatMessage.chatid);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.getString(Constants.KEY_RECEIVED_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                            if (!documentSnapshot.getBoolean(Constants.READ)){
                                documentReference.update(Constants.READ, true);
                            }
                        }
                    }
                }
            });
            ConstraintLayout.LayoutParams textDataTimeLayoutParams = (ConstraintLayout.LayoutParams) binding.textDataTime.getLayoutParams();
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
            preferenceManager = new PreferenceManager(mActivity);
            if (preferenceManager.getBoolean(Constants.THEME)){
                binding.containerlayout.setBackgroundColor(mActivity.getResources().getColor(R.color.black));
                binding.textmessage.setTextColor(mActivity.getResources().getColor(R.color.white));
                binding.textmessage.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.background_received_message_dark));
                binding.textDataTime.setTextColor(Color.parseColor("#80FFFFFF"));
            }
            if (chatMessage.message.contains("resendMsg")){
                binding.resend.setVisibility(View.VISIBLE);
                binding.resend.setText(chatMessage.message.split("resendMsg:")[1]);
                chatMessage.message = chatMessage.message.split("resendMsg:")[0];
            }
            if (chatMessage.message.contains("https://firebasestorage.googleapis.com/v0/b/new-messenger-331b1.appspot.com/o/ChatImages")){
                binding.textmessage.setVisibility(View.GONE);
                binding.photo.setVisibility(View.VISIBLE);
                binding.photoCard.setVisibility(View.VISIBLE);
                byte[] bytes = Base64.decode(chatMessage.message.split(" ")[1], Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.photo.setImageBitmap(bitmap);
                textDataTimeLayoutParams.topToBottom = binding.photoCard.getId();
                binding.imageprogressbar.setVisibility(View.VISIBLE);
                binding.photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(view.getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView((R.layout.warchfullphoto));
                        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageview);
                        Button back = (Button) dialog.findViewById(R.id.backImage);
                        back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        imageView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                imageView.bringToFront();
                                viewTransformation(view, motionEvent);
                                return true;
                            }
                        });
                        dialog.setCanceledOnTouchOutside(true);
                        ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progressbar);
                        progressBar.setVisibility(View.VISIBLE);
                        imageView.setImageDrawable(binding.photo.getDrawable());
                        progressBar.setVisibility(View.GONE);
                        dialog.show();
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        dialog.getWindow().setGravity(Gravity.NO_GRAVITY);
                    }
                });
            } else if (chatMessage.message.contains("https://firebasestorage.googleapis.com/v0/b/messenger-45e1b.appspot.com/o/ChatVideo")){
                textDataTimeLayoutParams.topToBottom = binding.photoCard.getId();
                binding.textmessage.setVisibility(View.GONE);
                binding.photo.setVisibility(View.VISIBLE);
               // binding.photo.setBackgroundResource(R.drawable.fileicon);
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever .setDataSource(chatMessage.message, new HashMap<String, String>());
                Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(1000); //unit in microsecond
                binding.photo.setImageBitmap(bmFrame);
                binding.photoCard.setVisibility(View.VISIBLE);
                binding.photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mActivity, watchfullvideo.class);
                        intent.putExtra("source", chatMessage.message);
                        mActivity.startActivity(intent);
                    }
                });
                binding.imageprogressbar.setVisibility(View.GONE);
            } else {
                textDataTimeLayoutParams.topToBottom = binding.textmessage.getId();
                binding.photo.setVisibility(View.GONE);
                binding.photoCard.setVisibility(View.GONE);
                binding.textmessage.setVisibility(View.VISIBLE);
                binding.textmessage.setText(chatMessage.message);
            }
            Date date = parseFormat.parse(chatMessage.dataTime.split("-")[1]);
            binding.textDataTime.setText(displayFormat.format(date));
        }
    }

    private void askPermission() {

        ActivityCompat.requestPermissions(mActivity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);

    }
}
