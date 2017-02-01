package com.utis.warrants.record;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.database.Cursor;

import com.utis.warrants.tables.WarrantTable;

public class WarrantRecord {
	public static final int WARRANT_DELETED = 2;
	public static final int W_DONE = 1;
	public static final int W_IN_PROGRESS = 0;
	public static final int W_STATE_NEW_DISCARD = 4;
	public static final int W_STATE_IN_WORK = 5;
	public static final int W_STATE_CLOSED = 6;
	public static final int W_STATE_COMPLETED = 23;
	public static final int W_REPORTED_STATE_MASK = 0x80000000;
	public static final String WARR_STATE_NM = "WARR_STATE_NM";
	public int id;
	public long id_external, id_contract;
	public int num, id_state, regl;
	public String warrStateName;
	public int id_ent, id_subent;
	public int id_owner, to_sign, id_user_sign;
	public Date d_sign, d_open;
	public String d_sign_str, d_open_str, remark, d_change_str;
	public String signEmpName;
	public String entName, subEntName, entAddress;
	public double entLat, entLng;
	public int jobCount, modified, localState;
	public boolean jobMarkStarted = false;
	public boolean jobMarkModified = false;
	public boolean reported = false;
	
	   /**
     * Constructor. 
     */
    public WarrantRecord() {
    	 id = 0;
    }

    public WarrantRecord(Cursor c) {
		int colid = c.getColumnIndex(WarrantTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(WarrantTable.ID_EXTERNAL);
    	id_external = c.getLong(colid);
		colid = c.getColumnIndex(WarrantTable.NUM);
    	num	= c.getInt(colid);    	
		colid = c.getColumnIndex(WarrantTable.ID_CONTRACT);
    	id_contract = c.getLong(colid);
		colid = c.getColumnIndex(WarrantTable.ID_ENT);
    	id_ent = c.getInt(colid);
    	entName = "" + id_ent;
		colid = c.getColumnIndex(WarrantTable.ID_SUBENT);
    	id_subent = c.getInt(colid);
    	subEntName = "" + id_subent;
    	colid = c.getColumnIndex(WarrantTable.IDSTATE);
    	id_state = c.getInt(colid) & 0x7FFFFFFF;
    	reported = (c.getInt(colid) & W_REPORTED_STATE_MASK) != 0;
    	
		colid = c.getColumnIndex(WARR_STATE_NM);
		if (colid >= 0) warrStateName = c.getString(colid);
    	
		colid = c.getColumnIndex(WarrantTable.D_OPEN);
    	d_open_str = c.getString(colid);
    	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");    	
		try {
			java.util.Date date = formatter.parse(d_open_str); 
			d_open = new Date(date.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		colid = c.getColumnIndex(WarrantTable.DATE_CHANGE);
    	d_change_str = c.getString(colid);
		colid = c.getColumnIndex(WarrantTable.REMARK);
    	remark = c.getString(colid);
		colid = c.getColumnIndex(WarrantTable.ID_OWNER);
    	id_owner = c.getInt(colid);    	
		colid = c.getColumnIndex(WarrantTable.TO_SIGN);
    	to_sign = c.getInt(colid);    	
		colid = c.getColumnIndex(WarrantTable.ID_USER_SIGN);
    	id_user_sign = c.getInt(colid);    	
		colid = c.getColumnIndex(WarrantTable.D_OPEN);
    	d_sign_str = c.getString(colid);
    	formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");    	
		try {
			java.util.Date date = formatter.parse(d_sign_str); 
			d_sign = new Date(date.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		colid = c.getColumnIndex(WarrantTable.MODIFIED);
    	modified = c.getInt(colid);
		colid = c.getColumnIndex(WarrantTable.LOCAL_STATE);
    	localState = c.getInt(colid);
       colid = c.getColumnIndex(WarrantTable.REGL);
        regl = c.getInt(colid);
    }

    public WarrantRecord(int ext_id, int num, int idEnt, int idSubEnt, int state, Date d_open, String rem) {
    	id_external = ext_id;
    	this.num = num;    	
    	id_ent = idEnt;
    	id_subent = idSubEnt;
    	id_state = state;
    	this.d_open = d_open;
    	remark = rem;
    	modified = 0;
       // regl = regl;
    }

	public String toString() {
		return String.format("№%d от %s\n(%s; %s)\n{%s} работ: %d", num, d_open_str,
				entName, subEntName, remark, jobCount);
	}

	public String getNumDate() {
		return String.format("№%d от %s", num, getDate());
	}
	
	public String getDate() {
		SimpleDateFormat df = new SimpleDateFormat("d MMM yy; H:mm");  
		String text = df.format(d_open);
		return text;
	}
	
}
