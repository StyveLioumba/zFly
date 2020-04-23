package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.SectionPagerAdapter;
import cg.essengogroup.zfly.view.fragments.chat_fragments.ChatFragment;
import cg.essengogroup.zfly.view.fragments.chat_fragments.UserFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.AlbumFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.ArtistesFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.ChansonsFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.GenreFragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            actionBar.setTitle("Zfy chat");
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getUid());

        reference.child("hasNewSMS").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

        setupViewPager();
    }
    private void setupViewPager(){
        SectionPagerAdapter adapter=new SectionPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new ChatFragment());
        adapter.addFragment(new UserFragment());

        ViewPager viewPager=findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout=findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorToolbar));
        tabLayout.setTabTextColors(getResources().getColor(android.R.color.black),getResources().getColor(R.color.colorBlanc));

        tabLayout.getTabAt(0).setText("Discussions");
        tabLayout.getTabAt(1).setText("Utilisateurs");

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
