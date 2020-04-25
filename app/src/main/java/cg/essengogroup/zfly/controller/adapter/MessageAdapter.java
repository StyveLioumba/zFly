package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Message;

import static cg.essengogroup.zfly.controller.utils.Methodes.convertSecondsToHMm;
import static cg.essengogroup.zfly.controller.utils.Methodes.convertSecondsToHMmSs;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    public static final  int SMS_LEFT=0;
    public static final  int SMS_RIGHT=1;

    private String imageUrl;

    private Context context;
    private ArrayList<Message> messages;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    public MessageAdapter(Context context, ArrayList<Message> messages,String imageUrl) {
        this.context = context;
        this.messages = messages;
        this.imageUrl = imageUrl;
        mAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==SMS_RIGHT){
            View view=LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
            return new MyViewHolder(view);
        }else {
            View view=LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message=messages.get(position);

        holder.message.setText(message.getMessage());
        holder.txtH.setText(new SimpleDateFormat("HH:mm").format(Long.parseLong(message.getHeure())));

        if (position==messages.size()-1){
            if (message.isVu()){
                holder.txtVu.setText("vu");
            }else {
                holder.txtVu.setText("pas vu");
            }
        }else {
            holder.txtVu.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView message,txtVu,txtH;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            message=itemView.findViewById(R.id.message);
            txtVu=itemView.findViewById(R.id.txtVu);
            txtH=itemView.findViewById(R.id.txtH);
        }
    }

    @Override
    public int getItemViewType(int position) {
        mUser=mAuth.getCurrentUser();
        Message message=messages.get(position);

        if (message.getEnvoyeur().equals(mUser.getUid())){
            return SMS_RIGHT;
        }else {
            return SMS_LEFT;
        }
    }
}
