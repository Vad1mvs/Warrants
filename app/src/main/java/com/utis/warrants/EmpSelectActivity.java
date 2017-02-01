package com.utis.warrants;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.utis.warrants.record.EmpRecord;
import com.utis.warrants.tables.EmpTable;

public class EmpSelectActivity extends Activity {
	private static final boolean D = true;
	private static final String TAG = "EmpSelectActivity";
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private ArrayAdapter<EmpRecord> mDBArrayAdapter;
    public Context mContext;
    private TextView captionView;
    private EmpRecord emp; 
    private String selfEmpId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emp_select);
		
		mContext = this;
		captionView = (TextView)findViewById(R.id.title_text);
		mDBArrayAdapter = new ArrayAdapter<EmpRecord>(this, R.layout.db_data);
        mDBListView = (ListView) findViewById(R.id.listViewEmp);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mEmpClickListener);	
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
        
		Bundle b = getIntent().getExtras();
		selfEmpId = b.getString("selfId");
        showEmp(dbSch, EmpTable.ID_EXTERNAL + " <> " + selfEmpId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emp_select, menu);
		return true;
	}
	
    // The on-click listener for ListViews
    private OnItemClickListener mEmpClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	emp = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + emp.id_external);

            Intent returnIntent = new Intent();
			Bundle b = new Bundle();
			b.putString("id", Integer.toString(emp.id_external));
			b.putString("nm", emp.toString());
			returnIntent.putExtras(b); 
            setResult(RESULT_OK, returnIntent);       
            finish();            
        }
    };    
	
    private void showEmp(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;  
    	String query;
    	if (sWhere.length() == 0) {
    		query = "SELECT * from " + EmpTable.TABLE_NAME + " ORDER BY " + EmpTable.SURNAME + " ASC";
    	} else {
    		query = "SELECT * from " + EmpTable.TABLE_NAME + " WHERE " + sWhere + 
    				" ORDER BY " + EmpTable.SURNAME + " ASC";
    	}
    	int cntr = 0; 		
		try {
    		c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		cntr++;
	    		emp = new EmpRecord(c);	    		
	    		if (D) Log.d(TAG, "Emp = " + emp.id_external);
	    		mDBArrayAdapter.add(emp);
	    	}    	   	
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
			if (D) Log.d(TAG, "emp Count = " + cntr);
	    	captionView.setText(getString(R.string.lbl_emps_count) + cntr);
	    	if (c != null) c.close();
		}
    }

}
