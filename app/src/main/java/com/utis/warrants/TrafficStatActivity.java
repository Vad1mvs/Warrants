package com.utis.warrants;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.utis.warrants.record.TrafficStatRecord;
import com.utis.warrants.tables.TrafficStatTable;

public class TrafficStatActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
	private static final boolean D = true;
	private static final String TAG = "TrafficStatActivity";
	private TextView caption;
    private DBSchemaHelper dbSch;
    private ListView mDBListView; 
    private String sPosition;
    public Context mContext;
    private CustomTrafficStatAdapter mDBArrayAdapter;
    private SwipeRefreshLayout swipeLayout;
    
    private static class CustomTrafficStatAdapter extends ArrayAdapter<TrafficStatRecord> {
    	private int id;
    	private Context context;
  	
    	static class TraffViewHolder {
    	    public TextView textDate;
    	    public TextView textRx;
    	    public TextView textRxData;
    	    public TextView textTx;
    	    public TextView textTxData;
    	}

    	public CustomTrafficStatAdapter(Context context, int textViewResourceId) { 
			//Call through to ArrayAdapter implementation 
			super(context, textViewResourceId);
			id = textViewResourceId;
			this.context = context;
		} 
  	
		  @Override
		public View getView(int position, View convertView, ViewGroup parent) {
    		TraffViewHolder viewHolder;
	    	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
	    	if (row == null) { 
				LayoutInflater inflater = (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.traffic_stat_row, null);
	    		// configure view viewHolder
	    	    viewHolder = new TraffViewHolder();
	    	    viewHolder.textDate = (TextView)row.findViewById(R.id.trafTime);
	    	    viewHolder.textRx = (TextView) row.findViewById(R.id.trafRx);
	    	    viewHolder.textRxData = (TextView) row.findViewById(R.id.trafRxData);
	    	    viewHolder.textTx = (TextView) row.findViewById(R.id.trafTx);
	    	    viewHolder.textTxData = (TextView) row.findViewById(R.id.trafTxData);
	    	    row.setTag(viewHolder);
	    	} else {
	    		viewHolder = (TraffViewHolder) row.getTag();
	    	}
	    	final TrafficStatRecord item = getItem(position);
	    	// fill data
	    	viewHolder.textDate.setText(item.statDateStr);
	    	viewHolder.textRx.setText(String.format("%3s=%8d", "Rx", item.rx_bytes));
	    	viewHolder.textRxData.setText(String.format("%3s=%8d", "RxD", item.rx_json_bytes));
	    	viewHolder.textTx.setText(String.format("%3s=%8d", "Tx", item.tx_bytes));
	    	viewHolder.textTxData.setText(String.format("%3s=%8d", "TxD", item.tx_json_bytes));
	    	return row;
			  
//			 View mView =  super.getView(position, convertView, parent);
//			 TextView tv = (TextView) mView.findViewById(android.R.id.text1);
//		     if (tv != null) {
//		    	 TrafficStatRecord item = getItem(position);
//	    		 tv.setTextColor(Color.BLACK);
//		      }		        
//		      return mView;
		}
    }
        

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic_stat);
		mContext = this;		
		caption = (TextView)findViewById(R.id.title_text);
	    caption.setText("");
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
		mDBArrayAdapter = new CustomTrafficStatAdapter(this, android.R.layout.simple_list_item_1);
        mDBListView = (ListView) findViewById(R.id.listViewTrafficStat);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mTrafficStatClickListener);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
        showTrafficStat(dbSch, "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.traffic_stat, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.action_refresh_traffic_stat:
        	if (D) Log.d(TAG, "onOptions action_refresh_traffic_stat");
        	showTrafficStat(dbSch, "");
        	return true;
        }
		return false;
	}	
	
    // The on-click listener for ListViews
    private OnItemClickListener mTrafficStatClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	TrafficStatRecord traffic;
        	traffic = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "Log = " + traffic);            
        }
    };    
	
    private void showTrafficStat(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;
    	TrafficStatRecord traffic;
    	String query;
    	if (sWhere.length() == 0) {
    		query = "SELECT * from " + TrafficStatTable.TABLE_NAME + " ORDER BY " +
    				TrafficStatTable.ID + " DESC";
    	} else {
    		query = "SELECT * from " + TrafficStatTable.TABLE_NAME + " WHERE " + sWhere + 
    				" ORDER BY " + TrafficStatTable.ID + " DESC";
    	}		
    	int cntr = 0; 
		try {
			c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		traffic = new TrafficStatRecord(c);
	    		mDBArrayAdapter.add(traffic);
	    		cntr++;
	    	}    	    	
		} catch(Exception e) {
	        //e.printStackTrace();
	        if (D) Log.e(TAG, "Exception: " + e.getMessage());
	    } finally {
	    	if (D) Log.d(TAG, "Rec Count = " + cntr);
			caption.setText(String.format(getString(R.string.m_cntr), cntr));
	    	if (c != null) c.close();
	    	swipeLayout.setRefreshing(false);
		}
    }

	@Override
	public void onRefresh() {
		showTrafficStat(dbSch, "");
	}
	

}
