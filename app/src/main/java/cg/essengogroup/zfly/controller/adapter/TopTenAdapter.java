package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
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

public class TopTenAdapter extends RecyclerView.Adapter<TopTenAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Music> musicArrayList;

    public TopTenAdapter(Context context, ArrayList<Music> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_top,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Music music=musicArrayList.get(position);
        holder.txtTitre.setText(music.getMorceau());
        holder.txtArtiste.setText(music.getArtiste());
//        holder.txtNum.setText(String.valueOf(position+1));
        holder.txtNum.setText(music.getNbreEcoute());
        Picasso.get()
                .load( music.getCover())
                .resize(400,400)
                .placeholder(R.drawable.default_img)
                .error(R.drawable.default_img)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView txtTitre,txtNum,txtArtiste;
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout=itemView.findViewById(R.id.lineGenre);
            txtTitre=itemView.findViewById(R.id.txtTitre);
            txtNum=itemView.findViewById(R.id.txtNum);
            txtArtiste=itemView.findViewById(R.id.txtArtiste);
            imageView=itemView.findViewById(R.id.imgAlbum);
        }
    }
}
