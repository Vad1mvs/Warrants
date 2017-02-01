package com.utis.warrants;

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

import com.utis.warrants.R.string;
import com.utis.warrants.RestTask.ResponseCallback;
import com.utis.warrants.location.MyGLocationService;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity implements ResponseCallback {
	private static final boolean D = true; 
	private static final String TAG = "WelcomeActivity";

	public static final int STATE_POST_AUTH = 1; 
	public static final int STATE_GET_VER = 2; 
	public static final int STATE_POST_INFO = 3;
    public static final String DOWNLOAD_SERVER_URI_SSL = "https://85.238.112.13:443/contenti";
	private String loginURI, helpURI, serverURI, verURI, infoURI;
	private String serverMode;
	private View authPswButton, authSimpleButton;
	private TextView mRequestView, mVersionView;
	Context mContext;
    private TelephonyManager tm;
    private String mPhoneNumber, mIMEI, mVersion = "";
    private int mVerCode;
    private DBSchemaHelper dbSch;
	private ProgressDialog mProgress;
	private int mState;
	private boolean verChecked = false;
	SharedPreferences prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneNumber = tm.getLine1Number();
		mIMEI = tm.getDeviceId();
		String deviceId = Settings.System.getString(getContentResolver(),
                Settings.System.ANDROID_ID);
		String srl = android.os.Build.SERIAL;
		dbSch = DBSchemaHelper.getInstance(this);
//		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs = getSharedPreferences(CommonClass.PREF_NAME, MODE_PRIVATE);
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mVersion = pInfo.versionName;
			mVerCode = pInfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		mVersionView = (TextView) findViewById(R.id.textViewVer);
		mVersionView.setText(getString(string.title_version) + mVersion);
		mRequestView = (TextView) findViewById(R.id.requestView);
		
		authPswButton = findViewById(R.id.auth_name_button); 
		authPswButton.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
		        	Intent runLoginActivity = new Intent(mContext, LoginActivity.class);
		        	startActivity(runLoginActivity);
				}
			}
		);
		authPswButton.setEnabled(false);
		
		authSimpleButton = findViewById(R.id.auth_simple_button);
		authSimpleButton.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					attemptAuthSimple();
				}
			}
		);
	}

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        
        serverMode = ConnectionManagerService.getServerMode(this);
		serverURI = ConnectionManagerService.getServerURI(this);
		loginURI = serverURI + ConnectionManagerService.LOGIN_IMEI_URI;
		helpURI = serverURI + ConnectionManagerService.HELP_URI; 
		verURI = serverURI + ConnectionManagerService.REST_WARRANTS_URI;
		infoURI = serverURI + ConnectionManagerService.REST_WARRANTS_URI + ConnectionManagerService.INFO;

		mRequestView.setText(serverMode + ": " + serverURI);
        Log.d(TAG, mIMEI);
        Log.d(TAG, mPhoneNumber);
        Log.d(TAG, loginURI);
        Log.d(TAG, helpURI);
        Log.d(TAG, verURI);
        Log.d(TAG, infoURI);

		if (!verChecked) {
		}
    } 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
        switch (item.getItemId()) {
			case R.id.action_settings:
				if (D) Log.d(TAG, "onOptions action_settings");
				mRequestView.setTextColor(Color.BLACK);
				intent = new Intent(this, ConnectSettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_call_developer:
				intent = new Intent(Intent.ACTION_CALL);
				//intent.setData(Uri.parse("tel:" + CommonClass.developerPhoneNumber));
				startActivity(intent);
				return true;
			case R.id.action_full_exit:
				fullExit();
				return true;
			case R.id.action_show_logs:
				showLogs();
				return true;
			case R.id.action_help:
				showHelp();
				return true;
            case R.id.action_soft_download:
                showWebSoft();
                return true;
        }
        return false;
	}

	private void showLogs() {
		Intent intent = new Intent(this, LogsActivity.class);
		startActivity(intent);
	}

	private void showHelp() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(helpURI));
		startActivity(i);
	}
	
	private void fullExit() {
		Intent serviceIntent = new Intent(this, ConnectionManagerService.class);
		stopService(serviceIntent); 
		Intent locationServiceIntent = new Intent(this, MyGLocationService.class);
		stopService(locationServiceIntent);
		finish();
	}
            	
	private void attemptAuthSimple() {
		mRequestView.setTextColor(Color.BLACK);
		mRequestView.setText("");
		postStateRequest(STATE_POST_AUTH);
	}
	
	private void sendRequest() {
		mState = STATE_GET_VER;
		String url = verURI + ConnectionManagerService.VER;

		HttpGet searchRequest = new HttpGet(url);
		searchRequest.addHeader(BasicScheme.authenticate(
				new UsernamePasswordCredentials(mIMEI, WarrListActivity.MOBILE), "UTF-8", false));
		RestTask task = new RestTask();
		task.setResponseCallback((ResponseCallback) mContext);
		task.execute(searchRequest);
        Log.d(TAG, url);
	}
	
	private void postStateRequest(int state) {
		String url = ""; 
		String warr = "";
		try {
			mState = state;
			switch (mState) {
			case STATE_POST_AUTH:
				url = loginURI;
	        	warr = "Login";
				break;
			case STATE_POST_INFO:
				url = infoURI;
//	        	warr = "Info";
	        	JSONObject header = new JSONObject();
	        	header.put("dev_model", android.os.Build.MODEL); // Device model
	        	header.put("dev_ver", android.os.Build.VERSION.RELEASE); // Device OS version
	        	header.put("soft_num", mVersion);
	        	warr = header.toString();
				break;
			}
	        if (warr.length() > 0 && url.length() > 0) {
				//Simple POST 
				HttpPost postRequest = new HttpPost(url); 
				postRequest.addHeader(BasicScheme.authenticate(
						new UsernamePasswordCredentials(mIMEI, ConnectionManagerService.MOBILE), "UTF-8", false));

				StringEntity se = new StringEntity(warr, "UTF-8");
	        	se.setContentType("text/plain;charset=UTF-8");
	        	se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "text/plain;charset=UTF-8"));
	        	postRequest.setEntity(se);
	        								
				RestTask task = new RestTask(); 
				task.setResponseCallback((ResponseCallback) mContext); 
				task.execute(postRequest); 
				//Display progress to the user 
				mProgress = ProgressDialog.show(this, getString(string.title_auth), getString(string.m_wait_answer), true);
	        }
		} catch (Exception e) { 
        	if (D) Log.e(TAG, e.getMessage());
        	mRequestView.setTextColor(Color.RED);
        	mRequestView.setText(e.getMessage());
		}		
	}	

	@Override
	public void onRequestSuccess(String response) {
		if (D) Log.d(TAG, "onRequestSuccess");
		boolean resultOK = false;
		String uName = "";
		int res_code;
		long uId = 0;
		int verCode = 0;
		String res = "";
		
		if(mProgress != null) { //Clear progress indicator 
			mProgress.dismiss(); 
		} 
		try {
			JSONArray records = new JSONArray(response);
			for (int i = 0; i < records.length(); i++) {
				JSONObject warrant = records.getJSONObject(i);			
				switch (mState) {
				case STATE_POST_AUTH:
                    res = warrant.getString("result");
					res_code = warrant.getInt("code");
					uName = warrant.getString("name");
					uId = warrant.getLong("id");
					resultOK = res_code == 0;					
					break;
				case STATE_POST_INFO:
					res = warrant.getString("result");
					res_code = warrant.getInt("code");
					resultOK = res_code == 0;
					break;
				case STATE_GET_VER:
					verCode = warrant.getInt("ver_code");
					CommonClass.newVerAvail = verCode > mVerCode; 
					if (CommonClass.newVerAvail) {
						Toast.makeText(mContext, getString(R.string.m_update_avail), Toast.LENGTH_LONG).show();
					}
					break;
				}				
			}		
		} catch (JSONException e) {
			Log.d(TAG, "Exception: " + e.getMessage()+ " " + response);
			res =/* e.getMessage() + " "+ */response;
		}
		switch (mState) {
		case STATE_POST_AUTH:
			if (resultOK) {
				long cntr = dbSch.getUserCount();
				if (cntr <= 0) {
					SQLiteDatabase sd = dbSch.getWritableDatabase();
					dbSch.createUserTable(sd);
				} else {
					dbSch.EmptyUserTable();
				}
				dbSch.addUserItem(uId);
				Toast.makeText(mContext, uName  +" "+ res + "!\n" + getString(R.string.msg_welcome), Toast.LENGTH_LONG).show();
				postStateRequest(STATE_POST_INFO);
			} else {
				mRequestView.setTextColor(Color.MAGENTA);
				mRequestView.setText(getString(R.string.msg_auth_error)+ ": "+ res);
			}
			break;
		case STATE_POST_INFO:
			if (resultOK) {
				sendRequest();
			}
			break;
		case STATE_GET_VER:
			showWarrants();
			break;
		}
	}

	@Override
	public void onRequestError(Exception error) {
		//Clear progress indicator 
		if(mProgress != null) { 
			mProgress.dismiss(); 
		} 
		if (D) Log.d(TAG, "onRequestError: "+ error.getMessage());
		mRequestView.setTextColor(Color.RED);
		mRequestView.setText(error.getMessage());
	}

    private void showWebSoft() {
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(DOWNLOAD_SERVER_URI_SSL + "/warrants.php"));
        startActivity(webIntent);
    }
	
	private void showWarrants() {
		boolean needAllWarrants = !ConnectionManagerService.getFirstRun() && (dbSch.getModifiedWarrantCount() == 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(WarrantsActivity.GET_ALL_WARRANTS, needAllWarrants);
		editor.commit();
		//Intent runWarrantsListActivity = new Intent(this, WarrTestList.class);
		Intent runWarrantsListActivity = new Intent(this, WarrantsActivity.class);
        startActivity(runWarrantsListActivity);
	}

}
