package com.utis.warrants;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.utis.warrants.InputNameDialogFragment.InputNameDialogListener;
import com.utis.warrants.R.string;
import com.utis.warrants.RestTask.ResponseCallback;
import com.utis.warrants.YesNoDialogFragment.YesNoDialogListener;
import com.utis.warrants.location.GPSTracker;
import com.utis.warrants.location.MyGLocationService;
import com.utis.warrants.record.EmpWorkTimeRecord;
import com.utis.warrants.record.LogsRecord;
import com.utis.warrants.record.StateRecord;
import com.utis.warrants.record.WarrContRecord;
import com.utis.warrants.record.WarrantRecord;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.SpecsTable;
import com.utis.warrants.tables.StateTable;
import com.utis.warrants.tables.WarrContTable;
import com.utis.warrants.tables.WarrUnitTable;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class WarrContActivity extends FragmentActivity implements OnClickListener, 
	InputNameDialogListener, YesNoDialogListener, ResponseCallback  {
	private static final boolean D = true;
	private static final String TAG = "WarrContActivity";
	private static final int JOB_SHOW = 1;
	private static final int EQ_SELECT = 2;
	private static final int JOB_SELECT = 3;
	private static final int EQ_SPECS_INV = 7;
	private static final int EQ_CHANGE_SRL = 4;
	private static final int SIGN_WARRANT = 5;
	private static final int JOB_REPLACE = 6;
	private static final int SHOW_SETTINGS = 8;
	private static final int DIALOG_MARK_START = 1;
	private static final int DIALOG_MARK_END = 2;
	private static final int DIALOG_DONE = 3;
	private static final int DIALOG_MARK_DONE = 4;
    private static final int SCAN_BARCODE = 9;
    private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private CustomWarrContAdapter mDBArrayAdapter;
    public Context mContext;
    private TextView captionView, infoView;
    private EditText textFilter;
    private String warrantId;
    private Long warrId;
    private static int warrantStateId;
    private boolean updateEnabled;
    private int warrantLocalState;
    private String warrNumDate, warrNum, warrContragentInfo, subEntName;
    private String warrEntAddr, warrEntNm, warrEntId, warrIdContract, warrRegl;
    private String warrRemark, warrWorkTimeMark;
    private double warrEntLat, warrEntLng;
    private WarrContRecord warrCont, selectedRecord;
	private View markBeginButton, markEndButton, entMapButton, infoScroll;
	private boolean updated, autoWorktimeMark;
	private int dialogMode;
	private GPSTracker mGPS;
	private double mLat, mLong;	
	private int checkedCntr = 0, newEqId, newEqJobId;
	private long newSpecsId;
	private String newEqSrl, newEqInv;
	private int selRecId, selJobId, warrToSign, arraySize;
	private long selSpecsId;
	private Date warrOpenDate;
	
    private MyGLocationService locationConnectionService;
    private Intent locationServiceIntent; 
    private String sPosition;
    private View viewContainer;
    private boolean searchPref, infoPref, btnPref;
    private ModeCallback mSelectedMode = null;
    private ActionMode mActionMode;

	
    private static class CustomWarrContAdapter extends ArrayAdapter<WarrContRecord> {
    	ArrayList<WarrContRecord> warrArray, warrFiltered;
    	WarrantFilter warrantFilter;
    	String preConstraint = "";
    	Context context;    	
    	private WarrContActivity ownerActivity;    	
    	
    	static class WarrContViewHolder {
    		public TextView textId;
    	    public TextView textEq;
    	    public TextView textEqState;
    	    public CheckBox checkBox;
    	    public TextView textEqSrl;
    	    public TextView textJob;
    	    public TextView textJobState;
    	    public TextView textRem;
    	    public TextView textJobCntr;
    	}

    	public CustomWarrContAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		this.context = context;
    		ownerActivity = (WarrContActivity) context;
	        this.warrArray = new ArrayList<WarrContRecord>();
	        this.warrFiltered = new ArrayList<WarrContRecord>();    		
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
    	    	    	
		public void clear() {
			super.clear();
			warrArray.clear();
//			warrFiltered.clear();
		}

		public void add(WarrContRecord item) {
			super.add(item);
			warrArray.add(item);			
//			if (preConstraint != null && preConstraint.length() > 0)
//				getFilter().filter(preConstraint);
//			else
//				getFilter().filter("");
		}

		public void addAll(ArrayList<WarrContRecord> items) {
			super.addAll(items);
			warrArray.clear();
			warrArray.addAll(items);
			warrFiltered.clear();
			warrFiltered.addAll(items);
		}
		
		public WarrContRecord getItem(int position) {
			return warrFiltered.get(position);
		}
	 
		public long getItemId(int position) {
			return position;
		}		
    	
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) {
    		WarrContViewHolder viewHolder;
	    	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) { 
				LayoutInflater inflater = (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.warr_cont_row, null);
//	    		row = LayoutInflater.from(getContext()).inflate(R.layout.warr_cont_row, parent, false);
	    		viewHolder = new WarrContViewHolder();
	    		viewHolder.textId = (TextView)row.findViewById(R.id.lineId);
	    		viewHolder.textEq = (TextView)row.findViewById(R.id.lineEq);
	    		viewHolder.textEqState = (TextView)row.findViewById(R.id.lineEqState);
	    		viewHolder.checkBox = (CheckBox) row.findViewById(R.id.checkBox);
	    		viewHolder.textEqSrl = (TextView)row.findViewById(R.id.lineEqSN);
	    		viewHolder.textJob = (TextView)row.findViewById(R.id.lineJob);
	    		viewHolder.textJobState = (TextView)row.findViewById(R.id.lineJobState);
	    		viewHolder.textRem = (TextView)row.findViewById(R.id.lineRemark);
	    		viewHolder.textJobCntr = (TextView)row.findViewById(R.id.lineJobCntr);
	    		row.setTag(viewHolder);
	    	} else {
	    		viewHolder = (WarrContViewHolder) row.getTag();	    		
	    	}
	    	final WarrContRecord item = getItem(position);
	    	viewHolder.textId.setText(""+item.id);
	    	viewHolder.textEq.setText(item.eqName);
			viewHolder.textEqSrl.setText("с/н: " + item.eq_srl + "; инв.№: " + item.eq_inv + "; " + item.specPlace);
	    	if (item.tbl_mode > 1) viewHolder.textEqSrl.setTextColor(Color.BLUE);
	    	if (item.id_external == 0) viewHolder.textEqSrl.setTextColor(Color.LTGRAY);
//	    	textEqState.setText(""+ item.eq_status); 
	    	viewHolder.textEqState.setText(item.eqStateName); 
	    	viewHolder.textJob.setText(item.jobName);
//	    	textJobState.setText(""+ item.job_status);
	    	viewHolder.textJobState.setText(item.jobStateName);
	    	viewHolder.textRem.setText(item.remark);
	    	viewHolder.textJobCntr.setText(""+ item.jobCount);
	    	if (item.specTmp > 0) viewHolder.textEq.setTextColor(CommonClass.clrDkRed);
	    	else viewHolder.textEq.setTextColor(Color.BLACK);
	    	if (!item.eqInService) viewHolder.textEq.setTextColor(Color.RED);
	    	else viewHolder.textEq.setTextColor(Color.BLACK);
	    	viewHolder.textJob.setTextColor(context.getResources().getColor(R.color.blue));
    		if (mSelection.get(position) != null) {
    			viewHolder.textJob.setTextColor(Color.WHITE);
				row.setBackgroundResource(R.drawable.list_selector_selected);    			
    		} else if (warrantStateId == WarrantRecord.W_STATE_NEW_DISCARD) {
	    		row.setBackgroundResource(R.drawable.list_selector_discarded);
	    	} else {
		    	if (item.job_status == StateRecord.JOB_STATE_IS_DONE)
		    		row.setBackgroundResource(R.drawable.list_selector_done);
		    	else if (item.modified == 1)
		    		row.setBackgroundResource(R.drawable.list_selector_modified);
		    	else
		    		row.setBackgroundResource(R.drawable.list_selector);
	    	}
	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    		viewHolder.checkBox.setEnabled(false);
	    		viewHolder.checkBox.setVisibility(View.GONE);
	    	} else {	    	
	    		viewHolder.checkBox.setChecked(item.selected);
	    		viewHolder.checkBox.setOnClickListener(ownerActivity);
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
//        	int res = 0;
//        	if (warrFiltered != null && warrFiltered.size() > 0)
//        		res = warrFiltered.size();
//        	else
//        		res = warrFiltered.size();
////        		res = super.getCount();
//        	return res;
        	return warrFiltered.size();
        }
        
	    private class WarrantFilter extends Filter {
	        @Override
	        protected FilterResults performFiltering(CharSequence constraint) {
				String filterString = constraint.toString().toLowerCase();				
				FilterResults results = new FilterResults();				
				ArrayList<WarrContRecord> list;	 
	        	
	            if (constraint == null || constraint.length() == 0) {
	                results.values = warrArray;
	                results.count = warrArray.size();
	            } else {
					if (preConstraint.length() == 0 || preConstraint.length() > constraint.length())
						list = warrArray;
					else
						list = warrFiltered;
					if (warrArray.size() > 0)
						preConstraint = constraint.toString();
	                List<WarrContRecord> nEntList = new ArrayList<WarrContRecord>();
	                for (WarrContRecord warr : list) {
	                    if (warr.eq_srl.toLowerCase().contains(filterString) ||
	                    		warr.eq_inv.toLowerCase().contains(filterString)||
	                    		warr.eqName.toLowerCase().contains(filterString)||
	                    		warr.jobName.toLowerCase().contains(filterString))
	                        nEntList.add(warr);
	                }
	                results.values = nEntList;
	                results.count = nEntList.size();	                 
	            }
	            return results;
	        }
	        
	        @SuppressWarnings("unchecked")
	        @Override
	        protected void publishResults(CharSequence constraint, FilterResults results) {
                warrFiltered = (ArrayList<WarrContRecord>) results.values;
                notifyDataSetChanged();
	        }
	    }		  
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warr_cont);		
		mContext = this;
        markBeginButton = findViewById(R.id.begin_mark_button);
        markBeginButton.setOnClickListener(this);
        markEndButton = findViewById(R.id.end_mark_button);
        markEndButton.setOnClickListener(this);
        entMapButton = findViewById(R.id.ent_map_button);
        entMapButton.setOnClickListener(this);
		captionView = (TextView)findViewById(R.id.title_text);
		infoView = (TextView)findViewById(R.id.info_text); 
		infoScroll = findViewById(R.id.scrollView1); 
		mDBArrayAdapter = new CustomWarrContAdapter(this, R.layout.warr_cont_row);
        mDBListView = (ListView) findViewById(R.id.listViewWarr);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mWarrContClickListener);
        textFilter = (EditText) findViewById(R.id.editTextFilter);
        textFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
            	WarrContActivity.this.mDBArrayAdapter.getFilter().filter(cs, new Filter.FilterListener() {
            	    public void onFilterComplete(int count) {
            	         showCounter(count);
            	    }
            	});
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            @Override
            public void afterTextChanged(Editable arg0) {}
        });  
        
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	mSelectedMode = new ModeCallback();
		    mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); 
		    mDBListView.setMultiChoiceModeListener(mSelectedMode); 
	    }						
		viewContainer = findViewById(R.id.undobar);
		
		//---get the data passed in using getIntExtra()---
		Bundle b = getIntent().getExtras();
		warrantId = b.getString("id");
		warrId = Long.parseLong(warrantId);
		warrIdContract = b.getString("id_contract");
		warrantStateId = Integer.parseInt(b.getString("id_state"));
		updateEnabled = (warrantStateId == 5 || warrantStateId == 34); 
		warrantLocalState = Integer.parseInt(b.getString("local_state"));
		warrNumDate = b.getString("num");
		warrNum = b.getString("w_num");
		warrContragentInfo = b.getString("contragent");
        subEntName = b.getString("sub_ent_nume");
		warrEntAddr = b.getString("ent_addr");
		warrEntNm = b.getString("ent_name");
		warrEntLat = Double.parseDouble(b.getString("ent_lat"));
		warrEntLng = Double.parseDouble(b.getString("ent_lng"));
		warrRemark = b.getString("rem");
		warrEntId = b.getString("id_ent");
        warrRegl = b.getString("regl");
		warrToSign = Integer.parseInt(b.getString("to_sign"));

		String warrDate = b.getString("w_date");
		try {
			warrOpenDate = new Date(dbSch.dateFormat.parse(warrDate).getTime());
		} catch (ParseException e) {
			warrOpenDate = new Date(new java.util.Date().getTime());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		int jobCntr = 0;
		dbSch.createEmpWorkTimeTable();
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		autoWorktimeMark = sharedPrefs.getBoolean("emp_time_mark_checkbox", false);
		checkMarkWorkTimeBtns();
		locationServiceIntent = new Intent(this, MyGLocationService.class);
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
    		if (D) Log.d(TAG, sPosition);
    	} 
    	
    	public void onServiceDisconnected(ComponentName className) { 
    		locationConnectionService = null; 
    	} 
    };    	
	
    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.d(TAG, "+ ON RESUME +");
		if ((warrantId != null)&&(warrantId.length() > 0)) {
			refreshWarrContList(false);
		}		
        updateWarrantInfo();
        bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        getSettingsPrefs();
    } 
	
    @Override 
    public void onPause() { 
    	super.onPause(); 
		if (locationConnectionService != null && locationConnectionService.isWorking()) {
			unbindService(locationServiceConnection);
		}
    }        
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.warr_cont, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_discard_warrant);
		if (mi != null) { 
			if (updateEnabled) {
				if (warrantStateId == WarrantRecord.W_STATE_NEW_DISCARD) 
					mi.setTitle(mContext.getString(string.action_dont_discard_warrant));			
				else
					mi.setTitle(mContext.getString(string.action_discard_warrant));
			} else mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.action_add);
		if (mi != null) { 
			mi.setEnabled(updateEnabled);
			mi.setVisible(updateEnabled);
		}		
		mi = menu.findItem(R.id.action_show_specs);
		if (mi != null) { 
			mi.setEnabled(checkedCntr == 0);
			mi.setVisible(checkedCntr == 0);
		}		
		mi = menu.findItem(R.id.action_discard_warrant);
		if (mi != null) { 
			mi.setEnabled(checkedCntr == 0);
			mi.setVisible(checkedCntr == 0);
		}		
		mi = menu.findItem(R.id.action_show_emp_mark_time);
		if (mi != null) { 
			mi.setEnabled(checkedCntr == 0);
			mi.setVisible(checkedCntr == 0);
		}		
		int mark = dbSch.getEmpWorkLastMark(warrId);
		mi = menu.findItem(R.id.action_mark_job_done);
		if (mi != null) { 
			mi.setEnabled(updateEnabled && checkedCntr > 0 && mark == EmpWorkTimeRecord.BEGIN_WORK);
			mi.setVisible(updateEnabled && checkedCntr > 0);
		}		
		mi = menu.findItem(R.id.action_remark);
		if (mi != null) { 
			mi.setEnabled(updateEnabled && checkedCntr == 1);
			mi.setVisible(updateEnabled && checkedCntr == 1);
		}		
		mi = menu.findItem(R.id.action_change_srl);
		if (mi != null) { 
			mi.setEnabled(updateEnabled && checkedCntr == 1);
			mi.setVisible(updateEnabled && checkedCntr == 1);
		}		
		mi = menu.findItem(R.id.action_wc_delete);
		if (mi != null) {
			mi.setEnabled(updateEnabled && checkedCntr > 0 && checkedCntr < mDBArrayAdapter.getCount());
			mi.setVisible(updateEnabled && checkedCntr > 0 && checkedCntr < mDBArrayAdapter.getCount());
		}
		mi = menu.findItem(R.id.action_change_job);
		if (mi != null) {
			mi.setEnabled(updateEnabled && checkedCntr == 1);
			mi.setVisible(updateEnabled && checkedCntr == 1);
		}
		mi = menu.findItem(R.id.action_sign_warrant);
		if (mi != null) {
			mi.setEnabled(/*true*/updateEnabled && warrToSign > 0 && warrantLocalState == WarrantRecord.W_DONE); // true for debug
			mi.setVisible(/*true*/updateEnabled && warrToSign > 0);
		}		
//		mi = menu.findItem(R.id.action_check_all);
//		if (mi != null) {
//			mi.setEnabled(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
//			mi.setVisible(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
//		}

		int mark_w_all = dbSch.getEmpWorkLastMark();
		mi = menu.findItem(R.id.action_begin_emp_mark);
		if (mi != null) {
			mi.setEnabled(updateEnabled && (mark != 0)&&(mark_w_all != 0)&&(warrantStateId != WarrantRecord.W_STATE_NEW_DISCARD));
		}
		mi = menu.findItem(R.id.action_end_emp_mark);
		if (mi != null) {
			mi.setEnabled((mark == 0)&&(warrantStateId != WarrantRecord.W_STATE_NEW_DISCARD));			
		}
		mi = menu.findItem(R.id.action_show_map);
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_discard_warrant:
			if (warrantStateId == WarrantRecord.W_STATE_NEW_DISCARD) {
				if (dbSch.setWarrantIdState(Long.parseLong(warrantId), WarrantRecord.W_STATE_IN_WORK))
					warrantStateId = WarrantRecord.W_STATE_IN_WORK;
			} else {
				if (dbSch.setWarrantIdState(Long.parseLong(warrantId), WarrantRecord.W_STATE_NEW_DISCARD))
					warrantStateId = WarrantRecord.W_STATE_NEW_DISCARD;
			}
			refreshWarrContList(false);
			checkMarkWorkTimeBtns();
        	return true;
        case R.id.action_remark:
        	showInputNameDialog();
        	return true;
        case R.id.action_change_srl:
        	selectEq(findSelectedEqId());
        	return true;
        case R.id.action_change_job:
        	if (findSelectedEqId() != "")
        		selectJob(JOB_REPLACE); 
        	return true;
        case R.id.action_show_emp_mark_time:
        	showEmpWorkTimeMarks();
        	return true;
        case R.id.action_add:
        	selectEq("");
        	return true;
        case R.id.action_wc_delete:
        	deleteCheckedEq();
        	return true;
        case R.id.action_sign_warrant:
        	signWarrant();
        	return true;
        case R.id.action_show_specs:
        	showSpecs();
        	return true;      
        case R.id.action_settings:
//        	CommonClass.showUndo(viewContainer);
        	showSettings();
        	return true;
        case R.id.action_check_all:
        	checkAll();
        	return true;
        case R.id.action_mark_job_done:
        	showYesNoDialog(DIALOG_DONE, getString(R.string.title_checked_jobs), 
        		getString(R.string.m_mark_job_done));        	
        	return true;
    	case R.id.action_begin_emp_mark:
    		markBeginWork();
        	return true;
    	case R.id.action_end_emp_mark:
    		markEndWork();
        	return true;
    	case R.id.action_show_map:
    		showMap();
        	return true;
            case R.id.action_scan_barcode:
                if (D) Log.d(TAG, "onOptions action_scan_barcode");
               // scanBarCode();
                showScanerActivity();
                return true;
        }
        return false;
    }
	
	private void showSettings() {
		Intent intent = new Intent(mContext, WarrContSettingsActivity.class);
		startActivityForResult(intent, SHOW_SETTINGS);		    			
	}
	
	public void onClickUndo(View view) {
	    Toast.makeText(this, "Deletion undone", Toast.LENGTH_LONG).show();
	    viewContainer.setVisibility(View.GONE);
	}
	
	// The on-click listener for ListViews
    private OnItemClickListener mWarrContClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {       	
        	warrCont = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + warrCont.id_external);
            if (warrantStateId != WarrantRecord.W_STATE_NEW_DISCARD) {
	            int mark = dbSch.getEmpWorkLastMark(warrCont.id_warrant);
	            if (!autoWorktimeMark || mark == EmpWorkTimeRecord.BEGIN_WORK) {  // интервал открыт
	            	showWarrUnit();
	            } else {
	            	showYesNoDialog(DIALOG_MARK_START, getString(R.string.title_b_emp_work_time), 
	            			getString(R.string.m_b_emp_work_time));
	            }
            }
        }
    };    
    
    private String findSelectedEqId() {
    	String res = "";
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			WarrContRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) {
				res = Integer.toString(item.id_eq);
				selRecId = item.id;
				selSpecsId = item.eq_spec;
				selJobId = item.id_job;
				break;
			}
		}
		return res;
    }
    
    private void checkAll() {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		mActionMode = startActionMode(mSelectedMode);
    		mSelectedMode.setAllSelected();
    	} else {
	    	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
	    		WarrContRecord item = mDBArrayAdapter.getItem(i);
	    		item.selected = !item.selected;
	    		if (item.selected)
	    			checkedCntr++;
	    		else
	    			checkedCntr--;
	    	}
	    	mDBArrayAdapter.notifyDataSetChanged();
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    	invalidateOptionsMenu();
	    	}
    	}
    }
    
    private void markCheckedJobDone() { 
    	Long addedId;
    	String eqStateInOrder, jobStateIsDone;
    	eqStateInOrder = dbSch.getStateName(StateRecord.EQ_STATE_IN_ORDER);
    	jobStateIsDone = dbSch.getStateName(StateRecord.JOB_STATE_IS_DONE);
    	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
    		WarrContRecord item = mDBArrayAdapter.getItem(i);
    		if (item.selected) {
    			addedId = dbSch.addWarrUnitItem(0, item.id, item.id_job, 2, "");
    			if (addedId > 0)
    				item.jobCount++;
    			if (dbSch.setWarrContJobEqState(Long.parseLong(warrantId), item.id, 
    				StateRecord.JOB_STATE_IS_DONE, StateRecord.EQ_STATE_IN_ORDER)) {
    				item.eq_status = StateRecord.EQ_STATE_IN_ORDER;
    				item.job_status = StateRecord.JOB_STATE_IS_DONE;
    				item.eqStateName = eqStateInOrder;
    				item.jobStateName = jobStateIsDone;    				
    				item.selected = false;
    				checkedCntr--;
    			}
    		}
    	}
    	mDBArrayAdapter.notifyDataSetChanged();
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	invalidateOptionsMenu();
    	}		    	
    }
    
    private void deleteCheckedEq() {
    	boolean needUpdate = false;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			WarrContRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) {
				if (item.jobCount == 0) {
					if (dbSch.removeWarrContSpecsId(item.id, item.id_external, item.eq_spec)) {
						needUpdate = true;
						checkedCntr--;
					}
				} else {
					showDelMessage();
					item.selected = false;
					checkedCntr--;
					needUpdate = true;
				}
			}
		}    	
		if (needUpdate) {
			refreshWarrContList(true);
			updated = true;
		}		
    }
            
	private void showDelMessage() {
		CommonClass.showMessage(mContext, getString(R.string.m_del_warr_cont), 
				getString(R.string.m_del_warr_cont_unit));
	}
        
	private void showSpecExistsMessage() {
		CommonClass.showMessage(mContext, getString(R.string.m_add_warr_cont), 
				getString(R.string.m_add_warr_cont_error) + getString(R.string.m_warr_cont_exists));
	}
        
	private void showChangeSpecExistsMessage() {
		CommonClass.showMessage(mContext, getString(R.string.m_change_warr_cont), 
				getString(R.string.m_change_warr_cont_error) + getString(R.string.m_warr_cont_exists));
	}

	private void showSpecs() {
		Intent intent = new Intent(mContext, SpecsSelectActivity.class);
		Bundle b = new Bundle();
		b.putString("ident", warrEntId);
		b.putString("idcontract", ""/*warrIdContract*/); // показывать спецификацию по всем договорам
		b.putString("idgds", "");
		b.putString("idwarr", "0");
		intent.putExtras(b); 
		startActivityForResult(intent, EQ_SPECS_INV);    			
	}
	
    private void selectEq(String idGds) {
    	int reqCode;
		Intent intent = new Intent(mContext, SpecsSelectActivity.class);
		Bundle b = new Bundle();
		b.putString("ident", warrEntId);
		b.putString("idcontract", warrIdContract);
		b.putString("idgds", idGds);
		b.putString("idwarr", warrantId);
		b.putString("idwarr_cont", warrantId);
		if (idGds.length() > 0)
			reqCode = EQ_CHANGE_SRL;
		else
			reqCode = EQ_SELECT;			
		intent.putExtras(b); 
		startActivityForResult(intent, reqCode);    	
    }
    
    private void selectJob(int resultCode) {
		Intent intent = new Intent(mContext, JobGroupSelectActivity.class);
		Bundle b = new Bundle();
		b.putString("ttype", Integer.toString(JobGroupSelectActivity.TYPE_JOB_SELECT));		
		intent.putExtras(b); 
		startActivityForResult(intent, resultCode);    	
    }

    private void setModified() {
        dbSch.setWarrantModifiedState(Integer.parseInt(warrantId));
        updated = true;    	
    }
    
	private void refreshWarrContList(boolean setModified) {
		if (setModified) {
			setModified();
		}
		showWarrCont(dbSch, "id_warrant = "+ warrantId + " AND "+
				WarrUnitTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION);
	}	
    
    private int showWarrCont(DBSchemaHelper sh, String sWhere) {
    	String q_eq = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
    			" WHERE "+ StateTable.ID_EXTERNAL +" = "+ WarrContTable.EQ_STATUS +")as "+
    			WarrContRecord.EQ_STATE_NM;
    	String q_job = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
    			" WHERE "+ StateTable.ID_EXTERNAL +" = "+ WarrContTable.JOB_STATUS +")as "+ 
    			WarrContRecord.JOB_STATE_NM;
    	String eq_spec_place = "(SELECT "+ SpecsTable.PLACE +" FROM "+ SpecsTable.TABLE_NAME +
    			" WHERE "+ SpecsTable.ID_EXTERNAL +" = "+ WarrContTable.EQ_SPEC+")as "+ 
    			WarrContRecord.EQ_SPEC_PLACE;
    	String eq_spec_tmp = "(SELECT "+ SpecsTable.TEMP +" FROM "+ SpecsTable.TABLE_NAME +
    			" WHERE "+ SpecsTable.ID_EXTERNAL +" = "+ WarrContTable.EQ_SPEC+")as "+ 
    			WarrContRecord.EQ_SPEC_TMP;
    	String eq_spec_bd = "(SELECT "+ SpecsTable.BEGIN_DATE +" FROM "+ SpecsTable.TABLE_NAME +
    			" WHERE "+ SpecsTable.ID_EXTERNAL +" = "+ WarrContTable.EQ_SPEC+")as "+ 
    			WarrContRecord.EQ_SPEC_BDATE;
    	String eq_spec_ed = "(SELECT "+ SpecsTable.END_DATE +" FROM "+ SpecsTable.TABLE_NAME +
    			" WHERE "+ SpecsTable.ID_EXTERNAL +" = "+ WarrContTable.EQ_SPEC+")as "+ 
    			WarrContRecord.EQ_SPEC_EDATE;
    	
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;
    	Cursor cGds;
    	String query;
    	
    	if (sWhere.length() == 0) {
    		query = "SELECT *,"+ q_eq + ", "+ q_job + ", "+ eq_spec_place + ", "+ 
    				eq_spec_tmp + ", "+ eq_spec_bd + ", "+ eq_spec_ed + " from " + WarrContTable.TABLE_NAME + 
    				" ORDER BY " + WarrContTable.ID_EXTERNAL + " DESC";
    	} else {
    		query = "SELECT *,"+ q_eq + ", "+ q_job + ", "+ eq_spec_place + ", "+ 
    				eq_spec_tmp + ", "+ eq_spec_bd + ", "+ eq_spec_ed + " from " + WarrContTable.TABLE_NAME + 
    				" WHERE " + sWhere + " ORDER BY " + WarrContTable.ID_EXTERNAL;
    	}
    	checkedCntr = 0;
    	arraySize = 0; int colid;
		try {
			c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		arraySize++;
	    		warrCont = new WarrContRecord(c);	    		
	    		cGds = sh.getGdsName(warrCont.id_job);
	    		if (cGds != null) {
	    			colid = cGds.getColumnIndex(GoodsTable.NM);
	    			warrCont.jobName = cGds.getString(colid);
	    		}
	    		cGds = sh.getGdsName(warrCont.id_eq);
	    		if (cGds != null) {
	    			colid = cGds.getColumnIndex(GoodsTable.NM);
	    			warrCont.eqName = cGds.getString(colid);
	    		}
	    		if (cGds != null) 
	    			cGds.close();
				warrCont.jobCount = (int) sh.getWarrContUnitCount(warrCont.id);
				warrCont.eqInService = CommonClass.isEqServDateValid(warrCont.specBeginDate, warrCont.specEndDate, warrOpenDate);
	    		
	    		if (D) Log.d(TAG, "WarrCont = " + warrCont.id_external);
	    		mDBArrayAdapter.add(warrCont);
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {    	
	    	if (D) Log.d(TAG, "WarrCont Count = " + arraySize);
	    	showCounter(arraySize);
//	        getSettingsPrefs();	    	
	    	if (c != null) c.close();
	        String filter = textFilter.getText().toString(); 
	        if (filter != null && filter.length() >= 0) {
            	WarrContActivity.this.mDBArrayAdapter.getFilter().filter(filter, new Filter.FilterListener() {
            	    public void onFilterComplete(int count) {
            	         showCounter(count);
            	    }
            	});	        	
	        }	    	
		}
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	invalidateOptionsMenu();
    	}		
    	return arraySize;
    }
    
    private void showCounter(int cntr) {
    	captionView.setText(getString(R.string.caption_warr_cont_info) + cntr);
    }
    
    private java.util.Date removeTime(Date date) {    
        Calendar cal = Calendar.getInstance();  
        cal.setTime(date);  
        cal.set(Calendar.HOUR_OF_DAY, 0);  
        cal.set(Calendar.MINUTE, 0);  
        cal.set(Calendar.SECOND, 0);  
        cal.set(Calendar.MILLISECOND, 0);  
        return cal.getTime(); 
    }
    
	@Override
	public void onClick(View v) {
		if (v instanceof CheckBox) { 
			RelativeLayout parent = (RelativeLayout) v.getParent();	    			
			TextView idTV = (TextView)parent.findViewById(R.id.lineId);
			long id = Long.parseLong(idTV.getText().toString());
			WarrContRecord item = findItem(id);
		    if (((CheckBox)v).isChecked()) { 
//		    	clearAllChecks();
		    	item.selected = true;
		    	checkedCntr++;
		    	mDBArrayAdapter.notifyDataSetChanged();
		    } else { 
		    	item.selected = false;
		    	checkedCntr--;
		    }
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    	invalidateOptionsMenu();
	    	}		
		} else {
	    	switch (v.getId()) {
	    	case R.id.begin_mark_button:
	    		if (D) Log.d(TAG, "clicked on begin_mark_button");
	    		markBeginWork();
	    		break;
	    	case R.id.end_mark_button:
	    		if (D) Log.d(TAG, "clicked on end_mark_button");
	    		markEndWork();
	    		break;
	    	case R.id.ent_map_button:
	    		if (D) Log.d(TAG, "clicked on ent_map_button");
	    		showMap();
	    		break;
	       	}		
		}
	}
	
	private void showMap() {
		if (warrEntLat != 0 && warrEntLng != 0) {
			CommonClass.showEntEmpLocationOnMap(mContext, warrEntLat, warrEntLng, warrEntNm, warrEntAddr);
		} else {
    		String addrStr = warrEntAddr + getString(string.m_city_odesa);
    		String newAddrStr = addrStr.replaceAll(" ", "+");
    		sendGeocodeRequest(newAddrStr);
		}
//    		getLatLongFromAddress(newAddrStr);		
	}
	
    private WarrContRecord findItem(long id) {
    	WarrContRecord result = null;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			WarrContRecord item = mDBArrayAdapter.getItem(i);
			if (item.id == id) {
				result = item;
				break;
			}
		}
		return result;
    }
    
    private void clearAllChecks() {
    	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
    		WarrContRecord item = mDBArrayAdapter.getItem(i);
    		item.selected = false;
    	}
    	checkedCntr = 0;
    }
	
	private void updateWarrantInfo() {
		warrWorkTimeMark = dbSch.getEmpWorkLastMarkInterval(warrId);
		infoView.setText(String.format(getString(string.m_wc_footer),
				warrNumDate, warrRemark, warrContragentInfo, warrWorkTimeMark));
		int lastMarkModified = dbSch.getEmpWorkLastMarkModified(warrId);
		if (lastMarkModified > 0) infoView.setTextColor(Color.CYAN);
		else infoView.setTextColor(Color.WHITE);
	}
	
	private void checkMarkWorkTimeBtns() {
		int mark = dbSch.getEmpWorkLastMark(Long.parseLong(warrantId));
		int mark_w_all = dbSch.getEmpWorkLastMark();
		markBeginButton.setEnabled(updateEnabled && (mark != 0)&&(mark_w_all != 0)&&(warrantStateId != WarrantRecord.W_STATE_NEW_DISCARD));
		markEndButton.setEnabled(/*updateEnabled &&*/ (mark == 0)&&(warrantStateId != WarrantRecord.W_STATE_NEW_DISCARD));
	}

	private void sendResult() {
        Intent returnIntent = new Intent();
		Bundle b = new Bundle();
		b.putString("updated", Integer.toString((updated) ? 1 : 0));
		returnIntent.putExtras(b); 
        setResult(RESULT_OK, returnIntent);		
	}
	
	@Override
	public void onBackPressed() {
		if (autoWorktimeMark && (dbSch.getWarrantLocalState(Long.parseLong(warrantId)) == WarrantRecord.W_DONE) &&
				(dbSch.getEmpWorkLastMark(Long.parseLong(warrantId)) == 0)){
        	showYesNoDialog(DIALOG_MARK_END, getString(R.string.title_e_emp_work_time), 
        			getString(R.string.m_e_emp_work_time));			
		} else {
			sendResult();
			super.onBackPressed();
		}
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case JOB_SHOW:
                if (data.hasExtra("updated")) {
                	int updated = Integer.parseInt(data.getExtras().getString("updated"));
                	if (updated > 0) {
                		this.updated = true;
                		refreshWarrContList(false);
                	}
                }
                break;
    		case EQ_SELECT:
                if (data.hasExtra("id")) {
                	newSpecsId = Long.parseLong(data.getExtras().getString("id"));
                	newEqId = Integer.parseInt(data.getExtras().getString("idEq"));
                	newEqSrl = data.getExtras().getString("EqSrl");
                	newEqInv = data.getExtras().getString("EqInv");
        			if (!dbSch.isWarrContSpecsIdPresent(newSpecsId, warrId, true)) {
        				selectJob(JOB_SELECT);
        			} else {
        				showSpecExistsMessage();
        			}
                }
    			break;    	
    		case EQ_CHANGE_SRL:
                if (data.hasExtra("id")) {
                	newSpecsId = Long.parseLong(data.getExtras().getString("id"));
                	newEqId = Integer.parseInt(data.getExtras().getString("idEq"));
                	newEqSrl = data.getExtras().getString("EqSrl");
                	newEqInv = data.getExtras().getString("EqInv");
        			if (!dbSch.isWarrContSpecsIdPresent(newSpecsId, warrId, true)) {
        				changeEqSrl(selRecId, selSpecsId, newSpecsId, newEqId, newEqSrl, newEqInv);
        			} else {
        				showChangeSpecExistsMessage();
        			}
                }    			
    			break;
    		case JOB_SELECT:
                if (data.hasExtra("id")) {
                	newEqJobId = Integer.parseInt(data.getExtras().getString("id"));
                	addWarrContEq(newSpecsId, newEqId, newEqJobId, newEqSrl, newEqInv);
                }
    			break;
    		case JOB_REPLACE:
                if (data.hasExtra("id")) {
                	int newJobId = Integer.parseInt(data.getExtras().getString("id"));
                	changeEqJob(selRecId, selJobId, newJobId);
                }
    			break;
    		case SIGN_WARRANT:
    			
    			break;
    		case EQ_SPECS_INV:
    			refreshWarrContList(false);
    			break;
    		case SHOW_SETTINGS:
    			getSettingsPrefs();
    			break;
    		}
        }
    }
    
    private boolean isArraySmall() {
    	int size = mDBArrayAdapter.getCount();
    	if (size == 0)
    		size = arraySize;
    	return size <= 3;
    }
    
	private void getSettingsPrefs() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		searchPref = sharedPrefs.getBoolean("search_checkbox", true);
		infoPref = sharedPrefs.getBoolean("info_checkbox", false);
		btnPref = sharedPrefs.getBoolean("buttons_checkbox", false);
		boolean choice = searchPref;
		textFilter.setVisibility(choice ? View.VISIBLE : View.GONE);
		choice = infoPref || isArraySmall();
		infoScroll.setVisibility(choice ? View.VISIBLE : View.GONE);
		choice = btnPref || isArraySmall();
		markBeginButton.setVisibility(choice ? View.VISIBLE : View.GONE);
		markEndButton.setVisibility(choice ? View.VISIBLE : View.GONE); 
		entMapButton.setVisibility(choice ? View.VISIBLE : View.GONE);
	}    
    
    private void addWarrContEq(long specsId, int eqId, int jobId, String srl, String inv) {
    	dbSch.addWarrContItem(0, warrId, eqId, StateRecord.EQ_STATE_NOTIN_ORDER, specsId, srl, inv, 
    		jobId, StateRecord.JOB_STATE_IS_NOTDONE, 2, "");
        refreshWarrContList(true);
        updated = true;
    }    
    
    private void changeEqSrl(long wcId, long oldSpecsId, long specsId, int eqId, String srl, String inv) {
    	if (oldSpecsId > 0 && oldSpecsId != specsId) {
	    	if (dbSch.changeWarrContEqSrl(wcId, specsId, eqId, srl, inv)) {
	    		refreshWarrContList(true);
	    		checkedCntr = 0; 
    		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    		    invalidateOptionsMenu();
		    	}
	    		updated = true;
	    	}
    	}
    }
    
    private void changeEqJob(long wcId, long oldJobId, long jobId) {
    	if (oldJobId > 0 && oldJobId != jobId) {
	    	if (dbSch.changeWarrContEqJob(wcId, jobId)) {
	    		refreshWarrContList(true);
	    		checkedCntr = 0; 
    		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    		    invalidateOptionsMenu();
		    	}
	    		updated = true;
	    	}
    	}
    }
    
    private void signWarrant() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        Bundle b = new Bundle();
        b.putString("idWarrant", warrantId);
        b.putString("idEnt", warrEntId); 
        intent.putExtras(b); 
        startActivityForResult(intent, SIGN_WARRANT);    	    	
    }

    private void showEmpWorkTimeMarks() {
    	CommonClass.showWarrantEmpWorkTimeMarks(mContext, warrantId, warrNum);
    }


    private void showScanerActivity() {
        CommonClass.showScannerActivity(mContext, warrantId, warrNum ,warrEntAddr , warrEntNm,
                warrIdContract, warrRemark, warrEntId, warrContragentInfo, subEntName, warrNumDate);
    }

    
    private void showWarrUnit() {
        Intent intent = new Intent(mContext, WarrUnitActivity.class);
        Bundle b = new Bundle();
        b.putString("id", Integer.toString(warrCont.id)); //Your id
        b.putString("id_c", Long.toString(warrCont.id_external));
        b.putString("id_w", warrantId); //Warrant id
        b.putString("update_enabled", Boolean.toString(updateEnabled));
        b.putString("num", warrNumDate);
        b.putString("job", warrCont.jobName);
        b.putString("idjob", Integer.toString(warrCont.id_job));
        b.putString("eq", warrCont.eqName);
        b.putString("id_eq", Long.toString(warrCont.id_eq));
        b.putString("id_specs", Long.toString(warrCont.eq_spec));
        b.putString("rem", warrCont.remark);
        b.putString("SrvBeginDate", warrCont.beginDate);
        b.putString("SrvEndDate", warrCont.endDate);
        intent.putExtras(b); //Put your id to your next Intent
        startActivityForResult(intent, JOB_SHOW);    	
    }
    
	//===YES/No Dialog===
	private void showYesNoDialog(int dialogMode, String dialogTitle, String dialogText) {
		this.dialogMode = dialogMode;
		FragmentManager fragmentManager = getSupportFragmentManager(); 
		YesNoDialogFragment yesnoDialog = new YesNoDialogFragment();
		yesnoDialog.setCancelable(false);
		yesnoDialog.setDialogTitle(dialogTitle);
		yesnoDialog.setDialogText(dialogText);
		yesnoDialog.show(fragmentManager, "yes/no dialog");
	}
	
	private boolean getCurrentPosition() {
		boolean res = false;
		String mLog;
		if (locationConnectionService != null && locationConnectionService.canGetLocation()) {
    		Location location = locationConnectionService.getLocation();
    		if (location != null) {
	    		mLat = location.getLatitude();
				mLong = location.getLongitude();
				mLog = String.format(getString(R.string.m_location), mLat, mLong);
    		} else {
    			mLat = 0;
    			mLong = 0;
    			mLog = getString(R.string.m_no_location);    			
    		}
		} else {
			mLat = 0;
			mLong = 0;
			mLog = getString(R.string.m_no_location);
		}		
		if (D) Log.d(TAG, mLog);
		return res;
	}
	
	private void markBeginWork() {
		getCurrentPosition();
		dbSch.addEmpWorkTime(Integer.parseInt(warrEntId), Long.parseLong(warrantId), 
				new java.util.Date(), EmpWorkTimeRecord.BEGIN_WORK, mLat, mLong);
		checkMarkWorkTimeBtns();
		updateWarrantInfo();
		if (CommonClass.keepLog) 
			dbSch.addLogItem(LogsRecord.WARNING, new java.util.Date(), String.format(
					getString(string.m_wt_mark_begin), warrNumDate));
		
	}
	
	private void markEndWork() {
		getCurrentPosition();
		dbSch.addEmpWorkTime(Integer.parseInt(warrEntId), Long.parseLong(warrantId), 
				new java.util.Date(), EmpWorkTimeRecord.END_WORK, mLat, mLong);
		dbSch.setWarrantModifiedState(Long.parseLong(warrantId), true);
		checkMarkWorkTimeBtns();
		updateWarrantInfo();
		if (CommonClass.keepLog) 
			dbSch.addLogItem(LogsRecord.WARNING, new java.util.Date(), String.format(
					getString(string.m_wt_mark_end), warrNumDate));
	}
	
	@Override
	public void onFinishYesNoDialog(boolean state) {
//		Toast.makeText(this, "Returned from dialog: "+ state, Toast.LENGTH_SHORT).show();
		switch (dialogMode) {
			case DIALOG_MARK_START:
				if (state) {
					markBeginWork();
				}
				showWarrUnit();
				break;
			case DIALOG_MARK_END:
				if (state) {
					markEndWork();
				}
				sendResult();
				this.finish();
				break;
			case DIALOG_DONE:
				if (state) {
					markCheckedJobDone();
				}
				break;
			case DIALOG_MARK_DONE:
				if (state) {
					markSelectedJobDone();
				}
				break;
		}
	}

	private void showInputNameDialog() {
		WarrContRecord wc = null;
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
    		wc = selectedRecord;
    	else
    		wc = getCheckedRecord();
		if (wc != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			InputNameDialogFragment inputNameDialog = new InputNameDialogFragment();
			inputNameDialog.setCancelable(false);
			inputNameDialog.setDialogTitle(getString(string.m_add_comment));
			inputNameDialog.setDialogText(wc.remark);
			inputNameDialog.show(fragmentManager, "input dialog");
		}
	}	
	
	@Override
	public void onFinishInputDialog(String inputText) {
//		Toast.makeText(this, "Returned from dialog: "+ inputText, Toast.LENGTH_SHORT).show();
		WarrContRecord wc = null;
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { 
    		wc = selectedRecord;    	
    		if (wc != null) {
    			if (dbSch.updateWarrContRemark(wc.id, inputText)) {
    				wc.remark = inputText;
    				setModified();
    				mDBArrayAdapter.notifyDataSetChanged();
    				selectedRecord = null;
    			}
    		}
    	} else {
    		wc = getCheckedRecord();
    		if (wc != null) {
    			if (dbSch.updateWarrContRemark(wc.id, inputText))
    				refreshWarrContList(true);
    		}
    	}
	}   
		
	private WarrContRecord getCheckedRecord() {
		WarrContRecord wc = null;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			WarrContRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) {
				wc = item;
				break;
			}
		}
		return wc;
	}	
	
    private void markSelectedJobDone() { 
    	mSelectedMode.markSelectedJobDone();
    }
    		
	private class ModeCallback implements ListView.MultiChoiceModeListener {
		private MenuItem miRemark, miDelete, miSrl, miJob, miMarkJobDone, miSelectAll;
		private ActionMode curMode;

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.warr_cont_rowselection, menu);
            mode.setTitle(mContext.getString(R.string.selected));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        	miRemark = menu.findItem(R.id.action_remark);
    		miDelete = menu.findItem(R.id.action_delete);
    		miSrl = menu.findItem(R.id.action_change_srl); 
    		miJob = menu.findItem(R.id.action_change_job); 
    		miMarkJobDone = menu.findItem(R.id.action_mark_job_done);
    		miSelectAll = menu.findItem(R.id.action_select_all);
    		updateMenuItems();
            return true;
        }

        private void updateMenuItems() {
        	int checkedCount = mDBListView.getCheckedItemCount();
    		if (miRemark != null)
    			miRemark.setEnabled(updateEnabled && checkedCount == 1);
    		if (miSrl != null)
    			miSrl.setEnabled(updateEnabled && checkedCount == 1);
    		if (miJob!= null)
    			miJob.setEnabled(updateEnabled && checkedCount == 1);
    		if (miMarkJobDone != null)
    			miMarkJobDone.setEnabled(updateEnabled && checkedCount > 0);
    		if (miDelete != null)
    			miDelete.setEnabled(updateEnabled && checkedCount > 0);
        }
        
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        	curMode = mode;
            switch (item.getItemId()) {
            case R.id.action_remark:       
            	selectedRecord = getSelectedRecord();
            	showInputNameDialog();             	
                mode.finish();
                return true;
            case R.id.action_delete:
            	deleteSelected();
            	mode.finish();
            	return true;
            case R.id.action_change_srl:
            	selectEq(getSelectedRecordId()); 
            	mode.finish();
            	return true;
            case R.id.action_change_job:
            	if (getSelectedRecordId() != "")
            		selectJob(JOB_REPLACE);             	
            	mode.finish();
            	return true;
            case R.id.action_mark_job_done:
            	showYesNoDialog(DIALOG_MARK_DONE, getString(R.string.title_checked_jobs), 
            			getString(R.string.m_mark_job_done));        	
//            	mode.finish();
            	return true;
            case R.id.action_select_all:
            	setAllSelected();
            	return true;
            default:
                return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
        	mDBArrayAdapter.clearSelection();
        }
        
        private void setTitle() {
	    	int checkedCount = mDBListView.getCheckedItemCount();
            switch (checkedCount) {
            case 0:
            	curMode.setSubtitle(null);
                break;
            default:
            	curMode.setSubtitle("" + checkedCount);
                break;
            }
            updateMenuItems();        	
        }

        public void onItemCheckedStateChanged(ActionMode mode,
                int position, long id, boolean checked) {
        	curMode = mode;
            if (checked) 
            	mDBArrayAdapter.setNewSelection(position, checked);                    
            else 
            	mDBArrayAdapter.removeSelection(position);
            setTitle();
        }
        
        public void setAllSelected() {
	    	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
	    		mDBArrayAdapter.setNewSelection(i, true);
	    		mDBListView.setItemChecked(i, true);
	    	}
	    	setTitle();
        }
        
    	private WarrContRecord getSelectedRecord() {
    		WarrContRecord wcont = null;
    		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
    			if (mDBArrayAdapter.isPositionChecked(i)) {
    				wcont = mDBArrayAdapter.getItem(i);        
    				break;
    			}
    		}
    		return wcont;
    	}
    	
    	private String getSelectedRecordId() {
    		WarrContRecord item = null;
    		String res = "";
    		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
    			if (mDBArrayAdapter.isPositionChecked(i)) {
    				item = mDBArrayAdapter.getItem(i);
    				res = Integer.toString(item.id_eq);
    				selRecId = item.id;
    				selSpecsId = item.eq_spec;
    				selJobId = item.id_job;
    				break;
    			}
    		}
    		return res;
    	}
    	
        private void deleteSelected() {
        	boolean needUpdate = false;
        	for (int i = mDBArrayAdapter.getCount() - 1; i >= 0; i--) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        			WarrContRecord item = mDBArrayAdapter.getItem(i);            			
    				if (item.jobCount == 0) {
    					if (dbSch.removeWarrContSpecsId(item.id, item.id_external, item.eq_spec)) {
    						needUpdate = true;
		        	    	mDBArrayAdapter.remove(item);
    					}
    				} else {
    					showDelMessage();
    					needUpdate = true;
    				}
        		}
        	}
    		if (needUpdate) {
    			updated = true;
//    			setModified();
    		}		
//        	mDBArrayAdapter.clearSelection();
        	mDBArrayAdapter.notifyDataSetChanged();
        	//showCounter(mDBArrayAdapter.getCount());
        	refreshWarrContList(true);
        }        
        
        private void markSelectedJobDone() { 
        	boolean needUpdate = false;
        	Long addedId;
        	String eqStateInOrder, jobStateIsDone;
        	eqStateInOrder = dbSch.getStateName(StateRecord.EQ_STATE_IN_ORDER);
        	jobStateIsDone = dbSch.getStateName(StateRecord.JOB_STATE_IS_DONE);
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
    			if (mDBArrayAdapter.isPositionChecked(i)) {
    				WarrContRecord item = mDBArrayAdapter.getItem(i);        
        			addedId = dbSch.addWarrUnitItem(0, item.id, item.id_job, 2, "");
        			if (addedId > 0) {
        				item.jobCount++;
        				needUpdate = true;
        			}
        			if (dbSch.setWarrContJobEqState(Long.parseLong(warrantId), item.id, 
        				StateRecord.JOB_STATE_IS_DONE, StateRecord.EQ_STATE_IN_ORDER)) {
        				item.eq_status = StateRecord.EQ_STATE_IN_ORDER;
        				item.job_status = StateRecord.JOB_STATE_IS_DONE;
        				item.eqStateName = eqStateInOrder;
        				item.jobStateName = jobStateIsDone;    				
        				item.selected = false;
        			}				
    			}
        	}
    		if (needUpdate) {
    			updated = true;
    			setModified();
    		}		        	
        	mDBArrayAdapter.notifyDataSetChanged();
   	    	invalidateOptionsMenu();
    	    curMode.finish();
        }
        
                
    }
	
	public static void getLatLongFromAddress(String youraddress) {
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
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    JSONObject jsonObject = new JSONObject();
	    try {
	        jsonObject = new JSONObject(stringBuilder.toString());

	        double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
	            .getJSONObject("geometry").getJSONObject("location")
	            .getDouble("lng");

	        double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
	            .getJSONObject("geometry").getJSONObject("location")
	            .getDouble("lat");

	        Log.d("latitude", Double.toString(lat));
	        Log.d("longitude", Double.toString(lng));
	    } catch (JSONException e) {
	        e.printStackTrace();
	    }

	}

	private void sendGeocodeRequest(String address) {
	    String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                address + "&sensor=false";

		HttpGet searchRequest = new HttpGet(uri);
		RestTask task = new RestTask();
		task.setResponseCallback((ResponseCallback) mContext);
		task.execute(searchRequest);	
	}	
	
	
	@Override
	public void onRequestSuccess(String response) {
		if (D) Log.d(TAG, "onRequestSuccess");		
	    JSONObject jsonObject = new JSONObject();
	    try {
	        jsonObject = new JSONObject(response);

	        double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
	            .getJSONObject("geometry").getJSONObject("location")
	            .getDouble("lng");

	        double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
	            .getJSONObject("geometry").getJSONObject("location")
	            .getDouble("lat");

	        Log.d("latitude", Double.toString(lat));
	        Log.d("longitude", Double.toString(lng));
	        CommonClass.showLocationOnMap(mContext, lat, lng);
	    } catch (JSONException e) {
	        e.printStackTrace();
	    }		
	}

	@Override
	public void onRequestError(Exception error) {
		String errMsg;
		errMsg = "onRequestError: "+ error.getMessage();
		if (D) Log.d(TAG, errMsg);		
	}

}
