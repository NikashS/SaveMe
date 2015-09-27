package silentassassins.saveme;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.content.Intent;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import java.util.Timer;


public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient myGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private LocationRequest myLocationRequest;
    //private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        myLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        myGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(myGoogleApiClient, this);
            myGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Default"));
    }


    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(myGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(myGoogleApiClient, myLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        };
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        Boolean withinDistance;
        double currentLatitude = location.getLatitude();                //TO RETURN
        double currentLongitude = location.getLongitude();              //TO RETURN
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        double tjLatitude = 38.818086;
        double tjLongitude = -77.168323;
        //double tjLatitude = 39.0038;   //these are test cases for making sure the markers turn green when within radius from home (when not at TJ)
        //double tjLongitude = -77.3017;
        LatLng tjlatLng = new LatLng(tjLatitude, tjLongitude);

        double radius = 0.4;
        double distance = Math.sqrt(((currentLatitude-tjLatitude)*(currentLatitude-tjLatitude)) + ((currentLongitude - tjLongitude)*(currentLongitude-tjLongitude)));
        if(distance<=radius)
            withinDistance = true;                                      //TO RETURN
        else
            withinDistance = false;


        //finish();

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Current Location");
        if(!withinDistance)
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        else
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mMap.addMarker(options);

        MarkerOptions tjoptions = new MarkerOptions()
                .position(tjlatLng)
                .title("TJHSST");
        if(!withinDistance)
            tjoptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        else
            tjoptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mMap.addMarker(tjoptions);

        String tolog = "" + currentLatitude + " " + currentLongitude;
        tolog += " " + withinDistance;
        Log.d(TAG, tolog);

        LatLng averagelatLng = new LatLng((currentLatitude + tjLatitude)/2, (currentLongitude + tjLongitude)/2);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(averagelatLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        Circle myCircle;
        CircleOptions circle = new CircleOptions()
                .center(tjlatLng)
                .radius(radius * 111045)
                .strokeWidth(3);

        if(!withinDistance)
        {
            circle.strokeColor(Color.RED)
                    .fillColor(0x32ff0000);  //semi-transparent
        }
        else
        {
            circle.strokeColor(Color.GREEN)
                    .fillColor(0x3200CC00);  //semi-transparent
        }

        myCircle = mMap.addCircle(circle);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(options.getPosition());
        builder.include(tjoptions.getPosition());
        builder.include(new LatLng(tjLatitude + radius, tjLongitude));
        builder.include(new LatLng(tjLatitude - radius, tjLongitude));
        builder.include(new LatLng(tjLatitude, tjLongitude + radius));
        builder.include(new LatLng(tjLatitude, tjLongitude - radius));

        LatLngBounds bounds = builder.build();
        int padding = (int)radius;
        padding = 200; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
        //mMap.moveCamera(cu);


        Intent resultIntent = new Intent();
        resultIntent.putExtra("returnedLatitude", currentLatitude);
        resultIntent.putExtra("returnedLongitude", currentLongitude);
        resultIntent.putExtra("returnedWithin", withinDistance);
        setResult(Activity.RESULT_OK, resultIntent);
        //finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);


    }

    public void buttonOnClick(View v)
    {
        //((Button) v).setBackgroundColor(999999);
        PackageManager manager = this.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage("com.zerion.apps.iform.main");
        if(i==null)
            return;
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        this.startActivity(i);
    }
}
