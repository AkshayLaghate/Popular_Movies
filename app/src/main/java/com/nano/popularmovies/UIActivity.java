package com.nano.popularmovies;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Akki on 15/07/15.
 */
public class UIActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.main_ui);

        FragmentManager MGR = getFragmentManager();
        MGR.beginTransaction().add(new GridFragment(), null).commit();
    }
}
