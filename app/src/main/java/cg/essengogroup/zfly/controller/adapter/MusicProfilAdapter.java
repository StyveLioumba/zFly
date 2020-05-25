package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Music;
import cg.essengogroup.zfly.view.dialogs.DialogMusicAccueil;

public class MusicProfilAdapter extends RecyclerView.Adapter<MusicProfilAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Music> musicArrayList;
    private DialogMusicAccueil dialog;

    public MusicProfilAdapter(Context context, ArrayList<Music> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_music_pro,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txtArtiste.setText(musicArrayList.get(position).getArtiste());
        holder.txtMorceau.setText(musicArrayList.get(position).getMorceau());

        holder.cardView.setOnClickListener(v->{
            Toast.makeText(context, "chargement...", Toast.LENGTH_LONG).show();
            dialog=new DialogMusicAccueil(context,musicArrayList.get(position).getChanson());
            dialog.setCancelable(true);
            dialog.show();
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        });


    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView txtMorceau,txtArtiste;
        ImageView fab;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.cardM);
            txtMorceau=itemView.findViewById(R.id.txtTitre);
            txtArtiste=itemView.findViewById(R.id.txtArtiste);
            fab=itemView.findViewById(R.id.fab);
        }
    }
}
