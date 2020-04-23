package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.view.activities.DetailMusicActivity;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Music> musicArrayList;

    public GenreAdapter(Context context, ArrayList<Music> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_genre,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Music music=musicArrayList.get(position);
        holder.txtGenre.setText(music.getGenre());

        holder.linearLayout.setOnClickListener(v->{
            context.startActivity(new Intent(context, DetailMusicActivity.class)
                    .putExtra("key",music.getGenre())
                    .putExtra("categorieKey","genre")
            );
        });
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView txtGenre;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout=itemView.findViewById(R.id.lineGenre);
            txtGenre=itemView.findViewById(R.id.txtTitre);
        }
    }
}
