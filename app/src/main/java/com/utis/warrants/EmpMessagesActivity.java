package com.utis.warrants;


import java.util.HashMap;
import java.util.Set;

import com.utis.warrants.ConnectionManagerService.ServiceResponseCallback;
import com.utis.warrants.record.EmpMessagesRecord;
import com.utis.warrants.tables.EmpTable;
import com.utis.warrants.tables.MessagesTable;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class EmpMessagesActivity extends Activity implements ServiceResponseCallback, SwipeRefreshLayout.OnRefreshListener {
	private static final boolean D = true;
	private static final String TAG = "EmpMessagesActivity";
	private static final int COMPOSE_MSG = 2;
    private DBSchemaHelper dbSch;
    private static ListView mDBListView;    
    private CustomUserMsgAdapter mDBArrayAdapter;
    public static Context mContext;
    private TextView captionView;
    private long selfEmpId = 0; 
    protected Object mActionMode;
    private Object mActionModeCallback;
    public int selectedItem = -1;
    private SwipeRefreshLayout swipeLayout;
    
    ConnectionManagerService connectionService; 
    Intent serviceIntent; 
    
    private static class CustomUserMsgAdapter extends ArrayAdapter<EmpMessagesRecord> {
    	private long selfUserId = 0;
    	private Context context;
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
        
    	static class EmpMsgViewHolder {
    	    public TextView textEmpName;
    	    public TextView textSentMsgCntr;
    	    public TextView textRecMsgCntr;
    	    public ImageView photoEmp;
    	}
    	
    	public CustomUserMsgAdapter(Context context, int layout, int resId, 
    			EmpMessagesRecord[] items) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, layout, resId, items); 
    		this.context = context;
    	} 

    	public CustomUserMsgAdapter(Context context, int textViewResourceId, long userId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId); 
    		selfUserId = userId;
    		this.context = context;
    	} 
    	
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) { 
	    	View row = convertView; 
	    	EmpMsgViewHolder viewHolder;
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) { 
				LayoutInflater inflater = (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.emp_msg_row, null);
//	    		row = LayoutInflater.from(getContext()).inflate(R.layout.emp_msg_row, parent, false);
	    		viewHolder = new EmpMsgViewHolder();
	    		viewHolder.textEmpName = (TextView)row.findViewById(R.id.lineEmpName);
	    		viewHolder.textSentMsgCntr = (TextView)row.findViewById(R.id.lineSentMsgCntr);
	    		viewHolder.textRecMsgCntr = (TextView)row.findViewById(R.id.lineRecMsgCntr);
	    		viewHolder.photoEmp = (ImageView)row.findViewById(R.id.imageViewEmp);
	    		row.setTag(viewHolder);
	    	} else {
	    		 viewHolder = (EmpMsgViewHolder) row.getTag();
	    	}
	    	EmpMessagesRecord item = getItem(position);	    	
	    	viewHolder.textEmpName.setText(item.combinedName);
	    	if (item.photo != null && item.photo.length > 0) {
		    	Bitmap pic;
		    	pic = BitmapFactory.decodeByteArray(item.photo, 0, item.photo.length);
		    	viewHolder.photoEmp.setImageBitmap(pic);
	    	} else {
	    		viewHolder.photoEmp.setImageResource(R.drawable.user);	    		
	    	}
	    	viewHolder.textSentMsgCntr.setText(mContext.getString(R.string.lbl_short_sent_msg_cntr) + 
	    			item.recievedMsgCnt);
	    	viewHolder.textRecMsgCntr.setText(mContext.getString(R.string.lbl_short_rec_msg_cntr) + 
	    			item.sentMsgCnt);
	    	
    		if (item.hasNewImpMsgs)
    			row.setBackgroundResource(R.drawable.list_selector_new_imp_msg);
    		else if (item.hasNewMsgs)
    			row.setBackgroundResource(R.drawable.list_selector_new_msg);
    		else if (item.hasModifiedMsgs)
    			row.setBackgroundResource(R.drawable.list_selector_modified);
    		else
    			row.setBackgroundResource(R.drawable.list_selector);
    		
    		if (mSelection.get(position) != null) {
				row.setBackgroundResource(R.drawable.list_selector_selected);    			
    		}
	    	
	    	return row; 
    	} 
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_messages);
		
		mContext = this;
		captionView = (TextView)findViewById(R.id.title_text);
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
		selfEmpId = dbSch.getUserId();
		mDBArrayAdapter = new CustomUserMsgAdapter(this, R.layout.msg_row, selfEmpId);
        mDBListView = (ListView) findViewById(R.id.listViewMessages);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mMessagesClickListener);
        setActionMode();
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//	    	mDBListView.setOnItemLongClickListener(mMessagesLongClickListener);
		    mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); 
		    mDBListView.setMultiChoiceModeListener(new ModeCallback()); 
	    }
	    //Set up this list with a contextual ActionMode 
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
        
        serviceIntent = new Intent(this, ConnectionManagerService.class); 
//		ShowUserMessages(dbSch, "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_messages, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
        	if (D) Log.d(TAG, "onOptions action_settings");
            // Launch the DeviceListActivity to see devices and do scan
//            serverIntent = new Intent(this, DeviceListActivity.class);
//            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.action_refresh_msg:
        	if (D) Log.d(TAG, "onOptions action_refresh_msg");
        	connectionService.postMessageTable();
            return true;
        case R.id.action_new_msg:
        	if (D) Log.d(TAG, "onOptions action_new_msg");
            Intent msgIntent = new Intent(this, ComposeNewMsgActivity.class);
            Bundle b = new Bundle();
            b.putString("selfEmpId", Long.toString(selfEmpId));
            b.putString("id_recipient", "");
            b.putString("recipient", "");
            b.putString("subj", "");
            b.putString("msg", "");
            msgIntent.putExtras(b);            
            startActivityForResult(msgIntent, COMPOSE_MSG);
            return true;
        case R.id.action_clear_msg:
        	if (D) Log.d(TAG, "onOptions action_clear_msg");
        	eraseAllMessages();
            return true;
        case R.id.action_warr_online:
        	if (D) Log.d(TAG, "onOptions action_warr_online");
        	CommonClass.openOnlineWarrants(mContext);
        	return true;
        }
        return false;
	}	
	
    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        if (!(connectionService != null && !connectionService.isWorking())) {
	        //Starting the service makes it stick, regardless of bindings 
	        startService(serviceIntent);
        }
        //Bind to the service 
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//        if (connectionService != null) connectionService.setServiceResponseCallback((ServiceResponseCallback) mContext);
        showUserMessages(dbSch, "");
    } 
    
    @Override 
    public void onPause() { 
    	super.onPause(); 
		connectionService.sendToasts = false;
    	//Unbind from the service 
    	unbindService(serviceConnection); 
    }
    
    private ServiceConnection serviceConnection = new ServiceConnection() { 
    	public void onServiceConnected(ComponentName className, IBinder service) { 
    		connectionService = ((ConnectionManagerService.ConnectionBinder)service).getService();
    		connectionService.setServiceResponseCallback((ServiceResponseCallback) mContext);
    		connectionService.sendToasts = true;
    		//updateStatus(); 
    	} 
    	
    	public void onServiceDisconnected(ComponentName className) { 
    		connectionService = null; 
    	} 
    };
        

    // The on-click listener for ListViews
    private OnItemClickListener mMessagesClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	EmpMessagesRecord empMsg;
        	empMsg = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "nm=" + empMsg.combinedName);
            
            Intent intent = new Intent(mContext, MessagesActivity.class);
            Bundle b = new Bundle();
            b.putString("id_emp", Long.toString(empMsg.id_external));
            b.putString("empNm", empMsg.combinedName);
            intent.putExtras(b);            
            startActivity(intent);            
        }
    };  
    
    private OnItemLongClickListener mMessagesLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long id) {
			if (mActionMode != null) {
				return false;
		    }
	        selectedItem = position;

		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		        // start the CAB using the ActionMode.Callback defined above
		        mActionMode = EmpMessagesActivity.this.startActionMode((ActionMode.Callback)mActionModeCallback);
		        mDBListView.setSelected(true);
		    }
	        return true;		
	    }
    	
    };
    
    private void setActionMode() {
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	mActionModeCallback = new ActionMode.Callback() {
	
	            // called when the action mode is created; startActionMode() was called
	            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	    			// Inflate a menu resource providing context menu items
	    			MenuInflater inflater = mode.getMenuInflater();
	    			// assumes that you have "contexual.xml" menu resources
	    			inflater.inflate(R.menu.emp_msg_rowselection, menu);
	    			return true;
	            }
	
	            // the following method is called each time 
	            // the action mode is shown. Always called after
	            // onCreateActionMode, but
	            // may be called multiple times if the mode is invalidated.
	            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	                return false; // Return false if nothing is done
	            }
	
	            // called when the user selects a contextual menu item
	            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	            	//Obtain a list of checked item locations to do the operation 
//	            	SparseBooleanArray items = mDBListView.getCheckedItemPositions();
	                switch (item.getItemId()) {
	                case R.id.action_mark_msg_as_read:            	
	                	markEmpAllRead();
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

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.emp_msg_rowselection, menu);
            mode.setTitle(mContext.getString(R.string.selected));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//        	SparseBooleanArray selectedItems = mDBListView.getCheckedItemPositions();
            switch (item.getItemId()) {
            case R.id.action_mark_msg_as_read:            	
            	markSelectedEmpAllRead();
                // the Action was executed, close the CAB
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
        }
        
    }    
    
    private void markSelectedEmpAllRead() {
    	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
    		if (mDBArrayAdapter.isPositionChecked(i)) {
    	    	EmpMessagesRecord item = mDBArrayAdapter.getItem(i);
    	    	dbSch.setEmpAllMsgRead(item.id_external);
    	    	item.hasNewMsgs = dbSch.isEmpHasNewMessages(item.id_external);
    	    	item.hasModifiedMsgs = true;				    			
    		}    		
    	}
    	mDBArrayAdapter.clearSelection();
    	mDBArrayAdapter.notifyDataSetChanged();
    }
        
    private void markEmpAllRead() {
    	EmpMessagesRecord item = mDBArrayAdapter.getItem(selectedItem);
    	dbSch.setEmpAllMsgRead(item.id_external);
    	item.hasNewMsgs = dbSch.isEmpHasNewMessages(item.id_external);
    	item.hasModifiedMsgs = true;
    	mDBArrayAdapter.notifyDataSetChanged();
    }
        
    private void eraseAllMessages() {
    	dbSch.emptyTable(MessagesTable.TABLE_NAME);
    	showUserMessages(dbSch, "");
    }
	
    private void showUserMessages(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;
    	EmpMessagesRecord userMsg; 
    	String sendCntrColName = "AS "+ EmpMessagesRecord.SEND_MSG_CNTR_COL_NAME;
    	String recCntrColName = "AS "+ EmpMessagesRecord.REC_MSG_CNTR_COL_NAME;

    	String msgOwnQuery = "(SELECT COUNT(" + MessagesTable.ID + ") FROM " + MessagesTable.TABLE_NAME + 
    			" WHERE " + MessagesTable.ID_SENDER + "=" + MessagesTable.ID_RECIPIENT +
    			" AND "+ MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION +")AS OWN_CNT";
    	
    	String msgSendQuery = "(SELECT COUNT(" + MessagesTable.ID + ") FROM " + MessagesTable.TABLE_NAME + 
    			" WHERE " + MessagesTable.ID_SENDER + "=" + "u."+ EmpTable.ID_EXTERNAL +
    			" AND "+ MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION +")";
    	String msgRecQuery = "(SELECT COUNT(" + MessagesTable.ID + ") FROM " + MessagesTable.TABLE_NAME + 
    			" WHERE " + MessagesTable.ID_RECIPIENT + "=" + "u."+ EmpTable.ID_EXTERNAL +
    			" AND "+ MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION +")";
    	String query;
    	    	
    	if (sWhere == "") {
    		query = "SELECT u.*," + msgSendQuery + sendCntrColName + ", " + msgRecQuery + recCntrColName +
    				", "+ msgOwnQuery +
    				" FROM " + EmpTable.TABLE_NAME + " u WHERE " + msgSendQuery + "> 0 OR "+ msgRecQuery + "> 0"+ 
    				" ORDER BY u." + EmpTable.SURNAME + " ASC";
    	} else {
    		query = "SELECT * FROM " + MessagesTable.TABLE_NAME + " WHERE " + sWhere + 
    				" ORDER BY " + MessagesTable.ID_EXTERNAL;
    	}
    	int cntr = 0; 		
		try {
			c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {	
	    		int colid = c.getColumnIndex("OWN_CNT");
	        	int cnt = c.getInt(colid);
	    		userMsg = new EmpMessagesRecord(c);
	    		userMsg.hasNewMsgs = dbSch.isEmpHasNewMessages(userMsg.id_external);
	    		userMsg.hasNewImpMsgs = dbSch.isEmpHasNewImportantMessages(userMsg.id_external);
	    		userMsg.hasModifiedMsgs = dbSch.isEmpHasModifiedMessages(userMsg.id_external);	
	    		if (D) Log.d(TAG, "Msg = " + userMsg.combinedName);
	    		if (userMsg.id_external != selfEmpId) {
		    		if ((userMsg.sentMsgCnt > 0 || userMsg.recievedMsgCnt > 0)) {
		    			mDBArrayAdapter.add(userMsg);
		    			cntr++;
		    		}	    			
	    		} else {
	    			if (cnt > 0) {
	    				userMsg.sentMsgCnt = cnt;
	    				userMsg.recievedMsgCnt = cnt;
	    				userMsg.hasNewMsgs = dbSch.isSelfEmpHasNewMessages(userMsg.id_external);
		    			mDBArrayAdapter.add(userMsg);
		    			cntr++;
	    			}
	    		}
	    	}
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
	    	if (D) Log.d(TAG, "userMsgs Count = " + cntr);
	    	captionView.setText(getString(R.string.action_msg_senders) + cntr);
	    	if (c != null) c.close();
		}
    }

	@Override
	public void onSrvRequestSuccess(String response) {
		showUserMessages(dbSch, "");		
	}

	@Override
	public void onSrvRequestError(Exception error) {
		Toast.makeText(this, "ConnService Exception: "+ error.getMessage(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSrvTaskChanged(int Task) {
		if ((connectionService.getState() == ConnectionManagerService.STATE_FINISH))
			swipeLayout.setRefreshing(false);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    invalidateOptionsMenu();		    
    	}
	}   
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case COMPOSE_MSG:
                if (data.hasExtra("id")) {
                	long id = Long.parseLong(data.getExtras().getString("id"));
                	if (id > 0)
                		showUserMessages(dbSch, "");					
                }
            	break;
    		}
        }
    }

	@Override
	public void onRefresh() {
		connectionService.postMessageTable();
	}


}
