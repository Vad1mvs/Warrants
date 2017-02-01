package com.utis.warrants;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;
import com.utis.warrants.InputNameDialogFragment.InputNameDialogListener;
import com.utis.warrants.R.string;
import com.utis.warrants.record.GoodsRecord;
import com.utis.warrants.record.SpecsRecord;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.SpecsTable;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SpecsSelectActivity extends FragmentActivity implements InputNameDialogListener, 
		OnClickListener, CompoundButton.OnCheckedChangeListener {
	private static final boolean D = true;
	private static final String TAG = "SpecsSelectActivity";
	private static final int EQ_SELECT = 1;
	private static final int EDIT_INVENTORY = 1;
	private static final int EDIT_CASH_NUM = 2;
	private static final int EDIT_CASH_IP = 3;
	private static final int EDIT_MODEM_IP = 4;
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private ArrayAdapter<SpecsRecord> mDBArrayAdapter;
    public Context mContext;
    private TextView captionView;
    private SpecsRecord specs;
    private String idEnt, idContract, idGds, whereStr;
    private long warrId;
    private boolean gdsGrouping;
    private int checkedCntr = 0, mInputNameMode;

    private static class CustomSpecsAdapter extends ArrayAdapter<SpecsRecord> {
    	private SpecsSelectActivity ownerActivity;
    	private Context context;
    	private String cashNum;
    	private static SpecsViewHolder viewHolder;
//    	private SpecsRecord item;
    	
    	public CustomSpecsAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		ownerActivity = (SpecsSelectActivity) context;
    		this.context = context;
    		cashNum = context.getString(R.string.lbl_cash_num);
    	} 
    	
    	static class SpecsViewHolder {
    		public TextView textId;
    	    public TextView textEq;
    	    public TextView textEqSrl;
    	    public TextView textEqPlace;
    	    public TextView textSrv;
    	    public CheckBox checkBox;
    	    public TextView textCashNum;
    	    public TextView textCashIp;
    	    public TextView textModemIp;
    	    public Switch factorySeal;
    	}
    	    	    	
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) { 		  	
		  	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
			if(row == null) {
				LayoutInflater inflater = (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.specs_row, null);
//				row = LayoutInflater.from(getContext()).inflate(R.layout.log_row, parent, false);
				viewHolder = new SpecsViewHolder();
				viewHolder.textId = (TextView)row.findViewById(R.id.lineId);
				viewHolder.textEq = (TextView)row.findViewById(R.id.lineEq);
				viewHolder.textEqSrl = (TextView)row.findViewById(R.id.lineEqSN);
				viewHolder.textEqPlace = (TextView)row.findViewById(R.id.lineEqPlace);
				viewHolder.textSrv = (TextView)row.findViewById(R.id.lineEqService);
				viewHolder.checkBox = (CheckBox) row.findViewById(R.id.checkBox);
				viewHolder.textCashNum = (TextView)row.findViewById(R.id.lineCashNum);
				viewHolder.textCashIp = (TextView)row.findViewById(R.id.lineCashIp);
				viewHolder.textModemIp = (TextView)row.findViewById(R.id.lineModemIp);
				viewHolder.factorySeal = (Switch)row.findViewById(R.id.switchFactorySeal);
				row.setTag(viewHolder);
			} else {
				viewHolder = (SpecsViewHolder) row.getTag();
			}
			SpecsRecord item = getItem(position);
			viewHolder.textId.setText("" + item.id);
			viewHolder.textEq.setText(item.eq_name);
	    	if (!ownerActivity.gdsGrouping) {
	    		if (ownerActivity.warrId == 0) {
	    			viewHolder.checkBox.setVisibility(View.VISIBLE);
	    			viewHolder.factorySeal.setVisibility(View.VISIBLE);
	    		} else {
	    			viewHolder.checkBox.setVisibility(View.INVISIBLE);
	    			viewHolder.factorySeal.setVisibility(View.GONE);
	    		}
	    		viewHolder.factorySeal.setEnabled(item.selected);
    			viewHolder.factorySeal.setChecked(item.factorySeal > 0);
				viewHolder.textEqSrl.setText(String.format(
						context.getString(string.m_srl_num), item.srl, item.inv));
				viewHolder.textEqPlace.setText(item.place);
				viewHolder.textSrv.setText(String.format(
						context.getString(string.m_specs_srv), item.beginDate, item.endDate));
		    	if (item.temp > 0) viewHolder.textEq.setTextColor(CommonClass.clrDkRed);
		    	else viewHolder.textEq.setTextColor(Color.BLACK);
		    	if (!item.eqInService) viewHolder.textEq.setTextColor(Color.RED);
		    	else viewHolder.textEq.setTextColor(Color.BLACK);
		    	if (item.gdsType == GoodsRecord.GDS_IS_PPO) {
			    	if (item.numCash > 0) {
			    		viewHolder.textCashNum.setText(cashNum + item.numCash);
			    	} else {
			    		viewHolder.textCashNum.setText(cashNum);
			    	}
		    		viewHolder.textCashIp.setText(item.ipCash);
		    		viewHolder.textModemIp.setText(item.ipModem);
		    	} else {
		    		viewHolder.textCashNum.setVisibility(View.GONE);
		    		viewHolder.textCashIp.setVisibility(View.GONE);
		    		viewHolder.textModemIp.setVisibility(View.GONE);
		    	}
	    	} else {
	    		viewHolder.checkBox.setVisibility(View.INVISIBLE);
	    		viewHolder.factorySeal.setVisibility(View.GONE);
	    		viewHolder.textEqSrl.setVisibility(View.GONE);
	    		viewHolder.textEqPlace.setVisibility(View.GONE);
	    		viewHolder.textSrv.setVisibility(View.GONE);
	    		viewHolder.textCashNum.setVisibility(View.GONE);
	    		viewHolder.textCashIp.setVisibility(View.GONE);
	    		viewHolder.textModemIp.setVisibility(View.GONE);
	    	}
	    	
	    	viewHolder.checkBox.setChecked(item.selected);
	    	viewHolder.checkBox.setOnClickListener(ownerActivity);
	    	viewHolder.factorySeal.setOnCheckedChangeListener(ownerActivity);
	    	return row; 
    	} 
    }
    
    // The on-click listener for ListViews
    private OnItemClickListener mSpecClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	specs = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + specs.id_external);
    		if (gdsGrouping) {
    			Intent intent = new Intent(mContext, SpecsSelectActivity.class);
    			Bundle b = new Bundle();
    			b.putString("ident", idEnt);
    			b.putString("idcontract", idContract);
    			b.putString("idgds", Integer.toString(specs.id_gds));
    			b.putString("idwarr", Long.toString(warrId));
    			intent.putExtras(b);
				if (warrId == 0) startActivity(intent);  // спецификация
				else startActivityForResult(intent, EQ_SELECT); // добавление оборудования
    		} else {
	            Intent returnIntent = new Intent();
				Bundle b = new Bundle();
				b.putString("id", Integer.toString(specs.id_external));//Your id
				b.putString("idEq", Integer.toString(specs.id_gds));
				b.putString("EqSrl", specs.srl);
				b.putString("EqInv", specs.inv);
				returnIntent.putExtras(b); //Put your id to your Intent
	            setResult(RESULT_OK, returnIntent);       
	            finish();
    		}
        }
    };    
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_specs_select);
		
		mContext = this;
		captionView = (TextView)findViewById(R.id.title_text);
//		mDBArrayAdapter = new ArrayAdapter<SpecsRecord>(this, R.layout.db_data);
		mDBArrayAdapter = new CustomSpecsAdapter(this, R.layout.specs_row);
        mDBListView = (ListView) findViewById(R.id.listViewSpecs);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mSpecClickListener);	
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
        
		Bundle b = getIntent().getExtras();
		idEnt = b.getString("ident");
		idContract = b.getString("idcontract");
		idGds = b.getString("idgds");
		warrId = Long.parseLong(b.getString("idwarr"));
		whereStr = "s."+ SpecsTable.ID_ENT + "=" + idEnt;
		if (idContract.length() > 0)
			whereStr += " AND s." + SpecsTable.ID_CONTRACT + "=" + idContract;
		gdsGrouping = !(idGds.length() > 0);
		if (idGds.length() > 0) 
			whereStr = whereStr + " AND s." + SpecsTable.ID_GDS + "=" + idGds;
		showSpecs(dbSch, whereStr);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.specs_select, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		SpecsRecord item = null;
		if (checkedCntr == 1)
			item = findFirstSelected();
		boolean condition = (warrId == 0) && (item != null);
		boolean conditionPPO = condition && item.gdsType == GoodsRecord.GDS_IS_PPO; 

		MenuItem mi = menu.findItem(R.id.action_edit_inventory_num);
		if (mi != null) { 
			mi.setEnabled(condition);
			mi.setVisible(condition);
		}		
		mi = menu.findItem(R.id.action_edit_cash_num);
		if (mi != null) { 
			mi.setEnabled(conditionPPO);
			mi.setVisible(conditionPPO);
		}		
		mi = menu.findItem(R.id.action_edit_cash_ip);
		if (mi != null) { 
			mi.setEnabled(conditionPPO);
			mi.setVisible(conditionPPO);
		}		
		mi = menu.findItem(R.id.action_edit_modem_ip);
		if (mi != null) { 
			mi.setEnabled(conditionPPO);
			mi.setVisible(conditionPPO);
		}		
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_edit_inventory_num:
        	if (D) Log.d(TAG, "onOptions action_edit_inventory_num");
        	mInputNameMode = EDIT_INVENTORY;
        	showInputNameDialog();
        	return true;
	    case R.id.action_edit_cash_num:
	    	mInputNameMode = EDIT_CASH_NUM;
	    	showInputNameDialog();
	    	return true;
	    case R.id.action_edit_cash_ip:
	    	mInputNameMode = EDIT_CASH_IP;
	    	showInputNameDialog();
	    	return true;
	    case R.id.action_edit_modem_ip:
	    	mInputNameMode = EDIT_MODEM_IP;
	    	showInputNameDialog();
	    	return true;
	    }
        return false;
    }
	
	private void showInputNameDialog() {
		SpecsRecord spec = getCheckedRecord();
		String title = "", editText = "", editComment = "";
		boolean setIpAddrCheck = false;
		int textInputType = InputType.TYPE_CLASS_TEXT;
		if (spec != null) {			
			switch (mInputNameMode) {
			case EDIT_INVENTORY:
				title = getString(R.string.action_edit_inventory_num);
				editComment = getString(R.string.edit_inventory_num);
				editText = spec.inv;
				break;
			case EDIT_CASH_NUM:
				title = getString(R.string.action_edit_cash_num);
				editComment = getString(R.string.edit_cash_num);
				if (spec.numCash > 0)
					editText = ""+ spec.numCash;
				else
					editText = "";
				textInputType = InputType.TYPE_CLASS_NUMBER;
				break;
			case EDIT_CASH_IP:
				title = getString(R.string.action_edit_cash_ip);
				editComment = getString(R.string.edit_cash_ip);
				editText = spec.ipCash;
				textInputType = InputType.TYPE_CLASS_PHONE;
				setIpAddrCheck = true;
				break;
			case EDIT_MODEM_IP:
				title = getString(R.string.action_edit_modem_ip);
				editComment = getString(R.string.edit_modem_ip);
				editText = spec.ipModem;
				textInputType = InputType.TYPE_CLASS_PHONE;
				setIpAddrCheck = true;
				break;
			}
			FragmentManager fragmentManager = getSupportFragmentManager();
			InputNameDialogFragment inputNameDialog = new InputNameDialogFragment();
			inputNameDialog.setCancelable(false);
			inputNameDialog.setDialogTitle(title);
			inputNameDialog.setDialogText(editText);
			inputNameDialog.setIPAddrFilter(setIpAddrCheck);
			inputNameDialog.setTextInputType(textInputType);
			inputNameDialog.setDialogComment(editComment);
			inputNameDialog.show(fragmentManager, "input dialog");
		}
	}	
		
	private SpecsRecord getCheckedRecord() {
		SpecsRecord spec = null;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			SpecsRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) {
				spec = item;
				break;
			}
		}
		return spec;
	}	
		
    private void showSpecs(DBSchemaHelper sh, String sWhere) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null;
    	Set<String> eqGds = new HashSet<String>();
    	String querySelect = "SELECT s.*, ";
    	String q_nm = "(SELECT "+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
    			" g WHERE g."+ GoodsTable.ID_EXTERNAL +" = s."+ SpecsTable.ID_GDS +")as "+ 
    			SpecsRecord.SPECS_EQ_NM;
    	String q_type = "(SELECT "+ GoodsTable.GDSTYPE +" FROM "+ GoodsTable.TABLE_NAME +
    			" g WHERE g."+ GoodsTable.ID_EXTERNAL +" = s."+ SpecsTable.ID_GDS +")as "+ 
    			SpecsRecord.SPECS_EQ_TYPE;
    	String query;
    	    	
    	if(sWhere.length() == 0) {
    		query = querySelect + q_nm + "," + q_type + " FROM " + SpecsTable.TABLE_NAME + " s ORDER BY s." + 
    				SpecsTable.ID_GDS;
    	} else {
    		query = querySelect + q_nm + "," + q_type + " FROM " + SpecsTable.TABLE_NAME + " s WHERE " + sWhere + 
    				" ORDER BY s." + SpecsTable.ID_GDS;
    	}
    	int cntr = 0; 
		try {
			c = sqdb.rawQuery(query, null);
			mDBArrayAdapter.clear();
	    	while (c.moveToNext()) {	    		
	    		specs = new SpecsRecord(c);
	    		if (gdsGrouping) {
	    			if (!eqGds.contains(Integer.toString(specs.id_gds))) {
	    				eqGds.add(Integer.toString(specs.id_gds));
		    			mDBArrayAdapter.add(specs);
		    			cntr++;
	    			}	    			
	    		} else {
	    			if (!dbSch.isWarrContSpecsIdPresent(specs.id_external, warrId, true)) {
						specs.eqInService = CommonClass.isEqServDateValid(specs.begin_date, specs.end_date, new Date(new java.util.Date().getTime()));	    		
		    			mDBArrayAdapter.add(specs);
		    			cntr++;
	    			}
	    		}
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {    	
	    	if (D) Log.d(TAG, "job Count = " + cntr);
			if (gdsGrouping)
				captionView.setText(mContext.getString(R.string.caption_choose_specs_group) + "; " + cntr);
			else
				captionView.setText(mContext.getString(R.string.caption_choose_specs) + "; " + cntr);
	    	if (c != null) c.close();
		}
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	invalidateOptionsMenu();
    	}		
    }

	@Override
	public void onBackPressed() {
		Intent returnIntent = new Intent();
		setResult(RESULT_OK, returnIntent);
		super.onBackPressed();
	}
    
	@Override
	public void onFinishInputDialog(String inputText) {
		SpecsRecord spec = getCheckedRecord();
		if (spec != null) {
			switch (mInputNameMode) {
				case EDIT_INVENTORY:
					if (dbSch.updateSpecInventoryNum(spec.id, inputText)) {
						checkedCntr = 0;
						dbSch.updateWarrContInventoryNum(spec.id_external, inputText);
						showSpecs(dbSch, whereStr);
					}
					break;
				case EDIT_CASH_NUM:
					if (dbSch.updateSpecCashNum(spec.id, Integer.parseInt(inputText))) {
						checkedCntr = 0;
						showSpecs(dbSch, whereStr);
					}					
					break;
				case EDIT_CASH_IP:
					if (dbSch.updateSpecCashIPAddr(spec.id, inputText)) {
						checkedCntr = 0;
						showSpecs(dbSch, whereStr);
					}										
					break;
				case EDIT_MODEM_IP:
					if (dbSch.updateSpecModemIPAddr(spec.id, inputText)) {
						checkedCntr = 0;
						showSpecs(dbSch, whereStr);
					}										
					break;
			}
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case EQ_SELECT:
                if (data.hasExtra("id")) {                	
    	            Intent returnIntent = new Intent();
    				Bundle b = new Bundle();
    				b.putString("id", data.getExtras().getString("id"));
    				b.putString("idEq", data.getExtras().getString("idEq"));
    				b.putString("EqSrl", data.getExtras().getString("EqSrl"));
    				b.putString("EqInv", data.getExtras().getString("EqInv"));
    				returnIntent.putExtras(b); //Put your id to your Intent
    	            setResult(RESULT_OK, returnIntent);       
    	            finish();                	
                }
    			break;
    		}
    	}
    }

	@Override
	public void onClick(View v) {
		if (v instanceof CheckBox) { 
			RelativeLayout parent = (RelativeLayout) v.getParent();	    			
			TextView idTV = (TextView)parent.findViewById(R.id.lineId);
			long id = Long.parseLong(idTV.getText().toString());
			SpecsRecord item = findItem(id);
			Switch factorySeal = (Switch)parent.findViewById(R.id.switchFactorySeal);			
		    if (((CheckBox)v).isChecked()) { 
		    	clearAllChecks();
		    	item.selected = true;
		    	checkedCntr++;
		    	mDBArrayAdapter.notifyDataSetChanged();
		    } else { 
		    	item.selected = false;
		    	checkedCntr--;
		    }
		    factorySeal.setEnabled(checkedCntr == 1 && item.selected);
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    	invalidateOptionsMenu();
	    	}		
		}
	}
	
    private SpecsRecord findItem(long id) {
    	SpecsRecord result = null;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			SpecsRecord item = mDBArrayAdapter.getItem(i);
			if (item.id == id) {
				result = item;
				break;
			}
		}
		return result;
    }

    private SpecsRecord findFirstSelected() {
    	SpecsRecord result = null;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			SpecsRecord item = mDBArrayAdapter.getItem(i);
			if (item.selected) {
				result = item;
				break;
			}
		}
		return result;
    }
    
    
    private void clearAllChecks() {
    	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
    		SpecsRecord item = mDBArrayAdapter.getItem(i);
    		item.selected = false;
    	}
    	checkedCntr = 0;
    }
    
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		RelativeLayout parent = (RelativeLayout) buttonView.getParent();	    			
		TextView idTV = (TextView)parent.findViewById(R.id.lineId);
		long id = Long.parseLong(idTV.getText().toString());
		SpecsRecord item = findItem(id);
		if (item.selected) {
			if (isChecked)
				item.factorySeal = 1;
			else
				item.factorySeal = 0;	
			dbSch.updateSpecFactorySeal(item.id, item.factorySeal);
		}		
	}
	

}
