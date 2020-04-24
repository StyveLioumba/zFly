package cg.essengogroup.zfly.view.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.MultiViewTypeAdapter;
import cg.essengogroup.zfly.controller.adapter.SliderPagerAdapter;
import cg.essengogroup.zfly.controller.adapter.UserAdapter;
import cg.essengogroup.zfly.model.Model;
import cg.essengogroup.zfly.model.Slider;
import cg.essengogroup.zfly.model.User;
import cg.essengogroup.zfly.view.activities.AllUserActivity;
import cg.essengogroup.zfly.view.activities.PostActivity;
import cg.essengogroup.zfly.view.activities.PostMusicActivity;
import cg.essengogroup.zfly.view.activities.PostPlaceActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccueilFragment extends Fragment {
    private View root;
    private Context context;
    private Activity activity;

    private RecyclerView recyclerView,recycleH;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;
    private ViewPager viewPager;
    private NestedScrollView nestedScrollView;

    private FirebaseDatabase database;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private UserAdapter userAdapter;
    private ArrayList<User> userArrayList;

    private MultiViewTypeAdapter adapter;
    private ArrayList<Model> modelArrayList;

    private SliderPagerAdapter sliderPagerAdapter;
    private ArrayList<Slider> sliderArrayList;

    private Query referenceUser,referenceModel,referenceSlider;
    private LinearLayoutManager manager;


    public AccueilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root=inflater.inflate(R.layout.fragment_accueil, container, false);
        context=getContext();
        activity=getActivity();

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        referenceUser=database.getReference("users").orderByChild("isArtiste").equalTo(true).limitToLast(50);
        referenceModel=database.getReference("post");

        referenceUser.keepSynced(true);
        referenceModel.keepSynced(true);

        userArrayList=new ArrayList<>();
        modelArrayList=new ArrayList<>();
        sliderArrayList=new ArrayList<>();

        FloatingActionButton fab = root.findViewById(R.id.fab1);
        recyclerView=root.findViewById(R.id.recycleActualite);
        recycleH=root.findViewById(R.id.recycleArtiste);
        progressBar=root.findViewById(R.id.progress);
        linearLayout=root.findViewById(R.id.lineBottom);
        viewPager=root.findViewById(R.id.viewpager);
        nestedScrollView=root.findViewById(R.id.nestedscrol);

        fab.setOnClickListener(v -> {
            if (linearLayout.getVisibility()== View.GONE){
                linearLayout.setVisibility(View.VISIBLE);
                fab.setAnimation(AnimationUtils.loadAnimation(context,R.anim.rotate_clockwise));
            }else {
                linearLayout.setVisibility(View.GONE);
                fab.setAnimation(AnimationUtils.loadAnimation(context,R.anim.rotate_anticlockwise));
            }
        });

        manager=new LinearLayoutManager(context);


        FloatingActionButton fabMusic=root.findViewById(R.id.fab_);
        fabMusic.setOnClickListener(v->startActivity(new Intent(context, PostMusicActivity.class)));

        FloatingActionButton fabPlace=root.findViewById(R.id.fab_place);
        fabPlace.setOnClickListener(v->startActivity(new Intent(context, PostPlaceActivity.class)));

        root.findViewById(R.id.fab).setOnClickListener(v->startActivity(new Intent(context, PostActivity.class)));

        root.findViewById(R.id.seeMore).setOnClickListener(v->startActivity(new Intent(context, AllUserActivity.class)));

        DatabaseReference isArtisteRef = database.getReference("users/"+mUser.getUid());
        isArtisteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!((Boolean) dataSnapshot.child("isArtiste").getValue())){
                    fabMusic.setVisibility(View.GONE);
                }

                if (dataSnapshot.child("isPlace").exists()){
                    if ((Boolean) dataSnapshot.child("isPlace").getValue()){
                        fabPlace.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Timer timer= new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);

        getDataUser();
        getDataActualite();
        getDataSlider();

        return root;
    }

    private void getDataUser(){

        referenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArrayList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    User user=data.getValue(User.class);
                    if (!user.getUser_id().equalsIgnoreCase(mUser.getUid())){
                        userArrayList.add(user);
                    }
                }

                userAdapter=new UserAdapter(context,userArrayList);
                LinearLayoutManager managerH=new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false);
                managerH.setReverseLayout(true);
                managerH.setStackFromEnd(true);
                recycleH.setLayoutManager(managerH);
                recycleH.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataActualite(){

        referenceModel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                modelArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Model model=new Model() ;
                    model.setUser_id(""+data.child("user_id").getValue());
                    model.setImage(""+data.child("imagePost").getValue());
                    model.setAudio(""+data.child("sonPost").getValue());
                    model.setDescription(""+data.child("description").getValue());
                    model.setType(Integer.parseInt(""+data.child("type").getValue()));
                    model.setCreateAt(String.valueOf(data.child("createAt").getValue()));
                    model.setPost_id(data.getKey());
                    modelArrayList.add(model);
                }

                adapter=new MultiViewTypeAdapter(modelArrayList,context);
                recyclerView.setLayoutManager(manager);
                manager.setReverseLayout(true);
                manager.setStackFromEnd(true);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataSlider(){
        referenceModel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sliderArrayList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Slider slider=new Slider() ;

                    slider.setImage(""+data.child("imagePost").getValue());
                    slider.setDesignation(""+data.child("description").getValue());

                    sliderArrayList.add(slider);
                }

                sliderPagerAdapter=new SliderPagerAdapter(context,sliderArrayList);
                viewPager.setAdapter(sliderPagerAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class SliderTimer extends TimerTask {

        @Override
        public void run() {
            activity.runOnUiThread(() -> {
                if(viewPager.getCurrentItem()< sliderArrayList.size()-1){
                    viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
                }else {
                    viewPager.setCurrentItem(0);
                }
            });
        }
    }

}
