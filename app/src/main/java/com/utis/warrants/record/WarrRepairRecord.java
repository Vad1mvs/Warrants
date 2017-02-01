package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.WarrRepairTable;
import com.utis.warrants.tables.WarrUnitTable;


public class WarrRepairRecord {
	public static final String NM = "NM";
	
	public int id;
	public long id_external;
	public long id_warr_unit;
	public long id_warrant;
	public int id_repair;
	public double quant;
	public double cost;
	public String remark;
	public String repairName;
	public int rec_stat;
	public int tbl_mode;
	public int modified;
	public boolean selected;

	   /**
     * Constructor. 
     */
    public WarrRepairRecord() {
    	 id = 0;
    }

    public WarrRepairRecord(Cursor c) {
		int colid = c.getColumnIndex(WarrRepairTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(WarrRepairTable.ID_EXTERNAL);
    	id_external = c.getLong(colid);
		colid = c.getColumnIndex(WarrRepairTable.ID_WARR_UNIT);
    	id_warr_unit = c.getLong(colid);    	
		colid = c.getColumnIndex(WarrRepairTable.ID_WARRANT);
    	id_warrant = c.getLong(colid);
		colid = c.getColumnIndex(WarrRepairTable.ID_REPAIR);
    	id_repair = c.getInt(colid);
		colid = c.getColumnIndex(WarrRepairTable.QUANT);
    	quant = c.getDouble(colid);
		colid = c.getColumnIndex(WarrRepairTable.COST);
    	cost = c.getDouble(colid);
		colid = c.getColumnIndex(WarrRepairTable.REMARK);
    	remark = c.getString(colid);
		colid = c.getColumnIndex(NM);
		if (colid >= 0) repairName = c.getString(colid);    	
		colid = c.getColumnIndex(WarrUnitTable.REC_STAT);
    	rec_stat = c.getInt(colid);
		colid = c.getColumnIndex(WarrRepairTable.TBL_MODE);
    	tbl_mode = c.getInt(colid);
		colid = c.getColumnIndex(WarrRepairTable.MODIFIED);
    	modified = c.getInt(colid);
//			System.out.println("GOT STUDENT " + name + " FROM " + state);
    }
    
	public String toString() {
//		return id_external + " " + id_repair;
//		return String.format("%d %s - %.2f", id_external, repairName, quant);
		return String.format("%s - %.2f", repairName, quant);
	}

}
