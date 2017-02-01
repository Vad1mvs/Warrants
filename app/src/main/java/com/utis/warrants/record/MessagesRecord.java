package com.utis.warrants.record;


import java.sql.Date;
import java.text.ParseException;

import android.database.Cursor;

import com.utis.warrants.DBSchemaHelper;
import com.utis.warrants.tables.MessagesTable;

public class MessagesRecord {
	public static final int MSG_NEW = 130;
	public static final int MSG_READ = 131;
	public static final int MSG_ORDINARY = 1;
	public static final int MSG_IMPORTANT = 2;
	public int id, id_external, id_sender, id_recipient;
	public int id_state, status;
	public Date m_date, m_dateCh;
	public java.util.Date mDate, mDateCh;
	public String subj, msg;
	public String senderName, recipientName, a_nm;
	public int a_size, modified, rec_stat;
	public boolean selected;
	public byte[] senderPhoto, recipientPhoto, attachment;
	
	   /**
     * Constructor. 
     */
    public MessagesRecord() {
    	 id = 0;
    }

    public MessagesRecord(Cursor c) {
		int colid = c.getColumnIndex(MessagesTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(MessagesTable.ID_EXTERNAL);
    	id_external = c.getInt(colid);
		colid = c.getColumnIndex(MessagesTable.ID_SENDER);
    	id_sender = c.getInt(colid);
		colid = c.getColumnIndex(MessagesTable.ID_RECIPIENT);
    	id_recipient = c.getInt(colid);
		colid = c.getColumnIndex(MessagesTable.ID_STATE);
		id_state = c.getInt(colid);
		colid = c.getColumnIndex(MessagesTable.STATUS);
    	status = c.getInt(colid);
		colid = c.getColumnIndex(MessagesTable.MSG_DATE);
    	String dateStr = c.getString(colid);
		try {
			mDate = DBSchemaHelper.dateFormat.parse(dateStr);
			m_date = new Date(mDate.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		colid = c.getColumnIndex(MessagesTable.MSG_DATE_CHANGE);
    	dateStr = c.getString(colid);
    	if (dateStr == null || dateStr.length() == 0)
    		dateStr = DBSchemaHelper.dateFormatYYMM.format(m_date);
		try {
			mDateCh = DBSchemaHelper.dateFormatYYMM.parse(dateStr); 
			m_dateCh = new Date(mDateCh.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		colid = c.getColumnIndex(MessagesTable.SUBJ);
    	subj = c.getString(colid);
		colid = c.getColumnIndex(MessagesTable.MSG);
    	msg = c.getString(colid);
		colid = c.getColumnIndex(MessagesTable.ATTACHMENT);
    	attachment = c.getBlob(colid);
		colid = c.getColumnIndex(MessagesTable.ATTACH_NAME);
    	a_nm = c.getString(colid);
		colid = c.getColumnIndex(MessagesTable.ATTACH_SIZE);
    	a_size = c.getInt(colid);
		colid = c.getColumnIndex(MessagesTable.MODIFIED);
    	modified = c.getInt(colid);
		colid = c.getColumnIndex(MessagesTable.REC_STAT);
    	rec_stat = c.getInt(colid);
    }
    
	public String toString() {
		return String.format("S: %s; M: %s", subj, msg);
	}

	public String getDate() {
		String text = DBSchemaHelper.dateFormatMName.format(m_date);
		return text;
	}

}
