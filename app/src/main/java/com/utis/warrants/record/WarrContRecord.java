package com.utis.warrants.record;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.database.Cursor;

import com.utis.warrants.CommonClass;
import com.utis.warrants.tables.WarrContTable;

public class WarrContRecord {
	public static final String JOB_NM = "JOB_NM";
	public static final String EQ_NM = "EQ_NM";
	public static final String EQ_STATE_NM = "EQ_STATE_NM";
	public static final String JOB_STATE_NM = "JOB_STATE_NM";
	public static final String EQ_SPEC_PLACE = "EQ_SPEC_PLACE";
	public static final String EQ_SPEC_BDATE = "EQ_SPEC_BDATE";
	public static final String EQ_SPEC_EDATE = "EQ_SPEC_EDATE";
	public static final String EQ_SPEC_TMP = "EQ_SPEC_TMP";
	
	
	public int id;
	public long id_external, id_warrant;
	public int id_eq, id_job, job_status, eq_status;
	public String eq_srl, eq_inv;
	public long eq_spec;
	public String remark, jobName, eqName;
	public String eqStateName, jobStateName;
	public int jobCount, modified, rec_stat, tbl_mode;
	public String specPlace;
	public int specTmp;
	public String beginDate, endDate;
	public Date specBeginDate, specEndDate;
	public boolean selected, eqInService = false;
//	private String tmpDateStr;
	
	
	   /**
     * Constructor. 
     */
    public WarrContRecord() {
    	 id = 0;
    }

    public WarrContRecord(Cursor c) {
		int colid = c.getColumnIndex(WarrContTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(WarrContTable.ID_EXTERNAL);
    	id_external = c.getLong(colid);
		colid = c.getColumnIndex(WarrContTable.ID_WARRANT);
    	id_warrant = c.getLong(colid);    	
		colid = c.getColumnIndex(WarrContTable.ID_EQ);
    	id_eq = c.getInt(colid);
    	eqName = "" + id_eq;
		colid = c.getColumnIndex(WarrContTable.ID_JOB);
    	id_job = c.getInt(colid);
    	jobName = "" + id_job;
		colid = c.getColumnIndex(WarrContTable.JOB_STATUS);
    	job_status = c.getInt(colid);
		colid = c.getColumnIndex(WarrContTable.EQ_STATUS);
    	eq_status = c.getInt(colid);
		colid = c.getColumnIndex(WarrContTable.EQ_SPEC);
		eq_spec = c.getLong(colid);

		colid = c.getColumnIndex(EQ_STATE_NM);
		if (colid >= 0) eqStateName = c.getString(colid);
		colid = c.getColumnIndex(JOB_STATE_NM);
		if (colid >= 0) jobStateName = c.getString(colid);

    	colid = c.getColumnIndex(WarrContTable.EQ_SRL);
    	eq_srl = c.getString(colid);
    	colid = c.getColumnIndex(WarrContTable.EQ_INV);
    	eq_inv = c.getString(colid);
		colid = c.getColumnIndex(WarrContTable.REMARK);
    	remark = c.getString(colid);
		colid = c.getColumnIndex(WarrContTable.MODIFIED);
    	modified = c.getInt(colid);
		colid = c.getColumnIndex(WarrContTable.TBL_MODE);
    	tbl_mode = c.getInt(colid);
		colid = c.getColumnIndex(WarrContTable.REC_STAT);
    	rec_stat = c.getInt(colid);
    	if (modified == 0 && id == 0) modified = 1;
    	
		colid = c.getColumnIndex(EQ_SPEC_PLACE);
		if (colid >= 0) specPlace = c.getString(colid);
		if (specPlace == null) specPlace = CommonClass.noData;
		colid = c.getColumnIndex(EQ_SPEC_TMP);
		if (colid >= 0) specTmp = c.getInt(colid);

    	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");    	
		colid = c.getColumnIndex(EQ_SPEC_BDATE);
		if (colid >= 0) {
			beginDate = c.getString(colid);
	    	if (beginDate != null) {
				try {
					java.util.Date date = formatter.parse(beginDate); 
					specBeginDate = new Date(date.getTime());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
		}
		colid = c.getColumnIndex(EQ_SPEC_EDATE);
		if (colid >= 0) {
			endDate = c.getString(colid);
	    	if (endDate != null) {
				try {
					java.util.Date date = formatter.parse(endDate); 
					specEndDate = new Date(date.getTime());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
		}
    }
    
	public String toString() {
//		return id_external + " " + job_status;
//		return String.format("%d %s [%d]; %s #%s [%d] {%s}; работ: %d", id_external, jobName, job_status,
//				eqName, eq_srl, eq_status, remark, jobCount);
//		return String.format("%s [%d]; %s #%s [%d] {%s}; работ: %d", jobName, job_status,
//				eqName, eq_srl, eq_status, remark, jobCount);
		return String.format("%s #%s [%d]; \n%s [%d];\n {%s}; работ: %d; id_warr :%d; eq_spec: %d", eqName, eq_srl, eq_status,
				jobName, job_status, remark, jobCount, id_warrant, beginDate);
	}
}
