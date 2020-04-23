package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.GallerieAdapter;
import cg.essengogroup.zfly.controller.adapter.MusicProfilAdapter;
import cg.essengogroup.zfly.model.Gallerie;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.model.User;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetailArtistActivity extends AppCompatActivity {
    private TextView txtPseudo,txtUsername,nbreAbonne,nbrePost;
    private CircularImageView userImage;
    private ImageView imageCouverture;
    private Button btnAbonnement;

    private ReadMoreTextView txtBio;

    private String userId;

    private Intent intent;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private DatabaseReference reference,referenceAbonnement;

    private ArrayList<Gallerie> gallerieArrayList;
    private ArrayList<Music> musicArrayList;

    private GallerieAdapter adapterG;
    private MusicProfilAdapter adapterM;

    private RecyclerView recycleGallerie,recycleMusic;

    private User userObject;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_artist);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }

        txtPseudo=findViewById(R.id.txtpseudo);
        txtUsername=findViewById(R.id.txtUsername);
        txtBio=findViewById(R.id.biographie);
        nbreAbonne=findViewById(R.id.nbreAbonne);
        nbrePost=findViewById(R.id.nbrePost);
        btnAbonnement=findViewById(R.id.abonner);

        imageCouverture=findViewById(R.id.imgCouverture);
        userImage=findViewById(R.id.imgUser);

        recycleGallerie=findViewById(R.id.recyclePhoto);
        recycleMusic=findViewById(R.id.recycleMusic);

        gallerieArrayList=new ArrayList<>();
        musicArrayList=new ArrayList<>();

        intent=getIntent();
        if (intent!=null){
            userId=intent.getStringExtra("user_id");
            userObject=intent.getParcelableExtra("user");
        }

        activity=DetailArtistActivity.this;

        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        user=mAuth.getCurrentUser();
        reference=database.getReference("users/"+userId);
        referenceAbonnement=database.getReference("users/"+user.getUid()).child("abonnements");

        getUserInfo();
        isAbonner();
        getNbreAbonne();
        getGallerieTopTen();
        getMusicPost();

        btnAbonnement.setOnClickListener(v->{
            if (btnAbonnement.getText().equals("Abonner")){
                setAbonnement();
            }else {
                desAbonnement();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.artiste_menu,menu);

        MenuItem menuImgOn = menu.findItem(R.id.chat);

        View viewImgOn= MenuItemCompat.getActionView(menuImgOn);

        ImageView imageSMS=viewImgOn.findViewById(R.id.imgSMS);

        imageSMS.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),MessageActivity.class).putExtra("user",userObject)));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.chat:
                startActivity(new Intent(DetailArtistActivity.this,MessageActivity.class).putExtra("user",userObject));
                return true;

            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getUserInfo(){
        Glide.with(DetailArtistActivity.this)
                .load(intent.getStringExtra("image"))
                .placeholder(R.drawable.default_img)
                .centerCrop()
                .into(userImage);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtPseudo.setText(String.valueOf(dataSnapshot.child("Apseudo").getValue()));
                txtUsername.setText(String.valueOf(dataSnapshot.child("pseudo").getValue()));
                nbrePost.setText(String.valueOf(dataSnapshot.child("posts").getChildrenCount()));
                if (!dataSnapshot.child("biographie").exists()){
                    txtBio.setText(String.valueOf(dataSnapshot.child("Apseudo").getValue()).toUpperCase()+" n'a pas encore ajouter sa biographie");
                }else {
                    txtBio.setText(String.valueOf(dataSnapshot.child("biographie").getValue()));
                }

                if (activity!=null && dataSnapshot.child("image_couverture").exists()){
                    Glide.with(activity)
                            .load(String.valueOf(dataSnapshot.child("image_couverture").getValue()))
                            .placeholder(R.drawable.default_img)
                            .centerCrop()
                            .into(imageCouverture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setAbonnement(){

        Map<String,Object> data=new HashMap<>();
        data.put("user_id",user.getUid());
        data.put("createAt", ServerValue.TIMESTAMP);

        reference.child("abonnes")
                .child(user.getUid())
                .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onSuccess(Void aVoid) {
                       setAbonner();
                    }
                });
    }

    /**
     *lorsque tu cliqu'un artiste s'abonne a un autre artiste
     * cette methode met un compteur sur les nombres de personne qu'il s'est abonner
     */
    private void setAbonner(){

        Map<String,Object> data=new HashMap<>();
        data.put("user_id",userId);
        data.put("createAt", ServerValue.TIMESTAMP);

        referenceAbonnement.child(userId)
                .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(Void aVoid) {
                btnAbonnement.setText("Désabonner");
                btnAbonnement.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btnAbonnement.setTextColor(getResources().getColor(R.color.colorBlanc));
            }
        });
    }


    /**
     * cette methode desabonne l'artiste qui s'est deja abonner
     */
    private void desAbonnement(){
        reference.child("abonnes")
                .child(user.getUid())
                .removeValue(new DatabaseReference.CompletionListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        desAbonner();
                    }
                });
    }

    private void desAbonner(){
        referenceAbonnement.child(userId)
                .removeValue(new DatabaseReference.CompletionListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        btnAbonnement.setText("S'abonner");
                        btnAbonnement.setBackgroundColor(getResources().getColor(R.color.c5));
                        btnAbonnement.setTextColor(getResources().getColor(R.color.colorBlanc));
                    }
                });
    }

    /**
     * cette methode verifie si l'artiste c'est deja abonner ou pas
     */

    private void isAbonner(){
        reference.child("abonnes")
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            btnAbonnement.setText("Désabonner");
                            btnAbonnement.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            btnAbonnement.setTextColor(getResources().getColor(R.color.colorBlanc));
                        }else {
                            btnAbonnement.setText("Abonner");
                            btnAbonnement.setBackgroundColor(getResources().getColor(R.color.c5));
                            btnAbonnement.setTextColor(getResources().getColor(R.color.colorBlanc));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

            }
                });
    }

    private void getNbreAbonne(){
        reference.child("abonnes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        nbreAbonne.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getGallerieTopTen(){
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

                adapterG=new GallerieAdapter(DetailArtistActivity.this,gallerieArrayList);
                recycleGallerie.setLayoutManager(new LinearLayoutManager(DetailArtistActivity.this,LinearLayoutManager.HORIZONTAL,false));
                recycleGallerie.setAdapter(adapterG);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMusicPost(){
        reference.child("chansons").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                musicArrayList.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    Music music=new Music();
                    music.setArtiste(String.valueOf(data.child("artiste").getValue()));
                    music.setMorceau(String.valueOf(data.child("morceau").getValue()));
                    music.setChanson(String.valueOf(data.child("chanson").getValue()));

                    musicArrayList.add(music);
                }

                adapterM=new MusicProfilAdapter(DetailArtistActivity.this,musicArrayList);
                recycleMusic.setLayoutManager(new GridLayoutManager(DetailArtistActivity.this,3));
                recycleMusic.setAdapter(adapterM);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
