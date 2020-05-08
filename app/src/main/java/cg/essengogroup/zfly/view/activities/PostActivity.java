package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.view.dialogs.Dialog_loading;
import id.zelory.compressor.Compressor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private static final int CHOIX_IMAGE=101,CHOIX_CHANSON=102,STORAGE_PERMISSION_CODE = 123;
    private Uri uriPreviewImage,uriChanson;

    private CardView btnPublier,btnSon,btnImg;
    private TextInputEditText txtDescription;
    private ImageView imageView,imageAddSon;
    private ProgressBar progressBar,progressBarTop;
    private FloatingActionButton fab;

    private Intent intent;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private String lienImage,lienChanson;
    private RelativeLayout relativeLayout;

    private MediaPlayer mPlayer;
    private boolean fabStateVolume = false;

    private Dialog_loading dialogLoading;

    private LinearLayout linearLayout;
    private int sizeSong=0;

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser()==null){
            startActivity(new Intent(PostActivity.this,LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        //Requesting storage permission
        requestStoragePermission();

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("users/"+firebaseUser.getUid());

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        btnPublier=findViewById(R.id.btnPublier);
        txtDescription=findViewById(R.id.txtDescription);
        imageView=findViewById(R.id.imgPreview);
        progressBar=findViewById(R.id.progress);
        progressBarTop=findViewById(R.id.progressTop);
        btnImg=findViewById(R.id.btnImg);
        btnSon=findViewById(R.id.btnSon);
        relativeLayout=findViewById(R.id.relaSong);
        fab=findViewById(R.id.fab);
        imageAddSon=findViewById(R.id.importeSon);
        linearLayout=findViewById(R.id.lineTop);
        TextView txtImg=findViewById(R.id.txtImg);
        TextView txtSong=findViewById(R.id.txtSong);
        txtImg.setTextSize(14);
        txtSong.setTextSize(11);

        dialogLoading=new Dialog_loading(PostActivity.this);
        dialogLoading.setCancelable(false);

        imageView.setOnClickListener(v->selectionnerImage());
        fab.setOnClickListener(v->playPreview());
        imageAddSon.setOnClickListener(v->getMusic());

        btnPublier.setOnClickListener(v->{
            if (relativeLayout.getVisibility()==View.VISIBLE){
                if(uriChanson!=null){
                    uploadSongToFireBase();
                }else {
                    Toast.makeText(this, "Ajoutez une chanson avant de publier", Toast.LENGTH_SHORT).show();
                }
            }

            if (imageView.getVisibility()==View.VISIBLE){
                if (uriPreviewImage!=null){
                    uploadImageToFireBase();
                }else {
                    Toast.makeText(this, "Ajoutez une image avant de publier", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSon.setOnClickListener(v->{
            if (imageView.getVisibility()==View.VISIBLE){
                imageView.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);
                txtImg.setTextSize(11);
                txtSong.setTextSize(14);
            }
        });

        btnImg.setOnClickListener(v->{
            if (relativeLayout.getVisibility()==View.VISIBLE){
                relativeLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                txtImg.setTextSize(14);
                txtSong.setTextSize(11);
            }
        });

        DatabaseReference isArtisteRef = database.getReference("users/"+firebaseUser.getUid());
        isArtisteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!((Boolean) dataSnapshot.child("isArtiste").getValue())){
                    linearLayout.setVisibility(View.GONE);
                }
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

    private void playPreview(){
        if (fabStateVolume) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            fab.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            fabStateVolume = false;

        } else {
            if (uriChanson!=null){
                mPlayer = MediaPlayer.create(PostActivity.this, uriChanson);
                mPlayer.setLooping(true);
                mPlayer.start();
                fab.setImageResource(R.drawable.ic_stop_black_24dp);
                fabStateVolume = true;
            }else {
                Toast.makeText(this, "Ajoute une chanson", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getMusic(){
        Intent intent = new Intent();
        intent.setType("audio/mp3");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(
                intent,
                "Open Audio (mp3) file"),
                CHOIX_CHANSON);
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
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (requestCode == CHOIX_CHANSON && resultCode == RESULT_OK && data != null ){

            uriChanson = data.getData();
            Cursor returnCursor = getContentResolver().query(uriChanson, null, null, null, null);
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            float sizeSongOctetToMega=(float) returnCursor.getLong(sizeIndex)/ 1048576;
            txtDescription.setText(returnCursor.getString(nameIndex));
            sizeSong= (int) sizeSongOctetToMega;

            if (sizeSongOctetToMega>2){
                Toast.makeText(this, "cette chanson pèse "+String.valueOf(sizeSongOctetToMega).substring(0,3)+"Mo", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadImageToFireBase(){

        mStorageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("post")
                .child("post"+System.currentTimeMillis()+".jpg");

        if (uriPreviewImage !=null){
            dialogLoading.show();
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

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload is paused");
                }
            });
        }
    }

    private void uploadSongToFireBase(){

        mStorageRef=FirebaseStorage.getInstance()
                .getReference()
                .child("post")
                .child("son"+System.currentTimeMillis()+".mp3");

        if (uriChanson !=null && sizeSong<=2){
            dialogLoading.show();
            mStorageRef.putFile(uriChanson).addOnSuccessListener(taskSnapshot -> {
                // Get a URL to the uploaded content
                if (taskSnapshot!=null){

                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienChanson=uri.toString();
                            sendPostToFireBase();
                        }
                    });
                }

            }).addOnFailureListener(exception -> {
                if (dialogLoading.isShowing()){
                    dialogLoading.dismiss();
                }
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }else {
            if (dialogLoading.isShowing()){
                dialogLoading.dismiss();
            }
            Toast.makeText(this, "impossible de publier cette chanson qui pèse plus de 2 Mo", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPostToFireBase(){

        String descriptionValue=txtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(descriptionValue)){
            descriptionValue="";
        }
        progressBarTop.setVisibility(View.VISIBLE);

        Map<String,Object> postData=new HashMap<>();
        postData.put("user_id",firebaseUser.getUid());
        postData.put("description",descriptionValue);
        postData.put("createAt", ServerValue.TIMESTAMP);

        if (relativeLayout.getVisibility()==View.VISIBLE){
            postData.put("sonPost",lienChanson);
            postData.put("imagePost",null);
            postData.put("type",2);
        }else {
            postData.put("imagePost",lienImage);
            postData.put("sonPost",null);
            postData.put("type",1);
        }

        database.getReference()
                .child("post")
                .child("post"+System.currentTimeMillis())
                .setValue(postData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setNumPost();
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
    }

    private void setNumPost(){

        Map<String,Object> data=new HashMap<>();
        data.put("user_id",firebaseUser.getUid());
        data.put("createAt", ServerValue.TIMESTAMP);

        reference.child("posts")
                .child("post_numero_"+System.currentTimeMillis())
                .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(Void aVoid) {
                if (dialogLoading.isShowing()){
                    dialogLoading.dismiss();
                }
                startActivity(new Intent(PostActivity.this,AccueilActivity.class));
                finish();
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
