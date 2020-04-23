package cg.essengogroup.zfly.view.fragments.music_fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.Request.MorceauRequest;
import cg.essengogroup.zfly.controller.adapter.MorceauAdapter;
import cg.essengogroup.zfly.controller.interfaces.MorceauInterface;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.view.activities.LecteurActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChansonsFragment extends Fragment {

    private View root;
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<Music> musicArrayList;
    private MorceauAdapter adapter;

    private int currentIndex;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    public ChansonsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root= inflater.inflate(R.layout.fragment_chansons, container, false);

        context=getContext();
        database=FirebaseDatabase.getInstance();

        musicArrayList=new ArrayList<>();

        recyclerView=root.findViewById(R.id.recycleMorceau);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);
        adapter=new MorceauAdapter(context,musicArrayList,(music, postion) -> {

            reference=database.getReference("music/morceaux/"+music.getRacine()+"/ecouter");

            Map<String,Object> data=new HashMap<>();
            data.put("createAt", ServerValue.TIMESTAMP);

            reference.child("ecoute"+System.currentTimeMillis())
                    .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    changeSelectedMusic(postion);
                    startActivity(new Intent(context, LecteurActivity.class)
                            .putParcelableArrayListExtra("arrayList",musicArrayList)
                            .putExtra("currentIndex",currentIndex)
                    );
                }
            });


        });
        recyclerView.setAdapter(adapter);

        getData();

        return root;
    }

    private void getData(){
        MorceauRequest request=new MorceauRequest();
        request.getSongList(new MorceauInterface() {
            @Override
            public void onScucces(ArrayList<Music> musics) {
                currentIndex=0;
                musicArrayList.clear();
                musicArrayList.addAll(musics);
                adapter.notifyDataSetChanged();
                adapter.setSelectedPostion(0);
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }

    private void changeSelectedMusic(int index){
        adapter.notifyItemChanged(adapter.getSelectedPostion());
        currentIndex=index;
        adapter.setSelectedPostion(currentIndex);
        adapter.notifyItemChanged(currentIndex);
    }
}
