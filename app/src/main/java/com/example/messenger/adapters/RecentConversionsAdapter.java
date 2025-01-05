package com.example.messenger.adapters;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.ChatActivity;
import com.example.messenger.MainActivity;
import com.example.messenger.OutgoingInvitationActivity;
import com.example.messenger.R;
import com.example.messenger.databinding.ItemContainerRecentConversionBinding;
import com.example.messenger.listeners.ConversionListener;
import com.example.messenger.models.ChatMessage;
import com.example.messenger.models.User;
import com.example.messenger.utilities.Constants;
import com.example.messenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecentConversionsAdapter extends RecyclerView.Adapter<RecentConversionsAdapter.ConversionViewHolder> {

    private List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    private Context context;
    static int PERMISSION_CODE = 100;
    private DocumentReference documentReference;
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
    private PreferenceManager preferenceManager;
    private List<ChatMessage> selectedMessage;

    public RecentConversionsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener, Context context) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
        this.context = context;
        this.selectedMessage = new ArrayList<>();
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversionsAdapter.ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void filterList(ArrayList<ChatMessage> filteredlist) {
        chatMessages = filteredlist;
        notifyDataSetChanged();
    }


    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;
        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage){
            preferenceManager = new PreferenceManager(context);
            if (preferenceManager.getBoolean(Constants.THEME)){
                binding.textName.setTextColor(context.getResources().getColorStateList(R.color.white));
            }
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                user.phone = chatMessage.conversionPhone;
                conversionListener.onConversionClicked(user, getPosition());
            });
            binding.imageProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(view.getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView((R.layout.aboutlayout));
                    ConstraintLayout mainactivitylayout;
                    mainactivitylayout = dialog.findViewById(R.id.mainactivitylayout);
                    ImageView profileimage = dialog.findViewById(R.id.image);
                    TextView name = dialog.findViewById(R.id.name);
                    Button audiocall = dialog.findViewById(R.id.audiocallbtn3);
                    if (preferenceManager.getBoolean(Constants.THEME)){
                        //mainactivitylayout.setBackgroundTintList(context.getResources().getColorStateList(R.color.white));
                    }
                    audiocall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(chatMessage.receivedId);
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if (context instanceof MainActivity) {
                                            ((MainActivity)context).call(documentSnapshot.getString(Constants.KEY_USER_PHONE));
                                            dialog.dismiss();
                                        }
                                    }
                                }
                            });
                        }
                    });
                    profileimage.setImageBitmap(getConversionImage(chatMessage.conversionImage));
                    name.setText(chatMessage.conversionName);
                    profileimage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
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
                            imageView.setImageBitmap(getConversionImage(chatMessage.conversionImage));
                            progressBar.setVisibility(View.GONE);
                            photodialog.show();
                            photodialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            photodialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            photodialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            photodialog.getWindow().setGravity(Gravity.NO_GRAVITY);
                        }
                    });
                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                }
            });
        }
    }

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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
}
