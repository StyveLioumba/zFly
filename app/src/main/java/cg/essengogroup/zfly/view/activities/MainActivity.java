package cg.essengogroup.zfly.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import cg.essengogroup.zfly.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private final int TIME_OUT=4000;
    private Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        handler.postDelayed(() -> {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        },TIME_OUT);
    }
}
