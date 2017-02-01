package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.StateTable;

public class StateRecord {
	public static final int EQ_STATE_IN_ORDER = 49;
	public static final int EQ_STATE_NOTIN_ORDER = 48;
	public static final int JOB_STATE_IS_DONE = 22;
	public static final int JOB_STATE_TODO = 21;
	public static final int JOB_STATE_IS_NOTDONE = 38;

	public int id, id_external, id_doc;
//	public int no;
	public String nm;

	
	   /**
     * Constructor. 
     */
    public StateRecord() {
    	 id = 0;
    }

    public StateRecord(Cursor c) {
		int colid = c.getColumnIndex(StateTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(StateTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(StateTable.ID_DOC);
    	id_doc = c.getInt(colid);
//		colid = c.getColumnIndex(StateTable.NO);
//    	no = c.getInt(colid);
		colid = c.getColumnIndex(StateTable.NM);
    	nm = c.getString(colid);
    }
    
	public String toString() {
		return id_external + " " + nm;
	}
}
