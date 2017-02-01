package com.utis.warrants;

import java.util.ArrayList;
import java.util.List;

import com.utis.warrants.R.string;
import com.utis.warrants.record.GoodsRecord;
import com.utis.warrants.tables.GoodsTable;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class JobSelectActivity extends Activity {
	private static final boolean D = true;
	private static final String TAG = "JobSelectActivity";
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private ArrayAdapter<GoodsRecord> mDBArrayAdapter;
    public Context mContext;
    private TextView Caption;
    private EditText textFilter;
    private GoodsRecord gds; 
    private String jobId;
    private String gdsParentName;
    private int ttype;
    private String typeCaption;
    private List<String> gdsHistory;
    private int excludedJob;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_job_select);
		
		mContext = this;
		Caption = (TextView)findViewById(R.id.title_text);
		mDBArrayAdapter = new ArrayAdapter<GoodsRecord>(this, R.layout.db_data);
        mDBListView = (ListView) findViewById(R.id.listViewJob);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mJobClickListener);	
        textFilter = (EditText) findViewById(R.id.editTextFilter);
        textFilter.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
//            	JobGroupSelectActivity.this.mDBArrayAdapter.getFilter().filter(cs);
            	JobSelectActivity.this.mDBArrayAdapter.getFilter().filter(cs, new Filter.FilterListener() {
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
        
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
        gdsHistory = new ArrayList<String>();
        
		Bundle b = getIntent().getExtras();
		jobId = b.getString("id");
		ttype = Integer.parseInt(b.getString("ttype"));
		try {
			excludedJob = Integer.parseInt(b.getString("excluded_job"));
		} catch (Exception e) {
			excludedJob = -1;
		}		
		gdsParentName = b.getString("gds_name");		
		if (ttype == JobGroupSelectActivity.TYPE_JOB_SELECT) {
			typeCaption = "Работ: ";
			setTitle(mContext.getString(string.title_activity_job_select));
		} else {
			typeCaption = "Оборудования: ";
			setTitle(mContext.getString(string.title_activity_eq_select));
		}
        showJob(dbSch, "gd." + GoodsTable.ID_PARENT + "=" + jobId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.job_select, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		if (gdsHistory.size() > 0) {
			String historyGds = gdsHistory.get(gdsHistory.size()-1);
			gdsHistory.remove(gdsHistory.size()-1);
			showJob(dbSch, "gd." + GoodsTable.ID_PARENT + "=" + historyGds);
		} else {
		    super.onBackPressed();			
		}
	}
		
    // The on-click listener for ListViews
    private OnItemClickListener mJobClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	gds = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + gds.id_external);

            if (gds.child_cnt == 0) {
	            Intent returnIntent = new Intent();
				Bundle b = new Bundle();
				b.putString("id", Integer.toString(gds.id_external));//Your id
				returnIntent.putExtras(b); //Put your id to your Intent
	            setResult(RESULT_OK, returnIntent);       
	            finish();            
            } else {
            	gdsParentName = gds.fnm;
            	gdsHistory.add(Integer.toString(gds.id_parent));
            	showJob(dbSch, "gd." + GoodsTable.ID_PARENT + "=" + gds.id_external);
            }
        }
    };    
	
    private void showJob(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c;  
    	String q_eq = "(SELECT COUNT(g."+ GoodsTable.ID_EXTERNAL +") FROM "+ GoodsTable.TABLE_NAME +
    			" g WHERE g."+ GoodsTable.ID_PARENT +" = gd."+ GoodsTable.ID_EXTERNAL +")as "+ 
    			GoodsRecord.GDS_CHILD_CNT;
    	String query;
    	    	
    	if(sWhere.length() == 0) {
    		query = "SELECT gd.*, " + q_eq + " from " + GoodsTable.TABLE_NAME + " gd ORDER BY gd." + 
    				GoodsTable.ID_EXTERNAL + " DESC";
    	}
    	else {
    		query = "SELECT gd.*, " + q_eq + " from " + GoodsTable.TABLE_NAME + " gd WHERE " + sWhere + 
    				" ORDER BY gd." + GoodsTable.ID_EXTERNAL;
    	}
		c = sqdb.rawQuery(query, null);
    	int cntr = 0; 
		
		try {
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {	    		
	    		gds = new GoodsRecord(c);
	    		if (!((gds.id_parent == GoodsRecord.SEAL_JOB_PARENT) && (gds.gds_type == GoodsRecord.JOB_ENABLE_EMP_HW))
	    				&& (gds.id_external != excludedJob)) {
	    			mDBArrayAdapter.add(gds);
	    			cntr++;
	    		}
	    		
//	    		if (D) Log.d(TAG, "Job = " + job.id_external);	    		
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {    	
	    	if (D) Log.d(TAG, "Count = " + cntr);
	    	showCounter(cntr);
	    	if (c != null) c.close();
		}
    }

    private void showCounter(int cntr) {
    	Caption.setText(gdsParentName + "; " + typeCaption + cntr);
    }
    
}
