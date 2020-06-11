package co.kr.emgram.mobilpackfieldtest;

import android.content.Context;

import com.skt.Tmap.TMapTapi;

public class TMapManager {
    private static TMapManager instance;
    private TMapTapi tmaptapi;

    private boolean isSucceeded = false;

    public static TMapManager getInstance() {
        if(instance == null) {
            instance = new TMapManager();
        }

        return instance;
    }

    public void init(Context context) {
        tmaptapi = new TMapTapi(context);
        tmaptapi.setSKTMapAuthentication("l7xx6d8f2e79b3804d79bc58a23113e3d0e2");

        tmaptapi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                isSucceeded = true;
            }

            @Override
            public void SKTMapApikeyFailed(String s) {
                isSucceeded = false;
            }
        });
    }

    public boolean getSucceeded() {
        return isSucceeded;
    }
}
