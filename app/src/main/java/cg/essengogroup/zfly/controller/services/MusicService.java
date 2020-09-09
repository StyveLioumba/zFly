package cg.essengogroup.zfly.controller.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.view.activities.AccueilActivity;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private int currentIndex=0;
    private ArrayList<Music> musics;
    private Music music;
    public static final int NOTIFICATION_ID = 234;


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer=new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        currentIndex=intent.getIntExtra("currentIndex",0);
        musics=intent.getParcelableArrayListExtra("arrayList");
        Log.e("TAG", "onStartCommand: "+musics.size()+"\nvide "+musics.isEmpty() );
        if (musics!=null){
            music=musics.get(currentIndex);
        }
        prepareMediaPlayer(music);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void autoPlay(Music music){
//        prepareMediaPlayer(music);
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }else {
            mediaPlayer.start();
        }
    }

    private void prepareMediaPlayer(Music music){
        if (music!=null){
            try {
                mediaPlayer.setDataSource(music.getChanson());
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        createNotification();
                        if (mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                        }else {
                            mediaPlayer.start();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createNotification(){

        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);

        Intent intent = new Intent(this, AccueilActivity.class);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(AccueilActivity.class);
        taskStackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = taskStackBuilder.
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Sample Notification")
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent)
                    .setCustomContentView(notificationLayoutExpanded)
                    .build();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);

    }

}
