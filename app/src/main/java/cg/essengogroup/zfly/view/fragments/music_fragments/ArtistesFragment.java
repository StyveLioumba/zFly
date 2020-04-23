package cg.essengogroup.zfly.view.fragments.music_fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.ArtisteAdapter;
import cg.essengogroup.zfly.model.Music;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistesFragment extends Fragment {

    private View root;
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<Music> musicArrayList;
    private ArtisteAdapter adapter;

    private DatabaseReference referenceMusic;
    private FirebaseDatabase database;

    public ArtistesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root= inflater.inflate(R.layout.fragment_artistes, container, false);

        context=getContext();

        database=FirebaseDatabase.getInstance();
        referenceMusic=database.getReference("music/artistes");

        recyclerView=root.findViewById(R.id.recycleArtiste);
        GridLayoutManager manager=new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        musicArrayList=new ArrayList<>();

        getData();

        return root;
    }

    private void getData(){
        referenceMusic.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                musicArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Music music=new Music();

                    music.setCover(""+data.child("image_artiste").getValue());
                    music.setArtiste(""+data.child("artiste").getValue());

                    musicArrayList.add(music);
                }


                adapter=new ArtisteAdapter(context,musicArrayList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
