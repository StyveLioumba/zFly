package cg.essengogroup.zfly.view.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.controller.utils.Methodes;
import cg.essengogroup.zfly.model.Slider;
import cg.essengogroup.zfly.view.activities.DetailActivity;

public class DialogShowSlide extends Dialog {
    private Context context;

    private ImageView imageView;
    private TextView txtDescription;

    private Slider slider;

    public DialogShowSlide(@NonNull Context context,Slider slider ) {
        super(context, R.style.full_screen_dialog);
        this.context=context;
        this.slider=slider;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_slider);

        imageView=findViewById(R.id.imgSelected);
        txtDescription=findViewById(R.id.txtDescription);

        Methodes.glideDownload(
                context,
                slider.getImage(),
                R.drawable.default_img,
                imageView
        );
        if (TextUtils.isEmpty(slider.getDescription())){
            txtDescription.setText(slider.getDesignation());
        }else {
            if (TextUtils.isEmpty(slider.getDesignation())){
                txtDescription.setVisibility(View.GONE);
            }else {
                txtDescription.setText(slider.getDesignation()+"\n\n"+slider.getDescription());
            }
        }


        findViewById(R.id.btnClose).setOnClickListener(v->{
            if(isShowing()) dismiss();
        });
    }
}
