package com.utis.warrants;

import java.util.HashMap;
import java.util.Set;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.utis.warrants.record.WarrRepairRecord;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.WarrRepairTable;

public class WarrRepairActivity extends Activity {
	private static final boolean D = true;
	private static final String TAG = "WarrRepairActivity";
	private static final int EMP_HW_SELECT = 1;
    private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private CustomWarrRepairAdapter mDBArrayAdapter;
    public Context mContext;
    private TextView captionView, info;
    private String warrUnitId, warrantId;
    private boolean updateEnabled, updated;
    private String warrNumDate, warrJob, warrEq, jobRemark;
    private WarrRepairRecord warrRepair;
	private int checkedCntr;

    private class CustomWarrRepairAdapter extends ArrayAdapter<WarrRepairRecord> {
    	private WarrRepairActivity ownerActivity;
    	private Context context;
    	
    	public CustomWarrRepairAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		this.context = context;    		
    		ownerActivity = (WarrRepairActivity) context;
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
    	
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) { 
	    	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
	    	if (row == null) { 
				LayoutInflater inflater = (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.warr_repair_row, null);
	    	} 
	    	final WarrRepairRecord item = getItem(position);   
	    	TextView textRepair = (TextView)row.findViewById(R.id.lineRepair);
	    	TextView textRepairCntr = (TextView)row.findViewById(R.id.lineRepairCntr);
	    	CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBox);
	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    		checkBox.setEnabled(false);
	    		checkBox.setVisibility(View.GONE);
	    	}
	    	textRepair.setText(item.repairName);
	    	textRepairCntr.setText(""+ item.quant);
    		if (mSelection.get(position) != null) {
				row.setBackgroundResource(R.drawable.list_selector_selected);    			
    		} else if (item.modified == 1 || item.id_external == 0)
	    		row.setBackgroundResource(R.drawable.list_selector_modified);
	    	else
	    		row.setBackgroundResource(R.drawable.list_selector);
	    	
	    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
		    	checkBox.setChecked(item.selected);       	    	
		    	checkBox.setOnClickListener(new View.OnClickListener() {	    	     
		    		public void onClick(View v) {
		    		    if (((CheckBox)v).isChecked()) {
		    		    	item.selected = true;
		    		    	ownerActivity.checkedCntr++;
		    		    } else { 
		    		    	item.selected = false;
		    		    	ownerActivity.checkedCntr--;
		    		    }
		    		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			    		    invalidateOptionsMenu();
	    		    	}
	    		    }
		    	});
	    	}
	    	return row; 
    	} 
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warr_repair);		
		mContext = this; 
		captionView = (TextView)findViewById(R.id.title_text);
		info = (TextView)findViewById(R.id.info_text);
		mDBArrayAdapter = new CustomWarrRepairAdapter(this, R.layout.warr_repair_row);
        mDBListView = (ListView) findViewById(R.id.listViewWarr);
        mDBListView.setAdapter(mDBArrayAdapter);
//        mDBListView.setOnItemClickListener(mGdsClickListener);
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);		
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); 
		    mDBListView.setMultiChoiceModeListener(new ModeCallback()); 
	    }		
		getIntent();
		Bundle b = getIntent().getExtras();
		warrUnitId = b.getString("id");		
		warrantId = b.getString("id_w");
		updateEnabled = Boolean.parseBoolean(b.getString("update_enabled"));
		warrNumDate = b.getString("num");
		warrJob = b.getString("job");
		warrEq = b.getString("eq");
		jobRemark = b.getString("rem");
		if ((warrUnitId != null)&&(warrUnitId.length() > 0)) {
//			Toast.makeText(this, warrUnitId, Toast.LENGTH_SHORT).show();
			refreshWarrRepairList(false);
		}
		info.setText(String.format("Наряд %s; \n%s %s %s", warrNumDate, warrEq, warrJob, jobRemark));
	}

	@Override
	protected void onResume() {
		super.onResume();
//		fStart = true;
	}
		
	@Override
	protected void onPause() {
	    super.onPause();  // Always call the superclass method first
	}
	    
	@Override
	public void onBackPressed() {
        Intent returnIntent = new Intent();
		Bundle b = new Bundle();
		b.putString("updated", Integer.toString((updated) ? 1 : 0));
		returnIntent.putExtras(b); 
        setResult(RESULT_OK, returnIntent);
	    super.onBackPressed();
	}
	
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.warr_repair, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_delete__repair);
		if (mi != null) {
			mi.setEnabled(updateEnabled && checkedCntr > 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
			mi.setVisible(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
		}
	    mi = menu.findItem(R.id.action_add__repair);
	    if (mi != null)
	    	mi.setEnabled(updateEnabled);
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
        	if (D) Log.d(TAG, "onOptions action_settings");
            return true;
        case R.id.action_add__repair:
        	if (D) Log.d(TAG, "onOptions action_add_repair");
        	addRepair(); 
        	return true;
        case R.id.action_delete__repair:
        	if (D) Log.d(TAG, "onOptions action_delete_repair");
        	deleteChecked(); 
        	return true;
        }
        return false;
    }		
	
	private void refreshWarrRepairList(boolean setModified) {
		if (setModified) {
        	dbSch.setWarrUnitModifiedState(Integer.parseInt(warrUnitId));
        	dbSch.setWarrantModifiedState(Integer.parseInt(warrantId));			
		}
        showWarrRepair(dbSch, WarrRepairTable.ID_WARR_UNIT + "=" + warrUnitId + " AND " +
				WarrRepairTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION);
	}	
	
    private void showWarrRepair(DBSchemaHelper sh, String sWhere) {
    	String q_nm = "(SELECT "+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
    			" WHERE "+ GoodsTable.ID_EXTERNAL +" = "+ WarrRepairTable.ID_REPAIR +")as "+ WarrRepairRecord.NM;
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;
    	String query;
    	if (sWhere.length() == 0) {
    		query = "SELECT *,"+ q_nm +" from " + WarrRepairTable.TABLE_NAME + " ORDER BY " + 
    				WarrRepairRecord.NM + " ASC";
    	} else {
    		query = "SELECT *,"+ q_nm +" from " + WarrRepairTable.TABLE_NAME + " WHERE " + sWhere + 
    				" ORDER BY " + WarrRepairRecord.NM;
    	}
		try {
			c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		warrRepair = new WarrRepairRecord(c);	    		
//	    		if (D) Log.d(TAG, "WarrRepair = " + warrRepair.id_external);
	    		mDBArrayAdapter.add(warrRepair);
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {    	
	    	updateCaption();
	    	if (c != null) c.close();
		}
    }
    
    private void updateCaption() {
    	captionView.setText(getString(R.string.caption_warr_repair_info) + mDBArrayAdapter.getCount());
    }

	private void addRepair() {
		Intent intent = new Intent(mContext, EmpHWSelectActivity.class);
		Bundle b = new Bundle();
		b.putString("id", warrUnitId);
		b.putString("warrId", warrantId);
		intent.putExtras(b); 
		startActivityForResult(intent, EMP_HW_SELECT);    			
	}
	
	private void deleteChecked() {
		boolean needUpdate = false;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			WarrRepairRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) { 
//				mDBArrayAdapter.remove(item);
				if (dbSch.removeWarrRepairRepairId(item.id, item.id_external, item.id_repair))
					needUpdate = true; 
			}
		}
		if (needUpdate) {
			refreshWarrRepairList(true);
			updated = true;
		}		
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK && requestCode == EMP_HW_SELECT) {
            if (data.hasExtra("id")) {
            	int repairId = Integer.parseInt(data.getExtras().getString("id"));
            	double cntr = Double.parseDouble(data.getExtras().getString("cntr"));
//                Toast.makeText(this, Integer.toString(repairId), Toast.LENGTH_SHORT).show();
                if (dbSch.addWarrRepairItem(0, Integer.parseInt(warrantId), 
                		Integer.parseInt(warrUnitId), repairId, 2, 0, cntr) > 0) {
                	refreshWarrRepairList(true);
                    updated = true;
                }
            } else {
            	refreshWarrRepairList(true);
                updated = true;            	
            }
        }
    }   
    
	private class ModeCallback implements ListView.MultiChoiceModeListener {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.warr_repair_rowselection, menu);
            mode.setTitle(mContext.getString(R.string.selected));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_remove_repair:            	
            	deleteSelected();
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
        
        private void deleteSelected() {
        	boolean needUpdate = false;
        	for (int i = mDBArrayAdapter.getCount() - 1; i >= 0; i--) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        			WarrRepairRecord item = mDBArrayAdapter.getItem(i);        			
    				if (dbSch.removeWarrRepairRepairId(item.id, item.id_external, item.id_repair))
    					needUpdate = true;         			
        	    	mDBArrayAdapter.remove(item);
        		}
        	}
    		if (needUpdate) {
    			updated = true;
    		}		
        	mDBArrayAdapter.clearSelection();
        	mDBArrayAdapter.notifyDataSetChanged();
        	updateCaption();
        }        
                
    }
    

}
