package com.utis.warrants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class CommonClass {
	private static final boolean D = true;
	private static final String TAG = "CommonClass";
	public static final String PREF_NAME = "MyPref";
	public static final int clrDkRed = 0xff8B0000;
	public static final String noData = "н/д";
	public static String lastErrorMsg = "";
	public static boolean keepLog;
	public static boolean newVerAvail = false;
	private static final String LAST_DICTIONARY_UPDATE_FILENAME = "last_dict_update.txt";
	private static final String LAST_REPLICATOR_TIME_FILENAME = "last_repl_time.txt";
	
	public static boolean isEqServDateValid(Date beginDate, Date endDate, Date date2compare) {
		boolean res = false;
		if (date2compare != null) {
			if (beginDate != null) {
				if (endDate != null) {
					res = (date2compare.before(endDate) && date2compare.after(beginDate))||
							date2compare.equals(endDate)||date2compare.equals(beginDate);
				} else {
					res = date2compare.equals(beginDate) || date2compare.after(beginDate);
				}
			} else {
				if (endDate != null) {
					res = date2compare.equals(endDate) || date2compare.before(endDate);
				} 			
			}			
		}		
		
//		return (((beginDate != null)&&(date2compare >= beginDate)&&(date2compare <= endDate))||
//    		((date2compare >= beginDate)&&(beginDate != null)&&(endDate == null)));
		
		return res;
	}
	
	public static void showMessage(Context mContext, String msgTitle, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    builder
	    .setTitle(msgTitle)
	    .setMessage(msg)
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
	
	//Create a new file and write some data
	private static boolean writeDate2File(Context mContext, Date aDate, String fileName) {
		boolean res = false;
		try { 
			FileOutputStream mOutput = mContext.openFileOutput(fileName, Activity.MODE_PRIVATE); 
//			String data = aDate.toString(); 
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");  
			String data = df.format(aDate);
			if (D) Log.d(TAG, "writeLastUpdate = " + data);	
			mOutput.write(data.getBytes()); 
			mOutput.close();
			res = true;
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
		} catch (IOException e) { 
			e.printStackTrace(); 
		}
		return res;
	}
	
	private static String readDateFromFile(Context mContext, String fileName) {
		String res = "";
		try { 
			FileInputStream mInput = mContext.openFileInput(fileName);
			byte[] data = new byte[12]; 
			mInput.read(data); 
			mInput.close(); 
			res = new String(data); 
			if (res.length() == 0)
				res = "201309010000";
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
		} catch (IOException e) { 
			e.printStackTrace(); 
		}
		return res;
	}

	public static boolean writeReplTime(Context mContext, Date aDate) {
		return writeDate2File(mContext, aDate, LAST_REPLICATOR_TIME_FILENAME);
	}
	
	public static String readReplTime(Context mContext) {
		return readDateFromFile(mContext, LAST_REPLICATOR_TIME_FILENAME);
	}	
	
	//Create a new file and write some data
	public static boolean writeLastUpdate(Context mContext, Date aDate) {
		return writeDate2File(mContext, aDate, LAST_DICTIONARY_UPDATE_FILENAME);
	}

	public static String readLastUpdate(Context mContext) {
		return readDateFromFile(mContext, LAST_DICTIONARY_UPDATE_FILENAME);
	}
	
    public static void showLocationOnMap(Context mContext, double lat, double lng) {
		Intent runMapActivity = new Intent(mContext, WarrantsMapActivity.class);
        Bundle b = new Bundle();
        b.putString("map_mode", Integer.toString(WarrantsMapActivity.SINGLE_LOCATION_MAP_MODE));
        b.putString("lat", Double.toString(lat));
        b.putString("lng", Double.toString(lng));
        runMapActivity.putExtras(b);            
        mContext.startActivity(runMapActivity);                	
    }
	
    public static void showEntEmpLocationOnMap(Context mContext, double lat, double lng, String EntNm, String EntAddr) {
		Intent runMapActivity = new Intent(mContext, WarrantsMapActivity.class);
        Bundle b = new Bundle();
        b.putString("map_mode", Integer.toString(WarrantsMapActivity.SINGLE_ENT_EMP_LOCATION_MAP_MODE));
        b.putString("lat", Double.toString(lat));
        b.putString("lng", Double.toString(lng));
        b.putString("eNm", EntNm);
        b.putString("eAddr", EntAddr);
        runMapActivity.putExtras(b);            
        mContext.startActivity(runMapActivity);                	
    }
    
    public static void showWarrantEmpWorkTimeMarks(Context mContext, String wId, String wNum) {
        Intent intent = new Intent(mContext, EmpWorkTimeActivity.class);
        Bundle b = new Bundle();
        b.putString("id", wId); 
        b.putString("num", wNum);
        intent.putExtras(b); 
        mContext.startActivity(intent);    	    	    	
    }

    public static void showScannerActivity(Context mContext, String wId, String wNum, String entAddress, String entName
            , String warrIdContract, String warrRemark, String warrEntId ,String warrContragentInfo,String subEntName, String dateNum) {
        Intent intent = new Intent(mContext, BarScanActivity.class);
        Bundle b = new Bundle();
        b.putString("id", wId);
        b.putString("num", wNum);
        b.putString("ent_addr", entAddress);
        b.putString("ent_name", entName);
        b.putString("id_contract", warrIdContract);
        b.putString("rem", warrRemark);
        b.putString("id_ent", warrEntId);
        b.putString("contragent", warrContragentInfo);
        b.putString("sub_ent_nume", subEntName);
        b.putString("w_date", dateNum);

        intent.putExtras(b);
        mContext.startActivity(intent);
    }
    
    public static void copy2Clipboard(String text, Activity activity) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
		    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		    clipboard.setText(text);
		} else {
		    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE); 
		    android.content.ClipData clip = android.content.ClipData.newPlainText("", text);
		    clipboard.setPrimaryClip(clip);
		}
    }

    public static void openOnlineWarrants(Context mContext) {
    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https:////85.238.112.13/contenti/Ol3"));
    	mContext.startActivity(browserIntent);
    }
	
	public static void showLocationSettingsAlert(Context mContext) {
		final Context myContext = mContext;
	    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
	    alertDialog.setTitle(mContext.getString(R.string.title_geo_alert));
	    alertDialog.setMessage(mContext.getString(R.string.m_geo_disabled));
	    alertDialog.setPositiveButton(mContext.getString(R.string.menu_settings),
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                    myContext.startActivity(intent);
	                }
	            });

	    // on pressing cancel button
	    alertDialog.setNegativeButton(mContext.getString(R.string.btn_cancel),
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                    dialog.cancel();
	                }
	            });

	    // Showing Alert Message
	    alertDialog.show();
	}

	public static int convertDpToPixel(Context mContext, float dp) {
	       DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
	       float px = dp * (metrics.densityDpi / 160f);
	       return (int) px;
	}
	
	public static void showUndo(final View viewContainer) {
	    viewContainer.setVisibility(View.VISIBLE);
	    viewContainer.setAlpha(1);
	    viewContainer.animate().alpha(0.4f).setDuration(5000)
	        .withEndAction(new Runnable() {

	          @Override
	          public void run() {
	        	  viewContainer.setVisibility(View.GONE);
	          }
	        });
	}

	public static boolean isNetworkAvailable(Context mContext) {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public static String printSecondsInterval(long interval) {
		long secInMin = 60;
		long minInHour = secInMin * 60;
 
		long elapsedHours = interval / minInHour;
		interval = interval % minInHour;
 
		long elapsedMinutes = interval / secInMin;
		interval = interval % secInMin;
 
		long elapsedSeconds = interval;
		return String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
	}
	
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		if (bm != null) {
		    int width = bm.getWidth();
		    int height = bm.getHeight();
		    float scaleWidth = ((float) newWidth) / width;
		    float scaleHeight = ((float) newHeight) / height;
		    // CREATE A MATRIX FOR THE MANIPULATION
		    Matrix matrix = new Matrix();
		    // RESIZE THE BIT MAP
		    matrix.postScale(scaleWidth, scaleHeight);
		
		    // "RECREATE" THE NEW BITMAP
		    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		    return resizedBitmap;
		} else
			return null;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
    	return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromStream(FileInputStream fIn, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeStream(fIn, null, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeStream(fIn, null, options);
	}

}
