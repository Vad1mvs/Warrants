package com.utis.warrants.location;
import java.util.Date;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.R;
import com.utis.warrants.record.LogsRecord;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class MyGLocationService extends Service implements
		LocationListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private static final boolean D = true;
	private static final String TAG = "MyGLocationService";
	private boolean isWorking = false;
	private DBSchemaHelper dbSch;
    private Context mContext;
	private boolean keepLog;
	private GPSTracker mGPS;
	public boolean sendToasts = false;

	// A request to connect to Location Services
    private LocationRequest mLocationRequest;
    // Stores the current instantiation of the location client in this object
    private GoogleApiClient mLocationClient;
    private String connectionStatus;
    private String connectionState;
	
	/* Service Access Methods */ 
	public class ConnectionBinder extends Binder { 
		public MyGLocationService getService() {
			return MyGLocationService.this; 
		} 
	} 
	
	private final IBinder binder = new ConnectionBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

//	@Override
//	public IBinder onBind(Intent arg0) {
//		return null;
//	}
	
	@Override 
	public void onCreate() {
		if (D) Log.d(TAG, "GLocation Service created");
		mContext = this;
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
		
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        // Create a new location client, using the enclosing class to handle callbacks.
		mLocationClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		mGPS = new GPSTracker(this, false);
	}
	
	public boolean canGetLocation() {
	    return mGPS.canGetLocation();
	}	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		keepLog = sharedPrefs.getBoolean("logging_checkbox", true);
		String loc_man = sharedPrefs.getString("location_provider_list", "NULL");
//		String[] lm_array = mContext.getResources().getStringArray(R.array.pref_location_provider_list_titles);
		
/*		
		String provider;
		int l_m;
		try {
			l_m = Integer.parseInt(loc_man);
		} catch (NumberFormatException e) {
			l_m = 0;
		}		
		if (l_m == 0) 
			provider = LocationManager.NETWORK_PROVIDER;
		else
			provider = LocationManager.GPS_PROVIDER;
		
		String mLog = "Location Service Started";
		if (sendToasts) Toast.makeText(this, mLog, Toast.LENGTH_LONG).show();
		if (D) Log.d(TAG, mLog);
		//---use the LocationManager class to obtain locations data---
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Intent i = new Intent(this, MyLocationReceiver.class);
		pendingIntent = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		//---request for location updates using GPS---
		lm.requestLocationUpdates(
			provider,	
//			LocationManager.NETWORK_PROVIDER, 
//			LocationManager.GPS_PROVIDER, 
			15000,  // time in msec
			50,    // distance in m
			pendingIntent);
		if (keepLog) dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog + "; " + provider);
*/		
		int accuracy;
		int l_m;
		try {
			l_m = Integer.parseInt(loc_man);
		} catch (NumberFormatException e) {
			l_m = 0;
		}		
		
        // Create a new global location parameters object
//      mLocationRequest = LocationRequest.create();
		if (l_m == 0) { 
			accuracy = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
	        // Set the update interval
	        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS_1);
	        // Set the interval ceiling to one minute
	        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS_1);
	        mLocationRequest.setSmallestDisplacement(LocationUtils.DISPLACEMENT_1);
		} else {
			// Use high accuracy
			accuracy = LocationRequest.PRIORITY_HIGH_ACCURACY;
	        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS_2);
	        // Set the interval ceiling to one minute
	        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS_2);
	        mLocationRequest.setSmallestDisplacement(LocationUtils.DISPLACEMENT_2);
		}
        mLocationRequest.setPriority(accuracy);        
        // Create a new location client, using the enclosing class to handle callbacks.
//        mLocationClient = new LocationClient(this, this, this);
	
        String mLog = "GLocation Service Started";
        if (keepLog) dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog);
        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();
		
		isWorking = true;
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		String mLog = "GLocation Service Destroyed";
        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
			if (keepLog) dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog);
			if (sendToasts) Toast.makeText(this, mLog, Toast.LENGTH_LONG).show();
        }
        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();	
		
		isWorking = false;
		super.onDestroy();		 
		if (D) Log.d(TAG, mLog);
	}

	public boolean isWorking() { 
		return isWorking; 
	}
	
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            String mLog = getString(R.string.play_services_available);
            Log.d(LocationUtils.APPTAG, mLog);
            if (keepLog) dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog);
            
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
/*        	
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
*/            
            return false;
        }
    }
	
	
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        connectionStatus = mContext.getResources().getString(R.string.connected);
        String mLog = "GLocation Service " + connectionStatus;
        Log.d(LocationUtils.APPTAG, mLog);
        if (keepLog) dbSch.addLogItem(LogsRecord.WARNING, new Date(), mLog);
        startPeriodicUpdates();
    }

	@Override
	public void onConnectionSuspended(int i) {
		connectionStatus = mContext.getResources().getString(R.string.suspended);
		String mLog = "GLocation Service " + connectionStatus;
		Log.d(LocationUtils.APPTAG, mLog);
		if (keepLog) dbSch.addLogItem(LogsRecord.WARNING, new Date(), mLog);
	}

    // Called by Location Services if the attempt to Location Services fails.
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
/*    	
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                        
            // Thrown if Google Play services canceled the original PendingIntent
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());        	
        }
*/        
    	String mLog = "MyGLocationService->ConnectionFailed: " + connectionResult.getErrorCode();
		if (D) Log.d(TAG, mLog);
		dbSch.addLogItem(LogsRecord.WARNING, new Date(), mLog);		        
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        connectionStatus = mContext.getResources().getString(R.string.location_updated);
        // set the latitude and longitude to the value received
		String locMsg = "Location changed: Lat: " + location.getLatitude() +
				" Lng: " + location.getLongitude();
//		Toast.makeText(context, locMsg, Toast.LENGTH_SHORT).show();
		if (D) Log.d(TAG, locMsg);
		dbSch.addLogItem(LogsRecord.WARNING, new Date(), locMsg);		
		dbSch.addLocationItem(location.getLatitude(), location.getLongitude(), new Date());
//		dbSch.close();
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
	private void startPeriodicUpdates() {
//        mLocationClient.requestLocationUpdates(mLocationRequest, this);
		LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
		connectionState = mContext.getResources().getString(R.string.location_requested);
	}

	// In response to a request to stop updates, send a request to Location Services
	private void stopPeriodicUpdates() {
//        mLocationClient.removeLocationUpdates(this);
		LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
		connectionState = mContext.getResources().getString(R.string.location_updates_stopped);
	}
        
    public String ConnectionStatus() {
    	return connectionStatus;
    }
    
    public String ConnectionState() {
    	return connectionState;
    }

    // Calls getLastLocation() to get the current location
    public Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
			return LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        } else return null;
    }
    
    
}
