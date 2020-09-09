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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
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
import com.iceteck.silicompressorr.SiliCompressor;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
            lienProfileImage;
    private boolean artisteValue;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;

    private ProgressDialog dialogLoading;
    private Bitmap imageBitmap;

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
        lienProfileImage="https://ui-avatars.com/api/?background=470000&color=fff&size=200&name="+nomValue+"&font-size=0.50";

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
        ImagePicker.create(this) // Activity or Fragment
                .start();
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

            UploadTask uploadTask = mStorageRef.putFile(uriProfileImage);
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
                    uriProfileImage= Uri.fromFile(new File(filePath));
                    imageView.setImageBitmap(imageBitmap);
                    uploadImageToFireBase();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUserInformation(){

        Map<String, Object> user = new HashMap<>();
        user.put("user_id", firebaseUser.getUid());
        user.put("nom", nomValue);
        user.put("pseudo", nomArtisteValue);
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

        if (artisteValue){
            user.put("genreArtiste", genreArtistValue);
            user.put("Apseudo", "@"+nomArtisteValue.toLowerCase());
        }else{
            user.put("Apseudo", "@"+nomValue.toLowerCase());
        }
        Log.e("TAG", "setUserInformation: "+user.toString() );
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setUserInformation();
    }
}
