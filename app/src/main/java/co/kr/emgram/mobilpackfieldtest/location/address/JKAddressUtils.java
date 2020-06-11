package co.kr.emgram.mobilpackfieldtest.location.address;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.naver.maps.geometry.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * Created by jinkookseok on 14. 12. 23..
 */
public class JKAddressUtils {

    public interface OnUpdatedAddress {
        public void onUpdatedAddress(@Nullable Address address, Throwable error);
    }

    public static String stringFromAddress(Address address) {
        if (address == null) return "";

        String addressText = String.format(
                "%s %s %s",
                address.getAdminArea() != null ? address.getAdminArea() : "",
                address.getLocality() != null ? address.getLocality() : "",
                address.getThoroughfare() != null ? address.getThoroughfare() : "");

        if (address.getFeatureName() != null && address.getFeatureName().endsWith("리")) {
            addressText += " " + address.getFeatureName();
        }

        return addressText;
    }

    public static String stringFullAddress(Address address) {
        try {
            String strAddr = address.getAddressLine(0);
            if (strAddr != null) {
                return strAddr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String localityFromAddress(Address address) {
        if (address == null) return "";

        String addressText = String.format(
                "%s %s",
                address.getAdminArea() != null ? address.getAdminArea() : "",
                address.getLocality() != null ? address.getLocality() : "");

        return addressText;
    }

    public static void reverseGeoCode(final Context ctx, final Location location, final OnUpdatedAddress callback) {

        new AsyncTask<Location, Void, Address>() {

            Throwable error;

            @Override
            protected Address doInBackground(Location... params) {

                Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());

                Location loc = params[0];
                // Create a list to contain the result address
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    error = new Throwable("주소를 가져오는데 실패하였습니다.", e1);

                    return null;
                } catch (IllegalArgumentException e2) {
                    // Error message to post in the log
                    String errorString = "Illegal arguments " +
                            Double.toString(loc.getLatitude()) +
                            " , " +
                            Double.toString(loc.getLongitude()) +
                            " passed to address service";
                    Log.v("LocationSampleActivity", errorString);
                    e2.printStackTrace();

                    error = new Throwable("잘못된 위치정보 입니다.", e2);
                    return null;
                }

                // If the reverse geocode returned an address
                if (addresses != null && addresses.size() > 0) {
                    error = null;
                    return addresses.get(0);
                } else {
                    error = new Throwable("해당 위치의 주소를 찾을 수 없습니다.");
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Address address) {
                super.onPostExecute(address);
                callback.onUpdatedAddress(address, error);
            }
        }.execute(location);
    }

    public static void geoCode(final Context ctx, String locationName, final OnUpdatedAddress callback) {

        new AsyncTask<String, Void, Address>() {

            Throwable error;

            @Override
            protected Address doInBackground(String... params) {

                Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());

                String locName = params[0];
                // Create a list to contain the result address
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocationName(locName, 1);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    error = new Throwable("위경도를 가져오는데 실패하였습니다.", e1);
                    return null;
                } catch (IllegalArgumentException e2) {
                    error = new Throwable("위경도를 가져오는데 실패하였습니다.", e2);
                    return null;
                }

                if (addresses != null && addresses.size() > 0) {
                    return addresses.get(0);
                } else {
                    error = new Throwable("해당 위치의 위경도를 찾을 수 없습니다.");
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Address address) {
                super.onPostExecute(address);

                callback.onUpdatedAddress(address, error);
            }
        }.execute(locationName);
    }

    private static Throwable parseGoogleAddress(JsonArray results, Address resultAddress) {

        Throwable throwable = null;

        HashMap<String, AddressComponent> addressMap = new HashMap<String, AddressComponent>();
        LatLng location = null;
        String formatted_address = "";

        for (JsonElement element : results) {
            if (!element.isJsonObject()) continue;
            if (!element.getAsJsonObject().has("address_components")) continue;

            ArrayList<AddressComponent> addresssComponents = null;
            try {
                addresssComponents = new Gson().fromJson(element.getAsJsonObject().get("address_components"),
                        new TypeToken<ArrayList<AddressComponent>>(){}.getType());
            } catch (Exception e) {
                continue;
            }

            if (addresssComponents.size() > 0) {
                for (AddressComponent component : addresssComponents) {
                    if (component.types.length > 0) {
                        addressMap.put(component.types[0], component);
                    }
                }

                JsonElement geometry = element.getAsJsonObject().get("geometry");
                if (geometry != null) {
                    try {
                        Geometry geo = new Gson().fromJson(geometry, Geometry.class);
                        location = new LatLng(geo.location.lat, geo.location.lng);
                    } catch (Exception e) {
                    }
                }

                try {
                    formatted_address = element.getAsJsonObject().get("formatted_address").getAsString();
                    if (formatted_address == null) formatted_address = "";
                } catch (Exception e) {
                    e.printStackTrace();
                    formatted_address = "";
                }

                break;
            }
        }

        if (addressMap.size() == 0) throwable = new Throwable("해당 위치의 주소를 찾을 수 없습니다.");
        else {
            resultAddress.setAddressLine(0, formatted_address);

            AddressComponent component = addressMap.get("country");
            if (component != null) {
                resultAddress.setCountryCode(component.short_name);
                resultAddress.setCountryName(component.long_name);
            }

            component = addressMap.get("postal_code");
            if (component != null) {    // 도 단위
                resultAddress.setPostalCode(component.long_name);
            }

            component = addressMap.get("administrative_area_level_1");
            if (component != null) {    // 도 단위
                resultAddress.setAdminArea(component.long_name);

                component = addressMap.get("locality");
                if (component != null) {
                    resultAddress.setLocality(component.long_name);
                }

                component = addressMap.get("sublocality_level_2");
                if (component != null) {
                    resultAddress.setThoroughfare(component.long_name);
                } else {
                    component = addressMap.get("sublocality_level_4");      // 거리주소
                    resultAddress.setThoroughfare(component.long_name);
                }

                component = addressMap.get("sublocality_level_3");
                if (component != null) {
                    resultAddress.setFeatureName(component.long_name);
                }

            } else {        // 특별시 단위
                component = addressMap.get("locality");
                if (component != null) {
                    resultAddress.setAdminArea(component.long_name);
                }

                component = addressMap.get("sublocality_level_1");
                if (component != null) {
                    resultAddress.setLocality(component.long_name);
                }

                component = addressMap.get("sublocality_level_2");
                if (component != null) {    // 동 단위
                    resultAddress.setThoroughfare(component.long_name);
                } else {
                    component = addressMap.get("sublocality_level_4");      // 거리주소
                    resultAddress.setThoroughfare(component.long_name);
                }
            }

            component = addressMap.get("premise");
            if (component != null) {
                resultAddress.setPremises(component.long_name);
                if (resultAddress.getFeatureName() == null)
                    resultAddress.setFeatureName(component.long_name);
            }

            if (location != null) {
                resultAddress.setLatitude(location.latitude);
                resultAddress.setLongitude(location.longitude);
            }
        }

        return throwable;
    }

    private class AddressComponent {
        String long_name;
        String short_name;
        String[]   types;
    }

    private class Geometry {
        public class Location {
            double      lat;
            double      lng;
        }

        Location location;
    }
}
