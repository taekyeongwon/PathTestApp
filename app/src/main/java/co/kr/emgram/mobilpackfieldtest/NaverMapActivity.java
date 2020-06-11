package co.kr.emgram.mobilpackfieldtest;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.ConnectionResult;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import co.kr.emgram.mobilpackfieldtest.location.EMLocationManager;
import co.kr.emgram.mobilpackfieldtest.location.LocationException;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class NaverMapActivity extends AppCompatActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks, EMLocationManager.EMLocationListener {
    private NaverMap naverMap;
    private String[] perms = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int REQUEST_PERMISSIONS = 0x00;

    private static NAsync asyncTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naver);


        Log.d("main", "oncreate");
//        Spinner spinner = findViewById(R.id.spinner);
//        adapter = new SpinnerAdapter(this);
//        spinner.setAdapter(adapter);
        EMLocationManager.getInstance().setApplicationContext(this);

        checkPermissions();
        asyncTask = new NAsync();

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onDestroy() {
        asyncTask.cancel(true);
        naverMap = null;
        EMLocationManager.getInstance().stopUpdateLocation();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private void checkPermissions() {
        if(EasyPermissions.hasPermissions(this, perms)) {
            if(EMLocationManager.getInstance().getApplicationContext() == null) {
                Log.d("MainActivity", "getApplicationContext() == null");
            }
            if(MainApplication.application == null) {
                Log.d("MainActivity", "GPSApplication.application == null");
            }
            if(!EMLocationManager.getInstance().isLocationUpdateStart()) {
                EMLocationManager.SettingValue value = new EMLocationManager.SettingValue()
                        .setProvider(EMLocationManager.PROVIDER_GOOGLEAPI)
                        .setUsingNetworkProvider(false)
                        .setGPSMinDistance(0f)
                        .setMaxAccuracyForGPS(30f);

                EMLocationManager.getInstance().setSettingValue(value);
                EMLocationManager.getInstance().startUpdateLocation(this);
            } else {
                EMLocationManager.getInstance().resetTimeoutCheck();
            }
        } else {
            EasyPermissions.requestPermissions(this, "need permissions", REQUEST_PERMISSIONS, perms);
        }
    }
    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        this.naverMap = naverMap;

        if(TMapManager.getInstance().getSucceeded()) {
            asyncTask.execute("여의도");
        }
    }

    private class NAsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                final ArrayList<LatLng> coordList = new ArrayList<>();
                TMapData data = new TMapData();
                ArrayList<TMapPOIItem> list = data.findAllPOI("여의도");
                ArrayList<String> poiName = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    Log.d("POI :: ", "\n" + list.get(i).name);
                    poiName.add(list.get(i).name);
                }
//                    adapter.setCarInfoData(poiName);
                final TMapPoint startPoint = new TMapPoint(37.5248, 126.93);
                TMapPoint endPoint = new TMapPoint(37.4601, 128.0428);
                TMapPolyLine pathList = data.findPathData(startPoint, endPoint);
                if (pathList == null || pathList.getLinePoint() == null) {
                    Log.d("Main", "pathList is null");
                }
                for (int i = 0; i < pathList.getLinePoint().size(); i++) {
                    double lat = pathList.getLinePoint().get(i).getLatitude();
                    double lng = pathList.getLinePoint().get(i).getLongitude();
                    LatLng latLng = new LatLng(lat, lng);
                    coordList.add(latLng);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Marker marker = new Marker();
                        marker.setPosition(new LatLng(37.5248, 126.93));
                        marker.setCaptionText("출발");
                        marker.setMap(naverMap);

                        Marker endMarker = new Marker();
                        endMarker.setPosition(new LatLng(37.4601, 128.0428));
                        endMarker.setCaptionText("도착");
                        endMarker.setMap(naverMap);
                    }
                });
                final PathOverlay path = new PathOverlay();
                final Marker current = new Marker();
                //for(int i = 0; i < coordList.size(); i++) {
//                    thread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
                final Iterator<LatLng> iter = coordList.iterator();
                while (iter.hasNext()) {

                    final LatLng pos = iter.next();
                    if (coordList.size() < 2 || isCancelled()) {
                        Log.d("NAsync", "coordList < 0 or isCancelled");
                        break;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isCancelled()) {
                                return;
                            }
                            //if (thread != null && !thread.isInterrupted()) {
                            path.setMap(null);
                            path.setCoords(coordList);
                            path.setMap(naverMap);

                            current.setMap(null);
                            current.setPosition(pos);
                            current.setMap(naverMap);
                            Log.d("Naver", "runOnUiThread");
                            iter.remove();
                            //}
                        }
                    });


                    //coordList.remove(0);
                    //i = 0;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private boolean isFirst = true;
    @Override
    public void onUpdatedLocation(Location location, LocationException error, ConnectionResult connectionResult) {
        LatLng coord = new LatLng(EMLocationManager.getInstance().getSuitableLocation());
        LocationOverlay overlay = naverMap.getLocationOverlay();
        overlay.setVisible(true);
        overlay.setPosition(coord);
        overlay.setBearing(EMLocationManager.getInstance().getSuitableLocation().getBearing());
        if(isFirst)
        naverMap.moveCamera(CameraUpdate.scrollTo(coord));
        isFirst = false;
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
