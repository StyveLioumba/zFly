package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.utils.Methodes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    private ImageView imageView,imageLike,imageUser;
    private TextView txtDescription,txtLikes,nomUser,txtDate;
    private ImageButton partager;

    private Intent intent;
    private String imageValue,descriptionValue,dateValue,post_idValue,user_idValue;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference,referenceUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        darkMode();
        setContentView(R.layout.activity_detail);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intent=getIntent();


        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference = database.getReference().child("post");
        referenceUser=database.getReference().child("users");

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        imageView=findViewById(R.id.imgSelected);
        txtDescription=findViewById(R.id.txtDescription);

        imageLike=findViewById(R.id.imgLikes);
        partager=findViewById(R.id.partager);
        txtLikes=findViewById(R.id.txtLikes);

        nomUser=findViewById(R.id.pseudoUser);
        txtDate=findViewById(R.id.datePoster);

        imageUser=findViewById(R.id.imgUser);

        if (intent!=null){
            imageValue=intent.getStringExtra("image");
            descriptionValue=intent.getStringExtra("description");
            dateValue=intent.getStringExtra("date");
            post_idValue=intent.getStringExtra("post_id");
            user_idValue=intent.getStringExtra("user_id");
        }

        Picasso.get()
                .load(imageValue)
                .placeholder(R.drawable.imgdefault)
                .error(R.drawable.imgdefault)
                .into(imageView);

        txtDescription.setText(descriptionValue);
        txtDate.setText("le "+Methodes.getDate(Long.parseLong(dateValue),"dd-MM-yyyy"));

        imageLike.setOnClickListener(v->likeOrDislike());
        partager.setOnClickListener(v->shareContent(imageView));
        verifierIfIsLike();
        getNbreLikes();
        getInfoUser();
    }

    private void darkMode(){
        if (AppCompatDelegate.getDefaultNightMode()!=AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.DarkTheme);
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


    private void likePost(String post_id){

        Map<String, Object> post=new HashMap<>();
        post.put("user_id",firebaseUser.getUid());
        post.put("createAt", ServerValue.TIMESTAMP);

        reference.child(post_id).child("likes").child(firebaseUser.getUid()).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

    }

    private void dislikePost(String post_id){
        reference.child(post_id)
                .child("likes")
                .child(firebaseUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

            }
        });
    }

    private void getNbreLikes(){
        reference.child(post_idValue)
                .child("likes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get total available quest
                        txtLikes.setText(""+dataSnapshot.getChildrenCount());
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void verifierIfIsLike(){
        reference.child(post_idValue)
                .child("likes").child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                        }else {
                            imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void likeOrDislike(){
        reference.child(post_idValue)
                .child("likes").child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            dislikePost(post_idValue);
                            imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                        }else {
                            likePost(post_idValue);
                            imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        }
                        getNbreLikes();
                        verifierIfIsLike();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getInfoUser(){
        referenceUser.child(user_idValue).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    Picasso.get()
                            .load(String.valueOf(dataSnapshot.child("image").getValue()))
                            .placeholder(R.drawable.imgdefault)
                            .error(R.drawable.imgdefault)
                            .into(imageUser);

                   nomUser.setText(String.valueOf(dataSnapshot.child("pseudo").getValue()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

   /* @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }*/

    private void shareContent(ImageView imageView){

        Bitmap bitmap =getBitmapFromView(imageView);
        try {
            File file = new File(getExternalCacheDir(),getResources().getString(R.string.app_name)+System.currentTimeMillis()+".png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }   else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }
}
