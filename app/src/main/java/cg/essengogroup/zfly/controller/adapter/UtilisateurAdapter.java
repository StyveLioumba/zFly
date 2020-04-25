package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Message;
import cg.essengogroup.zfly.model.User;
import cg.essengogroup.zfly.view.activities.MessageActivity;

public class UtilisateurAdapter extends RecyclerView.Adapter<UtilisateurAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<User> users;

    private  String lastSMS,heureSMS;
    private boolean isOnlinge;

    public UtilisateurAdapter(Context context, ArrayList<User> users,boolean isOnlinge) {
        this.context = context;
        this.users = users;
        this.isOnlinge = isOnlinge;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.item_utilisateur, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user=users.get(position);

        holder.txtPseudo.setText(user.getPseudo());

        Picasso.get()
                .load(user.getImage())
                .placeholder(R.drawable.imgdefault)
                .error(R.drawable.imgdefault)
                .into(holder.imageView);

        holder.relativeLayout.setOnClickListener(v->context.startActivity(new Intent(context, MessageActivity.class).putExtra("user",user)));

        if (isOnlinge){
            if (user.getStatus().equalsIgnoreCase("enligne")){
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            }else {
                holder.imgOn.setVisibility(View.GONE);
                holder.imgOff.setVisibility(View.VISIBLE);
            }
        }

        if (isOnlinge){
            holder.lastMessage(user.getUser_id(), holder.dernierSms,holder.txtH);
            holder.txtApseudo.setVisibility(View.GONE);
        }else {
            holder.txtApseudo.setText(user.getApseudo());
            holder.dernierSms.setVisibility(View.GONE);
        }

        if (holder.dernierSms.getVisibility()==View.GONE){
            holder.txtH.setVisibility(View.GONE);
        }else {
            holder.txtH.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView,imgOn,imgOff;
        TextView txtPseudo,txtApseudo,dernierSms,txtH;
        RelativeLayout relativeLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imgUser);
            imgOn=itemView.findViewById(R.id.imgOn);
            imgOff=itemView.findViewById(R.id.imgoff);
            txtPseudo=itemView.findViewById(R.id.pseudoUser);
            txtApseudo=itemView.findViewById(R.id.Apseudo_);
            txtH=itemView.findViewById(R.id.txtH);
            dernierSms=itemView.findViewById(R.id.Apseudo);
            relativeLayout=itemView.findViewById(R.id.relaUser);
        }


        private void lastMessage(String user_id,TextView last_msg,TextView heureSms){
            lastSMS="defaut";

            FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("chats");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        Message message=new Message();

                        message.setEnvoyeur(String.valueOf(data.child("envoyer").getValue()));
                        message.setReceveur(String.valueOf(data.child("recevoir").getValue()));
                        message.setMessage(String.valueOf(data.child("message").getValue()));
                        message.setHeure(String.valueOf(data.child("heure").getValue()));
                        message.setNew((Boolean) data.child("isNew").getValue());
                        message.setVu((Boolean) data.child("isVu").getValue());

                        if (message.getReceveur().equalsIgnoreCase(firebaseUser.getUid()) && message.getEnvoyeur().equalsIgnoreCase(user_id) ||
                                message.getReceveur().equalsIgnoreCase(user_id) && message.getEnvoyeur().equalsIgnoreCase(firebaseUser.getUid())){
                            lastSMS=message.getMessage();
                            heureSMS=message.getHeure();
                        }

                        if (message.getReceveur().equalsIgnoreCase(firebaseUser.getUid()) && message.getEnvoyeur().equalsIgnoreCase(user_id)   ){
                            if (message.isNew() && !message.isVu()){
                                relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.colorbaskSMS));
                            }
                        }

                    }

                    switch (lastSMS){
                        case "defaut":
                            last_msg.setText("pas de message");
                            break;
                        default:
                            last_msg.setText(lastSMS);
                            heureSms.setText(new SimpleDateFormat("HH:mm").format(Long.parseLong(heureSMS)));
                    }

                    lastSMS="defaut";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
