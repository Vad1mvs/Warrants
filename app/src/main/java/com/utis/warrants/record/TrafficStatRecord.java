package com.utis.warrants.record;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.database.Cursor;

import com.utis.warrants.tables.EmpWorkTimeTable;
import com.utis.warrants.tables.TrafficStatTable;

public class TrafficStatRecord {
	public int id;
	public long rx_bytes;
	public long tx_bytes;
	public long rx_json_bytes;
	public long tx_json_bytes;
	public Date statDate;
	public int modified;
	public String statDateStr;
	
	public TrafficStatRecord() {
		id = 0;
	}

	public TrafficStatRecord(Cursor c) {
		int colid = c.getColumnIndex(TrafficStatTable.ID);
	 	id = c.getInt(colid);
		colid = c.getColumnIndex(TrafficStatTable.RX_BYTES);
	 	rx_bytes = c.getLong(colid);
		colid = c.getColumnIndex(TrafficStatTable.TX_BYTES);
	 	tx_bytes = c.getLong(colid);
		colid = c.getColumnIndex(TrafficStatTable.RX_JSON_BYTES);
	 	rx_json_bytes = c.getLong(colid);
		colid = c.getColumnIndex(TrafficStatTable.TX_JSON_BYTES);
	 	tx_json_bytes = c.getLong(colid);
		colid = c.getColumnIndex(TrafficStatTable.STAT_DATE);
    	statDateStr = c.getString(colid);
    	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");    	
		try {
			java.util.Date date = formatter.parse(statDateStr); 
			statDate = new Date(date.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  		
		colid = c.getColumnIndex(EmpWorkTimeTable.MODIFIED);
	 	modified = c.getInt(colid);
	 }
 
	public String toString() {
		String res;
		res = String.format("%s; Rx=%d; Tx=%d\n RxDATA=%d; TxDATA=%d", 
				statDateStr, rx_bytes, tx_bytes, rx_json_bytes, tx_json_bytes);
		return res;
	}
	

}
