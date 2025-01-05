package com.example.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class watchfullvideo extends AppCompatActivity {
    VideoView videoView;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchfullvideo);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        videoView = (VideoView) findViewById(R.id.videoview);
        videoView.setVideoPath(String.valueOf(getIntent().getSerializableExtra("source")));
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressBar.setVisibility(View.GONE);
            }
        });
        videoView.requestFocus();
        videoView.start();
        progressBar.setVisibility(View.GONE);
    }
    public void play(View view){
        videoView.start();
    }
    public void pause(View view){
        videoView.pause();
    }
    public void stop(View view){
        videoView.stopPlayback();
        videoView.resume();
    }
}