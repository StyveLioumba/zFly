package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import cg.essengogroup.zfly.controller.adapter.MultiViewTypeAdapter;
import cg.essengogroup.zfly.model.Model;

public class AllPostActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private MultiViewTypeAdapter adapter;
    private ArrayList<Model> modelArrayList;


    private FirebaseDatabase database;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private DatabaseReference referenceModel;
    private LinearLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_post);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            actionBar.setTitle("Publications");
        }

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        referenceModel=database.getReference("post");

        referenceModel.keepSynced(true);

        recyclerView=findViewById(R.id.recycleAllPost);

        modelArrayList=new ArrayList<>();
        manager=new LinearLayoutManager(AllPostActivity.this);

        getDataActualite();
    }


    private void getDataActualite(){

        referenceModel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                modelArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Model model=new Model() ;
                    model.setUser_id(""+data.child("user_id").getValue());
                    model.setImage(""+data.child("imagePost").getValue());
                    model.setAudio(""+data.child("sonPost").getValue());
                    model.setDescription(""+data.child("description").getValue());
                    model.setType(Integer.parseInt(""+data.child("type").getValue()));
                    model.setCreateAt(String.valueOf(data.child("createAt").getValue()));
                    model.setPost_id(data.getKey());

                    if (model.getUser_id().equalsIgnoreCase(mUser.getUid())){
                        modelArrayList.add(model);
                    }
                }

                adapter=new MultiViewTypeAdapter(modelArrayList,AllPostActivity.this);
                recyclerView.setLayoutManager(manager);
                manager.setReverseLayout(true);
                manager.setStackFromEnd(true);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
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
