package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.utils.ScrollHandler;
import cg.essengogroup.zfly.view.fragments.AccueilFragment;
import cg.essengogroup.zfly.view.fragments.MusicFragment;
import cg.essengogroup.zfly.view.fragments.PlaceFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static cg.essengogroup.zfly.controller.utils.Methodes.shareApp;

public class AccueilActivity extends AppCompatActivity {

    private long backPressedTime;
    private Toast backToast;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onStart() {
        super.onStart();
        if (user==null){
            startActivity(new Intent(AccueilActivity.this,LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("users").child(user.getUid());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);

        appelleNavigation(bottomNavigationView);

        replaceFragment(new AccueilFragment());

        //Hide the Bottom Navigation when the page is scrolled
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new ScrollHandler());

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();

    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment);
        fragmentTransaction.commit();
    }

    public void appelleNavigation(BottomNavigationView bottomNavigationView){

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.homeFrame:
                    replaceFragment(new AccueilFragment());
                    return true;
                case R.id.musicFrame:
                    replaceFragment(new MusicFragment());
                    return true;
                case R.id.placeFrame:
                    replaceFragment(new PlaceFragment());
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            status("deconnecte");
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Cliquez encore pour quitter", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.profile);
        MenuItem menuImgOn = menu.findItem(R.id.chat);

        View view = MenuItemCompat.getActionView(menuItem);

        View viewImgOn= MenuItemCompat.getActionView(menuImgOn);

        ImageView imageView=view.findViewById(R.id.imgUser);
        ImageView imageOn=viewImgOn.findViewById(R.id.imgOn);
        ImageView imageSMS=viewImgOn.findViewById(R.id.imgSMS);

        if (user!=null){
            Glide.with(this)
                    .load(user.getPhotoUrl().toString())
                    .placeholder( R.drawable.imgdefault)
                    .circleCrop()
                    .into(imageView);
        }

        imageView.setOnClickListener(v ->startActivity(new Intent(getApplicationContext(),ProfileActivity.class)));
        imageSMS.setOnClickListener(v -> startActivity(new Intent(AccueilActivity.this,ChatActivity.class)));

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if ((Boolean) dataSnapshot.child("hasNewSMS").getValue()){
                   imageOn.setVisibility(View.VISIBLE);
               }else {
                   imageOn.setVisibility(View.GONE);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                startActivity(new Intent(AccueilActivity.this,AboutActivity.class));
                return true;

            case R.id.deconnexion:
                mAuth.signOut();
                startActivity(new Intent(AccueilActivity.this,LoginActivity.class));
                finish();
                return true;

            case R.id.inviter:
                shareApp(AccueilActivity.this);
                return true;

            case R.id.chat:
                startActivity(new Intent(AccueilActivity.this,ChatActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void status(String status){
        reference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("enligne");
    }

    @Override
    protected void onStop() {
        super.onStop();
        status("deconnecte");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status("deconnecte");
    }
}
