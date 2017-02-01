package com.utis.warrants.record;

import android.database.Cursor;

import com.utis.warrants.tables.SealCertSealsTable;

public class SealCertSealsRecord {
	public int id, idSealCert, modified, idSeals;
	public String sealInfo;
	
	   /**
     * Constructor. 
     */
    public SealCertSealsRecord() {
    	 id = 0;
    }

    public SealCertSealsRecord(Cursor c) {
		int colid = c.getColumnIndex(SealCertSealsTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(SealCertSealsTable.ID_SEAL_CERT);
    	idSealCert = c.getInt(colid);
		colid = c.getColumnIndex(SealCertSealsTable.ID_SEAL_EXTERNAL);
    	idSeals = c.getInt(colid);
		colid = c.getColumnIndex(SealCertSealsTable.MODIFIED);
    	modified = c.getInt(colid);
		colid = c.getColumnIndex(SealCertSealsTable.SEAL_INFO);
    	sealInfo = c.getString(colid);
    }
    
	public String toString() {
		return sealInfo;
	}


}
