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

import com.utis.warrants.RestTask.ResponseCallback;
import com.utis.warrants.record.SignUserRecord;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity implements ResponseCallback {
	private static final boolean D = true; 
	private static final String TAG = "LoginActivity";
	private static final int STATE_GET_USER = 1; 
	private static final int STATE_GET_SIGN_USER = 2; 
	private static final int STATE_GET_ENT_SIGN_EMP = 3;
	private static final int STATE_POST_SIGN_AUTH = 4;
	private static final int STATE_POST_SIGN_WARRANT = 5;
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private static final String LOGIN_URI_SSL = WarrListActivity.SERVER_URI_SSL + WarrListActivity.LOGIN_PSW_URI;	
	private static final String LOGIN = "/login";
	private static final String CHANGE_PSW = "/change_psw";
	private static final String DEBUG_USER_NAME = "CONTENT_86";
	private static final String PSW_BASE = "CONTENTONLINE_";
	
	private UserLoginTask mAuthTask = null;
	private String signGetEmpURI = "";
	// Values for email and password at the time of the login attempt.
	public String mEmail, mPassword, mNewPassword;
	public Context mContext;
	// UI references.
	private Spinner mUserSpinner;
	private EditText mEmailView, mPasswordView;
	private EditText mPasswordNewView, mPasswordNew2View;
	private View signInButton, changePswButton, okButton;
	private View cancelButton, okCancelView, signInView;
	private View mLoginFormView, mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private DBSchemaHelper dbSch;
    private TelephonyManager tm;
    private String mSIMId, mPhoneNumber, mIMEI;
    private String returnString, idEnt, idWarrant, uLogin = "";
    private boolean signMode = false;
    private int mState = 0;
    private ProgressDialog mProgress;
//    private ArrayAdapter<SignUserRecord> mDBArrayAdapter;
    private CustomSignUserAdapter mDBArrayAdapter;
    private SignUserRecord signUserSelected;
	
    
    private class CustomSignUserAdapter extends ArrayAdapter<SignUserRecord> {
    	private int id;
  	
		public CustomSignUserAdapter(Context context, int textViewResourceId) { 
			//Call through to ArrayAdapter implementation 
			super(context, textViewResourceId);
			id = textViewResourceId;
		} 
		
		private void setColor(View mView, int position) {
			 TextView tv = (TextView) mView.findViewById(android.R.id.text1);
		     if (tv != null) {
		    	 SignUserRecord item = getItem(position);
		    	 if (item.userOnline.length() == 0)
		    		 tv.setTextColor(Color.RED);
		    	 else
		    		 tv.setTextColor(Color.BLACK);
		      }		        			
		}
  	
		  @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			 View mView =  super.getView(position, convertView, parent);
			 setColor(mView, position);
		     return mView;
		}
		  
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
		     View mView = super.getDropDownView(position, convertView, parent);
		     setColor(mView, position);
		     return mView;
		}		  
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mContext = this;
//		dbSch = new DBSchemaHelper(this);
		dbSch = DBSchemaHelper.getInstance(this);
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		getTelephonyInfo();
		
		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);
		
		mUserSpinner = (Spinner) findViewById(R.id.userChooser);
		mUserSpinner.setOnItemSelectedListener(mUserSpinnerClickListener);
//		mDBArrayAdapter = new ArrayAdapter<SignUserRecord>(this, android.R.layout.simple_spinner_item);
		mDBArrayAdapter = new CustomSignUserAdapter(this, android.R.layout.simple_spinner_item);
		mDBArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});
		okCancelView = findViewById(R.id.button_Container2);
		signInView = findViewById(R.id.button_Container);
		mPasswordNewView = (EditText) findViewById(R.id.new_password);
		mPasswordNew2View = (EditText) findViewById(R.id.new_password2);
		
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		signInButton = findViewById(R.id.sign_in_button); 
		signInButton.setOnClickListener (
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					attemptLogin();
				}
			});
		changePswButton = findViewById(R.id.change_psw_button);
		changePswButton.setOnClickListener (
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					changePassword(true);
				}
			});
		okButton = findViewById(R.id.OK_button);
		okButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptChangePassword();
					}
				});
		cancelButton = findViewById(R.id.Cancel_button);
		cancelButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						changePassword(false);
					}
				});
		
		mEmailView.setText(DEBUG_USER_NAME);
		mPasswordView.setText(DEBUG_USER_NAME);

		Bundle b = getIntent().getExtras();
		if (b.containsKey("idEnt")) {
			setTitle(R.string.title_sign_warrant);
			idWarrant = b.getString("idWarrant");
			idEnt = b.getString("idEnt");
			if (idEnt.length() != 0) {
				signMode = true;
				changePswButton.setVisibility(View.GONE);
				mEmailView.setText("");
				mEmailView.setEnabled(false);
				mPasswordView.setText("");
				sendRequest();
			}
		}
	}

    private OnItemSelectedListener mUserSpinnerClickListener = new OnItemSelectedListener() {
        @Override
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	signUserSelected = (SignUserRecord) mUserSpinner.getSelectedItem();
       		signInButton.setEnabled(signUserSelected.userOnline.length() != 0);
			String signerName = signUserSelected.userSurName + " " + signUserSelected.userName; 
			mEmailView.setText(signerName);
			if (signUserSelected.userOnline.length() == 0)
				CommonClass.showMessage(mContext, getString(R.string.m_sign_warrant), 
						getString(R.string.m_sign_warr_no_auth));
			if (signerName.length() == 0)
				CommonClass.showMessage(mContext, getString(R.string.m_sign_warrant), 
					getString(R.string.m_sign_warr_no_name));       		
        }

        @Override
    	public void onNothingSelected(AdapterView<?> parent) {}
    };
    
	
	private void getTelephonyInfo() {
		//---get the SIM card ID---
		mSIMId = tm.getSimSerialNumber();
		//---get the phone number---
		mPhoneNumber = tm.getLine1Number();
		//---get the IMEI number---
		mIMEI = tm.getDeviceId();
	}
	
	private void changePassword(boolean show) {
		mPasswordNewView.setVisibility(show ? View.VISIBLE : View.GONE);
		mPasswordNew2View.setVisibility(show ? View.VISIBLE : View.GONE);
		okCancelView.setVisibility(show ? View.VISIBLE : View.GONE);
		signInView.setVisibility(show ? View.GONE : View.VISIBLE);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	public void attemptChangePassword() {
		boolean cancel = false;
		View focusView = null;
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mPasswordNewView.setError(null);
		mPasswordNew2View.setError(null);
		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mNewPassword = mPasswordNewView.getText().toString();
		String checkNewPsw = mPasswordNew2View.getText().toString();
		if (mNewPassword.endsWith(checkNewPsw)) {
			if (!mNewPassword.equals(mPassword)) {
				
			} else {  // new psw equals old psw
				mPasswordNewView.setError(getString(R.string.error_psw_new_old));
				focusView = mPasswordNewView;
				cancel = true;							
			}
		} else {  // new psws don't match 
			mPasswordNew2View.setError(getString(R.string.error_psw_not_match));
			focusView = mPasswordNew2View;
			cancel = true;			
		}		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.ch_psw_progress);
			showProgress(true);
			
//			postStateChangePswRequest();
		}		
	}
	
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		
		if (mEmail.equals(mPassword)) {
			changePassword(true);
			
		} else {
			boolean cancel = false;
			View focusView = null;
	
			// Check for a valid password.
			if (TextUtils.isEmpty(mPassword)) {
				mPasswordView.setError(getString(R.string.error_field_required));
				focusView = mPasswordView;
				cancel = true;
			} else if (mPassword.length() < 4) {
				mPasswordView.setError(getString(R.string.error_invalid_password));
				focusView = mPasswordView;
				cancel = true;
			}
	
			// Check for a valid email address.
			if (TextUtils.isEmpty(mEmail)) {
				mEmailView.setError(getString(R.string.error_field_required));
				focusView = mEmailView;
				cancel = true;
			} /*else if (!mEmail.contains("@")) {
				mEmailView.setError(getString(R.string.error_invalid_email));
				focusView = mEmailView;
				cancel = true;
			}*/
	
			if (cancel) {
				// There was an error; don't attempt login and focus the first
				// form field with an error.
				focusView.requestFocus();
			} else {
				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
//				showProgress(true);
	//			mAuthTask = new UserLoginTask();
	//			mAuthTask.execute((Void) null);
				
				postStateRequest(STATE_POST_SIGN_AUTH);
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
				.alpha(show ? 1 : 0)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mLoginStatusView.setVisibility(show ? View.VISIBLE
								: View.GONE);
					}
				});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
				.alpha(show ? 0 : 1)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mLoginFormView.setVisibility(show ? View.GONE
								: View.VISIBLE);
					}
				});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	private void sendRequest() {
		mState = STATE_GET_ENT_SIGN_EMP;
		String url = ConnectionManagerService.getServerURI(this) + 
				ConnectionManagerService.REST_WARRANTS_URI + ConnectionManagerService.GET_SIGN_EMP + idEnt;

		HttpGet searchRequest = new HttpGet(url);
		searchRequest.addHeader(BasicScheme.authenticate(
				new UsernamePasswordCredentials(mIMEI, WarrListActivity.MOBILE), "UTF-8", false));
		RestTask task = new RestTask();
		task.setResponseCallback((ResponseCallback) mContext);
		task.execute(searchRequest);	
		showProgress(true);
	}	

	private void postStateRequest(int state){
		String url = "", warr = "", userLogin = "", userPsw = "";
		try {
			mState = state;
			switch (mState) {
			case STATE_POST_SIGN_AUTH:
				url = ConnectionManagerService.getServerURI(this) + ConnectionManagerService.LOGIN_IMEI_URI;
	        	warr = "Login";
	        	userPsw = mPassword;
	        	if (signUserSelected != null) 
	        		userLogin = signUserSelected.userOnline;
	        	else
	        		userLogin = uLogin;
	        	break;
			case STATE_POST_SIGN_WARRANT:
				url = ConnectionManagerService.getServerURI(this) + ConnectionManagerService.REST_WARRANTS_URI +
						ConnectionManagerService.SIGN_WARR;
	        	userLogin = mIMEI; 
	        	userPsw = WarrListActivity.MOBILE;				
	        	if (signUserSelected != null) {
		        	JSONObject header = new JSONObject();
		        	header.put("id_warrant", idWarrant); 
		        	header.put("id_user", signUserSelected.userId); 
		        	warr = header.toString();
	        	}
				break;
			}
			String title = "";
	        if (warr.length() > 0 && url.length() > 0) {
				//Simple POST 
				HttpPost postRequest = new HttpPost(url); 
				postRequest.addHeader(BasicScheme.authenticate(
						new UsernamePasswordCredentials(userLogin, userPsw), "UTF-8", false));

				StringEntity se = new StringEntity(warr, "UTF-8");
				switch (mState) {
				case STATE_POST_SIGN_AUTH:
					se.setContentType("text/plain;charset=UTF-8");
					se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "text/plain;charset=UTF-8"));
					break;
				case STATE_POST_SIGN_WARRANT:
		        	se.setContentType("application/json;charset=UTF-8");
		        	se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
					break;
				}
	        	postRequest.setEntity(se);
	        								
				RestTask task = new RestTask(); 
				task.setResponseCallback((ResponseCallback) mContext); 
				task.execute(postRequest); 
				//Display progress to the user 
				showProgress(true);
//				mProgress = ProgressDialog.show(this, getString(string.title_auth), getString(string.m_wait_answer), true);
	        }
		} catch (Exception e) { 
        	if (D) Log.e(TAG, e.getMessage());
//			mResult.setText(e.getMessage()); 
		}		
	}
	
	private void postStateChangePswRequest(){
		try {
			String url = LOGIN_URI_SSL + CHANGE_PSW;
			String title = "", warr = "";
			
        	JSONObject header = new JSONObject();
        	header.put("u_name", android.os.Build.MODEL); 
        	header.put("old_psw", android.os.Build.VERSION.RELEASE); 
        	header.put("new_psw", mPhoneNumber);
        	warr = header.toString();
	        if (warr.length() > 0) {
				//Simple POST 
				HttpPost postRequest = new HttpPost(url); 
				postRequest.addHeader(BasicScheme.authenticate(
						 new UsernamePasswordCredentials(mEmail, mPassword), "UTF-8", false));

				StringEntity se = new StringEntity(warr, "UTF-8");
	        	se.setContentType("application/json;charset=UTF-8");
	        	se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
	        	postRequest.setEntity(se);
	        								
				RestTask task = new RestTask(); 
				task.setResponseCallback((ResponseCallback) mContext); 
				task.execute(postRequest); 
				//Display progress to the user 
				showProgress(true);
//				mProgress = ProgressDialog.show(this, getString(string.title_auth), getString(string.m_wait_answer), true);
	        }
		} catch (Exception e) { 
        	if (D) Log.e(TAG, e.getMessage());
		}		
	}
	
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}

			// TODO: register the new account here.
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
	        	Intent runWarrListActivity = new Intent(mContext, WarrListActivity.class);
	        	startActivity(runWarrListActivity);
//				finish();
			} else {
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}


	@Override
	public void onRequestSuccess(String response) {
		String res = ""; 
		int res_code = -1;
		String uName = "";
		SignUserRecord signUser;
		showProgress(false);
		if (D) Log.d(TAG, "onRequestSuccess");
		boolean resultOK = false;
		try {
			JSONArray records = new JSONArray(response);
			if (records.length() > 0/*1*/) {
				mEmailView.setVisibility(View.GONE);
				mUserSpinner.setVisibility(View.VISIBLE);
			}
			for (int i = 0; i < records.length(); i++) {
				JSONObject warrant = records.getJSONObject(i);
				switch (mState) {
				case STATE_GET_ENT_SIGN_EMP:
					res = warrant.getString("result");
					res_code = warrant.getInt("code");
					signUser = new SignUserRecord();
					signUser.userName = warrant.getString("name");
					signUser.userSurName = warrant.getString("surname");
					signUser.userOnline = warrant.getString("user");
					signUser.userSfx = warrant.getString("sfx");;
					signUser.userId = warrant.getString("id_emp");
					mDBArrayAdapter.add(signUser);
					resultOK = true;
					break;
				case STATE_GET_USER:
					break;
				case STATE_POST_SIGN_AUTH:
					res = warrant.getString("result");
					res_code = warrant.getInt("code");
					uName = warrant.getString("name");
					resultOK = res_code == 0;
					break;
				case STATE_POST_SIGN_WARRANT:
					res = warrant.getString("result");
					res_code = warrant.getInt("code");
					resultOK = res_code == 0;					
					break;
				default:
					res = warrant.getString("result");
					res_code = warrant.getInt("code");
					uName = warrant.getString("name");
					resultOK = true;
					break;
				}				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			returnString = "Exception: " + e.getMessage();
		}
		switch (mState) {
		case STATE_GET_ENT_SIGN_EMP:
			if (resultOK) {
				mUserSpinner.setAdapter(mDBArrayAdapter);
				if (!mDBArrayAdapter.isEmpty())
					mUserSpinner.setSelection(0);
				else {
					CommonClass.showMessage(mContext, getString(R.string.m_sign_warrant), 
							getString(R.string.m_sign_warr_error));					
				}
			} else {
				mEmailView.setText("");
				CommonClass.showMessage(mContext, getString(R.string.m_sign_warrant), 
					getString(R.string.m_sign_warr_error));
			}
			if (uName.length() == 0 || !resultOK) {
			}
			break;
		case STATE_GET_USER:
			break;
		case STATE_POST_SIGN_AUTH:
			if (resultOK) {
//				наряд подписан
				postStateRequest(STATE_POST_SIGN_WARRANT);
				
			} else {
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();			
			}
			break;
		case STATE_POST_SIGN_WARRANT:
			if (resultOK) {
				if (signUserSelected != null)
					dbSch.setWarrantSigned(Long.parseLong(idWarrant), Long.parseLong(signUserSelected.userId));
	        	Toast.makeText(mContext, "Наряд подписан", Toast.LENGTH_LONG).show();
				finish();				
			} else {
				CommonClass.showMessage(mContext, getString(R.string.m_sign_warrant), 
						getString(R.string.m_signing_warr_error));
				
			}
			break;
		default:
			if (resultOK) {
				Intent runWarrListActivity = new Intent(mContext, WarrListActivity.class);
				startActivity(runWarrListActivity);
			} else {
				mPasswordView.setError(getString(R.string.error_server));
				mPasswordView.requestFocus();			
			}
		}
	}

	@Override
	public void onRequestError(Exception error) {
		String errMsg;
		showProgress(false);
		errMsg = "onRequestError: "+ error.getMessage();
		if (D) Log.d(TAG, errMsg);
//		Toast.makeText(mContext, errMsg, Toast.LENGTH_LONG).show();
		switch (mState) {
		case STATE_GET_ENT_SIGN_EMP:
			mEmailView.setEnabled(true);
			mEmailView.setError(getString(R.string.error_get_sign_emp) +" " + errMsg);			
			mEmailView.requestFocus();
			break;
		case STATE_POST_SIGN_WARRANT:
			CommonClass.showMessage(mContext, getString(R.string.m_sign_warrant), 
					getString(R.string.m_signing_warr_error));
			break;
		default:
			mPasswordView.setError(getString(R.string.error_incorrect_password));
			mPasswordView.requestFocus();
		}
	}
}
