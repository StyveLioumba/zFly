package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.view.activities.DetailMusicActivity;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Music> musicArrayList;

    public AlbumAdapter(Context context, ArrayList<Music> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_album,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Music music=musicArrayList.get(position);
        holder.txtAlbum.setText(music.getAlbum());
        Picasso.get()
                .load( music.getCover())
                .placeholder(R.drawable.default_img)
                .error(R.drawable.music_cover)
                .into(holder.imageView);

        holder.cardView.setOnClickListener(v->{
            context.startActivity(new Intent(context, DetailMusicActivity.class)
                    .putExtra("key",music.getAlbum())
                    .putExtra("categorieKey","album")
            );
        });
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView txtAlbum;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.cardAlbum);
            imageView=itemView.findViewById(R.id.imgAlbum);
            txtAlbum=itemView.findViewById(R.id.txtAlbum);
        }
    }
}
