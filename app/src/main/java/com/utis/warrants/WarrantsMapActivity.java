package com.utis.warrants;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.utis.warrants.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.utis.warrants.record.LocationRecord;
import com.utis.warrants.record.WarrantRecord;
import com.utis.warrants.tables.EntTable;
import com.utis.warrants.tables.LocationTable;
import com.utis.warrants.tables.WarrantTable;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;


/**
 * This shows how to create a simple activity with a map and a marker on the map.
 * <p>
 * Notice how we deal with the possibility that the Google Play services APK is not
 * installed/enabled/updated on a user's device.
 */
public class WarrantsMapActivity extends FragmentActivity implements OnMarkerClickListener {
	private static final boolean D = true;
	private static final String TAG = "WarrantsMapActivity"; 
	private static final int DEF_ZOOM = 11;
	private static final int SINGLE_LOCATION_ZOOM = 15;
	private static final int SINGLE_ENT_EMP_LOCATION_ZOOM = 12;
	public static final LatLng ODESSA = new LatLng(46.484583, 30.7326);
	public static final int WARRANTS_ENT_MAP_MODE = 1;
	public static final int EMP_LOCATIONS_MAP_MODE = 2;
	public static final int SINGLE_LOCATION_MAP_MODE = 3;
	public static final int SINGLE_ENT_EMP_LOCATION_MAP_MODE = 4;
	private DBSchemaHelper dbSch;
	public Context mContext;
    private ArrayAdapter<WarrantRecord> mDBArrayAdapter;
    private LatLng userLocation;
    private CameraPosition cameraPosition;
    private FollowMeLocationSource followMeLocationSource;
    private int mapMode = WARRANTS_ENT_MAP_MODE;
    private double l_lat, l_lng;
    private String l_date, entNm, entAddr;
    private ProgressDialog mProgressDialog;
    private boolean mWaitGeocoding;
    double geoLng, geoLat;
	
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);         
        setContentView(R.layout.activity_map);
        mContext = this;
     // creates our custom LocationSource and initializes some of its members
//        followMeLocationSource = new FollowMeLocationSource();
        
		Bundle b = getIntent().getExtras();
		mapMode = Integer.parseInt(b.getString("map_mode"));
		if (mapMode == SINGLE_LOCATION_MAP_MODE) {
			l_lat = Double.parseDouble(b.getString("lat"));
			l_lng = Double.parseDouble(b.getString("lng"));
		} else if (mapMode == EMP_LOCATIONS_MAP_MODE) {
			l_date = b.getString("l_date");
		} else if (mapMode == SINGLE_ENT_EMP_LOCATION_MAP_MODE) {
			l_lat = Double.parseDouble(b.getString("lat"));
			l_lng = Double.parseDouble(b.getString("lng"));
			entNm = b.getString("eNm");
			entAddr = b.getString("eAddr");
		}
	    mProgressDialog = new ProgressDialog(this);
	    mProgressDialog.setTitle(getString(R.string.title_activity_map)) ;
		mProgressDialog.setMessage(getString(R.string.m_read_data));
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);			    
        
        setUpMapIfNeeded();
//        setUpMap();  
//        if (dbSch == null){ 
//        	dbSch = new DBSchemaHelper(this);
//        	if (mapMode == WARRANTS_ENT_MAP_MODE) {
//        		mDBArrayAdapter = new ArrayAdapter<WarrantRecord>(this, R.layout.db_data);
//        		showWarrants(dbSch, "");
//        	}
//        }
        
    }

    @Override
    protected void onResume() {
        super.onResume(); 
        /* We query for the best Location Provider everytime this fragment is displayed
         * just in case a better provider might have become available since we last displayed it */
//        followMeLocationSource.getBestAvailableProvider();
        // Get a reference to the map/GoogleMap object
        setUpMapIfNeeded();
        /* Enable the my-location layer (this causes our LocationSource to be automatically activated.)
         * While enabled, the my-location layer continuously draws an indication of a user's
         * current location and bearing, and displays UI controls that allow a user to interact
         * with their location (for example, to enable or disable camera tracking of their location and bearing).*/
//        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onPause() {
        /* Disable the my-location layer (this causes our LocationSource to be automatically deactivated.) */
        mMap.setMyLocationEnabled(false);
        super.onPause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.action_streetview:
        	if (D) Log.d(TAG, "onOptions action_streetview");
//            Intent runWebKitActivity= new Intent(this, WebKitActivity.class);
//            Bundle b = new Bundle();
//            String sLat = Double.toString(userLocation.latitude);
//            sLat.replace(",", ".");
//            String sLng = Double.toString(userLocation.longitude);
//            sLng.replace(",", ".");
//            b.putString("lat", sLat);
//            b.putString("lng", sLng);
//            runWebKitActivity.putExtras(b); 
//            startActivity(runWebKitActivity);

//        	showStreetView(userLocation.latitude, userLocation.longitude);
        	cameraPosition = mMap.getCameraPosition();
        	showStreetView(cameraPosition.target.latitude, cameraPosition.target.longitude);
            return true;
        case R.id.action_google_map:
        	if (D) Log.d(TAG, "onOptions action_google_map");
//        	showGoogleMap(userLocation.latitude, userLocation.longitude);	
        	cameraPosition = mMap.getCameraPosition();        	
        	showGoogleMap(cameraPosition.target.latitude, cameraPosition.target.longitude);
        	return true;
        }
        return false;
    }
    
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
            	// Replace the (default) location source of the my-location layer with our custom LocationSource
                mMap.setLocationSource(followMeLocationSource);
                mMap.setOnMarkerClickListener(this);
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {   
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
//        mMap.addMarker(new MarkerOptions().position(ODESSA).title("Одесса"));
//    	mMap.setStreetView(true);
    	Date routeDate;
    	if (dbSch == null){ 
//    		dbSch = new DBSchemaHelper(this);
    		dbSch = DBSchemaHelper.getInstance(this);
        	switch (mapMode) {
        	case WARRANTS_ENT_MAP_MODE:
        		mDBArrayAdapter = new ArrayAdapter<WarrantRecord>(this, R.layout.db_data);
        		showWarrants(dbSch, "");
        		getCurrentLocation();
        		break;
        	case SINGLE_LOCATION_MAP_MODE:
        		setLocation(l_lat, l_lng, SINGLE_LOCATION_ZOOM);
        		break;
        	case SINGLE_ENT_EMP_LOCATION_MAP_MODE:
        		setEntLocation(l_lat, l_lng, SINGLE_ENT_EMP_LOCATION_ZOOM, entNm, entAddr);
        		getCurrentLocation();
        		CommonClass.copy2Clipboard(entAddr, this);
        		break;
        	case EMP_LOCATIONS_MAP_MODE:
//        		mDBArrayAdapter = new ArrayAdapter<WarrantRecord>(this, R.layout.db_data);
        		//String sWhere = DateFormat.format(new java.sql.Date(new Date().getTime()));
        		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
//        		GregorianCalendar gcalendar = new GregorianCalendar();
//        		gcalendar.add(Calendar.DATE, -1);
        		try {
        			routeDate = formatter.parse(l_date);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					routeDate = new Date();
				}
//        		Calendar calendar = Calendar.getInstance();
//        		calendar.setTime(new Date());
//        		calendar.add(Calendar.DATE, -1);
//        		Date fDate = calendar.getTime();//new Date(); fDate.
        		String sWhere = String.format("l_date between '%s' and '%sZ'",
        				formatter.format(routeDate), formatter.format(routeDate));
        		showTrack(dbSch, sWhere);
        		break;        		
        	}
        }        
    }
    
    private void showStreetView(double latitude, double longitude) {
//		String geoUriStringSV = String.format("google.streetview:cbll=%s,%s&cbp=1,99.56,,1,-5.27&mz=21", 
//				WarrantsMapActivity.ODESSA.latitude, WarrantsMapActivity.ODESSA.longitude);  
    	String uri = "google.streetview:cbll="+ latitude +","+ longitude +"&cbp=1,99.56,,1,-5.27&mz=21";
    	Intent streetView = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    	startActivity(streetView);    	
    }
    
    private void showGoogleMap(double latitude, double longitude) {
	    String geoUriString = String.format("geo:%s,%s?z=15", latitude, longitude);  
	    Uri geoUri = Uri.parse(geoUriString);  
	    Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);  
	    startActivity(mapCall);  
    }
    
    private void showTrack(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getReadableDatabase();
    	Cursor c;
    	LocationRecord location = null;
    	List<LatLng> routePoints = new ArrayList<LatLng>();
    	LatLng mapPoint;
    	
    	if(sWhere.length() == 0) {
    		c = sqdb.rawQuery("SELECT * from " + LocationTable.TABLE_NAME + " ORDER BY " +
    				LocationTable.ID + " ASC", null);
    	} else {
    		c = sqdb.rawQuery("SELECT * from " + LocationTable.TABLE_NAME + " WHERE " + sWhere + 
    				" ORDER BY " + LocationTable.ID + " ASC", null);
    	}
    	int cntr = 0; 
    	while (c.moveToNext()) {
    		cntr++;
    		location = new LocationRecord(c);
    		if((location.lat > 0)&&(location.lng > 0)) {
    			mapPoint = new LatLng(location.lat, location.lng);
    			routePoints.add(mapPoint);
/*    			
    			if (mMap != null) {
    				mMap.addMarker(new MarkerOptions()
    					.position(new LatLng(location.lat, location.lng)));
//    					.title(warrant.entName)
//    					.snippet(warrant.entAddress));    					
    			}
*/    			
    		}
			if (mMap != null) {
				Polyline route = mMap.addPolyline(new PolylineOptions()
				  .width(5)
				  .color(Color.BLUE));
//				  .geodesic(true)
//				  .zIndex(z));
				route.setPoints(routePoints);
			}	
    		
//    		if (D) Log.d(TAG, "Location = " + location);
    	}    	  
    	if (cntr > 0 && location != null)
    		setLocation(location.lat, location.lng, DEF_ZOOM);
    	if (D) Log.d(TAG, "Location Count = " + cntr);
    	if (c != null) c.close();    	
    }
    
    private void setEntLocation(double lat, double lng, int zoom, String EntNm, String EntAddr) {
    	userLocation = new LatLng(lat, lng);
    	mMap.addMarker(new MarkerOptions()
    		.position(userLocation)
    		.title(EntNm)
    		.snippet(EntAddr) 
    		/*.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))*/);
    	CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLocation, zoom);
    	mMap.animateCamera(yourLocation);
    }
    
    private void setLocation(double lat, double lng, int zoom) {
    	userLocation = new LatLng(lat, lng);
    	mMap.addMarker(new MarkerOptions()
    		.position(userLocation)
				.title(getString(R.string.m_myself))
				.snippet(getString(R.string.m_my_position))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    	CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLocation, zoom);
    	mMap.animateCamera(yourLocation);
    }
    
    private void getCurrentLocation() {
    	LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
    	Criteria criteria = new Criteria();
    	String provider = service.getBestProvider(criteria, true/*false*/);
    	Location location = service.getLastKnownLocation(provider);
    	if (location != null) {
	    	userLocation = new LatLng(location.getLatitude(), location.getLongitude());
	    	mMap.addMarker(new MarkerOptions()
	    		.position(userLocation)
					.title(getString(R.string.m_myself))
					.snippet(getString(R.string.m_my_position))
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
	    	CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLocation, DEF_ZOOM);
	    	mMap.animateCamera(yourLocation);
    	}
//    	mMap.getUiSettings().setZoomControlsEnabled(false);
//    	mMap.setMyLocationEnabled(true);
//    	mMap.getUiSettings().setMyLocationButtonEnabled(true);

/*        Location myLocation  = mMap.getMyLocation();
        if(myLocation!= null)
        {
            double dLatitude = myLocation.getLatitude();
            double dLongitude = myLocation.getLongitude();
            Log.i("APPLICATION"," : "+dLatitude);
            Log.i("APPLICATION"," : "+dLongitude);
            mMap.addMarker(new MarkerOptions().position(
//                    new LatLng(dLatitude, dLongitude)).title("My Location").icon(BitmapDescriptorFactory.fromBitmap(Utils.getBitmap("pointer_icon.png"))));
            		new LatLng(dLatitude, dLongitude)).title("My Location"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 8));

        }
        else
        {
            Toast.makeText(this, "Unable to fetch the current location", Toast.LENGTH_SHORT);
        }
*/
    }
    
    private void showWarrants(DBSchemaHelper sh, String sWhere) {
    	GetWarrEntCoordsTask getCoordsTask = new GetWarrEntCoordsTask();
    	getCoordsTask.execute(sWhere);
    	
//    	SQLiteDatabase sqdb = sh.getWritableDatabase();
//    	Cursor c = null;
//    	Cursor cEnt;
//    	WarrantRecord warrant; 
//    	String query;
//    	
//    	if(sWhere == "") {
//    		query = "SELECT * from " + WarrantTable.TABLE_NAME + " ORDER BY " + 
//    				WarrantTable.ID_EXTERNAL + " DESC";
//    	} else {
//    		query = "SELECT * from " + WarrantTable.TABLE_NAME + " WHERE " + sWhere + 
//    				" ORDER BY " + WarrantTable.ID_EXTERNAL;
//    	}
//    	int cntr = 0; int colid;
//		try {
//			c = sqdb.rawQuery(query, null);
//			mDBArrayAdapter.clear();
//	    	while (c.moveToNext()) {
//	    		cntr++;
//	    		warrant = new WarrantRecord(c);
//	
//	    		cEnt = sh.getEntName(warrant.id_ent);
//	    		if (cEnt != null) {
//		    		colid = cEnt.getColumnIndex(EntTable.NM);
//		    		warrant.entName = cEnt.getString(colid);
//		    		if (warrant.id_subent > 0) {
//		    			cEnt.close();
//		    			cEnt = sh.getEntName(warrant.id_subent);
//		    			if (cEnt != null) {
//		    				colid = cEnt.getColumnIndex(EntTable.NM);
//		    				warrant.subEntName = cEnt.getString(colid);
//		    			}
//		    		}
//		    		if (cEnt != null) {
//			    		colid = cEnt.getColumnIndex(EntTable.ADDR);
//			    		warrant.entAddress = cEnt.getString(colid);  			
//			    		colid = cEnt.getColumnIndex(EntTable.LAT);
//			    		warrant.entLat = cEnt.getDouble(colid);  			
//			    		colid = cEnt.getColumnIndex(EntTable.LNG);
//			    		warrant.entLng = cEnt.getDouble(colid);
//			    		cEnt.close();		
//		    		}
//		    		warrant.jobCount = (int) sh.getWarrantContCount(warrant.id_external); 
//		    		if ((warrant.entLat > 0)&&(warrant.entLng > 0)) {
//		    			if (mMap != null) {
//		    				mMap.addMarker(new MarkerOptions()
//		    					.position(new LatLng(warrant.entLat, warrant.entLng))
//		    					.title(warrant.entName)
//		    					.snippet(warrant.entAddress));
//		    			}
//		    		} else {
///*		        		String addrStr = warrant.entAddress + " Одеса";
//		        		String newAddrStr = addrStr.replaceAll(" ", "+");
//		    			sendGeocodeRequest(newAddrStr);
//		    			do {
//		    				SystemClock.sleep(50);
//		    			} while (mWaitGeocoding);
//		    			warrant.entLat = geoLat;
//		    			warrant.entLng = geoLng; */		    			
//		    		}
//	    		}	    		
//	    		if (D) Log.d(TAG, "Warrant = " + warrant.num);
//	    		mDBArrayAdapter.add(warrant);
//	    	}    
//		} catch (Exception e) {
//			if (D) Log.e(TAG, "Exception: " + e.getMessage());
//		} finally {    	
//	    	if (D) Log.d(TAG, "Warrants Count = " + cntr);
//	    	if (c != null) c.close();
//		}
    }
    
    /* Our custom LocationSource. 
     * We register this class to receive location updates from the Location Manager
     * and for that reason we need to also implement the LocationListener interface. */
    private class FollowMeLocationSource implements LocationSource, LocationListener {

        private OnLocationChangedListener mListener;
        private LocationManager locationManager;
        private final Criteria criteria = new Criteria();
        private String bestAvailableProvider;
        /* Updates are restricted to one every 10 seconds, and only when
         * movement of more than 10 meters has been detected.*/
        private final int minTime = 10000;     // minimum time interval between location updates, in milliseconds
        private final int minDistance = 10;    // minimum distance between location updates, in meters

        private FollowMeLocationSource() {
            // Get reference to Location Manager
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            // Specify Location Provider criteria
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setAltitudeRequired(true);
            criteria.setBearingRequired(true);
            criteria.setSpeedRequired(true);
            criteria.setCostAllowed(true);
        }

        private void getBestAvailableProvider() {
            /* The preffered way of specifying the location provider (e.g. GPS, NETWORK) to use 
             * is to ask the Location Manager for the one that best satisfies our criteria.
             * By passing the 'true' boolean we ask for the best available (enabled) provider. */
            bestAvailableProvider = locationManager.getBestProvider(criteria, true);
        }

        /* Activates this provider. This provider will notify the supplied listener
         * periodically, until you call deactivate().
         * This method is automatically invoked by enabling my-location layer. */
        @Override
        public void activate(OnLocationChangedListener listener) {
            // We need to keep a reference to my-location layer's listener so we can push forward
            // location updates to it when we receive them from Location Manager.
            mListener = listener;
            // Request location updates from Location Manager
            if (bestAvailableProvider != null) {
                locationManager.requestLocationUpdates(bestAvailableProvider, minTime, minDistance, this);
            } else {
                // (Display a message/dialog) No Location Providers currently available.
            }
        }

        /* Deactivates this provider.
         * This method is automatically invoked by disabling my-location layer. */
        @Override
        public void deactivate() {
            // Remove location updates from Location Manager
            locationManager.removeUpdates(this);
            mListener = null;
        }

        @Override
        public void onLocationChanged(Location location) {
            /* Push location updates to the registered listener..
             * (this ensures that my-location layer will set the blue dot at the new/received location) */
            if (mListener != null) {
                mListener.onLocationChanged(location);
            }
            /* ..and Animate camera to center on that location !
             * (the reason for we created this custom Location Source !) */
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
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
    }

    private class GetWarrEntCoordsTask extends AsyncTask<String, Integer, Boolean> {
    	SQLiteDatabase sqdb = dbSch.getWritableDatabase();
    	Cursor c = null;
    	Cursor cEnt;
    	WarrantRecord warrant; 
    	int cntr;
    	int recCount = 0;
    	
    	private void getLatLongFromAddress(String youraddress) {
    	    String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
    	                  youraddress + "&sensor=false";
    	    HttpGet httpGet = new HttpGet(uri);
    	    HttpClient client = new DefaultHttpClient();
    	    HttpResponse response;
    	    StringBuilder stringBuilder = new StringBuilder();

    	    try {
    	        response = client.execute(httpGet);
    	        HttpEntity entity = response.getEntity();
    	        InputStream stream = entity.getContent();
    	        int b;
    	        while ((b = stream.read()) != -1) {
    	            stringBuilder.append((char) b);
    	        }
    	    } catch (ClientProtocolException e) {
    	    	if (D) Log.e(TAG, "Exception: " + e.getMessage());
    	    } catch (IOException e) {
    	    	if (D) Log.e(TAG, "Exception: " + e.getMessage());
    	    }

    	    JSONObject jsonObject = new JSONObject();
    	    try {
    	        jsonObject = new JSONObject(stringBuilder.toString());

    	        geoLng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
    	            .getJSONObject("geometry").getJSONObject("location")
    	            .getDouble("lng");

    	        geoLat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
    	            .getJSONObject("geometry").getJSONObject("location")
    	            .getDouble("lat");

    	        Log.d("latitude", Double.toString(geoLat));
    	        Log.d("longitude", Double.toString(geoLng));
    	    } catch (JSONException e) {
    	    	if (D) Log.e(TAG, "Exception: " + e.getMessage());
    	    }
    	}
    	
    	
    	@Override
    	protected Boolean doInBackground(String... strWhere) {
        	String sWhere = strWhere[0];     
        	String query;
        	Long idEnt = 0L;
        	if (sWhere.length() == 0) {
        		query = "SELECT * from " + WarrantTable.TABLE_NAME + " ORDER BY " +
        				WarrantTable.ID_EXTERNAL + " DESC";
        	} else {
        		query = "SELECT * from " + WarrantTable.TABLE_NAME + " WHERE " + sWhere + 
        				" ORDER BY " + WarrantTable.ID_EXTERNAL;
        	}
        	cntr = 0; int colid;
    		try {
    			c = sqdb.rawQuery(query, null);
    			mDBArrayAdapter.clear();
    	    	while (c.moveToNext()) {
    	    		if (cntr == 0) recCount = c.getCount();
    	    		cntr++;
    	    		warrant = new WarrantRecord(c);
    	
    	    		cEnt = dbSch.getEntName(warrant.id_ent);
    	    		if (cEnt != null) {
    		    		colid = cEnt.getColumnIndex(EntTable.NM);
    		    		warrant.entName = cEnt.getString(colid);
    		    		if (warrant.id_subent > 0) {
    		    			cEnt.close();
    		    			cEnt = dbSch.getEntName(warrant.id_subent);
    		    			if (cEnt != null) {
    		    				colid = cEnt.getColumnIndex(EntTable.NM);
    		    				warrant.subEntName = cEnt.getString(colid);
    		    			}
    		    		}
    		    		if (cEnt != null) {
    		    			colid = cEnt.getColumnIndex(EntTable.ID_EXTERNAL);
    		    			idEnt = cEnt.getLong(colid);
    			    		colid = cEnt.getColumnIndex(EntTable.ADDR);
    			    		warrant.entAddress = cEnt.getString(colid);  			
    			    		colid = cEnt.getColumnIndex(EntTable.LAT);
    			    		warrant.entLat = cEnt.getDouble(colid);  			
    			    		colid = cEnt.getColumnIndex(EntTable.LNG);
    			    		warrant.entLng = cEnt.getDouble(colid);
    			    		cEnt.close();		
    		    		}
    		    		warrant.jobCount = (int) dbSch.getWarrantContCount(warrant.id_external);
    		    		if (((warrant.entLat == 0)||(warrant.entLng == 0))&& warrant.entAddress.length() > 0) {
    		        		String addrStr = warrant.entAddress + getString(R.string.m_city_odesa);
    		        		String newAddrStr = addrStr.replaceAll(" ", "+");
    		    			getLatLongFromAddress(newAddrStr);
    		    			warrant.entLat = geoLat;
    		    			warrant.entLng = geoLng;
    		    			dbSch.setEntCoords(idEnt, warrant.entLat, warrant.entLng);
    		    		}
    	    		}
    	    		publishProgress((int) (cntr * 100 / recCount));
    	    		if (D) Log.d(TAG, "Warrant = " + warrant.num);
    	    		mDBArrayAdapter.add(warrant);
    	    	}    
    		} catch (Exception e) {
    			String err =  e.getMessage();
    			if (D) Log.e(TAG, "Exception: " + e.getMessage());
    		} finally {    	
    	    	if (D) Log.d(TAG, "Count = " + cntr);
    	    	if (c != null) c.close();
    		}
        	return true;    	
    	}
    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			mProgressDialog.setProgress(progress[0]);
		}
    	
    	protected void onPostExecute(Boolean success) {
			if (D) Log.d(TAG, "onPostExecute");
	    	if (mProgressDialog != null) {
	    		mProgressDialog.setProgress(0);
	    		mProgressDialog.dismiss(); 
	    	}
	    	for (int i = 0 ; i < mDBArrayAdapter.getCount() ; i++) { 	    		
	    		warrant = mDBArrayAdapter.getItem(i);
	    		if ((warrant.entLat > 0)&&(warrant.entLng > 0)) {
	    			if (mMap != null) {
	    				mMap.addMarker(new MarkerOptions()
	    					.position(new LatLng(warrant.entLat, warrant.entLng))
	    					.title(warrant.entName)
	    					.snippet(warrant.entAddress));
	    			}
	    		}
	    	}	    	
    	}
    }

	@Override
	public boolean onMarkerClick(Marker marker) {
		CommonClass.copy2Clipboard(marker.getSnippet(), this);
		return false;
	}
        
    
}
