package com.utis.warrants.record;

import java.text.ParseException;
import java.util.Date;

import android.database.Cursor;

import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.tables.SealCertTable;

public class SealCertRecord {
	public int id, idExternal, modified, idEmp, idReason, idOwner, idWarrCont, factorySeal, idSpecs;
	public String num, dateChange, dateBegin, dateEnd, defect;
	public String dateChangeFmt, dateBeginFmt, dateEndFmt;
	public double ppoMoney;
	public Date beginDate, endDate;
	public String eqName, eqInv, eqSrl, eqFisk, entName, seals;
	public int sealCnt = 0;
	public int warrNum;
	
	   /**
     * Constructor. 
     */
    public SealCertRecord() {
    	 id = 0;
    }

    public SealCertRecord(Cursor c) {
		int colid = c.getColumnIndex(SealCertTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(SealCertTable.ID_EXTERNAL);
    	idExternal = c.getInt(colid);
		colid = c.getColumnIndex(SealCertTable.ID_EMP);
    	idEmp = c.getInt(colid);
		colid = c.getColumnIndex(SealCertTable.ID_REASON);
    	idReason = c.getInt(colid);
		colid = c.getColumnIndex(SealCertTable.ID_OWNER);
    	idOwner = c.getInt(colid);
		colid = c.getColumnIndex(SealCertTable.ID_WARR_CONT);
    	idWarrCont = c.getInt(colid);
		colid = c.getColumnIndex(SealCertTable.ID_SPECS);
    	idSpecs = c.getInt(colid);
		colid = c.getColumnIndex(SealCertTable.FACTORY_SEAL);
    	factorySeal = c.getInt(colid);    	

    	colid = c.getColumnIndex(SealCertTable.DEFECT);
    	defect = c.getString(colid);
		colid = c.getColumnIndex(SealCertTable.CHANGE_DATE);
    	dateChange = c.getString(colid);
    	try {
			beginDate = DBSchemaHelper.dateFormatYYMM.parse(dateChange);
			dateChangeFmt = DBSchemaHelper.dateFormatMName.format(beginDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dateChangeFmt = dateChange;
		}    	
		colid = c.getColumnIndex(SealCertTable.BEGIN_DATE);
    	dateBegin = c.getString(colid);
    	try {
    		beginDate = DBSchemaHelper.dateFormatYYMM.parse(dateBegin);
			dateBeginFmt = DBSchemaHelper.dateFormatMName.format(beginDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dateBeginFmt = dateBegin;
		}    	    	
		colid = c.getColumnIndex(SealCertTable.END_DATE);
    	dateEnd = c.getString(colid);
    	try {
    		endDate = DBSchemaHelper.dateFormatYYMM.parse(dateEnd);
			dateEndFmt = DBSchemaHelper.dateFormatMName.format(endDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dateEndFmt = dateEnd;
		}    	
		colid = c.getColumnIndex(SealCertTable.PPO_MONEY);
    	ppoMoney = c.getDouble(colid);
		colid = c.getColumnIndex(SealCertTable.MODIFIED);
    	modified = c.getInt(colid);
		colid = c.getColumnIndex(SealCertTable.NUM);
    	num = c.getString(colid);
    	entName = "";
    	eqName = ""; 
    	eqInv = ""; 
    	eqSrl = "";
    	eqFisk = "";
    }
    
    public String getNum() {
    	if (idExternal > 0) {
    		if (num != null && num.length() > 0) {
    			return num;
    		} else {
    			return "" + id + ":" + idExternal;
    		}
    	} else
    		return "" + id;
    }
    
	public String toString() {
		return idExternal + " " + idWarrCont + " " + idEmp;
	}

}
