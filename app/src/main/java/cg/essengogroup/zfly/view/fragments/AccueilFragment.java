package cg.essengogroup.zfly.view.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.MultiViewTypeAdapter;
import cg.essengogroup.zfly.controller.adapter.SliderPagerAdapter;
import cg.essengogroup.zfly.controller.adapter.UserAdapter;
import cg.essengogroup.zfly.controller.utils.Methodes;
import cg.essengogroup.zfly.controller.utils.VolleySingleton;
import cg.essengogroup.zfly.model.Model;
import cg.essengogroup.zfly.model.Slider;
import cg.essengogroup.zfly.model.User;
import cg.essengogroup.zfly.view.activities.AllUserActivity;
import cg.essengogroup.zfly.view.activities.PostActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccueilFragment extends Fragment {
    private View root;
    private Context context;
    private Activity activity;

    private RecyclerView recyclerView, recycleH;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;
    private ViewPager viewPager;
    private NestedScrollView nestedScrollView;

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private UserAdapter userAdapter;
    private ArrayList<User> userArrayList;

    private MultiViewTypeAdapter adapter;
    private ArrayList<Model> modelArrayList;

    private SliderPagerAdapter sliderPagerAdapter;
    private ArrayList<Slider> sliderArrayList;

    private LinearLayoutManager manager;

    public AccueilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_accueil, container, false);
        context = getContext();
        activity = getActivity();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        userArrayList = new ArrayList<>();
        modelArrayList = new ArrayList<>();
        sliderArrayList = new ArrayList<>();

        recyclerView = root.findViewById(R.id.recycleActualite);
        recycleH = root.findViewById(R.id.recycleArtiste);
        progressBar = root.findViewById(R.id.progress);
        linearLayout = root.findViewById(R.id.lineBottom);
        viewPager = root.findViewById(R.id.viewpager);
        nestedScrollView = root.findViewById(R.id.nestedscrol);

        manager = new LinearLayoutManager(context);
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);

        root.findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(context, PostActivity.class)));

        root.findViewById(R.id.seeMore).setOnClickListener(v -> startActivity(new Intent(context, AllUserActivity.class)));


        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);

        getDataUser();
        getDataActualite();
        getDataSlider();

        return root;
    }

    private void getDataUser() {
        StringRequest stringRequest = new StringRequest(Request.Method.DEPRECATED_GET_OR_POST, "le lien ici",
                response -> {
                    if (!TextUtils.isEmpty(response)) {
                        userArrayList.clear();
                        /***
                         *  le parsing de data se fera ici
                         ***/

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject object = jsonArray.getJSONObject(i);

                                User user = new Gson().fromJson(String.valueOf(object), User.class);

                                if (!user.getUser_id().equalsIgnoreCase(mUser.getUid())) {
                                    userArrayList.add(user);
                                }
                            }

                            userAdapter = new UserAdapter(context, userArrayList);
                            LinearLayoutManager managerH = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);
                            managerH.setReverseLayout(true);
                            managerH.setStackFromEnd(true);
                            recycleH.setLayoutManager(managerH);
                            recycleH.setAdapter(userAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    Methodes.volleyError(context, error);
                });

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    private void getDataActualite() {

        /*referenceModel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                modelArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Model model = new Model();
                    model.setUser_id("" + data.child("user_id").getValue());
                    model.setImage("" + data.child("imagePost").getValue());
                    model.setAudio("" + data.child("sonPost").getValue());
                    model.setDescription("" + data.child("description").getValue());
                    model.setType(Integer.parseInt("" + data.child("type").getValue()));
                    model.setCreateAt(String.valueOf(data.child("createAt").getValue()));
                    model.setPost_id(data.getKey());
                    modelArrayList.add(model);
                }

                adapter = new MultiViewTypeAdapter(modelArrayList, context);
                recyclerView.setLayoutManager(manager);
                recyclerView.setNestedScrollingEnabled(false);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        StringRequest stringRequest = new StringRequest(Request.Method.DEPRECATED_GET_OR_POST, "le lien ici",
                response -> {
                    if (!TextUtils.isEmpty(response)) {
                        /***
                         * le parsing se fait ici
                         **/

                        modelArrayList.clear();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                Model model = new Gson().fromJson(String.valueOf(object), Model.class);
                                modelArrayList.add(model);
                            }

                            adapter = new MultiViewTypeAdapter(modelArrayList, context);
                            recyclerView.setLayoutManager(manager);
                            recyclerView.setNestedScrollingEnabled(false);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setAdapter(adapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, error -> {
            Methodes.volleyError(context, error);
        });

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    private void getDataSlider() {
        /*referenceSlider.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sliderArrayList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Slider slider = new Slider();

                    slider.setImage("" + data.child("imageURL").getValue());
                    slider.setDesignation("" + data.child("text").getValue());

                    sliderArrayList.add(slider);
                }

                sliderPagerAdapter = new SliderPagerAdapter(context, sliderArrayList);
                viewPager.setAdapter(sliderPagerAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        StringRequest stringRequest=new StringRequest(Request.Method.DEPRECATED_GET_OR_POST,"le lien ici",
                response -> {
                    if (TextUtils.isEmpty(response)){
                        /***
                         * le parsing se fait ici
                         ***/
                        sliderArrayList.clear();
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            JSONArray jsonArray=jsonObject.getJSONArray("data");

                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject object=jsonArray.getJSONObject(i);
                                Slider slider=new Gson().fromJson(String.valueOf(object),Slider.class);

                                sliderArrayList.add(slider);
                            }

                            sliderPagerAdapter = new SliderPagerAdapter(context, sliderArrayList);
                            viewPager.setAdapter(sliderPagerAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    Methodes.volleyError(context,error);
                });

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    class SliderTimer extends TimerTask {
        @Override
        public void run() {
            activity.runOnUiThread(() -> {
                if (viewPager.getCurrentItem() < sliderArrayList.size() - 1) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                } else {
                    viewPager.setCurrentItem(0);
                }
            });
        }
    }

}
