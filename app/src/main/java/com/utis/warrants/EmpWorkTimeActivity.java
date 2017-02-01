package com.utis.warrants;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.utis.warrants.record.EmpWorkTimeRecord;
import com.utis.warrants.tables.EmpWorkTimeTable;
import com.utis.warrants.tables.WarrantTable;

public class EmpWorkTimeActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{
	private static final boolean D = true;
	private static final String TAG = "EmpWorkTimeActivity";
	private TextView caption, warrCaption;
    private DBSchemaHelper dbSch;
    private ListView mDBListView; 
    private String warrantId, warrantNum;
    private Long idWarr;
    private String sPosition;
    public Context mContext;
    private Spinner warrChooser; 
//    private ArrayAdapter<LocationRecord> mDBArrayAdapter;
    private CustomEmpWorkTimeAdapter mDBArrayAdapter;
    private String[] wNumbers;
    private String wNum = "";
    private static int dkGreen;
    private SwipeRefreshLayout swipeLayout;

    private static class CustomEmpWorkTimeAdapter extends ArrayAdapter<EmpWorkTimeRecord> {
    	private int id;
    	private Context context;
    	private String sInterval, sStart, sStop; 
  	
		public CustomEmpWorkTimeAdapter(Context context, int textViewResourceId) { 
			//Call through to ArrayAdapter implementation 
			super(context, textViewResourceId);
			id = textViewResourceId;
			this.context = context;
			sInterval = context.getString(R.string.m_interval); 
			sStart = context.getString(R.string.m_start);
			sStop = context.getString(R.string.m_stop);
		} 
		
    	static class EmpWorkTimeViewHolder {
    	    public TextView textTimeIn;
    	    public TextView textIn;
    	    public TextView textTimeOut;
    	    public TextView textOut;
    	}    			
  	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	    	View row = convertView; 
	    	EmpWorkTimeViewHolder viewHolder;
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) { 
				LayoutInflater inflater = (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.emp_worktime_row, null);
//	    		row = LayoutInflater.from(getContext()).inflate(R.layout.emp_msg_row, parent, false);
	    		viewHolder = new EmpWorkTimeViewHolder();
	    		viewHolder.textTimeIn = (TextView)row.findViewById(R.id.empWTTime);
	    		viewHolder.textIn = (TextView)row.findViewById(R.id.empWTCaption);
	    		viewHolder.textTimeOut = (TextView)row.findViewById(R.id.empWTModif);
	    		viewHolder.textOut = (TextView)row.findViewById(R.id.empWTRem);
	    		row.setTag(viewHolder);
	    	} else {
	    		 viewHolder = (EmpWorkTimeViewHolder) row.getTag();
	    	}
	    	EmpWorkTimeRecord item = getItem(position);
	    	if (item.be == EmpWorkTimeRecord.BEGIN_WORK) {
	    		viewHolder.textTimeIn.setText(item.getMarkDateStr());
	    		viewHolder.textIn.setText("");
	    		viewHolder.textTimeOut.setText(sStart);
	    		viewHolder.textOut.setText("id="+ item.id_external);
	    	} else {
//	    		viewHolder.textTimeIn.setText("");
//	    		viewHolder.textIn.setText("");
//	    		viewHolder.textTimeOut.setText(""+item.getMarkDateStr());
//	    		viewHolder.textOut.setText("Уход");
	    		viewHolder.textTimeIn.setText(""+ item.getMarkDateStr());
	    		viewHolder.textIn.setText(sInterval + item.getIntervalStr());
	    		viewHolder.textTimeOut.setText(sStop);
	    		viewHolder.textOut.setText("id="+ item.id_external);
	    	}
	    	int timeColor;
	    	if (item.modified != 0) {
	    		if (item.be == 0) timeColor = Color.BLUE;
		    	else timeColor = dkGreen; 
	    	} else 
	    		timeColor = Color.BLACK; 		    	 
    		viewHolder.textTimeIn.setTextColor(timeColor);
    		viewHolder.textIn.setTextColor(timeColor);
    		viewHolder.textTimeOut.setTextColor(timeColor);
    		viewHolder.textOut.setTextColor(timeColor);	  
    		return row;
	    	
/*			
			 View mView =  super.getView(position, convertView, parent);
			 TextView tv = (TextView) mView.findViewById(android.R.id.text1);
		     if (tv != null) {
		    	 EmpWorkTimeRecord item = getItem(position);
		    	 if (item.modified != 0) {
			    	 if (item.be == 0) {			    		 
			    		 tv.setTextColor(Color.BLUE);
			    	 } else {
			    		 tv.setTextColor(dkGreen);
			    	 }
		    	 } else tv.setTextColor(Color.BLACK); 		    	 
		      }		        
		      return mView;*/
		}
    }    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emp_work_time);
		dkGreen = getResources().getColor(R.color.dkgreen);
		caption = (TextView)findViewById(R.id.title_text);
	    caption.setText("");
		warrCaption = (TextView)findViewById(R.id.warr_title);
		mContext = this;		
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
		Bundle b = getIntent().getExtras();
		warrantId = b.getString("id");
		idWarr = Long.parseLong(warrantId);
		warrantNum = b.getString("num");
		mDBArrayAdapter = new CustomEmpWorkTimeAdapter(this, android.R.layout.simple_list_item_1);
        mDBListView = (ListView) findViewById(R.id.listViewLogs);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mWorkTimeMarkClickListener);
		warrChooser = (Spinner) findViewById(R.id.warrantChooser);
	    warrChooser.setOnItemSelectedListener(mChooserClickListener);
		getWarrantNumbers();

		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);

	    //warrChooser.setEnabled(false);
		//warrChooser.setVisibility(View.INVISIBLE);
		//warrCaption.setVisibility(View.INVISIBLE);
	    String sWhere = EmpWorkTimeTable.ID_WARRANT + "=" + warrantId;
    	showWorkTimeMarks(dbSch, sWhere);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emp_work_time, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_resend_marks);
		if (mi != null) { 
			mi.setEnabled(mDBArrayAdapter.getCount() > 0);
		}
		return true;
	}
	
	@Override	
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_refresh_list:
        	showCurrentWorkTimeMarks();
        	return true;       	
	    case R.id.action_resend_marks:
	    	prepare2ResendCurrentWorkTimeMarks();
	    	return true;       	
	    }
        return false;
	}	

	private void getWarrantNumbers() {
		int idx = 0;
		wNumbers = dbSch.getWarrantNums();
		if (wNumbers != null && wNumbers.length > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, wNumbers);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        warrChooser.setAdapter(adapter);
	        for (int i = 0; i < wNumbers.length; i++) {
	        	if (warrantNum.equals(wNumbers[i])) {
	        		idx = i;
	        		break;
	        	}
	        }	        
	        warrChooser.setSelection(idx);
		} else {
			warrChooser.setVisibility(View.INVISIBLE);
		}				
	}
	
    private OnItemSelectedListener mChooserClickListener = new OnItemSelectedListener() {
        @Override
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	showCurrentWorkTimeMarks();
        }

        @Override
    	public void onNothingSelected(AdapterView<?> parent) {}
    };
    
    // The on-click listener for ListViews
    private OnItemClickListener mWorkTimeMarkClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	EmpWorkTimeRecord workTimeMark;
        	workTimeMark = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "Log = " + workTimeMark);
            shopLocationOnMap(workTimeMark.lat, workTimeMark.lng);
        }
    };           

    private void shopLocationOnMap(double lat, double lng) {
		Intent runMapActivity = new Intent(mContext, WarrantsMapActivity.class);
		Bundle b = new Bundle();
		b.putString("map_mode", Integer.toString(WarrantsMapActivity.SINGLE_LOCATION_MAP_MODE));
		b.putString("lat", Double.toString(lat));
		b.putString("lng", Double.toString(lng));
		runMapActivity.putExtras(b);            
		startActivity(runMapActivity);                	
    }    
    
    private void showCurrentWorkTimeMarks() {
    	wNum = warrChooser.getSelectedItem().toString();
    	String sWhere = EmpWorkTimeTable.ID_WARRANT + "= (SELECT " + WarrantTable.ID_EXTERNAL +
    			" FROM " + WarrantTable.TABLE_NAME + " WHERE " + WarrantTable.NUM + "=" + wNum + ") OR "+
    			EmpWorkTimeTable.ID_WARRANT + " = " + wNum;
    	showWorkTimeMarks(dbSch, sWhere);
    }
    
    private void showWorkTimeMarks(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;
    	EmpWorkTimeRecord workTimeMark;
    	String query;    	
    	if (sWhere.length() == 0) {
    		query = "SELECT * from " + EmpWorkTimeTable.TABLE_NAME + " ORDER BY " + 
    				EmpWorkTimeTable.DTBE + " DESC";
    	} else {
    		query = "SELECT * from " + EmpWorkTimeTable.TABLE_NAME + " WHERE " + sWhere + 
    				" ORDER BY " + EmpWorkTimeTable.DTBE + " DESC";
    	}
    	int cntr = 0;  
		try {
    		c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		workTimeMark = new EmpWorkTimeRecord(c);
	    		idWarr = workTimeMark.id_warrant;
	    		mDBArrayAdapter.add(workTimeMark);
	    		cntr++;
	    	}    	    	
		} catch(Exception e) {
	        if (D) Log.e(TAG, "Exception: " + e.getMessage());
	    } finally {
	    	long interval = calcIntervals();
	    	caption.setText(String.format(getString(R.string.wt_cntr), cntr, getString(R.string.m_interval),
					CommonClass.printSecondsInterval(interval)));
	    	if (c != null) c.close();
	    	swipeLayout.setRefreshing(false);
		}
    }
    
    private long calcIntervals() {    	
    	long interval = 0;
    	EmpWorkTimeRecord workTimeMark;
		Calendar comeDate = Calendar.getInstance();
		Calendar outDate = Calendar.getInstance();
    	for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
    		workTimeMark = mDBArrayAdapter.getItem(i);
    		if (workTimeMark.be == EmpWorkTimeRecord.BEGIN_WORK) {
    			comeDate.setTime(workTimeMark.date);	    			
    		} else {
    			outDate.setTime(workTimeMark.date);
    			workTimeMark.setInterval((outDate.getTimeInMillis() - comeDate.getTimeInMillis())/ 1000);
    			interval += workTimeMark.getInterval();
    		}
    	}
    	return interval;
    }
    
    private void prepare2ResendCurrentWorkTimeMarks() {
    	boolean updated = dbSch.setEmpWarrantWorkMarksModified(idWarr, 1);
    	if (updated)
    		showCurrentWorkTimeMarks();
    	else
    		CommonClass.showMessage(mContext, mContext.getString(R.string.action_resend_marks), 
    				mContext.getString(R.string.m_no_marks2resend));
    }

	@Override
	public void onRefresh() {
		showCurrentWorkTimeMarks();
	}
	
}
