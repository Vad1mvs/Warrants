package com.utis.warrants.location;

import java.util.Date;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.record.LogsRecord;

public class MyLocationService extends Service {
	private static final boolean D = true;
	private static final String TAG = "MyLocationService";
	LocationManager lm;
	PendingIntent pendingIntent;
	private boolean isWorking = false;
	private DBSchemaHelper dbSch;
    private Context mContext;
	private boolean keepLog;
	public boolean SendToasts = false;
	
	/* Service Access Methods */ 
	public class ConnectionBinder extends Binder { 
		MyLocationService getService() { 
			return MyLocationService.this; 
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
		if (D) Log.d(TAG, "Location Service created");
		mContext = this;
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		keepLog = sharedPrefs.getBoolean("logging_checkbox", true);
		String loc_man = sharedPrefs.getString("location_provider_list", "NULL");
//		String[] lm_array = mContext.getResources().getStringArray(R.array.pref_location_provider_list_titles);
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
		if (SendToasts) Toast.makeText(this, mLog, Toast.LENGTH_LONG).show(); 
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
		isWorking = true;
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		String mLog = "Location Service Destroyed";
		//---remove the pending intent---
		if (lm != null) {
			lm.removeUpdates(pendingIntent);
			if (keepLog) dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog);
			if (SendToasts) Toast.makeText(this, mLog, Toast.LENGTH_LONG).show();
		}
		isWorking = false;
		super.onDestroy();		 
		if (D) Log.d(TAG, mLog);
	}

	public boolean isWorking() { 
		return isWorking; 
	}

}
