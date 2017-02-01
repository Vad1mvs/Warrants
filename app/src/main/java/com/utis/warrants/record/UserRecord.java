package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.UserTable;

public class UserRecord {
	public int id;
	public int id_external;
	public String surname, name, patronimic;
	
	   /**
     * Constructor. 
     */
    public UserRecord() {
    	 id = 0;
    }

    public UserRecord(Cursor c) {
		int colid = c.getColumnIndex(UserTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(UserTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(UserTable.SURNAME);
    	surname = c.getString(colid);
		colid = c.getColumnIndex(UserTable.NAME);
    	name = c.getString(colid);
		colid = c.getColumnIndex(UserTable.PATRONIMIC);
    	patronimic = c.getString(colid);
    }
    
	public String toString() {
//		return surname + " " + name.substring(0, 1) + ". " + patronimic.substring(0, 1) + ".";
		return "" + id_external;
	}

}
