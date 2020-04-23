package cg.essengogroup.zfly.controller.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.view.dialogs.DialogMusicAccueil;

public class AllMediaAdapter extends RecyclerView.Adapter<AllMediaAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Music> musicArrayList;
    private DialogMusicAccueil dialog;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    public AllMediaAdapter(Context context, ArrayList<Music> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;

        mAuth= FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        database= FirebaseDatabase.getInstance();
        reference=database.getReference("users/"+user.getUid()+"/chansons");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_media,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {



        Music music=musicArrayList.get(position);
        holder.txtArtiste.setText(music.getArtiste());
        holder.txtMorceau.setText(music.getMorceau());

        holder.cardView.setOnClickListener(v->{

            dialog=new DialogMusicAccueil(context,music.getChanson());
            dialog.setCancelable(false);
            dialog.show();
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        });

        holder.btnClose.setOnClickListener(v->dialogMethode(music.getRacine()));


    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView txtMorceau,txtArtiste;
        ImageView fab;
        ImageButton btnClose;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.cardM);
            txtMorceau=itemView.findViewById(R.id.txtTitre);
            txtArtiste=itemView.findViewById(R.id.txtArtiste);
            fab=itemView.findViewById(R.id.fab);
            btnClose=itemView.findViewById(R.id.btnClose);
        }
    }


    private void dialogMethode(String imageRacine){
        AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        dialog.setTitle("supprimer");
        dialog.setMessage("Voulez vous vraiment supprimer cette chanson ?");
        dialog.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reference.child(imageRacine)
                        .removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Toast.makeText(context, "chanson supprimer", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialog.setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
