package com.utis.warrants;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.utis.warrants.record.EmpRecord;
import com.utis.warrants.tables.EmpTable;

public class EmpPhotoSelectActivity extends Activity {
	private static final boolean D = true;
	private static final String TAG = "EmpPhotoSelectActivity";
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private CustomEmpPhotoAdapter mDBArrayAdapter;
    public Context mContext;
    private TextView captionView;
    private EmpRecord emp;
    private long selfEmpId;

    
    private static class CustomEmpPhotoAdapter extends ArrayAdapter<EmpRecord> {
    	private long selfUserId = 0;
    	
    	public CustomEmpPhotoAdapter(Context context, int layout, int resId, EmpRecord[] items) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, layout, resId, items); 
    	} 

    	public CustomEmpPhotoAdapter(Context context, int textViewResourceId, long userId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId); 
    		selfUserId = userId;
    	} 
    	
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) { 
	    	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) { 
	    		row = LayoutInflater.from(getContext()).inflate(R.layout.emp_row, parent, false); 
	    	} 
	    	final EmpRecord item = getItem(position); 
	    	TextView textEmp = (TextView)row.findViewById(R.id.textViewEmp);
	    	CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBox);
	    	ImageView photoEmp = (ImageView)row.findViewById(R.id.imageViewEmp);
	    	textEmp.setText(item.toString());	    	
	    	if (item.photo != null && item.photo.length > 0) {
		    	Bitmap pic;
		    	pic = BitmapFactory.decodeByteArray(item.photo, 0, item.photo.length);
		    	photoEmp.setImageBitmap(pic);
	    	} else {
		    	photoEmp.setImageResource(R.drawable.user);	    		
	    	}	    	
//	    	if (selfUserId == item.id_external) 
//	    		row.setBackgroundResource(R.drawable.list_selector_done_lt);
//	    	else 
//	    		row.setBackgroundResource(R.drawable.list_selector);
	    	checkBox.setVisibility(View.GONE);
	    	checkBox.setChecked(item.selected);
	    	checkBox.setOnClickListener(new View.OnClickListener() {	    	     
	    		public void onClick(View v) {
	    		    if (((CheckBox)v).isChecked()) { 
	    		    	item.selected = true;
	    		    } else { 
	    		    	item.selected = false;
	    		    }
//	    		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//		    		    invalidateOptionsMenu();
//    		    	}
    		    }
	    	});	    	
	    	return row; 
    	} 
    }	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emp_photo_select);		
		mContext = this;
		captionView = (TextView)findViewById(R.id.title_text);
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
        selfEmpId = dbSch.getUserId();		
		mDBArrayAdapter = new CustomEmpPhotoAdapter(this, R.layout.emp_row, selfEmpId);		
//		mDBArrayAdapter = new ArrayAdapter<EmpRecord>(this, R.layout.db_data);
        mDBListView = (ListView) findViewById(R.id.listViewEmp);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mEmpClickListener);	
		//Bundle b = getIntent().getExtras();
		//selfEmpId = b.getString("selfId");
        showEmp(dbSch, EmpTable.ID_EXTERNAL + " <> " + selfEmpId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emp_photo_select, menu);
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
    	if(sWhere == "") {
    		query = "SELECT * FROM " + EmpTable.TABLE_NAME + " ORDER BY " + EmpTable.SURNAME + " ASC";
    	}
    	else {
    		query = "SELECT * FROM " + EmpTable.TABLE_NAME + " WHERE " + sWhere + " ORDER BY " + EmpTable.SURNAME + " ASC";
    	}
		try {
    		c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		emp = new EmpRecord(c);	    		
	    		if (D) Log.d(TAG, "Emp = " + emp.id_external);
	    		mDBArrayAdapter.add(emp);
	    	}    	   	
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
			captionView.setText(getString(R.string.lbl_emps_count) + mDBArrayAdapter.getCount());
	    	if (c != null) c.close();
		}
    }

}
