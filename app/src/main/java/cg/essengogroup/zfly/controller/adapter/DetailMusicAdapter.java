package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.interfaces.OnMusicItemClickListener;
import cg.essengogroup.zfly.model.Music;

public class DetailMusicAdapter extends RecyclerView.Adapter<DetailMusicAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Music> musicArrayList;
    private OnMusicItemClickListener listener;
    private int selectedPostion;

    public DetailMusicAdapter(Context context, ArrayList<Music> musicArrayList, OnMusicItemClickListener listener) {
        this.context = context;
        this.musicArrayList = musicArrayList;
        this.listener=listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_morceau,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Music music=musicArrayList.get(position);

        if (selectedPostion==position){
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorBackFlou));
            holder.btnPlay.setVisibility(View.VISIBLE);
        }
        else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            holder.btnPlay.setVisibility(View.GONE);
        }
        holder.txtArtiste.setText(music.getArtiste());
        holder.txtMorceau.setText(music.getMorceau());

        Picasso.get()
                .load( music.getCover())
                .placeholder(R.drawable.default_img)
                .error(R.drawable.default_img)
                .into(holder.imageView);

        holder.bind(music,listener);
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        ImageView imageView,btnPlay;
        TextView txtMorceau,txtArtiste;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout=itemView.findViewById(R.id.lineMorceau);
            imageView=itemView.findViewById(R.id.imgAlbum);
            btnPlay=itemView.findViewById(R.id.btnPlay);
            txtMorceau=itemView.findViewById(R.id.txtTitre);
            txtArtiste=itemView.findViewById(R.id.txtArtiste);
        }

        public void bind(Music music, OnMusicItemClickListener listener){
            itemView.setOnClickListener(v->listener.onClickListener(music,getLayoutPosition()));
        }
    }

    public void setSelectedPostion(int selectedPostion) {
        this.selectedPostion = selectedPostion;
    }

    public int getSelectedPostion() {
        return selectedPostion;
    }
}
