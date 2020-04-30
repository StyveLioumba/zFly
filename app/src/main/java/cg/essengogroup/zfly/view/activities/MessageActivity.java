package cg.essengogroup.zfly.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.MessageAdapter;
import cg.essengogroup.zfly.controller.utils.VolleySingleton;
import cg.essengogroup.zfly.model.Message;
import cg.essengogroup.zfly.model.User;

public class MessageActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView txtPseudo,txtApseudo;
    private ImageButton btnSend;
    private EditText editMessage;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private ArrayList<Message> messageArrayList;
    private MessageAdapter adapter;

    private Intent intent;
    private User user;

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference referenceSms,reference;

    private ValueEventListener vueListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }

        intent=getIntent();

        if (intent!=null){
            user=intent.getParcelableExtra("user");
        }


        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        referenceSms=database.getReference().child("chats");

        imageView=findViewById(R.id.imgUser);
        txtPseudo=findViewById(R.id.pseudoUser);
        txtApseudo=findViewById(R.id.Apseudo);
        recyclerView=findViewById(R.id.recycleSms);
        btnSend=findViewById(R.id.btnSend);
        editMessage=findViewById(R.id.editSms);

        messageArrayList=new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        manager=new LinearLayoutManager(MessageActivity.this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        txtPseudo.setText(user.getPseudo());
        txtApseudo.setText(user.getApseudo());

        Picasso.get()
                .load(user.getImage())
                .placeholder(R.drawable.imgdefault)
                .error(R.drawable.imgdefault)
                .into(imageView);

        btnSend.setOnClickListener(v->{
            sendMessage(mUser.getUid(),user.getUser_id());
        });

        reference = FirebaseDatabase.getInstance().getReference("users").child(user.getUser_id());
        reference.child("hasNewSMS").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

        readMessage(mUser.getUid(),user.getUser_id(),String.valueOf(user.getImage()));

        messageVu(user.getUser_id());

        status("enligne");
    }


    private void sendMessage(String idEnvoyeur,String idReceveur){

        String leMessage=editMessage.getText().toString().trim();

        if(TextUtils.isEmpty(leMessage)){
            editMessage.setError("entrez un message");
            editMessage.requestFocus();
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("envoyer",idEnvoyeur);
        data.put("recevoir",idReceveur);
        data.put("isVu",false);
        data.put("isNew",true);
        data.put("message",leMessage);
        data.put("heure",new Date().getTime());

        referenceSms.push().setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                editMessage.setText("");

                reference = FirebaseDatabase.getInstance().getReference("users").child(idReceveur);
                reference.child("hasNewSMS").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        editMessage.setHint(getResources().getString(R.string.r_diger_un_message));
                        sendNotification(user,"Nouveau message de "+mUser.getDisplayName(),leMessage);
                    }
                });
            }
        });


    }

    private void readMessage(String myId,String user_id,String imageUrl){
        referenceSms.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Message message=new Message();

                    message.setEnvoyeur(String.valueOf(data.child("envoyer").getValue()));
                    message.setReceveur(String.valueOf(data.child("recevoir").getValue()));
                    message.setMessage(String.valueOf(data.child("message").getValue()));
                    message.setHeure(String.valueOf(data.child("heure").getValue()));
                    message.setVu((Boolean) data.child("isVu").getValue());
                    message.setNew((Boolean) data.child("isNew").getValue());

                    if (message.getReceveur().equalsIgnoreCase(myId) && message.getEnvoyeur().equalsIgnoreCase(user_id) ||
                    message.getReceveur().equalsIgnoreCase(user_id) && message.getEnvoyeur().equalsIgnoreCase(myId)){
                        messageArrayList.add(message);
                    }
                }

                adapter=new MessageAdapter(MessageActivity.this,messageArrayList,imageUrl);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void messageVu(String user_id){
        vueListener=referenceSms.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Message message=new Message();

                    message.setEnvoyeur(String.valueOf(data.child("envoyer").getValue()));
                    message.setReceveur(String.valueOf(data.child("recevoir").getValue()));
                    message.setMessage(String.valueOf(data.child("message").getValue()));
                    message.setVu((Boolean) data.child("isVu").getValue());

                    if (message.getReceveur().equalsIgnoreCase(mUser.getUid()) && message.getEnvoyeur().equalsIgnoreCase(user_id)){
                        data.getRef().child("isVu").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                data.getRef().child("isNew").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        referenceSms.removeEventListener(vueListener);
    }

    private void status(String status){
        DatabaseReference referenceStatus=database.getReference().child("users").child(mUser.getUid());
        referenceStatus.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("enligne");
    }

    private  void sendNotification(User user,String title,String body){
        String leLien="http://att.bgrfacile.com/ApiZfly/index.php";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, leLien,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("TAG", "onResponse: "+response );
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("send","send");
                param.put("token",user.getToken());
                param.put("title",title);
                param.put("msg",body);
                return param;
            }
        };
        VolleySingleton.getInstance(MessageActivity.this).addToRequestQueue(stringRequest);
    }

}
