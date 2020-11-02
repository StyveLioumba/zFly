package cg.essengogroup.zfly.view.fragments.music_fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.TopTenAdapter;
import cg.essengogroup.zfly.controller.utils.Methodes;
import cg.essengogroup.zfly.controller.utils.VolleySingleton;
import cg.essengogroup.zfly.model.Music;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopFragment extends Fragment {


    private View root;
    private Context context;

    private RecyclerView recyclerView;
    private ArrayList<Music> musicArrayList;
    private TopTenAdapter adapter;

    public TopFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root= inflater.inflate(R.layout.fragment_top, container, false);

        context=getContext();

        recyclerView=root.findViewById(R.id.listTop);
        musicArrayList=new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        getData();

        return root;
    }

    private void getData(){
        /*referenceMusic.orderByChild("rang").limitToFirst(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnap) {
                for (DataSnapshot dataSnapshot:dataSnap.getChildren()){
                    Music music=new Music();

//                    music.setAlbum(String.valueOf(dataSnapshot.child("album").getValue()));
//                    music.setChanson(String.valueOf(dataSnapshot.child("chanson").getValue()));
//                    music.setGenre(String.valueOf(dataSnapshot.child("genre").getValue()));
//                    music.setUser_id(String.valueOf(dataSnapshot.child("user_id").getValue()));
//                    music.setRacine(String.valueOf(dataSnapshot.getKey()));
                    music.setArtiste(String.valueOf(dataSnapshot.child("artiste").getValue()));
                    music.setCover(String.valueOf(dataSnapshot.child("cover").getValue()));
                    music.setNbreEcoute(String.valueOf(dataSnapshot.child("ecouter").getValue()));
                    music.setMorceau(String.valueOf(dataSnapshot.child("morceau").getValue()));

                    musicArrayList.add(music);
                    adapter=new TopTenAdapter(context,musicArrayList);
                    recyclerView.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        StringRequest stringRequest=new StringRequest(Request.Method.DEPRECATED_GET_OR_POST,"le lien ici",
                response -> {

                    if (!TextUtils.isEmpty(response)){
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            JSONArray jsonArray=jsonObject.getJSONArray("data");

                            musicArrayList.clear();

                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject object=jsonArray.getJSONObject(i);
                                Music music=new Gson().fromJson(String.valueOf(object),Music.class);
                                musicArrayList.add(music);
                            }

                            adapter=new TopTenAdapter(context,musicArrayList);
                            recyclerView.setAdapter(adapter);

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    Methodes.volleyError(context,error);
                });
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
