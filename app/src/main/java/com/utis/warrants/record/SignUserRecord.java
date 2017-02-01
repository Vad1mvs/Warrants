package com.utis.warrants.record;


import android.database.Cursor;

public class SignUserRecord {
	public int id;
	public String userOnline;
	public String userName;
	public String userSurName;
	public String userSfx;
	public String userId;
	
	
	   /**
  * Constructor. 
  */
	public SignUserRecord() {
		id = 0;
	}

//	public SignUserRecord(Cursor c) {
//		int colid = c.getColumnIndex(LocationTable.ID);
//		id = c.getInt(colid);
//	}
 
	public String toString() {
		return String.format("%s %s", userName, userSurName);
	}


}
