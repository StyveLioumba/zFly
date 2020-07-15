package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;

public class TopTenAdapter extends RecyclerView.Adapter<TopTenAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<String> musicArrayList;

    public TopTenAdapter(Context context, ArrayList<String> musicArrayList) {
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

        holder.txtGenre.setText(musicArrayList.get(position));
        holder.txtNum.setText(String.valueOf(position+1));

    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView txtGenre,txtNum;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout=itemView.findViewById(R.id.lineGenre);
            txtGenre=itemView.findViewById(R.id.txtTitre);
            txtNum=itemView.findViewById(R.id.txtNum);
        }
    }
}
