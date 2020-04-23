package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.AllUserAdapter;
import cg.essengogroup.zfly.controller.adapter.UserAdapter;
import cg.essengogroup.zfly.model.User;

public class AllUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<User> userArrayList;
    private GridLayoutManager manager;
    private AllUserAdapter adapter;

    private FirebaseDatabase database;
    private Query reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            actionBar.setTitle("Nos artistes");
        }

        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("users").orderByChild("isArtiste").equalTo(true);

        recyclerView=findViewById(R.id.recycleAllUser);
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
                    User user=data.getValue(User.class);
                    if (!user.getUser_id().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        userArrayList.add(user);
                    }
                }

                adapter=new AllUserAdapter(AllUserActivity.this,userArrayList);
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
