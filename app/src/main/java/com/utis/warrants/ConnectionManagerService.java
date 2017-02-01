package com.utis.warrants;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
import com.utis.warrants.RestTask.ResponseCallback;
import com.utis.warrants.record.LogsRecord;
import com.utis.warrants.record.TrafficStatRecord;
import com.utis.warrants.record.WarrantRecord;
import com.utis.warrants.tables.EmpTable;
import com.utis.warrants.tables.EntTable;
import com.utis.warrants.tables.GoodsTable;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class ConnectionManagerService extends Service implements ResponseCallback {
	private static final boolean D = true;
	private static final String TAG = "ConnManagServ";
	private static final int LOG_LIFE = 7;
	private static final int UPDATE_DICT_INTERVAL = 10;  
	private static final String MAX_DEL_WARR_DATE = "maxWarrDate";
    public static final String REFRESH_LIST_ACTION = "RefreshListAction";

	public interface ServiceResponseCallback {
		public void onSrvRequestSuccess(String response);
		public void onSrvRequestError(Exception error);
		public void onSrvTaskChanged(int Task);
	}

	private WeakReference<ServiceResponseCallback> mCallback;

	//public static final String DEBUG_SERVER_URI_SSL = "https://192.168.2.238:8443";
	public static final String SERVER_URI_PRFX = "https://";
	public static final String SERVER_URI = "192.168.2.7";
	public static final String SERVER_URI_SSL_PORT = ":443"; 

	public static final String DEBUG_SERVER_URI_SSL = "https://192.168.2.7:443";
	public static final String SERVER_URI_SSL = "https://85.238.112.13:443/contenti";
	public static final String LOGIN_IMEI_URI = "/Ol2/i_login.php";
	public static final String LOGIN_PSW_URI = "/Ol2/a_login.php";
	public static final String HELP_URI = "/warrants.htm";

	public static final String REST_WARRANTS_URI = "/Ol2/warr_list_online.php";
	public static final int AUTH_IMEI = 1;
	public static final int AUTH_PSW = 2;
	public static final String MOBILE = "+mobile";
	private static final int SRV_INTERVAL = 60; // seconds
	private static final int SRV_WARR_DIFF_INTERVAL = 3; // x*SRV_INTERVAL
	private static final String DEBUG_USER_NAME = "CONTENT_86";
	private static final int DIALOG_GET_WARRANTS = 1;
	private static final int DIALOG_CLEAR_WARRANTS = 2;
	private static final int SCAN_BARCODE = 3;
	public static final String VER = "/ver";
	public static final String LOG_URI = "/logs";
	public static final String INFO = "/info";
	public static final String SIGN_WARR = "/sign_warr";
	public static final String GET_SIGN_EMP = "/get_sign_emp/";
	private static final String ENT = "/ent";
	private static final String GDS = "/gds";
	private static final String WARR_CONT = "/warrant/";
	private static final String WARRANTS = "/warrants";
	private static final String LAST_WARRANTS = "/warrants_last/";
	private static final String WARRANTS_REPORT = "/warrants_report";
	private static final String WARR_CONTRACTS = "/warr_contracts";
	private static final String WARR_SPECS = "/warr_specs";
	private static final String LAST_WARR_SPECS = "/warr_specs_last/";
	private static final String LAST_REPLICATOR_START = "/repl_time";
	private static final String NEW_WARRANTS = "/new_warrants/";
	private static final String EMP_HW = "/hw/538";
	private static final String EMPS = "/emps/538";
	private static final String USR = "/usr";
	private static final String MSGS = "/msgs";
	private static final String NEW_MSGS = "/new_msgs/";
	private static final String MOD_MSGS = "/mod_msgs/";
	private static final String EMPWORKTIME = "/worktime";
	private static final String EMPLOCATION = "/emp_locations";
	private static final String SPECS_INV = "/specs_inv";
	private static final String STATE = "/state";
	private static final String EMP_SEALS = "/seals/538";
	private static final String REASON = "/reason";
	private static final String SEAL_CERT = "/seal_cert";
	// Constants that indicate the current request state
	public static final int STATE_FINISH = 0; // process finishes
	public static final int STATE_GET_WARR = 1; // REST without params
	public static final int STATE_GET_ENT = 2; // REST ent
	public static final int STATE_GET_GDS = 3; // REST gds
	public static final int STATE_GET_STATE = 4; // REST get d_state
	public static final int STATE_GET_EMPS = 5; // REST get t_emp
	public static final int STATE_GET_USER = 6; // REST get t_emp
	public static final int STATE_GET_WARR_CONT = 7; // REST warrant
	public static final int STATE_GET_NEW_ENT = 8;
	public static final int STATE_GET_NEW_GDS = 9;
	public static final int STATE_GET_REASON = 10;
	public static final int STATE_GET_WARRANTS = 20; // REST get all warrants data
	public static final int STATE_GET_NEW_WARRANTS = 21; // REST get all warrants data
	public static final int STATE_GET_EMP_HW = 22; // REST get employee hardware
	public static final int STATE_GET_WARR_CONTRACTS = 23; // REST get employee warrant contracts
	public static final int STATE_GET_WARR_SPECS = 24; // REST get employee warrant contract's specification
	public static final int STATE_GET_LAST_REPLICATOR_START = 25; // REST get replicator's last start date/time
	public static final int STATE_GET_EMP_SEALS = 26; // REST get employee seals
	public static final int STATE_GET_LAST_WARRANTS = 27; // REST get warrants data after date
	public static final int STATE_GET_LAST_WARR_CONTRACTS = 28; // REST get employee warrant contracts after date
	public static final int STATE_GET_LAST_WARR_SPECS = 29; // REST get employee warrant contract's specification after date
	
	public static final int STATE_GET_MSGS = 30; // REST get messages
	public static final int STATE_GET_NEW_MSGS = 31; // REST get new msgs
	public static final int STATE_POST_WARRANTS = 100; // REST post all warrants data
	public static final int STATE_POST_MSGS = 101; // REST post messages
	public static final int STATE_POST_EMP_WORK_TIME = 102; // REST post  emp_work_time
	public static final int STATE_POST_EMP_LOCATIONS = 103; // REST post emp_locations
	public static final int STATE_POST_SPECS_INVENTORY_NUM = 104; // REST post d_specs inv
	public static final int STATE_POST_SEAL_CERT = 105; // REST post справки опломбирования

	private int mState = STATE_FINISH;
	private String ServerURI;
	private int serverWarrantInterval;
	private boolean notifyNewMsg, notifyNewWarrant;
	private boolean keepLog, needSpecs;
	private boolean autoEmpWorktimeMark, keepWarrantTillReport;
	private boolean needEmpPhotoLoad;
	private int[] taskArray = new int[15];
	private DBSchemaHelper dbSch;
	private TelephonyManager tm;
	private String currentServerURI;
	private String authUserName, authUserPsw;
	private String mIMEI, mPhoneNumber, wId;
	private int cntr = 1;
	private static boolean firstRun = true;
	public Context mContext;
	private boolean isWorking = false;
	private boolean isRequestProcessing = false;
	private boolean isRequestResultError = false;
	public boolean sendToasts = false;
	public boolean busyWarrants = false;
	public long rxBytes, txBytes;
	public long rxUidBytes, txUidBytes;
	public long rxMobBytes, txMobBytes;
	public long rxJSONBytes, txJSONBytes;
    private LocalBroadcastManager lbm;
    private boolean mNeed2RefreshWarrantsList;

	private int  uid;
	private long mStartRX = 0;
	private long mStartTX = 0;
	private long mStartUidRX = 0;
	private long mStartUidTX = 0;
	private long mStartMobRX = 0;
	private long mStartMobTX = 0;
	private boolean mTrafficStatEnabled = false;
	private boolean gettingAllConstTables = false;
	 
	private NotificationManager mNManager;
	private static final int MSG_NOTIFY_ID = 1100;
	private static final int WARRANT_NOTIFY_ID = 1101;
	final Notification n_msg = new Notification(R.drawable.utis_logo,
			"Наряды: новое сообщение", System.currentTimeMillis());
	final Notification n_warrants = new Notification(R.drawable.utis_logo,
			"Наряды: новое задание", System.currentTimeMillis());
	private long selfEmpId = 0;
	private long preMsgCount;
	private long preMaxMsgId;
	private long preMaxWarrantId;
	private String preMaxMsgDateChange;

	/* Service Access Methods */
	public class ConnectionBinder extends Binder {
		ConnectionManagerService getService() {
			return ConnectionManagerService.this;
		}
	}

	private final IBinder binder = new ConnectionBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	private Handler mHandler = new Handler();
	private Runnable timerTask = new Runnable() {
		int prevDayOfYear = -1;
		int updateDictCntr = 1;
		
		@Override
		public void run() {
			int curHour;
			int curDayOfYear;
			if (isWorking) {
				Calendar now = Calendar.getInstance();
				String sTime = String.format("%02d:%02d:%02d",
						now.get(Calendar.HOUR), now.get(Calendar.MINUTE),
						now.get(Calendar.SECOND));
				if (D) Log.d(TAG , sTime+ " time ");
				curHour = now.get(Calendar.HOUR_OF_DAY);
				curDayOfYear = now.get(Calendar.DAY_OF_YEAR);
				if (prevDayOfYear < 0) prevDayOfYear = curDayOfYear;
				if (curDayOfYear != prevDayOfYear) {
					prevDayOfYear = curDayOfYear; // starts new day
                    Log.d(TAG , prevDayOfYear+ " day ");
					deleteOldLogs();
				}

				rxMobBytes = TrafficStats.getMobileRxBytes()- mStartMobRX;
				txMobBytes = TrafficStats.getMobileTxBytes()- mStartMobTX;
				rxBytes = TrafficStats.getTotalRxBytes()- mStartRX;
				txBytes = TrafficStats.getTotalTxBytes()- mStartTX;
				rxUidBytes = TrafficStats.getUidRxBytes(uid)- mStartUidRX;
				txUidBytes = TrafficStats.getUidTxBytes(uid)- mStartUidTX;
				cntr++;
				updateDictCntr++;
				// msg interval
				if (firstRun) {
					firstRun = false;
					deleteOldLogs();
					TrafficStatRecord tsRec = dbSch.getTrafficStatDateData(new Date());
					if (tsRec != null) {
						rxUidBytes += tsRec.rx_bytes; 
						txUidBytes += tsRec.tx_bytes; 
						mStartUidRX -= tsRec.rx_bytes;
						mStartUidTX -= tsRec.tx_bytes;
						rxJSONBytes += tsRec.rx_json_bytes; 
						txJSONBytes += tsRec.tx_json_bytes;
					}							
//					getConstTables(false);
					if (dbSch.getModifiedWarrantCount() == 0) {
						gettingAllConstTables = true;
						getConstWarrantTables(false);
					} else
						prepareCombyPostDictUpdateEmpMsgWarrTask();
				} else {
                    if (mState == STATE_FINISH)
                        gettingAllConstTables = false;
					if (cntr > serverWarrantInterval /* SRV_WARR_DIFF_INTERVAL */) { // warrants interval
						cntr = 1;
						//if (curHour >= 8 && curHour <= 18)
						if (updateDictCntr > UPDATE_DICT_INTERVAL) {
							updateDictCntr = 1;
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.MINUTE, -1);
//							cal.add(Calendar.DAY_OF_YEAR, -10);//for debug
							CommonClass.writeLastUpdate(mContext, cal.getTime());
							postCombyDictUpdateMessageWarrantTable();
						} else {
							if (needEmpPhotoLoad) getEmpConstTables(true);
							else postCombyMessageWarrantTable();
						}
					} else {
						//if (curHour >= 8 && curHour <= 18)
							postCombyMessageTable();  // get new messages
					}
				}
				
				dbSch.addTrafficStatItem(rxUidBytes, txUidBytes, rxJSONBytes, txJSONBytes, new Date());
				if (keepLog)
					dbSch.addLogItem(LogsRecord.DEBUG, new Date(),
						String.format("Rx=%d; Tx=%d;\n uRx=%d; uTx=%d;\n MobRx=%d; MobTx=%d\n RxJSON=%d; TxJSON=%d", 
							rxBytes, txBytes, rxUidBytes, txUidBytes, rxMobBytes, txMobBytes, rxJSONBytes, txJSONBytes));
			}
			// Schedule the next update in one minute
			mHandler.postDelayed(timerTask, TimeUnit.SECONDS.toMillis(SRV_INTERVAL));
		}
	};

	public void reRunTimer() {
		mHandler.removeCallbacks(timerTask);
		mHandler.postDelayed(timerTask, 100);
	}

	public static String getServerURI(Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String serv = sharedPrefs.getString("server_list", "NULL");
		int srv;
		try {
			srv = Integer.parseInt(serv);
		} catch (NumberFormatException e) {
			srv = 1;
		}
		if (srv == 1) {
			return ConnectionManagerService.SERVER_URI_SSL;
		} else if (srv == 0) {
			return ConnectionManagerService.DEBUG_SERVER_URI_SSL;
		} else {
			String srv_addr = sharedPrefs.getString("server_addr", SERVER_URI);
			return SERVER_URI_PRFX + srv_addr + SERVER_URI_SSL_PORT;
		}
	}

	public static String getServerMode(Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String serv = sharedPrefs.getString("server_list", "NULL");
		String[] srv_array = context.getResources().getStringArray(R.array.pref_server_list_titles);
		int srv;
		try {
			srv = Integer.parseInt(serv);
		} catch (NumberFormatException e) {
			srv = 1;
		}
		if (srv == 1) return srv_array[0];
		else if (srv == 0) return srv_array[1];
		else return srv_array[2];
	}

	private void getServicePrefs() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String serv = sharedPrefs.getString("sync_frequency", "NULL");
		try {
			serverWarrantInterval = Integer.parseInt(serv);
		} catch (NumberFormatException e) {
			serverWarrantInterval = SRV_WARR_DIFF_INTERVAL;
		}
		notifyNewMsg = sharedPrefs.getBoolean("notifications_new_message", false);
		notifyNewWarrant = sharedPrefs.getBoolean("notifications_new_warrant", false);
		keepLog = sharedPrefs.getBoolean("logging_checkbox", true);
		CommonClass.keepLog = keepLog;
		autoEmpWorktimeMark = sharedPrefs.getBoolean("emp_time_mark_checkbox", false);
		keepWarrantTillReport = sharedPrefs.getBoolean("warr_report_checkbox", false);
		needEmpPhotoLoad = sharedPrefs.getBoolean("needEmpPhotoLoad", true);
	}

	
	private void setEmpPhotoLoadPrefs() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor mEditor = sharedPrefs.edit();
		mEditor.putBoolean("needEmpPhotoLoad", needEmpPhotoLoad);
	    mEditor.commit();
	}
	
    public void deleteOldLogs() {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -LOG_LIFE);
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");  
		String logDate = df.format(now.getTime());
		
		dbSch.clearOldLogs(logDate);
    }
		

	@Override
	public void onCreate() {
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
        lbm = LocalBroadcastManager.getInstance(this);
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneNumber = tm.getLine1Number();
		mIMEI = tm.getDeviceId();
		// currentServerURI = SERVER_URI_SSL + REST_WARRANTS_URI;
		authUserName = mIMEI;
		authUserPsw = MOBILE;
		mContext = this;
		selfEmpId = dbSch.getUserId();
		String ns = Context.NOTIFICATION_SERVICE;
		mNManager = (NotificationManager) getSystemService(ns);
		ApplicationInfo ai;
		try {			
			ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
			uid = ai.uid;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			uid = 0;
		}
		rxJSONBytes = 0;
		txJSONBytes = 0;
		mStartRX = TrafficStats.getTotalRxBytes();
		mStartTX = TrafficStats.getTotalTxBytes();
		mStartUidRX = TrafficStats.getUidRxBytes(uid);
		mStartUidTX = TrafficStats.getUidTxBytes(uid);
		mHandler.post(timerTask);			
		if (D) Log.d(TAG, "ConnectionManager Service created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		getServicePrefs();
		ServerURI = getServerURI(mContext);
		currentServerURI = ServerURI + REST_WARRANTS_URI;
		String mLog = "ConnectionManager Service Started " + ServerURI;
		if (D)
			Log.d(TAG, mLog);
		if (keepLog)
			dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog);

		if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
			mTrafficStatEnabled = false;
			dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "Учет трафика не поддерживается!");
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Внимание!");
			alert.setMessage("Ваше МУ не поддерживает учет трафика!");
			alert.show();
		} else {
			mTrafficStatEnabled = true;
			mStartMobRX = TrafficStats.getMobileRxBytes();
			mStartMobTX = TrafficStats.getMobileTxBytes();
			if (mStartMobRX == TrafficStats.UNSUPPORTED || mStartMobTX == TrafficStats.UNSUPPORTED) {
				dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "Учет моб.трафика не поддерживается!");
			}
			if (keepLog)
				dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "Учет трафика поддерживается!");
		}
		isWorking = true;

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		firstRun = true;
		mHandler.removeCallbacks(timerTask);
		isWorking = false;
		String mLog = "ConnectionManager Service Destroyed";
		if (keepLog)
			dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog);
		Toast.makeText(this, mLog, Toast.LENGTH_LONG).show();
		if (D)
			Log.d(TAG, mLog);
	}

	public void setServiceResponseCallback(ServiceResponseCallback callback) {
		mCallback = new WeakReference<ServiceResponseCallback>(callback);
	}

	public void stopWorking() {
		Toast.makeText(this, "Stopping ConnectionManager", Toast.LENGTH_SHORT)
				.show();
		isWorking = false;
	}

	public void setFirstRun() {
		firstRun = true;
	}
	
	public static boolean getFirstRun() {
		return firstRun;
	}
	
	public boolean isWorking() {
		return isWorking;
	}

	public boolean isRequestProcessing() {
		return isRequestProcessing;
	}

	public boolean isRequestResultError() {
		return isRequestResultError;
	}

	public int getState() {
		return mState;
	}
	
	public boolean gettingAllConstTables() {
		return gettingAllConstTables;
	}

	private void sendNextTask() {
		int task = getNextTask();
		sendActiveTask(task);
		switch (task) {
		case STATE_FINISH:
			break;
		case STATE_POST_WARRANTS:
		case STATE_POST_MSGS:
		case STATE_POST_EMP_WORK_TIME:
		case STATE_POST_EMP_LOCATIONS:
		case STATE_POST_SPECS_INVENTORY_NUM:
		case STATE_POST_SEAL_CERT:
			postStateRequest(task);
			break;
		default:
			sendRequest(task);
		}
	}

	private void clearAllTasks() {
        mNeed2RefreshWarrantsList = false;
		mState = STATE_FINISH;
		for (int i = 0; i < taskArray.length; i++) {
			taskArray[i] = STATE_FINISH;
		}
        Log.d(TAG + "[511]", String.valueOf(mNeed2RefreshWarrantsList));
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

	public void getConstTables(boolean forced) {
		if (mState == STATE_FINISH) {
			prepareGetConstTask(forced);
			sendNextTask();
		}
        Log.d(TAG, "getConstTables(boolean forced)");
	}

	public void getConstWarrantTables(boolean forced) {
		if (mState == STATE_FINISH) {
			prepareGetConstWarrantTask(forced);
			sendNextTask();
		}
        Log.d(TAG, "getConstWarrantTables(boolean forced)");
	}
	
	public void getEmpConstTables(boolean forced) {
		if (mState == STATE_FINISH) {
			prepareGetEmpConstTask(forced);
			sendNextTask();
		}
        Log.d(TAG, "getEmpConstTables(boolean forced)");
	}

	public void getWarrantTables() {
		if (mState == STATE_FINISH) {
			prepareGetWarrantsTask();
			sendNextTask();
		}
        Log.d(TAG, "getWarrantTables()");
	}

	public void getMessagesTables() {
		if (mState == STATE_FINISH) {
			prepareGetMessagesTask();
			sendNextTask();
		}
        Log.d(TAG, "getMessagesTables()");
	}

	public void postWarrantTables() {
		if (mState == STATE_FINISH) {
			preparePostWarrantsTask();
			sendNextTask();
		}
        Log.d(TAG, "postWarrantTables()");
	}

    public void postSpecsTables() {
        if (mState == STATE_FINISH) {
            preparePostSpecsInvTask();
            sendNextTask();
        }
        Log.d(TAG, "---postSpecsTables()---");
    }

	public void postMessageTable() {
		if (mState == STATE_FINISH) {
			preparePostMessageTask();
			sendNextTask();
		}
	}

	public void postEmpWorkTimeTable() {
		if (mState == STATE_FINISH) {
			preparePostEmpWorkTimeTask();
			sendNextTask();
		}
        Log.d(TAG, "postEmpWorkTimeTable()");
	}

	public void postEmpLocationsTable() {
		if (mState == STATE_FINISH) {
			preparePostEmpLocationsTask();
			sendNextTask();
		}
        Log.d(TAG, "postEmpLocationsTable()");
	}

	public void postCombyMessageTable() {
		if (mState == STATE_FINISH) {
			prepareCombyPostEmpMsgTask();
			sendNextTask();
		}
	}

	public void postCombyMessageWarrantTable() {
		if (mState == STATE_FINISH) {
			prepareCombyPostEmpMsgWarrTask();
			sendNextTask();
		}
	}
	
	public void postCombyDictUpdateMessageWarrantTable() {
		if (mState == STATE_FINISH) {
			prepareCombyPostDictUpdateEmpMsgWarrTask();
			sendNextTask();
		}
	}
	
	public void postDiscardWarrant() {
		if (mState == STATE_FINISH) {
//			preparePostWarrantsTask();
			sendNextTask();
		}
	}

	private int prepareGetConstTask(boolean forced) {		
		long cntr = 0;
		int idx = 0;
		clearAllTasks();	
		cntr = dbSch.getEntCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_ENT;
        else
            taskArray[idx++] = STATE_GET_NEW_ENT;
		cntr = dbSch.getGdsCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_GDS;
        else
            taskArray[idx++] = STATE_GET_NEW_GDS;
		cntr = dbSch.getEmpsCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_EMPS;
		cntr = dbSch.getReasonCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_REASON;
		cntr = dbSch.getStateCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_STATE;
		taskArray[idx++] = STATE_GET_USER;
		return idx;
	}

	private void prepareGetEmpConstTask(boolean forced) {		
		clearAllTasks();	
		taskArray[0] = STATE_GET_EMPS;  // debug
	}

	private void prepareGetWarrantsTask() {
        int idx = 0;
		clearAllTasks();
		taskArray[idx++] = STATE_GET_WARRANTS;
		taskArray[idx++] = STATE_GET_EMP_HW;
		taskArray[idx++] = STATE_GET_WARR_CONTRACTS;
		taskArray[idx++] = STATE_GET_WARR_SPECS;
		taskArray[idx++] = STATE_GET_EMP_SEALS;
	}

	private void prepareGetMessagesTask() {
        int idx = 0;
        long cntr = dbSch.getUserCount();
		clearAllTasks();
		taskArray[idx++] = STATE_GET_MSGS;
		if (cntr <= 0)
			taskArray[idx++] = STATE_GET_USER;
	}
	
	private void prepareGetConstWarrantTask(boolean forced) {
		int idx = prepareGetConstTask(forced);
		taskArray[idx++] = STATE_GET_WARRANTS;
		taskArray[idx++] = STATE_GET_EMP_HW;
		taskArray[idx++] = STATE_GET_WARR_CONTRACTS;
		taskArray[idx++] = STATE_GET_WARR_SPECS;			
		taskArray[idx++] = STATE_GET_EMP_SEALS;
		taskArray[idx++] = STATE_GET_LAST_REPLICATOR_START;
	}

	private void preparePostWarrantsTask() {
        int idx = 0;
        clearAllTasks();
		taskArray[idx++] = STATE_POST_WARRANTS;
		taskArray[idx++] = STATE_GET_LAST_WARRANTS; //STATE_GET_WARRANTS;
		taskArray[idx++] = STATE_GET_EMP_HW;
		taskArray[idx++] = STATE_GET_LAST_WARR_CONTRACTS; //STATE_GET_WARR_CONTRACTS;
		taskArray[idx++] = STATE_GET_LAST_WARR_SPECS; //STATE_GET_WARR_SPECS;
		taskArray[idx++] = STATE_GET_EMP_SEALS;
		taskArray[idx++] = STATE_GET_LAST_REPLICATOR_START;
	}

	private void preparePostMessageTask() {
        int idx = 0;
        long cntr = dbSch.getUserCount();
		clearAllTasks();
		taskArray[idx++] = STATE_POST_MSGS;
		taskArray[idx++] = STATE_GET_NEW_MSGS;
		if (cntr <= 0) {
			taskArray[idx++] = STATE_GET_USER;
		}
	}

	private void preparePostEmpWorkTimeTask() {
		clearAllTasks();
		taskArray[0] = STATE_POST_EMP_WORK_TIME;
        Log.d(TAG, "preparePostEmpWorkTimeTask()");
	}

	private void preparePostEmpLocationsTask() {
		clearAllTasks();
		taskArray[0] = STATE_POST_EMP_LOCATIONS;
        Log.d(TAG, "prepareCombyPostEmpMsgTask()");
	}
	
	private void preparePostSpecsInvTask() {
		clearAllTasks();
		taskArray[0] = STATE_POST_SPECS_INVENTORY_NUM;
        Log.d(TAG, "&&&&&preparePostEmpLocationsTask()");
	}

	private void prepareCombyPostEmpMsgTask() {
        Log.d(TAG, "prepareCombyPostEmpMsgTask()");
        int idx = 0;
		clearAllTasks();
		taskArray[idx++] = STATE_POST_EMP_LOCATIONS;
		taskArray[idx++] = STATE_POST_EMP_WORK_TIME;
		taskArray[idx++] = STATE_POST_MSGS;
		taskArray[idx++] = STATE_POST_SPECS_INVENTORY_NUM;
		taskArray[idx++] = STATE_POST_SEAL_CERT;
		taskArray[idx++] = STATE_GET_NEW_MSGS;
		long cntr = dbSch.getUserCount();
		if (cntr <= 0)
			taskArray[idx++] = STATE_GET_USER;
	}

	private void prepareCombyPostEmpMsgWarrTask() {
        Log.d(TAG, "prepareCombyPostEmpMsgWarrTask()");
        int idx = 0;
		clearAllTasks();
		taskArray[idx++] = STATE_POST_EMP_LOCATIONS;
		taskArray[idx++] = STATE_POST_EMP_WORK_TIME;
		taskArray[idx++] = STATE_POST_MSGS;
		taskArray[idx++] = STATE_POST_SPECS_INVENTORY_NUM;
		taskArray[idx++] = STATE_POST_SEAL_CERT;
		taskArray[idx++] = STATE_GET_NEW_MSGS;
		if (busyWarrants) {
			taskArray[idx++] = STATE_GET_NEW_WARRANTS;
			taskArray[idx++] = STATE_GET_LAST_REPLICATOR_START;
		} else {
			taskArray[idx++] = STATE_POST_WARRANTS;
			taskArray[idx++] = STATE_GET_LAST_WARRANTS; //STATE_GET_WARRANTS;
			taskArray[idx++] = STATE_GET_EMP_HW;
			taskArray[idx++] = STATE_GET_LAST_WARR_CONTRACTS; //STATE_GET_WARR_CONTRACTS;
			taskArray[idx++] = STATE_GET_LAST_WARR_SPECS; //STATE_GET_WARR_SPECS;
			taskArray[idx++] = STATE_GET_EMP_SEALS;
			taskArray[idx++] = STATE_GET_LAST_REPLICATOR_START;
		}
	}
	
	private void prepareCombyPostDictUpdateEmpMsgWarrTask() {
        Log.d(TAG, "prepareCombyPostDictUpdateEmpMsgWarrTask()");
        int idx = 0;
		clearAllTasks();
		taskArray[idx++] = STATE_POST_EMP_LOCATIONS;
		taskArray[idx++] = STATE_POST_EMP_WORK_TIME;
		taskArray[idx++] = STATE_POST_MSGS;
		taskArray[idx++] = STATE_POST_SPECS_INVENTORY_NUM;
		taskArray[idx++] = STATE_POST_SEAL_CERT;
		taskArray[idx++] = STATE_GET_NEW_MSGS;
		taskArray[idx++] = STATE_GET_NEW_ENT;
		taskArray[idx++] = STATE_GET_NEW_GDS;
		if (busyWarrants) {
			taskArray[idx++] = STATE_GET_NEW_WARRANTS;
		} else {
			taskArray[idx++] = STATE_POST_WARRANTS;
			taskArray[idx++] = STATE_GET_LAST_WARRANTS; //STATE_GET_WARRANTS;
			taskArray[idx++] = STATE_GET_EMP_HW;
			taskArray[idx++] = STATE_GET_LAST_WARR_CONTRACTS; //STATE_GET_WARR_CONTRACTS;
			taskArray[idx++] = STATE_GET_LAST_WARR_SPECS; //STATE_GET_WARR_SPECS;
			taskArray[idx++] = STATE_GET_EMP_SEALS;
		}		
	}

	private void sendRequest(Integer state) {
		String maxDateChange;
		String sRes = "", title = "";
		try {
			String url = currentServerURI;
			mState = state;
			switch (state) {
			case STATE_GET_ENT:
                Log.d(TAG, "STATE_GET_ENT");
				url += ENT;
				title = "Предприятия";
				break;
			case STATE_GET_GDS:
                Log.d(TAG, "STATE_GET_GDS");
				url += GDS;
				title = "Товары/услуги";
				break;
			case STATE_GET_NEW_ENT:
                Log.d(TAG, "STATE_GET_NEW_ENT");
				url += ENT + "/" + CommonClass.readLastUpdate(mContext);
				title = "Предприятия";
				break;
			case STATE_GET_NEW_GDS:
                Log.d(TAG, "STATE_GET_NEW_GDS");
				url += GDS + "/" + CommonClass.readLastUpdate(mContext);
				title = "Товары/услуги";
				break;
			case STATE_GET_EMPS:
                Log.d(TAG, "STATE_GET_EMPS");
				url += EMPS;
				title = "Сотрудники";
				break;
			case STATE_GET_WARR_CONT:
                Log.d(TAG, "STATE_GET_WARR_CONT");
				url = url + WARR_CONT + wId;
				title = "Наряд";
                Log.d(TAG, url);
				break;
			case STATE_GET_WARRANTS:
                Log.d(TAG +"[852]", "STATE_GET_WARRANTS");
				if (keepWarrantTillReport)
					url += WARRANTS_REPORT + "/0";
				else
					url += WARRANTS + "/0";
				title = "Наряды";
				preMaxWarrantId = dbSch.getMaxWarrantId();
                Log.d(TAG, url);
				break;
			case STATE_GET_NEW_WARRANTS:
                Log.d(TAG, "STATE_GET_NEW_WARRANTS");
				preMaxWarrantId = dbSch.getMaxWarrantId();
				if (keepWarrantTillReport)
					url += WARRANTS_REPORT + "/" + preMaxWarrantId;
				else
					url += WARRANTS + "/" + preMaxWarrantId;
				title = "Новые наряды";
                Log.d(TAG, url);
				break;

			case STATE_GET_LAST_WARRANTS:
                Log.d(TAG, "STATE_GET_LAST_WARR_SPECS");
				maxDateChange = dbSch.getWarrantMaxDateChange();
				if (maxDateChange == null || maxDateChange.length() == 0)
					maxDateChange = "20141010101010";
				SharedPreferences prefs = getSharedPreferences(CommonClass.PREF_NAME, MODE_PRIVATE);
				String maxDelWarrDate = prefs.getString(MAX_DEL_WARR_DATE, "");
				if (maxDateChange.compareTo(maxDelWarrDate) < 0) {
					maxDateChange = maxDelWarrDate;
				}
				url += LAST_WARRANTS + maxDateChange;
                Log.d(TAG +"[884]", url);
				title = "Новые наряды";
				break;
			case STATE_GET_LAST_WARR_CONTRACTS:
				needSpecs = false;
                Log.d(TAG, "STATE_GET_LAST_WARR_SPECS");
				long count = dbSch.getWarrantNoLocalContractCount();
				if (count > 0) {
					needSpecs = true;
					url += WARR_CONTRACTS + "/0";
					title = "Договора";
					mState = STATE_GET_WARR_CONTRACTS;
				} else {
					title = "Новые договора";
					sRes = "Нет необходимости для загрузки договоров!";
					url = "";  // don't send request
				}
				break;
			case STATE_GET_LAST_WARR_SPECS:
                Log.d(TAG, "STATE_GET_LAST_WARR_SPECS");
				if (needSpecs) {
					url += WARR_SPECS + "/0";
					title = "Спецификации";
					mState = STATE_GET_WARR_SPECS;
                    Log.d(TAG,url );

				} else {
					maxDateChange = dbSch.getSpecsMaxDateChange();
					if (maxDateChange == null || maxDateChange.length() == 0)
						maxDateChange = "20101010101010";
					url += LAST_WARR_SPECS + maxDateChange;
					title = "Новая спецификация";
                    Log.d(TAG,url );
				}
				break;
	
			case STATE_GET_EMP_HW:
				url += EMP_HW;
				title = "Запчасти";
                Log.d(TAG, "STATE_GET_EMP_HW");
				break;
			case STATE_GET_EMP_SEALS:
				url += EMP_SEALS;
				title = "Пломбы";
                Log.d(TAG, "STATE_GET_EMP_SEALS");
				break;
			case STATE_GET_WARR_CONTRACTS:
				url += WARR_CONTRACTS + "/0";
				title = "Договора";
                Log.d(TAG, "STATE_GET_WARR_CONTRACTS");
				break;
			case STATE_GET_WARR_SPECS:
				url += WARR_SPECS + "/0";
				title = "Спецификации";
                Log.d(TAG, "STATE_GET_WARR_SPECS");
				break;
			case STATE_GET_LAST_REPLICATOR_START:
				url += LAST_REPLICATOR_START;
				title = "Репликатор";
                Log.d(TAG, "STATE_GET_LAST_REPLICATOR_START");
				break;
			case STATE_GET_STATE:
				url += STATE;
				title = "Справочник состояний";
                Log.d(TAG, "STATE_GET_STATE");
				break;
			case STATE_GET_REASON:
				url += REASON;
				title = "Справочник причин неисправности";
                Log.d(TAG, "STATE_GET_REASON");
				break;
			case STATE_GET_USER:
				url += USR;
				title = "Пользователь";
                Log.d(TAG, "STATE_GET_USER");
				break;
			case STATE_GET_MSGS:
				url += MSGS;
				title = "Сообщения";
				preMsgCount = dbSch.getRecipientNewMsgsCount(selfEmpId);
				break;
			case STATE_GET_NEW_MSGS:
				preMaxMsgId = dbSch.getRecipientMaxMsgsId(selfEmpId);
				preMaxMsgDateChange = dbSch.getRecipientMaxDateChange(selfEmpId);
				if (preMaxMsgDateChange == null || preMaxMsgDateChange.length() == 0)
					preMaxMsgDateChange = "20101010101010";
				//url += NEW_MSGS + preMaxMsgId;
				url += MOD_MSGS + preMaxMsgDateChange;
				title = "Измененные сообщения";
				break;
			default:
				mState = STATE_GET_WARRANTS;
				url += WARRANTS + "/0";
				title = "Наряды";
			}
			if (mState != STATE_FINISH) {
				if (D) Log.d(TAG, title);
				if (url.length() > 0) {
					// Simple GET
					isRequestProcessing = true;
					HttpGet searchRequest = new HttpGet(url);
					searchRequest.addHeader(BasicScheme.authenticate(
						new UsernamePasswordCredentials(authUserName, authUserPsw), "UTF-8", false));
					searchRequest.addHeader("Accept-Encoding", "gzip,deflate");
					RestTask task = new RestTask();
					task.setResponseCallback((ResponseCallback) mContext);
					task.execute(searchRequest);
					if (D) Log.d(TAG, "sendRequest: " + title);
				} else {
					if (D) Log.d(TAG, sRes);
					sendTaskResult(sRes);
					if (keepLog) dbSch.addLogItem(LogsRecord.DEBUG, new Date(), sRes);
					checkNextTask();										
				}
			}
			if (keepLog)
				dbSch.addLogItem(LogsRecord.DEBUG, new Date(),
						String.format("sendRequest (%d-%s)", mState, title));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), String.format(
					"sendRequest (%d-%s): %s", mState, title, e.getMessage()));
		}
	}

	private void postStateRequest(Integer state) {
        String title = "";
		try {
			String url = currentServerURI;
			String warr = "";
			mState = state;
			switch (state) {
			case STATE_POST_WARRANTS:
				url += WARRANTS;
				title = "Наряды Offline выгрузка";
				warr = dbSch.getModifiedWarrants();
               // Log.d(TAG, warr);
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
               // Log.d(TAG, "STATE_POST_EMP_WORK_TIME");
				break;
			case STATE_POST_EMP_LOCATIONS:
				url += EMPLOCATION;
				title = "Местоположение";
				warr = dbSch.getEmpLocationRecords();
				break;
			case STATE_POST_SPECS_INVENTORY_NUM:
				url += SPECS_INV;
				title = "Инвентарный номер";
				warr = dbSch.getSpecsInvRecords();
                Log.d(TAG + "--", url);
                Log.d(TAG+ "--", warr);
				break;
			case STATE_POST_SEAL_CERT:
				url += SEAL_CERT;
				title = "Справка опломбирования";
				warr = dbSch.getReady2SendSealCerts();
				break;
			default:
				mState = STATE_FINISH;
			}
			if (mState != STATE_FINISH) {
				if (D) Log.d(TAG, title);
				if (warr.length() > 0) {
					isRequestProcessing = true;
					// Simple POST
					HttpPost postRequest = new HttpPost(url);
					postRequest.addHeader(BasicScheme.authenticate(
							new UsernamePasswordCredentials(authUserName,
									authUserPsw), "UTF-8", false));
					StringEntity se = new StringEntity(warr, "UTF-8");
					se.setContentType("application/json;charset=UTF-8");
					se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
							"application/json;charset=UTF-8"));
					postRequest.setEntity(se);
					txJSONBytes += warr.length();
					
					RestTask task = new RestTask();
					task.setResponseCallback((ResponseCallback) mContext);
					task.execute(postRequest);
					if (D) Log.d(TAG, "postStateRequest: " + title);
				} else {
					// mState = STATE_FINISH;
					String sRes = "Нет изменений для передачи!";
					if (D) Log.d(TAG, sRes);
					sendTaskResult(sRes);
                    if (keepLog) dbSch.addLogItem(LogsRecord.DEBUG, new Date(), sRes);
					checkNextTask();
				}
                if (keepLog)
                    dbSch.addLogItem(LogsRecord.DEBUG, new Date(), String
                            .format("postStateRequest (%d-%s): size=%d", mState, title, warr.length()));
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), String.format(
					"postStateRequest (%d-%s): %s", mState, title, e.getMessage()));
		}
	}

	@Override
	public void onRequestSuccess(String response) {
		if (D)
			Log.d(TAG, "onRequestSuccess");
		isRequestResultError = false;
		String resp;
		if (response.length() > 50)
			resp = response.substring(1, 50);
		else
			resp = response;
		if (keepLog)
			dbSch.addLogItem(LogsRecord.INFO, new Date(), String.format(
					"(%d)response: (size=%d); %s", mState, response.length(), resp));

		rxJSONBytes += response.length();
		ParseJSONArray parseJSON = new ParseJSONArray();
		parseJSON.execute(response);
	}

	@Override
	public void onRequestError(Exception error) {
		isRequestResultError = true;
		isRequestProcessing = false;
		clearAllTasks();
		if (D) Log.d(TAG, "onRequestError: " + error.getMessage());

		if (keepLog) 
			dbSch.addLogItem(LogsRecord.ERROR, new Date(),
					String.format("(%d)error: %s", mState, error.getMessage()));

		if (sendToasts && mCallback != null && mCallback.get() != null) {
			mCallback.get().onSrvRequestError(error);
		}
	}


	private class ParseJSONArray extends AsyncTask<String, Integer, Boolean> {
		String returnString = null;

		@Override
		protected Boolean doInBackground(String... sJSONStr) {
			long id, idWarrLocal, idContract;
			int id_const, id_parent, ttype, gdstype;
			int num, jobCnt, idEnt, idSub, idOwner, idState, regl;
			Double lat, lng;
			Date dOpen;
			String sLat, sLng;
			String nm, addr, job, srl, inv;
			String openDate, sRem, bDate, eDate;
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			SimpleDateFormat showDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			boolean lastTask = isNoTaskLeft();
			try {
				JSONArray records = new JSONArray(sJSONStr[0]);
				int recLength = records.length();

				returnString = "Найдено: " + recLength;
				if (mState == STATE_GET_WARR) {
					returnString = returnString + " нарядов.";
                    Log.d(TAG, returnString);
				} else {
					returnString = returnString + " записей.";

				}
				if (keepLog)
					dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "ParseJSONArray: " + returnString);

				if (mState == STATE_GET_WARRANTS) {
					dbSch.emptyAllWarrantTables();
				} else if (mState == STATE_GET_EMP_HW) {
					dbSch.emptyEmpHarwareTable();
				} else if (mState == STATE_GET_WARR_CONTRACTS) {
					id =  dbSch.getContractsCount();
					dbSch.emptyContractTable();
				} else if (mState == STATE_GET_EMP_SEALS) {
					dbSch.emptySealTable();
				} else if (mState == STATE_GET_STATE) {
					dbSch.emptyStateTable();
				} else if (mState == STATE_GET_REASON) {
					dbSch.emptyReasonTable();
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
					needEmpPhotoLoad = false;
					setEmpPhotoLoadPrefs();
					dbSch.emptyTable(EmpTable.TABLE_NAME);
					dbSch.insEmpsBulkJSON(records);
					cntr = dbSch.getEmpsCount();
					break;
				}
				if (mState != STATE_GET_ENT && mState != STATE_GET_GDS) {
					long startTime = System.currentTimeMillis();
					for (int i = 0; i < records.length(); i++) {
						JSONObject warrant = records.getJSONObject(i);
						switch (mState) {
						case STATE_GET_NEW_ENT:
							id = warrant.getInt("id");
							id_parent = warrant.getInt("id_p"); 
							sLat = warrant.getString("lat"); 
							lat = (double)Float.parseFloat(sLat.replace(",", ".")); 
							sLng = warrant.getString("lng"); 
							lng = (double)Float.parseFloat(sLng.replace(",", ".")); 
							nm = warrant.getString("nm"); 
							addr = warrant.getString("addr"); 
							dbSch.addEntItem(id, id_parent, lat, lng, addr, nm, true);							
							break;
						case STATE_GET_NEW_GDS:
							id = warrant.getInt("id"); 
							id_parent = warrant.getInt("id_p"); 
							ttype = warrant.getInt("ttype"); 
							gdstype = warrant.getInt("gdstype"); 
							nm = warrant.getString("nm"); 
							dbSch.addGdsItem(id, id_parent, ttype, gdstype, nm, true);							
							break;							
						case STATE_GET_WARR_CONT:
							id = warrant.getInt("id");
							nm = warrant.getString("nm");
							jobCnt = warrant.getInt("jobcnt");
							job = warrant.getString("job");
							srl = warrant.getString("srl");
							break;
						case STATE_GET_LAST_WARRANTS:
						case STATE_GET_NEW_WARRANTS:	
						case STATE_GET_WARRANTS:
							try {
								CommonClass.lastErrorMsg = "";
								id = warrant.getLong("id");
								num = warrant.getInt("num");
								idContract = warrant.getLong("id_c");
								idEnt = warrant.getInt("ident");
                               // Log.d(TAG," REGL = "+ warrant.getString("regl"));
								regl = warrant.getInt("regl");
								idSub = warrant.getInt("idsubent");
								idState = warrant.getInt("id_state");
								openDate = warrant.getString("d_open");
								String chDate = warrant.getString("d_ch");
								jobCnt = warrant.getInt("jobcnt");
								dOpen = sDateFormat.parse(openDate);
								idOwner = warrant.getInt("owner");
								sRem = warrant.getString("rem");
								int recState = warrant.getInt("r_st");
								int reported = warrant.getInt("rep");
								int toSign = warrant.getInt("to_sign");
								String signDate = warrant.getString("d_sign");
								long id_sign_user = warrant.getLong("id_user_sign");
								if (D) Log.d(TAG, "Rem= " + sRem);
                              //  Log.d(TAG,idState + " == "+  WarrantRecord.W_STATE_NEW_DISCARD );
                              //  Log.d(TAG,recState + " == "+  WarrantRecord.WARRANT_DELETED );
                              //  Log.d(TAG +" != ",String.valueOf(reported) );
                               //Log.d(TAG," REGL = "+ String.valueOf(warrant.getInt("regl")));
								if (idState == WarrantRecord.W_STATE_NEW_DISCARD || recState == WarrantRecord.WARRANT_DELETED ||
										reported != 0) {
									deleteWarrant(id, chDate);
									idWarrLocal = 0;
                               //     Log.d(TAG," REGL = "+ String.valueOf(warrant.getInt("regl")));
                                //    Log.d(TAG,"delete 1");
								} else if (idState == WarrantRecord.W_STATE_COMPLETED || idState == WarrantRecord.W_STATE_CLOSED) {
									boolean jobMarkStarted = dbSch.getEmpWorkLastMark(id) == 0;
									if (keepWarrantTillReport || jobMarkStarted) {
										idWarrLocal = dbSch.addWarrantItem(id, num, idContract, idEnt, idSub,
												idState, dOpen, sRem, idOwner, toSign, signDate, id_sign_user, chDate
                                             ,regl
                                        );
                                  //      Log.d(TAG,"add 2");
									} else {
										deleteWarrant(id, chDate);
										idWarrLocal = 0;
                                  //      Log.d(TAG,"delete 3");
									}									
								} else
									idWarrLocal = dbSch.addWarrantItem(id, num, idContract, idEnt, idSub,
										idState, dOpen, sRem, idOwner, toSign, signDate, id_sign_user, chDate
                                            , regl
                                    );
                               // Log.d(TAG + " 0 >", String.valueOf(idWarrLocal));

								boolean warr_state_done = true;
								String warrContStr = "";
								String warrUnitStr = "";
								String warrRepairStr = "";
								if (idWarrLocal > 0) {
									try {
										long idCont, idContLocal, eqSpec;
										int idGds, idJob, jobStatus, eqStatus, tblMode;
										boolean warr_cont_state_done;
										String eqSrl, eqInv;
										String remCont;
										JSONArray warrContArray = warrant.getJSONArray("warr_cont");
										for (int j = 0; j < warrContArray.length(); j++) {
											JSONObject warrCont = warrContArray.getJSONObject(j);
											warr_cont_state_done = false;
											idCont = warrCont.getLong("id");
											idGds = warrCont.getInt("id_gds");
											idJob = warrCont.getInt("id_job");
											jobStatus = warrCont.getInt("job_s");
											eqStatus = warrCont.getInt("eq_s");
											eqSpec = warrCont.getLong("eq_spec");
											eqSrl = warrCont.getString("srl");
											eqInv = warrCont.getString("inv");
											tblMode = warrCont.getInt("tbl");
											remCont = warrCont.getString("rem");
											if (idState == 4)
												idContLocal = dbSch.delWarrContItem(idCont);

											else
												idContLocal = dbSch.addWarrContItem(idCont, id, idGds, eqStatus,
													eqSpec, eqSrl, eqInv, idJob, jobStatus, tblMode, remCont);
											if (idContLocal > 0) {
												warrContStr += warrCont.getInt("id") + "; ";
												long idUnit, idUnitLocal = 0;
												int idJobU;
												String remUnit;
												
												JSONArray warrUnitArray = warrCont.getJSONArray("warr_unit");
												for (int k = 0; k < warrUnitArray.length(); k++) {
													JSONObject warrUnit = warrUnitArray.getJSONObject(k);
													idUnit = warrUnit.getLong("id");
													if (idUnit > 0) {
														idJobU = warrUnit.getInt("id_job");
														if (idJobU == idJob) warr_cont_state_done = true;
														remUnit = warrUnit.getString("rem");
														tblMode = warrUnit.getInt("tbl");
														if (idState == 4)
															idUnitLocal = dbSch.delWarrUnitItem(idUnit);
														else													
															idUnitLocal = dbSch.addWarrUnitItem(idUnit,
																		idContLocal, idJobU, tblMode, remUnit);
													}
													warrUnitStr += idUnit + "; ";
													
													if (idUnitLocal > 0) {
														long wrId;
														int idRepair;
														double cost;
														double quantity;
														String rUnits;
														JSONArray warrRepairArray = warrUnit.getJSONArray("warr_repair");
														for (int l = 0; l < warrRepairArray.length(); l++) {
															JSONObject warrRepair = warrRepairArray.getJSONObject(l);
															wrId = warrRepair.getLong("id");
															if (wrId > 0) {
																idRepair = warrRepair.getInt("id_repair");
																cost = warrRepair.getDouble("cost");
																quantity = warrRepair.getDouble("quant");
																rUnits = warrRepair.getString("units");
																tblMode = warrUnit.getInt("tbl");
																if (idState == 4)
																	dbSch.delWarrRepairItem(wrId);
																else													
																	dbSch.addWarrRepairItem(wrId, id, 
																		idUnitLocal, idRepair, tblMode,
																		cost, quantity);
															}
															warrRepairStr += wrId + "; ";
														}
													}
												}
											}
											warr_state_done = warr_state_done && warr_cont_state_done;
										}
										int wState = (warr_state_done) ? WarrantRecord.W_DONE : WarrantRecord.W_IN_PROGRESS; 
										dbSch.setWarrantLocalState(id, wState);
									} catch (JSONException e) {
										dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), String.format(
												"STATE_GET_WARRANTS: %s", e.getMessage()));													
										e.printStackTrace();
										throw new RuntimeException(e);
									}
								}
							} catch (Exception e) {
								nm = e.getMessage();
								CommonClass.lastErrorMsg = nm;
								dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), "GET_WARRS error: " + nm);
							}
							if (!ConnectionManagerService.getFirstRun())
								gettingAllConstTables = false;
							break;
						case STATE_GET_EMP_HW:
							id_const = warrant.getInt("id");
							sLng = warrant.getString("cnt");
							lng = (double) Float.parseFloat(sLng.replace(",", "."));
							dbSch.addEmpHWItem(id_const, lng);
							break;
						case STATE_GET_EMP_SEALS:
							id_const = warrant.getInt("id");
							inv = warrant.getString("inum");
							nm = warrant.getString("num");
							srl = warrant.getString("srl");
							openDate = warrant.getString("d_ch");
							bDate = warrant.getString("id_ch");
							dbSch.addSealItem(id_const, srl, nm, inv, openDate, bDate);
							break;
						case STATE_GET_LAST_WARR_CONTRACTS:
						case STATE_GET_WARR_CONTRACTS:
							Date dClose;
							try {
								CommonClass.lastErrorMsg = "";
								id_const = warrant.getInt("id");
								idEnt = warrant.getInt("ident");
								idSub = warrant.getInt("ident_u");
								idOwner = warrant.getInt("owner");
								idState = warrant.getInt("id_state");
								nm = warrant.getString("name");
								job = warrant.getString("no");
								openDate = warrant.getString("d_b");
								if (openDate.length() > 0)
									dOpen = sDateFormat.parse(openDate);
								else
									dOpen = null;
								openDate = warrant.getString("d_e");
								if (openDate.length() > 0)
									dClose = sDateFormat.parse(openDate);
								else
									dClose = null;
								dbSch.addContractItem(id_const, idEnt, idSub, idState, idOwner, nm, job, dOpen, dClose);
							} catch (Exception e) {
								nm = e.getMessage();
								CommonClass.lastErrorMsg = nm;
								dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), "GET_WARR_CONTRACTS error: "+ nm);
							}							
							break;
						case STATE_GET_LAST_WARR_SPECS:
						case STATE_GET_WARR_SPECS:
							Date dBegin, dEnd;
							try {
								CommonClass.lastErrorMsg = "";
								id_const = warrant.getInt("id");
								idEnt = warrant.getInt("ident");
								idContract = warrant.getInt("id_c");
								long idGds = warrant.getInt("id_eq");								
								idSub = warrant.getInt("tmp");
								nm = warrant.getString("fisk_num");
								job = warrant.getString("place");
								srl = warrant.getString("srl");
								inv = warrant.getString("inv");
								String ipModem = warrant.getString("ip_mdm");
								String ipCash = warrant.getString("ip_cash");
								int numCash = warrant.getInt("num_cash");
								int fSeal = warrant.getInt("f_seal");
								openDate = warrant.getString("d_c");
								if (openDate.length() > 0)
									dOpen = sDateFormat.parse(openDate);
								else
									dOpen = null;
								openDate = warrant.getString("d_b");
								if (openDate.length() > 0)
									dBegin = sDateFormat.parse(openDate);
								else
									dBegin = null;
								openDate = warrant.getString("d_e");
								if (openDate.length() > 0)
									dEnd = sDateFormat.parse(openDate);
								else
									dEnd = null;
								dbSch.addSpecItem(id_const, idEnt, idContract, idGds, idSub, openDate,
									dBegin, dEnd, nm, job, srl, inv, ipModem, ipCash, numCash, fSeal);
							} catch (Exception e) {
								nm = e.getMessage();
								CommonClass.lastErrorMsg = nm;
								dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), "GET_WARR_SPECS error: "+ nm);
							}
							break;
						case STATE_GET_STATE:
							id_const = warrant.getInt("id");
							id_parent = warrant.getInt("id_doc");
							nm = warrant.getString("nm");
							dbSch.addStateItem(id_const, id_parent, /* ttype, */ nm);
							break;
						case STATE_GET_REASON:
							id_const = warrant.getInt("id");
							id_parent = warrant.getInt("sign");
							nm = warrant.getString("nm");
							openDate = warrant.getString("d_ch");
							dbSch.addReasonItem(id_const, id_parent, nm, openDate);
							break;
						case STATE_GET_USER:
							id = warrant.getInt("id");
							dbSch.addUserItem(id);
							break;
						case STATE_GET_LAST_REPLICATOR_START:
							openDate = warrant.getString("r_time");
							if (openDate.length() > 0)
								dOpen = sDateFormat.parse(openDate);
							else
								dOpen = null;
							CommonClass.writeReplTime(mContext, dOpen);
							break;
						case STATE_GET_NEW_MSGS:
						case STATE_GET_MSGS:
							try {
								CommonClass.lastErrorMsg = "";
								long idExt = warrant.getLong("id");
								long usId = warrant.getLong("us_id");
								id_parent = warrant.getInt("id_sender");
								int id_recipient = warrant.getInt("id_recip");
								idState = warrant.getInt("id_state");
								openDate = warrant.getString("m_date");
								dOpen = sDateFormat.parse(openDate);
								String mDateCh = warrant.getString("m_date_ch");
								addr = warrant.getString("subj");
								nm = warrant.getString("msg");
								int status = warrant.getInt("status");
								int sent = warrant.getInt("sent");// if sent != 0 photo не передается
								int a_size = warrant.getInt("a_sz");
								String a_nm = warrant.getString("a_nm");
								String sPhoto = warrant.getString("photo");
								byte[] photo = Base64.decode(sPhoto, Base64.DEFAULT);								
								if (usId > 0) {
									if (!dbSch.exchandeMsgExtId(usId, idExt))
										if (sent == 0)
											dbSch.addMsgItem(idExt, id_parent, id_recipient, idState, 
													status, a_size, dOpen, mDateCh, addr, nm, a_nm, photo);
										else
											dbSch.addMsgItem(idExt, id_parent, id_recipient, idState, 
													status, a_size, dOpen, mDateCh, addr, nm, a_nm);
								} else {
									if (sent == 0)
										dbSch.addMsgItem(idExt, id_parent, id_recipient, idState, 
												status, a_size, dOpen, mDateCh, addr, nm, a_nm, photo);
									else	
										dbSch.addMsgItem(idExt, id_parent, id_recipient, idState, 
												status, a_size, dOpen, mDateCh, addr, nm, a_nm);
								}
							} catch (Exception e) {
								nm = e.getMessage();
								CommonClass.lastErrorMsg = nm;
								dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), "GET_MSGS error: "+ nm);
							}							
							break;
						case STATE_POST_WARRANTS:
							nm = warrant.getString("result");
                            id_const = warrant.getInt("code");
                            mNeed2RefreshWarrantsList = true;
                            Log.d(TAG+"[1521]",String.valueOf(mNeed2RefreshWarrantsList));

							break;
						case STATE_POST_MSGS:
							nm = warrant.getString("result");
							id_const = warrant.getInt("code");
							JSONArray msgResultArray = null;
							if (id_const == 0) {
								try {
									msgResultArray = warrant.getJSONArray("marks");
									for (int m = 0; m < msgResultArray.length(); m++) {
										JSONObject msgResult = msgResultArray.getJSONObject(m);
										long local_id = msgResult.getLong("id");
										long ext_id = msgResult.getLong("id_ext");
										if (local_id > 0 && ext_id > 0) // update record
											dbSch.setMsgExtId(local_id, ext_id);
									}
								} catch (Exception e) {									
									nm = e.getMessage();
									CommonClass.lastErrorMsg = nm;
									dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), "POST_MSGS error: "+ nm);
								}																
							}
							else {
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "POST_MSGS error: "
										 + id_const + "; " + nm);
							}
                            Log.d(TAG, "STATE_POST_MSGS");
							break;
						case STATE_POST_EMP_WORK_TIME:
							nm = warrant.getString("result");
							id_const = warrant.getInt("code");
							JSONArray marksResultArray = null;
							if (id_const == 0) {
								try {
									marksResultArray = warrant.getJSONArray("marks");
									for (int m = 0; m < marksResultArray.length(); m++) {
										JSONObject markResult = marksResultArray.getJSONObject(m);
										long local_id = markResult.getLong("id");
										long ext_id = markResult.getLong("id_ext");
										if (local_id > 0 && ext_id > 0) // update record
											dbSch.setEmpWorkMarksExtId(local_id, ext_id);
									}
								} catch (Exception e) {									
									nm = e.getMessage();
									CommonClass.lastErrorMsg = nm;
									dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), "POST_EMP_WORK_TIME error: "+ nm);
								}
							} else {
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "POST_EMP_WORK_TIME error: "
										+ id_const + "; " + nm);
							}
                            Log.d(TAG, "STATE_POST_EMP_WORK_TIME");
							break;
						case STATE_POST_EMP_LOCATIONS:
							nm = warrant.getString("result");
							id_const = warrant.getInt("code");
							if (id_const == 0)
								dbSch.setLocationModified(0);
							else
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "POST_EMP_LOCATIONS error: "
										 + id_const + "; " + nm);
                            Log.d(TAG, "STATE_POST_EMP_LOCATIONS");
							break;
						case STATE_POST_SPECS_INVENTORY_NUM:
							nm = warrant.getString("result");
							id_const = warrant.getInt("code");
							if (id_const == 0) {								
								try {
									marksResultArray = warrant.getJSONArray("recs");
									for (int m = 0; m < marksResultArray.length(); m++) {
										JSONObject recResult = marksResultArray.getJSONObject(m);
										long ext_id = recResult.getLong("id_ext");
										if (ext_id > 0) // update record
											dbSch.setSpecsIdModified(ext_id, 0);
									}
								} catch (Exception e) {									
									nm = e.getMessage();
									CommonClass.lastErrorMsg = nm;
									dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), "POST_SPECS_INVENTORY_NUM error: "+ nm);
								}
							} else {
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "POST_SPECS_INVENTORY_NUM error: "
										 + id_const + "; " + nm);
							}
                        //    Log.d(TAG, "-->STATE_POST_SPECS_INVENTORY_NUM");
							break;
						case STATE_POST_SEAL_CERT:
							nm = warrant.getString("result");
							id_const = warrant.getInt("code");
							if (id_const == 0) {
								try {
									marksResultArray = warrant.getJSONArray("recs");
									for (int m = 0; m < marksResultArray.length(); m++) {
										JSONObject recResult = marksResultArray.getJSONObject(m);
										long local_id = recResult.getLong("id");
										long ext_id = recResult.getLong("id_ext");
										if (ext_id > 0) // update record
											dbSch.updateSealCertIdExt(local_id, ext_id);
									}
								} catch (Exception e) {									
									nm = e.getMessage();
									CommonClass.lastErrorMsg = nm;
									dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), "POST_POST_SEAL_CERT error: "+ nm);
								}																
							} else {
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "STATE_POST_SEAL_CERT error: "
										 + id_const + "; " + nm);
							}
                            Log.d(TAG, "STATE_POST_SEAL_CERT");
							break;
						default:
						}
						publishProgress((int) (i * 100 / recLength));
					}
					long endtime = System.currentTimeMillis();
                    String msg = "Time to insert records: "+ (endtime - startTime) + "mc";
                    dbSch.addLogItem(LogsRecord.WARNING, new Date(), msg);
					Log.i(TAG, msg);
				}
			} catch (Exception e) {
				dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), String.format(
						"ParseJSONArray: %s", e.getMessage()));													
				e.printStackTrace();
				returnString = "Exception: " + e.getMessage();
				return false;
			} finally {
				switch (mState) {
				case STATE_GET_MSGS:
					long MsgCount = dbSch.getRecipientNewMsgsCount(selfEmpId);
					if (MsgCount > preMsgCount && notifyNewMsg) {
						sendNewMsgNotification();
					}
					break;
				case STATE_GET_NEW_MSGS:
					long MsgId = dbSch.getRecipientMaxMsgsId(selfEmpId);
					if (MsgId > preMaxMsgId && notifyNewMsg) {
						sendNewMsgNotification();
					}
					break;
				case STATE_GET_NEW_WARRANTS:
				case STATE_GET_WARRANTS:
					long maxWarrId = dbSch.getMaxWarrantId();
					if (maxWarrId > preMaxWarrantId
                            && notifyNewWarrant
                            ) {
						sendNewWarrantNotification();
					}
                    sendRefreshWarrantsMessage();
                  //  Log.d(TAG + "[1667]", String.valueOf(maxWarrId));
                   // Log.d(TAG + "[1667]", String.valueOf(preMaxWarrantId));
                   // Log.d(TAG + "[1667]", String.valueOf(notifyNewWarrant));
					break;
				}
				mState = STATE_FINISH;
			}
			return true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		protected void onPostExecute(Boolean success) {
			if (D)
				Log.d(TAG, "onPostExecute");

			checkNextTask();
		}
	}
	
	private void deleteWarrant(long id, String chDate) {
		long idWarrLocal = dbSch.delWarrantItem(id);
		SharedPreferences prefs = getSharedPreferences(CommonClass.PREF_NAME, MODE_PRIVATE);
		String maxDelWarrDate = prefs.getString(MAX_DEL_WARR_DATE, "");
		if (chDate.compareTo(maxDelWarrDate) > 0) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(MAX_DEL_WARR_DATE, chDate);
			editor.commit();
		}		
	}

	private void sendNewMsgNotification() {
		Context context = getApplicationContext();
		CharSequence contentTitle = "Получено новое сообщение";
		CharSequence contentText = "Просмотр сообщений";
		Intent msgIntent = new Intent(this, EmpMessagesActivity.class);
		PendingIntent intent = PendingIntent.getActivity(
				ConnectionManagerService.this, 0, msgIntent,
				Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		n_msg.defaults |= Notification.DEFAULT_SOUND;
		n_msg.flags |= Notification.FLAG_AUTO_CANCEL;
		n_msg.setLatestEventInfo(context, contentTitle, contentText, intent);
		mNManager.notify(MSG_NOTIFY_ID, n_msg);
	}

	private void sendNewWarrantNotification() {
		Context context = getApplicationContext();
		CharSequence contentTitle = "Получено новое задание";
		CharSequence contentText = "Просмотр нарядов";
		Intent msgIntent = new Intent(this, WarrantsActivity.class);
		PendingIntent intent = PendingIntent.getActivity(
				ConnectionManagerService.this, 0, msgIntent,
				Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT); // FLAG_ACTIVITY_NEW_TASK
		n_warrants.defaults |= Notification.DEFAULT_SOUND;
		n_warrants.flags |= Notification.FLAG_AUTO_CANCEL;
		n_warrants.setLatestEventInfo(context, contentTitle, contentText, intent);
		mNManager.notify(WARRANT_NOTIFY_ID, n_warrants);
	}

	private void sendNotification() {

	}
	private void checkNextTask() {
		if (isNoTaskLeft()) {
			mState = STATE_FINISH;
			sendNotification();
			isRequestProcessing = false;
			sendTaskResult("Операция выполнена успешно!");
			sendActiveTask(mState);
			if (keepLog)
				dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "NoTaskLeft");
		} else {
			sendNextTask();
		}
	}

	private void sendTaskResult(String res) {
        Log.d(TAG+"[1753]", String.valueOf(mNeed2RefreshWarrantsList));
        if (mNeed2RefreshWarrantsList)
            sendRefreshWarrantsMessage();
		if (sendToasts && mCallback != null && mCallback.get() != null) {
			mCallback.get().onSrvRequestSuccess(res);
		}
	}

	private void sendActiveTask(int Task) {
		if (sendToasts && mCallback != null && mCallback.get() != null) {
			mCallback.get().onSrvTaskChanged(Task);
		}
	}

    private void sendRefreshWarrantsMessage() {
        Intent intent = new Intent(REFRESH_LIST_ACTION);
        lbm.sendBroadcast(intent);
    }

    private void sendRefreshMessage(long idWrk, int res) {
        Intent intent = new Intent(REFRESH_LIST_ACTION);
        lbm.sendBroadcast(intent);
    }


}
