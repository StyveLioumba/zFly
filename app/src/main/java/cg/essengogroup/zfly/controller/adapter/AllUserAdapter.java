package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.User;
import cg.essengogroup.zfly.view.activities.DetailArtistActivity;

public class AllUserAdapter extends RecyclerView.Adapter<AllUserAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<User> arrayList;

    private FirebaseAuth mAuth;
    private FirebaseUser userFire;

    public AllUserAdapter(Context context, ArrayList<User> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

        mAuth=FirebaseAuth.getInstance();
        userFire=mAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public AllUserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_all_artiste,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllUserAdapter.MyViewHolder holder, int position) {

        holder.cardView.setOnClickListener(v->
                context.startActivity(new Intent(context, DetailArtistActivity.class)
                        .putExtra("apseudo",arrayList.get(position).getApseudo())
                        .putExtra("image",arrayList.get(position).getImage())
                        .putExtra("nom",arrayList.get(position).getNom())
                        .putExtra("prenom",arrayList.get(position).getPrenom())
                        .putExtra("pseudo",arrayList.get(position).getPseudo())
                        .putExtra("tel",arrayList.get(position).getTel())
                        .putExtra("user_id",arrayList.get(position).getUser_id())
                        .putExtra("user",arrayList.get(position))
                )
        );

        holder.pseudo.setText(arrayList.get(position).getPseudo().toLowerCase());
        holder.Apseudo.setText(arrayList.get(position).getApseudo().toLowerCase());

        Glide.with(context.getApplicationContext())
                .load(arrayList.get(position).getImage())
                .placeholder(R.drawable.default_img)
                .circleCrop()
                .into(holder.imageUser);

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        CircularImageView imageUser;
        TextView pseudo,Apseudo;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView=itemView.findViewById(R.id.item_artiste);
            imageUser=itemView.findViewById(R.id.imgArtist);
            pseudo=itemView.findViewById(R.id.pseudoUser);
            Apseudo=itemView.findViewById(R.id.ApseudoUser);
        }

    }
}
