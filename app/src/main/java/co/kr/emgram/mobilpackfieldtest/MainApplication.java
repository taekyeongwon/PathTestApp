package co.kr.emgram.mobilpackfieldtest;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

public class MainApplication extends MultiDexApplication {
    public static MainApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        TMapManager.getInstance().init(this);
    }
}
