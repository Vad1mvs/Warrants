package com.utis.warrants.record;

import java.text.ParseException;

import android.database.Cursor;

import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.tables.SealTable;

public class SealRecord {
	public int id, idExternal, modified;
	public String num, srl, inum, dateChange, iDateChange;
	
	   /**
     * Constructor. 
     */
    public SealRecord() {
    	 id = 0;
    }

    public SealRecord(Cursor c) {
		int colid = c.getColumnIndex(SealTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(SealTable.ID_EXTERNAL);
    	idExternal = c.getInt(colid);
		colid = c.getColumnIndex(SealTable.SRL);
    	srl = c.getString(colid);
		colid = c.getColumnIndex(SealTable.CHANGE_DATE);
    	dateChange = c.getString(colid);
		colid = c.getColumnIndex(SealTable.NUM);
    	num = c.getString(colid);
		colid = c.getColumnIndex(SealTable.INUM);
    	inum = c.getString(colid);
		colid = c.getColumnIndex(SealTable.I_CHANGE_DATE);
    	iDateChange = c.getString(colid);
		colid = c.getColumnIndex(SealTable.MODIFIED);
    	modified = c.getInt(colid);
    }
    
	public String toString() {
		return idExternal + " " + getSealInfo();
	}
	
	public String getSealInfo() {
		return srl + " " + num;
	}
	
	public String getSealInvInfo() {
	 	java.util.Date date;
	 	String text = "";
	 	try {
	 		date = DBSchemaHelper.dateFormatYYMM.parse(iDateChange);
			text = DBSchemaHelper.dateFormatMM.format(date);
	 	} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
	 	}
		return inum + " от " + text;
	}

}
