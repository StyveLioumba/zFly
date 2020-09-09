package cg.essengogroup.zfly.view.fragments;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager.widget.ViewPager;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.Request.MorceauRequest;
import cg.essengogroup.zfly.controller.adapter.SectionPagerAdapter;
import cg.essengogroup.zfly.controller.interfaces.MorceauInterface;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.view.activities.AccueilActivity;
import cg.essengogroup.zfly.view.activities.MusicLecteurActivity;
import cg.essengogroup.zfly.view.fragments.music_fragments.ArtistesFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.ChansonsFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.GenreFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.InstrumentalsFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.MixFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.TopFragment;
import dm.audiostreamer.AudioStreamingManager;
import dm.audiostreamer.CurrentSessionCallback;
import dm.audiostreamer.MediaMetaData;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment implements CurrentSessionCallback {

    private View root;
    private Context context;
    private FloatingActionButton fabMusic;
    private MediaMetaData mediaMetaData;
    private AudioStreamingManager streamingManager;
    private int currentPosition=0;
    private Music music;

    public MusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root= inflater.inflate(R.layout.fragment_music, container, false);
        context=getContext();

        setupViewPager();
        getData();

        mediaMetaData=new MediaMetaData();
        streamingManager = AudioStreamingManager.getInstance(context);

        Intent intent = new Intent(context, AccueilActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        streamingManager.setShowPlayerNotification(true);
        streamingManager.setPendingIntentAct(pendingIntent);
        streamingManager.setPlayMultiple(true);


        fabMusic=root.findViewById(R.id.fab_);
        fabMusic.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);

        return root;
    }

    private void setupViewPager(){
        SectionPagerAdapter adapter=new SectionPagerAdapter(getChildFragmentManager());

        adapter.addFragment(new ChansonsFragment());
        adapter.addFragment(new ArtistesFragment());
//        adapter.addFragment(new AlbumFragment());
        adapter.addFragment(new GenreFragment());
        adapter.addFragment(new TopFragment());
        adapter.addFragment(new InstrumentalsFragment());
        adapter.addFragment(new MixFragment());

        ViewPager viewPager=root.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout=root.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorToolbar));
        tabLayout.setTabTextColors(getResources().getColor(android.R.color.black),getResources().getColor(R.color.colorBlanc));

        tabLayout.getTabAt(0).setText("Chansons");
        tabLayout.getTabAt(1).setText("Artistes");
//        tabLayout.getTabAt(2).setText("Albums");
        tabLayout.getTabAt(2).setText("Genres");
        tabLayout.getTabAt(3).setText("Top");
        tabLayout.getTabAt(4).setText("Instru");
        tabLayout.getTabAt(5).setText("Mix Dj");

    }

    private void getData(){
        MorceauRequest request=new MorceauRequest();
        request.getSongList(new MorceauInterface() {
            @Override
            public void onScucces(ArrayList<Music> musics) {
                if (!musics.isEmpty()){
                    int min=0;
                    int max=musics.size();

                    currentPosition = (int) Math.floor(Math.random() * max);

                    streamingManager.setMediaList(listMedias(musics));

                    music=musics.get(currentPosition);

                    fabMusic.setEnabled(true);
                    fabMusic.setClickable(true);

                    fabMusic.setOnClickListener(v-> {
                        prepareSong(music);

                        if (streamingManager.isPlaying()){
                            fabMusic.setImageResource(R.drawable.ic_action_pause);
                        }else {
                            fabMusic.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                        }
                    });

                }else {
                    fabMusic.setEnabled(false);
                    fabMusic.setClickable(false);
                }

            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }


    private List<MediaMetaData> listMedias(ArrayList<Music> musics){
        List<MediaMetaData> mediaList=new ArrayList<>();
        mediaList.clear();
        StringBuilder builder=new StringBuilder();
        builder.delete(0,builder.length());
        for (int i=0;i<musics.size();i++){
            Music music=musics.get(i);

            builder.append(music.getUser_id().concat(" "));
            builder.append(music.getRacine().concat(" "));

            MediaMetaData mediaMetaData=new MediaMetaData();
            mediaMetaData.setMediaId(String.valueOf(i));
            mediaMetaData.setMediaUrl(music.getChanson());
            mediaMetaData.setMediaTitle(music.getMorceau());
            mediaMetaData.setMediaAlbum(music.getAlbum());
            mediaMetaData.setMediaArtist(music.getArtiste());
            mediaMetaData.setMediaDuration("03:55");
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
        mediaMetaData.setMediaDuration("03:55");
        mediaMetaData.setMediaArt(music.getCover());
        mediaMetaData.setMediaComposer(builder.toString());
        streamingManager.onPlay(mediaMetaData);
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
                fabMusic.setImageResource(R.drawable.ic_action_pause);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                Log.e("TAG", "updatePlaybackState: pause " );
                fabMusic.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                break;
            case PlaybackStateCompat.STATE_NONE:
                Log.e("TAG", "updatePlaybackState: none " );
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                Log.e("TAG", "updatePlaybackState: stop " );
                fabMusic.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                Log.e("TAG", "updatePlaybackState: buff " );
                break;
        }
    }

    @Override
    public void playSongComplete() {

    }

    @Override
    public void currentSeekBarPosition(int progress) {

    }

    @Override
    public void playCurrent(int indexP, MediaMetaData currentAudio) {

    }

    @Override
    public void playNext(int indexP, MediaMetaData currentAudio) {
        mediaMetaData=currentAudio;
    }

    @Override
    public void playPrevious(int indexP, MediaMetaData currentAudio) {
        mediaMetaData=currentAudio;
    }
}
