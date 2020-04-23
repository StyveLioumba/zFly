package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.AllUserAdapter;
import cg.essengogroup.zfly.model.User;

public class AllAbonnerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<User> userArrayList;
    private GridLayoutManager manager;
    private AllUserAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase database;
    private Query query;
    private DatabaseReference reference;

    private Intent intent;
    private String keyValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_abonner);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();


        intent=getIntent();
        if (intent!=null){
            keyValue=intent.getStringExtra("key");
        }

        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

            if (keyValue.equalsIgnoreCase("abonnes")){
                actionBar.setTitle("abonnés");
            }else {
                actionBar.setTitle("abonnements");
            }
        }

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        if (keyValue.equalsIgnoreCase("abonnes")){
            reference=database.getReference().child("users/"+mUser.getUid()+"/abonnes");
        }else {
            reference=database.getReference().child("users/"+mUser.getUid()+"/abonnements");
        }

        recyclerView=findViewById(R.id.recycleAllAbonner);
        userArrayList=new ArrayList<>();
        manager=new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(manager);

        getDataUser();
    }
    private void getDataUser(){

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Log.e("TAG", "onDataChange: "+data.getKey());
                    getProfilAbonne(data.getKey());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getProfilAbonne(String user_id){
        query=database.getReference().child("users").child(user_id);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user=dataSnapshot.getValue(User.class);

                if (!user.getUser_id().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    userArrayList.add(user);
                }

                adapter=new AllUserAdapter(AllAbonnerActivity.this,userArrayList);
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
