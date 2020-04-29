package cg.essengogroup.zfly.view.activities;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;


public class Zfly extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
