package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;
import dm.audiostreamer.AudioStreamingManager;
import dm.audiostreamer.CurrentSessionCallback;
import dm.audiostreamer.MediaMetaData;

import android.Manifest;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cg.essengogroup.zfly.controller.utils.Methodes.formateMilliSeccond;

public class MusicLecteurActivity extends AppCompatActivity implements CurrentSessionCallback {

    private static final int PERMISSION_STORAGE_CODE = 1000;

    private Intent intent;
    private AudioStreamingManager streamingManager;
    private ArrayList<Music> musicArrayList=new ArrayList<>();
    private Music music;
    private int currentPosition=0;
    private MediaMetaData mediaMetaData,currentAudio_;

    private SeekBar positionSeekBar,volumeSeekBar;
    private ImageButton btnPrevious,btnNext,btnDownload,btnShare;
    private CircularImageView imageCover;

    private FloatingActionButton playBtn;
    private TextView debutDure,finDure,morceauName,nomAlbum,nbreEcouter;
    private Toolbar toolbar;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_lecteur);

        intent=getIntent();
        if (intent!=null){
            musicArrayList=intent.getParcelableArrayListExtra("arrayList");
            currentPosition=intent.getIntExtra("currentIndex",0);
            music=musicArrayList.get(currentPosition);
        }

        serialisation();

        mediaMetaData=new MediaMetaData();
        streamingManager = AudioStreamingManager.getInstance(MusicLecteurActivity.this);

        Intent intent = new Intent(this, AccueilActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        streamingManager.setMediaList(listMedias());
        streamingManager.setShowPlayerNotification(true);
        streamingManager.setPendingIntentAct(pendingIntent);
        streamingManager.setPlayMultiple(true);

        prepareSong(music);
    }

    private List<MediaMetaData> listMedias(){
        List<MediaMetaData> mediaList=new ArrayList<>();
        mediaList.clear();
        StringBuilder builder=new StringBuilder();
        builder.delete(0,builder.length());
        for (int i=0;i<musicArrayList.size();i++){
            Music music=musicArrayList.get(i);

            builder.append(music.getUser_id().concat(" "));
            builder.append(music.getRacine().concat(" "));

            MediaMetaData mediaMetaData=new MediaMetaData();
            mediaMetaData.setMediaId(String.valueOf(i));
            mediaMetaData.setMediaUrl(music.getChanson());
            mediaMetaData.setMediaTitle(music.getMorceau());
            mediaMetaData.setMediaAlbum(music.getAlbum());
            mediaMetaData.setMediaArtist(music.getArtiste());
            mediaMetaData.setMediaDuration(music.getDuration());
            mediaMetaData.setMediaArt(music.getCover());
            mediaMetaData.setMediaComposer(builder.toString());
            mediaList.add(mediaMetaData);
        }
        return mediaList;
    }

    private void prepareSong(Music music){
        StringBuilder builder=new StringBuilder();
        builder.delete(0,builder.length());
        builder.append(music.getUser_id().concat(" "));
        builder.append(music.getRacine().concat(" "));

        mediaMetaData.setMediaId(String.valueOf(currentPosition));
        mediaMetaData.setMediaUrl(music.getChanson());
        mediaMetaData.setMediaTitle(music.getMorceau());
        mediaMetaData.setMediaAlbum(music.getAlbum());
        mediaMetaData.setMediaArtist(music.getArtiste());
        mediaMetaData.setMediaDuration(music.getDuration());
        mediaMetaData.setMediaArt(music.getCover());
        mediaMetaData.setMediaComposer(builder.toString());
        currentAudio_=mediaMetaData;
        streamingManager.onPlay(mediaMetaData);
        playBtn.setImageResource(R.drawable.ic_action_pause);
        setInfoMedia(mediaMetaData);
        setAction();
        setNumEcouter(mediaMetaData);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (streamingManager != null) {
            streamingManager.subscribesCallBack(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (streamingManager != null) {
            streamingManager.unSubscribeCallBack();
        }
    }

    @Override
    public void updatePlaybackState(int state) {
        switch (state) {
            case PlaybackStateCompat.STATE_PLAYING:
                Log.e("TAG", "updatePlaybackState: play" );
                playBtn.setImageResource(R.drawable.ic_action_pause);
                setInfoMedia(streamingManager.getCurrentAudio());
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                Log.e("TAG", "updatePlaybackState: pause " );
                playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                setInfoMedia(streamingManager.getCurrentAudio());
                break;
            case PlaybackStateCompat.STATE_NONE:
                Log.e("TAG", "updatePlaybackState: none " );
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                Log.e("TAG", "updatePlaybackState: stop " );
                playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                Log.e("TAG", "updatePlaybackState: buff " );
                playBtn.setImageResource(R.drawable.ic_action_pause);
                break;
        }
    }

    @Override
    public void playSongComplete() {
        Log.e("TAG", "playSongComplete: " );
        setInfoMedia(streamingManager.getCurrentAudio());
    }

    @Override
    public void currentSeekBarPosition(int progress) {

       Log.e("TAG", "currentSeekBarPosition: "+progress );
         positionSeekBar.setProgress(
                (int) (((float) progress / Integer.parseInt(currentAudio_.getMediaDuration()))*100)
        );
        debutDure.setText(
                formateMilliSeccond(
                        progress
                )
        );

    }

    @Override
    public void playCurrent(int indexP, MediaMetaData currentAudio) {
        Log.e("TAG", "indexP: currentAudio "+currentAudio+"\n indexP"+indexP );
        currentAudio_=currentAudio;
        setInfoMedia(currentAudio);
    }

    @Override
    public void playNext(int indexP, MediaMetaData currentAudio) {
        Log.e("TAG", "playNext: currentAudio "+currentAudio+"\n indexP"+indexP );
        currentAudio_=currentAudio;
        setNumEcouter(currentAudio);
        setInfoMedia(currentAudio);
    }

    @Override
    public void playPrevious(int indexP, MediaMetaData currentAudio) {
        Log.e("TAG", "playPrevious: currentAudio "+currentAudio+"\n indexP"+indexP );
        currentAudio_=currentAudio;
        setNumEcouter(currentAudio);
        setInfoMedia(currentAudio);
    }


    private void serialisation(){
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database= FirebaseDatabase.getInstance();

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        morceauName=findViewById(R.id.txtChanson);
        nomAlbum=findViewById(R.id.txtArtiste);
        nbreEcouter=findViewById(R.id.txtNbreEcoute);
        btnPrevious=findViewById(R.id.btnPrevious);
        btnNext=findViewById(R.id.btnNext);
        btnDownload=findViewById(R.id.download);
        btnShare=findViewById(R.id.share);

        imageCover=findViewById(R.id.imgCover);

        playBtn = findViewById(R.id.playBtn);
        debutDure = findViewById(R.id.elapsedTimeLabel);
        finDure = findViewById(R.id.remainingTimeLabel);

        positionSeekBar=findViewById(R.id.positionBar);
        volumeSeekBar = findViewById(R.id.volumeBar);
    }

    private void setInfoMedia(MediaMetaData currentAudio){
        Picasso.get()
                .load( currentAudio.getMediaArt())
                .placeholder(R.drawable.music_cover)
                .error(R.drawable.music_cover)
                .into(imageCover);

        morceauName.setText(currentAudio.getMediaTitle());
        nomAlbum.setText(currentAudio.getMediaArtist());
    }

    private void setAction(){
        btnDownload.setOnClickListener(v->{
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    String[] permissions={
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };

                    requestPermissions(permissions,PERMISSION_STORAGE_CODE);
                }else {
                    startDownloading(currentAudio_);
                    Toast.makeText(this, "Téléchargement encours...", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                startDownloading(currentAudio_);
                Toast.makeText(this, "Téléchargement encours...", Toast.LENGTH_SHORT).show();
            }
        });

        playBtn.setOnClickListener(v->{
            if (streamingManager!=null){
                if (streamingManager.isPlaying()){
                    streamingManager.onPause();
                    playBtn.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                }else {
                    playBtn.setImageResource(R.drawable.ic_action_pause);
                    streamingManager.onPlay(streamingManager.getCurrentAudio());
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            streamingManager.onSkipToNext();
        });

        btnPrevious.setOnClickListener(v -> {
            streamingManager.onSkipToPrevious();
        });

        btnShare.setOnClickListener(v->{
            share(currentAudio_.getMediaUrl());
        });

        finDure.setText(formateMilliSeccond(Long.parseLong(currentAudio_.getMediaDuration())));

    }

    private void startDownloading(MediaMetaData currentAudio){
        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(currentAudio.getMediaUrl()));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle("Telechargement");
        request.setDescription("Telechargement du fichier "+currentAudio.getMediaTitle()+".mp3 encours ...");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,currentAudio.getMediaTitle()+System.currentTimeMillis());

        DownloadManager manager= (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private void share(String body){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.app_name)));
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

    private void setNumEcouter(MediaMetaData currentAudio){

        String[] tabComposer=currentAudio.getMediaComposer().split(" ");

        reference=database.getReference("music/morceaux/"+tabComposer[1]+"/ecouter");

        Map<String,Object> data=new HashMap<>();
        data.put("createAt", ServerValue.TIMESTAMP);

        reference.child("ecoute"+System.currentTimeMillis())
                .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                getNumEcouter(currentAudio);
            }
        });
    }

    private void getNumEcouter(MediaMetaData currentAudio){

        String[] tabComposer=currentAudio.getMediaComposer().split(" ");

        reference=database.getReference("music/morceaux/"+tabComposer[1]+"/ecouter");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    nbreEcouter.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    saveNumEcouterUser(currentAudio,String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveNumEcouterUser(MediaMetaData currentAudio,String count){
        String[] tabComposer=currentAudio.getMediaComposer().split(" ");
        reference=database.getReference("users/"+tabComposer[0]);
        reference.child("nombreEcoute").setValue(count);
    }
}
