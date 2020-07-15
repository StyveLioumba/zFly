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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.TopTenAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopFragment extends Fragment {


    private View root;
    private Context context;

    private Query referenceMusic,referenceEcouter;
    private FirebaseDatabase database;

    private RecyclerView recyclerView;
    private ArrayList<String> musicArrayList;
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

        database=FirebaseDatabase.getInstance();
        referenceMusic=database.getReference("music/morceaux");

        recyclerView=root.findViewById(R.id.listTop);
        musicArrayList=new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        getData();

        return root;
    }

    private void getData(){
        referenceMusic.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                musicArrayList.clear();
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    getDataEcouter(data.getKey(),String.valueOf(data.child("morceau").getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataEcouter(String songRef,String morceau){

        referenceEcouter=database.getReference("music/morceaux/"+songRef+"/ecouter").orderByValue();

        referenceEcouter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String topValue="("+dataSnapshot.getChildrenCount()+") "+morceau;
                musicArrayList.add(topValue);
                adapter=new TopTenAdapter(context,musicArrayList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
