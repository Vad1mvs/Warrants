package com.utis.warrants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.utis.warrants.record.MessagesRecord;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ComposeNewMsgActivity extends Activity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final int EMP_SELECT = 1;
	private static final int TAKE_PHOTO = 2;
	private static final int SAVE_PHOTO = 3;
	private static final int PHOTO_MARGIN = 400000;
	
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;
	private Context mContext;

	// Values for email and password at the time of the login attempt.
	private String mSubj;
	private String mMsgText;

	// UI references.
	private EditText mSubjView, mMsgTextView;
	private TextView mRecipient, mLoginStatusMessageView;
	private View mLoginFormView, mLoginStatusView;
	private String recipientNm = "", selfId = "";
	private int recipientId = 0;
    private /*Zoomable*/ImageView photo;
    private Bitmap bitmap;
    private File photo_file;
    private DBSchemaHelper dbSch;
    private long selfEmpId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_compose_new_msg);
		dbSch = DBSchemaHelper.getInstance(this);
		
		Bundle b = getIntent().getExtras();  
		selfId = b.getString("selfEmpId");
		try {
			selfEmpId = Long.parseLong(selfId);
		} catch (Exception e) {
			selfEmpId = dbSch.getUserId();			
		}
		try {
			recipientId = Integer.parseInt(b.getString("id_recipient"));
		} catch (Exception e) {
			recipientId = 0;
		}
		recipientNm = b.getString("recipient");
		mSubj = b.getString("subj");
		mMsgText = b.getString("msg");
		photo_file = new File(Environment.getExternalStorageDirectory(), "tmp_image.jpg");
		
		mRecipient = (TextView) findViewById(R.id.msg_recipient);
		mRecipient.setText(mContext.getResources()
				.getString(R.string.action_msg_choose_recipient) + ": " + recipientNm);
		// Set up the login form.
//		mSubj = getIntent().getStringExtra(EXTRA_EMAIL);
		mSubjView = (EditText) findViewById(R.id.msg_subj);
		mSubjView.setText(mSubj);

		mMsgTextView = (EditText) findViewById(R.id.msg_text);
		mMsgTextView.setText(mMsgText);
		photo = (/*Zoomable*/ImageView)findViewById(R.id.imageViewPhoto);


		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.compose_new_msg, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.action_save_msg);
		mi = menu.findItem(R.id.action_select_recipient);
	    return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_save_msg:        	
        	attemtSaveMsg();
            return true;
        case R.id.action_select_recipient:        	
        	chooseRecipient();
            return true;
        case R.id.action_take_photo:        	
        	savePhoto();
            return true;
        }
        return false;
    }
	
	private void chooseRecipient() {
		Intent intent = new Intent(mContext, EmpPhotoSelectActivity.class);
		Bundle b = new Bundle();
		b.putString("selfId", selfId);
		intent.putExtras(b); 		
		startActivityForResult(intent, EMP_SELECT);		
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case EMP_SELECT:
                if (data.hasExtra("id")) {
                	recipientId = Integer.parseInt(data.getExtras().getString("id"));
                	recipientNm = data.getExtras().getString("nm");
//                    Toast.makeText(this, data.getExtras().getString("id"),
//                        Toast.LENGTH_SHORT).show();
                	
                	mRecipient.setText(mContext.getResources()
                			.getString(R.string.action_msg_choose_recipient) + ": " + recipientNm);
                }
    			break;
			case TAKE_PHOTO: 
				//Process and display the image 
				bitmap = (Bitmap)data.getExtras().get("data"); 
				photo.setImageBitmap(bitmap);			
				break;    			
			case SAVE_PHOTO:
				try { 
					FileInputStream in = new FileInputStream(photo_file);
					long fSize = in.getChannel().size();
					BitmapFactory.Options options = new BitmapFactory.Options(); 

//					options.inJustDecodeBounds = true;
//					BitmapFactory.decodeStream(in, null, options);
//					int imageHeight = options.outHeight;
//					int imageWidth = options.outWidth;
//					String imageType = options.outMimeType;						
					//photo.setImageBitmap(CommonClass.decodeSampledBitmapFromStream(in, photo.getWidth(), photo.getHeight()));
					
					options.inJustDecodeBounds = false;
					if (fSize < PHOTO_MARGIN) 
						options.inSampleSize = 1;
					else {
						options.inSampleSize = (int) (fSize / PHOTO_MARGIN) + 1;
					}
					//options.inSampleSize = 5; //Downsample by 5x 
					bitmap = BitmapFactory.decodeStream(in, null, options); 
					photo.setImageBitmap(bitmap); 
				} catch (Exception e) { 
					e.printStackTrace(); 
				}
				break;
    		}
        }
    }   	
    
    private void selectPhoto() {
//		Intent intent = new Intent();
//	    intent.setType("image/*");
//	    intent.setAction(Intent.ACTION_GET_CONTENT);
//	    intent.addCategory(Intent.CATEGORY_OPENABLE);
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
	    startActivityForResult(intent, TAKE_PHOTO);    	
    }
    
    private void savePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
		//Add extra to save full-image somewhere 
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo_file)); 
		startActivityForResult(intent, SAVE_PHOTO);    	
    }
    
	public void selectPhoto(View view) {
		selectPhoto();
	}
	
	public void savePhoto(View view) {
		savePhoto();
	}
    
    private void attemtSaveMsg() {		
		// Reset errors.
		mSubjView.setError(null);
		mMsgTextView.setError(null);

		// Store values at the time of the login attempt.
		mSubj = mSubjView.getText().toString();
		mMsgText = mMsgTextView.getText().toString();
		
		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mSubj)) {
			mSubjView.setError(getString(R.string.error_field_required));
			focusView = mSubjView;
			cancel = true;
		} else if (TextUtils.isEmpty(mMsgText)) {
			mMsgTextView.setError(getString(R.string.error_field_required));
			focusView = mMsgTextView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; focus the first form field with an error.
			focusView.requestFocus();
		} else {
			if (recipientNm.length() == 0) {
				chooseRecipient();
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				String aName = "";
				if (bitmap != null) {
					bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
					aName = "photo.jpg";
				}
	            byte[] ba = baos.toByteArray();
				long id = dbSch.addMsgItem(0, selfEmpId, recipientId, MessagesRecord.MSG_NEW,
						MessagesRecord.MSG_ORDINARY, ba.length, new Date(), "", mSubj, mMsgText, aName, ba);
				
		        Intent returnIntent = new Intent();
				Bundle b = new Bundle();
				b.putString("id", Long.toString(id));
				returnIntent.putExtras(b); 
		        setResult(RESULT_OK, returnIntent);       
		        finish();                	
			}
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
		mSubjView.setError(null);
		mMsgTextView.setError(null);

		// Store values at the time of the login attempt.
		mSubj = mSubjView.getText().toString();
		mMsgText = mMsgTextView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mMsgText)) {
			mMsgTextView.setError(getString(R.string.error_field_required));
			focusView = mMsgTextView;
			cancel = true;
		} else if (mMsgText.length() < 4) {
			mMsgTextView.setError(getString(R.string.error_invalid_password));
			focusView = mMsgTextView;
			cancel = true;
		}
		// Check for a valid email address.
		if (TextUtils.isEmpty(mSubj)) {
			mSubjView.setError(getString(R.string.error_field_required));
			focusView = mSubjView;
			cancel = true;
		} else if (!mSubj.contains("@")) {
			mSubjView.setError(getString(R.string.error_invalid_email));
			focusView = mSubjView;
			cancel = true;
		}
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
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
				if (pieces[0].equals(mSubj)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mMsgText);
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
				finish();
			} else {
				mMsgTextView
						.setError(getString(R.string.error_incorrect_password));
				mMsgTextView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
