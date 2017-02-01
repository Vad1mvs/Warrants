package com.utis.warrants.record;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.database.Cursor;

import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.tables.LogsTable;

public class LogsRecord {
	public static final int EXCEPTION = 1;
	public static final int ERROR = 2;
	public static final int WARNING = 3;
	public static final int DEBUG = 4;
	public static final int INFO = 5;
	public long id;
	public int level, modified, num;
	public Date m_date;
	public String msg;

 public LogsRecord() {
 	 id = 0;
 }

 public LogsRecord(Cursor c) {
	int colid = c.getColumnIndex(LogsTable.ID);
	id = c.getInt(colid);
	colid = c.getColumnIndex(LogsTable.LEVEL);
	level = c.getInt(colid);
	colid = c.getColumnIndex(LogsTable.MSG_DATE);
// 	m_date = new Date(c.getLong(colid));
 	String date_str = c.getString(colid);
 	java.util.Date date;
 	try {
 		date = DBSchemaHelper.dateFormatMSYY.parse(date_str);
 		m_date = new Date(date.getTime());
 	} catch (ParseException e) {
 		try {
 			date = DBSchemaHelper.dateFormatYY.parse(date_str); 
 			m_date = new Date(date.getTime());
 		} catch (ParseException pe) {
 			// TODO Auto-generated catch block
 			pe.printStackTrace();
 		}  		 		
 	}
	colid = c.getColumnIndex(LogsTable.MSG);
 	msg = c.getString(colid);
	colid = c.getColumnIndex(LogsTable.MODIFIED);
 	modified = c.getInt(colid);
 }
 
	public String toString() {
		return String.format("%s; %s", getTime(), msg);
	}

	private String getDate() {
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");  
		String text = df.format(m_date);
		return text;
	}

	public String getTime() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");  
		String text = df.format(m_date);
		return text;
	}

}
