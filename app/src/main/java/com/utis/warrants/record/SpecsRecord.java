package com.utis.warrants.record;

import java.sql.Date;
import java.text.ParseException;

import android.database.Cursor;

import com.utis.warrants.CommonClass;
import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.tables.SpecsTable;

public class SpecsRecord {
	public static final String SPECS_EQ_NM = "SPECS_EQ_NM";
	public static final String SPECS_EQ_NM_2 = "SPECS_EQ_NM_2";
	public static final String SPECS_EQ_TYPE = "SPECS_EQ_TYPE";
	public int id, id_external, id_ent, id_contract, id_gds, temp, numCash;
	public String fisk_num, place, srl;
	public String inv, ipModem, ipCash, eq_name, eq_name_2;
	public Date change_date, begin_date, end_date;
	public String beginDate, endDate;
	public boolean eqInService = false;
	private String DateStr;
	public boolean selected;
	public int modified, factorySeal, gdsType;
	
	   /**
     * Constructor. 
     */
    public SpecsRecord() {
    	 id = 0;
    }

    public int getId_external() {
        return id_external;
    }

    public void setId_external(int id_external) {
        this.id_external = id_external;
    }

    public String getSrl() {
        return srl;
    }

    public void setSrl(String srl) {
        this.srl = srl;
    }

    public SpecsRecord(Cursor c) {
		int colid = c.getColumnIndex(SpecsTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(SpecsTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(SpecsTable.ID_ENT);
    	id_ent = c.getInt(colid);
		colid = c.getColumnIndex(SpecsTable.ID_CONTRACT);
    	id_contract = c.getInt(colid);
		colid = c.getColumnIndex(SpecsTable.ID_GDS);
    	id_gds = c.getInt(colid);
		colid = c.getColumnIndex(SpecsTable.TEMP);
    	temp = c.getInt(colid);
		colid = c.getColumnIndex(SpecsTable.FISK_NUM);
    	fisk_num = c.getString(colid);
		colid = c.getColumnIndex(SpecsTable.PLACE);
    	place = c.getString(colid);
		colid = c.getColumnIndex(SpecsTable.SRL);
    	srl = c.getString(colid);
		colid = c.getColumnIndex(SpecsTable.INV);
    	inv = c.getString(colid);
		colid = c.getColumnIndex(SpecsTable.IP_MODEM);
    	ipModem = c.getString(colid);
		colid = c.getColumnIndex(SpecsTable.IP_CASH);
    	ipCash = c.getString(colid);
		colid = c.getColumnIndex(SpecsTable.NUM_CASH);
    	numCash = c.getInt(colid);
		colid = c.getColumnIndex(SpecsTable.FACTORY_SEAL);
    	factorySeal = c.getInt(colid);
		colid = c.getColumnIndex(SpecsTable.MODIFIED);
    	modified = c.getInt(colid);
    	
		colid = c.getColumnIndex(SPECS_EQ_NM);
		if (colid >= 0) eq_name = c.getString(colid);

        colid = c.getColumnIndex(SPECS_EQ_NM_2);
        if (colid >= 0) eq_name_2 = c.getString(colid);

		colid = c.getColumnIndex(SPECS_EQ_TYPE);
		if (colid >= 0) gdsType = c.getInt(colid);


    	
		colid = c.getColumnIndex(SpecsTable.CHANGE_DATE);
    	DateStr = c.getString(colid);
    	if (DateStr != null)
        {
			try {
				java.util.Date date = DBSchemaHelper.dateFormatYYMM.parse(DateStr);
				change_date = new Date(date.getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		colid = c.getColumnIndex(SpecsTable.BEGIN_DATE);
    	beginDate = c.getString(colid);
    	if (beginDate != null) {
			try {
				java.util.Date date = DBSchemaHelper.dateFormatDay.parse(beginDate);
				begin_date = new Date(date.getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else {
    		beginDate = CommonClass.noData;
    	}
		colid = c.getColumnIndex(SpecsTable.END_DATE);
		endDate = c.getString(colid);
    	if (endDate != null) {
			try {
				java.util.Date date = DBSchemaHelper.dateFormatDay.parse(endDate);
				end_date = new Date(date.getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else {
    		endDate = CommonClass.noData;
    	}
    }

	public String toString() {
		return  eq_name;
    }
}
