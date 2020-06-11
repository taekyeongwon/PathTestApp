package co.kr.emgram.mobilpackfieldtest;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.kr.emgram.mobilpackfieldtest.location.EMLocationManager;
import co.kr.emgram.mobilpackfieldtest.location.LocationException;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PathFindActivity extends AppCompatActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks, EMLocationManager.EMLocationListener {
    private NaverMap naverMap;
    private String[] perms = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int REQUEST_PERMISSIONS = 0x00;

    private PathFindThread pathFindThread;
    private ArrayAdapter<String> adapter;
    private EditText arrive_et;
    private Button arrive_btn;
    private Button btn_current;
    private TMapData data;
    private ArrayList<String> poiName;
    private ArrayList<TMapPOIItem> poiList;
    private ArrayList<LatLng> coordList = new ArrayList<>();

    private Marker startMarker = new Marker();
    private Marker endMarker = new Marker();
    private Marker current = new Marker();
    private PathOverlay path = new PathOverlay();
    private CircleOverlay circle = new CircleOverlay();
    private CircleOverlay first = new CircleOverlay();
    private CircleOverlay second = new CircleOverlay();

    private PolylineOverlay firstPolyLine = new PolylineOverlay();
    private PolylineOverlay secondPolyLine = new PolylineOverlay();

    private int[] diffDistance = {10, 20, 30, 40 ,50};

    private TMapPOIItem latestDest;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);

        arrive_et = (EditText) findViewById(R.id.arrive_et);
        arrive_btn = (Button) findViewById(R.id.arrive_btn);
        btn_current = (Button) findViewById(R.id.btn_current);

        data = new TMapData();

        final Spinner spinner = findViewById(R.id.spinner);
        arrive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            poiList = data.findAllPOI(arrive_et.getText().toString());
                            poiName = new ArrayList<>();
                            for (int i = 0; i < poiList.size(); i++) {
                                Log.d("POI :: ", "\n" + poiList.get(i).name);
                                poiName.add(poiList.get(i).name);
                            }
                            adapter = new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_spinner_dropdown_item,
                                    poiName);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    spinner.setAdapter(adapter);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
            }
        });

        btn_current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(naverMap != null) {
                    LatLng curPos = new LatLng(
                            EMLocationManager.getInstance().getSuitableLocation().getLatitude(),
                            EMLocationManager.getInstance().getSuitableLocation().getLongitude());
                    naverMap.moveCamera(CameraUpdate.scrollTo(curPos));
                }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if(poiList != null && poiList.get(i) != null) {
                        latestDest = poiList.get(i);
                    }
                    if(pathFindThread != null && pathFindThread.isAlive()) {
                        pathFindThread.interrupt();
                    }
                    pathFindThread = new PathFindThread(latestDest);
                    pathFindThread.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner distance_sp = (Spinner) findViewById(R.id.distance_sp) ;
        distance_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                EMLocationManager.getInstance().setDiffDistance(diffDistance[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        distance_sp.setSelection(2);

        EMLocationManager.getInstance().setApplicationContext(this);

        checkPermissions();


        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);


    }
    private void initMap() {
        startMarker.setMap(null);
        endMarker.setMap(null);
        current.setMap(null);
        path.setMap(null);
    }

    @Override
    protected void onDestroy() {
        if(pathFindThread != null && pathFindThread.isAlive()) {
            pathFindThread.interrupt();
        }
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
                Log.d("PathActivity", "getApplicationContext() == null");
            }
            if(MainApplication.application == null) {
                Log.d("PathActivity", "GPSApplication.application == null");
            }
            if(!EMLocationManager.getInstance().isLocationUpdateStart()) {
                Log.d("PathFind", "isLocationUpdateStart is false");
                EMLocationManager.SettingValue value = new EMLocationManager.SettingValue()
                        .setProvider(EMLocationManager.PROVIDER_GOOGLEAPI)
                        .setUsingNetworkProvider(false)
                        .setGPSMinDistance(0f)
                        .setMaxAccuracyForGPS(30f);

                EMLocationManager.getInstance().setSettingValue(value);
                EMLocationManager.getInstance().startUpdateLocation(this);
            } else {
                Log.d("PathFind", "isLocationUpdateStart is true");
                EMLocationManager.getInstance().resetTimeoutCheck();
            }
        } else {
            EasyPermissions.requestPermissions(this, "need permissions", REQUEST_PERMISSIONS, perms);
        }
    }
    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        this.naverMap = naverMap;
        initMap();
    }

    private class PathFindThread extends Thread {
        private TMapPOIItem item;
        public PathFindThread(TMapPOIItem item) {
            this.item = item;
            initMap();
        }
        @Override
        public void run() {
            try {
                coordList.clear();

                final double currentLat = EMLocationManager.getInstance().getSuitableLocation().getLatitude();
                final double currentLng = EMLocationManager.getInstance().getSuitableLocation().getLongitude();
                String arriveLat = item.frontLat != null ? item.frontLat : item.noorLat;
                String arriveLng = item.frontLon != null ? item.frontLon : item.noorLon;
                final double dLat = Double.parseDouble(arriveLat);
                final double dLng = Double.parseDouble(arriveLng);

                final TMapPoint startPoint = new TMapPoint(currentLat, currentLng);
                TMapPoint endPoint = new TMapPoint(dLat, dLng);
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

//                ArrayList<TMapPOIItem> around = data.findAroundNamePOI(startPoint, "편의점;은행", 2, 100);
//                for(int i = 0; i < around.size(); i++) {
//                    Log.d("Around", around.get(i).name);
//                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startMarker.setPosition(new LatLng(currentLat, currentLng));
                        startMarker.setCaptionText("출발");
                        startMarker.setMap(naverMap);

                        endMarker.setPosition(new LatLng(dLat, dLng));
                        endMarker.setCaptionText("도착");
                        endMarker.setMap(naverMap);

                        path.setMap(null);
                        path.setCoords(coordList);
                        path.setMap(naverMap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isFirst = true;
    @Override
    public void onUpdatedLocation(Location location, LocationException error, ConnectionResult connectionResult) {
        if(naverMap == null) {
            return;
        }
        final LatLng coord = new LatLng(EMLocationManager.getInstance().getSuitableLocation());
        LocationOverlay overlay = naverMap.getLocationOverlay();
        overlay.setVisible(true);
        overlay.setPosition(coord);
        overlay.setBearing(EMLocationManager.getInstance().getSuitableLocation().getBearing());
        if(isFirst)
            naverMap.moveCamera(CameraUpdate.scrollTo(coord));
        isFirst = false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                circle.setMap(null);
                circle.setCenter(coord);
                circle.setRadius(EMLocationManager.getInstance().getDiffDistance());
                circle.setColor(Color.argb(125, 255, 125, 255));
                circle.setMap(naverMap);

            }
        });
        if(coordList != null && coordList.size() > 0) {
            first.setMap(null);
            first.setCenter(coordList.get(0));
            first.setRadius(10);
            first.setColor(Color.argb(125, 0, 0, 225));
            first.setMap(naverMap);

            firstPolyLine.setMap(null);
            firstPolyLine.setCoords(Arrays.asList(
                    coord, coordList.get(0)
            ));
            firstPolyLine.setColor(Color.argb(125, 0, 0, 225));
            firstPolyLine.setMap(naverMap);

            if(coordList.size() > 1) {
                second.setMap(null);
                second.setCenter(coordList.get(1));
                second.setRadius(10);
                second.setColor(Color.argb(125, 255, 0, 0));
                second.setMap(naverMap);

                secondPolyLine.setMap(null);
                secondPolyLine.setCoords(Arrays.asList(
                        coord, coordList.get(1)
                ));
                secondPolyLine.setColor(Color.argb(125, 255, 0, 0));
                secondPolyLine.setMap(naverMap);

            }

            if (EMLocationManager.getInstance().checkLocationInPath(coord, coordList.get(0), coordList.get(1))) {
                Log.d("Path", "현재위치가 경로안에 있음");
                //coordList.set(0, coord);
                if (EMLocationManager.getInstance().checkLocationInPath(coord, coordList.get(1))) {
                    Log.d("Path", "현재위치가 1번째 위치 반경안에 있음");
                    //coordList.set(1, coord);
                    coordList.remove(0);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (coordList.size() > 2) {
                            path.setMap(null);
                            path.setCoords(coordList);
                            path.setMap(naverMap);
                        }

                        current.setMap(null);
                        current.setPosition(coordList.get(0));
                        current.setMap(naverMap);
                    }
                });
            } else {
                Log.d("Path", "현재 위치가 경로안에 없음. 재요청");
                if (pathFindThread != null && pathFindThread.isAlive()) {
                    pathFindThread.interrupt();
                }

                coordList.clear();
                pathFindThread = new PathFindThread(latestDest);
                pathFindThread.start();
            }
        }
//        if(coordList != null && coordList.size() > 0) {
//            //LatLng currentLoc = new LatLng(location);
//            if (EMLocationManager.getInstance().checkLocationInPath(coord, coordList.get(0))) {
//                Log.d("PathFind", "현재위치부터 0번째 인덱스");
//                coordList.set(0, coord);
//                if (EMLocationManager.getInstance().checkLocationInPath(coordList.get(0), coordList.get(1))) {
//                    Log.d("PathFind", "0번째부터 1번째 인덱스");
//                    coordList.remove(0);
//                    if(coordList.size() > 0) {
//                        coordList.set(0, coord);
//                    } else {
//                        coordList.add(coord);
//                    }
//                } else {    //가만히 있는데도 gps가 튀면 재요청을 해버림.. 0번째부터 1번째사이의 거리 판별하는 최소값을 늘려야 하지 않을까?
//                    Log.d("PathFind", "0번째부터 1번째 인덱스 거리 > 10");
//                    if (pathFindThread != null && pathFindThread.isAlive()) {
//                        pathFindThread.interrupt();
//                    }
//                    pathFindThread = new PathFindThread(latestDest);
//                    pathFindThread.start();
//
//                    return;
//                }
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(coordList.size() > 2) {
//                            path.setMap(null);
//                            path.setCoords(coordList);
//                            path.setMap(naverMap);
//                        }
//
//                        current.setMap(null);
//                        current.setPosition(coordList.get(0));
//                        current.setMap(naverMap);
//                    }
//                });
//            } else {
//                Log.d("PathFind", "현재위치부터 0번째 인덱스 거리 > 0");
//                if (pathFindThread != null && pathFindThread.isAlive()) {
//                    pathFindThread.interrupt();
//                }
//                pathFindThread = new PathFindThread(latestDest);
//                pathFindThread.start();
//            }
//        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
