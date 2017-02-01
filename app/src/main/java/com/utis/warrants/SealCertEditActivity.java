package com.utis.warrants;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;

import com.utis.warrants.record.ReasonRecord;
import com.utis.warrants.record.SealCertRecord;
import com.utis.warrants.record.SealCertSealsRecord;
import com.utis.warrants.tables.ReasonTable;
import com.utis.warrants.tables.SealCertSealsTable;
import com.utis.warrants.tables.SealCertTable;

public class SealCertEditActivity extends FragmentActivity {
	private static final boolean D = true;
	private static final String TAG = "SealCertEditActivity";
	public static final String KEY_DATE_BEGIN = "sealCertDateBegin";
	public static final String KEY_DATE_END = "sealCertDateEnd";
	private static final int BROKEN_SEAL_MODE = 1;
	private static final int SET_SEAL_MODE = 2;
	private static final int SELECT_SEAL = 3;
	private DBSchemaHelper dbSch;
    private ListView mDBListView;    
    public Context mContext;
    private ArrayAdapter<ReasonRecord> mReasonDBArrayAdapter;
//    private ArrayAdapter<SealCertSealsRecord> mSealDBArrayAdapter;
    private CustomSealCertSealsAdapter mSealDBArrayAdapter;
    private TextView certReasonText, sealBrokenDateText, sealSetDateText, numEdit, dateEdit, sealsText;
    private EditText defectEdit, ppoMoneyEdit;
    private CheckBox factorySealChkBox;
    private Spinner reasonSpinner; 
    private Button sealsButton;
    private int idReason, savedReason; 
    private Calendar calendar;
    private Date mDate, sealBrokenDate, sealSetDate;
    private int dateMode;
    private String warrContId, sealCertId, idSpecs;
    private SealCertRecord mSealCert;
	private int mSeal, mReason;
	private String mDateCh, mDateBegin, mDateEnd, mDefect, mNum, savedDateBegin, savedDateEnd;
	private boolean mFactorySeal; 
	private double mMoney;
	private SharedPreferences prefs;
    

    private static class CustomSealCertSealsAdapter extends ArrayAdapter<SealCertSealsRecord> {
    	private SealCertEditActivity ownerActivity;
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
    		ownerActivity = (SealCertEditActivity) context;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seal_cert_edit);		
		mContext = this;
		dbSch = DBSchemaHelper.getInstance(this);
		certReasonText = (TextView) findViewById(R.id.textViewReason);
		sealBrokenDateText = (TextView) findViewById(R.id.textViewSealBrokenDate);
		sealSetDateText = (TextView) findViewById(R.id.textViewSealSetDate);
		numEdit = (TextView) findViewById(R.id.editTextNum); 
		dateEdit = (TextView) findViewById(R.id.editTextDate);
		defectEdit = (EditText) findViewById(R.id.editTextDefect);
		factorySealChkBox = (CheckBox) findViewById(R.id.chkBoxFactorySeal);
		ppoMoneyEdit = (EditText) findViewById(R.id.editTextPPOMoney);
		sealsText = (TextView) findViewById(R.id.textViewSeals);
		sealsButton = (Button) findViewById(R.id.buttonSeals);
		sealsButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				selectSeals(SealViewActivity.SEAL_VIEW_DELETE_MODE);				
			}
		});
		
//		mSealDBArrayAdapter = new ArrayAdapter<SealCertSealsRecord>(this, android.R.layout.simple_list_item_1);
//		mSealDBArrayAdapter = new CustomSealCertSealsAdapter(this, R.layout.emp_seal_row);
//		mDBListView = (ListView) findViewById(R.id.listViewSeals);
//		mDBListView.setAdapter(mSealDBArrayAdapter);
//	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//		    mDBListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); 
//		    mDBListView.setMultiChoiceModeListener(new ModeCallback()); 
//	    }
		
		reasonSpinner = (Spinner) findViewById(R.id.spinnerReason);
		reasonSpinner.setOnItemSelectedListener(mReasonClickListener);
		mReasonDBArrayAdapter = new ArrayAdapter<ReasonRecord>(this, android.R.layout.simple_spinner_item);
		mReasonDBArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		reasonSpinner.setAdapter(mReasonDBArrayAdapter);
		
		Bundle b = getIntent().getExtras();
		sealCertId = b.getString("Id");
		warrContId = b.getString("warrContId");
		idSpecs = b.getString("id_specs");
		
		prefs = getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
		getReasons(); 		
		calendar = Calendar.getInstance();
//		updateCertDate();			
	}
	
    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");        
        sealsButton.setEnabled(sealCertId.length() > 0);        
        
		showSealCert(dbSch);
        Calendar c = Calendar.getInstance();
        savedDateBegin = prefs.getString(KEY_DATE_BEGIN, "");
		if (savedDateBegin.length() > 0) {
			try {
				sealBrokenDate = dbSch.dateFormatMName.parse(savedDateBegin);
				sealBrokenDateText.setText(savedDateBegin);
			} catch (Exception e) {
				sealBrokenDateText.setText("");
			}
		} else {
			if (sealCertId.length() == 0) {
				sealBrokenDate = c.getTime();
				sealBrokenDateText.setText(dbSch.dateFormatMName.format(sealBrokenDate));
			}
		}
		savedDateEnd = prefs.getString(KEY_DATE_END, "");
		if (savedDateEnd.length() > 0) {
			try {
				sealSetDate = dbSch.dateFormatMName.parse(savedDateEnd);
				sealSetDateText.setText(savedDateEnd);
			} catch (Exception e) {
				sealSetDateText.setText("");
			}
		} else {
			if (sealCertId.length() == 0) {
				sealSetDate = c.getTime();
				sealSetDateText.setText(dbSch.dateFormatMName.format(sealSetDate));
			}
		}        
    } 
	   
    @Override 
    public void onPause() { 
    	super.onPause(); 
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putString(KEY_DATE_END, sealSetDateText.getText().toString());
    	editor.putString(KEY_DATE_BEGIN, sealBrokenDateText.getText().toString());
//    	savedWH = mDBArrayAdapter.getPosition((PlacementRecord) WareHouseSpinner.getSelectedItem());
    	editor.commit();					
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.seal_cert_edit, menu);			
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_select_seals);
		if (mi != null) {
			mi.setEnabled(sealCertId.length() != 0);
			mi.setVisible(sealCertId.length() != 0);
		}
	    return true;
	}
		
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_save_cert:
        	attemptSave();
            return true;
        case R.id.action_select_seals:
        	selectSeals(SealViewActivity.SEAL_VIEW_ADD_MODE);
        	return true;
        }
        return false;
    }	
	
    private OnItemSelectedListener mReasonClickListener = new OnItemSelectedListener() {
        @Override
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	ReasonRecord reasonSelected = (ReasonRecord) reasonSpinner.getSelectedItem();
        	idReason = reasonSelected.idExternal;
        	savedReason = position;
        	certReasonText.setText(reasonSelected.toString());
        }

        @Override
    	public void onNothingSelected(AdapterView<?> parent) {}
    };
    
	private void getReasons() {
    	String query = "SELECT * FROM " + ReasonTable.TABLE_NAME + " ORDER BY " + ReasonTable.ID_EXTERNAL;
    	Cursor c = null;
    	ReasonRecord reason;
    	SQLiteDatabase sqdb = dbSch.getWritableDatabase();
		try {
			c = sqdb.rawQuery(query, null);
			mReasonDBArrayAdapter.clear();
	    	while (c.moveToNext()) {
	    		reason = new ReasonRecord(c);
	    		mReasonDBArrayAdapter.add(reason);
	    	}    	    	
		} catch(Exception e) {
	        if (D) Log.e(TAG, "Exception: " + e.getMessage());
	    } finally {
	    	if (c != null) c.close();
		}		
	}
	
	private void selectSeals(int mode) {
    	Intent intent = new Intent(this, SealViewActivity.class);
		Bundle b = new Bundle();
		b.putString("id", sealCertId);
		b.putString("warrContId", warrContId);
		b.putString("mode", Integer.toString(mode));
		intent.putExtras(b); 
        startActivityForResult(intent, SELECT_SEAL);		
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case SELECT_SEAL:
//    			ShowSealCertSeals(dbSch);
    			getSealCertSeals(dbSch);
    			break;
    		}
        }
    }   	
	
	private void attemptSave() {
		boolean cancel = false;
		View focusView = null;
		// Reset errors.
		sealBrokenDateText.setError(null);
		sealSetDateText.setError(null);
		defectEdit.setError(null);
		ppoMoneyEdit.setError(null);
		if (!sealBrokenDate.before(sealSetDate)) {
			focusView = sealSetDateText;
			sealSetDateText.setError(getString(R.string.error_date_seal));
			cancel = true;
		} else if (defectEdit.length() == 0) {
			focusView = defectEdit;
			defectEdit.setError(getString(R.string.error_no_defect_info));
			cancel = true;			
		} else if (ppoMoneyEdit.length() == 0) {
			focusView = ppoMoneyEdit;
			ppoMoneyEdit.setError(getString(R.string.error_no_money_info));
			cancel = true;			
		}
		if (cancel) {			
			focusView.requestFocus();
		} else {
			int seal = (factorySealChkBox.isChecked()) ? 1 : 0;
			Date certDate = Calendar.getInstance().getTime();
			String dateCh = dbSch.dateFormatYYMM.format(certDate); 
			String dateBegin = dbSch.dateFormatYYMM.format(sealBrokenDate);
			String dateEnd = dbSch.dateFormatYYMM.format(sealSetDate);
			double ppoMoney = Double.parseDouble(ppoMoneyEdit.getText().toString());
			if (sealCertId.length() == 0) {
				sealCertId = Long.toString(dbSch.addSealCertItem(Long.parseLong(idSpecs), idReason, 
					Integer.parseInt(warrContId), seal, defectEdit.getText().toString(), 
					dateCh, dateBegin, dateEnd, ppoMoney));
				numEdit.setText(sealCertId);
				dateEdit.setText(dbSch.dateFormatMM.format(certDate));
			} else {
				dbSch.updateSealCertItem(Long.parseLong(sealCertId), idReason, Integer.parseInt(warrContId), 
						seal, defectEdit.getText().toString(), dateCh, dateBegin, dateEnd, ppoMoney);
			}
			showSealCert(dbSch);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				invalidateOptionsMenu();
		}
	}

	public void selectTime() {
		DialogFragment newFragment = new SelectTimeFragment();
		newFragment.show(getSupportFragmentManager(), "TimePicker");
	}
	
	public void selectDate() {
		DialogFragment newFragment = new SelectDateFragment();
		newFragment.show(getSupportFragmentManager(), "DatePicker");
	}
	
	public void selectTimeBroken(View view) {
		dateMode = BROKEN_SEAL_MODE;
		calendar.setTime(sealBrokenDate);
		selectTime();
	}
	
	public void selectDateBroken(View view) {
		dateMode = BROKEN_SEAL_MODE;
		calendar.setTime(sealBrokenDate);
		selectDate();
	}
	
	public void selectTimeSet(View view) {
		dateMode = SET_SEAL_MODE;
		calendar.setTime(sealSetDate);
		selectTime();
	}
	
	public void selectDateSet(View view) {
		dateMode = SET_SEAL_MODE;
		calendar.setTime(sealSetDate);
		selectDate();
	}
	
    private void updateCertDate() {
		mDate = calendar.getTime();
		switch (dateMode) {
		case BROKEN_SEAL_MODE:
			sealBrokenDateText.setText(dbSch.dateFormatMName.format(mDate));
			sealBrokenDate = mDate;
			break;
		case SET_SEAL_MODE:
			sealSetDateText.setText(dbSch.dateFormatMName.format(mDate));
			sealSetDate = mDate;
			break;
		default:
			sealBrokenDateText.setText(dbSch.dateFormatMName.format(mDate));
			sealSetDateText.setText(dbSch.dateFormatMName.format(mDate));
			sealBrokenDate = mDate;
			sealSetDate = mDate;
		}				
    }
    
	public void populateSetTime(int hour, int minute) {
		int yy = calendar.get(Calendar.YEAR);
		int mm = calendar.get(Calendar.MONTH);
		int dd = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(yy, mm, dd, hour, minute);
		updateCertDate();
	}
	
	public void populateSetDate(int year, int month, int day) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);			
		calendar.set(year, month, day, hour, minute);
		updateCertDate();
	}
	
	@SuppressLint("ValidFragment")
	public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int yy = calendar.get(Calendar.YEAR);
			int mm = calendar.get(Calendar.MONTH);
			int dd = calendar.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(getActivity(), this, yy, mm, dd);
		}
		 
		public void onDateSet(DatePicker view, int yy, int mm, int dd) {
			populateSetDate(yy, mm/*+1*/, dd);
		}

	}	
	
	@SuppressLint("ValidFragment")
	public class SelectTimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);			
			return new TimePickerDialog(getActivity(), this, hour, minute, true);
		}
		 
		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {
			populateSetTime(hour, minute);
		}

	}	
	
    private void showSealCert(DBSchemaHelper sh) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null; 
    	String query;   	
    	if (sealCertId.length() > 0) {
	    	query = "SELECT * FROM " + SealCertTable.TABLE_NAME + " WHERE " + SealCertTable.ID + " = " + sealCertId;
			try {
				c = sqdb.rawQuery(query, null);
		    	while (c.moveToNext()) {    		
		    		mSealCert = new SealCertRecord(c);    		
		    		if (D) Log.d(TAG, "seal = " + mSealCert.toString());
		    		mSeal = mSealCert.factorySeal;
		    		mNum = mSealCert.getNum();
		    		mDateCh = mSealCert.dateChangeFmt;
		    		mDateBegin = mSealCert.dateBeginFmt; 
		    		mDateEnd = mSealCert.dateEndFmt;
		    		mMoney = mSealCert.ppoMoney;
		    		mDefect = mSealCert.defect;
		    		mReason = mSealCert.idReason;
		    		sealBrokenDate = mSealCert.beginDate;
		    		sealSetDate = mSealCert.endDate;
		    		mFactorySeal = mSealCert.factorySeal > 0 ? true : false;
		    		reasonSpinner.setSelection(findReasonPosition(mReason));
		    		numEdit.setText(mNum);
		    		dateEdit.setText(mDateCh);
		    		sealBrokenDateText.setText(mDateBegin);
		    		sealSetDateText.setText(mDateEnd);
		    		defectEdit.setText(mDefect);
		    		ppoMoneyEdit.setText(Double.toString(mMoney));
		    		factorySealChkBox.setChecked(mFactorySeal);
		    		getSealCertSeals(dbSch);
	//	    		ShowSealCertSeals(dbSch);
		    	}    
			} catch (Exception e) {
				if (D) Log.e(TAG, "Exception: " + e.getMessage());
			} finally {
		    	if (c != null) c.close();
			}
    	}
    }
    
    private void getSealCertSeals(DBSchemaHelper sh) {
    	sealsText.setText(dbSch.getSealCertSeals(Long.parseLong(sealCertId)));
    }
    
    private void showSealCertSeals(DBSchemaHelper sh) {
    	SQLiteDatabase sqdb = sh.getWritableDatabase();
    	Cursor c = null; 
    	String query;   	
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
	    	if (c != null) c.close();
		}
    }
    
    private int findReasonPosition(int reasonId) {
    	int res = 0;
    	for (int i = 0; i < mReasonDBArrayAdapter.getCount(); i++) {
    		ReasonRecord item = mReasonDBArrayAdapter.getItem(i);
    		if (item.idExternal == reasonId) {
    			res = i;
    			break;
    		}
    	}
    	return res;
    }
	
	private class ModeCallback implements ListView.MultiChoiceModeListener {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.seal_cert_seals_rowselection, menu);
            mode.setTitle(mContext.getString(R.string.selected));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    		MenuItem mi = menu.findItem(R.id.action_remove_seals);
//    		if (mi != null) { 
//    			mi.setEnabled(!onlyView);
//    		}
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_remove_seals:
            	deleteSelected();
                mode.finish();
                return true;
            default:
                return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
        	mSealDBArrayAdapter.clearSelection();
        }

        public void onItemCheckedStateChanged(ActionMode mode,
                int position, long id, boolean checked) {
            final int checkedCount = mDBListView.getCheckedItemCount();
            if (checked) 
            	mSealDBArrayAdapter.setNewSelection(position, checked);                    
            else 
            	mSealDBArrayAdapter.removeSelection(position);                 
            
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
        	for (int i = mSealDBArrayAdapter.getCount() - 1; i >= 0; i--) {
        		if (mSealDBArrayAdapter.isPositionChecked(i)) {
        			SealCertSealsRecord item = mSealDBArrayAdapter.getItem(i);
        	    	dbSch.removeSealCertSeal(item.idSeals);
        	    	mSealDBArrayAdapter.remove(item);
        		}
        	}
        	showSealCertSeals(dbSch);
//        	mDBArrayAdapter.clearSelection();
//        	mDBArrayAdapter.notifyDataSetChanged();
//        	invalidateOptionsMenu();
//        	showCounter(mDBArrayAdapter.getCount());
        }        
        
        
    }
	
    
    
}
