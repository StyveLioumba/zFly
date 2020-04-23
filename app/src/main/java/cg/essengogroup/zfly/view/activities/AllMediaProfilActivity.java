package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.AllMediaAdapter;
import cg.essengogroup.zfly.controller.adapter.MusicProfilAdapter;
import cg.essengogroup.zfly.model.Music;

public class AllMediaProfilActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private RecyclerView recyclerView;
    private ArrayList<Music> musicArrayList;
    private AllMediaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_media_profil);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("users/"+String.valueOf(user.getUid()));

        recyclerView=findViewById(R.id.recycleAllMedia);
        musicArrayList=new ArrayList<>();

        getData();

    }
    private void getData(){
        reference.child("chansons").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                musicArrayList.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    Music music=new Music();
                    music.setArtiste(String.valueOf(data.child("artiste").getValue()));
                    music.setMorceau(String.valueOf(data.child("morceau").getValue()));
                    music.setChanson(String.valueOf(data.child("chanson").getValue()));
                    music.setRacine(String.valueOf(data.getKey()));

                    musicArrayList.add(music);
                }

                adapter=new AllMediaAdapter(AllMediaProfilActivity.this,musicArrayList);
                recyclerView.setLayoutManager(new GridLayoutManager(AllMediaProfilActivity.this,3));
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
