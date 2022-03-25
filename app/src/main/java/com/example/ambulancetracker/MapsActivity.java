package com.example.ambulancetracker;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ambulancetracker.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationListener locationListener;
    private LocationManager locationManager;
    SharedPreferences sharedpreferences;

    private final long MIN_TIME = 1000; // 1 second
    private final long MIN_DIST = 5; // 5 Meters

    private LatLng latLng;

    Marker liveMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getSharedPreferences(ConfigSetting.MyPREFERENCES, Context.MODE_PRIVATE);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

    }

    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    public  float distFrom(double lat1, double lng1, double lat2, double lng2)
    {
        // Earth Radius in meters

        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        System.out.println("Distance is : " + dist);
        return dist; // distance in meters
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.



        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {

                try {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if(liveMarker == null){
                        liveMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("My Position"));
                    }
                    else {
                        liveMarker.setPosition(latLng);
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
                    float dis=distFrom(latLng.latitude,latLng.longitude,11.929948,79.806987);
                    Toast.makeText(MapsActivity.this,"Distance from Current loaction is : "+dis,Toast.LENGTH_LONG).show();
                    if(dis<10){
                        RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
                        JSONObject data=new JSONObject();
                        String id=sharedpreferences.getString(ConfigSetting.UserId,"").trim();
                        try {
                            data.put("userId",id);
                            data.put("lat",latLng.latitude);
                            data.put("lang",latLng.longitude);
                            data.put("signalLat","11.929948");
                            data.put("signalLong","79.806987");
                            data.put("dstlang","79.807562");
                            data.put("dstlat","11.930379");
                            data.put("direction","Left");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        JsonObjectRequest request =new JsonObjectRequest(Request.Method.POST, ConfigSetting.host+"/Home/AddLocation/", data,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                            Toast.makeText(MapsActivity.this, response.toString(), Toast.LENGTH_SHORT).show();

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(),error.getMessage().toString(),Toast.LENGTH_LONG).show();
                            }
                        });
                        queue.add(request);

                    }
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }
            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(11.913013, 79.636498),
                        new LatLng(11.913908, 79.636086),
                        new LatLng(11.914076, 79.634368),
                        new LatLng(11.915000, 79.634154),
                        new LatLng(11.915504, 79.634089),
                        new LatLng(11.918258, 79.633342)));
        polyline1.setTag("Hospital Route");
        mMap.addMarker(new MarkerOptions().position(new LatLng(11.918275,79.633939)).title("Hospital 1"));

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline polyline) {
                if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
                    polyline.setPattern(PATTERN_POLYLINE_DOTTED);
                } else {
                    // The default pattern is a solid stroke.
                    polyline.setPattern(null);
                }

                Toast.makeText(MapsActivity.this, "Route type " + polyline.getTag().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}