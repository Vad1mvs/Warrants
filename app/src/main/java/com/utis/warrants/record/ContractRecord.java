package com.utis.warrants.record;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.database.Cursor;

import com.utis.warrants.tables.ContractTable;

public class ContractRecord {
	public long id, id_external;
	public int id_ent, id_ent_u, id_owner, id_state;
	public String name;
	public String no;
	public Date begin_date;
	public Date end_date;
	private String DateStr;
	
	   /**
     * Constructor. 
     */
    public ContractRecord() {
    	 id = 0;
    }

    public ContractRecord(Cursor c) {
		int colid = c.getColumnIndex(ContractTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(ContractTable.ID_EXTERNAL);
    	id_external = c.getLong(colid);
		colid = c.getColumnIndex(ContractTable.ID_ENT);
    	id_ent = c.getInt(colid);
		colid = c.getColumnIndex(ContractTable.ID_ENT_U);
    	id_ent_u = c.getInt(colid);
		colid = c.getColumnIndex(ContractTable.ID_OWNER);
    	id_owner = c.getInt(colid);
		colid = c.getColumnIndex(ContractTable.ID_STATE);
    	id_state = c.getInt(colid);
		colid = c.getColumnIndex(ContractTable.NO);
    	no = c.getString(colid);
		colid = c.getColumnIndex(ContractTable.NAME);
    	name = c.getString(colid);
    	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");    	
		colid = c.getColumnIndex(ContractTable.BEGIN_DATE);
    	DateStr = c.getString(colid);
    	if (DateStr != null) {
			try {
				java.util.Date date = formatter.parse(DateStr); 
				begin_date = new Date(date.getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		colid = c.getColumnIndex(ContractTable.END_DATE);
    	DateStr = c.getString(colid);
    	if (DateStr != null) {
			try {
				java.util.Date date = formatter.parse(DateStr); 
				end_date = new Date(date.getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
	public String toString() {
		return no + "; " + name;
	}

}
