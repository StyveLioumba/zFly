package cg.essengogroup.zfly.view.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import cg.essengogroup.zfly.R;

public class DialogMusicAccueil extends Dialog {

    private Context context;
    private FloatingActionButton playBtn;
    private SeekBar positionBar;
    private SeekBar volumeBar;
    private TextView elapsedTimeLabel;
    private MediaPlayer mp;
    private int totalTime;
    private String lienAudio;
    private ProgressDialog chargement;

    public DialogMusicAccueil(@NonNull Context context,String lienAudio) {
        super(context);
        this.context=context;
        this.lienAudio=lienAudio;
    }

    @Override
    protected void onStart() {
        super.onStart();
        chargement=new ProgressDialog(context);
        chargement.setMessage("chargement...");
        chargement.setCancelable(false);
        chargement.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_music_accueil);

        playBtn = findViewById(R.id.playBtn);
        elapsedTimeLabel = findViewById(R.id.elapsedTimeLabel);


        // Media Player
//        mp = MediaPlayer.create(context, Uri.parse(lienAudio));
        mp = new MediaPlayer();
        try {
            mp.setDataSource(lienAudio);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.setLooping(false);
        mp.seekTo(0);
        mp.setVolume(0.5f, 0.5f);
        totalTime = mp.getDuration();

        // Position Bar
        positionBar =  findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        // Volume Bar
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        mp.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        // Thread (Update positionBar & timeLabel)
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(900);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();

        playBtn.setOnClickListener(v->{
            if (mp!=null &&!mp.isPlaying()) {
                // Stopping
                mp.start();
                playBtn.setImageResource(R.drawable.ic_stop_black_24dp);

            } else {
                // Playing
                mp.pause();
                playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            }
        });

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                chargement.dismiss();
                if (!mp.isPlaying()) {
                    // Stopping
                    mp.start();
                    playBtn.setImageResource(R.drawable.ic_stop_black_24dp);

                } else {
                    // Playing
                    mp.pause();
                    playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                }
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                if (isShowing()) {
                    dismiss();
                }
            }
        });

    }

    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            positionBar.setProgress(currentPosition);

            // Update Labels.
            String elapsedTime = createTimeLabel(currentPosition);

//            String remainingTime = createTimeLabel(totalTime-currentPosition);

            elapsedTimeLabel.setText((elapsedTime+"/"+createTimeLabel(totalTime)).toLowerCase());

        }
    };

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mp!=null && mp.isPlaying()){
            mp.stop();
            mp.release();
            mp = null;
        }
        if (isShowing()){
            dismiss();
        }
    }
}
