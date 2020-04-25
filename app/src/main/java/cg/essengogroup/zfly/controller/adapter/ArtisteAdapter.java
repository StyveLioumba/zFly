package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.view.activities.DetailMusicActivity;

public class ArtisteAdapter extends RecyclerView.Adapter<ArtisteAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Music> musicArrayList;

    public ArtisteAdapter(Context context, ArrayList<Music> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_artiste_music,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Music music=musicArrayList.get(position);
        holder.txtArtist.setText(music.getArtiste());

        Picasso.get()
                .load( music.getCover())
                .placeholder(R.drawable.music_cover)
                .error(R.drawable.music_cover)
                .into(holder.imageView);

        holder.relativeLayout.setOnClickListener(v->{
            context.startActivity(new Intent(context, DetailMusicActivity.class)
                    .putExtra("key",music.getArtiste())
                    .putExtra("categorieKey","artiste")
            );
        });
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout relativeLayout;
        ImageView imageView;
        TextView txtArtist;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout=itemView.findViewById(R.id.item_artiste);
            imageView=itemView.findViewById(R.id.imgArtist);
            txtArtist=itemView.findViewById(R.id.pseudoUser);
        }
    }
}
