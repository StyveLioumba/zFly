package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.utils.Methodes;
import cg.essengogroup.zfly.model.Model;
import cg.essengogroup.zfly.view.activities.DetailActivity;
import cg.essengogroup.zfly.view.dialogs.DialogMusicAccueil;

import static cg.essengogroup.zfly.controller.utils.Constantes.*;
import static cg.essengogroup.zfly.controller.utils.Methodes.makeTextViewResizable;

public class MultiViewTypeAdapter extends RecyclerView.Adapter {

    private ArrayList<Model> dataModel;
    private Context mContext;
    private MediaPlayer mPlayer;
    private DialogMusicAccueil dialog;
    private boolean fabStateVolume = false;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference,referenceUser;

    public MultiViewTypeAdapter(ArrayList<Model>data, Context context) {
        this.dataModel = data;
        this.mContext = context;
        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference = database.getReference().child("post");
        referenceUser=database.getReference().child("users");
        mPlayer=new MediaPlayer();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
                return new ImageTypeViewHolder(view);
            case AUDIO_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
                return new AudioTypeViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {

        switch (dataModel.get(position).getType()) {
            case 1:
                return IMAGE_TYPE;
            case 2:
                return AUDIO_TYPE;
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int listPosition) {

        Model model = dataModel.get(listPosition);
        if (model != null) {
            switch (model.getType()) {

                case IMAGE_TYPE:
                    ((ImageTypeViewHolder) holder).txtDesignation.setText(model.getDescription());
                    ((ImageTypeViewHolder) holder).txtDate.setText(Methodes.getDate(Long.parseLong(model.getCreateAt()),"dd-MMMM-yyyy"));

                    Methodes.glideDownload(
                            mContext,
                            model.getImage(),
                            R.drawable.default_img,
                            ((ImageTypeViewHolder) holder).imagePoster
                    );
                    ((ImageTypeViewHolder) holder).cardView.setOnClickListener(v->
                            mContext.startActivity(new Intent(mContext, DetailActivity.class)
                                    .putExtra("image",model.getImage())
                                    .putExtra("post_id",model.getPost_id())
                                    .putExtra("user_id",model.getUser_id())
                                    .putExtra("description",model.getDescription())
                                    .putExtra("date",model.getCreateAt())
                            )
                    );

                    referenceUser.child(model.getUser_id()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot!=null){
                                Glide.with(mContext.getApplicationContext())
                                        .load(String.valueOf(dataSnapshot.child("image").getValue()))
                                        .placeholder( R.drawable.default_img)
                                        .circleCrop()
                                        .into(((ImageTypeViewHolder) holder).imageUser);

                                ((ImageTypeViewHolder) holder).txtApseudo.setText(String.valueOf(dataSnapshot.child("Apseudo").getValue()));
                                ((ImageTypeViewHolder) holder).nomUser.setText(String.valueOf(dataSnapshot.child("pseudo").getValue()));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    reference.child(model.getPost_id())
                            .child("likes")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // get total available quest
                                    ((ImageTypeViewHolder) holder).txtLikes.setText(""+dataSnapshot.getChildrenCount());
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    reference.child(model.getPost_id())
                            .child("likes").child(firebaseUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        ((ImageTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                                    }else {
                                        ((ImageTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                    ((ImageTypeViewHolder) holder).imageLike.setOnClickListener(v->{

                        reference.child(model.getPost_id())
                                .child("likes").child(firebaseUser.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            dislikePost(model.getPost_id());
                                            ((ImageTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                                        }else {
                                            likePost(model.getPost_id());
                                            ((ImageTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    });
                    break;
                case AUDIO_TYPE:
                    ((AudioTypeViewHolder) holder).txtDesignation_.setText(model.getDescription());
                    ((AudioTypeViewHolder) holder).txtDate.setText(Methodes.getDate(Long.parseLong(model.getCreateAt()),"dd-MMMM-yyyy"));

                    referenceUser.child(model.getUser_id()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot!=null){
                                Glide.with(mContext.getApplicationContext())
                                        .load(String.valueOf(dataSnapshot.child("image").getValue()))
                                        .placeholder( R.drawable.default_img)
                                        .circleCrop()
                                        .into(((AudioTypeViewHolder) holder).imageUser);

                                ((AudioTypeViewHolder) holder).txtApseudo.setText(String.valueOf(dataSnapshot.child("Apseudo").getValue()));
                                ((AudioTypeViewHolder) holder).nomUser.setText(String.valueOf(dataSnapshot.child("pseudo").getValue()));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    /*((AudioTypeViewHolder) holder).fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           *//* if (fabStateVolume) {
                                if (mPlayer.isPlaying()) {
                                    mPlayer.stop();
                                }
                                ((AudioTypeViewHolder) holder).fab.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                                fabStateVolume = false;

                            }
                            else {
                                mPlayer = MediaPlayer.create(mContext, Uri.parse(model.getAudio()));
                                mPlayer.start();
                                ((AudioTypeViewHolder) holder).fab.setImageResource(R.drawable.ic_stop_black_24dp);
                                fabStateVolume = true;

                            }*//*

                        }
                    });*/

                    ((AudioTypeViewHolder) holder).cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog=new DialogMusicAccueil(mContext,model.getAudio());
                            dialog.setCancelable(false);
                            dialog.show();

                            Window window = dialog.getWindow();
                            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                            if (dialog.isShowing()){
                                Map<String,Object> data=new HashMap<>();
                                data.put("createAt",ServerValue.TIMESTAMP);

                                reference.child(model.getPost_id())
                                        .child("ecoute")
                                        .child(String.valueOf(System.currentTimeMillis()))
                                        .setValue(data)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                            }
                        }
                    });

                    reference.child(model.getPost_id())
                            .child("ecoute")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // get total available quest
                                    ((AudioTypeViewHolder) holder).txtLikes.setText(""+dataSnapshot.getChildrenCount());
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataModel.size();
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

    public static class ImageTypeViewHolder extends RecyclerView.ViewHolder {

        TextView txtDesignation,txtLikes,nomUser,txtDate,txtApseudo;
        CardView cardView;
        ImageView imageUser,imageLike,imagePoster;
        ImageButton partager;

        public ImageTypeViewHolder(View itemView) {
            super(itemView);

            txtDesignation=itemView.findViewById(R.id.txt);
            txtLikes=itemView.findViewById(R.id.txtLikes);
            nomUser=itemView.findViewById(R.id.pseudoUser);
            txtDate=itemView.findViewById(R.id.datePoster);
            txtApseudo=itemView.findViewById(R.id.Apseudo);

            imageLike=itemView.findViewById(R.id.imgLikes);
            imageUser=itemView.findViewById(R.id.imgUser);
            imagePoster=itemView.findViewById(R.id.img);

            partager=itemView.findViewById(R.id.partager);

            cardView=itemView.findViewById(R.id.cardImg);
        }
    }

    public static class AudioTypeViewHolder extends RecyclerView.ViewHolder {

        TextView txtDesignation_,txtLikes,nomUser,txtDate,txtApseudo;
        CardView cardView;
        FloatingActionButton fab;
        ImageView imageUser,imageLike,imagePoster;
        ImageButton commentaires,partager;

        public AudioTypeViewHolder(View itemView) {
            super(itemView);

            txtDesignation_=itemView.findViewById(R.id.txt);
            txtLikes=itemView.findViewById(R.id.txtLikes);
            nomUser=itemView.findViewById(R.id.pseudoUser);
            txtDate=itemView.findViewById(R.id.datePoster);
            txtApseudo=itemView.findViewById(R.id.Apseudo);

            imageLike=itemView.findViewById(R.id.imgLikes);
            imageUser=itemView.findViewById(R.id.imgUser);
            imagePoster=itemView.findViewById(R.id.img);

            commentaires=itemView.findViewById(R.id.comment);
            partager=itemView.findViewById(R.id.partager);
            fab=itemView.findViewById(R.id.fab);

            cardView=itemView.findViewById(R.id.cardImg);
        }
    }

}
