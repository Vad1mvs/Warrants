package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.GoodsTable;

public class GoodsRecord {
	//public static final int SEALJOBARRAY[] = {};
	public static final int SEAL_JOB_PARENT = 3448;
	public static final int JOB_ENABLE_EMP_HW = 4;
	public static final int GDS_IS_PPO = 2;
	public static final String GDS_CHILD_CNT = "GDS_CHILD_CNT";
	public int id, id_external, id_parent;
	public int ttype, gds_type, child_cnt;
	public String fnm;

	
	   /**
     * Constructor. 
     */
    public GoodsRecord() {
    	 id = 0;
    }

    public GoodsRecord(Cursor c) {
		int colid = c.getColumnIndex(GoodsTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(GoodsTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(GoodsTable.ID_PARENT);
    	id_parent = c.getInt(colid);
		colid = c.getColumnIndex(GoodsTable.TTYPE);
    	ttype = c.getInt(colid);
		colid = c.getColumnIndex(GoodsTable.GDSTYPE);
    	gds_type = c.getInt(colid);

		colid = c.getColumnIndex(GDS_CHILD_CNT);
		child_cnt = c.getInt(colid);
		colid = c.getColumnIndex(GoodsTable.NM);
    	fnm = c.getString(colid);
    }
    
	public String toString() {
		return  fnm +"/  "+id_external +"/  "+id_parent +"/  "+ttype +"/  "+gds_type ;
	}
	
	
}
