package com.utis.warrants.record;

import java.sql.Date;
import java.text.ParseException;

import android.database.Cursor;

import com.utis.warrants.CommonClass;
import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.tables.EmpWorkTimeTable;

public class EmpWorkTimeRecord {
	public static final int BEGIN_WORK = 0;
	public static final int END_WORK = 1;
	public int id;
	public int id_external;
	public int id_ent;
	public long id_warrant;
	public Date dtbe;
	public java.util.Date date;
	public int be, modified;
	public double lat, lng;
	private long interval;
	private String markDateStr, intervalStr;
	
	   /**
	    * Constructor. 
	   */
	public EmpWorkTimeRecord() {
		id = 0;
	}

	public EmpWorkTimeRecord(Cursor c) {
		int colid = c.getColumnIndex(EmpWorkTimeTable.ID);
	 	id = c.getInt(colid);
		colid = c.getColumnIndex(EmpWorkTimeTable.ID_EXTERNAL);
	 	id_external = c.getInt(colid);
		colid = c.getColumnIndex(EmpWorkTimeTable.ID_ENT);
	 	id_ent = c.getInt(colid);
		colid = c.getColumnIndex(EmpWorkTimeTable.ID_WARRANT);
	 	id_warrant = c.getLong(colid);
		colid = c.getColumnIndex(EmpWorkTimeTable.DTBE);
    	markDateStr = c.getString(colid);
		try {
			date = DBSchemaHelper.dateFormatYY.parse(markDateStr);
			dtbe = new Date(date.getTime());
			markDateStr = DBSchemaHelper.date2YFormat.format(dtbe);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  		
		colid = c.getColumnIndex(EmpWorkTimeTable.BE);
	 	be = c.getInt(colid);
		colid = c.getColumnIndex(EmpWorkTimeTable.LAT);
		lat = c.getDouble(colid);
		colid = c.getColumnIndex(EmpWorkTimeTable.LNG);
		lng = c.getDouble(colid);
		colid = c.getColumnIndex(EmpWorkTimeTable.MODIFIED);
	 	modified = c.getInt(colid);
	 	interval = 0;
	 }
 
	public String getMarkDateStr() {
		return markDateStr;
	}
	
	public String getIntervalStr() {
		return intervalStr;
	}
	
	public long getInterval() {
		return interval;
	}
	
	public void setInterval(long interval) {
		this.interval = interval;
		intervalStr = CommonClass.printSecondsInterval(interval);
	}
	
	public String toString() {
//		return id_ent + " " + id_warrant + " " + be + " " + dtbe.toString();
		String res;
//		res = (be == 0) ? "Начало работ: " : "Окончание работ: ";
		res = (be == 0) ? "Начало: " : "Окончание: ";
		res += markDateStr + " (id=" + id_external + ")";
		return res;
	}
	

}
