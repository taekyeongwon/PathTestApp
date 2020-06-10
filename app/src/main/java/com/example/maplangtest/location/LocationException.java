package com.example.maplangtest.location;

/**
 * Created by jinkook on 2016. 9. 27..
 */

public class LocationException extends Exception {

    public static final int LOCATION_TIMEOUT = -1;
    public static final int LOCATION_PERMISSION_DENIED = -2;
    public static final int LOCATION_GOOGLE_PLAY_INIT_FAIL = -3;
    public static final int LOCATION_SERVICE_DISCONNECT = -4;
    public static final int LOCATION_CUSTOM_PROVISION_FAIL = -5;

    int code;

    public LocationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
