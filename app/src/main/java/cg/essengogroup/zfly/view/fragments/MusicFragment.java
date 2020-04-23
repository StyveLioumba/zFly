package cg.essengogroup.zfly.view.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.SectionPagerAdapter;
import cg.essengogroup.zfly.view.fragments.music_fragments.AlbumFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.ArtistesFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.ChansonsFragment;
import cg.essengogroup.zfly.view.fragments.music_fragments.GenreFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment {

    private View root;

    public MusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root= inflater.inflate(R.layout.fragment_music, container, false);

        setupViewPager();
        return root;
    }


    private void setupViewPager(){
        SectionPagerAdapter adapter=new SectionPagerAdapter(getChildFragmentManager());

        adapter.addFragment(new ChansonsFragment());
        adapter.addFragment(new ArtistesFragment());
        adapter.addFragment(new AlbumFragment());
        adapter.addFragment(new GenreFragment());

        ViewPager viewPager=root.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout=root.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorToolbar));
        tabLayout.setTabTextColors(getResources().getColor(android.R.color.black),getResources().getColor(R.color.colorBlanc));

        tabLayout.getTabAt(0).setText("Morceaux");
        tabLayout.getTabAt(1).setText("Artistes");
        tabLayout.getTabAt(2).setText("Albums");
        tabLayout.getTabAt(3).setText("Genres");

    }
}
