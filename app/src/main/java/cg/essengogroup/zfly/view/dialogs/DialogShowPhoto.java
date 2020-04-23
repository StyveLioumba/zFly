package cg.essengogroup.zfly.view.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.adapter.ShowPhotoAdapter;
import cg.essengogroup.zfly.controller.adapter.SliderPagerPlaceAdapter;
import cg.essengogroup.zfly.model.Gallerie;

public class DialogShowPhoto extends Dialog {

    private Context context;

    private ArrayList<Gallerie> gallerieArrayList;

    private ViewPager viewPager;
    private ShowPhotoAdapter slideAdapter;
    private RecyclerView recyclerView;
    private MyAdapter adapter;

    private int position_;

    public DialogShowPhoto(@NonNull Context context,ArrayList<Gallerie> gallerieArrayList,int position_) {
        super(context,R.style.full_screen_dialog);
        this.context=context;
        this.position_=position_;
        this.gallerieArrayList=gallerieArrayList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_show_photo);

        viewPager=findViewById(R.id.viewpager);

        recyclerView=findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false));
        adapter=new MyAdapter(context,gallerieArrayList);
        recyclerView.setAdapter(adapter);

        adapter.setOnClickItem(new MyAdapter.onClickItem() {
            @Override
            public void onGalerieClicked(int position) {
                position_=position;
                viewPager.setCurrentItem(position_);
            }
        });

        findViewById(R.id.btnClose).setOnClickListener(v->{
            if(isShowing()) dismiss();
        });

        slideAdapter=new ShowPhotoAdapter(context,gallerieArrayList);
        viewPager.setAdapter(slideAdapter);

        viewPager.setCurrentItem(position_);
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        Context context;
        ArrayList<Gallerie> galleries;
        onClickItem onClickItem;

        public MyAdapter(Context context, ArrayList<Gallerie> galleries) {
            this.context = context;
            this.galleries = galleries;
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_mini_gallerie, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
            Glide.with(context)
                    .load(galleries.get(position).getImage())
                    .placeholder(R.drawable.default_img)
                    .into(holder.imageUser);

            holder.cardView.setOnClickListener(v->onClickItem.onGalerieClicked(position));

        }

        @Override
        public int getItemCount() {
            return galleries.size();
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            ImageView imageUser;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView=itemView.findViewById(R.id.cardG);
                imageUser=itemView.findViewById(R.id.imgUser);
            }
        }

        public MyAdapter.onClickItem getOnClickItem() {
            return onClickItem;
        }

        public void setOnClickItem(MyAdapter.onClickItem onClickItem) {
            this.onClickItem = onClickItem;
        }

        public interface onClickItem{
            void onGalerieClicked(int position);
        }
    }

}
