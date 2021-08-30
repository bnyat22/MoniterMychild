package com.example.localiserwear.extraActivities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.localiserwear.databinding.ActivityFullAudioBinding;

import java.util.concurrent.TimeUnit;


public class FullAudioActivity extends Activity {

    TextView playerPosition , playerDuration;
    SeekBar seekBar;
    ImageView btn_rw, btn_play , btn_ff , btn_pause , back;
    private ActivityFullAudioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btn_rw =binding.audioRw;
        btn_ff = binding.audioFf;
        btn_play = binding.audioPlay;
        btn_pause = binding.audioPause;

        back = binding.backAudio;
        playerPosition = binding.playerPosition;
        playerDuration = binding.playerDuration;
        seekBar = binding.seekAudio;

        binding = ActivityFullAudioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        MediaPlayer mediaPlayer = MediaPlayer.create(this , Uri.parse(getIntent().getStringExtra("audio")));
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        };
        int duration = mediaPlayer.getDuration();
        String sDuration = convertFormat(duration);
        playerDuration.setText(sDuration);
        btn_play.setOnClickListener(v ->{
            btn_play.setVisibility(View.GONE);
            btn_pause.setVisibility(View.VISIBLE);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());
            handler.postDelayed(runnable,0);

        });
        btn_pause.setOnClickListener(v ->{
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);
            mediaPlayer.stop();
            handler.removeCallbacks(runnable);

        });
        btn_ff.setOnClickListener(v ->{
            int currentPos = mediaPlayer.getCurrentPosition();
            int dur = mediaPlayer.getDuration();
            if (mediaPlayer.isPlaying() && dur != currentPos) {
                currentPos += 5000;
                playerPosition.setText(convertFormat(currentPos));
                mediaPlayer.seekTo(currentPos);
                playerPosition.setText(convertFormat(currentPos));
            }
        });
        btn_rw.setOnClickListener(v ->{
            int currentPos = mediaPlayer.getCurrentPosition();
            int dur = mediaPlayer.getDuration();
            if (mediaPlayer.isPlaying() &&  currentPos>5000) {
                currentPos -= 5000;
                playerPosition.setText(convertFormat(currentPos));
                mediaPlayer.seekTo(currentPos);
                playerPosition.setText(convertFormat(currentPos));
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                {
                    mediaPlayer.seekTo(progress);
                }
                playerPosition.setText(convertFormat(mediaPlayer.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mediaPlayer.setOnCompletionListener(t->
        {
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);
            mediaPlayer.seekTo(0);
        });
        back.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0,0);
        });

    }

    @SuppressLint("DefaultLocale")
    private String convertFormat(int duration) {
        return String.format("%02d:%02d" ,
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
    }
}