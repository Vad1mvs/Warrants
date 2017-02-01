package com.utis.warrants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.utis.warrants.R.string;
import com.utis.warrants.RestTask.ResponseCallback;
import com.utis.warrants.record.LogsRecord;
import com.utis.warrants.tables.LogsTable;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class LogsActivity extends FragmentActivity implements ResponseCallback, 
	SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {
	private static final boolean D = true;
	private static final String TAG = "LogsActivity";
	private static final String LOG_LEVEL = "logLevel";
	private static final String LOG_WHERE = "logWhere";
	private static final int URL_LOG_LOADER = 0;
	private TextView caption;
    private DBSchemaHelper dbSch;
    private ListView mDBListView; 
    private String sPosition, mWhere;
    public Context mContext;
    private Spinner dateChooser, viewModeChooser; 
//    private ArrayAdapter<LocationRecord> mDBArrayAdapter;
    private CustomLogsAdapter mDBArrayAdapter;
    private ArrayList<LogsRecord> logsList;
    private SwipeRefreshLayout swipeLayout; 
    private String[] dates;
    private String l_date = "", pre_l_date = "";
    private long maxId = 0;
    private int viewMode, logLevel;
    private String mIMEI, logURI, serverURI, serverMode;
	private SharedPreferences prefs;
    private boolean inProgress, mClearAdapter;
    private static int dkGreen;
    private ProgressDialog mProgress;
    private GetLogsTask mGetLogsTask;
    private SaveLogsTask mSaveLogsTask;
    private String[] viewModeNames;
//    private ArrayAdapter<String> viewModeAdapter;
    private CustomLogSpinAdapter viewModeAdapter;
    private Handler handler;
    private int recCntr;

    public String[] mFromColumns = {
    	WarrantsContentProvider.LOG_LEVEL_COLUMN,
	    WarrantsContentProvider.LOG_DATE_COLUMN,
	    WarrantsContentProvider.LOG_MSG_COLUMN
    };
    public int[] mToFields = {
       	R.id.logLevel,
    	R.id.logTime,
    	R.id.logText
    };
    SimpleCursorAdapter mAdapter;
    
    
    private class CustomLogSpinAdapter extends ArrayAdapter<String> {
  	
		public CustomLogSpinAdapter(Context context, int textViewResourceId, String[] strings) { 
			//Call through to ArrayAdapter implementation 
			super(context, textViewResourceId, strings);
		} 
		
		private void setColor(View mView, int position) {
			 TextView tv = (TextView) mView.findViewById(android.R.id.text1);
			 int lineColor = Color.BLACK;
		     if (tv != null) {
		    	switch (position) {
		    	 case LogsRecord.EXCEPTION:
		    		 lineColor = Color.RED;
		    		 break;
		    	 case LogsRecord.ERROR:
		    		 lineColor = Color.MAGENTA;
		    		 break;
		    	 case LogsRecord.WARNING:
		    		 lineColor = dkGreen /*Color.GREEN*/;
		    		 break;
		    	 case LogsRecord.DEBUG:
		    		 lineColor = Color.BLUE;
		    		 break;
		    	 default:
		    		 lineColor = Color.BLACK;
		    	}
		    	tv.setTextColor(lineColor);
		     }
		}
  	
		  @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			 View mView =  super.getView(position, convertView, parent);
			 setColor(mView, position);
		     return mView;
		}
		  
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
		     View mView = super.getDropDownView(position, convertView, parent);
		     setColor(mView, position);
		     return mView;
		}		  
    }
    
    
    private static class CustomLogsAdapter extends ArrayAdapter<LogsRecord> {
    	private Context context;
    	private int id;
//      private List <String>items ;
  	
    	static class LogViewHolder {
    	    public TextView textLogTime;
    	    public TextView textLogText;
    	}
    	
		public CustomLogsAdapter(Context context, int textViewResourceId) { 
			//Call through to ArrayAdapter implementation 
			super(context, textViewResourceId);
			id = textViewResourceId;
			this.context = context;
		} 

		public CustomLogsAdapter(Context context, int textViewResourceId, ArrayList<LogsRecord> list) { 
			//Call through to ArrayAdapter implementation 
			super(context, textViewResourceId, list);
			id = textViewResourceId;
			this.context = context;
		} 
  			
		  @Override
		public View getView(int position, View convertView, ViewGroup parent) {			  
		  	LogViewHolder viewHolder;
		  	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
			if(row == null) {
				LayoutInflater inflater = (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.log_row, null);
//				row = LayoutInflater.from(getContext()).inflate(R.layout.log_row, parent, false);
				viewHolder = new LogViewHolder();
				viewHolder.textLogTime = (TextView)row.findViewById(R.id.logTime);
				viewHolder.textLogText = (TextView)row.findViewById(R.id.logText);
				row.setTag(viewHolder);
			} else {
				viewHolder = (LogViewHolder) row.getTag();
			}
			LogsRecord item = getItem(position);
			viewHolder.textLogTime.setText("#"+ item.num + "; " + item.getTime());
			viewHolder.textLogText.setText(item.msg); 
			switch (item.level) {
			case LogsRecord.EXCEPTION:
				viewHolder.textLogText.setTextColor(Color.RED);
				 break;
			case LogsRecord.ERROR:
				viewHolder.textLogText.setTextColor(Color.MAGENTA);
				 break;
			case LogsRecord.WARNING:
				viewHolder.textLogText.setTextColor(dkGreen);
				 break;
			case LogsRecord.DEBUG:
				viewHolder.textLogText.setTextColor(Color.BLUE);
				 break;
			default:
				viewHolder.textLogText.setTextColor(Color.BLACK);
			}
			return row; 

		}
    }
    
    private class SimpleLogViewBinder implements ViewBinder {
    	private int level = 0;
    	private TextView txtView;
    	
    	private void setColor(TextView view) {
			switch (level) {
			case LogsRecord.EXCEPTION:
				view.setTextColor(Color.RED);
				 break;
			case LogsRecord.ERROR:
				view.setTextColor(Color.MAGENTA);
				 break;
			case LogsRecord.WARNING:
				view.setTextColor(dkGreen);
				 break;
			case LogsRecord.DEBUG:
				view.setTextColor(Color.BLUE);
				 break;
			default:
				view.setTextColor(Color.BLACK);
			}    		
    	}
    	 
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndex(WarrantsContentProvider.LOG_LEVEL_COLUMN)) {
                // If the column is LEVEL_COLUMN then we use custom view.
                level = cursor.getInt(columnIndex);
                txtView = (TextView) view;
                txtView.setText(""/*+ level*/);
                //setColor(txtView);
                return true;
            } else if (columnIndex == cursor.getColumnIndex(WarrantsContentProvider.LOG_MSG_COLUMN)) {
            	String txt = cursor.getString(columnIndex);
                txtView = (TextView) view;
                txtView.setText(txt);
                setColor(txtView);
                return true;
            } else if (columnIndex == cursor.getColumnIndex(WarrantsContentProvider.LOG_DATE_COLUMN)) {
            	String date = cursor.getString(columnIndex);
            	int space = date.indexOf(" ");
            	String time = date.substring(space + 1, date.length());
                txtView = (TextView) view;
                txtView.setText(time);
                //setColor(txtView);
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
		setContentView(R.layout.activity_logs);
		dkGreen = getResources().getColor(R.color.dkgreen);
		caption = (TextView)findViewById(R.id.title_text);
		mContext = this;		
		handler = new Handler();
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
		logsList = new ArrayList<LogsRecord>();
		
//		mDBArrayAdapter = new CustomLogsAdapter(this, android.R.layout.simple_list_item_1, logsList);
		mDBArrayAdapter = new CustomLogsAdapter(this, R.layout.log_row, logsList);
		TelephonyManager tm = (TelephonyManager) getSystemService(mContext.TELEPHONY_SERVICE);
		mIMEI = tm.getDeviceId();

		viewModeNames = getResources().getStringArray(R.array.log_level_names);
//		viewModeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, viewModeNames);
		viewModeAdapter = new CustomLogSpinAdapter(this, android.R.layout.simple_spinner_item, viewModeNames);
		viewModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		viewModeChooser = (Spinner) findViewById(R.id.viewChooser);
		viewModeChooser.setAdapter(viewModeAdapter);
		viewModeChooser.setOnItemSelectedListener(mViewModeClickListener);

		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
        mDBListView = (ListView) findViewById(R.id.listViewLogs);
//        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mLogsClickListener);
//      mDBListView.setAdapter(mDBArrayAdapter);
                
        mAdapter = new SimpleCursorAdapter(
	            this,                // Current context
	            R.layout.log_row,  // Layout for a single row
	            null,                // No Cursor yet
	            mFromColumns,        // Cursor columns to use
	            mToFields,           // Layout fields to use
	            0                    // No flags
	    );
        mAdapter.setViewBinder(new SimpleLogViewBinder());
        mDBListView.setAdapter(mAdapter);
        
        dateChooser = (Spinner) findViewById(R.id.dateChooser);
		getLogsDates();
	    dateChooser.setOnItemSelectedListener(mChooserClickListener);
	    caption.setText("");
//	    readPrefValues();
//	    viewModeChooser.setSelection(logLevel);
		
	}

	@Override public void onRefresh() {
		showCurrentLogs(true/*false*/, true);
//        new Handler().postDelayed(new Runnable() {
//            @Override public void run() {
//                swipeLayout.setRefreshing(false);
//            }
//        }, 5000);
    }
	
	private void readPrefValues() {
		prefs = getSharedPreferences(CommonClass.PREF_NAME, MODE_PRIVATE);
		logLevel = prefs.getInt(LOG_LEVEL, 0);
		mWhere = prefs.getString(LOG_WHERE, "");
	}	
		
	@Override
	public void onBackPressed() {
		if (logLevel != viewMode){
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putInt(LOG_LEVEL, viewMode);
    		editor.putString(LOG_WHERE, mWhere);
    		editor.commit();			
		}
		super.onBackPressed();
	}
	
    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");        
        serverMode = ConnectionManagerService.getServerMode(this);
		serverURI = ConnectionManagerService.getServerURI(this);
		logURI = serverURI  + ConnectionManagerService.REST_WARRANTS_URI + ConnectionManagerService.LOG_URI;
	    readPrefValues();
	    viewModeChooser.setSelection(logLevel);		
    } 
    
    @Override
    public void onPause() {
    	super.onPause();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(LOG_LEVEL, viewMode);
		editor.putString(LOG_WHERE, mWhere);
		editor.commit();			    	
    	if ((mGetLogsTask != null) && (isFinishing()))
    		mGetLogsTask.cancel(false);	
    	if ((mGetLogsTask != null) && (isFinishing()))
    		mGetLogsTask.cancel(false);	
//		mProgressDialog = null;
    }
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_send_log);
		if (mi != null) { 
			mi.setEnabled(mDBArrayAdapter.getCount() > 0);
		}
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_refresh_logs:
        	if (D) Log.d(TAG, "onOptions action_refresh_logs");
        	showCurrentLogs(false, true); 
        	return true;
        case R.id.action_clear_logs:
        	if (D) Log.d(TAG, "onOptions action_clear_logs");
        	l_date = dateChooser.getSelectedItem().toString();
        	dbSch.clearLogs(l_date);
        	getLogsDates();        	
        	return true;
        case R.id.action_send_log:
        	sendCurrentLogs();
        	return true;
        }
		return false;
	}
	
	private void getLogsDates() {
		dates = dbSch.getDistinctLogsDates();
		if (dates != null && dates.length > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dates);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        dateChooser.setAdapter(adapter);
	        dateChooser.setSelection(dates.length - 1);
		} else {
			dateChooser.setVisibility(View.INVISIBLE);
		}		
	}
	
	private void showCurrentLogs(boolean forced_clear, boolean refresh) {
		boolean sameDay;
		if (!inProgress) {
			String viewModeWhere = "";
	    	l_date = dateChooser.getSelectedItem().toString();
	    	sameDay = (l_date == pre_l_date); 
	    	pre_l_date = l_date;
	    	viewMode = viewModeChooser.getSelectedItemPosition();
	    	switch (viewMode) {
	    	case 0:
	    		viewModeWhere = "";
	    		break;
	    	case 1:
	    		viewModeWhere = " and " + LogsTable.LEVEL + " = " + LogsRecord.EXCEPTION;
	    		break;
	    	case 2:
	    		viewModeWhere = " and " + LogsTable.LEVEL + " = " + LogsRecord.ERROR;
	    		break;
	    	case 3:
	    		viewModeWhere = " and " + LogsTable.LEVEL + " = " + LogsRecord.WARNING;
	    		break;
	    	case 4:
	    		viewModeWhere = " and " + LogsTable.LEVEL + " = " + LogsRecord.DEBUG;
	    		break;
	    	case 5:
	    		viewModeWhere = " and " + LogsTable.LEVEL + " = " + LogsRecord.INFO;
	    		break;
	    	}
	     	try {
	     		Date date = dbSch.dateFormatDay.parse(l_date); 
	     		String logDateYY = dbSch.dateFormatDayYY.format(date);
		    	String sWhere = "SUBSTR("+ LogsTable.MSG_DATE + ",1,10) = \""+ logDateYY + "\"" + viewModeWhere;
		    	if (sameDay && !forced_clear)
		    		sWhere += " and " + LogsTable.ID + " > " + maxId;
		    	
		    	getLogsLoaderResolver(sWhere, !sameDay || forced_clear, refresh);
//		    	getLogsResolver(sWhere, !sameDay || forced_clear);
//		    	showLogs(dbSch, sWhere, !sameDay || forced_clear);	     		
	     	} catch (ParseException e) {
	 			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	     	}		    		    				   	    					
		}
	}
	
	private void getLogsResolver(String sWhere, boolean clearAdapter) {
		String sOrder;
    	// Get the Content Resolver.
    	ContentResolver cr = mContext.getContentResolver();
    	// Specify the result column projection. Return the minimum set
    	// of columns required to satisfy your requirements.
    	String[] result_columns = new String[] { 
	    	WarrantsContentProvider.KEY_ID, 
	    	WarrantsContentProvider.LOG_LEVEL_COLUMN,
	    	WarrantsContentProvider.LOG_DATE_COLUMN,
	    	WarrantsContentProvider.LOG_MSG_COLUMN,
	    	WarrantsContentProvider.LOG_MODIF_COLUMN
	    }; 
		if (clearAdapter) {
			//mDBArrayAdapter.clear();
			logsList.clear();
			sOrder = " DESC";
		} else {
			sOrder = " ASC";
		}
    	// Specify the where clause that will limit your results.
    	String where = sWhere;
    	// Replace these with valid SQL statements as necessary.
    	String whereArgs[] = null;
    	String order = WarrantsContentProvider.KEY_ID + sOrder;
    	// Return the specified rows.
    	Cursor resultCursor = cr.query(WarrantsContentProvider.CONTENT_LOG_URI, 
    			result_columns, where, whereArgs, order);		
    	
    	// Find the index to the column(s) being used.
//    	int LEVELCOLUMN_INDEX = resultCursor.getColumnIndexOrThrow(WarrantsLogsContentProvider.LEVEL_COLUMN);
//    	int MSG_COLUMN_INDEX = resultCursor.getColumnIndexOrThrow(WarrantsLogsContentProvider.MSG_COLUMN);
    	// Iterate over the cursors rows. 
    	// The Cursor is initialized at before first, so we can 
    	// check only if there is a 'next' row available. If the
    	// result Cursor is empty, this will return false.
    	int cntr = 0, recCount = 0, cur_num = 0;
    	LogsRecord log;
    	while (resultCursor.moveToNext()) {
    		log = new LogsRecord(resultCursor);
    		if (cntr == 0) {
    			recCount = resultCursor.getCount();
    			maxId = log.id;
    			cur_num = logsList.size();
    		}    	    		
    		//mDBArrayAdapter.add(log);
    		if (clearAdapter) {
    			log.num = recCount - cntr;
    			logsList.add(log);
    		} else {
    			log.num = cntr + cur_num + 1;
    			logsList.add(0, log);
    		}
    		cntr++;
    	}
    	// Close the Cursor when you�ve finished with it.
    	resultCursor.close();
		caption.setText(String.format(getString(R.string.m_cntr), logsList.size()));
        mDBArrayAdapter.notifyDataSetChanged();
        swipeLayout.setRefreshing(false);    			
	}
	
	private void getLogsLoaderResolver(String sWhere, boolean clearAdapter, boolean refresh) {		
		mClearAdapter = clearAdapter;
		android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
		Bundle args = null;
		if (mWhere != null && mWhere.equals(sWhere) && !refresh) {
			loaderManager.initLoader(URL_LOG_LOADER, args, this);
		} else {
			mWhere = sWhere;
			loaderManager.restartLoader(URL_LOG_LOADER, args, this);
		}
	}
	 
	
	private void sendCurrentLogs() {
    	if (!inProgress) {
    		mSaveLogsTask = new SaveLogsTask();
    		mSaveLogsTask.execute("");
    	}
/*		
		String logs_array, logDate;
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		JSONArray logs = new JSONArray();		
		try {						
			for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
				LogsRecord logItem = mDBArrayAdapter.getItem(i);
			    JSONObject jsonLogObj = new JSONObject();
				jsonLogObj.put("lvl", logItem.level);
				jsonLogObj.put("id_ent", logItem.msg);
				logDate = sDateFormat.format(logItem.m_date);
				jsonLogObj.put("date", logDate);
				logs.put(jsonLogObj);
			}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (logs.length() > 0) {
        		logs_array = logs.toString();
        	} else {
        		logs_array = "";
        	}
        }
*/        
	}
	
    private OnItemSelectedListener mViewModeClickListener = new OnItemSelectedListener() {
        @Override
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	showCurrentLogs(true, false);
        }

        @Override
    	public void onNothingSelected(AdapterView<?> parent) {}
    };
    
    private OnItemSelectedListener mChooserClickListener = new OnItemSelectedListener() {
        @Override
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	showCurrentLogs(true, false);
        }

        @Override
    	public void onNothingSelected(AdapterView<?> parent) {}
    };
    
    // The on-click listener for ListViews
    private OnItemClickListener mLogsClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
//        	LogsRecord log;
//        	log = mDBArrayAdapter.getItem(arg2);
//            if (D) Log.d(TAG, "Log = " + log);            
        }
    };    
	
    private void showLogs(DBSchemaHelper sh, String sWhere, boolean clearAdapter) {
    	if (!inProgress) {
    		mGetLogsTask = new GetLogsTask(mContext);
    		mGetLogsTask.clearAdapter = clearAdapter;
    		mGetLogsTask.execute(sWhere);
    	}
    	
//    	SQLiteDatabase sqdb = sh.getWritableDatabase();
//    	Cursor c;
//    	LogsRecord log;
//   	    	
//    	if (sWhere == "") {
//    		c = sqdb.rawQuery("SELECT * from " + LogsTable.TABLE_NAME + " ORDER BY " + 
//    				LogsTable.ID + " DESC", null);
//    	} else {
//    		c = sqdb.rawQuery("SELECT * from " + LogsTable.TABLE_NAME + " WHERE " + sWhere + 
//    				" ORDER BY " + LogsTable.ID + " DESC", null);
//    	}
//    	int cntr = 0; 
//		try {
//			mDBArrayAdapter.clear();
//	    	while (c.moveToNext()) {
//	    		log = new LogsRecord(c);
//	    		mDBArrayAdapter.add(log);
//	    		cntr++;
//	    	}    	    	
//		} catch(Exception e) {
//	        //e.printStackTrace();
//	        if (D) Log.e(TAG, "Exception: " + e.getMessage());
//	    } finally {
//	    	if (D) Log.d(TAG, "Log Count = " + cntr);
//		caption.setText(String.format(getString(R.string.m_cntr), cntr));
//	    	if (c != null) c.close();
//		}
    }
    
    private class GetLogsTask extends AsyncTask<String, Integer, Boolean> {
    	public boolean clearAdapter;
    	SQLiteDatabase sqdb = dbSch.getWritableDatabase();
    	Cursor c;
    	LogsRecord log;
    	int cntr;
    	int recCount = 0;
    	private ProgressDialog mProgressDialog;
    	private Context ctx;
    	
    	public GetLogsTask(Context context) {
    		ctx = context;
    	}
    	
    	@Override
    	protected Boolean doInBackground(String... strWhere) {
        	String sWhere = strWhere[0];     
        	String query;
        	int percent = 0, new_percent, cur_num = 0;
        	if (sWhere.length() == 0) {
        		query = "SELECT * from " + LogsTable.TABLE_NAME + " ORDER BY " + LogsTable.ID;        		
        	} else {
        		query = "SELECT * from " + LogsTable.TABLE_NAME + " WHERE " + sWhere + " ORDER BY " + LogsTable.ID;
        	}
        	cntr = 0; 
    		try {
    			if (clearAdapter) {
    				//mDBArrayAdapter.clear();
    				logsList.clear();
    				query += " DESC";
    			} else {
    				query += " ASC";
    			}
    			c = sqdb.rawQuery(query, null);
    	    	while (c.moveToNext()) {
    	    		log = new LogsRecord(c);
    	    		if (cntr == 0) {
    	    			recCount = c.getCount();
    	    			maxId = log.id;
    	    			cur_num = logsList.size();
    	    		}    	    		
    	    		//mDBArrayAdapter.add(log);
    	    		if (clearAdapter) {
    	    			log.num = recCount - cntr;
    	    			logsList.add(log);
    	    		} else {
    	    			log.num = cntr + cur_num + 1;
    	    			logsList.add(0, log);
    	    		}
    	    		cntr++;
    	    		if (isCancelled())
    	    			break;
    	    		new_percent = (int) (cntr * 100 / recCount);
    	    		if (new_percent != percent) {
    	    			percent = new_percent;
    	    			publishProgress(percent);
    	    		}    	    		
    	    	}    	    	
    		} catch(Exception e) {
    	        if (D) Log.e(TAG, "Exception: " + e.getMessage());
    	    } finally {
    	    	if (D) Log.d(TAG, "Log Count = " + cntr);
    	    	if (!clearAdapter) {
    	    		//cntr = mDBArrayAdapter.getCount();
    	    		cntr = logsList.size();
    	    	}    	    	
    	    	if (c != null) c.close();
    		}
        	return true;    	
    	}
    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			inProgress = true;
			
		    mProgressDialog = new ProgressDialog(ctx);
		    mProgressDialog.setTitle(getString(R.string.title_activity_log)) ;
			mProgressDialog.setMessage(getString(R.string.m_read_data));
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {				
				@Override
				public void onCancel(DialogInterface dialog) {
					GetLogsTask.this.cancel(false);
				}
			}); 
			
//			mDBListView.setAdapter(null);
//			mDBArrayAdapter.setNotifyOnChange(true);
//			mDBListView.setAdapter(mDBArrayAdapter);
			caption.setTextColor(Color.BLACK);
			mProgressDialog.setMessage(getString(R.string.m_read_data));
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			mProgressDialog.setProgress(progress[0]);
		}
    	
    	protected void onPostExecute(Boolean success) {
			if (D) Log.d(TAG, "onPostExecute");
	    	//if (mProgressDialog != null) {
	    		//mProgressDialog.setProgress(0);
	    		mProgressDialog.dismiss();
	    	//}
//	        mDBListView.setAdapter(mDBArrayAdapter);
	        mDBArrayAdapter.notifyDataSetChanged();
	        swipeLayout.setRefreshing(false);
			caption.setText(String.format(getString(R.string.m_cntr), cntr));
			inProgress = false;
    	}
    }
    
    private class SaveLogsTask extends AsyncTask<String, Integer, Boolean> {
		String logs_array, logDate;
		SimpleDateFormat sDateFormatMS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		JSONArray logs = new JSONArray();		
    	int recCount = 0;
    	private ProgressDialog mProgressDialog;
    	
    	@Override
    	protected Boolean doInBackground(String... strWhere) {
    		try {		
//    			logs = new JSONArray();
    			recCount = mDBArrayAdapter.getCount();
    			for (int i = recCount - 1; i >= 0 ; i--) {
    				LogsRecord logItem = mDBArrayAdapter.getItem(i);
    			    JSONObject jsonLogObj = new JSONObject();
    			    jsonLogObj.put("id", logItem.id);
    				jsonLogObj.put("lvl", logItem.level);
    				jsonLogObj.put("msg", logItem.msg);
    				logDate = sDateFormatMS.format(logItem.m_date);
    				jsonLogObj.put("dt", logDate);
    				logs.put(jsonLogObj);
    				publishProgress((int) ((recCount-i) * 100 / recCount));
    	    		if (isCancelled())
    	    			break;
    			}
        	} catch (JSONException ex) {
            	ex.printStackTrace();
            } finally {
            	if (logs.length() > 0) {
            		logs_array = logs.toString();
            	} else {
            		logs_array = "";
            	}
            }
        	return true;    	
    	}
    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			inProgress = true;
		    mProgressDialog = new ProgressDialog(mContext);
		    mProgressDialog.setTitle(getString(R.string.title_activity_log)) ;
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);			    
			mProgressDialog.setCancelable(true);
			mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {				
				@Override
				public void onCancel(DialogInterface dialog) {
					SaveLogsTask.this.cancel(false);
				}
			}); 
			
			mDBListView.setAdapter(null);
			mProgressDialog.setMessage(getString(R.string.m_send_log));
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			 mProgressDialog.setProgress(progress[0]);
		}
    	
    	protected void onPostExecute(Boolean success) {
			if (D) Log.d(TAG, "onPostExecute");
			inProgress = false;
	    	if (mProgressDialog != null) {
	    		mProgressDialog.setProgress(0);
	    		mProgressDialog.dismiss(); 
	    	}
	    	PostLog();
    	}
    	
        private void PostLog() {
	    	try {
	    		if (logs_array.length() > 0) {
					HttpPost postRequest = new HttpPost(logURI); 					
					postRequest.addHeader(BasicScheme.authenticate(
							new UsernamePasswordCredentials(mIMEI, ConnectionManagerService.MOBILE), "UTF-8", false));
		
					StringEntity se = new StringEntity(logs_array, "UTF-8");
		        	se.setContentType("text/plain;charset=UTF-8");
		        	se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "text/plain;charset=UTF-8"));
		        	postRequest.setEntity(se);
		        							
		        	int waitTimeout = (recCount / 450) * RestTask.SOCKET_TIMEOUT_MIN;
					RestTask.SOCKET_TIMEOUT = waitTimeout;  // увеличиваем время ожидания
					RestTask task = new RestTask();
					RestTask.SOCKET_TIMEOUT = RestTask.SOCKET_TIMEOUT_MIN;  // возвращаем исходное значение
					task.setResponseCallback((ResponseCallback) mContext); 
					task.execute(postRequest); 
					//Display progress to the user 
					mProgress = ProgressDialog.show(mContext, getString(string.m_send_data), getString(string.m_wait_answer), true);
	    		}
			} catch (Exception e) { 
		    	if (D) Log.e(TAG, e.getMessage());
			}			    	        	
        }
    	
    }
    
	@Override
	public void onRequestSuccess(String response) {
		if (D) Log.d(TAG, "onRequestSuccess");
		boolean resultOK = false;
		int res_code;
		String res = "";
		
		if (mProgress != null) { //Clear progress indicator 
			mProgress.dismiss(); 
		} 
		try {
			JSONArray records = new JSONArray(response);
			for (int i = 0; i < records.length(); i++) {
				JSONObject warrant = records.getJSONObject(i);			
				res = warrant.getString("result");
				res_code = warrant.getInt("code");
				resultOK = res_code == 0;								
			}		
		} catch (JSONException e) {
			Log.d(TAG, "Exception: " + e.getMessage()+ " " + response);
			res =/* e.getMessage() + " "+ */response;
		}
	}

	@Override
	public void onRequestError(Exception error) {
		if (mProgress != null) { 
			mProgress.dismiss(); 
		} 
		if (D) Log.d(TAG, "onRequestError: "+ error.getMessage());
		caption.setTextColor(Color.RED);
		caption.setText(error.getMessage());
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		// Construct the new query in the form of a Cursor Loader. Use the id
		// parameter to construct and return different loaders.
		switch (loaderID) {
		case URL_LOG_LOADER:
	    	// Specify the result column projection. Return the minimum set
	    	// of columns required to satisfy your requirements.
			inProgress = true;
	    	String[] result_columns = new String[] { 
		    	WarrantsContentProvider.KEY_ID, 
		    	WarrantsContentProvider.LOG_LEVEL_COLUMN,
		    	WarrantsContentProvider.LOG_DATE_COLUMN,
		    	WarrantsContentProvider.LOG_MSG_COLUMN,
		    	WarrantsContentProvider.LOG_MODIF_COLUMN
		    }; 
	    	String sOrder = " DESC";;
//			if (mClearAdapter) {
//				//mDBArrayAdapter.clear();
//				logsList.clear();
//				sOrder = " DESC";
//			} else {
//				sOrder = " ASC";
//			}
	    	// Specify the where clause that will limit your results.
	    	String where = mWhere;
	    	// Replace these with valid SQL statements as necessary.
	    	String[] whereArgs = null;
	    	String sortOrder = WarrantsContentProvider.KEY_ID + sOrder;
			
			// Query URI
			Uri queryUri = WarrantsContentProvider.CONTENT_LOG_URI;
			// Create the new Cursor loader.
			return new CursorLoader(this, queryUri, result_columns, where, whereArgs, sortOrder);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
/*    	int cntr = 0, recCount = 0, cur_num = 0;
    	LogsRecord log;
    	while (cursor.moveToNext()) {
    		log = new LogsRecord(cursor);
    		if (cntr == 0) {
    			recCount = cursor.getCount();
    			maxId = log.id;
    			cur_num = logsList.size();
    		}    	    		
    		if (mClearAdapter) {
    			log.num = recCount - cntr;
    			logsList.add(log);
    		} else {
    			log.num = cntr + cur_num + 1;
    			logsList.add(0, log);
    		}
    		cntr++;
    	}*/
		mAdapter.changeCursor(cursor);
		inProgress = false;
		recCntr = cursor.getCount();
		handler.post(new Runnable() {
			@Override
			public void run() {
				caption.setText(String.format(getString(R.string.m_cntr), recCntr));
		        swipeLayout.setRefreshing(false);
			}			
		});
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);			
	}

}
