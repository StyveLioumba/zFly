package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.GallerieAdapter;
import cg.essengogroup.zfly.controller.adapter.SliderPagerPlaceAdapter;
import cg.essengogroup.zfly.model.Gallerie;
import cg.essengogroup.zfly.model.User;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DetailPlaceActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private SliderPagerPlaceAdapter slideAdapter;

    private ImageView menuPhoto;
    private RecyclerView recycleGallerie;
    private ArrayList<Gallerie> gallerieArrayList;
    private GallerieAdapter adapter;

    private Intent intent;
    private String place_idValue,nomValue,descriptionValue,adresseValue,numValue,typeValue,image_couverture,emailValue,user_id,createAt;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private DatabaseReference reference;

    private User newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_place);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();

        intent=getIntent();

        if (intent!=null){
            user_id=intent.getStringExtra("user_id");
            nomValue=intent.getStringExtra("nom");
            descriptionValue=intent.getStringExtra("description");
            typeValue=intent.getStringExtra("type");
            emailValue=intent.getStringExtra("email");
            adresseValue=intent.getStringExtra("adresse");
            numValue=intent.getStringExtra("numero");
            image_couverture=intent.getStringExtra("imageCouverture");
            createAt=intent.getStringExtra("createAt");
            place_idValue=intent.getStringExtra("place_id");
        }

        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        user=mAuth.getCurrentUser();
        reference=database.getReference().child("place/"+place_idValue);

        TextView txtTitre=findViewById(R.id.txtTitre);
        TextView txtDescription=findViewById(R.id.txtDescription);
        TextView txtAdresse=findViewById(R.id.txtAdresse);
        TextView txtNum=findViewById(R.id.txtNum);
        TextView txtType=findViewById(R.id.txtType);
        TextView txtEmail=findViewById(R.id.txtEmail);
        viewPager=findViewById(R.id.viewpager);

        menuPhoto=findViewById(R.id.menuPhoto);

        recycleGallerie=findViewById(R.id.recyclePhoto);
        gallerieArrayList=new ArrayList<>();


        txtTitre.setText(nomValue);
        toolbar.setTitle(typeValue+" "+nomValue);

        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            actionBar.setTitle(nomValue);
        }

        if (TextUtils.isEmpty(descriptionValue) || descriptionValue.equalsIgnoreCase("null")){
            txtDescription.setText("Aucune description");
        }else {
            txtDescription.setText(descriptionValue);
        }
        txtAdresse.setText(adresseValue);
        txtNum.setText(numValue);
        txtType.setText(typeValue);

        if (TextUtils.isEmpty(emailValue) || emailValue.equalsIgnoreCase("null")){
            txtEmail.setText("indisponible");
        }else {
            txtEmail.setText(emailValue);
        }


        menuPhoto.setOnClickListener(v->actionMenuPhoto());

        newUser=new User();

        DatabaseReference referenceUser = FirebaseDatabase.getInstance().getReference("users/"+user_id);
        referenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newUser=dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getGallerie();
        getDataSlider();
        Timer timer= new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);

    }

    private void actionMenuPhoto(){

        PopupMenu popup = new PopupMenu(this, menuPhoto);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.one:startActivity(new Intent(DetailPlaceActivity.this,AllPhotoProfilActivity.class)
                            .putExtra("key",1)
                            .putExtra("place_id",place_idValue)
                            .putExtra("titre",nomValue)
                    );
                        return true;
                    case R.id.two:
                        startActivity(new Intent(DetailPlaceActivity.this,AddPhotoActivity.class)
                                .putExtra("key","place")
                                .putExtra("place_id",place_idValue)
                        );
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
    }

    private void getGallerie(){
        reference.child("galerie").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gallerieArrayList.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    Gallerie gallerie=new Gallerie();
                    gallerie.setImage(String.valueOf(data.child("image").getValue()));
                    gallerie.setRacine(String.valueOf(data.getKey()));
                    gallerieArrayList.add(gallerie);
                }

                adapter=new GallerieAdapter(DetailPlaceActivity.this,gallerieArrayList);
                recycleGallerie.setLayoutManager(new LinearLayoutManager(DetailPlaceActivity.this,LinearLayoutManager.HORIZONTAL,false));
                recycleGallerie.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataSlider(){

        reference.child("galerie").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                gallerieArrayList.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    Gallerie gallerie=new Gallerie();
                    gallerie.setImage(String.valueOf(data.child("image").getValue()));
                    gallerie.setRacine(String.valueOf(data.getKey()));
                    gallerieArrayList.add(gallerie);
                }

                slideAdapter=new SliderPagerPlaceAdapter(DetailPlaceActivity.this,gallerieArrayList);
                viewPager.setAdapter(slideAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.artiste_menu,menu);

        MenuItem menuImgOn = menu.findItem(R.id.chat);

        View viewImgOn= MenuItemCompat.getActionView(menuImgOn);

        ImageView imageSMS=viewImgOn.findViewById(R.id.imgSMS);

        imageSMS.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),MessageActivity.class).putExtra("user",newUser)));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.chat:
                startActivity(new Intent(getApplicationContext(),MessageActivity.class).putExtra("user",newUser));
                return true;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    class SliderTimer extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(() -> {
                if(viewPager.getCurrentItem()< gallerieArrayList.size()-1){
                    viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
                }else {
                    viewPager.setCurrentItem(0);
                }
            });
        }
    }
}
