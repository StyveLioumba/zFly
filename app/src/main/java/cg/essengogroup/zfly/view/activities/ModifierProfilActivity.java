package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import com.iceteck.silicompressorr.SiliCompressor;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cg.essengogroup.zfly.R;

public class ModifierProfilActivity extends AppCompatActivity {

    private CircularImageView imageProfile;
    private TextInputEditText pseudo,biographie,telephone;
    private Button btnModifier;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private ImageButton btnRetour;

    private String lienProfileImage;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference referenceUser;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;

    private Uri imageUri=null;
    private Bitmap imageBitmap;

    private ProgressDialog dialogLoading;

    private TextInputLayout txtBioLayout;

    private String biographieValue="J'utilise Zfly";
    private boolean isArtisteValue=true;

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser()==null){
            startActivity(new Intent(ModifierProfilActivity.this,LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_profil);

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        referenceUser=database.getReference("users/"+String.valueOf(user.getUid()));
        mStorageRef=storage.getReference().child("profileImage").child("profile"+System.currentTimeMillis()+".jpg");

        imageProfile=findViewById(R.id.imgUser);
        pseudo=findViewById(R.id.pseudo);
        biographie=findViewById(R.id.biographie);
        telephone=findViewById(R.id.tel);
        fab=findViewById(R.id.addImg);
        btnModifier=findViewById(R.id.btnModifier);
        progressBar=findViewById(R.id.progress);
        btnRetour=findViewById(R.id.back);
        txtBioLayout=findViewById(R.id.txtBio);

        chargeImageProfileIfExist();
        chargeDataTextIfExist();

        fab.setOnClickListener(v->selectImageFromGallery());

        btnRetour.setOnClickListener(v->{
            startActivity(new Intent(ModifierProfilActivity.this,ProfileActivity.class));
            finish();
        });

        dialogLoading=new ProgressDialog(ModifierProfilActivity.this);
        dialogLoading.setMessage("Chargement encours ...");

        btnModifier.setOnClickListener(v->{
            progressBar.setVisibility(View.VISIBLE);
            uploadImageToStorage();
        });

        DatabaseReference isArtisteRef = database.getReference("users/"+user.getUid());
        isArtisteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isArtisteValue=(Boolean) dataSnapshot.child("isArtiste").getValue();

                if (!isArtisteValue){
                    txtBioLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void chargeImageProfileIfExist(){
        user=mAuth.getCurrentUser();
        if (user!=null){
            if (user.getPhotoUrl()!=null){
                Picasso.get()
                        .load(user.getPhotoUrl().toString())
                        .placeholder(R.drawable.imgdefault)
                        .error(R.drawable.imgdefault)
                        .into(imageProfile);
            }
        }
    }

    private void chargeDataTextIfExist(){
        referenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pseudo.setText(String.valueOf(dataSnapshot.child("pseudo").getValue()));
                telephone.setText(String.valueOf(dataSnapshot.child("tel").getValue()));
                if (dataSnapshot.child("biographie").exists()){
                    biographie.setText(String.valueOf(dataSnapshot.child("biographie").getValue()));
                }else {
                    biographie.setText(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Procedure d'envoie
     * tout d'abord j'envoie l'image via la methode uploadImageToStorage(); , une fois l'image envoyé
     * je mets a jour le profile via la methode saveAllInformation(); une fois fait
     * j'envoie les info dans la database via la methode validateSendDataToFirebase();
     */
    private void uploadImageToStorage(){
        if (imageUri!=null){
            dialogLoading.show();
            //ce bout de code me permet de compresser la taille de l'image

            /*Bitmap bitmap = ((BitmapDrawable) imageProfile.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mStorageRef.putBytes(data);*/

            UploadTask uploadTask = mStorageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot!=null){
                        mStorageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            lienProfileImage=uri.toString();

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
            lienProfileImage=user.getPhotoUrl().toString();
            validateSendDataToFirebase();
        }
    }

    private void validateSendDataToFirebase(){
        String telValue=telephone.getText().toString();
        String pseudoValue=pseudo.getText().toString();
        biographieValue=biographie.getText().toString();

        if (TextUtils.isEmpty(telValue)){
            telephone.setError("entrez votre numero");
            telephone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pseudoValue)){
            pseudo.setError("entrez votre pseudo");
            pseudo.requestFocus();
            return;
        }

        if (isArtisteValue && TextUtils.isEmpty(biographieValue)){
            biographie.setError("parler un peu de vous ");
            biographie.requestFocus();
            return;
        }

        referenceUser.child("pseudo").setValue(pseudoValue).addOnSuccessListener(aVoidP -> {
            referenceUser.child("tel").setValue(telValue).addOnSuccessListener(aVoidT -> {
                referenceUser.child("biographie").setValue(biographieValue).addOnSuccessListener(aVoidB -> {
                    referenceUser.child("image").setValue(lienProfileImage).addOnSuccessListener(aVoidI -> {
                        referenceUser.child("updateAt").setValue(ServerValue.TIMESTAMP).addOnSuccessListener(aVoid -> {
                            if (dialogLoading.isShowing()){
                                dialogLoading.dismiss();
                            }
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(ModifierProfilActivity.this,ProfileActivity.class));
                            finish();
                        });
                    });
                });
            });
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
                    imageProfile.setImageBitmap(imageBitmap);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

}
