package co.kr.emgram.mobilpackfieldtest.location;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;

import java.util.List;


/**
 * 단말의 위치 정보 제공 서비스와 연동하여 위치를 수집하고 관리하는 역할을 담당한다.
 *
 * @author Created by jinkook on 2016. 9. 26..
 */

public class EMLocationManager implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private final String TAG = "EMLocationManager";

    private static EMLocationManager sharedInstance = null;

    // 현재 위치의 주소값을 갱신하기 위한 거리
    private final int ADDRESS_UPDATE_RANGE = 500;

    // 안드로이드에서 제공해주는 LocationManager 를 사용
    public static final int PROVIDER_LOCATION_MANAGER = 0x0001;
    // 구글 API에서 제공해주는 서비스를 이용
    public static final int PROVIDER_GOOGLEAPI = 0x0002;
    // GPSTrackingService로 부터 위치를 제공받음 (구현 재검토 필요)
   // public static final int PROVIDER_TRACKING_SERVICE = 0x0003;

    public static final String GPS_LATEST_PROVIDER = "gps_latest";

    /**
     * 안드로이드에서 제공해주는 LocationManager
     * 최초 실행시 PROVIDER_LOCATION_MANAGER 로 생성하면 설정된다.
     */
    private LocationManager locationManager;

    // 위치가 변경될 경우 호출될 Listener
    private EMLocationListener locationListener;

    // 앱이 실행된 후 위치서비스 제공자로 부터 최근에 제공받은 위치값 (검증단계 체크 된 위치)
    private Location latestLocation;

    // 앱이 실행된 후 위치서비스 제공자가 초기화 되기전 단말에서 최근 조회된 위치값
    private Location gpsLatestLocation;

    private Location lastGpsLocation;
    private Location lastNetworkLocation;

    // 현재 위치제공자를 통해 위치를 업데이트 받고 있는지 여부를 확인할 수 있는 플래그
    private boolean locationUpdateStart = false;

    // Google play service location
    private GoogleApiClient googleApiClient;
    private LocationRequest gpsRequest;

    // locationForAddress 기준으로한 주소값
    private Address currentAddress;

    // 주소를 가져왔을 때의 위치를 저장
    private Location locationForAddress;

    private SettingValue settingValue = new SettingValue();

    private Context context;
    //private Location location;
    private FusedLocationProviderClient fusedLocationProvider;

    private Handler gpsTimeoutHandler = new Handler(Looper.getMainLooper());
    private long resetTimeoutTime = 0;
    private Runnable GPSTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (settingValue.stopGPSTimeout == null) {
                return;
            }

            if (resetTimeoutTime > 0) {
                long currTime = System.currentTimeMillis();
                long remainTime = settingValue.stopGPSTimeout - (currTime - resetTimeoutTime);
                if (remainTime > 0) {
                    gpsTimeoutHandler.postDelayed(this, remainTime);
                    return;
                }
            }

            if (locationListener != null) {
                locationListener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_TIMEOUT, "timeout gps"), null);
            }
            stopUpdateLocation();
        }
    };
    /**
     * 위치가 변경될 경우 호출되는 Listener
     */
    public interface EMLocationListener {
        /**
         * 위치가 변경될 경우 호출 된다
         * @param location 변경된 위치
         * @param error 오류가 발생했을 경우의 정보
         * @param connectionResult GoogleApi를 이용할 경우 GoogleApi에서 전송된 오류 정보 LocationManager를 이용할 경우 항상 null
         */
        public void onUpdatedLocation(Location location, LocationException error, ConnectionResult connectionResult);
    }

    /**
     * 싱글톤 사용을 위한 함수
     * @return 싱글톤 객체
     */
    public static EMLocationManager getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new EMLocationManager();
        }
        return sharedInstance;
    }

    /**
     * 싱글톤 객체를 해제할때 사용함.
     */
    public static void releaseInstance() {
        if (sharedInstance != null) {
            sharedInstance.stopUpdateLocation();
            sharedInstance = null;
        }
    }

    public void setApplicationContext(Context ctx) {
        context = ctx;
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context);
    }

    public Context getApplicationContext() {
        return context;
    }

    private EMLocationManager() {
    }

    public void setSettingValue(SettingValue val) {
        settingValue = val;
    }

    public Location getLastGpsLocation() {
        return lastGpsLocation;
    }

    public Location getLastNetworkLocation() {
        return lastNetworkLocation;
    }

    /**
     * 초기 설정을 수행한다
     * 위치 제공자가 PROVIDER_LOCATION_MANAGER 일 경우 LocationManager를 설정하고
     * PROVIDER_GOOGLEAPI 일 경우 GoogleApiClient를 생성한다.
     * @param context Context
     */
    public void init(Context context) {
        if (settingValue.provider == PROVIDER_LOCATION_MANAGER) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        } else if (settingValue.provider == PROVIDER_GOOGLEAPI) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            gpsRequest = new LocationRequest();
            gpsRequest.setInterval(settingValue.interval);
            gpsRequest.setFastestInterval(settingValue.fastestInterval);
            gpsRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            gpsRequest.setSmallestDisplacement(settingValue.gpsMinDistance);
        }
    }

    public boolean isLocationUpdateStart() {
        return locationUpdateStart;
    }

    /**
     * 위치 제공자로 부터 위치를 주기적으로 업데이트 받기를 실행하기 위한 설정 및 요청을 수행한다
     * @param listener 위치 변경에 대한 결과를 전달받을 Listener
     */
    public void startUpdateLocation(EMLocationListener listener) {

        if (!GPSSettingReceiver.checkLocationPermission(context)) {
            if (listener != null) {
                listener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_PERMISSION_DENIED, "권한 없음"), null);
            }
            return;
        }

//        if (!GPSSettingReceiver.checkLocationProvision(context)) {
//            if (listener != null) {
//                listener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_CUSTOM_PROVISION_FAIL, "약관 미동의"), null);
//            }
//            return;
//        }

        stopUpdateLocation();

        init(context);

        locationListener = listener;
        if (settingValue.provider == PROVIDER_LOCATION_MANAGER) {

            if (latestLocation == null) {
                Location location = getLastKnowLocation(System.currentTimeMillis() - 120 * 60000);
                setGpsLatestLocation(location);
                if (location != null && locationListener != null) {
                    locationListener.onUpdatedLocation(location, null, null);
                }
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, settingValue.interval, settingValue.gpsMinDistance, this);
            if (settingValue.usingNetworkProvider) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, settingValue.interval, settingValue.gpsMinDistance, this);
            }

            startTimeoutCheck();
        } else if (settingValue.provider == PROVIDER_GOOGLEAPI) {
            googleApiClient.connect();
        }

        locationUpdateStart = true;
    }

    /**
     * 위치 제공자로 부터 위치를 주기적으로 제공받는 것을 중지한다
     */
    public void stopUpdateLocation() {
        locationUpdateStart = false;

        if (!GPSSettingReceiver.checkLocationPermission(context)) {
            if (locationListener != null) {
                locationListener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_PERMISSION_DENIED, "권한 없음"), null);
            }
            locationListener = null;
            return;
        }

//        if (!GPSSettingReceiver.checkLocationProvision(context)) {
//            if (locationListener != null) {
//                locationListener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_CUSTOM_PROVISION_FAIL, "약관 미동의"), null);
//            }
//            locationListener = null;
//            return;
//        }

        if (settingValue.provider == PROVIDER_LOCATION_MANAGER) {
            if (locationManager != null) {
                gpsTimeoutHandler.removeCallbacks(GPSTimeoutRunnable);
                locationManager.removeUpdates(this);
            }
            locationManager = null;
        } else if (settingValue.provider == PROVIDER_GOOGLEAPI) {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                fusedLocationProvider.removeLocationUpdates(locationCallback);
                //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                googleApiClient.disconnect();
            }
            googleApiClient = null;
            gpsRequest = null;
        }
        locationListener = null;
    }

    /**
     * 현재 위치에 대한 최적의 값을 가져온다.
     * 1순위는 앱 실행 후 위치 제공자로 가져온 위치
     * 2순위는 앱 실행 하기전 위치 제공자가 최근에 조회한 위치
     * 3순위는 개발자가 설정한 기본위치
     * @return 현재 위치
     */
    public Location getSuitableLocation() {
        if (latestLocation != null) {
            return latestLocation;
        } else if (gpsLatestLocation != null) {
            return gpsLatestLocation;
        } else {
            return settingValue.defaultLocation;
        }
    }

    /**
     * 개발자가 설정한 기본 위치
     * @return 개발자가 설정한 위치
     */
    public Location getDefaultLocation() {
        return settingValue.defaultLocation;
    }

    /**
     * 앱 실행 후 위치제공자로 부터 측정되어 전달된 위치값
     * @return 위치제공자로 부터 측정된 위치값
     */
    public Location getLatestLocation() {
        return latestLocation;
    }

    /**
     * 위치 제공자가 최근에 측정된 위치값을 리턴 한다
     * 위치 제공자가 LocationManager인 GPS 또는 network을 통해 측정된 값들 중
     * minTime을 기준으로 해당 시간보다 크면 위치의 정확도순, 그렇지 않을 경우 최근에 측정된 값을 기준으로 계산한다.
     * @param minTime 최근측정된 값으로 사용할 위치값의 기준 시간, 해당시간 보다 클 경우만 사용, PROVIDER_LOCATION_MANAGER 일 경우만 사용
     * @return 위치 제공자가 최근에 측정한 위치값
     */
    private Location getLastKnowLocation(long minTime) {

        if (!GPSSettingReceiver.checkLocationPermission(context)) {
            if (locationListener != null) {
                locationListener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_PERMISSION_DENIED, "권한 없음"), null);
            }
            return null;
        }

//        if (!GPSSettingReceiver.checkLocationProvision(context)) {
//            if (locationListener != null) {
//                locationListener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_CUSTOM_PROVISION_FAIL, "약관 미동의"), null);
//            }
//            return null;
//        }

        if (settingValue.provider == PROVIDER_LOCATION_MANAGER) {
            Location bestResult = null;
            float bestAccuracy = Float.MAX_VALUE;
            long bestTime = Long.MIN_VALUE;

            List<String> matchingProviders = locationManager.getAllProviders();

            for (String provider : matchingProviders) {

                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    float accuracy = location.getAccuracy();
                    long time = location.getTime();

                    if ((time > minTime && accuracy < bestAccuracy)) {
                        bestResult = location;
                        bestAccuracy = accuracy;
                        bestTime = time;
                    } else if (time < minTime &&
                            bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                        bestResult = location;
                        bestTime = time;
                    }
                }
            }
            return bestResult;
        } else {
            if (googleApiClient.isConnected()) {
                return fusedLocationProvider.getLastLocation().getResult();
               // return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            }
            return null;
        }
    }

    /**
     * 최근위치는 검증단계를 거쳐 측정된 위치로서 이를 무시하고 마지막으로 측정된 위치로 보정시킬 경우 사용
     * (PROVIDER_LOCATION_MANAGER 알 경우 사용 가능)<br>
     * 예를 들어 50m 단위로 측정되도록 설정된 경우 이 함수를 통해 50m이하 단위의 위치로 보정될 수 있도록 한다.
     */
    public Location correctLatestLocation(float minDistance, boolean usingNetworLocation, long maxValidateTimeNetworkLocation) {
        if (latestLocation == null || lastGpsLocation == null) {
            return null;
        }

        Location targetLoc = lastGpsLocation;
        if (usingNetworLocation && lastNetworkLocation != null) {
            long duration = lastNetworkLocation.getTime() - lastGpsLocation.getTime();
            if (duration <= maxValidateTimeNetworkLocation && duration > 0) {
                targetLoc = lastNetworkLocation;
            }
        }

        if (targetLoc.distanceTo(latestLocation) <= minDistance || targetLoc.getAccuracy() >= settingValue.maxAccuracyForGPS) {
            return null;
        }

        setLatestLocation(targetLoc);
        return targetLoc;
    }

    /**
     * Google api - FusedLocationApi가 deprecated 되어 FusedLocationProviderClient로 변경됨
     * 위 FusedLocationProviderClient가 위치를 받기위한 콜백
     */
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();
            Location location;

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);

                if (!checkAccuracy(location)) {
                    return;
                }

                Location validateLoc = checkLocationValidate(location);
                if (validateLoc != null) {
                    if (locationListener != null) {
                        locationListener.onUpdatedLocation(location, null, null);
                    }
                    setLatestLocation(location);
                }
            }
        }

    };

    /**
     * 위치제공자가 위치가 변경된 경우 호출되는 함수
     * LocationManager 와 GoogleApi 가 호출하는 함수 동일하여 하나로 사용중.
     * 위치 제공자가 전달하는 위치값을 그대로 사용할 경우 LocationManager 는 network을 통한 위치 측정에 의해 정확도가 떨어질수도 있으며,
     * 가만히 있는 상황임에도 불구하고 위치이동으로 간주 될 수 있어 checkLocationValidate 함수를 이용하여 위치 변화에 대한 유효성을 체크하여 위치정보를 사용할 지 결정한다
     * @param location 변경된 위치 값
     */
    @Override
    public void onLocationChanged(Location location) {

        if (location.getProvider().equals(GPS_LATEST_PROVIDER)) {
            setGpsLatestLocation(location);
            if (locationListener != null) {
                locationListener.onUpdatedLocation(location, null, null);
            }
            return;
        }

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            startTimeoutCheck();
        }

        if (!checkAccuracy(location)) {
            return;
        }

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            lastGpsLocation = location;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            lastNetworkLocation = location;
        }

        Location validateLoc = checkLocationValidate(location);
        if (validateLoc != null) {
            if (locationListener != null) {
                locationListener.onUpdatedLocation(location, null, null);
            }
            setLatestLocation(location);
        }
    }

    /**
     * LocationManager 사용 시 호출되는 함수
     *
     * @param provider 위치 제공자 (LocationManger.GPS_PROVIDER, LocationManger.NETWORK_PROVIDER...)
     * @param status 위치 제공자의 변경된 상태 LocationProvider.AVAILABLE, LocationProvider.OUT_OF_SERVICE...
     * @param extras 기타 정보
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * LocationManager 사용 시 호출되는 함수
     * @param provider 위치 제공자 (LocationManger.GPS_PROVIDER, LocationManger.NETWORK_PROVIDER...)
     */
    @Override
    public void onProviderEnabled(String provider) {
    }

    /**
     * LocationManager 사용 시 호출되는 함수
     * @param provider 위치 제공자 (LocationManger.GPS_PROVIDER, LocationManger.NETWORK_PROVIDER...)
     */
    @Override
    public void onProviderDisabled(String provider) {
    }

    /**
     * Google Play service 사용 시 호출되는 함수
     * Google API client에 연결됬을 때 호출된다.
     * Google Play service에서 제공하는 위치 서비스를 이용할 경우 먼저 Google Play Service에 연결해야 된다.
     * 이 연결이 성공적일 경우 해당 함수가 호출 된다
     * @param bundle 추가 정보
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d("EMLocation", "onConnected");
        if (!GPSSettingReceiver.checkLocationPermission(context)) {
            if (locationListener != null) {
                locationListener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_PERMISSION_DENIED, "권한 없음"), null);
            }
            return;
        }

//        if (!GPSSettingReceiver.checkLocationProvision(context)) {
//            if (locationListener != null) {
//                locationListener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_CUSTOM_PROVISION_FAIL, "약관 미동의"), null);
//            }
//            return;
//        }

        fusedLocationProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    setGpsLatestLocation(location);
                    if (locationListener != null) {
                        locationListener.onUpdatedLocation(location, null, null);
                    }
                }
            }
        });

        fusedLocationProvider.requestLocationUpdates(gpsRequest, locationCallback, Looper.myLooper());
       //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, gpsRequest, this);
    }

    /**
     * Google Play Service 연결이 끊어진 경우 호출됨.
     * 이 함수가 호출된 경우 Google Play Service는 자동으로 재연결을 시도하며 연결된 경우 onConnected를 호출한다
     * @param cause 연결이 끊어진 원인
     */
    @Override
    public void onConnectionSuspended(int cause) {

    }

    /**
     * Google Play Service 연결에 실패한 경우 호출됨
     * @param connectionResult 실패한 원인
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("EMLocation", "onConnectionFailed");
        locationUpdateStart = false;
        locationListener.onUpdatedLocation(null, new LocationException(LocationException.LOCATION_GOOGLE_PLAY_INIT_FAIL, "구글 서비스 연결 실패"), connectionResult);
    }

    /**
     * 앱 실행후 위치제공자에 의해 위치값이 변경된 경우 이를 저장한다
     * 위치가 변경될 경우 이전에 주소를 확인하기 위해 사용된 위치값 보다 ADDRESS_UPDATE_RANGE 거리만큼 벗어난 경우 확인된 주소를 null 처리 하여
     * 다음 주소값 사용시 재 확인할 수 있도록 한다
     * @param location 변경된 위치값
     */
    private void setLatestLocation(Location location) {
        latestLocation = location;

        if (locationForAddress != null && locationForAddress.distanceTo(latestLocation) > ADDRESS_UPDATE_RANGE) {
            currentAddress = null;
            locationForAddress = null;
        }
    }

    private void setGpsLatestLocation(Location location) {
        if (location != null) {
            location.setProvider(GPS_LATEST_PROVIDER);
        }
        gpsLatestLocation = location;
    }

    public void setLocationForAddress(Location locationForAddress) {
        this.locationForAddress = locationForAddress;
    }

    public Location getLocationForAddress() {
        return locationForAddress;
    }

    public void setCurrentAddress(Address currentAddress) {
        this.currentAddress = currentAddress;
    }

    public Address getCurrentAddress() {
        return currentAddress;
    }

    public void resetTimeoutCheck() {
        resetTimeoutTime = System.currentTimeMillis();
    }

    private boolean startTimeoutCheck() {
        if (settingValue.stopGPSTimeout == null || settingValue.provider != PROVIDER_LOCATION_MANAGER) {
            return false;
        }

        gpsTimeoutHandler.removeCallbacks(GPSTimeoutRunnable);
        gpsTimeoutHandler.postDelayed(GPSTimeoutRunnable, settingValue.stopGPSTimeout);
        resetTimeoutTime = 0;
        return true;
    }

    private boolean checkAccuracy(Location location) {

        if (latestLocation == null) {
            return true;
        }

        float accuracy = location.getAccuracy();
        String provider = location.getProvider();

        if (provider.equals(LocationManager.GPS_PROVIDER) && accuracy > settingValue.maxAccuracyForGPS) {
            return false;
        }
        else if (accuracy > settingValue.maxAccuracy) {
            return false;
        }
        return true;
    }

    /**
     * 위치 제공자에 의해 제공된 위치를 그대로 사용할 경우 정확도의 문제 등으로 인해 사용상에 무리가 있어 이를 보정하기 위한 위치 검증을 실행한다.
     * 현재 위치 정확도가 70 이상일 경우 와 이전 측정된 위치와 3m 이하로 차이가 날 경우 사용하지 않는다.
     * {@link LocationManager} 를 통해 위치를 가져올 경우 GPS_PROVIDER를 통해 제공받지 않았을 때에는 최초에만 사용을 하고 그 이후로는 사용하지 않는다.
     * @param location 위치 제공자로 부터 제공받은 위치값
     * @return 검증되고 보정된 위치값. null일 경우는 입력된 위치를 무시하기 위한 처리
     */
    private Location checkLocationValidate(Location location) {
        if (!settingValue.checkLocationValidate) {
            return location;
        }

        float accuracy = location.getAccuracy();
        String provider = location.getProvider();
        float speed = location.getSpeed();
        long time = location.getTime();

        if (settingValue.provider == PROVIDER_LOCATION_MANAGER || provider.equals(LocationManager.NETWORK_PROVIDER)) {
            if (!provider.equals(LocationManager.GPS_PROVIDER) && latestLocation != null) {
                return null;
            }
        }

        if (latestLocation != null) {

            float distance = latestLocation.distanceTo(location);

            if (distance < settingValue.minDistance) {
                return null;
            }

            if (settingValue.maxSpeedMeterPerSec != null) {
                long secDuration = (time - latestLocation.getTime()) / 1000;
                if ((distance / secDuration) >= settingValue.maxSpeedMeterPerSec) {
                    return null;
                }
            }
        }

        return location;
    }

    private int diffDistance = 30;  //default
    public void setDiffDistance(int diffDistance) {
        this.diffDistance = diffDistance;
    }

    public int getDiffDistance() {
        return diffDistance;
    }
    /**
     * 현재 위치로부터 탐색한 경로의 다음 위치까지 거리가 x미터 안에 있는지 여부 확인
     * @param current
     * @param nextLocation
     * @return
     */
    public boolean checkLocationInPath(LatLng current, LatLng nextLocation) {
        if (current != null) {
            double distance = current.distanceTo(nextLocation);

            if (distance < diffDistance) {
                return true;
            }
        }
        return false;

    }

    public boolean checkLocationInPath(LatLng current, LatLng startPos, LatLng nextPos) {
        double currentToStart = current.distanceTo(startPos);   //삼각형 변 a또는 b
        double currentToNext = current.distanceTo(nextPos);     //삼각형 변 a또는 b
        double startToNext = startPos.distanceTo(nextPos);      //삼각형 변 c(삼각형의 높이와 수직이 되는 밑변)

        double a, b;
        if(currentToStart < currentToNext) {
            a = currentToStart;
            b = currentToNext;
        } else {
            a = currentToNext;
            b = currentToStart;
        }
        double c = startToNext;

        if(startToNext == 0) {
            Log.d("Path", "startToNext == 0");
            return true;
        }
        double aSquare = Math.pow(a, 2);
        double bSquare = Math.pow(b, 2);
        double cSquare = Math.pow(c, 2);
        double tmpSquare = Math.pow((aSquare - bSquare + cSquare) / (2*c), 2);

        double height = Math.sqrt(Math.abs(aSquare - tmpSquare));

        Log.d("Height", "변 a        : "+a);
        Log.d("Height", "변 b        : "+b);
        Log.d("Height", "변 c        : "+c);
        Log.d("Height", "변 a제곱    : "+aSquare);
        Log.d("Height", "변 b제곱    : "+bSquare);
        Log.d("Height", "변 c제곱    : "+cSquare);
        Log.d("Height", "분수식 제곱 : "+tmpSquare);
        Log.d("Height", "높이        : "+height);

        return height <= diffDistance;  //true인 경우 start-next 좌표간 직선 안에 현재 위치의 반경이 포함되어 있음.
    }

    public static class SettingValue {
        int provider = PROVIDER_LOCATION_MANAGER;
        Location defaultLocation;

        boolean usingNetworkProvider = true;

        long interval = 1000;
        long fastestInterval = 1000;
        float gpsMinDistance = 0;

        float minDistance = 3;
        float maxAccuracy = 70;
        float maxAccuracyForGPS = 70;
        Float maxSpeedMeterPerSec = null;
        boolean checkLocationValidate = true;

        Long stopGPSTimeout = null; // LocationManager 전용. gps가 일정시간 응답없을 경우 위치추적 종료

        public SettingValue() {
            defaultLocation = new Location("default");
            // 서귀포
            defaultLocation.setLatitude(33.253148);
            defaultLocation.setLongitude(126.560895);
        }

        public SettingValue setProvider(int provider) {
            this.provider = provider;
            return this;
        }

        public SettingValue setDefaultLatitude(double lat) {
            if (defaultLocation == null) {
                defaultLocation = new Location("default");
            }
            defaultLocation.setLatitude(lat);
            return this;
        }

        public SettingValue setDefaultLongitude(double longitude) {
            if (defaultLocation == null) {
                defaultLocation = new Location("default");
            }
            defaultLocation.setLatitude(longitude);
            return this;
        }

        public SettingValue setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public SettingValue setFastestInterval(long interval) {
            this.fastestInterval = interval;
            return this;
        }

        public SettingValue setGPSMinDistance(float dist) {
            this.gpsMinDistance = dist;
            return this;
        }

        public SettingValue setMinDistance(long dist) {
            this.minDistance = dist;
            return this;
        }

        public SettingValue setCheckValidate(boolean check) {
            this.checkLocationValidate = check;
            return this;
        }

        public SettingValue setMaxAccuracy(float accuracy) {
            this.maxAccuracy = accuracy;
            return this;
        }

        public SettingValue setMaxAccuracyForGPS(float accuracy) {
            this.maxAccuracyForGPS = accuracy;
            return this;
        }

        public SettingValue setMaxSpeedMeterPerSec(float speed) {
            this.maxSpeedMeterPerSec = speed;
            return this;
        }

        public SettingValue setStopGPSTimeout(long timeout) {
            stopGPSTimeout = timeout;
            return this;
        }

        public SettingValue setUsingNetworkProvider(boolean using) {
            usingNetworkProvider = using;
            return this;
        }
    }
}
