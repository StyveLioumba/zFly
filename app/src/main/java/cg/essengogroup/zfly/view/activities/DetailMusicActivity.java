package cg.essengogroup.zfly.view.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.Request.DetailRequest;
import cg.essengogroup.zfly.controller.adapter.DetailMusicAdapter;
import cg.essengogroup.zfly.controller.interfaces.MorceauInterface;
import cg.essengogroup.zfly.model.Music;

public class DetailMusicActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DetailMusicAdapter adapter;
    private ArrayList<Music> musicArrayList;

    private Intent intent;

    private int currentIndex;
    private String keyValue,categorieValue;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_music);

        database= FirebaseDatabase.getInstance();

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();


        intent=getIntent();
        if (intent!=null){
            keyValue=intent.getStringExtra("key");
            categorieValue=intent.getStringExtra("categorieKey");
        }
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(keyValue);
        }

        recyclerView=findViewById(R.id.recycleDetailMusic);
        musicArrayList=new ArrayList<>();

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);
        adapter=new DetailMusicAdapter(DetailMusicActivity.this,musicArrayList,(music, postion) -> {
            changeSelectedMusic(postion);
            reference=database.getReference("music/morceaux/"+music.getRacine()+"/ecouter");

            Map<String,Object> data=new HashMap<>();
            data.put("createAt", ServerValue.TIMESTAMP);

            reference.child("ecoute"+System.currentTimeMillis())
                    .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    startActivity(new Intent(DetailMusicActivity.this, LecteurActivity.class)
                            .putParcelableArrayListExtra("arrayList",musicArrayList)
                            .putExtra("currentIndex",currentIndex)
                    );
                }
            });


        });
        recyclerView.setAdapter(adapter);

        getData();

    }

    private void getData(){
        DetailRequest request=new DetailRequest(keyValue,categorieValue);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
