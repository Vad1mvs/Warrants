package com.utis.warrants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

public class NetworkStatusReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
//		Toast.makeText(context, "Network Connectivity Status Changed!", Toast.LENGTH_SHORT).show();
		Bundle extras = intent.getExtras();
		boolean noNetwork = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if (extras != null) {
			String networkInfoStatus = ConnectivityManager.EXTRA_NETWORK_INFO;
			NetworkInfo netInfo = (NetworkInfo) extras.get(networkInfoStatus);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				Toast.makeText(context, "Есть подключение к сети " + netInfo.getTypeName(), Toast.LENGTH_SHORT).show();
			} else if (noNetwork) {
				Toast.makeText(context, "Нет подключения к сети!", Toast.LENGTH_SHORT).show();
			}
		}
		
	}

}
