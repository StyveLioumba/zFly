package cg.essengogroup.zfly.view.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cg.essengogroup.zfly.R;

public class AddPhotoActivity extends AppCompatActivity {
    private CardView btnPublier;
    private ImageView imageView;
    private ProgressBar progressBar;
    private FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Uri imageUri=null;
    private Intent intent;

    private String lienImage,activityValue,place_idValue;

    private ProgressDialog dialogLoading;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }

        intent=getIntent();

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        if (intent!=null){
            activityValue=intent.getStringExtra("key");
            place_idValue=intent.getStringExtra("place_id");
        }

        if (activityValue.equalsIgnoreCase("profile")){
            databaseReference=database.getReference().child("users/"+String.valueOf(user.getUid()));
            storageReference=storage.getReference().child("gallerie").child("img"+System.currentTimeMillis()+".jpg");
        }else {
            databaseReference=database.getReference().child("place/"+place_idValue);
            storageReference=storage.getReference().child("gallerie").child("img"+System.currentTimeMillis()+".jpg");
        }

        btnPublier=findViewById(R.id.btnPublier);
        imageView=findViewById(R.id.imgPreview);
        progressBar=findViewById(R.id.progress);

        fab=findViewById(R.id.addImg);


        dialogLoading=new ProgressDialog(AddPhotoActivity.this);
        dialogLoading.setMessage("Chargement encours ...");

        fab.setOnClickListener(v->selectImageFromGallery());
        imageView.setOnClickListener(v->selectImageFromGallery());

        btnPublier.setOnClickListener(v->{
            uploadImageToStorage();
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void uploadImageToStorage(){
        if (imageUri!=null){
            dialogLoading.show();
            //ce bout de code me permet de compresser la taille de l'image

            UploadTask uploadTask = storageReference.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot!=null){
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            lienImage=uri.toString();
                            sendDataToDataBase();
                        });
                    }
                }
            });
        }
    }

    private void sendDataToDataBase(){
        Map<String,Object> data=new HashMap<>();
        data.put("image",lienImage);
        data.put("createAt", ServerValue.TIMESTAMP);

        databaseReference.child("galerie")
                .child("img"+System.currentTimeMillis())
                .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                if (activityValue.equalsIgnoreCase("profile")){
                    if (dialogLoading.isShowing()){
                        dialogLoading.dismiss();
                    }
                    startActivity(new Intent(AddPhotoActivity.this,ProfileActivity.class));
                    finish();
                }else {
                    finish();
                }
            }
        });
    }

    private void selectImageFromGallery(){
        ImagePicker.create(this) // Activity or Fragment
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            try {
                // Get a list of picked images
                List<Image> images = ImagePicker.getImages(data);
                Image image=null;
                if (images.isEmpty()){
                    image = ImagePicker.getFirstImageOrNull(data);
                }else {
                    image=images.get(0);
                }

                if (image!=null){
                    imageBitmap= SiliCompressor.with(this).getCompressBitmap(image.getPath());
                    String filePath = SiliCompressor.with(this).compress(image.getPath(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                    imageUri= Uri.fromFile(new File(filePath));
                    imageView.setImageBitmap(imageBitmap);

                    fab.setVisibility(View.GONE);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
