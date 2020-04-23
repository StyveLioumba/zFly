package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
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
import cg.essengogroup.zfly.controller.adapter.AllPhotoAdapter;
import cg.essengogroup.zfly.model.Gallerie;

public class AllPhotoProfilActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private RecyclerView recyclerView;
    private ArrayList<Gallerie> galleries;
    private AllPhotoAdapter adapter;

    private Intent intent;
    private int valueIntent=0;
    private String titleValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_photo_profil);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intent=getIntent();

        if (intent!=null){
            valueIntent=intent.getIntExtra("key",0);
            titleValue=intent.getStringExtra("titre");
        }

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            actionBar.setTitle(titleValue);
        }

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();

        if (valueIntent==0){//si valueIntent=0 c'est que c'est toute les photos l'utilisateur
            reference=database.getReference().child("users/"+String.valueOf(user.getUid()));
        }else {//si valueIntent=0 c'est que c'est toute les photos l'utilisateur
            reference=database.getReference().child("place/"+intent.getStringExtra("place_id"));
        }

        recyclerView=findViewById(R.id.recycleAllPhoto);
        galleries=new ArrayList<>();

        getData();

    }

    private void getData(){
        reference.child("galerie").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                galleries.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    Gallerie gallerie=new Gallerie();
                    gallerie.setImage(String.valueOf(data.child("image").getValue()));
                    gallerie.setRacine(String.valueOf(data.getKey()));
                    galleries.add(gallerie);
                }
                if (valueIntent==0){
                    adapter=new AllPhotoAdapter(AllPhotoProfilActivity.this,galleries,valueIntent,null);
                }else {
                    adapter=new AllPhotoAdapter(AllPhotoProfilActivity.this,galleries,valueIntent,intent.getStringExtra("place_id"));
                }
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
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
