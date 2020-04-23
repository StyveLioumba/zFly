package cg.essengogroup.zfly.view.fragments;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;

/**
 * A simple {@link Fragment} subclass.
 */
public class LecteurFragment extends Fragment {

    private Activity activity;

    private ArrayList<Music> musics;
    private Music music;

    private TextView morceauName;
    private ProgressBar progressBar;
    private FloatingActionButton btnPlay;
    private SeekBar seekBar;
    private ImageButton btnPrevious,btnNext;

    private MediaPlayer mediaPlayer;

    private long currentSongLength;
    private int position,currentIndex;
    private boolean firstLaunch=true;

    private View root;

    public LecteurFragment(ArrayList<Music>musics,Music music,int position,int currentIndex) {
        // Required empty public constructor
        this.musics=musics;
        this.music=music;
        this.position=position;
        this.currentIndex=currentIndex;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity=getActivity();
        root= inflater.inflate(R.layout.fragment_lecteur, container, false);

        morceauName=root.findViewById(R.id.txtChanson);
        progressBar=root.findViewById(R.id.progress);
        btnPlay=root.findViewById(R.id.playBtn);
        seekBar=root.findViewById(R.id.positionBar);
        btnPrevious=root.findViewById(R.id.btnPrevious);
        btnNext=root.findViewById(R.id.btnNext);

        mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                togglePlay(mp);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (currentIndex+1<musics.size()){
                    Music next=musics.get(currentIndex+1);
                    prepareSong(next);
                }else {
                    Music next=musics.get(0);
                    prepareSong(next);
                }
            }
        });

        btnPlay.setOnClickListener(v->{
            firstLaunch=false;
            prepareSong(music);
        });

        pushPlay();
        pushNext();
        pushPrevious();
        return root;
    }

    private void prepareSong(Music music){
        progressBar.setVisibility(View.VISIBLE);
        morceauName.setText(music.getMorceau());
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(music.getChanson());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePlay(MediaPlayer mp){
        if (mp.isPlaying()){
            mp.stop();
            mp.reset();
        }else{
            progressBar.setVisibility(View.GONE);
            mp.start();
            btnPlay.setImageResource(android.R.drawable.ic_media_pause);

            /*Handler handler=new Handler();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setMax();
                }
            });*/
        }
    }

    private void pushPlay(){
        btnPlay.setOnClickListener(v->{

            if (mediaPlayer.isPlaying() && mediaPlayer!=null){
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
                mediaPlayer.pause();
            }else {
                if (firstLaunch ){
//                    Music music=musics.get(0);
                    prepareSong(music);
                }else {
                    mediaPlayer.start();
                    firstLaunch=false;
                }
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
            }
        });
    }

    private void pushPrevious(){
        btnPrevious.setOnClickListener(v->{
            firstLaunch=false;
            if (mediaPlayer!=null){
                if (currentIndex-1>=0){
                    Music previous=musics.get(currentIndex - 1);
                    prepareSong(previous);
                }
                else {
                    prepareSong(musics.get(musics.size() - 1));
                }
            }
        });
    }

    private void pushNext(){
        btnNext.setOnClickListener(v->{
            firstLaunch=false;
            if (mediaPlayer!=null){
                if (currentIndex + 1<musics.size()){
                    Music next=musics.get(currentIndex+1);
                    prepareSong(next);
                }else {
                    prepareSong(musics.get(0));
                }
            }
        });

    }

    private void destroy(){
        if (mediaPlayer !=null){
            mediaPlayer.release();;
        }
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }
}
