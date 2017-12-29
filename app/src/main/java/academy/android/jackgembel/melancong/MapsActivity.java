package academy.android.jackgembel.melancong;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public Context context;
    GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Marker mCurrLocationMarker;
    private Location mLastLocation;
    private GoogleMap mMap;
    private String[] id, nama, pesan, jam;
    int numData;
    LatLng latLng[];
    Boolean markerD[];
    private Double[] latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    //Fungsi panggil  peta
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLokasi();

        //Memulai Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
//                mMap.setMyLocationEnabled(true);
//                mMap.getUiSettings().setZoomControlsEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
//            mMap.setMyLocationEnabled(true);
//            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    // menginisialisasi Google Play Services Method
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    //memperbarui lokasi terkini secara berkala
    //Location Provider data lokasi dari GPS, sinyal cellular dan jaringan Wi-Fi
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    //izin untuk mengakses lokasi terkini
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        CameraPosition cameraPosition = new CameraPosition.Builder().
                target(new LatLng(latLng.latitude, latLng.longitude)).zoom(16).build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //Buat Marker baru
        MarkerOptions marker = new MarkerOptions()
                .position(latLng)
                .title("Lokasi saya");
        mCurrLocationMarker = mMap.addMarker(marker);
//        Toast.makeText(MapsActivity.this, " HASIL : "+latLng , Toast.LENGTH_SHORT).show();

        //menghentikan pembaruan lokasi
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    //Android 6.0 Marshmallow, aplikasi tidak akan diberi izin apapun pada saat instalasi
    //Permintaan Izin Akses Lokasi
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    // SHOW MARKER PUSAT //
    private void getLokasi() {
//        String url = "http://cms.sibasurya.com:81/android/randy/getmarker/getmarker.php";
        String url = "http://utomodwibudi.xyz/jack/getmarker.php";
//        http://utomodwibudi.xyz/armada/getmarker.php
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url
                , new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                numData = response.length();
                Log.d("DEBUG_", "Parse JSON");
                latLng = new LatLng[numData];
                markerD = new Boolean[numData];
                nama = new String[numData];
                pesan = new String[numData];
                id = new String[numData];
                latitude = new Double[numData];
                longitude = new Double[numData];
                jam = new String[numData];

                for (int i = 0; i < numData; i++) {
                    try {
                        JSONObject data = response.getJSONObject(i);
                        id[i] = data.getString("id");
                        latLng[i] = new LatLng(data.getDouble("latitude"),
                                data.getDouble("longitude"));
                        nama[i] = data.getString("nama");
                        pesan[i] = data.getString("pesan");
                        latitude[i] = data.getDouble("latitude");
                        longitude[i] = data.getDouble("longitude");
                        jam[i] = data.getString("jam");

                        markerD[i] = false;
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng[i])
                                .title(nama[i])
                                .snippet( pesan[i])
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pinwarning)));
                        //rollback balik
//                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setZoomControlsEnabled(true);
                    } catch (JSONException je) {
                    }
                }

//                        ////////////////////fungsi baru/////////////////////////////
//                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                            @Override
//                            public boolean onMarkerClick(final Marker marker) {
//                                Log.d("DEBUG_", "Marker clicked");
//                                for (int i = 0; i < numData; i++) {
//
//                                    if (marker.getTitle().equals(id[i])) {
//                                        if (markerD[i]) {
//                                            Log.d("DEBUG_", "show info");
//                                            AlertDialog.Builder builder= new AlertDialog.Builder(MapsActivity.this);
//                                            builder.setIcon(android.R.drawable.ic_dialog_alert);
//                                            builder.setMessage("Hapus Informasi Ini?");
//                                            //System.out.println("=============================================================");
//                                            //System.out.println(i);
//                                            //System.out.println("=============================================================");
//                                            final Integer x = new Integer(i) ;
//                                            builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    /////////////////////marker.remove();
//                                                    hapus(id[x]);
//                                                    getLokasi();
//                                                    //hapus();
//                                                    /////////////////////////////////////////
//                                                }
//                                            });
//                                            builder.setNegativeButton("Batal", null)
//                                                    .create()
//                                                    .show();
//                                        } else {
//                                            Log.d("DEBUG_", "show info");
//                                            // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.5f));
//                                            markerD[i] = true;
//                                            marker.showInfoWindow();
//                                            Toast ts = Toast.makeText(MapsActivity.this,"Klik 2x untuk menghapus informasi", Toast.LENGTH_LONG);
////                                            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
////                                            builder.setMessage("Hapus Informasi?");
////                                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
////                                                @Override
////                                                public void onClick(DialogInterface dialog, int which) {
////                                                    hapus();
////                                                }
////                                            });
////                                            builder.setNegativeButton("Tidak", null)
////                                            AlertDialog alert = builder.create();
////                                            alert.show();
//                                            TextView v = (TextView) ts.getView().findViewById(android.R.id.message);
//                                            if( v != null)
//                                                v.setGravity(Gravity.CENTER);
//                                            ts.show();
//                                        }
//                                    } else {
//                                        markerD[i] = false;
//                                    }
//                                }
//                                return false;
//                            }
//
//                        });
                    }
                    ///////////////////batas fungsi baru////////////////////////

                } , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setTitle("Error!");
                        builder.setMessage("No Internet Connection");
                        builder.setIcon(android.R.drawable.ic_dialog_alert);
                        builder.setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getLokasi();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
        Volley.newRequestQueue(this).add(request);
    }



}
