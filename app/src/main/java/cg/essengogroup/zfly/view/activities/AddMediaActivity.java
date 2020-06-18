package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.zfly.R;

public class AddMediaActivity extends AppCompatActivity {
    private Button addSong;
    private TextInputEditText morceauName,artisteName;
    private CardView btnPublier;
    private ProgressBar progressBar;
    private ImageButton playPreview;

    private String lienChanson;

    private Uri uriChanson;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private StorageReference mStorageRef ;
    private DatabaseReference reference;

    private Intent intent;

    private static final int CHOIX_CHANSON=102,STORAGE_PERMISSION_CODE = 123;

    private MediaPlayer mPlayer;
    private boolean fabStateVolume = false;

    private ProgressDialog dialogLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_media);

        //Requesting storage permission
        requestStoragePermission();

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("users/"+firebaseUser.getUid());
        mStorageRef= FirebaseStorage.getInstance().getReference().child("music").child("son"+System.currentTimeMillis()+".mp3");

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addSong=findViewById(R.id.addSong);
        playPreview=findViewById(R.id.playPreview);

        morceauName=findViewById(R.id.music_name);
        artisteName=findViewById(R.id.music_artiste);

        btnPublier=findViewById(R.id.btnPublier);

        progressBar=findViewById(R.id.progress);


        dialogLoading=new ProgressDialog(AddMediaActivity.this);
        dialogLoading.setMessage("Chargement encours ...");

        btnPublier.setOnClickListener(v->uploadSongToFireBase());
        addSong.setOnClickListener(v->getMusic());
        playPreview.setOnClickListener(v->playPreview());

    }

    private void getMusic(){
        intent = new Intent();
        intent.setType("audio/mp3");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(
                intent,
                "Open Audio (mp3) file"),
                CHOIX_CHANSON);
    }

    private void playPreview(){
        if (uriChanson!=null){

            if (fabStateVolume) {
                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                playPreview.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                fabStateVolume = false;

            } else {
                mPlayer = MediaPlayer.create(AddMediaActivity.this, uriChanson);
                mPlayer.start();
                playPreview.setImageResource(R.drawable.ic_stop_black_24dp);
                fabStateVolume = true;
            }
        }else {
            Toast.makeText(this, "pensez à ajouter une chanson", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadSongToFireBase(){
        if (uriChanson !=null){
            dialogLoading.show();
            mStorageRef.putFile(uriChanson).addOnSuccessListener(taskSnapshot -> {
                // Get a URL to the uploaded content
                if (taskSnapshot!=null){
                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienChanson=uri.toString();
                            sendMusicToFirebaseDataBase();
                        }
                    });
                }
            }).addOnFailureListener(exception -> {
                progressBar.setVisibility(View.GONE);
                if (dialogLoading.isShowing()){
                    dialogLoading.dismiss();
                }
            });
        }else {
            Toast.makeText(this, "pensez à ajouter une chanson", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMusicToFirebaseDataBase(){
        String artisteNameValue=artisteName.getText().toString().trim();
        String morceauNameValue=morceauName.getText().toString().trim();

        if (TextUtils.isEmpty(artisteNameValue)){
            artisteNameValue="inconnu";
        }

        if (TextUtils.isEmpty(morceauNameValue)){
            morceauNameValue="inconnu";
        }

        Map<String, Object> post=new HashMap<>();
        post.put("user_id",firebaseUser.getUid());
        post.put("chanson",lienChanson);
        post.put("morceau",morceauNameValue);
        post.put("artiste",artisteNameValue);

        if (uriChanson!=null ){
           reference.child("chansons")
                    .child("son"+System.currentTimeMillis())
                    .setValue(post)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            /**
                             * si l'insertion du son est un succes
                             * on cree une table genre d'abord puis album et enfin artiste
                             */
                            if (dialogLoading.isShowing()){
                                dialogLoading.dismiss();
                            }
                            startActivity(new Intent(AddMediaActivity.this,ProfileActivity.class));
                            finish();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (dialogLoading.isShowing()){
                                dialogLoading.dismiss();
                            }
                        }
                    });

        }else {
            Toast.makeText(this, "importer une chanson", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOIX_CHANSON && resultCode == RESULT_OK && data != null ){
            uriChanson = data.getData();
        }

    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission accordée maintenant, vous pouvez lire le stockage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oups vous venez de refuser la permission", Toast.LENGTH_LONG).show();
            }
        }
    }


}
