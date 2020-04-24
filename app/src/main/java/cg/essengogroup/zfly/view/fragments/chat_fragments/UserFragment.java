package cg.essengogroup.zfly.view.fragments.chat_fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.UtilisateurAdapter;
import cg.essengogroup.zfly.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private UtilisateurAdapter adapter;
    private ArrayList<User> userArrayList;

    private View root;
    private Context context;

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Query query;

    private SearchView searchView;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root=inflater.inflate(R.layout.fragment_user, container, false);
        context=getContext();

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("users");

        searchView=root.findViewById(R.id.search);
        recyclerView=root.findViewById(R.id.recycleUtilisateur);
        manager=new LinearLayoutManager(context);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        userArrayList=new ArrayList<>();
        getData();

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){
                    getData();
                }else {
                    searchUsers(newText);
                }
                return false;
            }
        });

        return root;
    }

    private void getData(){
        reference.orderByChild("isArtiste").equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    User user=new User();

                    user.setImage(String.valueOf(data.child("image").getValue()));
                    user.setUser_id(String.valueOf(data.child("user_id").getValue()));
                    user.setPseudo(String.valueOf(data.child("pseudo").getValue()));
                    user.setApseudo(String.valueOf(data.child("Apseudo").getValue()));
                    user.setStatus(String.valueOf(data.child("status").getValue()));

                    if (!user.getUser_id().equalsIgnoreCase(mUser.getUid())){
                        userArrayList.add(user);
                    }
                }

                adapter=new UtilisateurAdapter(context,userArrayList,false);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(String username){

        query=database.getReference().child("users").orderByChild("Apseudo").startAt(username).endAt(username+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    User user=new User();

                    user.setImage(String.valueOf(data.child("image").getValue()));
                    user.setUser_id(String.valueOf(data.child("user_id").getValue()));
                    user.setPseudo(String.valueOf(data.child("pseudo").getValue()));
                    user.setApseudo(String.valueOf(data.child("Apseudo").getValue()));
                    user.setStatus(String.valueOf(data.child("status").getValue()));

                    if (!user.getUser_id().equalsIgnoreCase(mUser.getUid())){
                        userArrayList.add(user);
                    }
                }

                adapter=new UtilisateurAdapter(context,userArrayList,false);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
