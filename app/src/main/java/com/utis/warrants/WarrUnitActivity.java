package com.utis.warrants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import com.utis.warrants.InputNameDialogFragment.InputNameDialogListener;
import com.utis.warrants.YesNoDialogFragment.YesNoDialogListener;
import com.utis.warrants.record.EmpWorkTimeRecord;
import com.utis.warrants.record.GoodsRecord;
import com.utis.warrants.record.StateRecord;
import com.utis.warrants.record.WarrUnitRecord;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.WarrUnitTable;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
//import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class WarrUnitActivity extends FragmentActivity implements OnClickListener, 
	InputNameDialogListener, YesNoDialogListener {
	private static final boolean D = true;
	private static final String TAG = "WarrUnitActivity";
	private static final int JOB_SELECT = 1;
	private static final int REPAIR_SHOW = 2;
	private static final int SEAL_CERT_EDIT = 3;
	private static final int DIALOG_DONE = 1;
	private static final int DIALOG_UNDONE = 2;
    private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private CustomWarrUnitAdapter mDBArrayAdapter;
    public Context mContext;
    private TextView captionView, info;
    private String warrantId, warrContId, warrContIdExt, specsId;
    private String warrNumDate, warrJob;
    private String eqSrvBeginDate, eqSrvEndDate;
    private int cur_warrUnitId, warrJobId, warrEqId;
    private long cur_warrUnitIdExt, warrId;
    private boolean updateEnabled, updated, eqInService;
    private String warrEq, warrRemark;
    private WarrUnitRecord warrUnit;
	private View addJobButton, sealJobButton, markJobButton;
	private int checkedCntr, dialogMode, gdsType, mark;
	private java.util.Date srvDateStart, srvDateEnd, todayDate; 
	private WarrUnitRecord selectedRecord = null;

    private class CustomWarrUnitAdapter extends ArrayAdapter<WarrUnitRecord> { 
    	private WarrUnitActivity ownerActivity;
    	private Context context;
    	
    	public CustomWarrUnitAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		this.context = context;
    		ownerActivity = (WarrUnitActivity) context;
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
	    	if(row == null) { 
				LayoutInflater inflater = (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.warr_unit_row, null);
//	    		row = LayoutInflater.from(getContext()).inflate(R.layout.warr_unit_row, parent, false); 
	    	} 
	    	final WarrUnitRecord item = getItem(position); 
	    	TextView textJob = (TextView)row.findViewById(R.id.lineJob);
	    	TextView textRem = (TextView)row.findViewById(R.id.lineRemark);
	    	TextView textJobCntr = (TextView)row.findViewById(R.id.lineRepairCntr);
	    	CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBox);
	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    		checkBox.setEnabled(false);
	    		checkBox.setVisibility(View.GONE);
	    	}
	    	ImageView arrow_image = (ImageView)row.findViewById(R.id.lineArrowImg);
	    	textJob.setText(item.jobName);
	    	textRem.setText(item.remark);
	    	if (item.repairPartsEnabled) {
	    		textJobCntr.setText(""+ item.repairCount);
	    		arrow_image.setVisibility(View.VISIBLE);
	    	} else {
	    		textJobCntr.setText("");
	    		arrow_image.setVisibility(View.INVISIBLE);  
	    	}
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
		    		    if(((CheckBox)v).isChecked()) { 
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
		setContentView(R.layout.activity_warr_unit); 		
		mContext = this;
        addJobButton = findViewById(R.id.add_job_button);
        addJobButton.setOnClickListener(this);
        sealJobButton = findViewById(R.id.seal_job_button);
        sealJobButton.setOnClickListener(this);
        markJobButton = findViewById(R.id.mark_job_button);
        markJobButton.setOnClickListener(this);
		captionView = (TextView)findViewById(R.id.title_text);
		info = (TextView)findViewById(R.id.info_text);
		mDBArrayAdapter = new CustomWarrUnitAdapter(this, R.layout.warr_unit_row);
//		mDBArrayAdapter = new ArrayAdapter<WarrUnitRecord>(this, android.R.layout.simple_list_item_multiple_choice);
        mDBListView = (ListView) findViewById(R.id.listViewWarr);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mWarrUnitClickListener);
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); 
		    mDBListView.setMultiChoiceModeListener(new ModeCallback()); 
	    }				
		Bundle b = getIntent().getExtras();
		warrContId = b.getString("id");
		warrContIdExt = b.getString("id_c");
		warrantId = b.getString("id_w");
		warrId = Long.parseLong(warrantId);
		updateEnabled = Boolean.parseBoolean(b.getString("update_enabled"));
		specsId = b.getString("id_specs");

		warrNumDate = b.getString("num");
		warrJob = b.getString("job");
		warrJobId = Integer.parseInt(b.getString("idjob"));
		warrEq = b.getString("eq");
		warrEqId = Integer.parseInt(b.getString("id_eq"));
		warrRemark = b.getString("rem");
		eqSrvBeginDate = b.getString("SrvBeginDate");
		eqInService = true;
//		todayDate = Calendar.getInstance().getTime();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		todayDate = cal.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");    	
		if (eqSrvBeginDate == null || eqSrvBeginDate.length() == 0) { 
			eqSrvBeginDate = CommonClass.noData;
			eqInService = false;
		} else {
			try {
				srvDateStart = formatter.parse(eqSrvBeginDate); 
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				eqInService = false;
			}  			
		}
		eqSrvEndDate = b.getString("SrvEndDate");
		if (eqSrvEndDate == null || eqSrvEndDate.length() == 0) { 
			eqSrvEndDate = CommonClass.noData;
//			eqInService = false;
		} else {
			try {
				srvDateEnd = formatter.parse(eqSrvEndDate); 
			} catch (ParseException e) {
				e.printStackTrace();
				eqInService = false;
			}  						
		}
		if (eqInService) {
			eqInService = ((srvDateStart == null)||(srvDateStart != null && !srvDateStart.after(todayDate)) &&
				((srvDateEnd == null) || (srvDateEnd != null && !srvDateEnd.before(todayDate))));
		}
//		warrContId = getIntent().getStringExtra("id_c");
		if ((warrContId != null)&&(warrContId.length() > 0)) {
//			Toast.makeText(this, warrContId, Toast.LENGTH_SHORT).show();
			refreshWarrUnitList(false);
		}
		info.setText(String.format(getString(R.string.m_wu_footer),
				warrNumDate, warrEq, warrJob, warrRemark, eqSrvBeginDate, eqSrvEndDate));
		
        mark = dbSch.getEmpWorkLastMark(warrId);
        if (updateEnabled && mark != EmpWorkTimeRecord.BEGIN_WORK) markJobButton.setEnabled(false);
        addJobButton.setEnabled(updateEnabled);
        
        gdsType = dbSch.getGdsType(warrEqId);
        if (gdsType == GoodsRecord.GDS_IS_PPO && eqInService) {
        	sealJobButton.setVisibility(View.VISIBLE);
        } else {
        	sealJobButton.setVisibility(View.INVISIBLE);
        }
        
        addJobButton.setVisibility(View.GONE);
        markJobButton.setVisibility(View.GONE);
        sealJobButton.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.warr_unit, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_remark);
		if (mi != null) {
			mi.setEnabled(updateEnabled && checkedCntr == 1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
			mi.setVisible(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);			
		}
	    mi = menu.findItem(R.id.action_delete);
	    if (mi != null) {
	    	mi.setEnabled(updateEnabled && checkedCntr > 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
	    	mi.setVisible(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
	    }
	    mi = menu.findItem(R.id.action_add_job);
	    if (mi != null) {
	    	mi.setEnabled(updateEnabled);
	    }
	    mi = menu.findItem(R.id.action_complete_job);
	    if (mi != null) {
	    	mi.setEnabled(updateEnabled && mark == EmpWorkTimeRecord.BEGIN_WORK);
//	    	mi.setVisible(updateEnabled && mark == EmpWorkTimeRecord.BEGIN_WORK);
	    }
	    mi = menu.findItem(R.id.action_show_sealcert);
	    if (mi != null) {
	    	mi.setEnabled(gdsType == GoodsRecord.GDS_IS_PPO && eqInService);
	    	mi.setVisible(gdsType == GoodsRecord.GDS_IS_PPO && eqInService);
	    }
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            return true;
        case R.id.action_remark:
        	showInputNameDialog();
        	return true;
        case R.id.action_delete:
        	deleteChecked();
        	return true;
        case R.id.action_add_job:
        	addJob();
        	return true;
        case R.id.action_complete_job:
        	markJob();
        	return true;
        case R.id.action_show_sealcert:
        	showSealCertList();
        	return true;
        }
        return false;
    }	

	//===Input Name Dialog===
	private void showInputNameDialog() {
		WarrUnitRecord wu = null;
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
    		wu = selectedRecord;
    	else
    		wu = getCheckedRecord();
		if (wu != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			InputNameDialogFragment inputNameDialog = new InputNameDialogFragment();
			inputNameDialog.setCancelable(false);
			inputNameDialog.setDialogTitle(getString(R.string.m_add_comment));
			inputNameDialog.setDialogText(wu.remark);
			inputNameDialog.show(fragmentManager, "input dialog");
		}
	}	

	@Override
	public void onFinishInputDialog(String inputText) {
//		Toast.makeText(this, "Returned from dialog: "+ inputText, Toast.LENGTH_SHORT).show();
		WarrUnitRecord wu = null;
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { 
    		wu = selectedRecord;    	
    		if (wu != null) {
    			if (dbSch.updateWarrUnitRemark(wu.id, inputText)) {
    				wu.remark = inputText;
    				mDBArrayAdapter.notifyDataSetChanged();
    				selectedRecord = null;
    			}
    		}
    	} else {
    		wu = getCheckedRecord();
    		if (wu != null) {
    			if (dbSch.updateWarrUnitRemark(wu.id, inputText))
    				refreshWarrUnitList(true);
    		}
    	}
	}
	
	private WarrUnitRecord getCheckedRecord() {
		WarrUnitRecord wu = null;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			WarrUnitRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) {
				wu = item;
				break;
			}
		}
		return wu;
	}
		

    // The on-click listener for ListViews
    private OnItemClickListener mWarrUnitClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	warrUnit = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + warrUnit.id_external);

            if (warrUnit.repairPartsEnabled) {  
				Intent i = new Intent(mContext, WarrRepairActivity.class);
				Bundle b = new Bundle();
				b.putString("id", Integer.toString(warrUnit.id));//Your id
				b.putString("id_w", warrantId); //Warrant id
				b.putString("update_enabled", Boolean.toString(updateEnabled));
				b.putString("num", warrNumDate);
				b.putString("job", warrUnit.jobName);
				b.putString("eq", warrEq);
	            b.putString("rem", warrUnit.remark); 
				i.putExtras(b); //Put your id to your next Intent
//				startActivity(i);
				startActivityForResult(i, REPAIR_SHOW); 
            }
        }
    };    
    	
	@Override
	public void onBackPressed() {
        Intent returnIntent = new Intent();
		Bundle b = new Bundle();
		b.putString("updated", Integer.toString((updated) ? 1 : 0));
		returnIntent.putExtras(b); 
        setResult(RESULT_OK, returnIntent);
	    super.onBackPressed();
	}
	
	private void refreshWarrUnitList(boolean setModified) {
		if (setModified) {
	        dbSch.setWarrContModifiedState(Long.parseLong(warrContId));
	        dbSch.setWarrantModifiedState(Long.parseLong(warrantId));
	        updated = true;
		}
		showWarrUnit(dbSch, WarrUnitTable.ID_WARR_CONT + " = " + warrContId + " AND " +
				WarrUnitTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION);
	}
	
    private void showWarrUnit(DBSchemaHelper sh, String sWhere) {
    	String q_nm = "(SELECT "+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
    			" WHERE "+ GoodsTable.ID_EXTERNAL +" = "+ WarrUnitTable.ID_JOB +")as "+ WarrUnitRecord.NM;
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;  
    	String query;
    	
    	if (sWhere.length() == 0) {
    		query = "SELECT *,"+ q_nm +" FROM " + WarrUnitTable.TABLE_NAME + " ORDER BY " + WarrUnitRecord.NM + " ASC";
    	} else {
    		query = "SELECT *,"+ q_nm +" FROM " + WarrUnitTable.TABLE_NAME + " WHERE " + sWhere + 
    				" ORDER BY " + WarrUnitRecord.NM;
    	}
		try {
			c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		warrUnit = new WarrUnitRecord(c);
	    		warrUnit.repairPartsEnabled = dbSch.isGdsIdEnableEmpHW(warrUnit.id_job);
	    		if (warrUnit.repairPartsEnabled) {
	    			warrUnit.repairCount = (int) sh.getWarrUnitRepairCount(warrUnit.id);
	    		}	    		
	    		if (D) Log.d(TAG, "WarrUnit = " + warrUnit.id_external);
	    		mDBArrayAdapter.add(warrUnit);
	    	}    	   	
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
			updateCaption();
	    	if (c != null) c.close();
	    	markJobButton.setEnabled(!checkWarrContJobDone(warrJobId));
		}
    }

    private void updateCaption() {
    	captionView.setText(getString(R.string.caption_warr_unit_info) + mDBArrayAdapter.getCount());
    }
    
    private void addJob() {
		Intent intent = new Intent(mContext, JobGroupSelectActivity.class);
		Bundle b = new Bundle();
		b.putString("ttype", Integer.toString(JobGroupSelectActivity.TYPE_JOB_SELECT));
		b.putString("excluded_job", Integer.toString(warrJobId));
		intent.putExtras(b); //Put your id to your next Intent
		startActivityForResult(intent, JOB_SELECT);    	
    }
    
    private void markJob() {
		if (!dbSch.isGdsIdSealRelated(warrJobId))
			addWarrUnitJob(warrJobId);
    	showYesNoDialog(DIALOG_DONE, getString(R.string.m_mark_job_done));    	
    }
    
    private void showSealCertList() {
		Intent intent = new Intent(mContext, SealCertListActivity.class);
		Bundle b = new Bundle();
		b.putString("Id", "");
		b.putString("id_specs", specsId);
		b.putString("warrContId", warrContIdExt);
		intent.putExtras(b); //Put your id to your next Intent
		startActivityForResult(intent, SEAL_CERT_EDIT);		    	
    }
    
	@Override
	public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.add_job_button:
    		if (D) Log.d(TAG, "clicked on add_job_button");
    		addJob();
    		break;
    	case R.id.seal_job_button:
    		if (D) Log.d(TAG, "clicked on seal_job_button");
    		showSealCertList();
    		break;
    	case R.id.mark_job_button:
    		if (D) Log.d(TAG, "clicked on mark_job_button");
    		markJob();
    		break;   
       	}				
	}
	
	private void deleteChecked() {
		boolean needUpdate = false;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			WarrUnitRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) { 
//				mDBArrayAdapter.remove(item);
				if (item.repairCount == 0) {
					if (item.id_job == warrJobId) {
						cur_warrUnitId = item.id;
						cur_warrUnitIdExt = item.id_external;	
						showYesNoDialog(DIALOG_UNDONE, getString(R.string.m_mark_job_undone) + warrJob +"?");
					} else if (dbSch.isGdsIdSealRelated(item.id_job)) {
						CommonClass.showMessage(mContext, item.jobName, getString(R.string.m_del_warr_unit_seals));
						
					} else {
						if (dbSch.removeWarrUnitJobId(item.id, item.id_external, item.id_job))
							needUpdate = true;
					}
				} else {
					showDelMessage();
					item.selected = false;
					needUpdate = true;
				}
			}
		}
		if (needUpdate) {
			refreshWarrUnitList(true);
			updated = true;
		}		
	}
	
	private void showDelMessage() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder
	    .setTitle(getString(R.string.m_del_warr_unit))
	    .setMessage(getString(R.string.m_del_warr_unit_repair))
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) 
	        {       
	            //do some thing here which you need
	        	dialog.dismiss();
	        }
	    });
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case JOB_SELECT:
                if (data.hasExtra("id")) {
                	int jobId = Integer.parseInt(data.getExtras().getString("id"));
//                    Toast.makeText(this, data.getExtras().getString("id"),
//                        Toast.LENGTH_SHORT).show();
                	addWarrUnitJob(jobId);
                }
    			break;
    		case SEAL_CERT_EDIT:
    			
    			break;
    		case REPAIR_SHOW:
                if (data.hasExtra("updated")) {
                	int updated = Integer.parseInt(data.getExtras().getString("updated"));
                	if (updated > 0) {
                		refreshWarrUnitList(true);
                	}
                }
    		}
        }
    }   
	
    private void addWarrUnitJob(int jobId) {
        dbSch.addWarrUnitItem(0, Long.parseLong(warrContId), jobId, 2, "");
        refreshWarrUnitList(true);
        updated = true;
    }

    private boolean checkWarrContJobDone(int WarrContJobId) {
		boolean jobFound = false;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			WarrUnitRecord item = mDBArrayAdapter.getItem(i);
			if (item.id_job == WarrContJobId) { 
				jobFound = true;
				break;
			}
		}
    	return jobFound; 
    } 

	//===YES/No Dialog===
	private void showYesNoDialog(int dialogMode, String dialogText) {
		this.dialogMode = dialogMode;
		FragmentManager fragmentManager = getSupportFragmentManager(); 
		YesNoDialogFragment yesnoDialog = new YesNoDialogFragment();
		yesnoDialog.setCancelable(false);	
		yesnoDialog.setDialogTitle(getString(R.string.title_change_status));
		yesnoDialog.setDialogText(dialogText);
		yesnoDialog.show(fragmentManager, "yes/no dialog");
	}
	
	@Override
	public void onFinishYesNoDialog(boolean state) {
//		Toast.makeText(this, "Returned from dialog: "+ state, Toast.LENGTH_SHORT).show();
		switch (dialogMode) {
		case DIALOG_DONE:
			int eqState = (state)? StateRecord.EQ_STATE_IN_ORDER : StateRecord.EQ_STATE_NOTIN_ORDER;
			if (dbSch.setWarrContJobEqState(Long.parseLong(warrantId), Long.parseLong(warrContId), 
				StateRecord.JOB_STATE_IS_DONE, eqState)) updated = true;
			break;
		case DIALOG_UNDONE:
			if (state) {
				if (dbSch.removeWarrUnitJobId(cur_warrUnitId, cur_warrUnitIdExt, warrJobId)) updated = true;
				cur_warrUnitId = 0;
				if (dbSch.setWarrContJobEqState(Long.parseLong(warrantId), Long.parseLong(warrContId), 
					StateRecord.JOB_STATE_TODO, StateRecord.EQ_STATE_NOTIN_ORDER)) updated = true;
				if (updated)
					refreshWarrUnitList(true);
			}
			break;
		}
	}

	private class ModeCallback implements ListView.MultiChoiceModeListener {
		private MenuItem miRemark, miDelete;

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.warr_unit_rowselection, menu);
            mode.setTitle(mContext.getString(R.string.selected));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        	miRemark = menu.findItem(R.id.action_remark);
    		miDelete = menu.findItem(R.id.action_delete);
    		updateMenuItems();
            return true;
        }

        private void updateMenuItems() {
        	int checkedCount = mDBListView.getCheckedItemCount();
    		if (miRemark != null)
    			miRemark.setEnabled(updateEnabled && checkedCount == 1);
    		if (miDelete != null)
    			miDelete.setEnabled(updateEnabled && checkedCount > 0);        	        	
        }
        
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_remark:       
            	selectedRecord = getSelectedRecord();
            	showInputNameDialog();             	
                mode.finish();
                return true;
            case R.id.action_delete:
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
            updateMenuItems();
        }
        
    	private WarrUnitRecord getSelectedRecord() {
    		WarrUnitRecord wu = null;
    		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
    			if (mDBArrayAdapter.isPositionChecked(i)) {
    				wu = mDBArrayAdapter.getItem(i);        
    				break;
    			}
    		}
    		return wu;
    	}
        
        private void deleteSelected() {
        	boolean needUpdate = false;
        	for (int i = mDBArrayAdapter.getCount() - 1; i >= 0; i--) {
        		if (mDBArrayAdapter.isPositionChecked(i)) {
        			WarrUnitRecord item = mDBArrayAdapter.getItem(i);                			
    				if (item.repairCount == 0) {
    					if (item.id_job == warrJobId) {
    						cur_warrUnitId = item.id;
    						cur_warrUnitIdExt = item.id_external;	
    						showYesNoDialog(DIALOG_UNDONE, getString(R.string.m_mark_job_undone) + warrJob +"?");
    					} else if (dbSch.isGdsIdSealRelated(item.id_job)) {
    						CommonClass.showMessage(mContext, item.jobName, getString(R.string.m_del_warr_unit_seals));    						
    					} else {
    						if (dbSch.removeWarrUnitJobId(item.id, item.id_external, item.id_job)) {
    							needUpdate = true;
    		        	    	mDBArrayAdapter.remove(item);
    						}
    					}
    				} else {
    					showDelMessage();
    					needUpdate = true;
    				}        			
        		}
        	}
    		if (needUpdate) {
    			refreshWarrUnitList(true);
    			updated = true;
    		}		
        	//mDBArrayAdapter.clearSelection();
        	mDBArrayAdapter.notifyDataSetChanged();
        	updateCaption();
        }        
                
    }

    public void btnTestAdd(View view){
        markJob();
    }
	
	
}
