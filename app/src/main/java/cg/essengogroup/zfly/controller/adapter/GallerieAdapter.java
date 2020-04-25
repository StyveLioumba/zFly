package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Gallerie;
import cg.essengogroup.zfly.view.dialogs.DialogShowPhoto;

public class GallerieAdapter extends RecyclerView.Adapter<GallerieAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Gallerie> arrayList;

    public GallerieAdapter(Context context, ArrayList<Gallerie> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public GallerieAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_gallerie,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GallerieAdapter.MyViewHolder holder, int position) {
        Picasso.get()
                .load(arrayList.get(position).getImage())
                .error(R.drawable.default_img)
                .into(holder.imageUser);

        holder.cardView.setOnClickListener(v->
                new DialogShowPhoto(context,arrayList,position).show()
        );
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageUser;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView=itemView.findViewById(R.id.cardG);
            imageUser=itemView.findViewById(R.id.imgUser);
        }
    }
}
