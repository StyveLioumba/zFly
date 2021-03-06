package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Place;
import cg.essengogroup.zfly.view.activities.DetailPlaceActivity;
import cg.essengogroup.zfly.view.activities.ModifierPlaceActivity;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Place> placeArrayList;

    public PlaceAdapter(Context context, ArrayList<Place> placeArrayList) {
        this.context = context;
        this.placeArrayList = placeArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_place,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.txtType.setText(placeArrayList.get(position).getType());
        holder.txtTitre.setText(placeArrayList.get(position).getNom());
        holder.txtAdresse.setText(placeArrayList.get(position).getAdresse());

        Picasso.get()
                .load(placeArrayList.get(position).getImage_couverture())
                .placeholder(R.drawable.default_img)
                .error(R.drawable.default_img)
                .into(holder.imageView);

        holder.relativeLayout.setOnClickListener(v->{
            context.startActivity(new Intent(context, DetailPlaceActivity.class)
                    .putExtra("adresse",placeArrayList.get(position).getAdresse())
                    .putExtra("createAt",placeArrayList.get(position).getCreateAt())
                    .putExtra("description",placeArrayList.get(position).getDescription())
                    .putExtra("image_couverture",placeArrayList.get(position).getImage_couverture())
                    .putExtra("nom",placeArrayList.get(position).getNom())
                    .putExtra("numero",placeArrayList.get(position).getNumero())
                    .putExtra("type",placeArrayList.get(position).getType())
                    .putExtra("user_id",placeArrayList.get(position).getUser_id())
                    .putExtra("place_id",placeArrayList.get(position).getPlace_id())
            );

        });

        DatabaseReference isArtisteRef = FirebaseDatabase.getInstance().getReference("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        isArtisteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!((Boolean) dataSnapshot.child("isPlace").getValue()) && !String.valueOf(dataSnapshot.child("user_id").getValue()).equalsIgnoreCase(placeArrayList.get(position).getUser_id())){
                    holder.imgBtn.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.imgBtn.setOnClickListener(v-> context.startActivity(new Intent(context, ModifierPlaceActivity.class).putExtra("place",placeArrayList.get(position))));
    }

    @Override
    public int getItemCount() {
        return placeArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        ImageView imageView,imgBtn;
        TextView txtTitre,txtAdresse,txtType;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout=itemView.findViewById(R.id.relaPlace);
            imageView=itemView.findViewById(R.id.imgCouverture);
            txtAdresse=itemView.findViewById(R.id.txtAdresse);
            txtTitre=itemView.findViewById(R.id.txtTitre);
            txtType=itemView.findViewById(R.id.txtType);
            imgBtn=itemView.findViewById(R.id.btnModifier);
        }
    }
}
