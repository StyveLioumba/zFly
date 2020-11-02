package cg.essengogroup.zfly.controller.Request;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cg.essengogroup.zfly.controller.interfaces.MorceauInterface;
import cg.essengogroup.zfly.controller.utils.Methodes;
import cg.essengogroup.zfly.controller.utils.VolleySingleton;
import cg.essengogroup.zfly.model.Music;

public class MorceauRequest {

    private DatabaseReference referenceMusic;
    private FirebaseDatabase database;
    private Query query,queryMix;
    private Context context;

    public MorceauRequest(Context context) {
        this.context = context;

        database=FirebaseDatabase.getInstance();
        referenceMusic=database.getReference("music/morceaux");

        query=referenceMusic.orderByChild("genre").equalTo("instrumental");
        queryMix=referenceMusic.orderByChild("genre").equalTo("Mix Dj");
    }

    public MorceauRequest() {
    }

    public void getSongList(MorceauInterface morceauInterface){

        /*referenceMusic.addValueEventListener(new ValueEventListener() {
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
        });*/

        StringRequest stringRequest=new StringRequest(Request.Method.DEPRECATED_GET_OR_POST,"le lien ici",
                response -> {
                    if (!TextUtils.isEmpty(response)){

                        ArrayList<Music> musics=new ArrayList<>();

                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            JSONArray jsonArray=jsonObject.getJSONArray("data");

                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject object=jsonArray.getJSONObject(i);

                                Music music=new Gson().fromJson(String.valueOf(object),Music.class);

                                musics.add(music);

                            }

                            morceauInterface.onScucces(musics);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    Methodes.volleyError(context,error);
                });
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
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

    public void getSongListMix(MorceauInterface morceauInterface){

        queryMix.addValueEventListener(new ValueEventListener() {
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
