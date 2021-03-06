package com.example.mainactivity;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import static com.example.mainactivity.MainActivity.dataArr;
import static com.example.mainactivity.MainActivity.mapView;


public class GpsTracker extends Service implements LocationListener {

    private final Context mContext;
    Location location;
    double latitude;
    double longitude;
    public static int radius = 700;
    public static MapCircle circleByGPS;

    public static String gender = "both";
    public static int urinal = 0, toilet = 0, handicap = Integer.MAX_VALUE, kid = Integer.MAX_VALUE;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000;
    protected LocationManager locationManager;


    public GpsTracker(Context context) {
        this.mContext = context;
        getLocation();
    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                } else
                    return null;


                if (isNetworkEnabled) {


                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            MainActivity.current_latitude = latitude;
                            MainActivity.current_longitude = longitude;
                        }
                    }
                }


                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                MainActivity.current_latitude = latitude;
                                MainActivity.current_longitude = longitude;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) { }

        return location;
    }

    public double getLatitude()
    {
        if(location != null)
        {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude()
    {
        if(location != null)
        {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        MainActivity.current_latitude = latitude;
        MainActivity.current_longitude = longitude;
        mapView.removeAllPOIItems();
        mapView.removeAllCircles();
        circleByGPS = new MapCircle(
                MapPoint.mapPointWithGeoCoord(latitude, longitude), // center
                radius, // radius
                Color.argb(0, 0, 0, 0), // strokeColor
                Color.argb(30, 0, 0, 255) // fillColor
        );
        mapView.addCircle(circleByGPS);
        circleByGPS.setCenter(MapPoint.mapPointWithGeoCoord(latitude, longitude));
        markerUpdate(latitude, longitude);
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
    public void stopUsingGPS() { if(locationManager != null) { locationManager.removeUpdates(this); } }

    public static void markerUpdate(double latitude, double longitude){
        mapView.removeAllPOIItems();
        for(int i = 0; i< dataArr.length; i++) {
            if (!dataArr[i][17].isEmpty() && !dataArr[i][18].isEmpty()) {
                if (MainActivity.distance(latitude, longitude, Double.parseDouble(dataArr[i][17]), Double.parseDouble(dataArr[i][18])) <= radius && checking(i)) {
                    try{
                        if(handicap == 0 && 0 == Integer.parseInt(dataArr[i][7])+ Integer.parseInt(dataArr[i][8]) + Integer.parseInt(dataArr[i][12])){
                            continue;
                        }
                        if(kid == 0 && 0 == Integer.parseInt(dataArr[i][9]) + Integer.parseInt(dataArr[i][10]) + Integer.parseInt(dataArr[i][13])){
                            continue;
                        }
                        if(toilet == 0 && 0 == Integer.parseInt(dataArr[i][5]) + Integer.parseInt(dataArr[i][11])){
                            continue;
                        }
                        if(urinal == 0 && 0 == Integer.parseInt(dataArr[i][6])){
                            continue;
                        }
                    } catch(Exception e){
                        Log.e("error ", "error : " + e);
                    }

                    MapPOIItem marker = new MapPOIItem();
                    marker.setItemName(dataArr[i][1]);
                    marker.setTag(i);
                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(dataArr[i][17]), Double.parseDouble(dataArr[i][18])));
                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    marker.setCustomImageResourceId(R.drawable.marker_toilet);
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                    mapView.addPOIItem(marker);
                }
            }
        }
    }
    public static boolean checking(int i){
        boolean result = true;
        if(!gender.equals("both")){
            if(gender.equals(dataArr[i][4])){
                result = true;
            } else result = false;
        }
        return result;
    }
}