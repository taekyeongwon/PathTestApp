package com.example.maplangtest.location;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.maplangtest.MainApplication;
import com.example.maplangtest.R;
import com.google.android.gms.common.ConnectionResult;

import java.util.HashMap;


/**
 * Created by jinkook on 2016. 11. 8..
 */

public class GPSTrackingService extends Service implements EMLocationManager.EMLocationListener, JKNotificationCenter.JKNotificationObserver {
    private final String TAG = "GPSTrackingService";

    public static final int NOTIFICATION_ID = 0xff00;

    private final long TIME_OF_LOCATION_STOP = 15000;

    private GPSSettingReceiver settingReceiver;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private int provider;
    private static OnLocationChanged locationChanged;
    private static Context context;

    public static boolean isServiceRunning(Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GPSTrackingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startService(Context ctx, final int provider) {
        if (!GPSTrackingService.isServiceRunning(ctx)) {
            Intent service = new Intent(ctx, GPSTrackingService.class);
            service.putExtra("provider", provider);
            ctx.startService(service);
        }
    }

    public static void startService(Context ctx, final int provider, OnLocationChanged listener) {
        if (!GPSTrackingService.isServiceRunning(ctx)) {
            Intent service = new Intent(ctx, GPSTrackingService.class);
            service.putExtra("provider", provider);

            locationChanged = listener;
            if(ctx == null) {
                Log.d("GPSTrackingService", "context == null");
            }
            if(EMLocationManager.getInstance().getApplicationContext() == null) {
                Log.e("GPSTrackingService", "getApplicationContext() == null");
            }
            context = ctx;
            ctx.startService(service);
        }
    }

    public static void stopService(Context ctx) {
        Intent service = new Intent(ctx, GPSTrackingService.class);
        ctx.stopService(service);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EMLocationManager.getInstance().setApplicationContext(MainApplication.application);
        provider = intent.getIntExtra("provider", EMLocationManager.PROVIDER_LOCATION_MANAGER);
        settingReceiver = GPSSettingReceiver.registerReceiver(this);
//        JKNotificationCenter.getInstance().addObserver(C.Notification.GPSEnable, this);
//        JKNotificationCenter.getInstance().addObserver(C.Notification.GPSDisable, this);

        Log.d("GPSTrackingService", "by onStartCommand");
        if(EMLocationManager.getInstance().getApplicationContext() == null) {
            Log.e("GPSTrackingService", "getApplicationContext() == null");
            //EMLocationManager.getInstance().setApplicationContext(context);
        }
        startLocationUpdate(provider);

        Notification notification = createNotification();
        this.startForeground(NOTIFICATION_ID, notification);

        Intent service = new Intent(getApplicationContext(), InvisibleForegroundService.class);
        startService(service);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {


        settingReceiver.unRegister();
        JKNotificationCenter.getInstance().removeObserver(this);

        EMLocationManager.releaseInstance();


        cancelGPSWakeUpTimer();

        stopForeground(true);


        super.onDestroy();
    }

    @Override
    public void onUpdatedLocation(Location location, LocationException error, ConnectionResult connectionResult) {

        Log.d(TAG, "onUpdated : " + location + ", " + error + ", " + connectionResult);

        if (location != null && location.getProvider().equals(EMLocationManager.GPS_LATEST_PROVIDER)) {
            return;
        }

        if (error != null) {
            if (error.getCode() != LocationException.LOCATION_TIMEOUT) {
                return;
            }

            // 외부에서 건물 내부로 들어갈 때 최대 50미터 이상 위치가 차이날 수 있어 이를 보정하기 위한 처리
            location = EMLocationManager.getInstance().correctLatestLocation(10, false, 0);
        } else {
            cancelGPSWakeUpTimer();
        }

        // 타임 아웃일 경우 오늘날짜 이전 로그들을 로그북 데이터로 변환처리
        if (error != null) {
            runGPSWakeUpTimer();
        }

        //locationChanged.onChanged(location);
    }


    @Override
    public void onNotification(int notificationCode, Object sender, HashMap<String, Object> notificationData) {
//        if (notificationCode == C.Notification.GPSEnable) {
//            logCollector.gpsEnable(true);
//        } else if (notificationCode == C.Notification.GPSDisable) {
//            logCollector.gpsEnable(false);
//        }
    }


    private void startLocationUpdate(final int provider) {
        if (!EMLocationManager.getInstance().isLocationUpdateStart()) {
            // 위치 정보는 gps 정보를 위주로 처리 된다.
            EMLocationManager.SettingValue value = new EMLocationManager.SettingValue()
                    .setProvider(provider)
                    .setUsingNetworkProvider(false)
                    .setInterval(0)
                    .setGPSMinDistance(0)
                    .setMaxAccuracyForGPS(30)
                    .setStopGPSTimeout(TIME_OF_LOCATION_STOP)
                    .setMinDistance(50);

            EMLocationManager.getInstance().setSettingValue(value);
            EMLocationManager.getInstance().startUpdateLocation(this);

        } else {
            EMLocationManager.getInstance().resetTimeoutCheck();
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_custom_layout);
        remoteViews.setTextViewText(R.id.title_tv, "tracking..");
        Intent intent = new Intent(getPackageName() + ".notification_close");
        PendingIntent closePending = PendingIntent.getBroadcast(this, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.close_ib, closePending);

        builder.setContent(remoteViews);

        return builder.build();
    }

//    private void sendUpdateLocation(Location location, LocationException error) {
//        try {
//            Message msg = Message.obtain(null, C.Service.Messenger_update_location);
//            Bundle data = new Bundle();
//            if (error != null) {
//                data.putInt(C.Service.Param.Update_error, error.getCode());
//            } else {
//                data.putDouble(C.Service.Param.Update_Lat, location.getLatitude());
//                data.putDouble(C.Service.Param.Update_Lng, location.getLongitude());
//                data.putLong(C.Service.Param.Update_Date, location.getTime());
//                data.putString(C.Service.Param.Update_provider, location.getProvider());
//            }
//            msg.setData(data);
////            sendMessageToClients(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    Runnable gpsWakeUpRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("GPSTrackingService", "by Runnable");
            startLocationUpdate(provider);
        }
    };

    private long wakeupTimerTime = 30000;

    private void runGPSWakeUpTimer() {
        mainHandler.postDelayed(gpsWakeUpRunnable, wakeupTimerTime);
        wakeupTimerTime *= 2;
    }

    private void cancelGPSWakeUpTimer() {
        wakeupTimerTime = 30000;
        mainHandler.removeCallbacks(gpsWakeUpRunnable);
    }

    public static class NotificationCloseButtonListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent serviceIntent = new Intent(context, GPSTrackingService.class);
            context.stopService(serviceIntent);

            if (EMLocationManager.getInstance().getApplicationContext() != null) {
                //EMLocationManager.getInstance().getApplicationContext().destroyApplication(false);
            }

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }
    }
}
