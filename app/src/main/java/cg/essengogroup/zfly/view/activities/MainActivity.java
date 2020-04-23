package cg.essengogroup.zfly.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import cg.essengogroup.zfly.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private final int TIME_OUT=4000;
    private Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler.postDelayed(() -> {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        },TIME_OUT);
    }
}
