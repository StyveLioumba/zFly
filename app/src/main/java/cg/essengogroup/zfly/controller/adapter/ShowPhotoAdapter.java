package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.model.Gallerie;

public class ShowPhotoAdapter extends PagerAdapter {

    private Context mContext;
    private List<Gallerie> mList;

    public ShowPhotoAdapter(Context mContext, List<Gallerie> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View sliderLayout = null;
        if (inflater != null) {
            sliderLayout = inflater.inflate(R.layout.item_img_galerie_showing_place,null);
        }

        ImageView slideImg=sliderLayout.findViewById(R.id.imgPlace);


        Picasso.get()
                .load(mList.get(position).getImage())
                .placeholder(R.drawable.default_img)
                .error(R.drawable.default_img)
                .into(slideImg);

        container.addView(sliderLayout);
        return sliderLayout;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view ==o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
