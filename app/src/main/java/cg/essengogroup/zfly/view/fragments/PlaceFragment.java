package cg.essengogroup.zfly.view.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.PlaceAdapter;
import cg.essengogroup.zfly.model.Place;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaceFragment extends Fragment {

    private RecyclerView recyclerView;
    private View root;
    private Context context;
    private ArrayList<Place> placeArrayList;
    private PlaceAdapter adapter;

    private Query referencePlace;
    private FirebaseDatabase database;

    private Spinner spinner;
    private ArrayAdapter adapterSpinner;
    private ArrayList<String> filtreList;

    private String filtreValue;
    private LinearLayoutManager manager;


    private SearchView searchView;
    private LinearLayout lineFiltre;

    public PlaceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root= inflater.inflate(R.layout.fragment_place, container, false);

        context=getContext();
        database=FirebaseDatabase.getInstance();
        referencePlace=database.getReference("place");

        referencePlace.keepSynced(true);

        searchView=root.findViewById(R.id.search);
        lineFiltre=root.findViewById(R.id.lineFiltre);
        recyclerView=root.findViewById(R.id.recyclePlace);
        spinner=root.findViewById(R.id.spinnerFiltre);
        filtreList=new ArrayList<>();

        placeArrayList=new ArrayList<>();
        manager=new LinearLayoutManager(context);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        getDataType();

        getDataSansFiltre();

        root.findViewById(R.id.btnFiltrer).setOnClickListener(v->{
            if (filtreList.size()>0){
                filtreValue=spinner.getSelectedItem().toString().trim();
                getDataAvecFiltre(filtreValue);
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lineFiltre.getVisibility()==View.VISIBLE){
                    lineFiltre.setVisibility(View.GONE);
                }
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (lineFiltre.getVisibility()==View.GONE){
                    lineFiltre.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (lineFiltre.getVisibility()==View.GONE){
                    lineFiltre.setVisibility(View.VISIBLE);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){

                }else {

                }
                return false;
            }
        });

        return root;
    }

    private void getDataSansFiltre(){

        referencePlace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                placeArrayList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Place place=new Place();

                    place.setAdresse(data.child("adresse").getValue().toString());
                    place.setCreateAt(data.child("createAt").getValue().toString());
                    place.setDescription(data.child("description").getValue().toString());
                    place.setImage_couverture(data.child("image_couverture").getValue().toString());
                    place.setNom(data.child("nom").getValue().toString());
                    place.setNumero(data.child("numero").getValue().toString());
                    place.setType(data.child("type").getValue().toString());
                    place.setUser_id(data.child("user_id").getValue().toString());
                    place.setPlace_id(data.getKey());
                    placeArrayList.add(place);
                }

                adapter=new PlaceAdapter(context,placeArrayList);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataAvecFiltre(String filtre){

        if (!TextUtils.isEmpty(filtre)){
         if (filtre.equalsIgnoreCase("tout afficher")){
             getDataSansFiltre();
         }else {
             referencePlace.orderByChild("type").equalTo(filtre).addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     placeArrayList.clear();
                     for (DataSnapshot data : dataSnapshot.getChildren()){
                         Place place=new Place();

                         place.setAdresse(data.child("adresse").getValue().toString());
                         place.setCreateAt(data.child("createAt").getValue().toString());
                         place.setDescription(data.child("description").getValue().toString());
                         place.setImage_couverture(data.child("image_couverture").getValue().toString());
                         place.setNom(data.child("nom").getValue().toString());
                         place.setNumero(data.child("numero").getValue().toString());
                         place.setType(data.child("type").getValue().toString());
                         place.setUser_id(data.child("user_id").getValue().toString());
                         place.setPlace_id(data.getKey());
                         placeArrayList.add(place);
                     }

                     adapter=new PlaceAdapter(context,placeArrayList);
                     recyclerView.setAdapter(adapter);
                 }
                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {

                 }
             });
         }
        }

    }

    private void getDataType(){
        referencePlace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                filtreList.clear();
                filtreList.add("tout afficher");
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Place place=new Place();
                    place.setType(data.child("type").getValue().toString());
                    if (!filtreList.contains(place.getType())){
                        filtreList.add(data.child("type").getValue().toString());
                    }
                }

                adapterSpinner=new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, android.R.id.text1,filtreList);
                spinner.setAdapter(adapterSpinner);
                adapterSpinner.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
