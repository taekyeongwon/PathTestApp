package co.kr.emgram.mobilpackfieldtest;

import android.app.Application;

public class MainApplication extends Application {
    public static MainApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        TMapManager.getInstance().init(this);
    }
}
