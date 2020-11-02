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
import cg.essengogroup.zfly.controller.adapter.AlbumAdapter;
import cg.essengogroup.zfly.controller.utils.Methodes;
import cg.essengogroup.zfly.controller.utils.VolleySingleton;
import cg.essengogroup.zfly.model.Music;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumFragment extends Fragment {

    private View root;
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<Music> musicArrayList;
    private AlbumAdapter adapter;

    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root=inflater.inflate(R.layout.fragment_album, container, false);

        context=getContext();

        recyclerView=root.findViewById(R.id.recycleAlbum);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
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

                    music.setCover(""+data.child("cover").getValue());
                    music.setAlbum(""+data.child("album").getValue());
                    musicArrayList.add(music);
                }

                adapter=new AlbumAdapter(context,musicArrayList);
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
                    adapter=new AlbumAdapter(context,musicArrayList);
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
