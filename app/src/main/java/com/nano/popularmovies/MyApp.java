package com.nano.popularmovies;

import android.app.Application;

import com.nano.popularmovies.Utils.FontsOverride;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Akki on 12/07/15.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        FontsOverride.setDefaultFont(this, "SERIF", "noto_reg.ttf");
    }
}
