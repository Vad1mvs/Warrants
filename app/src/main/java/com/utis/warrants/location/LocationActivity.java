package com.utis.warrants.location;

import com.utis.warrants.CommonClass;
import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.R;
import com.utis.warrants.R.string;
import com.utis.warrants.WarrantsContentProvider;
import com.utis.warrants.WarrantsMapActivity;
import com.utis.warrants.location.MyGLocationService;
import com.utis.warrants.record.LocationRecord;
import com.utis.warrants.tables.LocationTable;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class LocationActivity extends FragmentActivity implements SwipeRefreshLayout.OnRefreshListener,
	LoaderManager.LoaderCallbacks<Cursor>{
	private static final boolean D = true;
	private static final String TAG = "LocationActivity";
	private static final int URL_LOCATION_LOADER = 1;//"LocationProvider".hashCode();
	private static final String LOCATION_WHERE = "locWhere";

	//private GPSTracker mGPS;
	private TextView caption;
    private DBSchemaHelper dbSch;
    private ListView mDBListView; 
    private String sPosition, mWhere;
    public Context mContext;
    private Spinner dateChooser;
    private SwipeRefreshLayout swipeLayout;
//    private ArrayAdapter<LocationRecord> mDBArrayAdapter;
    private String[] dates;
    private String l_date = "";
    private int chooseCntr = 0, recCntr;
    private CustomLocationAdapter mDBArrayAdapter;
	double mLat;
	double mLong;
	double routeDistance;
	private boolean inProgress;
	private Handler handler;
	SharedPreferences prefs;
    public String[] mFromColumns = {
    	WarrantsContentProvider.LOCATION_MODIF_COLUMN,
    	WarrantsContentProvider.LOCATION_DATE_COLUMN,
    	WarrantsContentProvider.LOCATION_LAT_COLUMN,
    	WarrantsContentProvider.LOCATION_LNG_COLUMN	    	    
    };
    public int[] mToFields = {
       	R.id.locModif,
    	R.id.locTime,
    	R.id.locLat,
    	R.id.locLng
    };
    SimpleCursorAdapter mAdapter;

    MyGLocationService locationConnectionService;
    Intent locationServiceIntent; 
    
    
    private class CustomLocationAdapter extends ArrayAdapter<LocationRecord> {
        private int id;
    	
        public CustomLocationAdapter(Context context, int layout, int resId, 
    			LocationRecord[] items) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, layout, resId, items);
    		id = resId;
    	} 

    	public CustomLocationAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		id = textViewResourceId;
    	}     	
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            View mView = convertView;
//            if (mView == null) {
//                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                mView = vi.inflate(id, null);
//            }
//            TextView tv = (TextView) mView.findViewById(android.R.id.text1);
            
	    	View mView =  super.getView(position, convertView, parent);
	        TextView tv = (TextView) mView.findViewById(android.R.id.text1);
            if (tv != null) {
	            LocationRecord item = getItem(position);
	            if (item.modified == 0) tv.setTextColor(Color.BLACK);
	            else tv.setTextColor(getResources().getColor(R.color.modified));
            }	        
            return mView;
        }
    }
    
    private class SimpleLocationViewBinder implements ViewBinder {
    	private int modified = 0;
    	private TextView txtView;
    	
    	private void setColor(TextView view) {
			switch (modified) {
			case 1:
				view.setTextColor(Color.BLUE);
				break;
			default:
				view.setTextColor(Color.BLACK);
			}    		
    	}
    	 
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndex(WarrantsContentProvider.LOCATION_MODIF_COLUMN)) {
                // If the column is LEVEL_COLUMN then we use custom view.
            	modified = cursor.getInt(columnIndex);
                txtView = (TextView) view;
                txtView.setText("");
                return true;
            } else if (columnIndex == cursor.getColumnIndex(WarrantsContentProvider.LOCATION_LAT_COLUMN)) {
            	double lat = cursor.getDouble(columnIndex);
                txtView = (TextView) view;
                txtView.setText("Широта: "+ lat);
                setColor(txtView);
                return true;
            } else if (columnIndex == cursor.getColumnIndex(WarrantsContentProvider.LOCATION_LNG_COLUMN)) {
            	double lng = cursor.getDouble(columnIndex);
                txtView = (TextView) view;
                txtView.setText("Долгота: "+ lng);
                setColor(txtView);
                return true;
            } else if (columnIndex == cursor.getColumnIndex(WarrantsContentProvider.LOCATION_DATE_COLUMN)) {
            	String date = cursor.getString(columnIndex);
            	int space = date.indexOf(" ");
            	String time = date.substring(space + 1, date.length());
                txtView = (TextView) view;
                txtView.setText(time);
                setColor(txtView);
                return true;
            } 	
            // For others, we simply return false so that the default binding
            // happens.
            return false;
        }
     
    }    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		caption = (TextView)findViewById(R.id.title_text);
		mContext = this;
		handler = new Handler();
		
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
		dateChooser = (Spinner) findViewById(R.id.dateChooser);
		dates = dbSch.getDistinctLocationDates();
		if (dates != null && dates.length > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dates);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        dateChooser.setAdapter(adapter);
	        dateChooser.setSelection(dates.length - 1);
//	        dateChooser.setPrompt("Даты маршрутов");
		} else {
			dateChooser.setVisibility(View.INVISIBLE);
		}
/*		
	    Cursor c = dbSch.getLocationDates();
	    // make an adapter from the cursor
	    String[] from = new String[] {LocationTable.L_DATE};
	    int[] to = new int[] {android.R.id.text1};
	    SimpleCursorAdapter sca = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, from, to);
	    // set layout for activated adapter
	    sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
	    // get xml file spinner and set adapter 		
		dateChooser.setAdapter(sca);
*/		
	    dateChooser.setOnItemSelectedListener(mChooserClickListener);	
/*		
		mGPS = new GPSTracker(this, false);
		if (mGPS.canGetLocation ) {
			mLat = mGPS.getLatitude();
			mLong = mGPS.getLongitude();
			sPosition = String.format(getString(R.string.m_location), mLat, mLong);			
		} else {
			// can't get the location
			sPosition = getString(R.string.m_no_location);
			mGPS.showSettingsAlert();
		}
		caption.setText(sPosition);
		if (D) Log.d(TAG, caption.getText().toString());
*/		
		locationServiceIntent = new Intent(this, MyGLocationService.class);		
		
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
		//mDBArrayAdapter = new ArrayAdapter<LocationRecord>(this, android.R.layout.simple_list_item_1);
//		mDBArrayAdapter = new CustomLocationAdapter(this, android.R.layout.simple_list_item_1);
        mDBListView = (ListView) findViewById(R.id.listViewLocations);
//        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mLocationClickListener);        
        mAdapter = new SimpleCursorAdapter(
	            this,                // Current context
	            R.layout.location_row,  // Layout for a single row
	            null,                // No Cursor yet
	            mFromColumns,        // Cursor columns to use
	            mToFields,           // Layout fields to use
	            0                    // No flags
	    );
        mAdapter.setViewBinder(new SimpleLocationViewBinder());
        mDBListView.setAdapter(mAdapter);
	}

    private ServiceConnection locationServiceConnection = new ServiceConnection() { 
    	public void onServiceConnected(ComponentName className, IBinder service) { 
    		locationConnectionService = ((MyGLocationService.ConnectionBinder)service).getService();
    		if (locationConnectionService != null) {
	    		Location location = locationConnectionService.getLocation();
	    		if (location != null) {
		    		mLat = location.getLatitude();
					mLong = location.getLongitude();
					sPosition = String.format(getString(R.string.m_location), mLat, mLong);
	    		} else {
	    			sPosition = getString(R.string.m_no_location);
	    			if (!locationConnectionService.canGetLocation())
	    				CommonClass.showLocationSettingsAlert(mContext);
	    		}
    		} else {
    			sPosition = getString(R.string.m_no_location);
    		}
    		caption.setText(sPosition);
    		if (D) Log.d(TAG, caption.getText().toString());
    	} 
    	
    	public void onServiceDisconnected(ComponentName className) { 
    		locationConnectionService = null; 
    	} 
    };
    
    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        readPrefValues();
//		startService(locationServiceIntent);
        if (locationServiceIntent != null) 
        	bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    } 
    
    @Override 
    public void onPause() { 
    	super.onPause(); 
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(LOCATION_WHERE, mWhere);
		editor.commit();			    	    	
		if (locationConnectionService != null && locationConnectionService.isWorking()) {
			unbindService(locationServiceConnection);
		}
    }        
    
	private void readPrefValues() {
		prefs = getSharedPreferences(CommonClass.PREF_NAME, MODE_PRIVATE);
		mWhere = prefs.getString(LOCATION_WHERE, "");
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_disable_location_service);
		if (mi != null && locationConnectionService != null) {
			if (locationConnectionService.canGetLocation()) {
				if (locationConnectionService.isWorking()) 
					mi.setTitle(mContext.getString(string.m_disable_location_service));
				else
					mi.setTitle(mContext.getString(string.m_enable_location_service));
			} else mi.setEnabled(false);
		} else mi.setEnabled(false);
		mi = menu.findItem(R.id.action_current_location);
		if (mi != null) 
			mi.setVisible(locationConnectionService != null && locationConnectionService.canGetLocation());
	    return true;
	}
	
	@Override	
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_locations_showmap:
        	showRoute(l_date);
        	return true;       	
        case R.id.action_current_location:
            ShowLocationOnMap(mLat, mLong);
        	return true;       	
        case R.id.action_disable_location_service:
        	disableLocationService();
        	return true;       	
        case R.id.action_refresh_list:
        	refreshLocations(true);
        	return true;       	
        }
        return false;
	}
    
    private void disableLocationService() {
		if (locationConnectionService != null && locationConnectionService.isWorking()) {    	
			unbindService(locationServiceConnection);
			stopService(locationServiceIntent); 
		} else {
			startService(locationServiceIntent); 
			bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);			
		}
    }	
    
    private void refreshLocations(boolean refresh) {
    	String sWhere;
    	if (dateChooser.getSelectedItem() != null) {
    		l_date = dateChooser.getSelectedItem().toString();
    		sWhere = "SUBSTR("+ LocationTable.L_DATE + ",1,10) = \""+ l_date + "\"";
    	} else sWhere = "";
    	
		if (!inProgress) {
//	     	try {
//	     		Date date = dbSch.dateFormatDay.parse(l_date); 
//	     		String logDateYY = dbSch.dateFormatDayYY.format(date);
//		    	sWhere = "SUBSTR("+ LocationTable.L_DATE + ",1,10) = \""+ logDateYY + "\"";
//		    	if (sameDay && !forced_clear)
//		    		sWhere += " and " + LogsTable.ID + " > " + maxId;
		    	
		    	getLocationLoaderResolver(sWhere, refresh);
//	     	} catch (ParseException e) {
//	 			if (D) Log.e(TAG, "Exception: " + e.getMessage());
//	     	}		    		    				   	    								
		}
//    	ShowLocations(dbSch, sWhere);    	
    }
	
	private void getLocationLoaderResolver(String sWhere, boolean refresh) {		
		android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
		Bundle args = null;
		if (mWhere != null && mWhere.equals(sWhere) && !refresh) {
			loaderManager.initLoader(URL_LOCATION_LOADER, args, this);
		} else {
			mWhere = sWhere;
			loaderManager.restartLoader(URL_LOCATION_LOADER, args, this);
		}
	}
	 	
    private OnItemSelectedListener mChooserClickListener = new OnItemSelectedListener() {
        @Override
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	refreshLocations(false);
/*        	
        	l_date = dateChooser.getSelectedItem().toString();
//            Toast.makeText(mContext, "Selected item =" + curDate, Toast.LENGTH_LONG).show();
//        	if (chooseCntr > 0)       		
//        		showRoute(l_date);
//        	chooseCntr++;
        	String sWhere = "SUBSTR("+ LocationTable.L_DATE + ",1,10) = \""+ l_date + "\"";
        	ShowLocations(dbSch, sWhere);*/
        }

        @Override
    	public void onNothingSelected(AdapterView<?> parent) {}
    };
    
	private void showRoute(String routeDate) {
		if (routeDate == "") {
            ShowLocationOnMap(mLat, mLong);			
		} else {
			Intent runMapActivity = new Intent(mContext, WarrantsMapActivity.class);
	        Bundle b = new Bundle();
	        b.putString("map_mode", Integer.toString(WarrantsMapActivity.EMP_LOCATIONS_MAP_MODE));
	        b.putString("l_date", routeDate);
	        runMapActivity.putExtras(b);            
	        startActivity(runMapActivity);
		}
	}
	
    // The on-click listener for ListViews
    private OnItemClickListener mLocationClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View view, int position, long id) {
        	int sp = 0;
        	String sLat = ((TextView) view.findViewById(R.id.locLat)).getText().toString();
        	String sLng = ((TextView) view.findViewById(R.id.locLng)).getText().toString();
        	sp = sLat.indexOf(" ");
        	sLat = sLat.substring(sp+1, sLat.length());
        	double lat = Double.parseDouble(sLat);
        	sp = sLng.indexOf(" ");
        	sLng = sLng.substring(sp+1, sLng.length());
        	double lng = Double.parseDouble(sLng);
        	ShowLocationOnMap(lat, lng);
        	
//        	LocationRecord location;
//        	location = mDBArrayAdapter.getItem(arg2);
//            if (D) Log.d(TAG, "Location= " + location);
//            ShowLocationOnMap(location.lat, location.lng);
        }
    };    
    
    private void ShowLocationOnMap(double lat, double lng) {
		Intent runMapActivity = new Intent(mContext, WarrantsMapActivity.class);
        Bundle b = new Bundle();
        b.putString("map_mode", Integer.toString(WarrantsMapActivity.SINGLE_LOCATION_MAP_MODE));
        b.putString("lat", Double.toString(lat));
        b.putString("lng", Double.toString(lng));
        runMapActivity.putExtras(b);            
        startActivity(runMapActivity);                	
    }
	
    private void ShowLocations(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;
    	LocationRecord location;
    	double preLat = 0;
    	double preLng = 0;
    	double routeDistance = 0;
    	String query;
    	    	
    	if (sWhere == "") {
    		query = "SELECT * from " + LocationTable.TABLE_NAME + " ORDER BY " + 
    				LocationTable.L_DATE + " DESC";    		
    	} else {
    		query = "SELECT * from " + LocationTable.TABLE_NAME + " WHERE " + sWhere + 
    				" ORDER BY " + LocationTable.L_DATE + " DESC";
    	}
    	int cntr = 0; 		
		try {
			c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		location = new LocationRecord(c);
	    		if (cntr > 0) {
	    			routeDistance += distance(preLat, preLng, location.lat, location.lng);
	    		}
	    		preLat = location.lat;
	    		preLng = location.lng;
	//    		if (D) Log.d(TAG, "Msg = " + msg.msg);
	    		mDBArrayAdapter.add(location);
	    		cntr++;
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {    	
	    	if (D) Log.d(TAG, "Locations Count = " + cntr);
			caption.setText(String.format(getString(string.m_location_dist), sPosition, cntr, routeDistance));
	    	if (c != null) c.close();
	    	swipeLayout.setRefreshing(false);
		}
    }
    
//    public void onStart(View view) { 
//    	startService(new Intent(getBaseContext(), MyLocationService.class));
//    }
//    
//    public void onStop(View view) {
//    	stopService(new Intent(getBaseContext(), MyLocationService.class)); 
//    } 
//    
//    public void onMap(View view) {
//    	showRoute(l_date);
//    }
    
	private void getLocationsLoaderResolver(String sWhere, boolean refresh) {		
		android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
		Bundle args = null;
		if (mWhere != null && mWhere.equals(sWhere) && !refresh) {
			loaderManager.initLoader(URL_LOCATION_LOADER, args, this);
		} else {
			mWhere = sWhere;
			loaderManager.restartLoader(URL_LOCATION_LOADER, args, this);
		}
	}
    
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + 
        		Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        if (dist > 1) dist = 1;
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist *= 111.12; //60 * 1.853159616;
//        dist = dist * 60 * 1.1515;
//        if (unit == "K") {
//          dist = dist * 1.609344;
//        } else if (unit == "N") {
//          dist = dist * 0.8684;
//          }
        return (dist);
      }

      /*::  This function converts decimal degrees to radians             :*/
      private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
      }

      /*::  This function converts radians to decimal degrees             :*/
      private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
      }

	@Override
	public void onRefresh() {
		refreshLocations(true);		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		// Construct the new query in the form of a Cursor Loader. Use the id
		// parameter to construct and return different loaders.
		switch (loaderID) {
		case URL_LOCATION_LOADER:
	    	// Specify the result column projection. Return the minimum set
	    	// of columns required to satisfy your requirements.
			inProgress = true;
	    	String[] result_columns = new String[] { 
		    	WarrantsContentProvider.KEY_ID, 
		    	WarrantsContentProvider.LOCATION_LAT_COLUMN,
		    	WarrantsContentProvider.LOCATION_DATE_COLUMN,
		    	WarrantsContentProvider.LOCATION_LNG_COLUMN,
		    	WarrantsContentProvider.LOCATION_MODIF_COLUMN
		    }; 
	    	String sOrder = " DESC";;
	    	// Specify the where clause that will limit your results.
	    	String where = mWhere;
	    	// Replace these with valid SQL statements as necessary.
	    	String[] whereArgs = null;
	    	String sortOrder = WarrantsContentProvider.KEY_ID + sOrder;
			
			// Query URI
			Uri queryUri = WarrantsContentProvider.CONTENT_LOCATION_URI;
			// Create the new Cursor loader.
			return new CursorLoader(this, queryUri, result_columns, where, whereArgs, sortOrder);
		default:
			return null;
		}
	}

	private void calcDistance(Cursor cursor) {
		int cntr = 0;
		int columnIndexLat = cursor.getColumnIndex(WarrantsContentProvider.LOCATION_LAT_COLUMN);
		int columnIndexLng = cursor.getColumnIndex(WarrantsContentProvider.LOCATION_LNG_COLUMN);
    	double preLat = 0, preLng = 0, lat, lng;
    	routeDistance = 0;
    	if (columnIndexLat >= 0 && columnIndexLng >= 0) {
    		cursor.move(-cursor.getCount());
			while (cursor.moveToNext()) {
	        	lat = cursor.getDouble(columnIndexLat);
	        	lng = cursor.getDouble(columnIndexLng);
	    		if (cntr > 0) {
	    			routeDistance += distance(preLat, preLng, lat, lng);
	    		}
	    		preLat = lat;
	    		preLng = lng;
	    		cntr++;
	    	}    		
    	}
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		inProgress = false;				
		calcDistance(cursor);
		recCntr = cursor.getCount();
		handler.post(new Runnable() {
			@Override
			public void run() {
				caption.setText(String.format(String.format(
						getString(string.m_location_dist1), sPosition, recCntr, routeDistance)));
		        swipeLayout.setRefreshing(false);
			}			
		});		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);				
	}
}
