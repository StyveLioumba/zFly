package cg.essengogroup.zfly.view.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.viewpager.widget.ViewPager;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.SectionPagerAdapter;
import cg.essengogroup.zfly.view.activities.PostMusicActivity;
import cg.essengogroup.zfly.view.activities.PostPlaceActivity;
import cg.essengogroup.zfly.view.fragments.music_fragments.AlbumFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.ArtistesFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.ChansonsFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.GenreFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.TopFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment {

    private View root;
    private Context context;

    public MusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root= inflater.inflate(R.layout.fragment_music, container, false);
        context=getContext();

        FloatingActionButton fabMusic=root.findViewById(R.id.fab_);
        fabMusic.setOnClickListener(v->startActivity(new Intent(context, PostMusicActivity.class)));

        DatabaseReference isArtisteRef = FirebaseDatabase.getInstance().getReference("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        isArtisteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!((Boolean) dataSnapshot.child("isArtiste").getValue())){
                    fabMusic.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        setupViewPager();
        return root;
    }


    private void setupViewPager(){
        SectionPagerAdapter adapter=new SectionPagerAdapter(getChildFragmentManager());

        adapter.addFragment(new ChansonsFragment());
        adapter.addFragment(new ArtistesFragment());
//        adapter.addFragment(new AlbumFragment());
        adapter.addFragment(new GenreFragment());
        adapter.addFragment(new TopFragment());

        ViewPager viewPager=root.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout=root.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorToolbar));
        tabLayout.setTabTextColors(getResources().getColor(android.R.color.black),getResources().getColor(R.color.colorBlanc));

        tabLayout.getTabAt(0).setText("Son");
        tabLayout.getTabAt(1).setText("Artistes");
//        tabLayout.getTabAt(2).setText("Albums");
        tabLayout.getTabAt(2).setText("Genres");
        tabLayout.getTabAt(3).setText("Top");

    }
}
