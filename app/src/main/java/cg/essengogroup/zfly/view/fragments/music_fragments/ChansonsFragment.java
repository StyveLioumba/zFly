package cg.essengogroup.zfly.view.fragments.music_fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
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

import static cg.essengogroup.zfly.controller.utils.Methodes.isConnectingToInternet;

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
        LinearLayoutManager manager=new LinearLayoutManager(context);
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        adapter=new MorceauAdapter(context,musicArrayList,(music, postion) -> {

            reference=database.getReference("music/morceaux/"+music.getRacine()+"/ecouter");

            Map<String,Object> data=new HashMap<>();
            data.put("createAt", ServerValue.TIMESTAMP);

            if (isConnectingToInternet(context)){
                reference.child("ecoute"+System.currentTimeMillis())
                        .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        changeSelectedMusic(postion);
                        new PreparationAsync().execute(music);

                    /*startActivity(new Intent(context, LecteurActivity.class)
                            .putParcelableArrayListExtra("arrayList",musicArrayList)
                            .putExtra("currentIndex",currentIndex)
                    );*/
                    }
                });

            }else {
                Toast.makeText(context, "Veuillez vous connecté à internet", Toast.LENGTH_SHORT).show();
            }


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
                musicArrayList.clear();
                currentIndex=musics.size()-1;
                musicArrayList.addAll(musics);
                adapter.notifyDataSetChanged();
                adapter.setSelectedPostion(currentIndex);
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

    public class PreparationAsync extends AsyncTask<Music,Void,Boolean> {
        private ProgressDialog dialog;
        private MediaPlayer mp;
        private boolean isReady=false;

        public boolean isReady() {
            return isReady;
        }

        public void setReady(boolean ready) {
            isReady = ready;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog=new ProgressDialog(context);
            dialog.setMessage("chargement");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Music... music) {

            mp = new MediaPlayer();
            try {
                mp.setDataSource(music[0].getChanson());
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                  setReady(true);
                }
            });
            return isReady;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            dialog.dismiss();
            if (isReady()){
                startActivity(new Intent(context, LecteurActivity.class)
                        .putParcelableArrayListExtra("arrayList",musicArrayList)
                        .putExtra("currentIndex",currentIndex)
                );
            }else {
                Toast.makeText(context, "Veuillez vous connecté", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
