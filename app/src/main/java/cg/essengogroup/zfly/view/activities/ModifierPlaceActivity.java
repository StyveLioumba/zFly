package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Place;
import cg.essengogroup.zfly.view.dialogs.Dialog_loading;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModifierPlaceActivity extends AppCompatActivity {

    private Place place;
    private Intent intent;

    private CardView btnPublier,btnSupprimer;
    private ImageView imageCouverture;
    private TextInputEditText placeName,placeDescription,placeNum,placeAdresse;
    private Spinner placeType;


    private ProgressBar progressBar,progressBarTop;

    private String lienImage;

    private Uri uriPreviewImage;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private StorageReference mStorageRef;
    private DatabaseReference reference;

    private static final int CHOIX_IMAGE=101, STORAGE_PERMISSION_CODE = 123;

    private String[] listeType={"hotel","vip","restaurant"};

    private Dialog_loading dialogLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_place);
        //Requesting storage permission
        requestStoragePermission();
        intent=getIntent();
        if (intent!=null){
            place=intent.getParcelableExtra("place");
        }

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("place/"+place.getPlace_id());

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(place.getNom());
        }


        btnPublier=findViewById(R.id.btnPublier);
        btnSupprimer=findViewById(R.id.btnSupp);

        imageCouverture=findViewById(R.id.imgPreview);

        placeName=findViewById(R.id.placeName);
        placeDescription=findViewById(R.id.placeDescription);
        placeNum=findViewById(R.id.placeNum);
        placeAdresse=findViewById(R.id.placeAdresse);

        placeType=findViewById(R.id.spinnerType);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1,listeType);
        placeType.setAdapter(adapter);

        progressBar=findViewById(R.id.progress);
        progressBarTop=findViewById(R.id.progressTop);

        dialogLoading=new Dialog_loading(ModifierPlaceActivity.this);
        dialogLoading.setCancelable(false);

        imageCouverture.setOnClickListener(v->selectionnerImage());
        btnPublier.setOnClickListener(v->uploadImageToFireBase());
        btnSupprimer.setOnClickListener(v->deletePlace());

        getPlaceData();
    }

    private void getPlaceData(){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Picasso.get()
                        .load(String.valueOf(dataSnapshot.child("image_couverture").getValue()))
                        .placeholder(R.drawable.default_img)
                        .error(R.drawable.default_img)
                        .into(imageCouverture);

                placeName.setText(String.valueOf(dataSnapshot.child("nom").getValue()));
                placeNum.setText(String.valueOf(dataSnapshot.child("numero").getValue()));
                placeDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                placeAdresse.setText(String.valueOf(dataSnapshot.child("adresse").getValue()));

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

    private void uploadImageToFireBase(){

        mStorageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("image_place")
                .child("place"+System.currentTimeMillis()+".jpg");

        if (uriPreviewImage !=null){
            dialogLoading.show();
            mStorageRef.putFile(uriPreviewImage).addOnSuccessListener(taskSnapshot -> {
                // Get a URL to the uploaded content

                if (taskSnapshot!=null){

                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienImage=uri.toString();

                            sendPlaceToFirebaseDataBase();
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
            lienImage=place.getImage_couverture();
            sendPlaceToFirebaseDataBase();
        }
    }

    private void sendPlaceToFirebaseDataBase(){

        String nameValue=placeName.getText().toString().trim();
        String descriptionValue=placeDescription.getText().toString().trim();
        String typeValue=placeType.getSelectedItem().toString().trim();
        String adresseValue=placeAdresse.getText().toString().trim();
        String numValue=placeNum.getText().toString().trim();

        if (TextUtils.isEmpty(nameValue)){
            placeName.setError("veuillez entrer le nom de la structure");
            placeName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(descriptionValue)){
            placeDescription.setError("veuillez entrer une petite description");
            placeDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(adresseValue)){
            placeAdresse.setError("veuillez preciser la position de votre structure");
            placeAdresse.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(numValue)){
            placeNum.setError("veuillez entré vos coordonnées téléphonique");
            placeNum.requestFocus();
            return;
        }

        Map<String, Object> post=new HashMap<>();
        post.put("user_id",firebaseUser.getUid());
        post.put("image_couverture",lienImage);
        post.put("nom",nameValue);
        post.put("description",descriptionValue);
        post.put("type",typeValue);
        post.put("adresse",adresseValue);
        post.put("numero",numValue);
        post.put("createAt", ServerValue.TIMESTAMP);

        progressBarTop.setVisibility(View.VISIBLE);

        reference.setValue(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBarTop.setVisibility(View.GONE);
                        if (dialogLoading.isShowing()){
                            dialogLoading.dismiss();
                        }
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

    private void deletePlace(){
        AlertDialog.Builder builder=new AlertDialog.Builder(ModifierPlaceActivity.this);
        builder.setTitle("supprimer");
        builder.setMessage("Voulez vous vraiment supprimer cet article ?");
        builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reference.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Toast.makeText(ModifierPlaceActivity.this, "image supprimé", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
        builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

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

            } catch (IOException e) {
                e.printStackTrace();
            }
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
