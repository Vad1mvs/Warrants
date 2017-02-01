package com.utis.warrants;

import com.utis.warrants.R;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;

public class WebKitActivity extends Activity {
	private static final boolean D = true;
	private static final String TAG = "WebKitActivity";
	
	private String htmlLocation = "        var myLocation = new google.maps.LatLng(%s,%s);\n";
	private String htmlCode1 = "\n"
	+ "<html>\n"
	+ "  <head>\n"
	+ "    <meta charset=\"utf-8\">\n"
	+ "    <title>Google Maps JavaScript API v3 Example: Street View Layer</title>\n"
	+ "    <link href=\"/maps/documentation/javascript/examples/default.css\" rel=\"stylesheet\">\n"
	+ "    <script src=\"https://maps.googleapis.com/maps/api/js?sensor=false\"></script>\n"
	+ "    <script>\n"
	+ "      var panorama;\n"
	+ "\n"
	+ "      function initialize() {\n";
//	+ "        var myLocation = new google.maps.LatLng(51.5271124,-0.1337308);\n"
//	+ "        var myLocation = new google.maps.LatLng(%s,%s);\n"

	private String htmlCode2 = "\n"
	+ "        var panoramaOptions = {\n"
	+ "          position: myLocation,\n"
	+ "        };\n"
    + "\n"
	+ "        panorama = new  google.maps.StreetViewPanorama(document.getElementById('pano'),panoramaOptions);\n"
    + "\n"
//	+ "        var stationMarker = new google.maps.MarkerImage('http://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Underground.svg/500px-Underground.svg.png');\n"
//	+ "        var station = new google.maps.Marker({\n"
//	+ "        position: new google.maps.LatLng(51.5274683,-0.1345183),\n"
//	+ "          map: panorama,\n"
//	+ "          icon: stationMarker,\n"
//	+ "          title: 'Euston Station',\n"
//	+ "        });\n"
	+ "      }\n"
	+ "\n"
	+ "    </script>\n"
	+ "  </head>\n"
	+ "  <body onload=\"initialize()\">\n"
	+ "    <div id=\"pano\" style='width:100%; height:100%; padding:0; margin:0; -webkit-user-modify: read-write-plaintext-only; -webkit-tap-highlight-color:rgba(0,0,0,0)'></div>\n"
	+ "  </body>\n"
	+ "</html>";


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_kit);
		
		final WebView wv = (WebView) findViewById(R.id.web_holder);
		WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            	Log.e(TAG, "onReceivedSslError: " + error.toString());
                handler.proceed();
            }
        });
        
        String myLocation = "";// = htmlCode;
		Bundle b = getIntent().getExtras();
		if (b != null && b.containsKey("lat") && b.containsKey("lng")) {
			String sLat = b.getString("lat");
			String sLng = b.getString("lng");
			myLocation = String.format(htmlLocation, sLat, sLng);
		} else {
			try {
				myLocation = String.format(htmlLocation, WarrantsMapActivity.ODESSA.latitude, 
					WarrantsMapActivity.ODESSA.longitude);
			} catch (Exception e) {
				Log.d(TAG, "format exception: " + e.getMessage());
			}
		}
//		String myCode = htmlCode1 + myLocation + htmlCode2;
//		wv.loadData(myCode, "text/html", "utf-8");

		wv.loadUrl(WarrListActivity.SERVER_URI_SSL +"/index.php");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_kit, menu);
		return true;
	}

}
