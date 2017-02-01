package com.utis.warrants;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.integration.android.IntentIntegratorSupportV4;
import com.utis.warrants.RestTask.ResponseCallback;
import com.utis.warrants.YesNoDialogFragment.YesNoDialogListener;
import com.utis.warrants.location.LocationActivity;
import com.utis.warrants.tables.EmpTable;
import com.utis.warrants.tables.EntTable;
import com.utis.warrants.tables.GoodsTable;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.ProgressDialog;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.View.OnClickListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class WarrListActivity extends FragmentActivity implements OnClickListener, 
	ResponseCallback, YesNoDialogListener {
	
	/* Setting this is effectively shorthand for setting explicit formats with {@link #FORMATS}.
	 * It is overridden by that setting.
	 */
	public static final String MODE = "SCAN_MODE";
	/**
	 * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get
	 * prices, reviews, etc. for products.
	 */
	public static final String PRODUCT_MODE = "PRODUCT_MODE";
	/**
	 * Decode only 1D barcodes.
	 */
	public static final String ONE_D_MODE = "ONE_D_MODE";
	/**
	 * Decode only QR codes.
	 */
	public static final String QR_CODE_MODE = "QR_CODE_MODE";
	/**
	 * Decode only Data Matrix codes.
	 */
	public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";
	/**
	 * Comma-separated list of formats to scan for. The values must match the names of
	 * {@link com.google.zxing.BarcodeFormat}s, e.g. {@link com.google.zxing.BarcodeFormat#EAN_13}.
	 * Example: "EAN_13,EAN_8,QR_CODE"
	 *
	 * This overrides {@link #MODE}.
	 */
	
	private static final boolean D = true; 
	private static final String TAG = "WarrListActivity";
//	public static final String SERVER_URI_SSL =
//			"https://192.168.2.238:8443";
	public static final String SERVER_URI_SSL =
			"https://85.238.112.13:443/contenti";
	public static final String LOGIN_IMEI_URI =
			"/Ol2/i_login.php";
	public static final String LOGIN_PSW_URI =
			"/Ol2/a_login.php";
	private static final String SEARCH_URI =
			"http://192.168.2.238:8080/Ol2/warrant_list.php";
//	private static final String SEARCH_URI_SSL =
//			"https://192.168.2.238:8443/Ol2/warrant_list.php";
//	private static final String SEARCH_URI_SSL =
//			"https://192.168.2.238:8443/Ol2/warr_list_online.php";
	private static final String REST_WARRANTS_URI =
			"/Ol2/warr_list_online.php";
	public static final int AUTH_IMEI = 1;
	public static final int AUTH_PSW = 2;
	public static final String MOBILE = "+mobile";
	private static final String DEBUG_USER_NAME = "CONTENT_86";
	private static final int DIALOG_GET_WARRANTS = 1;
	private static final int DIALOG_CLEAR_WARRANTS = 2;
	private static final int SCAN_BARCODE = 3;
	private static final String ENT = "/ent";
	private static final String GDS = "/gds";
	private static final String WARR_CONT = "/warrant/";
	private static final String WARRANTS = "/warrants";
	private static final String EMP_HW = "/hw/538";
	private static final String EMPS = "/emps/538";
	private static final String USR = "/usr";
	private static final String MSGS = "/msgs";
	private static final String EMPWORKTIME = "/worktime";
	private static final String EMPLOCATION = "/emp_locations";
	private static final String STATE = "/state";
	// Constants that indicate the current request state
    public static final int STATE_FINISH = 0; // process finishes
    public static final int STATE_GET_WARR = 1; // REST without params
    public static final int STATE_GET_ENT = 2;  // REST ent
    public static final int STATE_GET_GDS = 3;  // REST gds
    public static final int STATE_GET_STATE = 4; // REST get d_state
    public static final int STATE_GET_EMPS = 5; // REST get t_emp
    public static final int STATE_GET_USER = 6; // REST get t_emp
    public static final int STATE_GET_WARR_CONT = 7;  // REST warrant
    public static final int STATE_GET_WARRANTS = 20; // REST get all warrants data
    public static final int STATE_GET_EMP_HW = 21; // REST get employee hardware
    public static final int STATE_GET_MSGS = 30; // REST get d_state
    public static final int STATE_POST_WARRANTS = 100; // REST post all warrants data
    public static final int STATE_POST_MSGS = 101; // REST post messages
    public static final int STATE_POST_EMP_WORK_TIME = 102; // REST post emp_work_time
    public static final int STATE_POST_EMP_LOCATIONS = 103; // REST post emp_locations
    private String wId;
    private int dialogMode;
    private boolean warrantsModified;
	private int[] taskArray = new int[4];
	private IntentIntegratorSupportV4 iss = null; 
			 
	//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems = new ArrayList<String>();
    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    ArrayAdapter<String> adapter;
    
    private TelephonyManager tm;
    private String telephonyInfo = "";
	private TextView mResult;
	private ListView mWarrantList; 
	private ProgressDialog mProgress;
	private ProgressDialog mProgressDialog;
	public Context mContext;
	private DBSchemaHelper dbSch;
	private TextView Caption;
	private TextView getData;
	private String resultString ="";
	private View getButton;
	private View getConstButton;
	private View clearWarrantsButton;
	private View clearConstButton;
    private int text_string = R.string.caption_get_data; 
    private int background_color = Color.DKGRAY;
    private String currentServerURI;
    private String authUserName;
    private String authUserPsw;
	
	final Handler mHandler = new Handler(); 

	// Create runnable for posting results to the UI thread 
	final Runnable mUpdateResults = new Runnable() {
		public void run() { 
			Caption.setText(text_string); 
			Caption.setBackgroundColor(background_color);
		} 
	};
	
	int mState = STATE_FINISH;
	
    protected MyGetState myState = new MyGetState();
	public class MyGetState{
	  protected int mGetState = STATE_FINISH;

	  public synchronized int getState(){
	    return this.mGetState;
	  }

	  public synchronized void setState(int newState){
	    this.mGetState = newState;  
	  }
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warr_list);
		
		mContext = this;
		Caption = (TextView)findViewById(R.id.title_text);
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
//		iss = new IntentIntegratorSupportV4(this);
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		getTelephonyInfo(false);
		
        getButton = findViewById(R.id.get_button);
        getButton.setOnClickListener(this);
        getConstButton = findViewById(R.id.get_const_button);
        getConstButton.setOnClickListener(this);
        clearWarrantsButton = findViewById(R.id.w_clear_button);
        clearWarrantsButton.setOnClickListener(this);
        clearConstButton = findViewById(R.id.clear_const_button);
        clearConstButton.setOnClickListener(this);
		mResult = (TextView) findViewById(R.id.textViewWarr);
		mWarrantList = (ListView) findViewById(R.id.listViewWarr);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
//		mWarrantList.setListAdapter(adapter);
		mWarrantList.setAdapter(adapter);
		mWarrantList.setOnItemClickListener(mRecordsClickListener);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("Обработка данных...");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		currentServerURI = SERVER_URI_SSL + REST_WARRANTS_URI;//  SEARCH_URI_SSL; // SEARCH_URI;
		getData = (TextView) findViewById(R.id.textGetWarr);
		getData.setText(getText(R.string.caption_get_data) +";\n "+ currentServerURI);
		Bundle b = getIntent().getExtras();
		if (b != null) {
			authUserName = b.getString("usr");
			authUserPsw = b.getString("psw");			
		}
		mState = STATE_FINISH;
	}

	private void getTelephonyInfo(boolean showToast) {
		//---get the SIM card ID---
		String simID = tm.getSimSerialNumber();
		if (simID != null) {
			if (showToast) Toast.makeText(this, "SIM card ID: " + simID, Toast.LENGTH_LONG).show();
			telephonyInfo = "SIM card ID: " + simID;
		}
		
		//---get the phone number---
		String telNumber = tm.getLine1Number();
		if (telNumber != null) {
			if (showToast) Toast.makeText(this, "Phone number: " + telNumber, Toast.LENGTH_LONG).show();
			telephonyInfo += "\nPhone number: " + telNumber;
		}
		
		//---get the IMEI number---
		String IMEI = tm.getDeviceId();
		if (IMEI != null) { 
			if (showToast) Toast.makeText(this, "IMEI number: " + IMEI, Toast.LENGTH_LONG).show();
			telephonyInfo += "\nIMEI number: " + IMEI;
		}
	}
	
	private void sendNextTask() {
    	int task = getNextTask();
		switch (task) {
		case STATE_FINISH:
			break;
		case STATE_POST_WARRANTS:
		case STATE_POST_MSGS:
		case STATE_POST_EMP_WORK_TIME:	
		case STATE_POST_EMP_LOCATIONS:	
			postStateRequest(task);
			break;
		default:
    		sendRequest(task);
		}
	}

	private void getConstTables(boolean forced) {
		if (mState == STATE_FINISH) {	
			prepareGetConstTask(false);
			sendNextTask();
		}
	}
	
	private void getWarrantTables() {
		if (mState == STATE_FINISH) {	
			prepareGetWarrantsTask();
			sendNextTask();
		}
	}
	
	private void getMessagesTables() {
		if (mState == STATE_FINISH) {	
			prepareGetMessagesTask();
			sendNextTask();
		}
	}
	
	private void postWarrantTables() {
		if (mState == STATE_FINISH) {	
			preparePostWarrantsTask();
			sendNextTask();
		}
	}
	
	private void postMessageTable() {
		if (mState == STATE_FINISH) {	
			preparePostMessageTask();
			sendNextTask();
		}		
	}
	
	private void postEmpWorkTimeTable() {
		if (mState == STATE_FINISH) {	
			preparePostEmpWorkTimeTask();
			sendNextTask();
		}		
	}
	
	private void postEmpLocationsTable() {
		if (mState == STATE_FINISH) {	
			preparePostEmpLocationsTask();
			sendNextTask();
		}		
	}
	
	@Override
	public void onClick(View v) { 
    	switch (v.getId()) {
    	case R.id.get_button:
    		if (D) Log.d(TAG, "clicked on get_button");
    		warrantsModified = dbSch.isWarrantsModified();
    		if (warrantsModified) {
    			showYesNoDialog(DIALOG_GET_WARRANTS, getString(R.string.m_w_modified) +
    					getString(R.string.m_continue));
    		} else {
    			getWarrantTables();
    		}

    		break;
    	case R.id.get_const_button:
    		if (D) Log.d(TAG, "clicked on get_const_button");
    		getConstTables(false);
    		break;
    	case R.id.w_clear_button:
    		if (D) Log.d(TAG, "clicked on w_clear_button");
    		warrantsModified = dbSch.isWarrantsModified();
    		if (warrantsModified) {
    			showYesNoDialog(DIALOG_CLEAR_WARRANTS, getString(R.string.m_w_modified) +
    					getString(R.string.m_continue));
    		} else {
    			dbSch.emptyAllWarrantTables();
    			updateCounters("");
    		}	
    		break;
    	case R.id.clear_const_button:
    		if (D) Log.d(TAG, "clicked on clear_const_button");
    		dbSch.emptyAllConstTables();
    		updateCounters("");
    		break;
       	}		
	}
	
    // The on-click listener for ListViews
    private OnItemClickListener mRecordsClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	String info = ((TextView) v).getText().toString();            
            int idx = info.indexOf("id=");
            int sp = info.indexOf(" ");
            String id = info.substring(idx+3, sp);
            if (mState == STATE_GET_WARR){
            	wId = "";
	            if (idx != -1) {
	            	wId = id;            	
	            }
            }
//            Toast.makeText(mContext, "Selected ID=" + info, Toast.LENGTH_LONG).show();
            if (D) Log.d(TAG, "OnClick() " + id);
            if ((mState == STATE_GET_WARR) && (wId.length() > 0)){
            	sendRequest(STATE_GET_WARR_CONT);
            }
        }
    };    
    
	private void sendRequest(Integer state){
		try {
			String url = currentServerURI;
			String title;
			mState = state;
			switch (state) {
				case STATE_GET_ENT:
					url += ENT;
					title = "Предприятия";
					break;
				case STATE_GET_GDS:
					url += GDS;
					title = "Товары/услуги";
					break;
				case STATE_GET_EMPS:
					url += EMPS;
					title = "Сотрудники";
					break;
				case STATE_GET_WARR_CONT:
					url = url + WARR_CONT + wId;
					title = "Наряд";
					break;
				case STATE_GET_WARRANTS:
					url += WARRANTS;
					title = "Наряды Offline";
					break;
				case STATE_GET_EMP_HW:
					url += EMP_HW;
					title = "Запчасти";
					break;
				case STATE_GET_STATE:
					url += STATE;
					title = "Справочник состояний";
					break;
				case STATE_GET_USER:
					url += USR;
					title = "Пользователь";
					break;
				case STATE_GET_MSGS:
					url += MSGS;
					title = "Сообщения";
					break;
				default:
					mState = STATE_GET_WARR;
					title = "Наряды";
			}
			if (mState != STATE_FINISH) {
				//Simple GET
				HttpGet searchRequest = new HttpGet(url);
				searchRequest.addHeader(BasicScheme.authenticate(
						new UsernamePasswordCredentials(authUserName, authUserPsw), "UTF-8", false));
				RestTask task = new RestTask();
				task.setResponseCallback((ResponseCallback) mContext);
				task.execute(searchRequest);
				//Display progress to the user
				mProgress = ProgressDialog.show(this, title, "Ожидаем данные...", true);
			}
		} catch (Exception e) {
			mResult.setText(e.getMessage());
		}
	}
	
	private void postStateRequest(Integer state){
		try {
			String url = currentServerURI;
			String title = "";
			String warr = "";
			mState = state;
			switch (state) {
				case STATE_POST_WARRANTS:
					url += WARRANTS;
					title = "Наряды Offline выгрузка";
					warr = dbSch.getModifiedWarrants();
                   // Log.d(TAG + " list ", warr);
					break;
				case STATE_POST_MSGS:
					url += MSGS;
					title = "Сообщения Offline выгрузка";
					warr = dbSch.getModifiedMsgs();
					break;
				case STATE_POST_EMP_WORK_TIME:
					url += EMPWORKTIME;
					title = "Отметки рабочего времени";
					warr = dbSch.getEmpWorkTimeRecords();
					break;
				case STATE_POST_EMP_LOCATIONS:
					url += EMPLOCATION;
					title = "Местоположение";
					warr = dbSch.getEmpLocationRecords();
					break;
				default:
					mState = STATE_FINISH;
			}
			if (mState != STATE_FINISH) {
				if (warr.length() > 0) {
					//Simple POST
					HttpPost postRequest = new HttpPost(url);
					postRequest.addHeader(BasicScheme.authenticate(
							new UsernamePasswordCredentials(authUserName, authUserPsw), "UTF-8", false));

					StringEntity se = new StringEntity(warr, "UTF-8");
					//se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					se.setContentType("application/json;charset=UTF-8");
					se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
					postRequest.setEntity(se);


					RestTask task = new RestTask();
					task.setResponseCallback((ResponseCallback) mContext);
					task.execute(postRequest);
					//Display progress to the user
					mProgress = ProgressDialog.show(this, title, "Ожидаем ответ...", true);
				} else {
					mState = STATE_FINISH;
					listItems.add("Нет изменений для передачи!");
					adapter.notifyDataSetChanged();
				}
			}
		} catch (Exception e) { 
			mResult.setText(e.getMessage()); 
		}		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.warr_list, menu);
		return true;
	}

	@Override 
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.search:
        	if (D) Log.d(TAG, "onOptions Search");
            return true;
        case R.id.action_show_msgs:
        	if (D) Log.d(TAG, "onOptions action_show_msgs");
        	getMessagesTables();
            return true;        
        case R.id.action_send_msgs:
        	if (D) Log.d(TAG, "onOptions action_send_msgs");
        	postMessageTable();
            return true;        
        case R.id.action_send_locations:
        	if (D) Log.d(TAG, "onOptions action_send_locations");
        	postEmpLocationsTable();
            return true;        
        case R.id.action_send_worktime:
        	if (D) Log.d(TAG, "onOptions action_send_worktime");
        	postEmpWorkTimeTable();
            return true;        
        case R.id.action_post_warr:
        	if (D) Log.d(TAG, "onOptions action_post_warr");
        	postWarrantTables();
            return true;
        case R.id.action_show_warr:
        	if (D) Log.d(TAG, "onOptions action_show_warr");
            Intent runWarrantsListActivity = new Intent(this, WarrantsActivity.class);
            startActivity(runWarrantsListActivity);        	
            return true;
        case R.id.action_show_msgs_list:
        	if (D) Log.d(TAG, "onOptions action_show_msgs_list");
            Intent runMsgListActivity = new Intent(this, EmpMessagesActivity.class);
            startActivity(runMsgListActivity);        	        	
            return true;        
        case R.id.action_scan_barcode:
        	if (D) Log.d(TAG, "onOptions action_scan_barcode");
        	Intent intent = new Intent(this, BarScanActivity.class);
        	startActivityForResult(intent, SCAN_BARCODE);        	
            return true;
        case R.id.action_show_webkit:
        	Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SERVER_URI_SSL +"/index.php"));
        	//Intent webIntent = new Intent(this, WebKitActivity.class);
        	startActivity(webIntent);        	
        	return true;
        case R.id.action_show_location:
        	if (D) Log.d(TAG, "onOptions action_show_location");
        	Intent runLocationActivity = new Intent(this, LocationActivity.class);
        	startActivity(runLocationActivity);        	
            return true;        
        }
        return false;
    }	

	@Override
	public void onRequestSuccess(String response) {
		//Clear progress indicator 
		if(mProgress != null) { 
			mProgress.dismiss(); 
		}
		if (D) Log.d(TAG, "onRequestSuccess");
		ParseJSONArray parseJSON = new ParseJSONArray();
		parseJSON.execute(response);

		if (D) Log.d(TAG, "onRequestSuccess - Exit");
		Log.d(TAG, response);
	}

	@Override
	public void onRequestError(Exception error) {
		//Clear progress indicator 
		if(mProgress != null) { 
			mProgress.dismiss(); 
		} 
		//Process the response data (here we just display it) 
		mResult.setText(error.getMessage());		
	}
	private class ParseJSONArray extends AsyncTask<String, Integer, Boolean> {
		String returnString = null;
	
	    @Override
	    protected Boolean doInBackground(String... sJSONStr) {
			long id, idContract;
			int id_const;
			int id_parent;
			int ttype;
			int gdstype;
			int num;
			int jobCnt;
			int idEnt;
			int idSub;
			int idOwner;
			int idState;
			int regl;
			long idWarrLocal;
			Double lat;
			Double lng;
			Date dOpen;
			String sLat;			
			String sLng;
			String nm;
			String addr;
			String job;
			String srl;	
			String openDate; 
			String sRem;
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			SimpleDateFormat showDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			boolean lastTask = isNoTaskLeft();
	        try {
	            JSONArray records = new JSONArray(sJSONStr[0]);
	            listItems.clear();
	            // this will be useful so that you can show a typical 0-100% progress bar
	            int recLength = records.length();

    			returnString = "�������: " + recLength;
    			if (mState == STATE_GET_WARR) {
    				returnString = returnString + " �������.";
    			} else {
    			 	returnString = returnString + " �������.";
    			}
    			if (mState == STATE_GET_WARRANTS) {
    				dbSch.emptyAllWarrantTables();
    			}
                else if (mState == STATE_GET_EMP_HW) {
    				dbSch.emptyEmpHarwareTable();
    			} else if (mState == STATE_GET_STATE) {
    				dbSch.emptyStateTable();
    			} else if (mState == STATE_GET_EMPS) {
    				dbSch.emptyEmpTable();
    			} else if (mState == STATE_GET_MSGS) {
    				dbSch.emptyMsgsTable();
    			}
    			long cntr;
    			switch (mState) {
				case STATE_GET_ENT:
					dbSch.emptyTable(EntTable.TABLE_NAME);
					dbSch.insEntBulkJSON(records);
					cntr = dbSch.getEntCount();
					break;
				case STATE_GET_GDS:
					dbSch.emptyTable(GoodsTable.TABLE_NAME);
					dbSch.insGoodsBulkJSON(records);
					cntr = dbSch.getGdsCount();
					break;
				case STATE_GET_EMPS:
					dbSch.emptyTable(EmpTable.TABLE_NAME);
					dbSch.insEmpsBulkJSON(records);
					cntr = dbSch.getEmpsCount();
					break;
    			}
    			if (mState != STATE_GET_ENT && mState != STATE_GET_GDS) {
	    			long startTime = System.currentTimeMillis();
	    			for (int i = 0; i < records.length(); i++) {
	//    				if (D) Log.d(TAG, Integer.toString(i));
	    				JSONObject warrant = records.getJSONObject(i);
	    				switch (mState) {

	    				case STATE_GET_WARR_CONT:
	    					id = warrant.getInt("id");
	    					nm = warrant.getString("nm");
	    					jobCnt = warrant.getInt("jobcnt");
	    					job = warrant.getString("job");
	    					srl = warrant.getString("srl");
	    					listItems.add(String.format("id=%d %s S/N:%s; %s; �����: %d", 
	    							id, nm, srl, job, jobCnt));    					
	    					break;
	    				case STATE_GET_WARRANTS:   					
	    					id = warrant.getInt("id");
	    					num = warrant.getInt("num");
	    					idContract = warrant.getInt("id_c");
	    					idEnt = warrant.getInt("ident");
	    					regl = warrant.getInt("regl");
	    					idSub = warrant.getInt("idsubent");
	    					idState = warrant.getInt("id_state"); 
	    					openDate = warrant.getString("d_open");
	    					String chDate = warrant.getString("d_ch");
	    					jobCnt = warrant.getInt("jobcnt");
	    					dOpen = sDateFormat.parse(openDate);
	    					idOwner = warrant.getInt("owner");
	    					sRem = warrant.getString("rem");
							int toSign = warrant.getInt("to_sign");
							String signDate = warrant.getString("d_sign");
							long id_sign_user = warrant.getLong("id_sign_user");
	    					//if (D) Log.d(TAG, "Rem List= "+ sRem);
	    					idWarrLocal = dbSch.addWarrantItem(id, num, idContract, idEnt,
                                    idSub, idState,
	    						dOpen, sRem, idOwner, toSign, signDate, id_sign_user, chDate
                                    ,regl
                            );
	    					String warrContStr = "";
	    					String warrUnitStr = "";
	    					String warrRepairStr = ""; 
	    					if (idWarrLocal > 0) {
		    					try {
									long idCont, idContLocal, eqSpec;
									int idGds, idJob, jobStatus, eqStatus, tblMode;
		    						String eqSrl, eqInv;
		    						String remCont;
			    					JSONArray warrContArray = warrant.getJSONArray("warr_cont");
			    			        for (int j = 0; j < warrContArray.length(); j++) {
			    			        	JSONObject warrCont = warrContArray.getJSONObject(j);
			        					idCont = warrCont.getInt("id");
			        					idGds = warrCont.getInt("id_gds");
			        					idJob = warrCont.getInt("id_job");
			        					jobStatus = warrCont.getInt("job_s");
			        					eqStatus = warrCont.getInt("eq_s");
			        					eqSpec = warrCont.getInt("eq_spec");
			        					eqSrl = warrCont.getString("srl");
			        					eqInv = warrCont.getString("srl");
			        					tblMode = warrCont.getInt("tbl");
		//	        					jobCnt = warrCont.getInt("jobcnt");
			        					remCont = warrCont.getString("rem");
			        					idContLocal = dbSch.addWarrContItem(idCont, id, idGds, eqStatus, eqSpec, 
			        							eqSrl, eqInv, idJob, jobStatus, tblMode, remCont);
			    			        	if (idContLocal > 0) {
				    			        	warrContStr += warrCont.getInt("id") + "; ";				    			        	    			        	
				    						long idUnit;
				    						int idJobU;
				    						long idUnitLocal = 0;
				    						String remUnit;
				    			        	JSONArray warrUnitArray = warrCont.getJSONArray("warr_unit");
				    			        	for (int k = 0; k < warrUnitArray.length(); k++) {
				    			        		JSONObject warrUnit = warrUnitArray.getJSONObject(k);
					        					idUnit = warrUnit.getInt("id");
					        					if (idUnit > 0) {
						        					idJobU = warrUnit.getInt("id_job");
						        					remUnit = warrUnit.getString("rem");
						        					tblMode = warrUnit.getInt("tbl");
						        					idUnitLocal = dbSch.addWarrUnitItem(idUnit, idContLocal, idJobU, tblMode, remUnit);	
					        					}
				    			        		warrUnitStr += idUnit + "; ";
				    			        		
				    			        		if (idUnitLocal > 0) {
				    			        			int wrId;
				    			        			int idRepair;
				    			        			double cost;
				    			        			double quantity;
				    			        			String rUnits;
			//	    			        			warrRepairStr = "";
					    			        		JSONArray warrRepairArray = warrUnit.getJSONArray("warr_repair");
					    			        		for (int l = 0; l < warrRepairArray.length(); l++) {
					    			        			JSONObject warrRepair = warrRepairArray.getJSONObject(l);
					    			        			wrId = warrRepair.getInt("id");
					    			        			if(wrId > 0) {
						    			        			idRepair = warrRepair.getInt("id_repair");
						    			        			cost = warrRepair.getDouble("cost");
						    			        			quantity = warrRepair.getDouble("quant");
						    			        			rUnits = warrRepair.getString("units");
						    			        			tblMode = warrUnit.getInt("tbl");
															dbSch.addWarrRepairItem(wrId, id, idUnitLocal, idRepair, tblMode, cost, quantity);		
					    			        			}
					    			        			warrRepairStr += wrId + "; ";
					    			        		}
				    			        		}
				    			        	}
			    			        	}
			    			        }
		    					}
		    			        catch (JSONException e) {
		    			        	e.printStackTrace();
		    			            throw new RuntimeException(e);
		    			        }
	    					}
	    					
	    					if (lastTask)
	    						listItems.add(String.format("id=%d #%d [%d] %d/%d �� %s; �����: %d; wc: %s; wu: %s; wr: %s", 
	    							id, num, idState, idEnt, idSub, showDateFormat.format(dOpen), jobCnt, warrContStr, warrUnitStr, warrRepairStr));   					
	    					break;
	    				case STATE_GET_EMP_HW:
	    					id_const = warrant.getInt("id");
	    					sLng = warrant.getString("cnt");
	    					lng = (double) Float.parseFloat(sLng.replace(",", "."));
	    					dbSch.addEmpHWItem(id_const, lng);	
	    					if (lastTask)
	    						listItems.add(String.format("id=%d; �������: %f", id_const, lng));    					
	    					break;
	    				case STATE_GET_STATE:
	    					id_const = warrant.getInt("id");
	    					id_parent = warrant.getInt("id_doc");
//	    					ttype = warrant.getInt("no");
	    					nm = warrant.getString("nm");
	    					dbSch.addStateItem(id_const, id_parent, /*ttype,*/ nm);	
	    					if (lastTask)
	    						listItems.add(String.format("id=%d; nm: %s", id_const, nm));    					
	    					break;
	    				case STATE_GET_USER:
	    					id = warrant.getInt("id");
	    					dbSch.addUserItem(id);
	    					break;
	    				case STATE_GET_MSGS:
	    					id_const = warrant.getInt("id");
	    					id_parent = warrant.getInt("id_sender");
	    					int id_recipient = warrant.getInt("id_recip");
	    					idState = warrant.getInt("id_state");
	    					openDate = warrant.getString("m_date");
	    					dOpen = sDateFormat.parse(openDate);
	    					addr = warrant.getString("subj");
	    					nm = warrant.getString("msg");
	    					int status = warrant.getInt("status");
	    					int a_size = warrant.getInt("a_sz");
	    					String a_nm = warrant.getString("a_nm");
	    					dbSch.addMsgItem(id_const, id_parent, id_recipient, idState, status, a_size, dOpen, "", addr, nm, a_nm);
	    					if (lastTask)
	    						listItems.add(String.format("id=%d; sender: %s; subj: \"%s\"\nmsg: \"%s\"", 
	    								id_const, id_parent, addr, nm));    					
	    					break;
	    				case STATE_POST_WARRANTS:
	    					nm = warrant.getString("result");
	    					listItems.add(String.format("Got response: %s", nm));
	    					//dbSch.clearModifiedWarrants();
	    					prepareGetWarrantsTask();
	    					//mState = STATE_FINISH;
	    					//getWarrantTables();
	    					break;
	    				case STATE_POST_MSGS:
	    					nm = warrant.getString("result");
	    					listItems.add(String.format("Got response: %s", nm));
	    					prepareGetMessagesTask();
	    					break;
	    				case STATE_POST_EMP_WORK_TIME:
	    					nm = warrant.getString("result");
	    					id_const = warrant.getInt("code");
	    					listItems.add(String.format("Got response: %s", nm));
	    					if (id_const == 0)
	    						dbSch.EmptyEmpWorkTimeTable();
	    					break;
	    				case STATE_POST_EMP_LOCATIONS:
	    					nm = warrant.getString("result");
	    					id_const = warrant.getInt("code");
	    					listItems.add(String.format("Got response: %s", nm));
	    					if (id_const == 0)
	    						dbSch.setLocationModified(0);

	    					break;
	    				default:
	    				}
		                publishProgress((int) (i * 100 / recLength));
	                }
	    			long endtime = System.currentTimeMillis();
	    	        Log.i("Time to insert Members: ", String.valueOf(endtime - startTime));
    			}
	        } catch (Exception e) {
	        	e.printStackTrace();
    			returnString = "Exception: " + e.getMessage();
    			listItems.add(String.format("Got %s \n%s", returnString, sJSONStr[0]));
    			return false;
	        } finally {
	        	mState = STATE_FINISH;
	        }
	        return true;
	    }
	    
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        mProgressDialog.show();
	    }

	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        mProgressDialog.setProgress(progress[0]);
	    }
	    
	    protected void onPostExecute(Boolean success) {
	    	mProgressDialog.setProgress(0);
	    	mProgressDialog.dismiss();
	        // Show dialog with result
	    	if (D) Log.d(TAG, "onPostExecute");
	    	adapter.notifyDataSetChanged();
			mResult.setText(returnString);

			int task = getNextTask();
	    	if (task != STATE_FINISH)
	    		sendRequest(task);	
	    	else
	    		updateCounters("");
	     }
	}
		    	
	private void prepareGetConstTask(boolean forced) {
		long cntr = 0;
		cntr = dbSch.getEntCount();
		if (forced || (cntr <= 0)) 
			taskArray[0] = STATE_GET_ENT;
		else
			taskArray[0] = STATE_FINISH;
		cntr = dbSch.getGdsCount();
		if (forced || (cntr <= 0)) 
			taskArray[1] = STATE_GET_GDS;
		else
			taskArray[1] = STATE_FINISH;
		cntr = dbSch.getEmpsCount();
		if (forced || (cntr <= 0)) 
			taskArray[2] = STATE_GET_EMPS;
		else
			taskArray[2] = STATE_FINISH;
		taskArray[3] = STATE_GET_STATE;		
		
//		taskArray[0] = STATE_FINISH;
//		taskArray[1] = STATE_FINISH;
//		taskArray[2] = STATE_FINISH;
	}

	private void prepareGetWarrantsTask() {
		taskArray[0] = STATE_GET_WARRANTS;
		taskArray[1] = STATE_FINISH;//STATE_GET_EMP_HW;
		taskArray[2] = STATE_FINISH;
		taskArray[3] = STATE_FINISH;
	}
	
	private void prepareGetMessagesTask() {
		long cntr = dbSch.getUserCount();
		taskArray[0] = STATE_GET_MSGS;
		if (cntr <= 0) {
			taskArray[1] = STATE_GET_USER;
		} else {
			taskArray[1] = STATE_FINISH;			
		}
		taskArray[2] = STATE_FINISH;
		taskArray[3] = STATE_FINISH;
	}
	
	private void preparePostWarrantsTask() {
		taskArray[0] = STATE_POST_WARRANTS;
		taskArray[1] = STATE_FINISH;
		taskArray[2] = STATE_FINISH;
		taskArray[3] = STATE_FINISH;
	}
	
	private void preparePostMessageTask() {
		taskArray[0] = STATE_POST_MSGS;
		taskArray[1] = STATE_FINISH;
		taskArray[2] = STATE_FINISH;
		taskArray[3] = STATE_FINISH;		
	}
	
	private void preparePostEmpWorkTimeTask() {
		taskArray[0] = STATE_POST_EMP_WORK_TIME;
		taskArray[1] = STATE_FINISH;
		taskArray[2] = STATE_FINISH;
		taskArray[3] = STATE_FINISH;		
	}
	
	private void preparePostEmpLocationsTask() {
		taskArray[0] = STATE_POST_EMP_LOCATIONS;
		taskArray[1] = STATE_FINISH;
		taskArray[2] = STATE_FINISH;
		taskArray[3] = STATE_FINISH;		
	}
	
	private int getNextTask() {
		int result = STATE_FINISH;
		for (int i = 0; i < taskArray.length; i++) {
			if (taskArray[i] != STATE_FINISH) {
				result = taskArray[i]; 
				taskArray[i] = STATE_FINISH;
				break;
			}
		}	
		return result;
	}
	
	private boolean isNoTaskLeft() {
		boolean result = true;
		for (int i = 0; i < taskArray.length; i++) {
			if (taskArray[i] != STATE_FINISH) {
				result = false; 
				break;
			}
		}	
		return result;
	}
	
	private void updateCounters(String initString) {
		long cntr;
		cntr = dbSch.getEntCount();
		if (initString != "")
			initString += "\n";
		resultString = initString + "\tEntCount = " + cntr;
		if (D) Log.d(TAG, "getEntCount() = " + cntr);
		cntr = dbSch.getGdsCount();
		resultString += "\n\tGdsCount = " + cntr;
		if (D) Log.d(TAG, "getGdsCount() = " + cntr);  
		cntr = dbSch.getEmpsCount();
		resultString += "\n\tEmpsCount = " + cntr;
		if (D) Log.d(TAG, "getEmpsCount() = " + cntr);  

		cntr = dbSch.getMsgsCount();
		resultString += "\n\tMsgsCount = " + cntr;
		cntr = dbSch.getLocationCount();
		resultString += "\n\tLocationCount = " + cntr;
		
		cntr = dbSch.getWarrantCount();
		resultString += "\n\tWarrCount = " + cntr;
		if (D) Log.d(TAG, "getWarrantCount() = " + cntr);
		cntr = dbSch.getWarrContCount();
		resultString += "\n\tWarrContCount = " + cntr;
		if (D) Log.d(TAG, "getWarrContCount() = " + cntr);
		cntr = dbSch.getWarrUnitCount();
		resultString += "\n\tWarrUnitCount = " + cntr;
		if (D) Log.d(TAG, "getWarrUnitCount() = " + cntr);
		cntr = dbSch.getWarrRepairCount();
		resultString += "\n\tWarrRepairCount = " + cntr;
		if (D) Log.d(TAG, "getWarrRepairCount() = " + cntr);

		mResult.setText(resultString);		
	}
	
	//===YES/No Dialog===
	private void showYesNoDialog(int dialogMode, String dialogText) {
		this.dialogMode = dialogMode;
		FragmentManager fragmentManager = getSupportFragmentManager(); 
		YesNoDialogFragment yesnoDialog = new YesNoDialogFragment();
		yesnoDialog.setCancelable(false);
		switch (dialogMode) {
		case DIALOG_CLEAR_WARRANTS:
			yesnoDialog.setDialogTitle(getString(R.string.m_clear_warr));			
			break;
		case DIALOG_GET_WARRANTS:
			yesnoDialog.setDialogTitle(getString(R.string.m_get_warr));		
			break;
		}
		yesnoDialog.setDialogText(dialogText);
		yesnoDialog.show(fragmentManager, "yes/no dialog");
	}	

	@Override
	public void onFinishYesNoDialog(boolean state) {
//		Toast.makeText(this, "Returned from dialog: "+ state, Toast.LENGTH_SHORT).show();
		switch (dialogMode) {
		case DIALOG_CLEAR_WARRANTS:
			if (state) {
    			dbSch.emptyAllWarrantTables();
    			updateCounters("");				
			}
			break;
		case DIALOG_GET_WARRANTS:
			if (state)
				getWarrantTables();
			break; 
		}
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//    	if (scanResult != null) {
//    		
//    	}
        if (requestCode == SCAN_BARCODE) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
                if (D) Log.d(TAG, "scan successful = "+ contents);
                Toast.makeText(mContext, "Scan =" + contents + " "+ format, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            	if (D) Log.d(TAG, "scan failed");
            }
        }
    }
	
	
}
