package com.utis.warrants;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import com.utis.warrants.record.EmpHardwareRecord;
import com.utis.warrants.tables.EmpHardwareTable;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.WarrRepairTable;

public class EmpHWSelectActivity extends Activity implements OnClickListener {
	private static final boolean D = true;
	private static final String TAG = "EmpHWSelectActivity";
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    private CustomEmpHWRepairAdapter mDBArrayAdapter;
    public Context mContext;
    private TextView aptionView, infoText;
    private EmpHardwareRecord repairPart;
    private static EmpHardwareRecord selectedRec = null; 
    private String jobId;
    private String warrUnitId, warrantId;
    private int checkedCntr;
    private boolean onlyView;

    private static class CustomEmpHWRepairAdapter extends ArrayAdapter<EmpHardwareRecord> {
    	private EmpHWSelectActivity ownerActivity;
    	private static EmpHardwareRecord curRec = null;
    	static class HWRepairViewHolder {
    		public TextView textId;
    	    public TextView textRepair;
    	    public TextView textRepairCntr;
    	    public CheckBox checkBox;
    	    public LinearLayout cntrButtons;
    	    public ImageButton upButton;
    	    public ImageButton downButton;
    	    public EditText cntrEdit;
    	}    	
    	    	
    	public CustomEmpHWRepairAdapter(Context context, int textViewResourceId) { 
    		//Call through to ArrayAdapter implementation 
    		super(context, textViewResourceId);
    		ownerActivity = (EmpHWSelectActivity) context;
    	} 
    	
    	public static void parseCntrInput(EditText editText, EmpHardwareRecord item, boolean focusChanged) {
    		Double val;
    		String input = editText.getText().toString();
    		input = input.replace(",", ".");
            if (input.length() != 0) {
            	try {
            		val = Double.parseDouble(input);
            	} catch (NumberFormatException e) {
            		val = 1.0;
            	}
            } else {
            	val = 0.0;
            }
            val = (val < 0) ? -val : val;
            if (val != item.selected_cntr && val > 0) {
            	item.selected_cntr = val;            	
            } else if (val == 0 && focusChanged) {
            	editText.setText(Double.toString(item.selected_cntr));
            }
    	}
    	
		private void updateRecord(EmpHardwareRecord item, HWRepairViewHolder viewHolder) {
    		viewHolder.textRepairCntr.setText(""+ item.cntr);
    		viewHolder.cntrEdit.setText(Double.toString(item.selected_cntr));
	    	viewHolder.upButton.setEnabled(item.cntr > 0);
	    	viewHolder.downButton.setEnabled(item.selected_cntr > 0);
		}
		
    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent) {
    		final HWRepairViewHolder viewHolder;
	    	View row = convertView; 
	    	//Inflate a new row if one isn't recycled
	    	if(row == null) { 
	    		row = LayoutInflater.from(getContext()).inflate(R.layout.emp_hw_row, parent, false); 
	    		viewHolder = new HWRepairViewHolder();
	    		viewHolder.textId = (TextView)row.findViewById(R.id.lineId);
	    		viewHolder.textRepair = (TextView)row.findViewById(R.id.lineRepair);
	    		viewHolder.textRepairCntr = (TextView)row.findViewById(R.id.lineRepairCntr);
	    		viewHolder.checkBox = (CheckBox) row.findViewById(R.id.checkBox);
	    		viewHolder.cntrButtons = (LinearLayout)row.findViewById(R.id.linearLayout1);
	    		viewHolder.upButton = (ImageButton)row.findViewById(R.id.imageButtonUp);
	    		viewHolder.downButton = (ImageButton)row.findViewById(R.id.imageButtonDown);
	    		viewHolder.cntrEdit = (EditText)row.findViewById(R.id.editTextCntr);
	    		row.setTag(viewHolder);
	    	} else {
	    		viewHolder = (HWRepairViewHolder) row.getTag();	    		
	    	}
	    	final EmpHardwareRecord item = getItem(position);
	    	viewHolder.textId.setText(""+ item.id);
	    	if (item.fnm != null)
	    		viewHolder.textRepair.setText(item.fnm);
	    	else 
	    		viewHolder.textRepair.setText(""+ item.id_gds);
	    	
//	    	if (GoodsRecord.GDS_PIECE == item.getGdsType())
//	    		viewHolder.textRepairCntr.setText(""+ String.format("%d", (int)item.cntr));
//	    	else
//	    		viewHolder.textRepairCntr.setText(""+ String.format("%.3f", item.cntr));	    	
	    	viewHolder.textRepairCntr.setText(""+ item.cntr);
	    	viewHolder.cntrButtons.setVisibility(item.selected ? View.VISIBLE : View.GONE);
	    	viewHolder.cntrEdit.setText(Double.toString(item.selected_cntr));
	    	viewHolder.upButton.setEnabled(item.cntr > 0);
	    	viewHolder.downButton.setEnabled(item.selected_cntr > 0);
	    	
	    	viewHolder.checkBox.setVisibility(ownerActivity.onlyView ? View.GONE : View.VISIBLE);
	    	viewHolder.checkBox.setChecked(item.selected);
	    	viewHolder.checkBox.setOnClickListener(ownerActivity);
/*	    	viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {	    	     
	    		public void onClick(View v) {
	    			EmpHardwareRecord item = getItem(position);
	    		    if (((CheckBox)v).isChecked()) { 
	    		    	item.selected = true;
	    		    	ownerActivity.checkedCntr++;
	    		    	viewHolder.cntrButtons.setVisibility(View.VISIBLE);
	    		    	if (item.selected_cntr == 0) {
	    		    		if (item.avail_cntr >= 1) {
	        		    		item.selected_cntr += 1;
	        		    		item.cntr -= 1;	    		    			
	    		    		} else {
	        		    		item.selected_cntr += item.cntr;
	        		    		item.cntr = 0;	    		    				    		    			
	    		    		}
	    		    	}
	    		    } else { 
	    		    	item.selected = false;
	    		    	ownerActivity.checkedCntr--;
	    		    	viewHolder.cntrButtons.setVisibility(View.GONE);
	    		    }
//	    		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//	    		    	ownerActivity.invalidateOptionsMenu();
//    		    	}
    		    }
	    	});*/
	    	viewHolder.upButton.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					if (ownerActivity.selectedRec != null) {
	    		    	if (ownerActivity.selectedRec.cntr >= 1) {
	    		    		ownerActivity.selectedRec.selected_cntr += 1;
	    		    		ownerActivity.selectedRec.cntr -= 1;
	    		    		updateRecord(ownerActivity.selectedRec, viewHolder);
	    		    	}					
					}
				}
			});
	    	viewHolder.downButton.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					if (ownerActivity.selectedRec != null) {
	    		    	if (ownerActivity.selectedRec.selected_cntr >= 1) {	    		    		
	    		    		ownerActivity.selectedRec.cntr += 1;
	    		    		ownerActivity.selectedRec.selected_cntr -= 1;
	    		    	} else if (ownerActivity.selectedRec.selected_cntr > 0) {	    		    		
	    		    		ownerActivity.selectedRec.cntr += ownerActivity.selectedRec.selected_cntr;
	    		    		ownerActivity.selectedRec.selected_cntr = 0;
	    		    	}		
			    		updateRecord(ownerActivity.selectedRec, viewHolder);
					}
				}
			});
	    	viewHolder.cntrEdit.setOnEditorActionListener(new OnEditorActionListener() {
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
	                	parseCntrInput((EditText) v, item, false);
	                }
					return false;
	            }
/*            
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                String input;
	                Double val;
	                if (actionId == EditorInfo.IME_ACTION_DONE) {
	                    input = v.getText().toString();
	                    val = Double.parseDouble(input);
	                    val = (val < 0) ? -val : val;
	                    if (val != item.selected_cntr) {
		                    if (val <= item.avail_cntr) {
		                    	item.cntr = item.avail_cntr - val;
		                    	item.selected_cntr = val;
		                    } else {
		                    	item.selected_cntr = item.avail_cntr;
		                    	item.cntr = 0;
		                    }
	    		    		updateRecord(item, viewHolder);
	                    }
	                }
					return false;
	            }
	        });
	    	viewHolder.cntrEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	            @Override
	            public void onFocusChange(View v, boolean hasFocus) {
	                String input;
	                EditText editText;
	                Double val;
	                if (!hasFocus) {
	                    editText = (EditText) v;
	                    input = editText.getText().toString();
	                    val = Double.parseDouble(input);
	                    val = (val < 0) ? -val : val;
	                    if (val != item.selected_cntr) {
		                    if (val <= item.avail_cntr) {
		                    	item.cntr = item.avail_cntr - val;
		                    	item.selected_cntr = val;
		                    } else {
		                    	item.selected_cntr = item.avail_cntr;
		                    	item.cntr = 0;
		                    }
	    		    		updateRecord(item, viewHolder);
	                    }
	                }
	            }*/
	        });	    	
	    	
	    	return row; 
    	} 
    }    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emp_hwselect);		
		mContext = this;
		aptionView = (TextView)findViewById(R.id.title_text);
		infoText = (TextView)findViewById(R.id.info_text);
		mDBArrayAdapter = new CustomEmpHWRepairAdapter(this, R.layout.emp_hw_row);
        mDBListView = (ListView) findViewById(R.id.listViewHardware);
        mDBListView.setAdapter(mDBArrayAdapter);
        mDBListView.setOnItemClickListener(mHWClickListener);	
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
        
		Bundle b = getIntent().getExtras();
		warrUnitId = b.getString("id");
		warrantId = b.getString("warrId");
		onlyView = warrantId.length() == 0;
		aptionView.setVisibility(onlyView ? View.GONE : View.VISIBLE);
	}

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        showEmpHW(dbSch);
    } 
    	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emp_hwselect, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		boolean enabled = warrUnitId.length() > 0;
		MenuItem mi = menu.findItem(R.id.action_select_repairs);		
		if (mi != null) {
			mi.setEnabled(enabled);
			mi.setVisible(enabled);
		}
	    return true;
	}
		
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		Bundle b;
        switch (item.getItemId()) {
        case R.id.action_select_repairs:
    		addSelectedHW();
    		Intent returnIntent = new Intent();
    		setResult(RESULT_OK, returnIntent);
    		finish();        	
        	return true;
        case R.id.action_show_seals:
        	intent = new Intent(this, SealViewActivity.class);
    		b = new Bundle();
    		b.putString("id", "");
    		b.putString("warrContId", "");
    		b.putString("mode", Integer.toString(SealViewActivity.SEAL_VIEW_ONLY));
    		intent.putExtras(b); 
            startActivity(intent);
            return true;
        case R.id.action_show_seal_cert:
        	intent = new Intent(this, SealCertListActivity.class);
    		b = new Bundle();
    		b.putString("id_specs", "");
    		b.putString("warrContId", "");
//    		b.putString("mode", Integer.toString(SealViewActivity.SEAL_VIEW_ONLY));
    		intent.putExtras(b); 
            startActivity(intent);
            return true;
        }
        return false;
	}	
	

    // The on-click listener for ListViews
    private OnItemClickListener mHWClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	repairPart = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + repairPart.id_gds);
            if (onlyView) finish();
//            Intent returnIntent = new Intent();
//			Bundle b = new Bundle();
//			b.putString("id", Integer.toString(repairPart.id_gds));//Your id
//			b.putString("cntr", "1");
//			returnIntent.putExtras(b); //Put your id to your Intent
//            setResult(RESULT_OK, returnIntent);       
//            finish();            
        }
    };    
    
	@Override
	public void onClick(View v) {
		if (v instanceof CheckBox) { 
			RelativeLayout parent = (RelativeLayout) v.getParent();	    			
			TextView idTV = (TextView)parent.findViewById(R.id.lineId);
			long id = Long.parseLong(idTV.getText().toString());
			selectedRec = findItem(id);
		    if (((CheckBox)v).isChecked()) { 
//		    	clearAllChecks();
		    	selectedRec.selected = true;
		    	if (selectedRec.selected_cntr == 0) {
		    		if (selectedRec.avail_cntr >= 1) {
		    			selectedRec.selected_cntr += 1;
		    			selectedRec.cntr -= 1;	    		    			
		    		} else {
		    			selectedRec.selected_cntr += selectedRec.cntr;
		    			selectedRec.cntr = 0;	    		    				    		    			
		    		}
		    	}
		    	checkedCntr++;		    	
		    } else { 
		    	selectedRec.selected = false;		    	
		    	selectedRec.cntr += selectedRec.selected_cntr;
		    	selectedRec.selected_cntr = 0;
		    	checkedCntr--;
		    }
		    mDBArrayAdapter.notifyDataSetChanged();
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    	invalidateOptionsMenu();
	    	}		
		} else {
	    	switch (v.getId()) {
	       	}
		}
	}	
	
    private EmpHardwareRecord findItem(long id) {
    	EmpHardwareRecord result = null;
		for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
			EmpHardwareRecord item = mDBArrayAdapter.getItem(i);
			if (item.id == id) {
				result = item;
				break;
			}
		}
		return result;
    }
    
    private void clearAllChecks() {
    	for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
    		EmpHardwareRecord item = mDBArrayAdapter.getItem(i);
    		item.selected = false;
    	}
    	checkedCntr = 0;
    	//selectedRec = null;
    }
	
    
	private void addSelectedHW() {
		EmpHardwareRecord repairPart;
		for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
			repairPart = mDBArrayAdapter.getItem(i);
			if (repairPart.selected && repairPart.selected_cntr > 0) {
                dbSch.addWarrRepairItem(0, Integer.parseInt(warrantId), 
                	Integer.parseInt(warrUnitId), repairPart.id_gds, 2, 0, repairPart.selected_cntr);				
			}
		}
	}
    
    private void showEmpHW(DBSchemaHelper sh) {
    	String q_nm = "(SELECT "+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
    			" WHERE "+ GoodsTable.ID_EXTERNAL +" = "+ EmpHardwareTable.ID_GDS +")as "+ EmpHardwareRecord.NM;
    	String q_cntr = "(SELECT SUM("+ WarrRepairTable.QUANT +") FROM "+ WarrRepairTable.TABLE_NAME +
    			" WHERE "+ WarrRepairTable.ID_REPAIR + " = "+ EmpHardwareTable.ID_GDS +
    			" AND "+ WarrRepairTable.REC_STAT + "<> 2)as "+ EmpHardwareRecord.SPENT_CNTR;
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null; 
    	String query;
    	
    	query = "SELECT *,"+ q_nm +", "+ q_cntr + " from " + EmpHardwareTable.TABLE_NAME + 
				" ORDER BY " + EmpHardwareTable.ID_GDS;    	
    	int cntr = 0; 		
		try {
			mDBArrayAdapter.clear();
			c = sqdb.rawQuery(query, null);
	    	while (c.moveToNext()) {    		
	    		repairPart = new EmpHardwareRecord(c);    		
	    		if (D) Log.d(TAG, "Repair = " + repairPart.id_gds);
	    		if (repairPart.cntr > 0 && repairPart.id_gds != EmpHardwareRecord.SEAL_ID) {
	    			cntr++;
	    			mDBArrayAdapter.add(repairPart);
	    		}
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
	    	if (D) Log.d(TAG, "repairParts Count = " + cntr);
	    	infoText.setText(getString(R.string.hw_cntr) + cntr);
	    	if (c != null) c.close();
		}
    }
    
}
