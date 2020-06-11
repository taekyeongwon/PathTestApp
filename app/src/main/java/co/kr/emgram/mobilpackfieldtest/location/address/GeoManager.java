package co.kr.emgram.mobilpackfieldtest.location.address;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import androidx.annotation.Nullable;

import co.kr.emgram.mobilpackfieldtest.location.EMLocationManager;


public class GeoManager {
    private static GeoManager sharedInstance = null;

    /**
     * 싱글톤 사용을 위한 함수
     * @return 싱글톤 객체
     */
    public static GeoManager getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new GeoManager();
        }
        return sharedInstance;
    }

    /**
     * 싱글톤 객체를 해제할때 사용함.
     */
    public static void releaseInstance() {
        if (sharedInstance != null) {
            sharedInstance = null;
        }
    }
    /**
     * 현재 위치를 기준으로 해당 위치의 주소값을 가져온다. 이미 확인된 주소가 있을 경우 그 주소를 리턴 한다
     * @param ctx Context
     * @param callback 주소확인 후 호출될 callback
     * @return 이미 확인된 주소가 있을 경우 다시 확인하지 않고 그 값을 리턴한다
     */
    public Address getCurrentAddress(Context ctx, JKAddressUtils.OnUpdatedAddress callback) {
        if (EMLocationManager.getInstance().getCurrentAddress() == null) {
            updateCurrentAddress(ctx, EMLocationManager.getInstance().getSuitableLocation(), callback);
        }
        return EMLocationManager.getInstance().getCurrentAddress();
    }

    /**
     * 입력된 location 의 위치를 기준으로 주소값을 설정한다.
     * @param ctx Context
     * @param location 주소를 확인할 위치값
     * @param callback 주소 확인 후 호출될 callback
     */
    public void updateCurrentAddress(Context ctx, Location location, final JKAddressUtils.OnUpdatedAddress callback) {
        JKAddressUtils.reverseGeoCode(ctx, location, new JKAddressUtils.OnUpdatedAddress() {
            @Override
            public void onUpdatedAddress(@Nullable Address address, Throwable error) {
                if (error == null) {
                    EMLocationManager.getInstance().setCurrentAddress(address);
                    EMLocationManager.getInstance().setLocationForAddress(EMLocationManager.getInstance().getSuitableLocation());
                }

                if (callback != null) {
                    callback.onUpdatedAddress(address, error);
                }
            }
        });
    }
}
