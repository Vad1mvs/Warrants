package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.EntTable;

public class EntRecord {
	public int id, id_external, id_parent;
	public double lat, lng;
	public String nm, addr;
	
	   /**
     * Constructor. 
     */
    public EntRecord() {
    	 id = 0;
    }

    public EntRecord(Cursor c) {
		int colid = c.getColumnIndex(EntTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(EntTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(EntTable.ID_PARENT);
    	id_parent = c.getInt(colid);
		colid = c.getColumnIndex(EntTable.LAT);
    	lat = c.getDouble(colid);
		colid = c.getColumnIndex(EntTable.LNG);
    	lng = c.getDouble(colid);
		colid = c.getColumnIndex(EntTable.ADDR);
    	addr = c.getString(colid);
		colid = c.getColumnIndex(EntTable.NM);
    	nm = c.getString(colid);
    }
    
	public String toString() {
		return id_external + " " + nm;
	}

}
