package com.utis.warrants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.utis.warrants.ConnectionManagerService.ServiceResponseCallback;
import com.utis.warrants.InputNameDialogFragment.InputNameDialogListener;
import com.utis.warrants.R.string;
import com.utis.warrants.YesNoDialogFragment.YesNoDialogListener;
import com.utis.warrants.location.LocationActivity;
import com.utis.warrants.location.MyGLocationService;
import com.utis.warrants.record.LogsRecord;
import com.utis.warrants.record.WarrantRecord;
import com.utis.warrants.tables.EmpWorkTimeTable;
import com.utis.warrants.tables.EntTable;
import com.utis.warrants.tables.StateTable;
import com.utis.warrants.tables.WarrantTable;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class WarrantsActivity extends FragmentActivity implements OnClickListener,
	InputNameDialogListener, YesNoDialogListener, ServiceResponseCallback, SwipeRefreshLayout.OnRefreshListener {

	private static final boolean D = true;
	private static final String TAG = "WarrantsActivity";
	private static final int WARR_CONT_SHOW = 1;
    private static final int SCAN_BARCODE = 3;
	private static final String YDM = "YDM_dateFormat";
	private static final String EWT_YDM = "EWT_YDM_dateFormat";
	public static final String GET_ALL_WARRANTS = "all_warrants";
    public static final String WARRANTS_SCROLL_POS = "warrants_pos";
	private ImageButton getImgButton;
    private ImageButton btn1, btn2, btn3;
    private DBSchemaHelper dbSch;
    private ListView mDBListView;
    private CustomWarrantAdapter mDBArrayAdapter;
    private ArrayList<WarrantRecord> warrantArrayList;
    public Context mContext;
    private TextView captionView, replicatorTime, srvErrMsg, tvWarr;
    private RelativeLayout footerLayout;
    private EditText textFilter;
    protected Object mActionMode;
    private Object mActionModeCallback;
    public int selectedItem = -1;
    private boolean keepWarrantTillReport, getAllWarrantsSpecs;
	private static boolean mGettingAllWarrants;
    private SharedPreferences prefs;
    private boolean yearDateFormat, empWorkTimeYearDateFormat;
    private SwipeRefreshLayout swipeLayout;
    private static int currentConnTask;
    private long startTimeGetAllWarrants;
    private LocalBroadcastManager lbm;
    private android.os.Handler mHandler = new android.os.Handler();
    private SensorManager mSensorManager;
	private float mAccel; //текущая Акселерация (ускорение) без гравитации
	private float mAccelCurrent; // текущая акселерация (ускорение), включая гравитацию
	private float mAccelLast; // последняя акселерация (ускорение), включая гравитацию
    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
        	float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    ConnectionManagerService connectionService;
    Intent serviceIntent;
    MyGLocationService locationConnectionService;
    Intent locationServiceIntent;

    private static class CustomWarrantAdapter extends ArrayAdapter<WarrantRecord> implements Filterable{
    	ArrayList<WarrantRecord> warrArray, warrFiltered;
    	WarrantFilter warrantFilter;
    	String preConstraint = "";
    	Context context;
    	public String regl;
    	static class WarrantViewHolder {
    	    public TextView textNo;
    	    public TextView textStatus;
    	    public TextView textCntr;
    	    public TextView textDate;
    	    public TextView textName;
    	    public TextView textRem;
    	    public TextView textToSign;
    	    public TextView regl;
    	}

    	private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

    	public void setNewSelection(int position, boolean value) {
            mSelection.put(position, value);
            notifyDataSetChanged();
        }

        public boolean isPositionChecked(int position) {
            Boolean result = mSelection.get(position);
            return result == null ? false : result;
        }

        public Set<Integer> getCurrentCheckedPosition() {
            return mSelection.keySet();
        }

        public void removeSelection(int position) {
            mSelection.remove(position);
            notifyDataSetChanged();
        }

        public void clearSelection() {
            mSelection = new HashMap<Integer, Boolean>();
            notifyDataSetChanged();
        }

        public CustomWarrantAdapter(Context context, int layout, int resId,
    			WarrantRecord[] items) {
    		//Call through to ArrayAdapter implementation
    		super(context, layout, resId, items);
    		this.context = context;
    	}

    	public CustomWarrantAdapter(Context context, int textViewResourceId) {
    		//Call through to ArrayAdapter implementation
    		super(context, textViewResourceId);
    		this.context = context;
	        this.warrArray = new ArrayList<WarrantRecord>();
	        this.warrFiltered = new ArrayList<WarrantRecord>();
    	}

		public CustomWarrantAdapter(Context context, int resource, ArrayList<WarrantRecord> litem) {
	        super(context, resource, litem);
	        this.context = context;
//	        inflater2 = LayoutInflater.from(context);
	        this.warrArray = new ArrayList<WarrantRecord>();
	        this.warrArray.addAll(litem);
	        this.warrFiltered = new ArrayList<WarrantRecord>();
	        this.warrFiltered.addAll(litem);

	    }

		public void clear() {
			super.clear();
			warrArray.clear();
		}

		public void add(WarrantRecord item) {
			super.add(item);
			warrArray.add(item);

		}

		public void addAll(ArrayList<WarrantRecord> items) {
			super.addAll(items);
			warrArray.clear();
			warrArray.addAll(items);
			warrFiltered.clear();
			warrFiltered.addAll(items);
		}

		public WarrantRecord getItem(int position) {
			return warrFiltered.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		WarrantViewHolder viewHolder;
	    	View row = convertView;
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.warrant_row, null);
//	    		row = LayoutInflater.from(getContext()).inflate(R.layout.warrant_row, parent, false);
	    		viewHolder = new WarrantViewHolder();
	    		viewHolder.textNo = (TextView)row.findViewById(R.id.lineNo);
	    		viewHolder.textStatus = (TextView)row.findViewById(R.id.lineState);
	    		viewHolder.textCntr = (TextView)row.findViewById(R.id.lineCntr);
	    		viewHolder.textDate = (TextView)row.findViewById(R.id.lineDate);
	    		viewHolder.textName = (TextView)row.findViewById(R.id.lineName);
	    		viewHolder.textRem = (TextView)row.findViewById(R.id.lineRemark);
	    		viewHolder.textToSign = (TextView)row.findViewById(R.id.lineToSign);
	    		viewHolder.regl = (TextView)row.findViewById(R.id.regl);
	    		row.setTag(viewHolder);
	    	} else {
	    		viewHolder = (WarrantViewHolder) row.getTag();
	    	}
	    	WarrantRecord item = getItem(position);
	    	viewHolder.textNo.setText("№" + item.num);
	    	if (item.jobMarkStarted) viewHolder.textNo.setTextColor(Color.RED);
	    	else if (item.jobMarkModified) viewHolder.textNo.setTextColor(Color.BLUE);
	    		 else viewHolder.textNo.setTextColor(Color.BLACK);
	    	viewHolder.textStatus.setText(item.warrStateName);
	    	viewHolder.textCntr.setText( ""+item.jobCount);
	    	viewHolder.textDate.setText(item.getDate());
	    	viewHolder.textName.setText(item.entName + "; " + item.subEntName);
	    	viewHolder.textRem.setText(item.remark);
	    	viewHolder.regl.setText(""+item.regl);
            regl = String.valueOf(item.regl);
	    	if (mGettingAllWarrants) {
	    		row.setBackgroundResource(R.drawable.list_selector_disabled);
	    	} else if (item.id_state == WarrantRecord.W_STATE_NEW_DISCARD || item.reported) {
	    		row.setBackgroundResource(R.drawable.list_selector_discarded);
	    	} else {
		    	if (item.localState == WarrantRecord.W_DONE || item.id_state == WarrantRecord.W_STATE_CLOSED ||
		    			item.id_state == WarrantRecord.W_STATE_COMPLETED)
		    		row.setBackgroundResource(R.drawable.list_selector_done);
		    	else if (item.modified == 1)
		    		row.setBackgroundResource(R.drawable.list_selector_modified);
		    	else
		    		row.setBackgroundResource(R.drawable.list_selector);

                    if(regl.equals("1")){
                row.setBackgroundResource(R.drawable.list_selector);
                    }else if(regl.equals("0")){
                row.setBackgroundResource(R.drawable.list_selector_done_lt);
                }
	    	}
    		if (mSelection.get(position) != null) {
				row.setBackgroundResource(R.drawable.list_selector_selected);
    		}
	    	if (item.to_sign != 0) {
	    		if (item.id_user_sign > 0) {
	    			viewHolder.textToSign.setText(getContext().getResources().getString(R.string.caption_warr_signed)+
	    					" " + item.d_sign_str);
	    		} else {
	    			viewHolder.textToSign.setText(getContext().getResources().getString(R.string.caption_to_sign));
	    		}
	    	} else {
	    		viewHolder.textToSign.setText("");
	    	}

	    	return row;
    	}

        @Override
	    public Filter getFilter() {
	        if (warrantFilter == null) {
	        	warrantFilter = new WarrantFilter();
	        }
	        return warrantFilter;
	    }

        @Override
        public int getCount() {
            return warrFiltered.size();
        }

	    private class WarrantFilter extends Filter {
	        @Override
	        protected FilterResults performFiltering(CharSequence constraint) {
				String filterString = constraint.toString().toLowerCase();
				FilterResults results = new FilterResults();
				ArrayList<WarrantRecord> list;
	            if (constraint == null || constraint.length() == 0) {
	                results.values = warrArray;
	                results.count = warrArray.size();
	            } else {
					if (preConstraint == null || preConstraint.length() == 0 || preConstraint.length() > constraint.length())
						list = warrArray;
					else
						list = warrFiltered;
					if (warrArray.size() > 0)
						preConstraint = constraint.toString();
	                List<WarrantRecord> nEntList = new ArrayList<WarrantRecord>();
	                for (WarrantRecord warr : list) {
	                    if (warr.entName.toLowerCase().contains(filterString) ||
	                    		(warr.subEntName != null && warr.subEntName.toLowerCase().contains(filterString))||
	                    		Integer.toString(warr.num).contains(filterString))
	                        nEntList.add(warr);
	                }
	                results.values = nEntList;
	                results.count = nEntList.size();
	            }
	            if (D) Log.d(TAG, "filter exit");
	            return results;
	        }

	        @SuppressWarnings("unchecked")
	        @Override
	        protected void publishResults(CharSequence constraint, FilterResults results) {
                warrFiltered = (ArrayList<WarrantRecord>) results.values;
                notifyDataSetChanged();
				if (D) Log.d(TAG, "filter3");
	        }
	    }

    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean w = requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_warrants_main);

		mContext = this;
        btn1 = (ImageButton) findViewById(R.id.btn1);
        btn2 = (ImageButton) findViewById(R.id.btn2);
        btn3 = (ImageButton) findViewById(R.id.btn3);
		footerLayout = (RelativeLayout)findViewById(R.id.footer_layout);
		captionView = (TextView)findViewById(R.id.title_text);
        tvWarr = (TextView)findViewById(R.id.tvWarr);
		replicatorTime = (TextView)findViewById(R.id.repl_text);
		srvErrMsg = (TextView)findViewById(R.id.srv_err_msg);
		srvErrMsg.setTextColor(Color.RED);
		srvErrMsg.setVisibility(View.GONE);
        getImgButton = (ImageButton) findViewById(R.id.w_get_imageButton);
        getImgButton.setOnClickListener(this);
        lbm = LocalBroadcastManager.getInstance(this);
		mDBArrayAdapter = new CustomWarrantAdapter(this, R.layout.warrant_row);
        mDBListView = (ListView) findViewById(R.id.listViewWarrants);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mWarrantsClickListener);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		    mDBListView.setMultiChoiceModeListener(new ModeCallback());
	    }
        textFilter = (EditText) findViewById(R.id.editTextFilter);
        textFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
            	WarrantsActivity.this.mDBArrayAdapter.getFilter().filter(cs, new Filter.FilterListener() {
            	    public void onFilterComplete(int count) {
            	         showCounter(count);
            	    }
            	});
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) {}
        });
		dbSch = DBSchemaHelper.getInstance(this);

		readPrefValues();
		if (!yearDateFormat) convertDateFormat();
		if (!empWorkTimeYearDateFormat) convertEmpWorkTimeDateFormat();

		serviceIntent = new Intent(this, ConnectionManagerService.class);
		locationServiceIntent = new Intent(this, MyGLocationService.class);

		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	}

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.d(TAG, "+ ON RESUME +");

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		keepWarrantTillReport = sharedPrefs.getBoolean("warr_report_checkbox", false);
		getAllWarrantsSpecs = prefs.getBoolean(GET_ALL_WARRANTS, false);

		mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        if (!(connectionService != null && !connectionService.isWorking())) {
        	startService(serviceIntent);
        }
		if (!(locationConnectionService != null && locationConnectionService.isWorking())) {
			startService(locationServiceIntent);
		}
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        lbm.registerReceiver(onRefresh, new IntentFilter(ConnectionManagerService.REFRESH_LIST_ACTION));

		showWarrants("");
        btn3.setVisibility(View.GONE);
        btn1.setVisibility(View.VISIBLE);
        tvWarr.setText("ВСЕ НАРЯДЫ");

        int scroll = prefs.getInt(WARRANTS_SCROLL_POS, 0);
        mDBListView.setSelection(scroll);
    }

    @Override
    public void onPause() {
    	mSensorManager.unregisterListener(mSensorListener);
    	super.onPause();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(WARRANTS_SCROLL_POS, mDBListView.getFirstVisiblePosition());
        editor.commit();
        lbm.unregisterReceiver(onRefresh);
		connectionService.sendToasts = false;
		if (connectionService != null && connectionService.isWorking()) {
			unbindService(serviceConnection);
		}
		if (locationConnectionService != null) {
			locationConnectionService.sendToasts = false;
			unbindService(locationServiceConnection);
		}
    }

    private BroadcastReceiver onRefresh = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    showWarrants("");
                    btn3.setVisibility(View.GONE);
                    btn1.setVisibility(View.VISIBLE);
                    tvWarr.setText("ВСЕ НАРЯДЫ");
                        updateFooterBg(false);

                }
            }, 10);

        }
    };


    private void updateFooterBg(boolean showGetAllWarrants) {
    	mGettingAllWarrants = showGetAllWarrants;
    	getImgButton.setEnabled(!showGetAllWarrants);
		if (showGetAllWarrants) {
            startTimeGetAllWarrants = System.currentTimeMillis();
			footerLayout.setBackgroundResource(R.drawable.gradient_bg_disabled);
			textFilter.setBackgroundResource(R.drawable.gradient_bg_disabled);
			captionView.setText(getString(R.string.m_warr_wait));
			dbSch.addLogItem(LogsRecord.DEBUG, new Date(), getString(R.string.m_warr_wait));
		} else {
            long endtime = 0;
            if (startTimeGetAllWarrants > 0)
                endtime = System.currentTimeMillis() - startTimeGetAllWarrants;
            startTimeGetAllWarrants = 0;
			footerLayout.setBackgroundResource(R.drawable.gradient_footer_bg);
			textFilter.setBackgroundResource(R.drawable.gradient_bg);
			showCounter(mDBArrayAdapter.getCount());
            if (endtime > 0)
                dbSch.addLogItem(LogsRecord.WARNING, new Date(), getString(R.string.m_warr_wait) +
                    "; " + getString(R.string.m_warr_cntr) +
                    mDBArrayAdapter.getCount() + "; " + endtime + "mc");
		}
		mDBArrayAdapter.notifyDataSetChanged();
    }

	private void readPrefValues() {
		prefs = getSharedPreferences(CommonClass.PREF_NAME, MODE_PRIVATE);
		yearDateFormat = prefs.getBoolean(YDM, false);
		empWorkTimeYearDateFormat = prefs.getBoolean(EWT_YDM, false);
	}

	private void convertDateFormat() {
    	int cntr = 1;
//    	cntr = dbSch.convertLogDateFormat();
    	dbSch.EmptyLogsTable();
    	if (cntr > 0) {
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putBoolean(YDM, true);
    		editor.commit();
    	}
	}

	private void convertEmpWorkTimeDateFormat() {
    	int cntr = 1;
    	cntr = dbSch.convertEmpWorkTimeDateFormat();
    	if (cntr > 0) {
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putBoolean(EWT_YDM, true);
    		editor.commit();
    	}
	}

    private ServiceConnection serviceConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		connectionService = ((ConnectionManagerService.ConnectionBinder)service).getService();
    		connectionService.setServiceResponseCallback((ServiceResponseCallback) mContext);
    		connectionService.sendToasts = true;
    		connectionService.busyWarrants = false;
			if (getAllWarrantsSpecs) {
    			connectionService.setFirstRun();
    			connectionService.reRunTimer();
    			getAllWarrantsSpecs = false;
        		SharedPreferences.Editor editor = prefs.edit();
                Log.d(TAG , " srv "+String.valueOf(getAllWarrantsSpecs));
        		editor.putBoolean(GET_ALL_WARRANTS, getAllWarrantsSpecs);
        		editor.commit();
    		}
			long cnt = dbSch.getModifiedWarrantCount();
			updateFooterBg((ConnectionManagerService.getFirstRun() || connectionService.gettingAllConstTables())
					&& (cnt == 0));
			if (cnt > 0)
				connectionService.postWarrantTables();
    		//updateStatus();
    	}

    	public void onServiceDisconnected(ComponentName className) {
    		connectionService = null;
    	}
    };

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		locationConnectionService = ((MyGLocationService.ConnectionBinder)service).getService();
    		locationConnectionService.sendToasts = true;
    	}

    	public void onServiceDisconnected(ComponentName className) {
    		locationConnectionService = null;
    	}
    };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.warrants, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		setProgressBarIndeterminateVisibility(connectionService != null && connectionService.isWorking() &&
				connectionService.getState() != ConnectionManagerService.STATE_FINISH);
		MenuItem mi = menu.findItem(R.id.action_disable_conn_service);
		if (mi != null && connectionService != null && connectionService.isWorking())
			mi.setTitle(mContext.getString(string.m_disable_conn_service));
		else
			mi.setTitle(mContext.getString(string.m_enable_conn_service));

		mi = menu.findItem(R.id.action_disable_location_service);
		if (mi != null && locationConnectionService != null) {
			if (locationConnectionService.canGetLocation()) {
				mi.setEnabled(true);
				if (locationConnectionService.isWorking())
					mi.setTitle(mContext.getString(string.m_disable_location_service));
				else
					mi.setTitle(mContext.getString(string.m_enable_location_service));
			} else mi.setEnabled(false);
		} else mi.setEnabled(false);

		boolean connTaskFinish = (connectionService != null)&& connectionService.isWorking() &&
				(connectionService.getState() == ConnectionManagerService.STATE_FINISH) && !mGettingAllWarrants;
		mi = menu.findItem(R.id.action_update_dictionaries);
		if (mi != null) {
			mi.setEnabled(connTaskFinish);
			if (connectionService != null) {
				if (connectionService.getState() == ConnectionManagerService.STATE_FINISH) {
					mi.setTitle(mContext.getString(string.m_update_dictionaries));
				} else {
					mi.setTitle(mContext.getString(string.m_update_dictionaries) +" "+ connectionService.getState());
				}
			}
		}
		mi = menu.findItem(R.id.action_refresh_warrants);
		if (mi != null) {
			mi.setVisible(false/*connTaskFinish*/);
		}
		mi = menu.findItem(R.id.action_show_web_soft);
		if (mi != null) {
			mi.setVisible(CommonClass.newVerAvail);
			mi.setEnabled(CommonClass.newVerAvail);
		}
	    return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.search:
        	if (D) Log.d(TAG, "onOptions Search");
            return true;
        case R.id.action_refresh_warrants:
        	if (D) Log.d(TAG, "onOptions action_refresh_warrants");
        	connectionService.postWarrantTables();
        	return true;
        case R.id.action_show_warr_map:
        	if (D) Log.d(TAG, "onOptions action_show_warr_map");
    		showWarrantsOnMap();
        	return true;
        case R.id.action_show_msgs_list:
        	if (D) Log.d(TAG, "onOptions action_show_msgs_list");
    		showEmpMsgs();
        	return true;
        case R.id.action_show_emp_locations:
        	if (D) Log.d(TAG, "onOptions action_show_emp_locations");
    		showEmpLocations();
        	return true;
        case R.id.action_scan_barcode:
        	if (D) Log.d(TAG, "onOptions action_scan_barcode");
    		scanBarCode();
//            Intent intent  = new Intent(WarrantsActivity.this, ATestActivity.class);
//            startActivity(intent);
        	return true;
        case R.id.action_show_web_soft:
        	if (D) Log.d(TAG, "onOptions action_show_web_soft");
        	showMessage(getString(R.string.title_activity_web_kit), getString(R.string.m_load_soft));
//    		ShowWebSoft();
        	return true;
        case R.id.action_update_dictionaries:
        	if (D) Log.d(TAG, "onOptions action_update_dictionaries");
    		connectionService.getConstTables(true);
        	return true;
        case R.id.action_disable_conn_service:
        	if (D) Log.d(TAG, "onOptions action_disable_conn_service");
        	disableConnService();
        	return true;
        case R.id.action_disable_location_service:
        	if (D) Log.d(TAG, "onOptions action_disable_location_service");
        	disableLocationService();
        	return true;
        case R.id.action_show_emp_hw:
        	if (D) Log.d(TAG, "onOptions action_show_emp_hw");
    		showEmpHW();
        	return true;
        case R.id.action_show_traffic_stat:
        	if (D) Log.d(TAG, "onOptions action_show_traffic_stat");
    		showTrafficStat();
        	return true;
        case R.id.action_show_logs:
        	if (D) Log.d(TAG, "onOptions action_show_logs");
    		showLogs();
        	return true;
        case R.id.dialog_name:
        	if (D) Log.d(TAG, "onOptions dialog_name");
        	showInputNameDialog();
        	return true;
        case R.id.dialog_ask:
        	if (D) Log.d(TAG, "onOptions dialog_ask");
        	showYesNoDialog();
        	return true;
        }
        return false;
    }

    private void disableConnService() {
		connectionService.sendToasts = false;
		if (connectionService != null && connectionService.isWorking()) {
			unbindService(serviceConnection);
			stopService(serviceIntent);
		} else {
	        startService(serviceIntent);
	        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		}
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    invalidateOptionsMenu();
    	}
    }

    private void disableLocationService() {
		if (locationConnectionService != null && locationConnectionService.isWorking()) {
			//unbindService(locationServiceConnection);
			stopService(locationServiceIntent);
		} else {
			startService(locationServiceIntent);
			//bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
		}
    }

	//===Input Name Dialog===
	private void showInputNameDialog() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		InputNameDialogFragment inputNameDialog = new InputNameDialogFragment();
		inputNameDialog.setCancelable(false);
		inputNameDialog.setDialogTitle(getString(string.m_add_comment));
		inputNameDialog.show(fragmentManager, "input dialog");
	}

	@Override
	public void onFinishInputDialog(String inputText) {
		Toast.makeText(this, "Returned from dialog: "+ inputText, Toast.LENGTH_SHORT).show();
	}

	//===YES/No Dialog===
	private void showYesNoDialog() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		YesNoDialogFragment yesnoDialog = new YesNoDialogFragment();
		yesnoDialog.setCancelable(false);
		yesnoDialog.setDialogTitle(getString(string.m_ch_status));
		yesnoDialog.show(fragmentManager, "yes/no dialog");
	}

	@Override
	public void onFinishYesNoDialog(boolean state) {
		Toast.makeText(this, "Returned from dialog: "+ state, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.map_button:
    		if (D) Log.d(TAG, "clicked on map_button");
    		showWarrantsOnMap();
    		break;
    	case R.id.w_get_imageButton:
		case R.id.w_get_button:
			if (D) Log.d(TAG, "clicked on w_get_button");
        	connectionService.postWarrantTables();
			break;
       	}
   	}

	private void showEmpMsgs() {
        Intent runMsgListActivity = new Intent(this, EmpMessagesActivity.class);
        startActivity(runMsgListActivity);
	}

	private void showEmpHW() {
		Intent intent = new Intent(mContext, EmpHWSelectActivity.class);
		Bundle b = new Bundle();
		b.putString("id", "");
		b.putString("warrId", "");
		intent.putExtras(b);
		startActivity(intent);
	}

	private void showTrafficStat(){
        Intent intent = new Intent(this, TrafficStatActivity.class);
        startActivity(intent);
	}

	private void showLogs() {
        Intent intent = new Intent(this, LogsActivity.class);
        startActivity(intent);
	}

	private void showEmpLocations() {
    	Intent runLocationActivity = new Intent(this, LocationActivity.class);
    	startActivity(runLocationActivity);
	}

	private void scanBarCode() {
    	Intent intent = new Intent(this, BarScanWarrantsActivity.class);
    	startActivityForResult(intent, SCAN_BARCODE);
	}

	private void showMessage(String msgTitle, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder
	    .setTitle(msgTitle)
	    .setMessage(msg)
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener()
	    {
	        public void onClick(DialogInterface dialog, int which)
	        {
	            //do some thing here which you need
	        	dialog.dismiss();
	        	showWebSoft();
	        }
	    });
	    AlertDialog alert = builder.create();
	    alert.show();
	}

	private void showWebSoft() {
    	Intent webIntent = new Intent(Intent.ACTION_VIEW,
    			Uri.parse(ConnectionManagerService.SERVER_URI_SSL +"/index.php"));
    	//Intent webIntent = new Intent(this, WebKitActivity.class);
    	startActivity(webIntent);
	}

	private void showWarrantsOnMap() {
		Intent runMapActivity = new Intent(this, WarrantsMapActivity.class);
        Bundle b = new Bundle();
        b.putString("map_mode", Integer.toString(WarrantsMapActivity.WARRANTS_ENT_MAP_MODE)); //Your id
        runMapActivity.putExtras(b);
        startActivity(runMapActivity);
	}

    // The on-click listener for ListViews
    private OnItemClickListener mWarrantsClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	WarrantRecord warrant;
        	warrant = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + warrant.id_external);
            if (currentConnTask != ConnectionManagerService.STATE_GET_WARRANTS && !mGettingAllWarrants) {
	            Intent intent = new Intent(mContext, WarrContActivity.class);
	            Bundle b = new Bundle();
	            b.putString("id", Long.toString(warrant.id_external)); //Your id
	            b.putString("id_contract", Long.toString(warrant.id_contract));
	            b.putString("id_state", Integer.toString(warrant.id_state));
	            b.putString("to_sign", Integer.toString((warrant.id_user_sign == 0) ? warrant.to_sign : 0));
	            b.putString("local_state", Integer.toString(warrant.localState));
	            b.putString("contragent", String.format("%s; %s;\n%s", warrant.entName, warrant.subEntName, warrant.entAddress));
	            b.putString("ent_addr", warrant.entAddress);
	            b.putString("ent_name", warrant.entName);
	            b.putString("ent_lat", Double.toString(warrant.entLat));
	            b.putString("ent_lng", Double.toString(warrant.entLng));
	            b.putString("num", warrant.getNumDate());
	            b.putString("w_num", Integer.toString(warrant.num));
	            b.putString("rem", warrant.remark);
	            b.putString("w_date", warrant.d_open_str);
	            b.putString("sub_ent_nume", warrant.subEntName);
	            b.putString("regl", Integer.toString(warrant.regl));
	            int ent = warrant.id_subent;
	            if (ent == 0) ent = warrant.id_ent;
	            b.putString("id_ent", Integer.toString(ent));
	            intent.putExtras(b);
	            connectionService.busyWarrants = true;
		        startActivityForResult(intent, WARR_CONT_SHOW);
            }
        }
    };


    private OnItemLongClickListener mWarrantsLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long id) {
			if (mActionMode != null) {
				return false;
		    }
	        selectedItem = position;

	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		        // start the CAB using the ActionMode.Callback defined above
		        mActionMode = WarrantsActivity.this.startActionMode((ActionMode.Callback)mActionModeCallback);
		        mDBListView.setSelected(true);
	        }
	        return true;
	    }

    };

    private void showEmpWorkTimeMarks() {
    	WarrantRecord item = mDBArrayAdapter.getItem(selectedItem);
    	CommonClass.showWarrantEmpWorkTimeMarks(mContext, String.valueOf(item.id_external),
    			String.valueOf(item.num));
    }

    private void showWarrants(String sWhere) {
    	SQLiteDatabase sqdb = dbSch.getWritableDatabase();
    	Cursor c = null;
    	Cursor cEnt = null;
    	WarrantRecord warrant;

    	String w_state = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
    			" WHERE "+ StateTable.ID_EXTERNAL +" = w."+ WarrantTable.IDSTATE +")as "+
    			WarrantRecord.WARR_STATE_NM;

    	String markQuery = 	"(SELECT MAX(" + EmpWorkTimeTable.ID +
				") FROM " + EmpWorkTimeTable.TABLE_NAME +
				" WHERE " + EmpWorkTimeTable.ID_WARRANT + "= w.id_external)AS DTBE";
    	String query;

    	if (sWhere.length() == 0) {
    		query = "SELECT w.*, "+ markQuery +", "+ w_state +" FROM " + WarrantTable.TABLE_NAME +
    				" w  ORDER BY "+ WarrantTable.REGL +" ASC," + WarrantTable.D_OPEN + " DESC";
    	} else {
    		query = "SELECT w.*, "+ markQuery +", "+ w_state +" FROM " + WarrantTable.TABLE_NAME +
    				" w WHERE " + sWhere
                  //  + " ORDER BY " + WarrantTable.ID_EXTERNAL + " DESC"
            ;
    	}
    	int cntr = 0, colid, scrollPos = 0;
		try {
            scrollPos = mDBListView.getFirstVisiblePosition();
			c = sqdb.rawQuery(query, null);

	        mDBListView.setAdapter(null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		cntr++;
	    		warrant = new WarrantRecord(c);

	    		warrant.jobMarkStarted = dbSch.getEmpWorkLastMark(warrant.id_external) == 0;
	    		warrant.jobMarkModified = dbSch.getEmpWorkLastMarkModified(warrant.id_external) > 0;
                //Log.d(TAG," == 0 "+ String.valueOf(warrant.jobMarkStarted));
                //Log.d(TAG," > 0  "+ String.valueOf(warrant.jobMarkModified));
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
			    		colid = cEnt.getColumnIndex(EntTable.ADDR);
			    		warrant.entAddress = cEnt.getString(colid);
			    		colid = cEnt.getColumnIndex(EntTable.LAT);
			    		warrant.entLat = cEnt.getDouble(colid);
			    		colid = cEnt.getColumnIndex(EntTable.LNG);
			    		warrant.entLng = cEnt.getDouble(colid);
			    		cEnt.close();
		    		}
	    		}
                warrant.jobCount = (int) dbSch.getWarrantContCount(warrant.id_external);
                Log.d(TAG," =  "+ String.valueOf(warrant.jobCount));
	    		mDBArrayAdapter.add(warrant);
	    	}
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
	    	if (D) Log.d(TAG, "Warrants Count = " + cntr);
	    	if (CommonClass.lastErrorMsg.length() != 0) captionView.setText(CommonClass.lastErrorMsg);
	    	else showCounter(cntr);

	        String filter = textFilter.getText().toString();
	        if (D) Log.d(TAG, "filter = " + filter);
	        if (filter != null && filter.length() >= 0) {
            	WarrantsActivity.this.mDBArrayAdapter.getFilter().filter(filter, new Filter.FilterListener() {
            	    public void onFilterComplete(int count) {
            	         showCounter(count);
            	    }
            	});
	        } else {
	        }
	        mDBListView.setAdapter(mDBArrayAdapter);

	    	String sDate = CommonClass.readReplTime(mContext);
	    	String srDate = "";
    		Date rDate = null;
	    	if (sDate != null && sDate.length() > 0) {
	    		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
	    		try {
					rDate = sDateFormat.parse(sDate);
					SimpleDateFormat df = new SimpleDateFormat(getString(string.m_repl_date_fmt));
					srDate = df.format(rDate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	if (srDate != null && srDate.length() > 0) {
	    		replicatorTime.setText(getString(R.string.m_db_replication) + srDate);
	    		long diffInMs = new Date().getTime() - rDate.getTime();
	    		long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
	    		if (diffInSec > 5*60)
	    			replicatorTime.setTextColor(Color.RED);
	    		else
	    			replicatorTime.setTextColor(Color.WHITE);
	    	}
	    	if (c != null) c.close();
//            if (cEnt != null) cEnt.close();
	    	if (D) Log.d(TAG, "cursor close");
            if (scrollPos > 0)
                mDBListView.setSelection(scrollPos);
		}
    }

    private void showWarrantsSortOne(String sWhere) {
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        Cursor c = null;
        Cursor cEnt = null;
        WarrantRecord warrant;

        String w_state = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
                " WHERE "+ StateTable.ID_EXTERNAL +" = w."+ WarrantTable.IDSTATE +")as "+
                WarrantRecord.WARR_STATE_NM;

        String markQuery = 	"(SELECT MAX(" + EmpWorkTimeTable.ID +
                ") FROM " + EmpWorkTimeTable.TABLE_NAME +
                " WHERE " + EmpWorkTimeTable.ID_WARRANT + "= w.id_external)AS DTBE";
        String query;

        if (sWhere.length() == 0) {
            query = "SELECT w.*, "+ markQuery +", "+ w_state +" FROM " + WarrantTable.TABLE_NAME +
                    " w" + " WHERE "+ WarrantTable.REGL+ " = " + 1 +
                    " ORDER BY DTBE DESC," + WarrantTable.ID_EXTERNAL + " DESC";
        } else {
            query = "SELECT w.*, "+ markQuery +", "+ w_state +" FROM " + WarrantTable.TABLE_NAME +
                    " w WHERE "+ WarrantTable.REGL+ " = " + 1 + " AND "
                    + sWhere + " ORDER BY " + WarrantTable.ID_EXTERNAL + " DESC";
        }
        int cntr = 0, colid, scrollPos = 0;
        try {
            scrollPos = mDBListView.getFirstVisiblePosition();
            c = sqdb.rawQuery(query, null);

            mDBListView.setAdapter(null);
            mDBArrayAdapter.clear();
            while (c.moveToNext()) {
                cntr++;
                warrant = new WarrantRecord(c);
                warrant.jobMarkStarted = dbSch.getEmpWorkLastMark(warrant.id_external) == 0;
                warrant.jobMarkModified = dbSch.getEmpWorkLastMarkModified(warrant.id_external) > 0;
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
                        colid = cEnt.getColumnIndex(EntTable.ADDR);
                        warrant.entAddress = cEnt.getString(colid);
                        colid = cEnt.getColumnIndex(EntTable.LAT);
                        warrant.entLat = cEnt.getDouble(colid);
                        colid = cEnt.getColumnIndex(EntTable.LNG);
                        warrant.entLng = cEnt.getDouble(colid);
                        cEnt.close();
                    }
                }
                warrant.jobCount = (int) dbSch.getWarrantContCount(warrant.id_external);
                mDBArrayAdapter.add(warrant);
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
          //  if (D) Log.d(TAG, "Warrants Count = " + cntr);
            if (CommonClass.lastErrorMsg.length() != 0) captionView.setText(CommonClass.lastErrorMsg);
            else showCounter(cntr);

            String filter = textFilter.getText().toString();
            if (D) Log.d(TAG, "filter = " + filter);
            if (filter != null && filter.length() >= 0) {
                WarrantsActivity.this.mDBArrayAdapter.getFilter().filter(filter, new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        showCounter(count);

                    }
                });
            } else {
            }
            mDBListView.setAdapter(mDBArrayAdapter);

            String sDate = CommonClass.readReplTime(mContext);
            String srDate = "";
            Date rDate = null;
            if (sDate != null && sDate.length() > 0) {
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
                try {
                    rDate = sDateFormat.parse(sDate);
                    SimpleDateFormat df = new SimpleDateFormat(getString(string.m_repl_date_fmt));
                    srDate = df.format(rDate);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (srDate != null && srDate.length() > 0) {
                replicatorTime.setText(getString(R.string.m_db_replication) + srDate);
                long diffInMs = new Date().getTime() - rDate.getTime();
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
                if (diffInSec > 5*60)
                    replicatorTime.setTextColor(Color.RED);
                else
                    replicatorTime.setTextColor(Color.WHITE);
            }
            if (c != null) c.close();
            if (D) Log.d(TAG, "cursor close");
            if (scrollPos > 0)
                mDBListView.setSelection(scrollPos);
        }
    }

    private void showWarrantsZero(String sWhere) {
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        Cursor c = null;
        Cursor cEnt = null;
        WarrantRecord warrant;

        String w_state = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
                " WHERE "+ StateTable.ID_EXTERNAL +" = w."+ WarrantTable.IDSTATE +")as "+
                WarrantRecord.WARR_STATE_NM;

        String markQuery = 	"(SELECT MAX(" + EmpWorkTimeTable.ID +
                ") FROM " + EmpWorkTimeTable.TABLE_NAME +
                " WHERE " + EmpWorkTimeTable.ID_WARRANT + "= w.id_external)AS DTBE";
        String query;

        if (sWhere.length() == 0) {
            query = "SELECT w.*, "+ markQuery +", "+ w_state +" FROM " + WarrantTable.TABLE_NAME +
                    " w" + " WHERE "+ WarrantTable.REGL+ " = " + 0 +
                    " ORDER BY DTBE DESC," + WarrantTable.ID_EXTERNAL + " DESC";
        } else {
            query = "SELECT w.*, "+ markQuery +", "+ w_state +" FROM " + WarrantTable.TABLE_NAME +
                    " w WHERE "+ WarrantTable.REGL+ " = " + 0 + " AND "
                    + sWhere + " ORDER BY " + WarrantTable.ID_EXTERNAL + " DESC";
        }
        int cntr = 0, colid, scrollPos = 0;
        try {
            scrollPos = mDBListView.getFirstVisiblePosition();
            c = sqdb.rawQuery(query, null);

            mDBListView.setAdapter(null);
            mDBArrayAdapter.clear();
            while (c.moveToNext()) {
                cntr++;
                warrant = new WarrantRecord(c);
                warrant.jobMarkStarted = dbSch.getEmpWorkLastMark(warrant.id_external) == 0;
                warrant.jobMarkModified = dbSch.getEmpWorkLastMarkModified(warrant.id_external) > 0;
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
                        colid = cEnt.getColumnIndex(EntTable.ADDR);
                        warrant.entAddress = cEnt.getString(colid);
                        colid = cEnt.getColumnIndex(EntTable.LAT);
                        warrant.entLat = cEnt.getDouble(colid);
                        colid = cEnt.getColumnIndex(EntTable.LNG);
                        warrant.entLng = cEnt.getDouble(colid);
                        cEnt.close();
                    }
                }
                warrant.jobCount = (int) dbSch.getWarrantContCount(warrant.id_external);
                mDBArrayAdapter.add(warrant);
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (D) Log.d(TAG, "Warrants Count = " + cntr);
            if (CommonClass.lastErrorMsg.length() != 0) captionView.setText(CommonClass.lastErrorMsg);
            else showCounter(cntr);

            String filter = textFilter.getText().toString();
            if (D) Log.d(TAG, "filter = " + filter);
            if (filter != null && filter.length() >= 0) {
                WarrantsActivity.this.mDBArrayAdapter.getFilter().filter(filter, new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        showCounter(count);

                    }
                });
            } else {
            }
            mDBListView.setAdapter(mDBArrayAdapter);

            String sDate = CommonClass.readReplTime(mContext);
            String srDate = "";
            Date rDate = null;
            if (sDate != null && sDate.length() > 0) {
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
                try {
                    rDate = sDateFormat.parse(sDate);
                    SimpleDateFormat df = new SimpleDateFormat(getString(string.m_repl_date_fmt));
                    srDate = df.format(rDate);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (srDate != null && srDate.length() > 0) {
                replicatorTime.setText(getString(R.string.m_db_replication) + srDate);
                long diffInMs = new Date().getTime() - rDate.getTime();
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
                if (diffInSec > 5*60)
                    replicatorTime.setTextColor(Color.RED);
                else
                    replicatorTime.setTextColor(Color.WHITE);
            }
            if (c != null) c.close();
            if (D) Log.d(TAG, "cursor close");
            if (scrollPos > 0)
                mDBListView.setSelection(scrollPos);
        }
    }


    private void showCounter(int cntr) {
    	if (!mGettingAllWarrants)
    		captionView.setText(getString(R.string.m_warr_cntr) + cntr);
    	if (D) Log.d(TAG, "showCounter = " + cntr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case WARR_CONT_SHOW:
                if (data.hasExtra("updated")) {
                	int updated = Integer.parseInt(data.getExtras().getString("updated"));
                	if (updated > 0) {
                    //    Log.d(TAG +"update ", String.valueOf(updated));
                		showWarrants("");
                	}
                }
                break;
    		case SCAN_BARCODE:
                if (resultCode == RESULT_OK) {
                    String contents = data.getStringExtra("SCAN_RESULT");
                    String format = data.getStringExtra("SCAN_RESULT_FORMAT");
                    // Handle successful scan
                    if (D) Log.d(TAG, "scan successful = "+ contents);
                    Toast.makeText(mContext, "Scan =" + contents + " "+ format, Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    // Handle cancel
                	if (D) Log.d(TAG, "scan failed");
                }
    			break;
    		}
        }
    }

	@Override
	public void onSrvRequestSuccess(String response) {
		captionView.setTextColor(Color.WHITE);
		srvErrMsg.setVisibility(View.GONE);
		//Toast.makeText(this, "ConnService: "+ response, Toast.LENGTH_SHORT).show();
//		if (D) Log.d(TAG, "onSrvRequestSuccess 1: "+ response);
//		showWarrants("");
//		if (D) Log.d(TAG, "onSrvRequestSuccess 2");
		if (connectionService.getState() == ConnectionManagerService.STATE_FINISH && !connectionService.gettingAllConstTables())
    		updateFooterBg(false);
	}

	@Override
	public void onSrvRequestError(Exception error) {
//		Caption.setTextColor(Color.RED);
		srvErrMsg.setVisibility(View.VISIBLE);
		srvErrMsg.setText(error.getMessage());
		Toast.makeText(this, "ConnService Exception: "+ error.getMessage(), Toast.LENGTH_SHORT).show();
		swipeLayout.setRefreshing(false);
	}

	@Override
	public void onSrvTaskChanged(int task) {
		currentConnTask = task;//connectionService.getState();
		if ((currentConnTask == ConnectionManagerService.STATE_FINISH))
			swipeLayout.setRefreshing(false);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    invalidateOptionsMenu();
    	}
	}

    private void setActionMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	mActionModeCallback = new ActionMode.Callback() {

                // called when the action mode is created; startActionMode() was called
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        			// Inflate a menu resource providing context menu items
        			MenuInflater inflater = mode.getMenuInflater();
        			// assumes that you have "contexual.xml" menu resources
        			inflater.inflate(R.menu.warrants_rowselection, menu);
        			return true;
                }
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                	WarrantRecord item = mDBArrayAdapter.getItem(selectedItem);
            		MenuItem mi = menu.findItem(R.id.action_discard_warrant);
            		if (mi != null) {
            			if (item.id_state == WarrantRecord.W_STATE_NEW_DISCARD)
            				mi.setTitle(mContext.getString(string.action_dont_discard_warrant));
            			else
            				mi.setTitle(mContext.getString(string.action_discard_warrant));
            		}
                    return true; // Return false if nothing is done
                }

                // called when the user selects a contextual menu item
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                	WarrantRecord warrant = mDBArrayAdapter.getItem(selectedItem);
                    switch (item.getItemId()) {
                    case R.id.action_show_emp_mark_time:
                    	showEmpWorkTimeMarks();
                        // the Action was executed, close the CAB
                        mode.finish();
                        return true;
                    case R.id.action_report_warrant:
                    	showEmpWorkTimeMarks();
                        // the Action was executed, close the CAB
                        mode.finish();
                        return true;
                    case R.id.action_discard_warrant:
            			if (warrant.id_state == WarrantRecord.W_STATE_NEW_DISCARD) {
            				if (dbSch.setWarrantIdState(warrant.id_external, WarrantRecord.W_STATE_IN_WORK))
            					warrant.id_state = WarrantRecord.W_STATE_IN_WORK;
            			}
            			else {
            				if (dbSch.setWarrantIdState(warrant.id_external, WarrantRecord.W_STATE_NEW_DISCARD))
            					warrant.id_state = WarrantRecord.W_STATE_NEW_DISCARD;
            			}
            			mDBArrayAdapter.notifyDataSetChanged();
                        // the Action was executed, close the CAB
                        mode.finish();
                        return true;
                    default:
                        return false;
                    }
                }

                // called when the user exits the action mode
                public void onDestroyActionMode(ActionMode mode) {
                    mActionMode = null;
                    selectedItem = -1;
                }
            };

        }
    }

	private class ModeCallback implements ListView.MultiChoiceModeListener {
		MenuItem miDiscard, miReport;

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.warrants_rowselection, menu);
            mode.setTitle(mContext.getString(R.string.selected));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    		miDiscard = menu.findItem(R.id.action_discard_warrant);
    		miReport = menu.findItem(R.id.action_report_warrant);
    		updateMenuItems();
            return true;
        }

        private void updateMenuItems() {
        	int w_state = getSelectedWarrantState();
    		if (miDiscard != null) {
    			if (w_state == WarrantRecord.W_STATE_NEW_DISCARD) {
    				miDiscard.setTitle(mContext.getString(string.action_dont_discard_warrant));
    				miDiscard.setEnabled(true);
    			} else if (w_state != 0) {
    				miDiscard.setTitle(mContext.getString(string.action_discard_warrant));
    				miDiscard.setEnabled(true);
    			} else {
    				miDiscard.setEnabled(false);
    			}
    		}
    		if (miReport != null) {
    			miReport.setVisible(keepWarrantTillReport);
    			if (keepWarrantTillReport)
    				miReport.setEnabled(checkSelectedWarrantClosedState());
    		}
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_show_emp_mark_time:
            	showSelectedEmpWorkTimeIntervals();
                // the Action was executed, close the CAB
                mode.finish();
                return true;
            case R.id.action_report_warrant:
            	setSelectedWarrantReportedState();
                mode.finish();
                return true;
            case R.id.action_discard_warrant:
            	setSelectedWarrantState();
                mode.finish();
                return true;
            default:
                return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
        	mDBArrayAdapter.clearSelection();
        }

        public void onItemCheckedStateChanged(ActionMode mode,
                int position, long id, boolean checked) {
            final int checkedCount = mDBListView.getCheckedItemCount();
            if (checked)
            	mDBArrayAdapter.setNewSelection(position, checked);
            else
            	mDBArrayAdapter.removeSelection(position);

            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                default:
                    mode.setSubtitle("" + checkedCount);
                    break;
            }
            updateMenuItems();
        }

        private int getSelectedWarrantState() {
        	int res = 0;
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        			WarrantRecord item = mDBArrayAdapter.getItem(i);
        			if (item.id_state == WarrantRecord.W_STATE_NEW_DISCARD &&
        					(res == 0 || res == WarrantRecord.W_STATE_NEW_DISCARD))
        				res = WarrantRecord.W_STATE_NEW_DISCARD;
        			else
        				res = WarrantRecord.W_STATE_IN_WORK;
        		}
        	}
        	return res;
        }

        private boolean checkSelectedWarrantClosedState() {
        	boolean res = false;
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        			WarrantRecord item = mDBArrayAdapter.getItem(i);
        			if (item.id_state == WarrantRecord.W_STATE_CLOSED ||
        					item.id_state == WarrantRecord.W_STATE_COMPLETED) {
        				res = true;
        				break;
        			}
        		}
        	}
        	return res;
        }

        private void showSelectedEmpWorkTimeIntervals() {
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        	    	WarrantRecord item = mDBArrayAdapter.getItem(i);
        	    	CommonClass.showWarrantEmpWorkTimeMarks(mContext, String.valueOf(item.id_external),
        	    			String.valueOf(item.num));
        		}
        	}
        }

        private void setSelectedWarrantState() {
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        	    	WarrantRecord warrant = mDBArrayAdapter.getItem(i);
        			if (warrant.id_state == WarrantRecord.W_STATE_NEW_DISCARD) {
        				if (dbSch.setWarrantIdState(warrant.id_external, WarrantRecord.W_STATE_IN_WORK))
        					warrant.id_state = WarrantRecord.W_STATE_IN_WORK;
        			} else {
        				if (dbSch.setWarrantIdState(warrant.id_external, WarrantRecord.W_STATE_NEW_DISCARD))
        					warrant.id_state = WarrantRecord.W_STATE_NEW_DISCARD;
        			}
        		}
        	}
        	mDBArrayAdapter.clearSelection();
        	mDBArrayAdapter.notifyDataSetChanged();
        }

        private void setSelectedWarrantReportedState() {
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        	    	WarrantRecord warrant = mDBArrayAdapter.getItem(i);
        			if (warrant.id_state == WarrantRecord.W_STATE_CLOSED ||
        					warrant.id_state == WarrantRecord.W_STATE_COMPLETED) {
        				if (dbSch.setWarrantIdState(warrant.id_external, warrant.id_state ^ WarrantRecord.W_REPORTED_STATE_MASK))
        					warrant.reported = true;
        			}
        		}
        	}
        	mDBArrayAdapter.clearSelection();
        	mDBArrayAdapter.notifyDataSetChanged();
        }

    }
	@Override
	public void onRefresh() {
		if (!mGettingAllWarrants && connectionService.getState() == ConnectionManagerService.STATE_FINISH)
			connectionService.postWarrantTables();
		else
			swipeLayout.setRefreshing(false);
	}

    public void btnOne(View view){
        showWarrantsSortOne("");
        btn1.setVisibility(View.GONE);
        btn2.setVisibility(View.VISIBLE);
        tvWarr.setText("РЕГЛАМЕНТЫ");

    }
    public void btnTwo(View view){
        showWarrantsZero("");
        btn2.setVisibility(View.GONE);
        btn3.setVisibility(View.VISIBLE);
        tvWarr.setText("РЕМОНТЫ");
    }
    public void btnThree(View view){
        showWarrants("");
        btn3.setVisibility(View.GONE);
        btn1.setVisibility(View.VISIBLE);
        tvWarr.setText("ВСЕ НАРЯДЫ");
    }
}
