package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.ReasonTable;

public class ReasonRecord {
	public int id, idExternal, sign;
	public String nm, dateChange;
	
	   /**
     * Constructor. 
     */
    public ReasonRecord() {
    	 id = 0;
    }

    public ReasonRecord(Cursor c) {
		int colid = c.getColumnIndex(ReasonTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(ReasonTable.ID_EXTERNAL);
    	idExternal = c.getInt(colid);
		colid = c.getColumnIndex(ReasonTable.SIGN);
    	sign = c.getInt(colid);
		colid = c.getColumnIndex(ReasonTable.CHANGE_DATE);
    	dateChange = c.getString(colid);
		colid = c.getColumnIndex(ReasonTable.NM);
    	nm = c.getString(colid);
    }
    
	public String toString() {
//		return idExternal + " " + nm;
		return nm;
	}

}
