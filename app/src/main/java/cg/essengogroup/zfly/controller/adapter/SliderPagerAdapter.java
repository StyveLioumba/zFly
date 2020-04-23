package cg.essengogroup.zfly.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.utils.Methodes;
import cg.essengogroup.zfly.model.Slider;

public class SliderPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<Slider> mList;

    public SliderPagerAdapter(Context mContext, List<Slider> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View sliderLayout = null;
        if (inflater != null) {
            sliderLayout = inflater.inflate(R.layout.item_slide,null);
        }

        ImageView slideImg=sliderLayout.findViewById(R.id.imgSlide);
        TextView slideText =sliderLayout.findViewById(R.id.txtDescription);

        Methodes.glideDownload(
                mContext,
                mList.get(position).getImage(),
                R.drawable.ic_launcher_background,
                slideImg
        );
        slideText.setText(mList.get(position).getDesignation());

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
