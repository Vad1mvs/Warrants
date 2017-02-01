package com.utis.warrants;

import java.util.HashMap;
import java.util.Set;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.utis.warrants.record.SealCertSealsRecord;
import com.utis.warrants.record.SealRecord;
import com.utis.warrants.tables.SealCertSealsTable;
import com.utis.warrants.tables.SealTable;

public class SealViewActivity extends FragmentActivity {
	private static final boolean D = true;
	private static final String TAG = "SealViewActivity";
	public static final int SEAL_VIEW_ONLY = 1;
	public static final int SEAL_VIEW_ADD_MODE = 2;
	public static final int SEAL_VIEW_DELETE_MODE = 3;
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private CustomEmpSealAdapter mDBArrayAdapter;
    private CustomSealCertSealsAdapter mSealDBArrayAdapter;
    public Context mContext;
    private TextView captionText;
    private SealRecord sealRec;
    private String sealCertId, warrContId;
    private int mSealsMode;
    private boolean onlyView;
    
    
    private static class CustomSealCertSealsAdapter extends ArrayAdapter<SealCertSealsRecord> {
    	private SealViewActivity ownerActivity;
    	static class SealViewHolder {
    	    public TextView textSeal;
    	    public TextView textSealInfo;
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
    	
    	public CustomSealCertSealsAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		ownerActivity = (SealViewActivity) context;
    	} 
    	
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) {
    		final SealViewHolder viewHolder;
	    	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) { 
	    		row = LayoutInflater.from(getContext()).inflate(R.layout.emp_seal_row, parent, false); 
	    		viewHolder = new SealViewHolder();
	    		viewHolder.textSeal = (TextView)row.findViewById(R.id.lineSeal);
	    		viewHolder.textSealInfo = (TextView)row.findViewById(R.id.lineSealInfo);
	    		row.setTag(viewHolder);
	    	} else {
	    		viewHolder = (SealViewHolder) row.getTag();	    		
	    	}
	    	final SealCertSealsRecord item = getItem(position); 
    		viewHolder.textSeal.setText(item.toString());
    		viewHolder.textSealInfo.setText("");
//    		viewHolder.textSealInfo.setVisibility(View.GONE);
    		row.setBackgroundResource(R.drawable.list_selector);
    		if (mSelection.get(position) != null) {
				row.setBackgroundResource(R.drawable.list_selector_selected);    			
    		}	
	    	return row; 
    	} 
    }    
    
    private static class CustomEmpSealAdapter extends ArrayAdapter<SealRecord> {
    	private SealViewActivity ownerActivity;
    	static class SealViewHolder {
    	    public TextView textSeal;
    	    public TextView textSealInfo;
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
    	
    	public CustomEmpSealAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		ownerActivity = (SealViewActivity) context;
    	} 
    	
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) {
    		final SealViewHolder viewHolder;
	    	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) { 
	    		row = LayoutInflater.from(getContext()).inflate(R.layout.emp_seal_row, parent, false); 
	    		viewHolder = new SealViewHolder();
	    		viewHolder.textSeal = (TextView)row.findViewById(R.id.lineSeal);
	    		viewHolder.textSealInfo = (TextView)row.findViewById(R.id.lineSealInfo);
	    		row.setTag(viewHolder);
	    	} else {
	    		viewHolder = (SealViewHolder) row.getTag();	    		
	    	}
	    	final SealRecord item = getItem(position); 
    		viewHolder.textSeal.setText(item.getSealInfo());
    		viewHolder.textSealInfo.setText(ownerActivity.getString(R.string.lbl_invoice) + item.getSealInvInfo());
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
		setContentView(R.layout.activity_seal_view);		
		mContext = this;
		captionText = (TextView)findViewById(R.id.title_text);
		Bundle b = getIntent().getExtras();
		sealCertId = b.getString("id");
		warrContId = b.getString("warrContId");
		mSealsMode = Integer.parseInt(b.getString("mode"));
		onlyView = mSealsMode == SEAL_VIEW_ONLY;// warrContId.length() == 0;
//		captionText.setVisibility(onlyView ? View.GONE : View.VISIBLE);
		mDBArrayAdapter = new CustomEmpSealAdapter(this, R.layout.emp_seal_row);
		mSealDBArrayAdapter = new CustomSealCertSealsAdapter(this, R.layout.emp_seal_row);
        mDBListView = (ListView) findViewById(R.id.listViewSeals);
        if (mSealsMode == SEAL_VIEW_DELETE_MODE) {
        	mDBListView.setAdapter(mSealDBArrayAdapter);
        } else {
        	mDBListView.setAdapter(mDBArrayAdapter);
        	mDBListView.setOnItemClickListener(mSealClickListener);
        }
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); 
		    mDBListView.setMultiChoiceModeListener(new ModeCallback()); 
	    }		        
	}

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        showEmpSeal(dbSch);
    } 
    
	@Override
	public void onBackPressed() {
		passResult();
	    super.onBackPressed();
	}
	
	private void passResult() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);		
	}
    	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.seal_view, menu);
		return true;
	}
	
    // The on-click listener for ListViews
    private OnItemClickListener mSealClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	sealRec = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "seal =" + sealRec.toString());
            if (onlyView) finish();
        }
    };    
	
    private void showEmpSeal(DBSchemaHelper sh) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null; 
    	String query;   	
        if (mSealsMode == SEAL_VIEW_DELETE_MODE) {
        	query = "SELECT * FROM " + SealCertSealsTable.TABLE_NAME +
        			" WHERE " + SealCertSealsTable.ID_SEAL_CERT + " = " + sealCertId;        	
    		try {
    			SealCertSealsRecord sealRec;
    			mSealDBArrayAdapter.clear();
    			c = sqdb.rawQuery(query, null);
    	    	while (c.moveToNext()) {    		
    	    		sealRec = new SealCertSealsRecord(c);    		
    	    		if (D) Log.d(TAG, "seal = " + sealRec.toString());
    	    		mSealDBArrayAdapter.add(sealRec);
    	    	}    
    		} catch (Exception e) {
    			if (D) Log.e(TAG, "Exception: " + e.getMessage());
    		} finally {
    	    	if (D) Log.d(TAG, "seals Count = " + mSealDBArrayAdapter.getCount());
		    	captionText.setText(getString(R.string.caption_get_emp_seal) +
			    		String.format(getString(R.string.caption_seal_cnt), mSealDBArrayAdapter.getCount()));
    	    	if (c != null) c.close();
    		}
        } else {
	    	query = "SELECT * FROM " + SealTable.TABLE_NAME + " WHERE " + SealTable.ID_EXTERNAL +
	    			" NOT IN (SELECT " + SealCertSealsTable.ID_SEAL_EXTERNAL + " FROM " + SealCertSealsTable.TABLE_NAME +
					") ORDER BY " + SealTable.ID_EXTERNAL;
			try {
				mDBArrayAdapter.clear();
				c = sqdb.rawQuery(query, null);
		    	while (c.moveToNext()) {    		
		    		sealRec = new SealRecord(c);    		
		    		if (D) Log.d(TAG, "seal = " + sealRec.toString());
		    		mDBArrayAdapter.add(sealRec);
		    	}    
			} catch (Exception e) {
				if (D) Log.e(TAG, "Exception: " + e.getMessage());
			} finally {
		    	if (D) Log.d(TAG, "seals Count = " + mDBArrayAdapter.getCount());
		    	if (mSealsMode == SEAL_VIEW_ADD_MODE)
		    		captionText.setText(getString(R.string.caption_get_emp_seal) +
		    				String.format(getString(R.string.caption_seal_cnt), mDBArrayAdapter.getCount()));
		    	else
		    		captionText.setText(String.format(getString(R.string.caption_seal_cnt), mDBArrayAdapter.getCount()));
		    	if (c != null) c.close();
			}
        }
    }

	private class ModeCallback implements ListView.MultiChoiceModeListener {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.seals_rowselection, menu);
            mode.setTitle(mContext.getString(R.string.selected));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    		MenuItem mi = menu.findItem(R.id.action_submit_seals);
    		if (mi != null) { 
    			mi.setEnabled(mSealsMode == SEAL_VIEW_ADD_MODE);
    			mi.setVisible(mSealsMode == SEAL_VIEW_ADD_MODE);
    		}
    		mi = menu.findItem(R.id.action_remove_seals);
    		if (mi != null) { 
    			mi.setEnabled(mSealsMode == SEAL_VIEW_DELETE_MODE);
    			mi.setVisible(mSealsMode == SEAL_VIEW_DELETE_MODE);
    		}
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_submit_seals:
            	setSelected2SealCert();
                mode.finish();
                passResult();
                finish();
                return true;
            case R.id.action_remove_seals:
            	deleteSelected();
                mode.finish();
//                finish();
                return true;
            default:
                return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
        	if (mSealsMode == SEAL_VIEW_DELETE_MODE)
        		mSealDBArrayAdapter.clearSelection();
        	else
        		mDBArrayAdapter.clearSelection();
        }

        public void onItemCheckedStateChanged(ActionMode mode,
                int position, long id, boolean checked) {
            final int checkedCount = mDBListView.getCheckedItemCount();
            if (mSealsMode == SEAL_VIEW_DELETE_MODE) {
	            if (checked) 
	            	mSealDBArrayAdapter.setNewSelection(position, checked);                    
	            else 
	            	mSealDBArrayAdapter.removeSelection(position);
            } else {
	            if (checked) 
	            	mDBArrayAdapter.setNewSelection(position, checked);                    
	            else 
	            	mDBArrayAdapter.removeSelection(position);            	
            }
            
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                default:
                    mode.setSubtitle("" + checkedCount);
                    break;
            }
        }
        
        private void setSelected2SealCert() {
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        	    	SealRecord item = mDBArrayAdapter.getItem(i);
        	    	dbSch.addSealInSealCert(Integer.parseInt(sealCertId), item.idExternal, item.getSealInfo());
        		}
        	}	        	
        	mDBArrayAdapter.clearSelection();
        	mDBArrayAdapter.notifyDataSetChanged();
        }
        
        private void deleteSelected() {
        	for (int i = mSealDBArrayAdapter.getCount() - 1; i >= 0; i--) {
        		if (mSealDBArrayAdapter.isPositionChecked(i)) {
        			SealCertSealsRecord item = mSealDBArrayAdapter.getItem(i);
        	    	dbSch.removeSealCertSeal(item.idSeals);
        	    	mSealDBArrayAdapter.remove(item);
        		}
        	}
        	showEmpSeal(dbSch);
//        	mDBArrayAdapter.clearSelection();
//        	mDBArrayAdapter.notifyDataSetChanged();
//        	invalidateOptionsMenu();
//        	showCounter(mDBArrayAdapter.getCount());
        }        
        
        
    }
	
	
}
