/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.conestogac.msd.gpsuploader;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseObject;

import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = this.getClass().getSimpleName();
    TextView testViewStatus;
    TextView textViewLatitude;
    TextView textViewLongitude;
    TextView textViewTargetLatitude;
    TextView textViewTargetLongitude;

    TextView textViewSeekDistance;
    TextView textViewdirection;
    //Spinner
    SeekBar seekBarDistance=null;
    Integer distance = 1000;
    double targetLatitude;
    double targetLongitude;


    LocationManager myLocationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    String locationProvider = LocationManager.GPS_PROVIDER;
    Location currentLocation = new Location(locationProvider);
    //Or use LocationManager.NETWORK_PROVIDER;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        initTextViews();

        myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        initSeekBar();
    }

    private void initSeekBar() {
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = progress;
                displayTarget(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarDistance.setProgress(distance);
        textViewSeekDistance.setText(String.valueOf(distance));
    }

    private void displayTarget(int progress) {
        textViewSeekDistance.setText(String.valueOf(progress));
        setTargetLocation(progress, 60.0);  //todo degree must get from compass
        textViewTargetLatitude.setText(getResources().getString(R.string.latPrefix) + String.format("%1$,.6f",targetLatitude));
        textViewTargetLongitude.setText(getResources().getString(R.string.lonPrefix) + String.format("%1$,.6f",targetLongitude));
    }

    private void initTextViews() {
        testViewStatus = (TextView) findViewById(R.id.tv_status);
        textViewLatitude = (TextView) findViewById(R.id.tv_latitude);
        textViewLongitude = (TextView) findViewById(R.id.tv_longitude);

        seekBarDistance = (SeekBar) findViewById(R.id.sb_distance);
        textViewSeekDistance = (TextView) findViewById(R.id.tv_seekdistance);
        textViewdirection = (TextView) findViewById(R.id.tv_seekdirection);

        textViewTargetLatitude = (TextView) findViewById(R.id.tv_targetlatitude);
        textViewTargetLongitude = (TextView) findViewById(R.id.tv_targetlongitude);
    }

    public void onClick(View v) {
        Context context = getApplicationContext();
        CharSequence text;
        if (currentLocation != null) {
            text = "Sent!";
            uploadTargetLocation();
        } else {
            text = "Fail to send data due to Out of Service!";
        }
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onPause() {
// TODO Auto-generated method stub
        super.onPause();
        // Remove the listener you previously added
        myLocationManager.removeUpdates(myLocationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //To update status service, when foreground, this code block is moved from onCreate()
        isGPSEnabled = myLocationManager.isProviderEnabled(myLocationManager.GPS_PROVIDER);
        isNetworkEnabled = myLocationManager.isProviderEnabled(myLocationManager.NETWORK_PROVIDER);
        if(!isGPSEnabled && !isNetworkEnabled) {
            Context context = getApplicationContext();
            CharSequence text = "Please enable location service at setting menu!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            //first try to get GPS provider then Check Network Provider, because GPS provider has higher accuracy
            if (isGPSEnabled) {
                Log.d(TAG,"Use NETWORK_PROVIDER" );
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else {
                Log.d(TAG,"Use GPS_PROVIDER" );
                locationProvider = LocationManager.GPS_PROVIDER;
            }
            //start with last known location
            currentLocation = this.getLastKnownLocation();
            showMyLocation(currentLocation);
            displayTarget(distance);
        }
        myLocationManager.requestLocationUpdates(
                locationProvider, //provider
                MIN_TIME_BW_UPDATES, //minTime
                MIN_DISTANCE_CHANGE_FOR_UPDATES, //minDistance
                myLocationListener); //LocationListener
    }


    private void showMyLocation(Location l) {
        if (l == null) {
            testViewStatus.setText(this.getString(R.string.outofservice));
        } else {
            testViewStatus.setText(this.getString(R.string.inservice));
            textViewLatitude.setText(this.getString(R.string.latPrefix) + String.format("%1$,.6f", l.getLatitude()));
            textViewLongitude.setText(this.getString(R.string.lonPrefix) + String.format("%1$,.6f", l.getLongitude()));
        }
    }

    private LocationListener myLocationListener
            = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isBetterLocation(location,currentLocation)){
                currentLocation = location;
                showMyLocation(currentLocation);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
// TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
// TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
// TODO Auto-generated method stub
        }
    };

    //Maintaining a current best estimate
    //Check if the location retrieved is significantly newer than the previous estimate.
    //Check if the accuracy claimed by the location is better or worse than the previous estimate.
    //Check which provider the new location is from and determine if you trust it more.
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;

            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private Location getLastKnownLocation() {

        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = myLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {

            Location l = myLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
            // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void setTargetLocation(Integer distance, Double degree) {
        double deltaDistLongitude;
        double deltaDistLatitude;
        final double PI = 3.141592;
        final double ratioDist2Degree = 111000;

        deltaDistLongitude = (double)distance * Math.cos(Math.toRadians(degree));
        deltaDistLatitude =(double)distance * Math.sin(Math.toRadians(degree));
        if (currentLocation != null) {
            targetLatitude = currentLocation.getLatitude() + deltaDistLatitude / ratioDist2Degree;
            targetLongitude = currentLocation.getLongitude() + deltaDistLongitude / ratioDist2Degree;
        }
    }

    private void uploadTargetLocation() {
        ParseObject testObject = new ParseObject("GPS");
        testObject.put("lat",  Math.floor(targetLatitude * 1e6) / 1e6);
        testObject.put("lon", Math.floor(targetLongitude * 1e6) / 1e6);
        testObject.saveInBackground();
    }


}


/*
User location : lat_org, lon_org
Distance to target: d
Compass : NE direction -> α >0
Target Location:
lon_target = lon_org + d * cos α
lat_target = lat_org + d * sin α
http://www.ig.utexas.edu/outreach/googleearth/latlong.html
1° ≈ 111 km
0.001° ≈ 111 m
0.00001° ≈ 1 m
*/