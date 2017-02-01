package com.utis.warrants;

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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class JobGroupSelectActivity extends Activity implements OnClickListener{
	private static final boolean D = true;
	private static final String TAG = "JobGroupSelectActivity";
	private static final int JOB_SELECT = 1;
	public static final int TYPE_JOB_SELECT = 5;
	public static final int TYPE_EQ_GROUP_SELECT = 1;
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private ArrayAdapter<GoodsRecord> mDBArrayAdapter;
    public Context mContext;
    private TextView caption;
    private EditText textFilter;
    private int ttype;
    private GoodsRecord jobGroup; 
    private String typeCaption, excludedJob;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_job_group_select);

		mContext = this;
		caption = (TextView)findViewById(R.id.title_text);
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
            	JobGroupSelectActivity.this.mDBArrayAdapter.getFilter().filter(cs, new Filter.FilterListener() {
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
		Bundle b = getIntent().getExtras();
		ttype = Integer.parseInt(b.getString("ttype"));
		if (b.containsKey("excluded_job"))
			excludedJob = b.getString("excluded_job");
		else
			excludedJob = "";
		if (ttype == TYPE_JOB_SELECT) {
			typeCaption = mContext.getString(string.m_job_group);
			setTitle(mContext.getString(string.title_activity_job_select));
		} else {
			typeCaption = mContext.getString(string.m_eq_group);
			setTitle(mContext.getString(string.title_activity_eq_select));
		}
        showJobGroups(dbSch, "gd." + GoodsTable.TTYPE + "="+ ttype);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.job_group_select, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
    // The on-click listener for ListViews
    private OnItemClickListener mJobClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	jobGroup = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + jobGroup.id_external);

			Intent i = new Intent(mContext, JobSelectActivity.class);
			Bundle b = new Bundle();
			b.putString("id", Integer.toString(jobGroup.id_external));//Your id
			b.putString("ttype", Integer.toString(ttype));
			b.putString("gds_name", jobGroup.fnm);
			b.putString("excluded_job", excludedJob);
			i.putExtras(b); //Put your id to your next Intent
//			startActivity(i);
			startActivityForResult(i, JOB_SELECT);
        }
    };    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK && requestCode == JOB_SELECT) {
            if (data.hasExtra("id")) {
//                Toast.makeText(this, data.getExtras().getString("id"),
//                    Toast.LENGTH_SHORT).show();
                
            	Intent returnIntent = new Intent();
    			Bundle b = new Bundle();
    			b.putString("id", data.getExtras().getString("id"));//Your id
    			returnIntent.putExtras(b); //Put your id to your Intent
                setResult(RESULT_OK, returnIntent);
                Toast.makeText(this,data + " data " + excludedJob , Toast.LENGTH_SHORT).show();

                finish();                           
            }
        }
    }   
    
    private void showJobGroups(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c;  
    	
    	String q_eq = "(SELECT COUNT(g."+ GoodsTable.ID_EXTERNAL +") FROM "+ GoodsTable.TABLE_NAME +
    			" g WHERE g."+ GoodsTable.ID_PARENT +" = gd."+ GoodsTable.ID_EXTERNAL +")as "+ 
    			GoodsRecord.GDS_CHILD_CNT;
    	String query;
    	    	
    	if (sWhere.length() == 0) {
    		query = "SELECT gd.*, " + q_eq + " from " + GoodsTable.TABLE_NAME + " gd ORDER BY gd." + 
    				GoodsTable.NM + " ASC";
    	} else {
    		query = "SELECT gd.*, " + q_eq + " from " + GoodsTable.TABLE_NAME + " gd WHERE " + sWhere + 
    				" ORDER BY gd." + GoodsTable.NM;
    	}
		c = sqdb.rawQuery(query, null);
		
    	int cntr = 0; 		
		try {
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		cntr++;
	    		jobGroup = new GoodsRecord(c);    		
//	    		if (D) Log.d(TAG, "Job = " + jobGroup.id_external);
	    		mDBArrayAdapter.add(jobGroup);
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
    	caption.setText(typeCaption + cntr);
    }
	
}
