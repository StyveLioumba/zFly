package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Gallerie;
import cg.essengogroup.zfly.view.dialogs.DialogShowPhoto;

public class AllPhotoAdapter extends RecyclerView.Adapter<AllPhotoAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Gallerie> arrayList;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private int keyValue=0;
    private String placeId;

    public AllPhotoAdapter(Context context, ArrayList<Gallerie> arrayList,int keyValue,String placeId) {
        this.context = context;
        this.arrayList = arrayList;
        this.keyValue=keyValue;
        this.placeId=placeId;

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();

    }

    @NonNull
    @Override
    public AllPhotoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_all_photo,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllPhotoAdapter.MyViewHolder holder, int position) {

        Gallerie gallerie=arrayList.get(position);
        Picasso.get()
                .load(gallerie.getImage())
                .error(R.drawable.default_img)
                .into(holder.imageUser);
        holder.btnClose.setOnClickListener(v->dialog(gallerie.getRacine()));
        holder.cardView.setOnClickListener(v-> new DialogShowPhoto(context,arrayList,position).show());

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageUser;
        ImageButton btnClose;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView=itemView.findViewById(R.id.cardG);
            imageUser=itemView.findViewById(R.id.imgUser);
            btnClose=itemView.findViewById(R.id.btnClose);
        }
    }

    private void dialog(String imageRacine){
        AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        dialog.setTitle("supprimer");
        dialog.setMessage("Voulez vous vraiment supprimer cette image ?");
        dialog.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (keyValue==0 && TextUtils.isEmpty(placeId)){
                    reference=database.getReference("users/"+user.getUid()+"/galerie/"+imageRacine);
                }else {
                    reference=database.getReference("place/"+placeId+"/galerie/"+imageRacine);
                }
                reference.removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Toast.makeText(context, "image supprimé", Toast.LENGTH_SHORT).show();
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
