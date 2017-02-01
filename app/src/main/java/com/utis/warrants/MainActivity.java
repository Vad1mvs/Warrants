package com.utis.warrants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.utis.warrants.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class MainActivity extends Activity  implements OnClickListener{

	private static final boolean D = true;
	private static final String TAG = "LANTest";
	private static final String WEBUrl = "http://192.168.2.238:8080/Ol2/login.html";
	
	private TextView Caption;
	private TextView txtView;
	private TextView receiveView;
	private TextView sendView;
	private View sendButton;
	private View confirmButton;
	private View testButton;
	private View test2Button;
	private View test3Button;
	private Uri alert;
	private Ringtone ringtone;
//	private DBSchemaHelper dbSch;
	
	private static final int MAX_UDP_DATAGRAM_LEN = 1500;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
     // Set up click listeners for all the buttons
        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);
        confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(this);
        testButton = findViewById(R.id.test_button);
        testButton.setOnClickListener(this);
        test2Button = findViewById(R.id.test2_button);
        test2Button.setOnClickListener(this);
        test3Button = findViewById(R.id.test3_button);
        test3Button.setOnClickListener(this);
        
        ConnectivityManager cm = (ConnectivityManager)
		getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWifiAvail = ni.isAvailable();
		boolean isWifiConn = ni.isConnected();
//		ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//		boolean isMobileAvail = ni.isAvailable();
//		boolean isMobileConn = ni.isConnected();
		
		sendView = (TextView)findViewById(R.id.send_editText);
		sendView.setText("Test msg");
		receiveView = (TextView)findViewById(R.id.receive_editText);
		txtView = (TextView)findViewById(R.id.textView1);
		String Ip = "0.0.0.0";
		if (isWifiConn) {
			Ip = getIpAddr(); 
		}
		txtView.setText("WiFi Avail = "+ isWifiAvail + "; Conn = " + isWifiConn + "; IP = " + Ip);
//		Caption = (TextView)findViewById(R.id.editText1);
//		Caption.setText("WiFi\nAvail = "+ isWifiAvail + "\nConn = " + isWifiConn +
//				"\nMobile\nAvail = "+ isMobileAvail + "\nConn = " + isMobileConn);
		
		initSound();
//		dbSch = new DBSchemaHelper(this);
//		runUdpServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.search:
        	if (D) Log.d(TAG, "onOptions Search");
            // Launch the DeviceListActivity to see devices and do scan
//            serverIntent = new Intent(this, DeviceListActivity.class);
//            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        }
        return false;
    }
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        runUdpServer();
    }    
        
	@Override
	public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.send_button:
    		if (D) Log.d(TAG, "clicked on send_button");
//    		String udpMsg = sendView.getText().toString();
//    		runUdpClient();
    		sendMsgThread(sendView.getText().toString());
    		break;
    	case R.id.confirm_button:
    		if (D) Log.d(TAG, "clicked on confirm_button");
//    		String udpMsg = sendView.getText().toString();
    		stopSound();
    		sendMsgThread("Got message: "+ receiveView.getText().toString());
    		break;
    	case R.id.test_button:
    		if (D) Log.d(TAG, "clicked on test_button");
//    		launchBrowser(WEBUrl);
    		launchWebKit();
    		break;
    	case R.id.test2_button:
    		if (D) Log.d(TAG, "clicked on test2_button");
//    		launchWebKit();
    		launchWarrListKit();
    		break;
    	case R.id.test3_button:
    		if (D) Log.d(TAG, "clicked on test3_button");
//    		launchWebKit();
    		launchWarrantsList();
    		break;
//    	case R.id.exit_button:
//    		finish();
//    		break;
       	}		
	}
	
    private void launchWarrantsList() {
        Intent runWarrantsListActivity = new Intent(this, WarrantsActivity.class);
        startActivity(runWarrantsListActivity);
    }
	
    private void launchWarrListKit() {
        Intent runWarrListActivity = new Intent(this, WarrListActivity.class);
        startActivity(runWarrListActivity);
    }
	
    private void launchWebKit() {
        Intent runWebKitActivity= new Intent(this, WebKitActivity.class);
        startActivity(runWebKitActivity);
    }
	
	private void launchBrowser(String Url) {
		Uri uriUrl = Uri.parse(Url);
		Intent runBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
		startActivity(runBrowser);
	}
	
	private String getIpAddr() {
	   WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	   int ip = wifiInfo.getIpAddress();

	   String ipString = String.format("%d.%d.%d.%d", 
			   (ip & 0xff),
			   (ip >> 8 & 0xff),
			   (ip >> 16 & 0xff),
			   (ip >> 24 & 0xff));

	   return ipString;
	} 
	
	private void initSound() {
		alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
	    if(alert == null){
	         // alert is null, using backup
	         alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	         if(alert == null){  // I can't see this ever being null (as always have a default notification) but just incase
	             // alert backup is null, using 2nd backup
	             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
	         }
	    }		
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);		
	}
	
	private void playSound() {
        ringtone.play();
	}
	
	private void stopSound() {
        ringtone.stop();
	}

	private void runUdpServer() {
		new Thread(new Runnable() {
		    String lText;
		    byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
		    DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
		    DatagramSocket ds = null;

		    public void run() {
			    try {
			        ds = new DatagramSocket(SendMsgRunnable.UDP_PORT);
			        //disable timeout for testing
			        //ds.setSoTimeout(100000);
			        while(true){
				        ds.receive(dp);
				        lText = new String(lMsg, 0, dp.getLength());
				        Log.i("UDP packet received", lText);
				        receiveView.post(new Runnable() {				
							@Override
							public void run() {
								receiveView.setText(lText);								
								playSound();
							}
						});
			        }
			    } catch (SocketException e) {
			        e.printStackTrace();
			    } catch (IOException e) {
			        e.printStackTrace();
			    } finally {
			        if (ds != null) {
			            ds.close();
			        }
			    }			
			}
		}).start();	    
	}
	
	private void sendMsg(String Msg) {
		String udpMsg;
		DatagramSocket ds = null;
		
	    try {
	    	udpMsg = Msg;
	        ds = new DatagramSocket();
	        InetAddress serverAddr = InetAddress.getByName(SendMsgRunnable.DEST_IP);
	        DatagramPacket dp;
	        dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, SendMsgRunnable.UDP_PORT);
	        ds.send(dp);
	        Log.i("UDP packet sent to "+ serverAddr, udpMsg);

	    } catch (SocketException e) {
	        e.printStackTrace();
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }
	    }
	}

	private void sendMsgThread(String Msg) {
		Thread sendThread;
		SendMsgRunnable runnable;
		
		runnable = new SendMsgRunnable();
		runnable.msg = Msg;
		sendThread = new Thread(runnable);
		sendThread.start();
	}
	
	private void runUdpClient()  {
		new Thread(new Runnable() {
		    String udpMsg = "hello world from UDP client " + SendMsgRunnable.UDP_PORT;
		    
			public void run() {			
			    try {
//			    	sendView.post(new Runnable() {				
//						@Override
//						public void run() {
//							udpMsg = sendView.getText().toString();
//						}
//					});
			    	udpMsg = sendView.getText().toString();
			    	sendMsg(udpMsg);
			    } catch (Exception e) {
			        e.printStackTrace();
			    } finally {
			    }
			}
		}).start();		
	}
	
	
}
