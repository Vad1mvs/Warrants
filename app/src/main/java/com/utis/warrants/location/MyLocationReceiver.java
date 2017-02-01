package com.utis.warrants.location;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.record.LogsRecord;

public class MyLocationReceiver extends BroadcastReceiver { 
	private static final boolean D = true;
	private static final String TAG = "MyLocationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
//		DBSchemaHelper dbSch = new DBSchemaHelper(context);
		DBSchemaHelper dbSch = DBSchemaHelper.getInstance(context);
		String locationKey = LocationManager.KEY_LOCATION_CHANGED;
		String providerEnabledKey = LocationManager.KEY_PROVIDER_ENABLED;
		if (intent.hasExtra(providerEnabledKey)) {
			String mLog;
			if (!intent.getBooleanExtra(providerEnabledKey, true)) {
				mLog = "Provider disabled";
			} else {
				mLog = "Provider enabled";
			}
			Toast.makeText(context, mLog, Toast.LENGTH_SHORT).show();
			if (D) Log.d(TAG, mLog);
			dbSch.addLogItem(LogsRecord.WARNING, new Date(), mLog);
		}
		if (intent.hasExtra(locationKey)) {
			Location loc = (Location)intent.getExtras().get(locationKey);
			String locMsg = "Location changed : Lat: " + loc.getLatitude() +
					" Lng: " + loc.getLongitude();
//			Toast.makeText(context, locMsg, Toast.LENGTH_SHORT).show();
			if (D) Log.d(TAG, locMsg);
			dbSch.addLogItem(LogsRecord.WARNING, new Date(), locMsg);
			
//			DBAdapter db = new DBAdapter(context);
//			db.open();
//			db.insertLocation(String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()));
//			db.close();
			dbSch.addLocationItem(loc.getLatitude(), loc.getLongitude(), new Date());
			dbSch.close();
		} else {
			if (D) Log.d(TAG, "No LocationManager.KEY_LOCATION_CHANGED");
		}
	}

}
