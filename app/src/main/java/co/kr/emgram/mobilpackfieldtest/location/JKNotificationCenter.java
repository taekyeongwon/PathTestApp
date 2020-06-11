
/*
 * Copyright (c) 2014. Jin Kook Seok & RedstoneSolution. All Rights Reserved.
 */

package co.kr.emgram.mobilpackfieldtest.location;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by jinkookseok on 2014. 9. 16..
 */
public class JKNotificationCenter {

    private static JKNotificationCenter aInstance;
    private HashMap<Integer, ArrayList<WeakReference<JKNotificationObserver>>> mObserversMap = new HashMap<Integer, ArrayList<WeakReference<JKNotificationObserver>>>();

    private JKNotificationCenter() {
    }

    public static JKNotificationCenter getInstance() {
        if (aInstance == null) {
            aInstance = new JKNotificationCenter();
        }
        return aInstance;
    }

    public void addObserver(int notificationCode, JKNotificationObserver observer) {
        ArrayList<WeakReference<JKNotificationObserver>> observerArray = mObserversMap.get(notificationCode);

        if (observerArray == null) {
            observerArray = new ArrayList<WeakReference<JKNotificationObserver>>();
            mObserversMap.put(notificationCode, observerArray);
        } else {
            cleanObserverArray(observerArray, observer);
        }

        observerArray.add(new WeakReference<JKNotificationObserver>(observer));
    }

    public void removeAllObserver() {
        mObserversMap.clear();
    }

    public void removeObserver(JKNotificationObserver observer) {

        ArrayList<Integer> removes = new ArrayList<Integer>();

        Iterator<Integer> keyItr = mObserversMap.keySet().iterator();
        while (keyItr.hasNext()) {
            int notificationCode = keyItr.next();
            ArrayList<WeakReference<JKNotificationObserver>> observerArray = mObserversMap.get(notificationCode);
            for (int i=0; i<observerArray.size(); i++) {
                WeakReference<JKNotificationObserver> ref = observerArray.get(i);
                if (ref.get() == null || ref.get() == observer) {
                    observerArray.remove(i--);
                }
            }
            if (observerArray.size() == 0) {
                removes.add(notificationCode);
            }
        }

        for (Integer obj : removes) {
            mObserversMap.remove(obj);
        }
    }

    public void removeObserver(int notificationCode) {
        mObserversMap.remove(notificationCode);
    }

    public void sendNotification(int notificationCode, Object sender, HashMap<String, Object> notificationData) {

        ArrayList<WeakReference<JKNotificationObserver>> observerArray = mObserversMap.get(notificationCode);

        if (observerArray != null) {

            boolean cleanFlag = false;
            for (int i=0; i<observerArray.size(); i++) {
                WeakReference<JKNotificationObserver> element = observerArray.get(i);
                JKNotificationObserver observer = element.get();
                if (observer != null) {
                    observer.onNotification(notificationCode, sender, notificationData);
                } else {
                    cleanFlag = true;
                }
            }

            if (cleanFlag) {
                cleanObserverArray(observerArray, null);
                if (observerArray.size() == 0) {
                    mObserversMap.remove(notificationCode);
                }
            }
        }
    }

    private void cleanObserverArray(ArrayList<WeakReference<JKNotificationObserver>> observerArray, JKNotificationObserver duplicationCheck) {
        for (int i=0; i<observerArray.size(); i++) {
            WeakReference<JKNotificationObserver> element = observerArray.get(i);
            JKNotificationObserver src = element.get();
            if (src == null || src == duplicationCheck) {
                observerArray.remove(i--);
            }
        }
    }

    public interface JKNotificationObserver {
        public void onNotification(int notificationCode, Object sender, HashMap<String, Object> notificationData);
    }
}
