package com.utis.warrants.record;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.database.Cursor;

import com.utis.warrants.tables.LocationTable;

public class LocationRecord {
	public int id;
	public double lat, lng;
	public Date l_date;
	public int modified; 
	
	   /**
  * Constructor. 
  */
	public LocationRecord() {
		id = 0;
	}

	public LocationRecord(Cursor c) {
		int colid = c.getColumnIndex(LocationTable.ID);
		id = c.getInt(colid);
		colid = c.getColumnIndex(LocationTable.LAT);
		lat = c.getDouble(colid);
		colid = c.getColumnIndex(LocationTable.LNG);
		lng = c.getDouble(colid);
		colid = c.getColumnIndex(LocationTable.L_DATE);
// 	m_date = new Date(c.getLong(colid));
		String date_str = c.getString(colid);
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");    	
		try {
			java.util.Date date = formatter.parse(date_str); 
			l_date = new Date(date.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  		
		colid = c.getColumnIndex(LocationTable.MODIFIED);
		modified = c.getInt(colid);
	}
 
	public String toString() {
		return String.format("%s;\n Lat: %f; Lng: %f", getDate(), lat, lng);
	}

	public String getDate() {
		SimpleDateFormat df = new SimpleDateFormat("d MMM yyyy; H:mm:ss");  
		String text = df.format(l_date);
		return text;
	}

}
