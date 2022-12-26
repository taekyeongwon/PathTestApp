package co.kr.emgram.mobilpackfieldtest;

import android.content.Context;
import android.util.Log;

import com.skt.Tmap.TMapTapi;

import java.util.Timer;
import java.util.TimerTask;

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
    interface OnTest {
        void testing();
    }
    static class Nested {
        OnTest t;
    }
    private Nested testInstance = null;
    private OnTest testInterface = null;
    public void setTest(Nested test) {
        testInstance = test;
    }
    public void setTestInterface(OnTest test) {
        testInterface = test;
    }
    public void tasking() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(testInstance.t != null) {
                    testInstance.t.testing();
                } else {
                    timer.cancel();
                }
            }
        },0, 1000);
    }
    public void tasking2() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(testInterface != null) {
                    testInterface.testing();
                } else {
                    timer.cancel();
                }
            }
        },0, 1000);
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
