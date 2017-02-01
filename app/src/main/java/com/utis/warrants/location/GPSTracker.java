package com.utis.warrants.location;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.utis.warrants.R;

public final class GPSTracker implements LocationListener {
	private final Context mContext;

	// flag for GPS status
	public boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	private boolean canGetLocation = false;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;
	private boolean useLocationUpdates = true;

	public GPSTracker(Context context) {
	    this.mContext = context;
	    getLocation();
	}

	public GPSTracker(Context context, boolean requestLocationUpdates) {
	    this.mContext = context;
	    useLocationUpdates = requestLocationUpdates;
	    getLocation();
	}

	/**
	 * Function to get the user's current location
	 * @return
	 */
	public Location getLocation() {
	    try {
	        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	        //---printout all the location providers---
	        List<String> locationProviders= locationManager.getAllProviders();
	        for(String provider : locationProviders) {
	        	Log.d("LocationProviders", provider);
	        }
	        
	        // getting GPS status
	        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	        Log.v("isGPSEnabled", "=" + isGPSEnabled);

	        // getting network status
	        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	        Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

	        if (isGPSEnabled == false && isNetworkEnabled == false) {
	            // no provider is enabled
	        	
	        } else {
	            this.canGetLocation = true;
	            if (isNetworkEnabled) {
	            	if (useLocationUpdates) {
	            		locationManager.requestLocationUpdates(
	                        LocationManager.NETWORK_PROVIDER,
	                        MIN_TIME_BW_UPDATES,
	                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	            	}
	                Log.d("Network", "Network");
	                if (locationManager != null) {
	                    location = locationManager
	                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                    if (location != null) {
	                        latitude = location.getLatitude();
	                        longitude = location.getLongitude();
	                    }
	                }
	            }
	            // if GPS Enabled get lat/long using GPS Services
	            if (isGPSEnabled) {
	                if (location == null) {
	                	if (useLocationUpdates) {
	                		locationManager.requestLocationUpdates(
	                            LocationManager.GPS_PROVIDER,
	                            MIN_TIME_BW_UPDATES,
	                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                	}
	                    Log.d("GPS Enabled", "GPS Enabled");
	                    if (locationManager != null) {
	                        location = locationManager
	                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                        if (location != null) {
	                            latitude = location.getLatitude();
	                            longitude = location.getLongitude();
	                        }
	                    }
	                }
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
	    if (locationManager != null) {
	        locationManager.removeUpdates(GPSTracker.this);
	    }
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
	    if (location != null) {
	        latitude = location.getLatitude();
	    }
	    return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
	    if (location != null) {
	        longitude = location.getLongitude();
	    }
	    return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        canGetLocation = isGPSEnabled || isNetworkEnabled;		
		
	    return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * launch Settings Options
	 * */
	public void showSettingsAlert() {
	    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

	    // Setting Dialog Title
	    alertDialog.setTitle(mContext.getString(R.string.title_geo_alert));

	    // Setting Dialog Message
	    alertDialog.setMessage(mContext.getString(R.string.m_geo_disabled));

	    // On pressing Settings button
	    alertDialog.setPositiveButton(mContext.getString(R.string.menu_settings),
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                    mContext.startActivity(intent);
	                }
	            });

	    // on pressing cancel button
	    alertDialog.setNegativeButton(mContext.getString(R.string.btn_cancel),
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                    dialog.cancel();
	                }
	            });

	    // Showing Alert Message
	    alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
//		Toast.makeText(getBaseContext(),
//				"Provider: "+ provider + "enabled",
//				Toast.LENGTH_SHORT).show();
	}	

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		String statusString = ""; 
		switch (status) {
			case android.location.LocationProvider.AVAILABLE:
				statusString = "available";
			case android.location.LocationProvider.OUT_OF_SERVICE:
				statusString = "out of service";
			case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
				statusString = "temporarily unavailable";
		} 		
	}
	

}
