package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.GallerieAdapter;
import cg.essengogroup.zfly.controller.adapter.MultiViewTypeAdapter;
import cg.essengogroup.zfly.controller.adapter.MusicProfilAdapter;
import cg.essengogroup.zfly.model.Gallerie;
import cg.essengogroup.zfly.model.Model;
import cg.essengogroup.zfly.model.Music;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.IOException;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private TextView txtPseudo,txtUsername,nbreAbonne,nbrePost,txtBio,nbreAbonnement;
    private CircularImageView userImage;
    private ProgressBar progressBar,progressBarProfil;
    private ImageView imageCouverture,menuSon,menuPhoto,btnAddImgCouverture;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private DatabaseReference reference,referenceModel;
    private FirebaseStorage storage;
    private StorageReference storageReference,storageReferenceProfile;


    private LinearLayoutManager manager;

    private ArrayList<Gallerie> gallerieArrayList;
    private ArrayList<Music> musicArrayList;

    private GallerieAdapter adapterG;
    private MusicProfilAdapter adapterM;

    private RecyclerView recycleGallerie,recycleMusic,recyclerView;

    private MultiViewTypeAdapter adapter;
    private ArrayList<Model> modelArrayList;

    private static final int CHOIX_IMAGE=101,CHOIX_IMAGE_P=102;
    private Uri uriPreviewImage,uriImageP;

    private Intent intent;

    private String lienImage,lienImageP,pseudoValue;

    private FloatingActionButton fab;

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        user=mAuth.getCurrentUser();
        reference=database.getReference().child("users/"+String.valueOf(user.getUid()));
        storageReference=storage.getReference().child("image_couverture").child("post"+System.currentTimeMillis()+".jpg");;
        storageReferenceProfile=storage.getReference().child("profileImage").child("profile"+System.currentTimeMillis()+".jpg");
        referenceModel=database.getReference("post");

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
        nbreAbonnement=findViewById(R.id.nbreAbonnement);
        nbrePost=findViewById(R.id.nbrePost);
        btnAddImgCouverture=findViewById(R.id.addImgCouverture);
        progressBar=findViewById(R.id.progress);
        progressBarProfil=findViewById(R.id.progressP);
        fab=findViewById(R.id.addImg);
        linearLayout=findViewById(R.id.lineTop);

        menuPhoto=findViewById(R.id.menuPhoto);
        menuSon=findViewById(R.id.menuSons);

        imageCouverture=findViewById(R.id.imguser);
        userImage=findViewById(R.id.imgUser);

        recycleGallerie=findViewById(R.id.recyclePhoto);
        recycleMusic=findViewById(R.id.recycleMusic);

        gallerieArrayList=new ArrayList<>();
        musicArrayList=new ArrayList<>();

        menuSon.setOnClickListener(v->actionMenuSon());
        menuPhoto.setOnClickListener(v->actionMenuPhoto());
        btnAddImgCouverture.setOnClickListener(v->selectionnerImage());
        findViewById(R.id.lineAbonnes).setOnClickListener(v->startActivity(new Intent(ProfileActivity.this,AllAbonnerActivity.class).putExtra("key","abonnes")));
        findViewById(R.id.lineAbonnement).setOnClickListener(v->startActivity(new Intent(ProfileActivity.this,AllAbonnerActivity.class).putExtra("key","abonnements")));
        findViewById(R.id.linePost).setOnClickListener(v->startActivity(new Intent(ProfileActivity.this,AllPostActivity.class)));

        fab.setOnClickListener(v->selectionnerImageP());

        getUserInfo();
        getGallerieTopTen();
        getNbreAbonne();
        getMusicPost();

        recyclerView=findViewById(R.id.recycleAllPost);

        modelArrayList=new ArrayList<>();
        manager=new LinearLayoutManager(ProfileActivity.this);

        getDataActualite();

        DatabaseReference isArtisteRef = database.getReference("users/"+user.getUid());
        isArtisteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((Boolean) dataSnapshot.child("isArtiste").getValue()){
                    recyclerView.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                }else {
                    recyclerView.setVisibility(View.VISIBLE);
                    linearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getUserInfo(){
        if (user!=null){
            Glide.with(ProfileActivity.this)
                    .load(user.getPhotoUrl().toString())
                    .placeholder(R.drawable.default_img)
                    .centerCrop()
                    .into(userImage);
        }

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                txtPseudo.setText(String.valueOf(dataSnapshot.child("Apseudo").getValue()));
                txtUsername.setText(String.valueOf(dataSnapshot.child("pseudo").getValue()));

                pseudoValue=String.valueOf(dataSnapshot.child("pseudo").getValue());

                nbrePost.setText(String.valueOf(dataSnapshot.child("posts").getChildrenCount()));
                nbreAbonnement.setText(String.valueOf(dataSnapshot.child("abonnements").getChildrenCount()));

                if (dataSnapshot.child("biographie").exists()){
                    txtBio.setText(String.valueOf(dataSnapshot.child("biographie").getValue()));
                }else {
                    txtBio.setText(String.valueOf(dataSnapshot.child("Apseudo").getValue()).toUpperCase()+" n'a pas encore ajouter sa biographie");
                }
                if (dataSnapshot.child("image_couverture").exists()){
                    Glide.with(ProfileActivity.this)
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

    private void actionMenuSon(){

        PopupMenu popup = new PopupMenu(this, menuSon);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.one:
                       startActivity(new Intent(ProfileActivity.this,AllMediaProfilActivity.class));
                        return true;
                    case R.id.two:
                        startActivity(new Intent(ProfileActivity.this,AddMediaActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
    }

    private void actionMenuPhoto(){

        PopupMenu popup = new PopupMenu(this, menuPhoto);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.one:
                        startActivity(new Intent(ProfileActivity.this,AllPhotoProfilActivity.class)
                                .putExtra("key",0)
                                .putExtra("titre",pseudoValue)
                        );
                        return true;
                    case R.id.two:
                        startActivity(new Intent(ProfileActivity.this,AddPhotoActivity.class).putExtra("key","profile"));
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
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

                adapterG=new GallerieAdapter(ProfileActivity.this,gallerieArrayList);
                recycleGallerie.setLayoutManager(new LinearLayoutManager(ProfileActivity.this,LinearLayoutManager.HORIZONTAL,false));
                recycleGallerie.setAdapter(adapterG);
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

                adapterM=new MusicProfilAdapter(ProfileActivity.this,musicArrayList);
                recycleMusic.setLayoutManager(new GridLayoutManager(ProfileActivity.this,3));
                recycleMusic.setAdapter(adapterM);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void selectionnerImage(){
        intent=new Intent();
        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CHOIX_IMAGE);
    }

    private void selectionnerImageP(){
        intent=new Intent();
        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CHOIX_IMAGE_P);
    }

    private void uploadImgCouverture(){

        if (uriPreviewImage !=null){

            progressBar.setVisibility(View.VISIBLE);

            storageReference.putFile(uriPreviewImage).addOnSuccessListener(taskSnapshot -> {
                // Get a URL to the uploaded content
                if (taskSnapshot!=null){

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienImage=uri.toString();
                            sendPostToFireBase();
                        }
                    });
                }
                progressBar.setVisibility(View.GONE);

            }).addOnFailureListener(exception -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void sendPostToFireBase(){
        reference.child("image_couverture")
                .setValue(lienImage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "l'image de couverture a bien été mise à jour", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        // ...
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.modifier:
                startActivity(new Intent(ProfileActivity.this,ModifierProfilActivity.class));
                return true;
            case R.id.partager:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==CHOIX_IMAGE && resultCode == RESULT_OK && data != null ){
            uriPreviewImage = data.getData();
            try {
                //getting bitmap object from uri
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriPreviewImage);

                //displaying selected image to imageview
                imageCouverture.setImageBitmap(bitmap);
                uploadImgCouverture();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else  if (requestCode ==CHOIX_IMAGE_P && resultCode == RESULT_OK && data != null ){
            uriImageP = data.getData();
            try {
                //getting bitmap object from uri
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriImageP);

                //displaying selected image to imageview
                userImage.setImageBitmap(bitmap);
                uploadImageProfil();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageProfil(){
        if (uriImageP!=null){
            progressBarProfil.setVisibility(View.VISIBLE);
            storageReferenceProfile.putFile(uriImageP).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot!=null){
                        storageReferenceProfile.getDownloadUrl().addOnSuccessListener(uri -> {
                            lienImageP=uri.toString();

                            UserProfileChangeRequest profileChangeRequest=new UserProfileChangeRequest
                                    .Builder()
                                    .setPhotoUri(uri)
                                    .build();

                            user.updateProfile(profileChangeRequest).addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    validateSendDataToFirebase();
                                }
                            });
                        });
                    }
                }
            });
        }else {
            lienImageP=user.getPhotoUrl().toString();
        }
    }

    private void validateSendDataToFirebase(){
        reference.child("image").setValue(lienImageP).addOnSuccessListener(aVoidI -> {
            reference.child("updateAt").setValue(ServerValue.TIMESTAMP).addOnSuccessListener(aVoid -> {
                progressBarProfil.setVisibility(View.GONE);
            });
        });
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

                    if (model.getUser_id().equalsIgnoreCase(user.getUid())){
                        modelArrayList.add(model);
                    }
                }

                adapter=new MultiViewTypeAdapter(modelArrayList,ProfileActivity.this);
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

}
