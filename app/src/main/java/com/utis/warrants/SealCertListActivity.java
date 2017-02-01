package com.utis.warrants;

import java.util.HashMap;
import java.util.Set;

import com.utis.warrants.R.string;
import com.utis.warrants.record.SealCertRecord;
import com.utis.warrants.record.SpecsRecord;
import com.utis.warrants.tables.EntTable;
import com.utis.warrants.tables.SealCertTable;

import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SealCertListActivity extends FragmentActivity {
	private static final boolean D = true;
	private static final String TAG = "SealCertListActivity";
	private static final int SEAL_CERT_EDIT = 1;
	public static final int SEAL_CERT_READY_TO_SEND = 2;
	private static final int SEAL_CERT_EXTERNAL = 3;
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private CustomSealCertAdapter mDBArrayAdapter;
    public Context mContext;
    private TextView captionView;
    private SealCertRecord mSealCert;
    private String mWarrContId, specsId;
    private long reasonCntr, idWarrCont;
    private SharedPreferences prefs;
    

    private static class CustomSealCertAdapter extends ArrayAdapter<SealCertRecord> {
    	private SealCertListActivity ownerActivity;
    	static class SealCertViewHolder {
    	    public TextView textNum;
    	    public TextView textCertDate;
    	    public TextView textSealBrokenDate;
    	    public TextView textSealSetDate;
    	    public TextView textMoney;
    	    public TextView textEntName;
    	    public TextView textEqName;
    	    public TextView textEqInfo;
    	    public TextView textCntr;
    	    public TextView textWarrNum;
    	    public TextView textSeals;
    	    public ImageView imageArrow;
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
    	
    	public CustomSealCertAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		ownerActivity = (SealCertListActivity) context;
    	} 
    	
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) {
    		final SealCertViewHolder viewHolder;
	    	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) { 
	    		row = LayoutInflater.from(getContext()).inflate(R.layout.seal_cert_row, parent, false); 
	    		viewHolder = new SealCertViewHolder();
	    		viewHolder.textNum = (TextView)row.findViewById(R.id.lineNo);
	    		viewHolder.textWarrNum = (TextView)row.findViewById(R.id.lineState);
	    		viewHolder.textCertDate = (TextView)row.findViewById(R.id.lineDate);
	    		viewHolder.textSealBrokenDate = (TextView)row.findViewById(R.id.lineSealBrokenDate);
	    		viewHolder.textSealSetDate = (TextView)row.findViewById(R.id.lineSealSetDate);
	    		viewHolder.textMoney = (TextView)row.findViewById(R.id.lineMoney);
	    		viewHolder.textEntName = (TextView)row.findViewById(R.id.lineEntName);
	    		viewHolder.textEqName = (TextView)row.findViewById(R.id.lineEqName);
	    		viewHolder.textEqInfo = (TextView)row.findViewById(R.id.lineEqInfo);
	    		viewHolder.textCntr = (TextView)row.findViewById(R.id.lineCntr);
	    		viewHolder.textSeals = (TextView)row.findViewById(R.id.lineSeals);
	    		viewHolder.imageArrow = (ImageView)row.findViewById(R.id.right_arrow);
	    		row.setTag(viewHolder);
	    	} else {
	    		viewHolder = (SealCertViewHolder) row.getTag();	    		
	    	}
	    	final SealCertRecord item = getItem(position); 
    		viewHolder.textNum.setText("№"+ item.getNum());
    		viewHolder.textWarrNum.setText(ownerActivity.getString(R.string.m_warr_num) + item.warrNum);
    		viewHolder.textCertDate.setText(item.dateChangeFmt);
    		viewHolder.textSealBrokenDate.setText(item.dateBeginFmt);
    		viewHolder.textSealSetDate.setText(item.dateEndFmt);
    		viewHolder.textMoney.setText(Double.toString(item.ppoMoney));
    		viewHolder.textEntName.setText(item.entName);
    		viewHolder.textEqName.setText(item.eqName);
			viewHolder.textEqInfo.setText("с/н: "+ item.eqSrl + "; ф/н: "+ item.eqFisk);
    		viewHolder.textCntr.setText(""+ item.sealCnt);
    		viewHolder.textSeals.setText(item.seals);
    		if (item.idExternal <= 0) {
    			if (item.sealCnt > 0)
    				viewHolder.textNum.setTextColor(Color.BLUE);
    			else
    				viewHolder.textNum.setTextColor(Color.RED);
    		} else {
    			viewHolder.textNum.setTextColor(Color.BLACK);
    		}
    		
	    	if (item.idExternal > 0) {
	    		viewHolder.imageArrow.setVisibility(View.INVISIBLE);
	    		row.setBackgroundResource(R.drawable.list_selector_done);
	    	} else if (item.modified == SEAL_CERT_READY_TO_SEND) {
	    		viewHolder.imageArrow.setVisibility(View.INVISIBLE);
	    		row.setBackgroundResource(R.drawable.list_selector_modified);
	    	} else {
	    		viewHolder.imageArrow.setVisibility(View.VISIBLE);
	    		row.setBackgroundResource(R.drawable.list_selector);
	    	}
//    		row.setBackgroundResource(R.drawable.list_selector);
    		if (mSelection.get(position) != null) {
				row.setBackgroundResource(R.drawable.list_selector_selected);    			
    		}	
	    	return row; 
    	} 
    }    
    
        
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seal_cert_list);		
		mContext = this;
		captionView = (TextView)findViewById(R.id.title_text);
		mDBArrayAdapter = new CustomSealCertAdapter(this, R.layout.seal_cert_row);
        mDBListView = (ListView) findViewById(R.id.listViewSealCert);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mSealCertClickListener);	
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); 
		    mDBListView.setMultiChoiceModeListener(new ModeCallback()); 
	    }
		Bundle b = getIntent().getExtras();
		mWarrContId = b.getString("warrContId");
		try {
			idWarrCont = Long.parseLong(mWarrContId);
		} catch (Exception e) {
			idWarrCont = -1;
		}
		specsId = b.getString("id_specs");
		reasonCntr = dbSch.getReasonCount();
		prefs = getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
	}
	
    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        showSealCert(dbSch);
    } 
    	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.seal_cert_list, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_add_seal_cert);
		boolean warrContInSealCert = dbSch.isWarrContInSealCert(idWarrCont);
		if (mi != null) {
			mi.setEnabled(specsId.length() > 0 && reasonCntr > 0 && !warrContInSealCert);
			mi.setVisible(specsId.length() > 0 && reasonCntr > 0 && !warrContInSealCert);
		}
	    return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.action_add_seal_cert:        	
			showSealCertEdit("", mWarrContId, specsId);
            return true;
        }
        return false;
    }

    // The on-click listener for ListViews
    private OnItemClickListener mSealCertClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	mSealCert = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "seal =" + mSealCert.toString());
            if (mSealCert.idExternal <= 0 && (mSealCert.modified == SEAL_CERT_EDIT || mSealCert.modified == 0))
            	showSealCertEdit(Integer.toString(mSealCert.id), Integer.toString(mSealCert.idWarrCont),
					Integer.toString(mSealCert.idSpecs));
        }
    };    
	
    private void showSealCertEdit(String id, String idWarrCont, String idSpecs) {
    	clearSavedDates();
		Intent intent = new Intent(mContext, SealCertEditActivity.class);
		Bundle b = new Bundle();
		b.putString("Id", id);
		b.putString("warrContId", idWarrCont);
		b.putString("id_specs", idSpecs);
		intent.putExtras(b); //Put your id to your next Intent
		startActivityForResult(intent, SEAL_CERT_EDIT);    	
    }
    
    private void clearSavedDates() {
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putString(SealCertEditActivity.KEY_DATE_END, "");
    	editor.putString(SealCertEditActivity.KEY_DATE_BEGIN, "");
    	editor.commit();					    	
    }
        
    private void showSealCert(DBSchemaHelper sh) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null, cSpecs = null, cEnt = null; 
    	String query;   	
    	int colid;
    	query = "SELECT * FROM " + SealCertTable.TABLE_NAME + //" WHERE " + Seal
				" ORDER BY " + SealCertTable.ID + " DESC";    	
		try {
			mDBArrayAdapter.clear();
			c = sqdb.rawQuery(query, null);
	    	while (c.moveToNext()) {    		
	    		mSealCert = new SealCertRecord(c);    		
	    		if (D) Log.d(TAG, "seal = " + mSealCert.toString());
	    		cSpecs = dbSch.getSpecs(mSealCert.idSpecs);
	    		if (cSpecs != null) {
	    			SpecsRecord specs = new SpecsRecord(cSpecs);
	    			mSealCert.eqName = specs.eq_name; 
	    			mSealCert.eqInv = specs.inv; 
	    			mSealCert.eqSrl = specs.srl;
	    			mSealCert.eqFisk = specs.fisk_num;
	    			mSealCert.sealCnt = dbSch.getSealCertSealsCount(mSealCert.id);
	    			mSealCert.warrNum = dbSch.getWarrantNumFromCont(mSealCert.idWarrCont);
	    			mSealCert.seals = dbSch.getSealCertSeals(mSealCert.id);
		    		cEnt = dbSch.getEntName(specs.id_ent);
		    		if (cEnt != null) {
			    		colid = cEnt.getColumnIndex(EntTable.NM);
			    		mSealCert.entName = cEnt.getString(colid);
			    		colid = cEnt.getColumnIndex(EntTable.ID_PARENT);
			    		int idParent = cEnt.getInt(colid);
			    		cEnt = dbSch.getEntName(idParent);
			    		if (cEnt != null) {
			    			colid = cEnt.getColumnIndex(EntTable.NM);
			    			mSealCert.entName = cEnt.getString(colid) + "; " + mSealCert.entName;
			    		}
		    		}
	    		}
	    		mDBArrayAdapter.add(mSealCert);
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
	    	if (D) Log.d(TAG, "Count = " + mDBArrayAdapter.getCount());
	    	if (reasonCntr > 0)
	    		captionView.setText(getString(R.string.caption_seal_cert) + mDBArrayAdapter.getCount());
	    	else
	    		captionView.setText(getString(R.string.caption_no_seal_cert_reasons));
	    	if (c != null) c.close();
	    	if (cSpecs != null) cSpecs.close();
	    	if (cEnt != null) cEnt.close();
		}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case SEAL_CERT_EDIT:
    			showSealCert(dbSch);
    			break;
    		}
        }
    }   
	
	
	private class ModeCallback implements ListView.MultiChoiceModeListener {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.sealcerts_rowselection, menu);
            mode.setTitle(mContext.getString(R.string.selected));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        	int scsState = getSelectedSealCertState();
    		MenuItem mi = menu.findItem(R.id.action_delete_sealcert);
    		if (mi != null) { 
    			mi.setEnabled(scsState == SEAL_CERT_EDIT);
    		}
    		mi = menu.findItem(R.id.action_mark_2_send);
    		if (mi != null) {    			
    			if (scsState == SEAL_CERT_EDIT) { 
    				mi.setTitle(mContext.getString(string.action_send));
    				mi.setEnabled(true);
    			} else if (scsState == SEAL_CERT_READY_TO_SEND) {
    				mi.setTitle(mContext.getString(string.action_not_send));
    				mi.setEnabled(true);
    			} else {
    				mi.setEnabled(false);
    			}    			
    		}
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_delete_sealcert:
            	deleteSelected();
                // the Action was executed, close the CAB
                mode.finish();
                return true;
            case R.id.action_mark_2_send:
            	setSelectedSealCertState();
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
        
        private int getSelectedSealCertState() {
        	int res = SEAL_CERT_EDIT;
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {       			
        			SealCertRecord item = mDBArrayAdapter.getItem(i);
        			if (item.idExternal > 0) {
        				res = SEAL_CERT_EXTERNAL;
        				break;
        			} else if (item.modified == SEAL_CERT_READY_TO_SEND) {
        				res = SEAL_CERT_READY_TO_SEND;
//        				break;        				
        			}
        		}    		
        	}
        	return res;
        }
        
        private void setSelectedSealCertState() {
        	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        	    	SealCertRecord item = mDBArrayAdapter.getItem(i);
        	    	if (item.idExternal <= 0) {
	        			if ((item.modified == 0 || item.modified == SEAL_CERT_EDIT)  && item.sealCnt > 0) {
	        				if (dbSch.updateSealCertModified(item.id, SEAL_CERT_READY_TO_SEND))
	        					item.modified = SEAL_CERT_READY_TO_SEND;
	        			} else if (item.modified == SEAL_CERT_READY_TO_SEND){
	        				if (dbSch.updateSealCertModified(item.id, SEAL_CERT_EDIT))
	        					item.modified = SEAL_CERT_EDIT;
	        			}
        	    	}
        		}
        	}	        	
        	mDBArrayAdapter.clearSelection();
        	mDBArrayAdapter.notifyDataSetChanged();
        }

        
        private void deleteSelected() {
        	for (int i = mDBArrayAdapter.getCount() - 1; i >= 0; i--) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        			SealCertRecord item = mDBArrayAdapter.getItem(i);
        	    	dbSch.removeSealCert(item.id);
        	    	mDBArrayAdapter.remove(item);
        		}
        	}
        	showSealCert(dbSch);
			invalidateOptionsMenu();
        }        
        
                
    }

}
