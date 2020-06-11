package co.kr.emgram.mobilpackfieldtest.location;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;


/**
 * Created by jinkook on 2016. 9. 27..
 */

public class GPSSettingReceiver extends BroadcastReceiver {

    protected Context context;

    public static boolean checkGPSEnabled(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
        return manager.isProviderEnabled( LocationManager.GPS_PROVIDER );
    }

    public static boolean checkLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23 &&
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

//    public static boolean checkLocationProvision(Context context) {
//        //return UserInfoManager.getInstance().getAgreeLocationProvision();
//        return true;
//    }
//
    public static GPSSettingReceiver registerReceiver(Context context) {
        GPSSettingReceiver receiver = new GPSSettingReceiver();
        receiver.context = context;
        context.registerReceiver(receiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        return receiver;
    }

    public void unRegister() {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (checkGPSEnabled(context)) {
//            JKNotificationCenter.getInstance().sendNotification(C.Notification.GPSEnable, null, null);
//        } else {
//            JKNotificationCenter.getInstance().sendNotification(C.Notification.GPSDisable, null, null);
//        }
    }
}
