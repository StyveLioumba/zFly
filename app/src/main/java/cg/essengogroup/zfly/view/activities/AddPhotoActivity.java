package cg.essengogroup.zfly.view.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.view.dialogs.Dialog_loading;

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
    private static final int CHOIX_IMAGE=101;

    private String lienImage,activityValue,place_idValue;

    private Dialog_loading dialogLoading;

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


        dialogLoading=new Dialog_loading(AddPhotoActivity.this);
        dialogLoading.setCancelable(false);

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
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);
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
        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CHOIX_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CHOIX_IMAGE && resultCode==RESULT_OK && data!=null){
            imageUri=data.getData();
            try {
                //getting bitmap object from uri
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                //displaying selected image to imageview
                imageView.setImageBitmap(bitmap);

                fab.setVisibility(View.GONE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
