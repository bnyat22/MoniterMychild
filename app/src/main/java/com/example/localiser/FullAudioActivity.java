package com.example.localiser;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.localiser.R;

import java.util.concurrent.TimeUnit;

public class FullAudioActivity extends AppCompatActivity {

    TextView playerPosition , playerDuration;
    SeekBar seekBar;
    ImageView btn_rw, btn_play , btn_ff , btn_pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_full_screen);
        btn_rw = findViewById(R.id.audio_rw);
        btn_ff = findViewById(R.id.audio_ff);
        btn_play = findViewById(R.id.audio_play);
        btn_pause = findViewById(R.id.audio_pause);
        playerPosition = findViewById(R.id.player_position);
        playerDuration = findViewById(R.id.player_duration);
        seekBar = findViewById(R.id.seekAudio);



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