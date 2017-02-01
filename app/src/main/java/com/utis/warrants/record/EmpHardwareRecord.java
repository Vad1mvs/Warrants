package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.EmpHardwareTable;

public class EmpHardwareRecord {
	public static final String NM = "NM";
	public static final String SPENT_CNTR = "SPENT_CNTR";
	public static final int SEAL_ID = 2838; 

	public int id;
	public int id_gds;
	public double cntr, selected_cntr, avail_cntr; 
	public boolean selected;
	public String fnm;

	
	   /**
     * Constructor. 
     */
    public EmpHardwareRecord() {
    	 id = 0;
    }

    public EmpHardwareRecord(Cursor c) {
		int colid = c.getColumnIndex(EmpHardwareTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(EmpHardwareTable.ID_GDS);
    	id_gds = c.getInt(colid);
		colid = c.getColumnIndex(EmpHardwareTable.COUNT);
    	cntr = c.getDouble(colid);
		colid = c.getColumnIndex(SPENT_CNTR);
    	cntr -= c.getDouble(colid);
    	avail_cntr = cntr;
		colid = c.getColumnIndex(NM);
		if (colid >= 0) fnm = c.getString(colid);
    }
    
	public String toString() {
//		return id_gds + " " + fnm;
//		return Integer.toString(id_gds);
		return String.format("%s - %.2f", fnm, cntr) ;
	}

}
