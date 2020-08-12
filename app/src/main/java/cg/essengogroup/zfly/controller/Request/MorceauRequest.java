package cg.essengogroup.zfly.controller.Request;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cg.essengogroup.zfly.controller.interfaces.MorceauInterface;
import cg.essengogroup.zfly.model.Music;

public class MorceauRequest {

    private DatabaseReference referenceMusic;
    private FirebaseDatabase database;
    private Query query;

    public MorceauRequest() {
        database=FirebaseDatabase.getInstance();
        referenceMusic=database.getReference("music/morceaux");

        query=referenceMusic.orderByChild("genre").equalTo("instrumental");
    }

    public void getSongList(MorceauInterface morceauInterface){

        referenceMusic.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Music> musics=new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Music music=new Music();

                    music.setAlbum(String.valueOf(data.child("album").getValue()));
                    music.setArtiste(String.valueOf(data.child("artiste").getValue()));
                    music.setChanson(String.valueOf(data.child("chanson").getValue()));
                    music.setCover(String.valueOf(data.child("cover").getValue()));
                    music.setGenre(String.valueOf(data.child("genre").getValue()));
                    music.setMorceau(String.valueOf(data.child("morceau").getValue()));
                    music.setUser_id(String.valueOf(data.child("user_id").getValue()));
                    music.setRacine(String.valueOf(data.getKey()));

                    musics.add(music);
                }
                morceauInterface.onScucces(musics);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getSongListInstrumental(MorceauInterface morceauInterface){

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Music> musics=new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Music music=new Music();

                    music.setAlbum(String.valueOf(data.child("album").getValue()));
                    music.setArtiste(String.valueOf(data.child("artiste").getValue()));
                    music.setChanson(String.valueOf(data.child("chanson").getValue()));
                    music.setCover(String.valueOf(data.child("cover").getValue()));
                    music.setGenre(String.valueOf(data.child("genre").getValue()));
                    music.setMorceau(String.valueOf(data.child("morceau").getValue()));
                    music.setUser_id(String.valueOf(data.child("user_id").getValue()));
                    music.setRacine(String.valueOf(data.getKey()));

                    musics.add(music);
                }
                morceauInterface.onScucces(musics);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
