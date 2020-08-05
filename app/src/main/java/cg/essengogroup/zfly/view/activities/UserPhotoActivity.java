package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import cg.essengogroup.zfly.R;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserPhotoActivity extends AppCompatActivity {

    private static final int CHOIX_IMAGE=101,STORAGE_PERMISSION_CODE = 123;
    private Uri uriProfileImage;

    private CircularImageView imageView;
    private CardView btnIgnore,btnContinuer;
    private ProgressBar progressBar;
    private Intent intent;
    private FloatingActionButton fab;
    private String nomValue,telValue,nomArtisteValue,genreArtistValue,paysValue,
            lienCouverture="https://firebasestorage.googleapis.com/v0/b/zfly2020-151d6.appspot.com/o/default%2Fdefault_img.png?alt=media&token=acb214d0-afcc-4afd-82ea-fea66baf789f",
            lienProfileImage="https://firebasestorage.googleapis.com/v0/b/zfly2020-151d6.appspot.com/o/default%2F264x264-000000-80-0-0.jpg?alt=media&token=feceb79d-6109-43b7-8275-c6ab0f2fc4a3";
    private boolean artisteValue;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;

    private ProgressDialog dialogLoading;

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser()==null){
            startActivity(new Intent(UserPhotoActivity.this,RegisterActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_photo);

        //Requesting storage permission
        requestStoragePermission();

        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        intent=getIntent();

        imageView=findViewById(R.id.imgUser);
        progressBar=findViewById(R.id.progress);
        btnContinuer=findViewById(R.id.btnContinue);
        btnIgnore=findViewById(R.id.btnIgnore);
        fab=findViewById(R.id.addImg);

        if (intent!=null){
            nomValue=intent.getStringExtra("nom");
            telValue=intent.getStringExtra("tel");
            paysValue=intent.getStringExtra("pays");
            nomArtisteValue=intent.getStringExtra("nomArtiste");
            artisteValue=intent.getBooleanExtra("artiste",false);
            if (artisteValue){
                genreArtistValue=intent.getStringExtra("genreArtiste");
            }
        }

        dialogLoading=new ProgressDialog(UserPhotoActivity.this);
        dialogLoading.setMessage("Chargement encours ...");

        fab.setOnClickListener(v->selectionnerImage());

        btnContinuer.setOnClickListener(v->{
            if (progressBar.getVisibility()==View.VISIBLE){
                Toast.makeText(this, "attendez jusqu'au télechargement de l'image", Toast.LENGTH_SHORT).show();
            }else {
                savegarderAllInformation();
            }
        });
        btnIgnore.setOnClickListener(v->setUserInformation());

        chargeDataIfExist();
    }

    private void chargeDataIfExist(){
        firebaseUser=mAuth.getCurrentUser();
        if (firebaseUser!=null){
            if (firebaseUser.getPhotoUrl()!=null){
                Picasso.get()
                        .load(firebaseUser.getPhotoUrl().toString())
                        .placeholder(R.drawable.imgdefault)
                        .error(R.drawable.imgdefault)
                        .into(imageView);
            }
        }
    }

    private void selectionnerImage(){
        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CHOIX_IMAGE);
    }

    private void savegarderAllInformation(){
        firebaseUser=mAuth.getCurrentUser();

        if (uriProfileImage !=null){
            if (firebaseUser !=null){
                dialogLoading.show();
                UserProfileChangeRequest profileChangeRequest=new UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(nomValue)
                        .setPhotoUri(uriProfileImage)
                        .build();

                firebaseUser.updateProfile(profileChangeRequest).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        setUserInformation();
                    }
                });
            }
        }else {
            Toast.makeText(this, "ajoutez une image ou Ignorez cette étape", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImageToFireBase(){

        mStorageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("profileImage")
                .child("profile"+System.currentTimeMillis()+".jpg");

        if (uriProfileImage !=null){
            progressBar.setVisibility(View.VISIBLE);
            //ce bout de code me permet de compresser la taille de l'image
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mStorageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get a URL to the uploaded content
                if (taskSnapshot!=null){

                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienProfileImage=uri.toString();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==CHOIX_IMAGE && resultCode == RESULT_OK && data != null ){
            uriProfileImage = data.getData();
            try {
                //getting bitmap object from uri
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriProfileImage);

                //displaying selected image to imageview
                imageView.setImageBitmap(bitmap);

                uploadImageToFireBase();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUserInformation(){

        Map<String, Object> user = new HashMap<>();
        user.put("user_id", firebaseUser.getUid());
        user.put("nom", nomValue);
        user.put("pseudo", nomArtisteValue);
        user.put("Apseudo", "@"+nomArtisteValue.toLowerCase());
        user.put("tel", telValue);
        user.put("pays", paysValue);
        user.put("ville", "inconnue");
        user.put("image", lienProfileImage);
        user.put("image_couverture", lienCouverture);
        user.put("isArtiste", artisteValue);
        user.put("isPlace", false);
        user.put("status", "deconnecte");
        user.put("token", "token");
        user.put("hasNewSMS", false);
        user.put("createAt", ServerValue.TIMESTAMP);
        user.put("createAt", ServerValue.TIMESTAMP);
        if (artisteValue){
            user.put("genreArtiste", genreArtistValue);
        }

        database.getReference()
                .child("users")
                .child(firebaseUser.getUid())
                .setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (dialogLoading.isShowing()){
                            dialogLoading.dismiss();
                        }
                        startActivity(new Intent(getApplicationContext(),AccueilActivity.class));
                        finish();
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
