package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.WarrUnitTable;

public class WarrUnitRecord {
	public static final String NM = "NM";
	
	public int id;
	public long id_external, id_warr_cont;
	public int id_job, job_status, eq_status;
	public String remark, jobName;
	public int rec_stat, tbl_mode, modified, repairCount;
	public boolean repairPartsEnabled, selected;

	   /**
     * Constructor. 
     */
    public WarrUnitRecord() {
    	 id = 0;
    }

    public WarrUnitRecord(Cursor c) {
		int colid = c.getColumnIndex(WarrUnitTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(WarrUnitTable.ID_EXTERNAL);
//		if (colid > 0)
			id_external = c.getLong(colid);
//		else
//			id_external = 0;
		colid = c.getColumnIndex(WarrUnitTable.ID_WARR_CONT);
    	id_warr_cont = c.getLong(colid);    	
		colid = c.getColumnIndex(WarrUnitTable.ID_JOB);
    	id_job = c.getInt(colid);
    	jobName = "" + id_job;
		colid = c.getColumnIndex(WarrUnitTable.JOB_STATUS);
    	job_status = c.getInt(colid);
		colid = c.getColumnIndex(WarrUnitTable.EQ_STATUS);
    	eq_status = c.getInt(colid);
		colid = c.getColumnIndex(WarrUnitTable.REMARK);
    	remark = c.getString(colid);
    	colid = c.getColumnIndex(NM);
		if (colid >= 0) jobName = c.getString(colid);
		colid = c.getColumnIndex(WarrUnitTable.REC_STAT);
    	rec_stat = c.getInt(colid);
		colid = c.getColumnIndex(WarrUnitTable.TBL_MODE);
    	tbl_mode = c.getInt(colid);
		colid = c.getColumnIndex(WarrUnitTable.MODIFIED);
    	modified = c.getInt(colid);
//			System.out.println("GOT STUDENT " + name + " FROM " + state);
    }
    
	public String toString() {
//		return id_external + " " + id_job;
//		return String.format("%d %s {%s} [з/ч: %d]", id_external, jobName, remark, repairCount);
		if (repairPartsEnabled) {
			return String.format("%s {%s} [з/ч: %d]", jobName, remark, repairCount);
		} else {
			return String.format("%s {%s}", jobName, remark);
		}
	}

}
