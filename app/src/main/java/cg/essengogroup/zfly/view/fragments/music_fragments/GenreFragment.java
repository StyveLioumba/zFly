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

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.GenreAdapter;
import cg.essengogroup.zfly.controller.utils.Methodes;
import cg.essengogroup.zfly.controller.utils.VolleySingleton;
import cg.essengogroup.zfly.model.Music;

/**
 * A simple {@link Fragment} subclass.
 */
public class GenreFragment extends Fragment {

    private View root;
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<Music> musicArrayList;
    private GenreAdapter adapter;

    public GenreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root= inflater.inflate(R.layout.fragment_genre, container, false);

        context=getContext();

        recyclerView=root.findViewById(R.id.recycleGenre);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));

        recyclerView.setHasFixedSize(true);

        musicArrayList=new ArrayList<>();

        getData();

        return root;
    }


    private void getData(){
        /*referenceMusic.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                musicArrayList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Music music=new Music();

                    music.setGenre(""+data.child("genre").getValue());
                    musicArrayList.add(music);
                }


                adapter=new GenreAdapter(context,musicArrayList);
                recyclerView.setAdapter(adapter);
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

                            adapter=new GenreAdapter(context,musicArrayList);
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
