package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.ConstTable;

public class ConstRecord {
	
	public int id;
	public int id_external;
	public int type_id;
	public int ttype;
	public String nmval;
	public String nm;

	
	   /**
     * Constructor. 
     */
    public ConstRecord() {
    	 id = 0;
    }

    public ConstRecord(Cursor c) {
		int colid = c.getColumnIndex(ConstTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(ConstTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(ConstTable.TYPE_ID);
    	type_id= c.getInt(colid);
		colid = c.getColumnIndex(ConstTable.TTYPE);
    	ttype = c.getInt(colid);
		colid = c.getColumnIndex(ConstTable.NMVAL);
    	nmval = c.getString(colid);
		colid = c.getColumnIndex(ConstTable.NM);
    	nm = c.getString(colid);
    }
    
	public String toString() {
		return id_external + " " + nm;
	}
}

