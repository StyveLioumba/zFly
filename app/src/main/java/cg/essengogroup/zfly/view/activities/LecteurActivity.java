package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;


public class LecteurActivity extends AppCompatActivity {

    private static final int PERMISSION_STORAGE_CODE = 1000;
    private Intent intent;

    private ArrayList<Music> musics;
    private Music music;

    private ProgressBar progressBar;
    private SeekBar positionSeekBar,volumeSeekBar;
    private ImageButton btnPrevious,btnNext,btnDownload;
    private CircularImageView imageCover;

    private FloatingActionButton playBtn;
    private TextView debutDure,finDure,morceauName,nomAlbum,nbreEcouter;

    private Handler handler=new Handler();
    private MediaPlayer mediaPlayer;

    private int currentIndex=0;

    private Toolbar toolbar;

    private boolean isRepeat=true;

    private ImageView btnRepeat;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecteur);

        intent=getIntent();

        if (intent!=null){
            currentIndex=intent.getIntExtra("currentIndex",0);
            musics=intent.getParcelableArrayListExtra("arrayList");

            if (musics!=null){
                music=musics.get(currentIndex);
            }
        }

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database=FirebaseDatabase.getInstance();

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        morceauName=findViewById(R.id.txtChanson);
        nomAlbum=findViewById(R.id.txtArtiste);
        nbreEcouter=findViewById(R.id.txtNbreEcoute);
        progressBar=findViewById(R.id.progress);
        btnPrevious=findViewById(R.id.btnPrevious);
        btnNext=findViewById(R.id.btnNext);
        btnDownload=findViewById(R.id.download);
        btnRepeat=findViewById(R.id.repeat);

        imageCover=findViewById(R.id.imgCover);

        playBtn = findViewById(R.id.playBtn);
        debutDure = findViewById(R.id.elapsedTimeLabel);
        finDure = findViewById(R.id.remainingTimeLabel);

        positionSeekBar=findViewById(R.id.positionBar);
        volumeSeekBar = findViewById(R.id.volumeBar);

        addVolumeOnSong();

        mediaPlayer=new MediaPlayer();
        if (!MediaPlayer.class.isInstance(mediaPlayer)){
            mediaPlayer=new MediaPlayer();
        }

        positionSeekBar.setMax(100);

        if (isRepeat){
            btnRepeat.setImageResource(R.drawable.ic_repeat);
        }else {
            btnRepeat.setImageResource(R.drawable.ic_repeat_black_24dp);
        }

        autoPlay();
        getNumEcouter(music);

        /**
         * pour sauvegarder prendre en compte l'avancement de la seekbar par rapport a l'avancement du son
         */

        positionSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SeekBar seekBar= (SeekBar) v;
                int playPosition=(mediaPlayer.getDuration()/100)*seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                debutDure.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            }
        });

        /**
         * pour voir le chargement du son avant de demarrer
         */
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                positionSeekBar.setSecondaryProgress(percent);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                positionSeekBar.setProgress(0);
                debutDure.setText("0:00");
                finDure.setText("0:00");
                music=nextMusic();
                if (music!=null){
                    mediaPlayer.reset();
                    prepareMediaPlayer(music);
                    if (mediaPlayer.isPlaying()){
                        handler.removeCallbacks(updater);
                        mediaPlayer.pause();
                        playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                    }else {
                        mediaPlayer.start();
                        playBtn.setImageResource(android.R.drawable.ic_media_pause);
                        updateSeekBar();
                    }
                }else {
                    music=musics.get(0);
                    prepareMediaPlayer(music);
                }
            }
        });


        playBtn.setOnClickListener(v->{
            if (mediaPlayer.isPlaying()){
                handler.removeCallbacks(updater);
                mediaPlayer.pause();
                playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            }else {
                mediaPlayer.start();
                playBtn.setImageResource(android.R.drawable.ic_media_pause);
                updateSeekBar();

            }
        });

        btnNext.setOnClickListener(v->{
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer.reset();
                positionSeekBar.setProgress(0);
                debutDure.setText("0:00");
                finDure.setText("0:00");
                music=nextMusic();

                prepareMediaPlayer(music);
                if (mediaPlayer.isPlaying()){
                    handler.removeCallbacks(updater);
                    mediaPlayer.pause();
                    playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                }else {
                    mediaPlayer.start();
                    playBtn.setImageResource(android.R.drawable.ic_media_pause);
                    updateSeekBar();

                }
            }else {
                mediaPlayer.reset();
                positionSeekBar.setProgress(0);
                debutDure.setText("0:00");
                finDure.setText("0:00");
                music=nextMusic();
                prepareMediaPlayer(music);
                mediaPlayer.start();
                playBtn.setImageResource(android.R.drawable.ic_media_pause);
                updateSeekBar();
            }
        });

        btnPrevious.setOnClickListener(v->{
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer.reset();
                positionSeekBar.setProgress(0);
                debutDure.setText("0:00");
                finDure.setText("0:00");
                music=previousMusic();
                prepareMediaPlayer(music);
                if (mediaPlayer.isPlaying()){
                    handler.removeCallbacks(updater);
                    mediaPlayer.pause();
                    playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                }else {
                    mediaPlayer.start();
                    playBtn.setImageResource(android.R.drawable.ic_media_pause);
                    updateSeekBar();

                }
            }else {
                mediaPlayer.reset();
                positionSeekBar.setProgress(0);
                debutDure.setText("0:00");
                finDure.setText("0:00");
                music=previousMusic();
                prepareMediaPlayer(music);
                mediaPlayer.start();
                playBtn.setImageResource(android.R.drawable.ic_media_pause);
                updateSeekBar();
            }
        });

        btnDownload.setOnClickListener(v->{
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    String[] permissions={
                      Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };

                    requestPermissions(permissions,PERMISSION_STORAGE_CODE);
                }else {
                    startDownloading(music);
                }
            }else {
                startDownloading(music);
            }
        });

        btnRepeat.setOnClickListener(v->{
            if (isRepeat){
                isRepeat=false;
                btnRepeat.setImageResource(R.drawable.ic_repeat_black_24dp);
            }else {
                isRepeat=true;
                btnRepeat.setImageResource(R.drawable.ic_repeat);
            }
        });
    }

    private void autoPlay(){
        prepareMediaPlayer(music);
        if (mediaPlayer.isPlaying()){
            handler.removeCallbacks(updater);
            mediaPlayer.pause();
            playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        }else {
            mediaPlayer.start();
            playBtn.setImageResource(android.R.drawable.ic_media_pause);
            updateSeekBar();

        }
    }

    private void prepareMediaPlayer(Music music){
        if (music!=null){
            getNumEcouter(music);
            progressBar.setVisibility(View.VISIBLE);

            Picasso.get()
                    .load( music.getCover())
                    .placeholder(R.drawable.music_cover)
                    .error(R.drawable.music_cover)
                    .into(imageCover);

            morceauName.setText(music.getMorceau());
            nomAlbum.setText(music.getAlbum());
            try {
                mediaPlayer.setDataSource(music.getChanson());
                mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                finDure.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable updater=new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            long currentDuration=mediaPlayer.getCurrentPosition();
            debutDure.setText(milliSecondsToTimer(currentDuration));
        }
    };

    private void updateSeekBar(){
        if (mediaPlayer.isPlaying()){
            positionSeekBar.setProgress(
                    (int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration())*100)
            );

            handler.postDelayed(updater,1000);
        }
    }

    private String milliSecondsToTimer(long milliSeconds){
        String timerString="";
        String secondsString;

        int heure=(int) (milliSeconds/(1000*60*60));
        int minute=(int) (milliSeconds % (1000*60*60))/ (1000*60);
        int seconde=(int) ((milliSeconds % (1000*60*60)) % (1000*60)/1000);

        if (heure > 0){
            timerString=heure+":";
        }

        if (seconde<10){
            secondsString="0"+seconde;
        }else {
            secondsString=""+seconde;
        }
        timerString=timerString+minute+":"+secondsString;


        return timerString;
    }

    private Music previousMusic(){

        playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        currentIndex=currentIndex-1;
        Music music=new Music();

        if (musics.size()>currentIndex && currentIndex>0){
            music=musics.get(currentIndex);
        }else if (currentIndex==0){
            music=musics.get(currentIndex);
        }else if (currentIndex<0){
            currentIndex=musics.size()-1;
            music=musics.get(currentIndex);
        }
        setNumEcouter(music);
        return music;
    }

    private Music nextMusic(){

        playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        currentIndex=currentIndex+1;
        Music music=new Music();

        if (musics.size()>currentIndex){
            music=musics.get(currentIndex);
            setNumEcouter(music);
        }else if (currentIndex>=musics.size()){
            if (isRepeat){
                currentIndex=0;
                music=musics.get(currentIndex);
                setNumEcouter(music);
            }else {
                music=null;
            }
        }
        return music;
    }

    private void addVolumeOnSong(){
        // Volume Bar
        volumeSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        mediaPlayer.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    private void getNumEcouter(Music music){

        reference=database.getReference("music/morceaux/"+music.getRacine()+"/ecouter");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    nbreEcouter.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setNumEcouter(Music music){
        reference=database.getReference("music/morceaux/"+music.getRacine()+"/ecouter");

        Map<String,Object> data=new HashMap<>();
        data.put("createAt", ServerValue.TIMESTAMP);

        reference.child("ecoute"+System.currentTimeMillis())
                .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    private void startDownloading(Music music){
        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(music.getChanson()));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle("Telechargement");
        request.setDescription("Telechargement du fichier "+music.getMorceau()+".mp3 encours ...");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,music.getMorceau()+System.currentTimeMillis());

        DownloadManager manager= (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_STORAGE_CODE:
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    startDownloading(music);
                }else {
                    Toast.makeText(this, "Permission refusé ...!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }
}
