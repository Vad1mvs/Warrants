package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.EmpTable;

public class EmpRecord {
	public int id;
	public int id_external;
	public int id_ent;
	public String surname, name, patronimic;
	public byte[] photo;
	public boolean selected;
	
	   /**
     * Constructor. 
     */
    public EmpRecord() {
    	 id = 0;
    }

    public EmpRecord(Cursor c) {
		int colid = c.getColumnIndex(EmpTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(EmpTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(EmpTable.ID_ENT);
    	id_ent = c.getInt(colid);
		colid = c.getColumnIndex(EmpTable.SURNAME);
    	surname = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.NAME);
    	name = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.PATRONIMIC);
    	patronimic = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.PHOTO);
    	photo = c.getBlob(colid);
    }
    
	public String toString() {
		return surname + " /name:    " + name.substring(0, 1) + ". " + patronimic.substring(0, 1) + ". " +
                " / name:" + name + " / patronimic " + patronimic;
	}

}
