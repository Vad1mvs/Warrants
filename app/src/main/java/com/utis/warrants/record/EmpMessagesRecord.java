package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.EmpTable;

public class EmpMessagesRecord {
	public static final String SEND_MSG_CNTR_COL_NAME = "S_CNT";
	public static final String REC_MSG_CNTR_COL_NAME = "R_CNT";
	public String surname, name, patronimic, combinedName;
	public long id_external;
	public int id_ent;
	public int sentMsgCnt, recievedMsgCnt;
	public boolean hasNewMsgs, hasNewImpMsgs, hasModifiedMsgs;
	public byte[] photo;

	   /**
	    * Constructor. 
	    */
	 public EmpMessagesRecord() {
		 combinedName = "";
	 }

	 public EmpMessagesRecord(String uName, int sCnt, int rCnt) {
		 combinedName = uName;
		 sentMsgCnt = sCnt;
		 recievedMsgCnt = rCnt;
	 }
	 
    public EmpMessagesRecord(Cursor c) {
		int colid = c.getColumnIndex(EmpTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(EmpTable.ID_ENT);
    	id_ent = c.getInt(colid);
		colid = c.getColumnIndex(EmpTable.SURNAME);
    	surname = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.NAME);
    	name = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.PATRONIMIC);
    	patronimic = c.getString(colid);
    	combinedName = surname + " " + name.substring(0, 1) + ". " + patronimic.substring(0, 1) + ".";
		colid = c.getColumnIndex(SEND_MSG_CNTR_COL_NAME);
    	sentMsgCnt = c.getInt(colid);
		colid = c.getColumnIndex(REC_MSG_CNTR_COL_NAME);
    	recievedMsgCnt = c.getInt(colid);    	
		colid = c.getColumnIndex(EmpTable.PHOTO);
    	photo = c.getBlob(colid);
    }
	 
	 
}
