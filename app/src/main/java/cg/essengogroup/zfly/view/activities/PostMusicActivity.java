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

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cg.essengogroup.zfly.controller.utils.Methodes.formateMilliSeccond;
import static cg.essengogroup.zfly.controller.utils.Methodes.hideKeyboard;

public class PostMusicActivity extends AppCompatActivity {
    private ImageView imageCover;
    private Button addSong;
    private TextInputEditText morceauName,artisteName,albumName;
    private Spinner genreName;
    private CardView btnPublier;
    private ProgressBar progressBar,progressBarTop;
    private ImageButton playPreview;

    private String lienImage,lienChanson,imgArtiste;

    private Uri uriPreviewImage,uriChanson;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private StorageReference mStorageRef ;
    private DatabaseReference reference;

    private Intent intent;

    private static final int CHOIX_IMAGE=101,CHOIX_CHANSON=102,STORAGE_PERMISSION_CODE = 123;

    private MediaPlayer mPlayer;
    private boolean fabStateVolume = false;

    private String[] listeGenre={
            "Chanson","instrumental","Mix Dj" };

    private ProgressDialog dialogLoading;
    private long millSecond;
    private String defaultCover="https://firebasestorage.googleapis.com/v0/b/zfly2020-151d6.appspot.com/o/default%2Fmusic_cover.png?alt=media&token=53a33970-d821-4a95-8c15-b08ed8109de6";
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_music);

        //Requesting storage permission
        requestStoragePermission();

        hideKeyboard(PostMusicActivity.this);

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("users/"+firebaseUser.getUid());

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        imageCover=findViewById(R.id.imgCover);
        addSong=findViewById(R.id.addSong);
        playPreview=findViewById(R.id.playPreview);

        morceauName=findViewById(R.id.music_name);
        artisteName=findViewById(R.id.music_artiste);
        albumName=findViewById(R.id.music_album);

        genreName=findViewById(R.id.spinnerGenre);

        btnPublier=findViewById(R.id.btnPublier);

        progressBar=findViewById(R.id.progress);
        progressBarTop=findViewById(R.id.progressTop);


        dialogLoading=new ProgressDialog(PostMusicActivity.this);
        dialogLoading.setMessage("Chargement encours ...");
        dialogLoading.setCancelable(false);

        imageCover.setOnClickListener(v->selectionnerImage());
        btnPublier.setOnClickListener(v->uploadSongToFireBase());
        addSong.setOnClickListener(v->getMusic());
        playPreview.setOnClickListener(v->playPreview());

        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1,listeGenre);
        genreName.setAdapter(adapter);

    }

    private void selectionnerImage(){
        ImagePicker.create(this) // Activity or Fragment
                .start();
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
                mPlayer = MediaPlayer.create(PostMusicActivity.this, uriChanson);
                mPlayer.setLooping(true);
                mPlayer.start();
                playPreview.setImageResource(R.drawable.ic_stop_black_24dp);
                fabStateVolume = true;
            }
        }else {
            Toast.makeText(this, "pensez à ajouter une chanson", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (ImagePicker.shouldHandle(requestCode, resultCode, data) ){

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
                    uriPreviewImage= Uri.fromFile(new File(filePath));
                    imageCover.setImageBitmap(imageBitmap);
                }

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
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE );
            returnCursor.moveToFirst();
            float sizeSongOctetToMega=(float) returnCursor.getLong(sizeIndex)/ 1048576;
            morceauName.setText(returnCursor.getString(nameIndex));

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(PostMusicActivity.this,uriChanson);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            millSecond = Long.parseLong(durationStr);

            Log.e("TAG", "onActivityResult: "+millSecond );
            Log.e("TAG", "onActivityResult: converti "+formateMilliSeccond(millSecond) );
        }


    }

    private void getMusic(){
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(
                intent,
                "Open Audio (mp3) file"),
                CHOIX_CHANSON);
    }

    /**
     *j'upload d'abord la chanson
     * si c'est ok , j'upload l'image
     * si c'est ok , j'insers dans la database
     */
    private void uploadSongToFireBase(){

        mStorageRef=FirebaseStorage.getInstance()
                .getReference()
                .child("music")
                .child("son"+System.currentTimeMillis()+".mp3");

        if (uriChanson !=null){
            dialogLoading.show();
            mStorageRef.putFile(uriChanson).addOnSuccessListener(taskSnapshot -> {
                // Get a URL to the uploaded content
                if (taskSnapshot!=null){

                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienChanson=uri.toString();
                            uploadImageToFireBase();
                        }
                    });


                }

            }).addOnFailureListener(exception -> {
                progressBar.setVisibility(View.GONE);
                if (dialogLoading.isShowing()){
                    dialogLoading.dismiss();
                }
            });
        }
    }

    private void uploadImageToFireBase(){

        mStorageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("image_cover")
                .child("cover"+System.currentTimeMillis()+".jpg");

        if (uriPreviewImage !=null){
            //ce bout de code me permet de compresser la taille de l'image

            UploadTask uploadTask = mStorageRef.putFile(uriPreviewImage);
            uploadTask.addOnSuccessListener(taskSnapshot -> {

                if (taskSnapshot!=null){

                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienImage=uri.toString();
                            sendMusicToFirebaseDataBase();
                        }
                    });
                }

            }).addOnFailureListener(exception -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }else {
            lienImage=defaultCover;
            sendMusicToFirebaseDataBase();
        }
    }

    private void sendMusicToFirebaseDataBase(){

        String artisteNameValue=firebaseUser.getDisplayName();
        String albumNameValue=albumName.getText().toString().trim();
        String genreValue=genreName.getSelectedItem().toString().trim();
        String morceauNameValue=morceauName.getText().toString().trim();

        if (TextUtils.isEmpty(artisteNameValue)){
            artisteNameValue="inconnu";
        }

        if (TextUtils.isEmpty(albumNameValue)){
            albumNameValue="inconnu";
        }

        if (TextUtils.isEmpty(morceauNameValue)){
            morceauNameValue="inconnu";
        }

        Map<String, Object> post=new HashMap<>();
        post.put("user_id",firebaseUser.getUid());
        post.put("cover",lienImage);
        post.put("chanson",lienChanson);
        post.put("morceau",morceauNameValue);
        post.put("artiste",artisteNameValue);
        post.put("album",albumNameValue);
        post.put("genre",genreValue);
        post.put("duration",millSecond);
        post.put("time",formateMilliSeccond(millSecond));


        if (uriChanson!=null ){
            progressBarTop.setVisibility(View.VISIBLE);

            database.getReference()
                    .child("music")
                    .child("morceaux")
                    .child("son"+System.currentTimeMillis())
                    .setValue(post)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            /**
                             * si l'insertion du son est un succes
                             * on cree une table genre d'abord puis album et enfin artiste
                             */
                            createGenre();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (dialogLoading.isShowing()) dialogLoading.dismiss();
                            // Write failed
                            // ...
                        }
                    });

        }else {
            Toast.makeText(this, "importer une chanson", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * --------------------------------------------
     * tout d'abord on a :
     * Genre
     * Album
     * Artiste
     * ---------------------------------------------
     */
    private void createGenre(){

        String genreValue=genreName.getSelectedItem().toString().trim();

        Map<String, Object> data=new HashMap<>();
        data.put("genre",genreValue);

        database.getReference()
                .child("music")
                .child("genres")
                .child(genreValue)
                .setValue(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        /*createAlbum();*///Avant je passais d'abord par la creation de l'album puis l'artiste
                        createArtiste();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (dialogLoading.isShowing())
                            dialogLoading.dismiss();
                    }
                });
    }
   /* private void createAlbum(){

        String albumNameValue=albumName.getText().toString().trim();

        if (TextUtils.isEmpty(albumNameValue)){
            albumNameValue="inconnu";
        }

        Map<String, Object> data=new HashMap<>();
        data.put("cover",lienImage);
        data.put("album",albumNameValue);

        database.getReference()
                .child("music")
                .child("albums")
                .child(albumNameValue)
                .setValue(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        createArtiste();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        if (dialogLoading.isShowing())
                            dialogLoading.dismiss();
                    }
                });
    }*/
    private void createArtiste(){

        String artisteNameValue=firebaseUser.getDisplayName();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot!=null){
                    imgArtiste=String.valueOf(dataSnapshot.child("image").getValue());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Map<String, Object> data=new HashMap<>();

        if (TextUtils.isEmpty(artisteNameValue)){
            artisteNameValue="inconnu";
        }

        if (!TextUtils.isEmpty(imgArtiste)){
            data.put("image_artiste",imgArtiste);
        }else {
            data.put("image_artiste",lienImage);
        }

        data.put("artiste",artisteNameValue);

        database.getReference()
                .child("music")
                .child("artistes")
                .child(artisteNameValue)
                .setValue(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendMusicToFirebaseDataBaseUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (dialogLoading.isShowing())
                            dialogLoading.dismiss();
                    }
                });
    }


    private void sendMusicToFirebaseDataBaseUser(){
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
        post.put("artiste",firebaseUser.getDisplayName());

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
                            progressBarTop.setVisibility(View.GONE);
                            if (dialogLoading.isShowing()){
                                dialogLoading.dismiss();
                            }
                            startActivity(new Intent(PostMusicActivity.this,AccueilActivity.class));
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
    public void onBackPressed() {
        super.onBackPressed();
        if (uriChanson!=null){
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
        }
    }
}
