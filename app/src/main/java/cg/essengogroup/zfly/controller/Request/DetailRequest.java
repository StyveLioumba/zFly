package cg.essengogroup.zfly.controller.Request;

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

public class DetailRequest {
    private Query referenceMusic;
    private FirebaseDatabase database;
    private String motifRequette,categorie;

    public DetailRequest(String motifRequette,String categorie) {
        this.motifRequette=motifRequette;
        this.categorie=categorie;
        database=FirebaseDatabase.getInstance();
        referenceMusic=database.getReference()
                .child("music")
                .child("morceaux")
                .orderByChild(categorie).startAt(motifRequette).endAt(motifRequette);
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
                    music.setTime(String.valueOf(data.child("time").getValue()));
                    music.setDuration(String.valueOf(data.child("duration").getValue()));
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
