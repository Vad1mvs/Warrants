package com.utis.warrants;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.database.DatabaseUtils.InsertHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import com.utis.warrants.record.EmpWorkTimeRecord;
import com.utis.warrants.record.GoodsRecord;
import com.utis.warrants.record.LocationRecord;
import com.utis.warrants.record.MessagesRecord;
import com.utis.warrants.record.SealCertRecord;
import com.utis.warrants.record.SealCertSealsRecord;
import com.utis.warrants.record.SpecsRecord;
import com.utis.warrants.record.StateRecord;
import com.utis.warrants.record.TrafficStatRecord;
import com.utis.warrants.record.WarrContRecord;
import com.utis.warrants.record.WarrRepairRecord;
import com.utis.warrants.record.WarrUnitRecord;
import com.utis.warrants.record.WarrantRecord;
import com.utis.warrants.tables.ConstTable;
import com.utis.warrants.tables.ContractTable;
import com.utis.warrants.tables.EmpHardwareTable;
import com.utis.warrants.tables.EmpTable;
import com.utis.warrants.tables.EmpWorkTimeTable;
import com.utis.warrants.tables.EntTable;
import com.utis.warrants.tables.GoodsTable;
import com.utis.warrants.tables.LocationTable;
import com.utis.warrants.tables.LogsTable;
import com.utis.warrants.tables.MessagesTable;
import com.utis.warrants.tables.ReasonTable;
import com.utis.warrants.tables.SealCertSealsTable;
import com.utis.warrants.tables.SealCertTable;
import com.utis.warrants.tables.SealTable;
import com.utis.warrants.tables.SpecsTable;
import com.utis.warrants.tables.StateTable;
import com.utis.warrants.tables.TrafficStatTable;
import com.utis.warrants.tables.UserTable;
import com.utis.warrants.tables.WarrContTable;
import com.utis.warrants.tables.WarrRepairTable;
import com.utis.warrants.tables.WarrUnitTable;
import com.utis.warrants.tables.WarrantTable;

public class DBSchemaHelper extends SQLiteOpenHelper {
	private static final boolean D = true;
	private static final String TAG = "DBHelper";
	private static final String ZERO_CONDITION = " = 0 ";
	private static final String NOT_ZERO_CONDITION = " <> 0 ";
	public static final String REC_STAT_DEL_CONDITION = " = 2 ";
	public static final String REC_STAT_NODEL_CONDITION = " < 2 ";
	private static final String DATABASE_NAME = "content9.db";
	// TOGGLE THIS NUMBER FOR UPDATING TABLES AND DATABASE
	private static final int DATABASE_VERSION = 30;

	public static final SimpleDateFormat dateFormatYYMM = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
	
	public final SimpleDateFormat dateFormatDayYY = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd.MM.yyyy");
	
	public static final SimpleDateFormat dateFormatYY = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	public static final SimpleDateFormat date2YFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
	
	public final static SimpleDateFormat dateFormatMM = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	public final static SimpleDateFormat dateFormatMName = new SimpleDateFormat("d MMM yy; H:mm");
	
	public static final SimpleDateFormat dateFormatMSYY = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public final SimpleDateFormat dateFormatMS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

	
	private DBSchemaHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	private static DBSchemaHelper INSTANCE = null;

	public synchronized static DBSchemaHelper getInstance(Context context) {
        if (INSTANCE == null)
        	INSTANCE = new DBSchemaHelper(context);
        return INSTANCE;
    }

	

	@Override
	public void onCreate(SQLiteDatabase db) {
		// CREATE DB TABLES
		createCommonTables(db);
		createWarrantTables(db);
		if (D) { Log.d(TAG, "Tables created"); };
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 29 && newVersion == 30) {
            db.execSQL("CREATE INDEX msg_idx ON " + MessagesTable.TABLE_NAME + "(" + MessagesTable.ID + ");");
            db.execSQL("CREATE INDEX loc_idx ON " + LocationTable.TABLE_NAME + "(" + LocationTable.ID + ");");
            db.execSQL("CREATE INDEX contr_idx ON " + ContractTable.TABLE_NAME + "(" + ContractTable.ID + ");");
            db.execSQL("CREATE INDEX specs_idx ON " + SpecsTable.TABLE_NAME + "(" + SpecsTable.ID + ");");
            db.execSQL("CREATE INDEX seal_idx ON " + SealTable.TABLE_NAME + "(" + SealTable.ID + ");");
            db.execSQL("CREATE INDEX seal_cert_idx ON " + SealCertTable.TABLE_NAME + "(" + SealCertTable.ID + ");");
            db.execSQL("CREATE INDEX seal_cert_seal_idx ON " + SealCertSealsTable.TABLE_NAME + "(" + SealCertSealsTable.ID + ");");
            db.execSQL("CREATE INDEX emp_wt_idx ON " + EmpWorkTimeTable.TABLE_NAME + "(" + EmpWorkTimeTable.ID + ");");
            db.execSQL("CREATE INDEX emp_idx ON " + EmpTable.TABLE_NAME + "(" + EmpTable.ID + ");");
        } else if (oldVersion == 28 && newVersion == 30) {
            db.execSQL("CREATE INDEX gds_idx ON " + GoodsTable.TABLE_NAME + "(" + GoodsTable.ID + ");");
            db.execSQL("CREATE INDEX gds_ext_idx ON " + GoodsTable.TABLE_NAME + "(" + GoodsTable.ID_EXTERNAL + ");");
            db.execSQL("CREATE INDEX ent_idx ON " + EntTable.TABLE_NAME + "(" + EntTable.ID + ");");
            db.execSQL("CREATE INDEX ent_ext_idx ON " + EntTable.TABLE_NAME + "(" + EntTable.ID_EXTERNAL + ");");
			db.execSQL("CREATE INDEX warrant_idx ON " + WarrantTable.TABLE_NAME + "(" + WarrantTable.ID + ");");
			db.execSQL("CREATE INDEX warrant_ext_idx ON " + WarrantTable.TABLE_NAME + "(" + WarrantTable.ID_EXTERNAL + ");");
			db.execSQL("CREATE INDEX warr_cont_idx ON " + WarrContTable.TABLE_NAME + "(" + WarrContTable.ID_WARRANT + ");");
            db.execSQL("CREATE INDEX warr_unit_idx ON " + WarrUnitTable.TABLE_NAME + "(" + WarrUnitTable.ID + ");");
            db.execSQL("CREATE INDEX warr_unit_idx ON " + WarrRepairTable.TABLE_NAME + "(" + WarrRepairTable.ID + ");");
            db.execSQL("CREATE INDEX log_idx ON "+ LogsTable.TABLE_NAME +"("+ LogsTable.ID +");");
            db.execSQL("CREATE INDEX traff_stat_idx ON "+ TrafficStatTable.TABLE_NAME +"("+ TrafficStatTable.ID +");");
		} else if (oldVersion == 27 && newVersion == 30) {
			String query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.DATE_CHANGE + " TEXT";
			db.execSQL(query);
            query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.REGL + " TEXT";////////////////////////
            db.execSQL(query);                                                                              ////////////////////////
		} else if (oldVersion == 26 && newVersion == 30) {
			String query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.MSG_DATE_CHANGE + " TEXT";
			db.execSQL(query);			
			query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.DATE_CHANGE + " TEXT";
			db.execSQL(query);			
		} else if (oldVersion == 25 && newVersion == 30) {
			String query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.ATTACHMENT + " BLOB";
			db.execSQL(query);			
			query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.MSG_DATE_CHANGE + " TEXT";
			db.execSQL(query);			
			query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.DATE_CHANGE + " TEXT";
			db.execSQL(query);			
		} else if (oldVersion == 24 && newVersion == 29) {
			String query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.ATTACHMENT + " BLOB";
			db.execSQL(query);			
			query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.MSG_DATE_CHANGE + " TEXT";
			db.execSQL(query);			
			query = "ALTER TABLE " + SealCertTable.TABLE_NAME + " ADD COLUMN " + SealCertTable.ID_SPECS + " INTEGER";
			db.execSQL(query);
			query = "ALTER TABLE " + SealCertSealsTable.TABLE_NAME + " ADD COLUMN " + SealCertSealsTable.SEAL_INFO + " TEXT";
			db.execSQL(query);
			query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.DATE_CHANGE + " TEXT";
			db.execSQL(query);			
		} else if (oldVersion == 23 && newVersion == 29) {
			String query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.ATTACHMENT + " BLOB";
			db.execSQL(query);			
			query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.MSG_DATE_CHANGE + " TEXT";
			db.execSQL(query);			
			query = "ALTER TABLE " + SealCertTable.TABLE_NAME + " ADD COLUMN " + SealCertTable.NUM + " INTEGER";
			db.execSQL(query);
			createSealCertSealsTable(db);
			query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.DATE_CHANGE + " TEXT";
			db.execSQL(query);			
		} else if (oldVersion == 22 && newVersion == 29) {
			String query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.ATTACHMENT + " BLOB";
			db.execSQL(query);			
			query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.MSG_DATE_CHANGE + " TEXT";
			db.execSQL(query);			
			createSealTable(db);
			createSealCertTable(db);
			createSealCertSealsTable(db);
			createReasonTable(db);
			query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.DATE_CHANGE + " TEXT";
			db.execSQL(query);			
		} else if (oldVersion == 21 && newVersion == 28) {//  SpecsTable
			String query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.ATTACHMENT + " BLOB";
			db.execSQL(query);			
			query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.MSG_DATE_CHANGE + " TEXT";
			db.execSQL(query);			
			query = "ALTER TABLE " + SpecsTable.TABLE_NAME + " ADD COLUMN " + SpecsTable.FACTORY_SEAL + " INTEGER";
			db.execSQL(query);
			createSealTable(db);
			createSealCertTable(db);
			createSealCertSealsTable(db);
			createReasonTable(db);
			query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.DATE_CHANGE + " TEXT";
			db.execSQL(query);			
		} else if (oldVersion == 20 && newVersion == 28) {//  SpecsTable
			String query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.ATTACHMENT + " BLOB";
			db.execSQL(query);			
			query = "ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.MSG_DATE_CHANGE + " TEXT";
			db.execSQL(query);			
			query = "ALTER TABLE " + SpecsTable.TABLE_NAME + " ADD COLUMN " + SpecsTable.IP_MODEM + " TEXT";
			db.execSQL(query);
			query = "ALTER TABLE " + SpecsTable.TABLE_NAME + " ADD COLUMN " + SpecsTable.IP_CASH + " TEXT";
			db.execSQL(query);
			query = "ALTER TABLE " + SpecsTable.TABLE_NAME + " ADD COLUMN " + SpecsTable.NUM_CASH + " INTEGER";
			db.execSQL(query);
			query = "ALTER TABLE " + SpecsTable.TABLE_NAME + " ADD COLUMN " + SpecsTable.FACTORY_SEAL + " INTEGER";
			db.execSQL(query);
			createSealTable(db);
			createSealCertTable(db);
			createSealCertSealsTable(db);
			createReasonTable(db);
			query = "ALTER TABLE " + WarrantTable.TABLE_NAME + " ADD COLUMN " + WarrantTable.DATE_CHANGE + " TEXT";
			db.execSQL(query);			
		} else {
			if (D) { Log.w(TAG, "Upgrading database FROM version "
					+ oldVersion + " to " + newVersion + ",	which will destroy all old data"); };
			// Implement how to "move" your application data
			// during an upgrade of schema versions.
			// Move or delete data as required. Your call.		
			// KILL PREVIOUS TABLES IF UPGRADED
			dropTable(db, GoodsTable.TABLE_NAME);
			dropTable(db, ConstTable.TABLE_NAME);
			dropTable(db, EntTable.TABLE_NAME);
			dropTable(db, StateTable.TABLE_NAME);
			dropTable(db, EmpTable.TABLE_NAME);
			dropTable(db, EmpHardwareTable.TABLE_NAME);
			dropTable(db, LocationTable.TABLE_NAME);
			dropTable(db, MessagesTable.TABLE_NAME);
			dropTable(db, EmpWorkTimeTable.TABLE_NAME);
			dropTable(db, UserTable.TABLE_NAME);
			dropTable(db, LogsTable.TABLE_NAME);
			dropTable(db, ContractTable.TABLE_NAME);
			dropTable(db, SpecsTable.TABLE_NAME);
			dropWarrantTables(db);
			// CREATE NEW INSTANCE OF SCHEMA
			onCreate(db);
		}
	}

	public void createCommonTables(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + GoodsTable.TABLE_NAME + 
				" (" + GoodsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ GoodsTable.ID_EXTERNAL + " INTEGER,"
				+ GoodsTable.ID_PARENT + " INTEGER,"
				+ GoodsTable.TTYPE + " INTEGER,"
				+ GoodsTable.GDSTYPE + " INTEGER,"
				+ GoodsTable.NM + " TEXT);");
            db.execSQL("CREATE INDEX gds_idx ON " + GoodsTable.TABLE_NAME + "(" + GoodsTable.ID + ");");
            db.execSQL("CREATE INDEX gds_ext_idx ON " + GoodsTable.TABLE_NAME + "(" + GoodsTable.ID_EXTERNAL + ");");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + ConstTable.TABLE_NAME +
				" (" + ConstTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ ConstTable.ID_EXTERNAL + " INTEGER,"
				+ ConstTable.TYPE_ID+ " INTEGER,"
				+ ConstTable.TTYPE + " INTEGER,"
				+ ConstTable.NMVAL + " TEXT,"
				+ ConstTable.NM + " TEXT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + EntTable.TABLE_NAME + 
				" (" + EntTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ EntTable.ID_EXTERNAL + " INTEGER,"
				+ EntTable.ID_PARENT + " INTEGER,"
				+ EntTable.LAT + " REAL,"
				+ EntTable.LNG + " REAL,"
				+ EntTable.ADDR	+ " TEXT,"
				+ EntTable.NM + " TEXT);");
            db.execSQL("CREATE INDEX ent_idx ON " + EntTable.TABLE_NAME + "(" + EntTable.ID + ");");
            db.execSQL("CREATE INDEX ent_ext_idx ON " + EntTable.TABLE_NAME + "(" + EntTable.ID_EXTERNAL + ");");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + StateTable.TABLE_NAME +
				" (" + StateTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ StateTable.ID_EXTERNAL + " INTEGER,"
				+ StateTable.ID_DOC + " INTEGER,"
//				+ StateTable.NO + " INTEGER,"
				+ StateTable.NM + " TEXT);");
			createEmpTable(db);
			createEmpWorkTimeTable(db);
			createLocationTable(db);
			createUserTable(db);
			createMsgsTable(db);
			createLogsTable(db);
			createTrafficStatTable(db);
			createContractsTable(db);
			createSpecsTable(db);
			createSealTable(db);
			createSealCertTable(db);
			createSealCertSealsTable(db);
			createReasonTable(db);
            createEmpHardwareTables(db);  // I'm not sure need this table or not?
		} catch (Exception e) {
			Log.e("createCommonTables", e.getMessage());
		}
	}
		
	public void createWarrantTables(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + WarrantTable.TABLE_NAME + 
				" (" + WarrantTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ WarrantTable.ID_EXTERNAL + " INTEGER,"
				+ WarrantTable.NUM + " INTEGER,"
				+ WarrantTable.ID_CONTRACT + " INTEGER,"
				+ WarrantTable.ID_ENT + " INTEGER,"
				+ WarrantTable.ID_SUBENT + " INTEGER,"
				+ WarrantTable.IDSTATE + " INTEGER,"
				+ WarrantTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ WarrantTable.LOCAL_STATE + " INTEGER DEFAULT 0,"
				+ WarrantTable.D_OPEN + " TEXT,"
				+ WarrantTable.DATE_CHANGE + " TEXT,"
				+ WarrantTable.ID_OWNER + " INTEGER,"
				+ WarrantTable.TO_SIGN + " INTEGER,"
				+ WarrantTable.D_SIGN + " TEXT,"
				+ WarrantTable.ID_USER_SIGN + " INTEGER,"
                    + WarrantTable.REGL + " INTEGER DEFAULT 0,"
				+ WarrantTable.REMARK + " TEXT);");
            db.execSQL("CREATE INDEX warrant_idx ON " + WarrantTable.TABLE_NAME + "(" + WarrantTable.ID + ");");
            db.execSQL("CREATE INDEX warrant_ext_idx ON " + WarrantTable.TABLE_NAME + "(" + WarrantTable.ID_EXTERNAL + ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + WarrContTable.TABLE_NAME +
				" (" + WarrContTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ WarrContTable.ID_EXTERNAL + " INTEGER,"
				+ WarrContTable.ID_WARRANT + " INTEGER,"
				+ WarrContTable.ID_EQ + " INTEGER,"
				+ WarrContTable.ID_JOB + " INTEGER,"
				+ WarrContTable.JOB_STATUS + " INTEGER,"
				+ WarrContTable.EQ_STATUS + " INTEGER,"
				+ WarrContTable.EQ_SRL + " TEXT,"
				+ WarrContTable.EQ_INV + " TEXT,"
				+ WarrContTable.EQ_SPEC + " INTEGER,"
				+ WarrContTable.REC_STAT + " INTEGER DEFAULT 0,"
				+ WarrContTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ WarrContTable.TBL_MODE + " INTEGER,"
				+ WarrContTable.REMARK + " TEXT);");
            db.execSQL("CREATE INDEX warr_cont_idx ON " + WarrContTable.TABLE_NAME + "(" + WarrContTable.ID_WARRANT + ");");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + WarrUnitTable.TABLE_NAME + 
				" (" + WarrUnitTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ WarrUnitTable.ID_EXTERNAL + " INTEGER,"
				+ WarrUnitTable.ID_WARR_CONT + " INTEGER,"
				+ WarrUnitTable.ID_JOB + " INTEGER,"
				+ WarrUnitTable.JOB_STATUS + " INTEGER,"
				+ WarrUnitTable.EQ_STATUS + " INTEGER,"
				+ WarrUnitTable.REC_STAT + " INTEGER DEFAULT 0,"
				+ WarrUnitTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ WarrUnitTable.TBL_MODE + " INTEGER,"
				+ WarrUnitTable.REMARK + " TEXT);");
            db.execSQL("CREATE INDEX warr_unit_idx ON " + WarrUnitTable.TABLE_NAME + "(" + WarrUnitTable.ID + ");");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + WarrRepairTable.TABLE_NAME +
				" (" + WarrRepairTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ WarrRepairTable.ID_EXTERNAL + " INTEGER,"
				+ WarrRepairTable.ID_WARR_UNIT + " INTEGER,"
				+ WarrRepairTable.ID_WARRANT + " INTEGER,"
				+ WarrRepairTable.ID_REPAIR + " INTEGER,"
				+ WarrRepairTable.REC_STAT + " INTEGER DEFAULT 0,"
				+ WarrRepairTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ WarrRepairTable.TBL_MODE + " INTEGER,"
				+ WarrRepairTable.QUANT + " REAL,"
				+ WarrRepairTable.COST + " REAL,"
				+ WarrRepairTable.REMARK + " TEXT);");
            db.execSQL("CREATE INDEX warr_unit_idx ON " + WarrRepairTable.TABLE_NAME + "(" + WarrRepairTable.ID + ");");

			createEmpHardwareTables(db);
		} catch (Exception e) {
			Log.e("createWarrantTables", e.getMessage());
		}
	}
	
	public void dropWarrantTables(SQLiteDatabase db) {
		dropTable(db, WarrantTable.TABLE_NAME);
		dropTable(db, WarrContTable.TABLE_NAME);
		dropTable(db, WarrUnitTable.TABLE_NAME);
		dropTable(db, WarrRepairTable.TABLE_NAME);
	}
	
	public void dropTable(SQLiteDatabase db, String tableName) {
		try {
			db.execSQL("DROP TABLE IF EXISTS " + tableName);
		} catch (Exception e) {
			Log.e("dropTable: "+ tableName, e.getMessage());
		}
	}
		
	public boolean emptyTable(String tableName) {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 0;
		try {
			result = sd.delete(tableName, "1", null);
		} catch (Exception e) {
			Log.e("LOG_TAG", e.getMessage());
		}
		return (result > 0);		
	}
	
	public boolean emptyAllConstTables() {
		SQLiteDatabase sd = getWritableDatabase();
//		String[] whereArgs = new String[] { };
		
		int result = sd.delete(GoodsTable.TABLE_NAME, "1", null);
		result = sd.delete(ConstTable.TABLE_NAME, "1", null);
		result = sd.delete(EntTable.TABLE_NAME, "1", null);
		result = sd.delete(StateTable.TABLE_NAME, "1", null);
		result = sd.delete(EmpTable.TABLE_NAME, "1", null);
		return (result > 0);		
	}
	
	public void createMsgsTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + MessagesTable.TABLE_NAME + 
				" (" + MessagesTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ MessagesTable.ID_EXTERNAL + " INTEGER,"
				+ MessagesTable.ID_SENDER + " INTEGER,"
				+ MessagesTable.ID_RECIPIENT + " INTEGER,"
				+ MessagesTable.STATUS + " INTEGER,"
				+ MessagesTable.ID_STATE + " INTEGER,"
				+ MessagesTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ MessagesTable.REC_STAT + " INTEGER,"
				+ MessagesTable.MSG_DATE + " TEXT,"
				+ MessagesTable.MSG_DATE_CHANGE + " TEXT,"
				+ MessagesTable.SUBJ + " TEXT,"
				+ MessagesTable.MSG + " TEXT,"
				+ MessagesTable.ATTACHMENT + " BLOB,"
				+ MessagesTable.ATTACH_NAME + " TEXT,"
				+ MessagesTable.ATTACH_SIZE + " INTEGER);");
            db.execSQL("CREATE INDEX msg_idx ON " + MessagesTable.TABLE_NAME + "(" + MessagesTable.ID + ");");
        } catch (Exception e) {
			Log.e("createMsgsTable", e.getMessage());
		}
	}
	
	public void createLocationTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + LocationTable.TABLE_NAME + 
				" (" + LocationTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ LocationTable.LAT + " REAL,"
				+ LocationTable.LNG + " REAL,"
				+ LocationTable.MODIFIED + " INTEGER DEFAULT 1,"  // признак необходимости передачи на сервер
				+ LocationTable.L_DATE + " TEXT);");
            db.execSQL("CREATE INDEX loc_idx ON " + LocationTable.TABLE_NAME + "(" + LocationTable.ID + ");");
		} catch (Exception e) {
			Log.e("createLocationTable", e.getMessage());
		}
	}

	public void createLocationTable() {
		createLocationTable(getWritableDatabase());
	}
	
	public void createContractsTable() {
		createContractsTable(getWritableDatabase());
	}

	public void createContractsTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + ContractTable.TABLE_NAME + 
				" (" + ContractTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ ContractTable.ID_EXTERNAL + " INTEGER,"
				+ ContractTable.ID_ENT + " INTEGER,"
				+ ContractTable.ID_ENT_U + " INTEGER,"
				+ ContractTable.ID_STATE + " INTEGER,"
				+ ContractTable.ID_OWNER + " INTEGER,"
				+ ContractTable.NO + " TEXT,"
				+ ContractTable.NAME + " TEXT,"
				+ ContractTable.BEGIN_DATE + " TEXT,"  
				+ ContractTable.END_DATE + " TEXT);");
            db.execSQL("CREATE INDEX contr_idx ON " + ContractTable.TABLE_NAME + "(" + ContractTable.ID + ");");
		} catch (Exception e) {
			Log.e("createContractsTable", e.getMessage());
		}
	}
	
	public void createSpecsTable() {
		createSpecsTable(getWritableDatabase());
	}

	public void createSpecsTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + SpecsTable.TABLE_NAME + 
				" (" + SpecsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ SpecsTable.ID_EXTERNAL + " INTEGER,"
				+ SpecsTable.ID_ENT + " INTEGER,"
				+ SpecsTable.ID_CONTRACT + " INTEGER,"
				+ SpecsTable.ID_GDS + " INTEGER,"
				+ SpecsTable.TEMP + " INTEGER,"
				+ SpecsTable.FISK_NUM + " TEXT,"
				+ SpecsTable.PLACE + " TEXT,"
				+ SpecsTable.SRL + " TEXT,"
				+ SpecsTable.INV + " TEXT,"
				+ SpecsTable.IP_MODEM + " TEXT,"
				+ SpecsTable.IP_CASH + " TEXT,"
				+ SpecsTable.NUM_CASH + " INTEGER,"
				+ SpecsTable.MODIFIED + " INTEGER DEFAULT 0,"  // признак необходимости передачи на сервер
				+ SpecsTable.FACTORY_SEAL + " INTEGER,"
				+ SpecsTable.CHANGE_DATE + " TEXT,"
				+ SpecsTable.BEGIN_DATE + " TEXT,"  
				+ SpecsTable.END_DATE + " TEXT);");
            db.execSQL("CREATE INDEX specs_idx ON " + SpecsTable.TABLE_NAME + "(" + SpecsTable.ID + ");");
		} catch (Exception e) {
			Log.e("createSpecsTable", e.getMessage());
		}
	}	

	public void createReasonTable() {
		createReasonTable(getWritableDatabase());
	}

	public void createReasonTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + ReasonTable.TABLE_NAME +
				" (" + ReasonTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ ReasonTable.ID_EXTERNAL + " INTEGER,"
				+ ReasonTable.SIGN + " INTEGER,"
				+ ReasonTable.NM + " TEXT,"
				+ ReasonTable.CHANGE_DATE + " TEXT);");
		} catch (Exception e) {
			Log.e("createReasonTable", e.getMessage());
		}
	}	

	public void createSealTable() {
		createSealTable(getWritableDatabase());
	}

	public void createSealTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + SealTable.TABLE_NAME + 
				" (" + SealTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ SealTable.ID_EXTERNAL + " INTEGER,"
				+ SealTable.SRL + " TEXT,"
				+ SealTable.NUM + " TEXT,"
				+ SealTable.INUM + " TEXT,"
				+ SealTable.MODIFIED + " INTEGER DEFAULT 0,"  
				+ SealTable.CHANGE_DATE + " TEXT,"
				+ SealTable.I_CHANGE_DATE + " TEXT);");
            db.execSQL("CREATE INDEX seal_idx ON " + SealTable.TABLE_NAME + "(" + SealTable.ID + ");");
		} catch (Exception e) {
			Log.e("createSealTable", e.getMessage());
		}
	}	
	
	public void createSealCertTable() {
		createSealCertTable(getWritableDatabase());
	}

	public void createSealCertTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + SealCertTable.TABLE_NAME + 
				" (" + SealCertTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ SealCertTable.ID_EXTERNAL + " INTEGER,"
				+ SealCertTable.NUM + " INTEGER,"
				+ SealCertTable.ID_EMP + " INTEGER,"
				+ SealCertTable.ID_OWNER + " INTEGER,"
				+ SealCertTable.ID_WARR_CONT + " INTEGER,"
				+ SealCertTable.ID_SPECS + " INTEGER,"
				+ SealCertTable.ID_REASON + " INTEGER,"
				+ SealCertTable.FACTORY_SEAL + " INTEGER,"
				+ SealCertTable.DEFECT + " TEXT,"
				+ SealCertTable.BEGIN_DATE + " TEXT,"
				+ SealCertTable.END_DATE + " TEXT,"
				+ SealCertTable.MODIFIED + " INTEGER DEFAULT 0,"  
				+ SealCertTable.CHANGE_DATE + " TEXT,"
				+ SealCertTable.PPO_MONEY + " REAL);");
            db.execSQL("CREATE INDEX seal_cert_idx ON " + SealCertTable.TABLE_NAME + "(" + SealCertTable.ID + ");");
		} catch (Exception e) {
			Log.e("createSealCertTable", e.getMessage());
		}
	}	
	
	public void createSealCertSealsTable() {
		createSealCertTable(getWritableDatabase());
	}

	public void createSealCertSealsTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + SealCertSealsTable.TABLE_NAME + 
				" (" + SealCertSealsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ SealCertSealsTable.ID_SEAL_CERT + " INTEGER,"
				+ SealCertSealsTable.ID_SEAL_EXTERNAL + " INTEGER,"
				+ SealCertSealsTable.SEAL_INFO + " TEXT,"
				+ SealCertSealsTable.MODIFIED + " INTEGER DEFAULT 0);");
            db.execSQL("CREATE INDEX seal_cert_seal_idx ON " + SealCertSealsTable.TABLE_NAME + "(" + SealCertSealsTable.ID + ");");
		} catch (Exception e) {
			Log.e(TAG, "createSealCertSealsTable: "+ e.getMessage());
		}
	}	
		
	public void createEmpWorkTimeTable() {
		createEmpWorkTimeTable(getWritableDatabase());
	}
	
	public void createEmpWorkTimeTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + EmpWorkTimeTable.TABLE_NAME + 
				" (" + EmpWorkTimeTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ EmpWorkTimeTable.ID_EXTERNAL + " INTEGER DEFAULT 0,"
				+ EmpWorkTimeTable.ID_ENT + " INTEGER,"
				+ EmpWorkTimeTable.ID_WARRANT + " INTEGER,"
				+ EmpWorkTimeTable.BE + " INTEGER,"
				+ EmpWorkTimeTable.LAT + " REAL,"
				+ EmpWorkTimeTable.LNG + " REAL,"
				+ EmpWorkTimeTable.MODIFIED + " INTEGER DEFAULT 1,"
				+ EmpWorkTimeTable.DTBE + " TEXT);");
            db.execSQL("CREATE INDEX emp_wt_idx ON " + EmpWorkTimeTable.TABLE_NAME + "(" + EmpWorkTimeTable.ID + ");");
		} catch (Exception e) {
			Log.e("createEmpWorkTimeTable", e.getMessage());
		}
	}
	
	public void createUserTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + UserTable.TABLE_NAME + 
				" (" + UserTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ UserTable.ID_EXTERNAL + " INTEGER,"
				+ UserTable.SURNAME + " TEXT,"
				+ UserTable.NAME + " TEXT,"
				+ UserTable.PATRONIMIC + " TEXT);");
		} catch (Exception e) {
			Log.e("createUserTable", e.getMessage());
		}
	}
	
	public void createEmpTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + EmpTable.TABLE_NAME + 
				" (" + EmpTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ EmpTable.ID_EXTERNAL + " INTEGER,"
				+ EmpTable.ID_ENT + " INTEGER,"
				+ EmpTable.SURNAME + " TEXT,"
				+ EmpTable.NAME + " TEXT,"
				+ EmpTable.PATRONIMIC + " TEXT,"
				+ EmpTable.PHOTO + " BLOB);");
            db.execSQL("CREATE INDEX emp_idx ON " + EmpTable.TABLE_NAME + "(" + EmpTable.ID + ");");
		} catch (Exception e) {
			Log.e("createEmpTable", e.getMessage());
		}
	}
	
	public void createEmpHardwareTables(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + EmpHardwareTable.TABLE_NAME + 
				" (" + EmpHardwareTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ EmpHardwareTable.ID_GDS + " INTEGER,"
				+ EmpHardwareTable.COUNT + " REAL);");
		} catch (Exception e) {
			Log.e("createEmpHardwareTables", e.getMessage());
		}
	}
	
	public void createLogsTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + LogsTable.TABLE_NAME + 
				" (" + LogsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ LogsTable.LEVEL + " INTEGER,"
				+ LogsTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ LogsTable.MSG_DATE + " TEXT,"
				+ LogsTable.MSG + " TEXT);");
            db.execSQL("CREATE INDEX log_idx ON "+ LogsTable.TABLE_NAME +"("+ LogsTable.ID +");");
//            db.execSQL("CREATE INDEX log_idx_ext ON "+ LogsTable.TABLE_NAME +"("+ LogsTable.ID_EXTERNAL +");");
		} catch (Exception e) {
			Log.e("createLogsTable", e.getMessage());
		}
	}	
	
	public void createTrafficStatTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TrafficStatTable.TABLE_NAME + 
				" (" + TrafficStatTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ TrafficStatTable.RX_BYTES + " INTEGER,"
				+ TrafficStatTable.TX_BYTES + " INTEGER,"
				+ TrafficStatTable.RX_JSON_BYTES + " INTEGER,"
				+ TrafficStatTable.TX_JSON_BYTES + " INTEGER,"
				+ TrafficStatTable.MODIFIED + " INTEGER DEFAULT 1,"
				+ TrafficStatTable.STAT_DATE + " TEXT);");
            db.execSQL("CREATE INDEX traff_stat_idx ON "+ TrafficStatTable.TABLE_NAME +"("+ TrafficStatTable.ID +");");
		} catch (Exception e) {
			Log.e("createTrafficStatTable", e.getMessage());
		}
	}
	
	
	public boolean emptyEmpTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		
//		dropTable(sd, EmpTable.TABLE_NAME);
//		createEmpTable(sd);
		
		result = sd.delete(EmpTable.TABLE_NAME, "1", null);
		
		if (D) { Log.d(TAG, "EmpTable emptied"); };
		return (result > 0);			
	}

	public boolean emptyEmpHarwareTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(EmpHardwareTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "EmpHarwareTable emptied"); };
		return (result > 0);			
	}

	public boolean emptyContractTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(ContractTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "ContractTable emptied"); };
		return (result > 0);			
	}

	public boolean EmptySpecsTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(SpecsTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "SpecsTable emptied"); };
		return (result > 0);			
	}

	public boolean EmptyUserTable() {
		SQLiteDatabase sd = getWritableDatabase();
		long result = 1;
		
//		dropTable(sd, LogsTable.TABLE_NAME);
		result = getLogsCount();
		if (result < 0)
			createLogsTable(sd);
		result = sd.delete(UserTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "UserTable emptied"); };
		return (result > 0);			
	}

	public boolean emptyMsgsTable() {
		SQLiteDatabase sd = getWritableDatabase();
		long result = 1;				
		result = getUserCount();
		if (result <= 0) createUserTable(sd);
		result = sd.delete(MessagesTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "MsgsTable emptied"); };
		return (result > 0);			
	}		

	public boolean EmptyLogsTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(LogsTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "LogsTable emptied"); };
		return (result > 0);			
	}		

	public boolean EmptyLocationTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(LocationTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "LocationTable emptied"); };
		return (result > 0);			
	}		

	public boolean EmptyEmpWorkTimeTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(EmpWorkTimeTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "EmpWorkTimeTable emptied"); };
		return (result > 0);			
	}		

	public boolean emptyStateTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;		
		result = sd.delete(StateTable.TABLE_NAME, "1", null);
		if (D) { Log.d(TAG, "StateTable emptied"); };
		return (result > 0);			
	}
	
	public boolean emptyReasonTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;	
		result = sd.delete(ReasonTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "ReasonTable emptied"); };
		return (result > 0);			
	}
	
	public boolean emptySealTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;	
		result = sd.delete(SealTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "SealTable emptied"); };
		return (result > 0);			
	}
	
	public boolean EmptyTrafficStatTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(TrafficStatTable.TABLE_NAME, "1", null);		
		if (D) { Log.d(TAG, "TrafficStatTable emptied"); };
		return (result > 0);			
	}

	
	public boolean emptyAllWarrantTables() {
		SQLiteDatabase sd = getWritableDatabase();
//		String[] whereArgs = new String[] { };
		int result = 1;
		
		result = sd.delete(WarrantTable.TABLE_NAME, "1", null);
		result = sd.delete(WarrContTable.TABLE_NAME, "1", null);
		result = sd.delete(WarrUnitTable.TABLE_NAME, "1", null);
		result = sd.delete(WarrRepairTable.TABLE_NAME, "1", null);
		if (D) { Log.d(TAG, "AllWarrantTables emptied"); };
		return (result > 0);		
	}
	
	public long getTableCount(String query) {
		long result = 0;
		Cursor c = null;
		SQLiteDatabase sd = getWritableDatabase();
    	try {
    		c = sd.rawQuery(query, null);
    		if (c != null && c.moveToFirst()) {
	    		result = c.getLong(0);
    		}
    	} catch (Exception e) {
    		result = -1;
    		if (D) Log.e(TAG, "getTableCount exception = " + e.getMessage());
		} finally {
			if (c != null)
				c.close();
		}
		return result;
	}
	
	public String getTableStringField(String query) {
		String result = "";
		Cursor c = null;
		SQLiteDatabase sd = getWritableDatabase();
    	try {
    		c = sd.rawQuery(query, null);
    		if (c != null && c.moveToFirst()) {
	    		result = c.getString(0);
    		}
    	} catch (Exception e) {
    		result = "";
    		if (D) Log.e(TAG, "getTableStringField exception = " + e.getMessage());
		} finally {
			if (c != null)
				c.close();
		}
		return result;
	}
	
	/* ****************************************** */
	public void insEntBulkJSON(JSONArray records) {
		SQLiteDatabase sd = getWritableDatabase();
		InsertHelper ih = new InsertHelper(sd, EntTable.TABLE_NAME);
		final int idExtColumn = ih.getColumnIndex(EntTable.ID_EXTERNAL);
		final int idParentColumn = ih.getColumnIndex(EntTable.ID_PARENT);
		final int idLatColumn = ih.getColumnIndex(EntTable.LAT);
		final int idLngColumn = ih.getColumnIndex(EntTable.LNG);
		final int idAddrColumn = ih.getColumnIndex(EntTable.ADDR);
		final int idNmColumn = ih.getColumnIndex(EntTable.NM);
		
		int id;
		int id_parent;
		Double lat, lng;
		String sLat, sLng, nm, addr;
		long startTime = System.currentTimeMillis();
		try {
			sd.execSQL("PRAGMA synchronous=OFF");
		    sd.setLockingEnabled(false);
		    sd.beginTransaction();			
			for (int i = 0; i < records.length(); i++) {			
				JSONObject warrant;
				try {
					warrant = records.getJSONObject(i);
					id = warrant.getInt("id");
					id_parent = warrant.getInt("id_p");
					sLat = warrant.getString("lat");
					lat = (double) Float.parseFloat(sLat.replace(",", "."));
					sLng = warrant.getString("lng");
					lng = (double) Float.parseFloat(sLng.replace(",", "."));
					nm = warrant.getString("nm");
					addr = warrant.getString("addr");   					
					// Get the InsertHelper ready to insert a single row
		            ih.prepareForInsert();
		            // Add the data for each column
		            ih.bind(idExtColumn, id);
		            ih.bind(idParentColumn, id_parent);
		            ih.bind(idLatColumn, lat);
		            ih.bind(idLngColumn, lng);
		            ih.bind(idAddrColumn, addr);
		            ih.bind(idNmColumn, nm);
		            // Insert the row into the database.
	                ih.execute();			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			sd.setTransactionSuccessful();
		} finally {
			sd.endTransaction();
		    sd.setLockingEnabled(true);
		    sd.execSQL("PRAGMA synchronous=NORMAL");
		    ih.close();			
		    final long endtime = System.currentTimeMillis();
	        Log.i(TAG, "insEntBulkJSON: Time to insert Members: "+ String.valueOf(endtime - startTime));
		}					
	}
	
	/* ****************************************** */
	public void insGoodsBulkJSON(JSONArray records) {
		SQLiteDatabase sd = getWritableDatabase();		
		InsertHelper ih = new InsertHelper(sd, GoodsTable.TABLE_NAME);		
		final int idExtColumn = ih.getColumnIndex(GoodsTable.ID_EXTERNAL);
		final int idParentColumn = ih.getColumnIndex(GoodsTable.ID_PARENT);
		final int idTtypeColumn = ih.getColumnIndex(GoodsTable.TTYPE);
		final int idGdsTypeColumn = ih.getColumnIndex(GoodsTable.GDSTYPE);
		final int idNmColumn = ih.getColumnIndex(GoodsTable.NM);
		
		int id, id_parent, ttype, gdstype;
		String nm;
		long startTime = System.currentTimeMillis();
		try {
			sd.execSQL("PRAGMA synchronous=OFF");
		    sd.setLockingEnabled(false);
		    sd.beginTransaction();			
			for (int i = 0; i < records.length(); i++) {			
				JSONObject warrant;
				try {
					warrant = records.getJSONObject(i);
					id = warrant.getInt("id");
					id_parent = warrant.getInt("id_p");					
					ttype = warrant.getInt("ttype");
					gdstype = warrant.getInt("gdstype");
					nm = warrant.getString("nm");
					// Get the InsertHelper ready to insert a single row
		            ih.prepareForInsert();
		            // Add the data for each column
		            ih.bind(idExtColumn, id);
		            ih.bind(idParentColumn, id_parent);
		            ih.bind(idTtypeColumn, ttype);
		            ih.bind(idGdsTypeColumn, gdstype);
		            ih.bind(idNmColumn, nm);
		            // Insert the row into the database.
	                ih.execute();			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			sd.setTransactionSuccessful();
		} finally {
			sd.endTransaction();
		    sd.setLockingEnabled(true);
		    sd.execSQL("PRAGMA synchronous=NORMAL");
		    ih.close();	
		    final long endtime = System.currentTimeMillis();
	        Log.i(TAG, "insGoodsBulkJSON: Time to insert Members: "+ String.valueOf(endtime - startTime));
		}				
	}

	/* ****************************************** */
	public void insEmpsBulkJSON(JSONArray records) {
		SQLiteDatabase sd = getWritableDatabase();		
		InsertHelper ih = new InsertHelper(sd, EmpTable.TABLE_NAME);		
		final int idExtColumn = ih.getColumnIndex(EmpTable.ID_EXTERNAL);
		final int idEntColumn = ih.getColumnIndex(EmpTable.ID_ENT);
		final int idSurnameColumn = ih.getColumnIndex(EmpTable.SURNAME);
		final int idNameColumn = ih.getColumnIndex(EmpTable.NAME);
		final int idPatronimicColumn = ih.getColumnIndex(EmpTable.PATRONIMIC);
		final int idPhotoColumn = ih.getColumnIndex(EmpTable.PHOTO);
		
		int id;
		int id_ent;
		String surname, name, patronimic, sPhoto;
		byte[] photo;
		long startTime = System.currentTimeMillis();
		try {
			sd.execSQL("PRAGMA synchronous=OFF");
		    sd.setLockingEnabled(false);
		    sd.beginTransaction();			
			for (int i = 0; i < records.length(); i++) {			
				JSONObject emp;
				try {
					emp = records.getJSONObject(i);
					id = emp.getInt("id");
					id_ent = emp.getInt("id_ent");					
					surname = emp.getString("fam");
					name = emp.getString("nm");
					patronimic = emp.getString("otch");
					sPhoto = emp.getString("photo");
					photo = Base64.decode(sPhoto, Base64.DEFAULT);
					
					// Get the InsertHelper ready to insert a single row
		            ih.prepareForInsert();
		            // Add the data for each column
		            ih.bind(idExtColumn, id);
		            ih.bind(idEntColumn, id_ent);
		            ih.bind(idSurnameColumn, surname);
		            ih.bind(idNameColumn, name);
		            ih.bind(idPatronimicColumn, patronimic);
		            ih.bind(idPhotoColumn, photo);
		            // Insert the row into the database.
	                ih.execute();			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			sd.setTransactionSuccessful();
		} finally {
			sd.endTransaction();
		    sd.setLockingEnabled(true);
		    sd.execSQL("PRAGMA synchronous=NORMAL");
		    ih.close();	
		    final long endtime = System.currentTimeMillis();
	        Log.i(TAG, "insEmpsBulkJSON: Time to insert Members: "+ String.valueOf(endtime - startTime));
		}				
	}
	
	/* ************ COMMON ****************************** */
	private Boolean isRecordPresent(String query) {
		Boolean result = true;
		Cursor c = null;
		if (query.length() > 0) {
			try {
				SQLiteDatabase sd = getWritableDatabase();
		    	c = sd.rawQuery(query, null);
				result = c.moveToNext();
	    	} catch (Exception ex) {
	        	ex.printStackTrace();
	        } finally {
				if (c != null)
					c.close();
	        }					
		}
		return result;
	}
		
	/* ****************************************** */
	private Boolean isStateIdPresent(int idExt) {
		Boolean result = true;
		if (idExt > 0) {
			String query = "SELECT "+ StateTable.ID +" FROM " + 
	    			StateTable.TABLE_NAME + " WHERE " + StateTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long getStateCount() {
		String query = "SELECT COUNT("+ StateTable.ID +") FROM " + StateTable.TABLE_NAME;
		return getTableCount(query);
	}	
				
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addStateItem(int idExt, int idDoc, /*int no,*/ String itemName) {
		long result = 0;
		if (!isStateIdPresent(idExt)) {
			ContentValues cv = new ContentValues();
			cv.put(StateTable.ID_EXTERNAL, idExt);
			cv.put(StateTable.ID_DOC, idDoc);
//			cv.put(StateTable.NO, no);
			cv.put(StateTable.NM, itemName);
			SQLiteDatabase sd = getWritableDatabase();			
			result = sd.insert(StateTable.TABLE_NAME, StateTable.NM, cv);
		}
		if (result < 0) Log.d(TAG, "addStateItem - Error");
		return result;
	}
	
	public String getStateName(int idExt) {
		String result = "";
		if (idExt > 0) {
			String query = "SELECT "+ StateTable.NM +" FROM " + 
	    			StateTable.TABLE_NAME + " WHERE " + StateTable.ID_EXTERNAL + " = " + idExt;
			SQLiteDatabase sd = getReadableDatabase();
			Cursor c = null;
			try {
		    	c = sd.rawQuery(query, null);
		    	if (c.moveToNext()) {
		    		int colid = c.getColumnIndex(StateTable.NM);
		    		result = c.getString(colid); 
		    	}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getStateName exception: " + e.getMessage());
            } finally {
                if (c != null)
                    c.close();
			}
		}		
		return result;
	}
	
	
	/* ****************************************** */
	private Boolean isReasonIdPresent(int idExt) {
		Boolean result = true;
		if (idExt > 0) {
			String query = "SELECT "+ ReasonTable.ID +" FROM " + 
					ReasonTable.TABLE_NAME + " WHERE " + ReasonTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long getReasonCount() {
		String query = "SELECT COUNT("+ ReasonTable.ID +") FROM " + ReasonTable.TABLE_NAME;
		return getTableCount(query);
	}	
			
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addReasonItem(int idExt, int sign, String itemName, String dateChange) {
		long result = 0;
		if (!isReasonIdPresent(idExt)) {
			ContentValues cv = new ContentValues();
			cv.put(ReasonTable.ID_EXTERNAL, idExt);
			cv.put(ReasonTable.SIGN, sign);
			cv.put(ReasonTable.CHANGE_DATE, dateChange);
			cv.put(ReasonTable.NM, itemName);
			SQLiteDatabase sd = getWritableDatabase();			
			result = sd.insert(ReasonTable.TABLE_NAME, ReasonTable.NM, cv);
		}
		if (result < 0) Log.d(TAG, "addReasonItem - Error");
		return result;
	}
	
	
	
	/* ****************************************** */
	public long addEmpWorkTime(int id_ent, long id_warrant, Date dtbe, int be, double lat, double lng) {
		long result = 0;
		java.sql.Date sqlDate = new java.sql.Date(dtbe.getTime());
		ContentValues cv = new ContentValues();
		cv.put(EmpWorkTimeTable.ID_ENT, id_ent);
		cv.put(EmpWorkTimeTable.ID_WARRANT, id_warrant);
		cv.put(EmpWorkTimeTable.DTBE, dateFormatYY.format(sqlDate));
		cv.put(EmpWorkTimeTable.BE, be);
		cv.put(EmpWorkTimeTable.LAT, lat);
		cv.put(EmpWorkTimeTable.LNG, lng);
		SQLiteDatabase sd = getWritableDatabase();		
		result = sd.insert(EmpWorkTimeTable.TABLE_NAME, EmpWorkTimeTable.ID_WARRANT, cv);
		return result;
	}
	
	public int convertEmpWorkTimeDateFormat() {
		String query = "SELECT "+ EmpWorkTimeTable.DTBE + ", " + EmpWorkTimeTable.ID + 
				" FROM " + EmpWorkTimeTable.TABLE_NAME;
    	SQLiteDatabase sqdb = getReadableDatabase();
    	Cursor c = null;
    	String dateStr;
    	int cntr = 0, id; 		
		try {
			c = sqdb.rawQuery(query, null);
	    	while (c.moveToNext()) {	
	    		int colid = c.getColumnIndex(EmpWorkTimeTable.ID);
	    		id = c.getInt(colid);
	    		colid = c.getColumnIndex(EmpWorkTimeTable.DTBE);
	    		dateStr = c.getString(colid);
	         	java.util.Date date;
	         	try {
	         		date = dateFormat.parse(dateStr); 
	         		updateEmpWorkTimeDate(id, date);
	         		cntr++;
	         	} catch (ParseException e) {
	     			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	         	}		    		    				   
	    	}
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
	    	if (c != null) c.close();
		}
		return cntr;
	}
	
	public boolean updateEmpWorkTimeDate(long id, Date m_date) {
		java.sql.Date sqlDate = new java.sql.Date(m_date.getTime());
		Boolean result = false;
		try {
			ContentValues cv = new ContentValues();
			cv.put(EmpWorkTimeTable.DTBE, dateFormatYY.format(sqlDate));
			SQLiteDatabase sd = getWritableDatabase();	
			result = sd.update(EmpWorkTimeTable.TABLE_NAME, cv, EmpWorkTimeTable.ID + "=" + id, null) == 1;
		} catch (Exception e) {
			if (D) Log.e(TAG, "updateEmpWorkTimeDate exception = " + e.getMessage());
		}
		return result;		
	}
		
	public int getEmpWorkLastMark() {
		int mark = -1;
		Cursor c = null;
		String query = "SELECT " + EmpWorkTimeTable.BE + " FROM " + EmpWorkTimeTable.TABLE_NAME +
				" WHERE " + EmpWorkTimeTable.ID_WARRANT + " in (SELECT " + WarrantTable.ID_EXTERNAL +
				" FROM " + WarrantTable.TABLE_NAME + ")" +
				" ORDER BY " + EmpWorkTimeTable.DTBE + " DESC";
		SQLiteDatabase sd = getReadableDatabase();
		try {
	    	c = sd.rawQuery(query, null);
	    	if (c.moveToNext()) {
	    		int colid = c.getColumnIndex(EmpWorkTimeTable.BE);
	    		mark = c.getInt(colid); 
	    	}
		} catch(Exception e) {
			if (D) Log.e(TAG, "getEmpWorkLastMark exception: " + e.getMessage());
        } finally {
            if (c != null)
                c.close();
		}
		return mark;
	}
	
	public int getEmpWorkLastMark(long id_warrant) {
		return getEmpWorkLastMarkIntFld(id_warrant, EmpWorkTimeTable.BE);
	}
	
	public int getEmpWorkLastMarkModified(long id_warrant) {
		return getEmpWorkLastMarkIntFld(id_warrant, EmpWorkTimeTable.MODIFIED);
	}
	
	private int getEmpWorkLastMarkIntFld(long id_warrant, String fldName) {
		int mark = -1;
		Cursor c = null;
		String query = "SELECT " + fldName + " FROM " + EmpWorkTimeTable.TABLE_NAME +
				" WHERE " + EmpWorkTimeTable.ID_WARRANT + "=" + id_warrant + 
				" ORDER BY " + EmpWorkTimeTable.ID + " DESC";//EmpWorkTimeTable.DTBE + " DESC";
		SQLiteDatabase sd = getReadableDatabase();
		try {
	    	c = sd.rawQuery(query, null);
	    	if (c.moveToNext()) {
	    		int colid = c.getColumnIndex(fldName);
	    		mark = c.getInt(colid); 
	    	}
		} catch(Exception e) {
			if (D) Log.e(TAG, "getEmpWorkLastMarkIntFld exception: " + e.getMessage());
        } finally {
            if (c != null)
                c.close();
		}
		return mark;
	}
	
	public String getEmpWorkLastMarkInterval(long id_warrant) {
		int mark = -1;
		String m_date = "";
//		String b_m = "Начало работ: ";
//		String e_m = "Окончание работ: ";
//		String b_m = "Начало: ";
//		String e_m = "Окончание: ";
//		String b_m = "Нач.: ";
//		String e_m = "Оконч.: ";
//		String b_m = "Н.: ";
//		String e_m = "З.: ";
		String b_m = "Работа с ";
		String e_m = "по ";
		String n_a = "н/д";
		String res = "";
		int cntr = 0;
		boolean b_found = false, e_found = false;
		Cursor c = null;
		String query = "SELECT " + EmpWorkTimeTable.BE + ", "+ EmpWorkTimeTable.DTBE + 
				" FROM " + EmpWorkTimeTable.TABLE_NAME +
				" WHERE " + EmpWorkTimeTable.ID_WARRANT + "=" + id_warrant + 
				" ORDER BY " + EmpWorkTimeTable.ID /*DTBE*/ + " DESC"; 
		SQLiteDatabase sd = getReadableDatabase();
		try {
	    	c = sd.rawQuery(query, null);
	    	while (c.moveToNext()) {
	    		int colid = c.getColumnIndex(EmpWorkTimeTable.BE);
	    		mark = c.getInt(colid); 
	    		colid = c.getColumnIndex(EmpWorkTimeTable.DTBE);
	    		m_date = c.getString(colid);
	         	java.util.Date date;
	         	try {
	         		date = dateFormatYY.parse(m_date); 
	         		m_date = date2YFormat.format(date);
	         	} catch (ParseException e) {
	     			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	         	}		    		    				   	    		
	    		if (mark == 0) {
	    			b_m += m_date;
	    			b_found = true;
	    		}
	    		if (mark == 1) {
	    			e_m += m_date;
	    			e_found = true;
	    		}
	    		cntr++;
	    		if ((cntr >= 2)||(cntr == 1 && mark == 0))
	    			break;
	    	}
	    	if (!b_found)
	    		b_m += n_a;
	    	if (!e_found)
	    		e_m += n_a;
//	    	res = b_m + "; " + e_m;
//	    	res = b_m + " - " + e_m;
	    	res = b_m + " " + e_m;
		} catch(Exception e) {
			if (D) Log.e(TAG, "getEmpWorkLastMarkInterval exception: " + e.getMessage());
        } finally {
            if (c != null)
                c.close();
		}
		return res;
	}
	
	public int delNoWarrantEmpWorkMarks() {
		SQLiteDatabase sd = getWritableDatabase();
		String sWhere;
		int res = 0;
		sWhere = "("+ EmpWorkTimeTable.ID_WARRANT + " NOT IN (SELECT "+ WarrantTable.ID_EXTERNAL + " FROM "+
				WarrantTable.TABLE_NAME + ")) AND ("+ EmpWorkTimeTable.MODIFIED + ZERO_CONDITION + ")";
		try {
			res = sd.delete(EmpWorkTimeTable.TABLE_NAME, sWhere, null);
		} catch (Exception e) {
			if (D) Log.e(TAG, "delNoWarrantEmpWorkMarks exception: " + e.getMessage());
		}	
		return res;
	}
	
	public boolean setEmpWorkMarksExtId(long localId, long extId) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(EmpWorkTimeTable.ID_EXTERNAL, extId);
		cv.put(EmpWorkTimeTable.MODIFIED, 0);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(EmpWorkTimeTable.TABLE_NAME, cv, EmpWorkTimeTable.ID + "=" + localId, null) > 0;
		return result;		
	}
	
	public boolean setEmpWorkMarksModified(int newModified, int oldModified) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(EmpWorkTimeTable.MODIFIED, newModified);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(EmpWorkTimeTable.TABLE_NAME, cv, EmpWorkTimeTable.MODIFIED + "=" + oldModified, null) > 0;
		return result;
	}	
	
	public boolean setEmpWarrantWorkMarksModified(long idWarrant, int fModified) {
		boolean result = false;
		String where = EmpWorkTimeTable.ID_WARRANT + "=" + idWarrant + " AND " +
				EmpWorkTimeTable.ID_EXTERNAL + ZERO_CONDITION;
		ContentValues cv = new ContentValues();
		cv.put(EmpWorkTimeTable.MODIFIED, fModified);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(EmpWorkTimeTable.TABLE_NAME, cv, where, null) > 0;
		return result;
	}	
	
	private void checkEmpWorkClosedIntervals() {
		Cursor c = null;
		int idWarrant;
		int idEnt;
		String query = "SELECT DISTINCT " + EmpWorkTimeTable.ID_WARRANT + ", " + EmpWorkTimeTable.ID_ENT +  
				" FROM " + EmpWorkTimeTable.TABLE_NAME;
		SQLiteDatabase sd = getReadableDatabase();
		try {
	    	c = sd.rawQuery(query, null);
	    	while (c.moveToNext()) {
	    		int colid = c.getColumnIndex(EmpWorkTimeTable.ID_WARRANT);
	    		idWarrant = c.getInt(colid);
	    		colid = c.getColumnIndex(EmpWorkTimeTable.ID_ENT);
	    		idEnt = c.getInt(colid);
	    		int mark = getEmpWorkLastMark(idWarrant);
	    		if (mark == EmpWorkTimeRecord.BEGIN_WORK) { //  нужно закрыть интервал
	    			addEmpWorkTime(idEnt, idWarrant, new Date(), EmpWorkTimeRecord.END_WORK, 0, 0);
	    		}
	    	}
		} catch(Exception e) {
			if (D) Log.e(TAG, "getEmpWorkLastMark exception = " + e.getMessage());
        } finally {
            if (c != null)
                c.close();
		}
	}
	
	/* ****************************************** */
	private Boolean isUserIdPresent(long idExt) {
		Boolean result = true;
		if (idExt > 0) {
			String query = "SELECT "+ UserTable.ID +" FROM " + 
	    			UserTable.TABLE_NAME + " WHERE " + UserTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addUserItem(long idExt, String surname, String name, String patronimic) {
		long result = 0;
		if (!isUserIdPresent(idExt)) {
			ContentValues cv = new ContentValues();
			cv.put(UserTable.ID_EXTERNAL, idExt);
			cv.put(UserTable.SURNAME, surname);
			cv.put(UserTable.NAME, name);
			cv.put(UserTable.PATRONIMIC, patronimic);
			SQLiteDatabase sd = getWritableDatabase();	
			result = sd.insert(UserTable.TABLE_NAME, UserTable.SURNAME, cv);
		}
		if (result < 0) Log.d(TAG, "addUserItem - Error");
		return result;
	}

	public long addUserItem(long idExt) {
		long result = 0;
		if (!isUserIdPresent(idExt)) {
			ContentValues cv = new ContentValues();
			cv.put(UserTable.ID_EXTERNAL, idExt);
			SQLiteDatabase sd = getWritableDatabase();			
			result = sd.insert(UserTable.TABLE_NAME, UserTable.ID_EXTERNAL, cv);
		}
		if (result < 0) Log.d(TAG, "addUserItem - Error");
		return result;
	}
	
	public long getUserCount() {
		String query = "SELECT COUNT("+ UserTable.ID +") FROM " + UserTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public long getUserId() {
		String query = "SELECT "+ UserTable.ID_EXTERNAL +" FROM " + UserTable.TABLE_NAME;		
		return getTableCount(query);		
	}
	
	/* ****************************************** */
	public long addLocationItem(double lat, double lng, Date l_date) {
		java.sql.Date sqlDate = new java.sql.Date(l_date.getTime());
		long result = 0;
		ContentValues cv = new ContentValues();
		cv.put(LocationTable.LAT, lat);
		cv.put(LocationTable.LNG, lng);
		cv.put(LocationTable.MODIFIED, 1);
		cv.put(LocationTable.L_DATE, dateFormat.format(sqlDate));			

		SQLiteDatabase sd = getWritableDatabase();		
		result = sd.insert(LocationTable.TABLE_NAME, LocationTable.L_DATE, cv);
		if (result < 0) Log.d(TAG, "addLocationItem - Error");
		return result;
	}
	
	public long getLocationCount() {
		String query = "SELECT COUNT("+ LocationTable.ID +") FROM " + LocationTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public boolean setLocationModified(int newModified) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(LocationTable.MODIFIED, newModified);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(LocationTable.TABLE_NAME, cv, null, null) > 0;
		return result;
	}	
	
	public Cursor getLocationDates() {
		Cursor c;
		SQLiteDatabase sd = getWritableDatabase();
    	c = sd.rawQuery("SELECT DISTINCT "+ LocationTable.ID +", SUBSTR("+ LocationTable.L_DATE + ",1,10)AS "+LocationTable.L_DATE+
    			" FROM " + LocationTable.TABLE_NAME, null);
		return c;		
	}

	public String[] getDistinctLocationDates() {
    	Cursor c = null;
		String[] dates = null;
		String locationDate;
		String query = "SELECT DISTINCT SUBSTR("+ LocationTable.L_DATE + ",1,10)AS "+ LocationTable.L_DATE +
    			" FROM " + LocationTable.TABLE_NAME;
		SQLiteDatabase sd = getWritableDatabase();

		try {
			int cntr = 0;
	    	c = sd.rawQuery(query, null);
	    	dates = new String[c.getCount()];
	    	while (c.moveToNext()) {
	    		int colid = c.getColumnIndex(LocationTable.L_DATE);
	    		locationDate = c.getString(colid);
	    		dates[cntr] = locationDate;
	    		cntr++;
	    	}
	    	return dates;
		} catch(Exception e) {
			if (D) Log.e(TAG, "getDistinctLocationDates exception = " + e.getMessage());
        } finally {
            if (c != null)
                c.close();
		}
    	return dates;		
	}

	/* ****************************************** */
	private Boolean isContractIdPresent(long idExt) {
		Boolean result = false;
		if (idExt > 0) {
			String query = "SELECT "+ ContractTable.ID +" FROM " + 
	    		ContractTable.TABLE_NAME + " WHERE " + ContractTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addContractItem(long idExt, long idEnt, long idEntU, int idState, int idOwner,  
			String name, String no, Date dateBegin, Date dateEnd) {
		java.sql.Date sqlDateBegin, sqlDateEnd;
		long result = 0;
		ContentValues cv = new ContentValues();
		cv.put(ContractTable.ID_STATE, idState);
		if (dateBegin != null) {
			sqlDateBegin = new java.sql.Date(dateBegin.getTime());
			cv.put(ContractTable.BEGIN_DATE, dateFormatDay.format(sqlDateBegin));
		}
		if (dateEnd != null) {
			sqlDateEnd = new java.sql.Date(dateEnd.getTime());
			cv.put(ContractTable.END_DATE, dateFormatDay.format(sqlDateEnd));
		}
		cv.put(ContractTable.NAME, name);
		cv.put(ContractTable.NO, no);
		
		SQLiteDatabase sd = getWritableDatabase();
		if (!isContractIdPresent(idExt)) {		
			cv.put(ContractTable.ID_EXTERNAL, idExt);
			cv.put(ContractTable.ID_ENT, idEnt);
			cv.put(ContractTable.ID_ENT_U, idEntU);
			cv.put(ContractTable.ID_OWNER, idOwner);
			result = sd.insert(ContractTable.TABLE_NAME, ContractTable.ID_EXTERNAL, cv);
		} else {
			if (sd.update(ContractTable.TABLE_NAME, cv, ContractTable.ID_EXTERNAL + "=" + idExt, null) == 1)
				result = idExt;
		}
		if (result < 0) Log.d(TAG, "addContractItem - Error");
		return result;
	}
	
	public long getContractsCount() {
		String query = "SELECT COUNT("+ ContractTable.ID +") FROM " + ContractTable.TABLE_NAME;
		return getTableCount(query);
	}

	/* ****************************************** */
	private Boolean isSpecIdPresent(long idExt) {
		Boolean result = false;
		if (idExt > 0) {
			String query = "SELECT "+ SpecsTable.ID +" FROM " + 
	    			SpecsTable.TABLE_NAME + " WHERE " + SpecsTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public Cursor getSpecs(long idExt) {
		Cursor result = null;
    	String querySelect = "SELECT s.*, ";
    	String q_nm = "(SELECT "+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
    			" g WHERE g."+ GoodsTable.ID_EXTERNAL +" = s."+ SpecsTable.ID_GDS +")as "+ 
    			SpecsRecord.SPECS_EQ_NM;
    	String q_type = "(SELECT "+ GoodsTable.GDSTYPE +" FROM "+ GoodsTable.TABLE_NAME +
    			" g WHERE g."+ GoodsTable.ID_EXTERNAL +" = s."+ SpecsTable.ID_GDS +")as "+ 
    			SpecsRecord.SPECS_EQ_TYPE;
    	String query = querySelect + q_nm + "," + q_type + " FROM " + SpecsTable.TABLE_NAME + " s WHERE " + 
   				SpecsTable.ID_EXTERNAL + " = " + idExt + " ORDER BY s." + SpecsTable.ID_GDS;
		
		if (idExt > 0) {
			SQLiteDatabase sd = getReadableDatabase();
			try {
		    	result = sd.rawQuery(query, null);
				if (!result.moveToNext()) {
					result = null;
				}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getSpecs exception = " + e.getMessage());
			} 
		}				
		return result;
	}
			
	private long getSpecId(long idExt) {
		long result = 0;
		if (idExt > 0) {
			String query = "SELECT "+ SpecsTable.ID +" FROM " + 
	    			SpecsTable.TABLE_NAME + " WHERE " + SpecsTable.ID_EXTERNAL + " = " + idExt;
			result = getTableCount(query);
		}
		return result;
	}
	
	public int getGdsType(int idGds) {
		int gdsType = 0;
		String query = "SELECT "+ GoodsTable.GDSTYPE +" FROM " + 
    			GoodsTable.TABLE_NAME + " WHERE " + GoodsTable.ID_EXTERNAL + " = " + idGds;
		gdsType = (int) getTableCount(query);
		return gdsType;
	}
	
	public long addSpecItem(long idExt,  long idEnt,  long idContract, long idGds, int isTemp,
			String dateChange, Date dateBegin, Date dateEnd, String fiskNum, String place, 
			String srl, String inv, String ipModem, String ipCash, int numCash, int factorySeal) {
		java.sql.Date sqlDateC, sqlDateB, sqlDateE;
		long result = 0;
		long id = getSpecId(idExt);
		ContentValues cv = new ContentValues();
		cv.put(SpecsTable.ID_ENT, idEnt);
		cv.put(SpecsTable.ID_CONTRACT, idContract);
		cv.put(SpecsTable.ID_GDS, idGds);
		cv.put(SpecsTable.TEMP, isTemp);
		if (dateBegin != null) {
			sqlDateB = new java.sql.Date(dateBegin.getTime());
			cv.put(SpecsTable.BEGIN_DATE, dateFormatDay.format(sqlDateB));
		}
		if (dateEnd != null) {
			sqlDateE = new java.sql.Date(dateEnd.getTime());
			cv.put(SpecsTable.END_DATE, dateFormatDay.format(sqlDateE));
		}
		if (dateChange != null) {
//			sqlDateC = new java.sql.Date(dateChange.getTime());
//			cv.put(SpecsTable.CHANGE_DATE, dateFormatDay.format(sqlDateC));
			cv.put(SpecsTable.CHANGE_DATE, dateChange);
		}
		cv.put(SpecsTable.FISK_NUM, fiskNum);
		cv.put(SpecsTable.PLACE, place);
		cv.put(SpecsTable.SRL, srl);
		cv.put(SpecsTable.INV, inv);
		cv.put(SpecsTable.IP_MODEM, ipModem);
		cv.put(SpecsTable.IP_CASH, ipCash);
		cv.put(SpecsTable.NUM_CASH, numCash);
		cv.put(SpecsTable.FACTORY_SEAL, factorySeal);
		
		SQLiteDatabase sd = getWritableDatabase();		
		if (id <= 0/*!isSpecIdPresent(idExt)*/) {
			cv.put(SpecsTable.ID_EXTERNAL, idExt);
			result = sd.insert(SpecsTable.TABLE_NAME, SpecsTable.ID_EXTERNAL, cv);
		} else {
			if (sd.update(SpecsTable.TABLE_NAME, cv, SpecsTable.ID + "=" + id, null) == 1)
				result = id;			
		}
		if (result < 0) Log.d(TAG, "addSpecItem - Error");
		return result;
	}
	
	public long getSpecsCount() {
		String query = "SELECT COUNT("+ SpecsTable.ID +") FROM " + SpecsTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public String getSpecsMaxDateChange() {
		String query = "SELECT MAX("+ SpecsTable.CHANGE_DATE +") FROM " + SpecsTable.TABLE_NAME;
		return getTableStringField(query);
	}	

	
	public long getSpecsIdContract(long idExt) {
		long idContr = -1;
		String query = "SELECT "+ SpecsTable.ID_CONTRACT +" FROM " + SpecsTable.TABLE_NAME +
				" WHERE " + SpecsTable.ID_EXTERNAL + "=" + idExt;
		
		Cursor c = null;
		SQLiteDatabase sd = getReadableDatabase();
		try {
	    	c = sd.rawQuery(query, null);
	    	if (c.moveToNext()) {
	    		int colid = c.getColumnIndex(SpecsTable.ID_CONTRACT);
	    		idContr = c.getInt(colid); 
	    	}
		} catch(Exception e) {
			if (D) Log.e(TAG, "getSpecsIdContract exception: " + e.getMessage());
        } finally {
            if (c != null)
                c.close();
		}
		return idContr;
	}
	
	public boolean updateSpecInventoryNum(int id, String inv) {
		Boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(SpecsTable.INV, inv);		
		cv.put(SpecsTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(SpecsTable.TABLE_NAME, cv, SpecsTable.ID + "=" + id, null) == 1;
		return result;		
	}
	
	public boolean updateSpecCashNum(int id, int num) {
		Boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(SpecsTable.NUM_CASH, num);		
		cv.put(SpecsTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(SpecsTable.TABLE_NAME, cv, SpecsTable.ID + "=" + id, null) == 1;
		return result;		
	}
	
	public boolean updateSpecCashIPAddr(int id, String ip) {
		Boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(SpecsTable.IP_CASH, ip);		
		cv.put(SpecsTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(SpecsTable.TABLE_NAME, cv, SpecsTable.ID + "=" + id, null) == 1;
		return result;		
	}
	
	public boolean updateSpecModemIPAddr(int id, String ip) {
		Boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(SpecsTable.IP_MODEM, ip);		
		cv.put(SpecsTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(SpecsTable.TABLE_NAME, cv, SpecsTable.ID + "=" + id, null) == 1;
		return result;		
	}
		
	public boolean updateSpecFactorySeal(int id, int seal) {
		Boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(SpecsTable.FACTORY_SEAL, seal);		
		cv.put(SpecsTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(SpecsTable.TABLE_NAME, cv, SpecsTable.ID + "=" + id, null) == 1;
		return result;		
	}
		
	public boolean setSpecsInvModified(int newModified) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(SpecsTable.MODIFIED, newModified);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(SpecsTable.TABLE_NAME, cv, null, null) > 0;
		return result;
	}	
	
	public boolean setSpecsIdModified(long extId, int newModified) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(SpecsTable.MODIFIED, newModified);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(SpecsTable.TABLE_NAME, cv, SpecsTable.ID_EXTERNAL + "=" + extId, null) == 1;
		return result;
	}	
	
	
	
	
	/* ****************************************** */
	private Boolean isMsgIdPresent(long idExt) {
		Boolean result = false;
		if (idExt > 0) {
			String query = "SELECT "+ MessagesTable.ID +" FROM " + 
	    			MessagesTable.TABLE_NAME + " WHERE " + MessagesTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addMsgItem(long idExt, long idSender, long idRecipient, int idState, int status, int a_size, Date m_date, 
			String dateChange, String subj, String msg, String a_nm) {
		java.sql.Date sqlDate = new java.sql.Date(m_date.getTime());
		long result = 0;
		SQLiteDatabase sd = getWritableDatabase();		
		ContentValues cv = new ContentValues();
		if (!isMsgIdPresent(idExt)) {
			cv.put(MessagesTable.ID_EXTERNAL, idExt);
			cv.put(MessagesTable.ID_SENDER, idSender);
			cv.put(MessagesTable.ID_RECIPIENT, idRecipient);
			cv.put(MessagesTable.ID_STATE, idState);
			cv.put(MessagesTable.STATUS, status);
			cv.put(MessagesTable.REC_STAT, 1);
			cv.put(MessagesTable.MSG_DATE, dateFormat.format(sqlDate));			
			if (dateChange.length() == 0)
				cv.put(MessagesTable.MSG_DATE_CHANGE, dateFormatYYMM.format(sqlDate));
			else
				cv.put(MessagesTable.MSG_DATE_CHANGE, dateChange);
			cv.put(MessagesTable.ATTACH_SIZE, a_size);
			cv.put(MessagesTable.SUBJ, subj);
			cv.put(MessagesTable.MSG, msg);
			cv.put(MessagesTable.ATTACH_NAME, a_nm);
			result = sd.insert(MessagesTable.TABLE_NAME, MessagesTable.MSG, cv);
		} else {
			cv.put(MessagesTable.ID_STATE, idState);
			cv.put(MessagesTable.REC_STAT, 1);
			if (dateChange.length() > 0)
				cv.put(MessagesTable.MSG_DATE_CHANGE, dateChange);
			if (sd.update(MessagesTable.TABLE_NAME, cv, MessagesTable.ID_EXTERNAL + "=" + idExt, null) == 1)		
				result = idExt;			
		}
		if (result < 0) Log.d(TAG, "addMsgItem - Error");
		return result;
	}
	
	public long addMsgItem(long idExt, long idSender, long idRecipient, int idState, int status, int a_size, Date mDate, 
			String dateChange, String subj, String msg, String a_nm, byte[] photo) {
		java.sql.Date sqlDate = new java.sql.Date(mDate.getTime());
		long result = 0;
		SQLiteDatabase sd = getWritableDatabase();		
		ContentValues cv = new ContentValues();
		if (!isMsgIdPresent(idExt)) {
			cv.put(MessagesTable.ID_EXTERNAL, idExt);
			cv.put(MessagesTable.ID_SENDER, idSender);
			cv.put(MessagesTable.ID_RECIPIENT, idRecipient);
			cv.put(MessagesTable.ID_STATE, idState);
			cv.put(MessagesTable.STATUS, status);
			cv.put(MessagesTable.REC_STAT, 1);
			cv.put(MessagesTable.MSG_DATE, dateFormat.format(sqlDate));
			if (dateChange.length() == 0)
				cv.put(MessagesTable.MSG_DATE_CHANGE, dateFormatYYMM.format(sqlDate));
			else
				cv.put(MessagesTable.MSG_DATE_CHANGE, dateChange);
			cv.put(MessagesTable.ATTACH_SIZE, a_size);
			cv.put(MessagesTable.SUBJ, subj);
			cv.put(MessagesTable.MSG, msg);
			cv.put(MessagesTable.ATTACH_NAME, a_nm);
			cv.put(MessagesTable.ATTACHMENT, photo);
			result = sd.insert(MessagesTable.TABLE_NAME, MessagesTable.MSG, cv);
		} else {
			cv.put(MessagesTable.ID_STATE, idState);
			cv.put(MessagesTable.REC_STAT, 1);
			if (dateChange.length() > 0)
				cv.put(MessagesTable.MSG_DATE_CHANGE, dateChange);
			if (sd.update(MessagesTable.TABLE_NAME, cv, MessagesTable.ID_EXTERNAL + "=" + idExt, null) == 1)		
				result = idExt;
		}
		if (result < 0) Log.d(TAG, "addMsgItem - Error");
		return result;
	}
	
	public long getMsgsCount() {
		String query = "SELECT COUNT("+ MessagesTable.ID +") FROM " + MessagesTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public long getMsgsModifiedCount() {
		String query = "SELECT COUNT("+ MessagesTable.ID +") FROM " + MessagesTable.TABLE_NAME +
				" WHERE " + MessagesTable.MODIFIED + "> 0 OR " + MessagesTable.ID_EXTERNAL + "= 0";
		return getTableCount(query);
	}
	
	public long getSenderNewMsgsCount(long idSender) {
		String query = "SELECT COUNT("+ MessagesTable.ID +") FROM " + MessagesTable.TABLE_NAME +
				" WHERE " + MessagesTable.ID_SENDER + "=" + idSender + " AND " + MessagesTable.ID_STATE +
				" = " + MessagesRecord.MSG_NEW;
		query += " AND " + MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		return getTableCount(query);
	}
	
	public long getRecipientNewMsgsCount(long idRecipient) {
		String query = "SELECT COUNT("+ MessagesTable.ID +") FROM " + MessagesTable.TABLE_NAME +
				" WHERE " + MessagesTable.ID_RECIPIENT + "=" + idRecipient + " AND " + MessagesTable.ID_STATE +
				" = " + MessagesRecord.MSG_NEW;
		query += " AND " + MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		return getTableCount(query);
	}
	
	public long getRecipientMaxMsgsId(long idRecipient) {
		String query = "SELECT MAX("+ MessagesTable.ID_EXTERNAL +") FROM " + MessagesTable.TABLE_NAME;// +
				//" WHERE " + MessagesTable.ID_RECIPIENT + "=" + idRecipient;
		return getTableCount(query);
	}
	
	public String getRecipientMaxDateChange(long idRecipient) {
		String query = "SELECT MAX("+ MessagesTable.MSG_DATE_CHANGE +") FROM " + MessagesTable.TABLE_NAME +
				" WHERE " + MessagesTable.ID_EXTERNAL + "> 0";
		query += " AND " + MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		return getTableStringField(query);
	}
	
	public boolean isSelfEmpHasNewMessages(long idEmp) {
		String query = "SELECT COUNT("+ MessagesTable.ID +") FROM " + MessagesTable.TABLE_NAME +
				" WHERE (" + MessagesTable.ID_SENDER + "=" + idEmp + " AND " +
				MessagesTable.ID_RECIPIENT + "=" + idEmp +
				") AND " + MessagesTable.ID_STATE + " = " + MessagesRecord.MSG_NEW;
		query += " AND " + MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		return (getTableCount(query) > 0);		
	}
	
	public boolean isEmpHasNewMessages(long idEmp) {
		String query = "SELECT COUNT("+ MessagesTable.ID +") FROM " + MessagesTable.TABLE_NAME +
				" WHERE (" + MessagesTable.ID_SENDER + "=" + idEmp + //" OR " +
				//MessagesTable.ID_RECIPIENT + "=" + idEmp +
				") AND " + MessagesTable.ID_STATE + " = " + MessagesRecord.MSG_NEW;
		query += " AND " + MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		return (getTableCount(query) > 0);		
	}
	
	public boolean isEmpHasNewImportantMessages(long idEmp) {
		String query = "SELECT COUNT("+ MessagesTable.ID +") FROM " + MessagesTable.TABLE_NAME +
				" WHERE (" + MessagesTable.ID_SENDER + "=" + idEmp + //" OR " +
				//MessagesTable.ID_RECIPIENT + "=" + idEmp +
				") AND " + MessagesTable.ID_STATE + " = " + MessagesRecord.MSG_NEW +
				" AND " + MessagesTable.STATUS + " = " + MessagesRecord.MSG_IMPORTANT;
		query += " AND " + MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		return (getTableCount(query) > 0);		
	}
	
	public boolean isEmpHasModifiedMessages(long idEmp) {
		String query = "SELECT COUNT("+ MessagesTable.ID +") FROM " + MessagesTable.TABLE_NAME +
				" WHERE (" + MessagesTable.ID_SENDER + "=" + idEmp + " OR " +
				MessagesTable.ID_RECIPIENT + "=" + idEmp +
				") AND " + MessagesTable.MODIFIED + " > 0 OR " + MessagesTable.ID_EXTERNAL + " = 0";
		return (getTableCount(query) > 0);		
	}
	
	public boolean setMsgRead(int id) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(MessagesTable.ID_STATE, MessagesRecord.MSG_READ);
		cv.put(MessagesTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(MessagesTable.TABLE_NAME, cv, MessagesTable.ID + "=" + id, null) == 1;
		return result;
	}
	
	public boolean setMsgExtId(long localId, long extId) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(MessagesTable.ID_EXTERNAL, extId);
		cv.put(MessagesTable.MODIFIED, 0);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(MessagesTable.TABLE_NAME, cv, MessagesTable.ID + "=" + localId, null) > 0;
		return result;		
	}
	
	public boolean exchandeMsgExtId(long oldId, long newId) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(MessagesTable.ID_EXTERNAL, newId);
		cv.put(MessagesTable.MODIFIED, 0);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(MessagesTable.TABLE_NAME, cv, MessagesTable.ID_EXTERNAL + "=" + oldId, null) > 0;
		return result;		
	}
	
	public boolean setEmpAllMsgRead(long idEmp) {
		boolean result = false;
		String sWhere = "(" + MessagesTable.ID_SENDER + " = " + idEmp + " OR " + 
				MessagesTable.ID_RECIPIENT + " = " + idEmp + ") AND " +
				MessagesTable.ID_STATE + " = " + MessagesRecord.MSG_NEW + " AND " +
				MessagesTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		ContentValues cv = new ContentValues();
		cv.put(MessagesTable.ID_STATE, MessagesRecord.MSG_READ);
		cv.put(MessagesTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(MessagesTable.TABLE_NAME, cv, sWhere, null) > 0;
		return result;
	}

	public boolean removeMsg(int id, long id_ext) {
		boolean result = false;
		SQLiteDatabase sd = getWritableDatabase();
		if (id_ext == 0) {
			result = sd.delete(MessagesTable.TABLE_NAME, MessagesTable.ID + "=" + id, null) > 0;
		} else {  // update rec_stat
			ContentValues cv = new ContentValues();
			cv.put(MessagesTable.MODIFIED, 1);
			cv.put(MessagesTable.REC_STAT, 2);
			result = sd.update(MessagesTable.TABLE_NAME, cv, MessagesTable.ID + "=" + id, null) == 1;				
		} 
		return result;
	}		
	

	/* ****************************************** */
	public long getLogsCount() {
		String query = "SELECT COUNT("+ LogsTable.ID +") FROM " + LogsTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public long addLogItem(int level, Date m_date, String msg) {
		java.sql.Date sqlDate = new java.sql.Date(m_date.getTime());
		long result = 0;
		ContentValues cv = new ContentValues();
		cv.put(LogsTable.LEVEL, level);
		cv.put(LogsTable.MSG_DATE, dateFormatMSYY.format(sqlDate));			
		cv.put(LogsTable.MSG, msg);

		SQLiteDatabase sd = getWritableDatabase();		
		result = sd.insert(LogsTable.TABLE_NAME, LogsTable.MSG, cv);
		if (result < 0) Log.d(TAG, "addLogItem - Error");
		return result;
	}
	
	public String[] getDistinctLogsDates() {
    	Cursor c = null;
		String[] dates = null;
		String logDate;
		String query = "SELECT DISTINCT SUBSTR("+ LogsTable.MSG_DATE + ",1,10)AS "+ LogsTable.MSG_DATE +
    			" FROM " + LogsTable.TABLE_NAME;
		SQLiteDatabase sd = getWritableDatabase();

		try {
			int cntr = 0;
	    	c = sd.rawQuery(query, null);
	    	dates = new String[c.getCount()];
	    	while (c.moveToNext()) {
	    		int colid = c.getColumnIndex(LogsTable.MSG_DATE);
	    		logDate = c.getString(colid);
	         	java.util.Date date;
	         	try {
	         		date = dateFormatDayYY.parse(logDate); 
		    		dates[cntr] = dateFormatDay.format(date);
		    		cntr++;
	         	} catch (ParseException e) {
	     			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	         	}		    		    				   	    		
	    	}
	    	return dates;
		} catch(Exception e) {
			if (D) Log.e(TAG, "getDistinctLogsDates exception = " + e.getMessage());
        } finally {
            if (c != null)
                c.close();
		}
    	return dates;		
	}
	
	public void clearLogs(String logDate) {
		java.util.Date date;
		String sWhere = null, logDateYY;
		
		if (logDate != null) {
	     	try {
	     		date = dateFormatDay.parse(logDate); 
	     		logDateYY = dateFormatDayYY.format(date);
	     		sWhere = "SUBSTR("+ LogsTable.MSG_DATE + ",1,10) = \""+ logDateYY + "\"";
	     	} catch (ParseException e) {
	 			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	     	}		    		    				   	    					
		} else
			sWhere = "1";
		if (sWhere != null) {
			SQLiteDatabase sd = getWritableDatabase();		
			sd.delete(LogsTable.TABLE_NAME, sWhere, null);
		}
	}
	
	public void clearOldLogs(String logDate) {
		java.util.Date date;
		String sWhere = null, logDateYY;
		if (logDate != null) {
	     	try {
	     		date = dateFormatDay.parse(logDate); 
	     		logDateYY = dateFormatDayYY.format(date);
	     		sWhere = "SUBSTR("+ LogsTable.MSG_DATE + ",1,10) <= \""+ logDateYY + "\"";
	     	} catch (ParseException e) {
	 			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	     	}		    		    				   	    					
		} else
			sWhere = "1";
		if (sWhere != null) {
			try {
				SQLiteDatabase sd = getWritableDatabase();		
				sd.delete(LogsTable.TABLE_NAME, sWhere, null);
			} catch (Exception e) {
				if (D) Log.e(TAG, "clearOldLogs exception = " + e.getMessage());
			}
		}
	}
	
	public int convertLogDateFormat() {
		String query = "SELECT "+ LogsTable.MSG_DATE + ", " + LogsTable.ID + 
				" FROM " + LogsTable.TABLE_NAME;
    	SQLiteDatabase sqdb = getReadableDatabase();
    	Cursor c = null;
    	String dateStr;
    	int cntr = 0, id; 		
		try {
			c = sqdb.rawQuery(query, null);
	    	while (c.moveToNext()) {	
	    		int colid = c.getColumnIndex(LogsTable.ID);
	    		id = c.getInt(colid);
	    		colid = c.getColumnIndex(LogsTable.MSG_DATE);
	    		dateStr = c.getString(colid);
	         	java.util.Date date;
	         	try {
	         		date = dateFormat.parse(dateStr); 
	         		updateLogDate(id, date);
	         		cntr++;
	         	} catch (ParseException e) {
	     			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	         	}		    		    				   
	    	}
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
	    	if (c != null) c.close();
		}
		return cntr;
	}
	
	public boolean updateLogDate(long id, Date m_date) {
		java.sql.Date sqlDate = new java.sql.Date(m_date.getTime());
		Boolean result = false;
		try {
			ContentValues cv = new ContentValues();
			cv.put(LogsTable.MSG_DATE, dateFormatYY.format(sqlDate));
			cv.put(LogsTable.MODIFIED, 1);
			SQLiteDatabase sd = getWritableDatabase();	
			result = sd.update(LogsTable.TABLE_NAME, cv, LogsTable.ID + "=" + id, null) == 1;
		} catch (Exception e) {
			if (D) Log.e(TAG, "updateLogDate exception = " + e.getMessage());
		}
		return result;		
	}
	
	
	/* ****************************************** */
	public long getTrafficStatCount() {
		String query = "SELECT COUNT("+ TrafficStatTable.ID +") FROM " + TrafficStatTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	private long isTrafficStatDatePresent(Date s_date) {
		long result = -1;
		if (s_date != null) {
			java.sql.Date sqlDate = new java.sql.Date(s_date.getTime());
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			String stat_date = formatter.format(s_date);
			SQLiteDatabase sd = getWritableDatabase();
			String query = "SELECT "+ TrafficStatTable.ID +" FROM " + TrafficStatTable.TABLE_NAME + 
					" WHERE " + TrafficStatTable.STAT_DATE + " = \"" + stat_date + "\"";
	    	Cursor c = sd.rawQuery(query, null);
			if (c.moveToNext()) {
	    		int colid = c.getColumnIndex(TrafficStatTable.ID);
	    		result = c.getLong(colid);				
			}
            if (c != null)
                c.close();
		}
		return result;
	}
	
	public TrafficStatRecord getTrafficStatDateData(Date s_date) {
		TrafficStatRecord res = null;
		if (s_date != null) {
			java.sql.Date sqlDate = new java.sql.Date(s_date.getTime());
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			String stat_date = formatter.format(s_date);
			SQLiteDatabase sd = getWritableDatabase();
			String query = "SELECT * FROM " + TrafficStatTable.TABLE_NAME + 
					" WHERE " + TrafficStatTable.STAT_DATE + " = \"" + stat_date + "\"";
	    	Cursor c = sd.rawQuery(query, null);
			if (c.moveToNext()) {
				res = new TrafficStatRecord(c);
			}
            if (c != null)
                c.close();
		}
		return res; 
	}
	
	public long addTrafficStatItem(long rx, long tx, long rx_json, long tx_json, Date s_date) {
		java.sql.Date sqlDate = new java.sql.Date(s_date.getTime());
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
		long result = 0;
		TrafficStatRecord res = getTrafficStatDateData(s_date);
		long id_found = (res != null) ? res.id : -1; //isTrafficStatDatePresent(s_date);
		
		ContentValues cv = new ContentValues();
		cv.put(TrafficStatTable.RX_BYTES, rx);
		cv.put(TrafficStatTable.TX_BYTES, tx);
		cv.put(TrafficStatTable.RX_JSON_BYTES, rx_json);
		cv.put(TrafficStatTable.TX_JSON_BYTES, tx_json);
		cv.put(TrafficStatTable.STAT_DATE, formatter.format(sqlDate));			

		SQLiteDatabase sd = getWritableDatabase();
		if (id_found < 0)
			result = sd.insert(TrafficStatTable.TABLE_NAME, TrafficStatTable.RX_BYTES, cv);
		else
			result = sd.update(TrafficStatTable.TABLE_NAME, cv, TrafficStatTable.ID + "=" + id_found, null);
		if (result < 0) Log.d(TAG, "addTrafficStatItem - Error");
		return result;
	}
	
	public String[] getTrafficStatDates() {
    	Cursor c = null;
		String[] dates = null;
		String logDate;
		String query = "SELECT DISTINCT SUBSTR("+ TrafficStatTable.STAT_DATE + ",1,10)AS "+ 
				TrafficStatTable.S_DATE + " FROM " + TrafficStatTable.TABLE_NAME;
		SQLiteDatabase sd = getWritableDatabase();

		try {
			int cntr = 0;
	    	c = sd.rawQuery(query, null);
	    	dates = new String[c.getCount()];
	    	while (c.moveToNext()) {
	    		int colid = c.getColumnIndex(TrafficStatTable.S_DATE);
	    		logDate = c.getString(colid);
	    		dates[cntr] = logDate;
	    		cntr++;
	    	}
	    	return dates;
		} catch(Exception e) {
			if (D) Log.e(TAG, "getTrafficStatDates exception = " + e.getMessage());
		} 		   	
    	return dates;		
	}
	
	
	/* ****************************************** */
	private Boolean isEmpIdPresent(int idExt) {
		Boolean result = true;
		if (idExt > 0) {
			String query = "SELECT "+ EmpTable.ID +" FROM " + 
	    			EmpTable.TABLE_NAME + " WHERE " + EmpTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addEmpItem(int idExt, int idEnt, String surname, String name, String patronimic) {
		long result = 0;
		if (!isEmpIdPresent(idExt)) {
			ContentValues cv = new ContentValues();
			cv.put(EmpTable.ID_EXTERNAL, idExt);
			cv.put(EmpTable.ID_ENT, idEnt);
			cv.put(EmpTable.SURNAME, surname);
			cv.put(EmpTable.NAME, name);
			cv.put(EmpTable.PATRONIMIC, patronimic);
			SQLiteDatabase sd = getWritableDatabase();
			
			result = sd.insert(EmpTable.TABLE_NAME, EmpTable.SURNAME, cv);
		}
		if (result < 0) Log.d(TAG, "addEmpItem - Error");
		return result;
	}
	
	public long getEmpsCount() {
		String query = "SELECT COUNT("+ EmpTable.ID +") FROM " + EmpTable.TABLE_NAME;
		return getTableCount(query);
	}	
		
	public Cursor getEmpName(int idExt) {
		Cursor result = null;
		if(idExt > 0){
			SQLiteDatabase sd = getReadableDatabase();
			try {
		    	result = sd.rawQuery("SELECT * FROM " +  
		    			EmpTable.TABLE_NAME + " WHERE " + EmpTable.ID_EXTERNAL + " = " + idExt, null);
				if (!result.moveToNext()){
					result = null;
			}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getEmpName exception = " + e.getMessage());
			}
		}				
		return result;
	}
	
	
	/* ****************************************** */
	public long getGdsCount() {
		String query = "SELECT COUNT("+ GoodsTable.ID +") FROM " + GoodsTable.TABLE_NAME;
		return getTableCount(query);
	}

	private Boolean isGdsIdPresent(long idExt) {
		Boolean result = true;
		if (idExt > 0) {
			String query = "SELECT "+ GoodsTable.ID +" FROM " + 
	    			GoodsTable.TABLE_NAME + " WHERE " + GoodsTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public boolean isGdsIdEnableEmpHW(int idExt) {
		boolean result = false;	
		if (idExt > 0) {
			Cursor c = null;
			SQLiteDatabase sd = getReadableDatabase();
	    	try {
	    		c = sd.rawQuery("SELECT "+ GoodsTable.GDSTYPE + ", "+ GoodsTable.ID_PARENT +" FROM " + 
		    			GoodsTable.TABLE_NAME + " WHERE " + GoodsTable.ID_EXTERNAL + " = " + idExt, null);
	    		if (c != null && c.moveToFirst()) {
		    		result = (c.getInt(0) == GoodsRecord.JOB_ENABLE_EMP_HW)&&(c.getInt(1) != GoodsRecord.SEAL_JOB_PARENT);
	    		}
	    	} catch (Exception e) {
	    		if (D) Log.e(TAG, "isGdsIdEnableEmpHW exception = " + e.getMessage());
			} finally {
				if (c != null)
					c.close();
			}
		}
		return result;
	}
	
	public boolean isGdsIdSealRelated(int idExt) {
		boolean result = false;	
		if (idExt > 0) {
			Cursor c = null;
			SQLiteDatabase sd = getReadableDatabase();
	    	try {
	    		c = sd.rawQuery("SELECT "+ GoodsTable.GDSTYPE + ", "+ GoodsTable.ID_PARENT +" FROM " + 
		    			GoodsTable.TABLE_NAME + " WHERE " + GoodsTable.ID_EXTERNAL + " = " + idExt, null);
	    		if (c != null && c.moveToFirst()) {
		    		result = (c.getInt(1) == GoodsRecord.SEAL_JOB_PARENT);		    		
	    		}
	    	} catch (Exception e) {
	    		if (D) Log.e(TAG, "isGdsIdSealRelated exception = " + e.getMessage());
			} finally {
				if (c != null)
					c.close();
			}
		}
		return result;
	}
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addGdsItem(long idExt, int idParent, int ttype, int gdsType, 
			String itemName, boolean checkPresent) {
		long result = 0;
		// CREATE A CONTENTVALUE OBJECT		
		ContentValues cv = new ContentValues();
		cv.put(GoodsTable.ID_PARENT, idParent);
		cv.put(GoodsTable.TTYPE, ttype);
		cv.put(GoodsTable.GDSTYPE, gdsType);
		cv.put(GoodsTable.NM, itemName);
		// RETRIEVE WRITEABLE DATABASE AND INSERT
		SQLiteDatabase sd = getWritableDatabase();
		if (!checkPresent || !isGdsIdPresent(idExt)) {			
			cv.put(GoodsTable.ID_EXTERNAL, idExt);
			result = sd.insert(GoodsTable.TABLE_NAME, GoodsTable.NM, cv);
		} else {
			result = sd.update(GoodsTable.TABLE_NAME, cv, GoodsTable.ID_EXTERNAL + "=" + idExt, null);		
		}
		if (result < 0) Log.d(TAG, "addGdsItem - Error");
		return result;
	}
	
	// METHOD FOR SAFELY REMOVING AN ITEM 
	public boolean removeGdsItem(int gdsId) {
		SQLiteDatabase sd = getWritableDatabase();
		String[] whereArgs = new String[] { String.valueOf(gdsId) };
		int result = sd.delete(GoodsTable.TABLE_NAME, GoodsTable.ID + "= ? ", whereArgs);
		return (result > 0);
	}
	
	public Cursor getGdsName(int idExt) {
		Cursor result = null;
		if (idExt > 0) {
			SQLiteDatabase sd = getReadableDatabase();
			try {
		    	result = sd.rawQuery("SELECT "+ GoodsTable.NM +" FROM " + 
		    			GoodsTable.TABLE_NAME + " WHERE " + GoodsTable.ID_EXTERNAL + " = " + idExt, null);
				if (!result.moveToNext()){
					result = null;
			}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getGoodsName exception = " + e.getMessage());
			} 
		}				
		return result;
	}
	
	/* ****************************************** */
	private Boolean isConstIdPresent(int idExt) {
		Boolean result = true;
		if (idExt > 0) {
			String query = "SELECT "+ ConstTable.ID +" FROM " + 
	    			ConstTable.TABLE_NAME + " WHERE " + ConstTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addConstItem(int idExt, int id_type, int ttype, String nmVal, String itemName) {
		long result = 0;
		if (!isConstIdPresent(idExt)) {
			// CREATE A CONTENTVALUE OBJECT		
			ContentValues cv = new ContentValues();
			cv.put(ConstTable.ID_EXTERNAL, idExt);
			cv.put(ConstTable.TYPE_ID, id_type);
			cv.put(ConstTable.TTYPE, ttype);
			cv.put(ConstTable.NMVAL, nmVal);
			cv.put(ConstTable.NM, itemName);
			// RETRIEVE WRITEABLE DATABASE AND INSERT
			SQLiteDatabase sd = getWritableDatabase();			
			result = sd.insert(ConstTable.TABLE_NAME, ConstTable.NM, cv);
		}
		if (result < 0) Log.d(TAG, "addConstItem - Error");
		return result;
	}
	
	public int getConstTypeId(long id) {
		int result = 0;
		SQLiteDatabase sd = getWritableDatabase();
    	Cursor c = sd.rawQuery("SELECT "+ ConstTable.TYPE_ID +" FROM " + 
    			ConstTable.TABLE_NAME + " WHERE " + ConstTable.ID_EXTERNAL + " = " + id, null);
    	if (c.moveToNext()) {
    		int colid = c.getColumnIndex(ConstTable.TYPE_ID);
    		result = c.getInt(colid); 
    	}
        if (c != null)
            c.close();
		return result;
	}
		
	
	/* ****************************************** */
	public long getEntCount() {
		String query = "SELECT COUNT("+ EntTable.ID +") FROM " + EntTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public Cursor getEntName(int idExt) {
		Cursor result = null;
		if (idExt > 0) {
			SQLiteDatabase sd = getReadableDatabase();
			try {
		    	result = sd.rawQuery("SELECT "+ EntTable.ID_EXTERNAL + "," + EntTable.NM + "," + EntTable.ADDR + "," + 
		    			EntTable.LAT + "," + EntTable.LNG + "," + EntTable.ID_PARENT + " FROM " + 
		    			EntTable.TABLE_NAME + " WHERE " + EntTable.ID_EXTERNAL + " = " + idExt, null);
				if (!result.moveToNext()) {
					result = null;
    			}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getEntName exception = " + e.getMessage());
			} 
		}				
		return result;
	}
	
	private Boolean isEntIdPresent(long idExt) {
		Boolean result = true;
		if (idExt > 0) {
			String query = "SELECT "+ EntTable.ID +" FROM " + 
	    			EntTable.TABLE_NAME + " WHERE " + EntTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addEntItem(long idExt, int idParent, double lat, double lng, String Addr, 
			String itemName, boolean checkPresent) {
		long result = 0;
		// CREATE A CONTENTVALUE OBJECT		
		ContentValues cv = new ContentValues();
		cv.put(EntTable.ID_EXTERNAL, idExt);
		cv.put(EntTable.ID_PARENT, idParent);
		cv.put(EntTable.LAT, lat);
		cv.put(EntTable.LNG, lng);
		cv.put(EntTable.ADDR, Addr);
		cv.put(EntTable.NM, itemName);
		// RETRIEVE WRITEABLE DATABASE AND INSERT
		SQLiteDatabase sd = getWritableDatabase();
		try {
			if (!checkPresent || !isEntIdPresent(idExt)) {
				result = sd.insert(EntTable.TABLE_NAME, EntTable.NM, cv);
			} else {
				result = sd.update(EntTable.TABLE_NAME, cv, EntTable.ID_EXTERNAL + "=" + idExt, null);
			}
		} catch(Exception e) {
			if (D) Log.e(TAG, "exception = " + e.getMessage());	
		}
		if (result < 0) Log.d(TAG, "addEntItem - Error");
		return result;
	}
	
	public Boolean setEntCoords(long idExt, double lat, double lng) {
		boolean result = false;

		ContentValues cv = new ContentValues();
		cv.put(EntTable.LAT, lat);
		cv.put(EntTable.LNG, lng);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(EntTable.TABLE_NAME, cv, EntTable.ID_EXTERNAL + "=" + idExt, null) == 1;
		return result;
	}	

	
	/* ****************************************** */
	public boolean isWarrantModified(long idWarr) {
		boolean result = false;		
		String query = "SELECT "+ WarrContTable.ID +" FROM " + WarrContTable.TABLE_NAME + " WHERE (" + 
				WarrContTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrContTable.MODIFIED + " > 0" +
				") AND " + WarrContTable.ID_WARRANT + "=" + idWarr;		
		result = isRecordPresent(query);
		return result;
	}		
		
	public boolean isWarrantsModified() {
		boolean result = false;
		String query;
		query = "SELECT "+ WarrRepairTable.ID +" FROM " + WarrRepairTable.TABLE_NAME + " WHERE " + 
				WarrRepairTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrRepairTable.MODIFIED + " > 0";
		result = isRecordPresent(query);
		if (!result) {
			query = "SELECT "+ WarrUnitTable.ID +" FROM " + WarrUnitTable.TABLE_NAME + " WHERE " + 
					WarrUnitTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrUnitTable.MODIFIED + " > 0";
			result = isRecordPresent(query);
			if (!result) {
				query = "SELECT "+ WarrContTable.ID +" FROM " + WarrContTable.TABLE_NAME + " WHERE " + 
						WarrContTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrContTable.MODIFIED + " > 0";
				result = isRecordPresent(query);
			}
		}		
		return result;
	}	
	
	private long getWarrantLocalId(long idExt) {
		String query = "SELECT "+ WarrantTable.ID +" FROM " + 
    		WarrantTable.TABLE_NAME + " WHERE " + WarrantTable.ID_EXTERNAL + " = " + idExt;			
		return getTableCount(query);
	}
	
	public long addWarrantItem(long idExt, int num, long IdContract, int idEnt,
                               int idSub, int state, Date d_open,
			String rem, int idOwner, int toSign, String signDate, long id_sign_user, String dateChange, int regl) {
		java.sql.Date sqlDate = new java.sql.Date(d_open.getTime());
		long result = getWarrantLocalId(idExt);
		if (idExt > 0) {
			ContentValues cv = new ContentValues();
			cv.put(WarrantTable.IDSTATE, state);
			cv.put(WarrantTable.DATE_CHANGE, dateChange);
			cv.put(WarrantTable.REMARK, rem);
			cv.put(WarrantTable.TO_SIGN, toSign);
			cv.put(WarrantTable.D_SIGN, signDate);
			cv.put(WarrantTable.ID_USER_SIGN, id_sign_user);
			cv.put(WarrantTable.REGL, regl);

			cv.put(WarrantTable.MODIFIED, 0);
			SQLiteDatabase sd = getWritableDatabase();
			if (result == 0) {
				cv.put(WarrantTable.ID_EXTERNAL, idExt);
				cv.put(WarrantTable.NUM, num);
				cv.put(WarrantTable.ID_CONTRACT, IdContract);
				cv.put(WarrantTable.ID_ENT, idEnt);
				cv.put(WarrantTable.ID_SUBENT, idSub);
				cv.put(WarrantTable.D_OPEN, dateFormat.format(sqlDate));
				cv.put(WarrantTable.ID_OWNER, idOwner);
				result = sd.insert(WarrantTable.TABLE_NAME, WarrantTable.NUM, cv);
			} else {
				sd.update(WarrantTable.TABLE_NAME, cv, WarrantTable.ID_EXTERNAL + "=" + idExt, null);
			}
		}
		if (result < 0) Log.d(TAG, "addWarrantItem - Error");
		return result;
	}




	
	public int delWarrantItem(long idExt) {
		SQLiteDatabase sd = getWritableDatabase();
		String where;
		int res;
		where = WarrRepairTable.ID_WARR_UNIT + 
			" IN (SELECT " + WarrUnitTable.ID + " FROM " + WarrUnitTable.TABLE_NAME + " WHERE " + WarrUnitTable.ID_WARR_CONT +  
			" IN (SELECT " + WarrContTable.ID + " FROM " + WarrContTable.TABLE_NAME + " WHERE " + 
				WarrContTable.ID_WARRANT + "=" + idExt + "))";
		res = sd.delete(WarrRepairTable.TABLE_NAME, where, null);
		where = WarrUnitTable.ID_WARR_CONT + 
			" IN (SELECT " + WarrContTable.ID + " FROM " + WarrContTable.TABLE_NAME + " WHERE " + 
				WarrContTable.ID_WARRANT + "=" + idExt + ")";
		res = sd.delete(WarrUnitTable.TABLE_NAME, where, null);
		where = WarrContTable.ID_WARRANT + "=" + idExt;
		res = sd.delete(WarrContTable.TABLE_NAME, where, null);
		return sd.delete(WarrantTable.TABLE_NAME, WarrantTable.ID_EXTERNAL + "=" + idExt, null);
	}
	
	public boolean setWarrantLocalState(long idWarr, int warrState) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(WarrantTable.LOCAL_STATE, warrState);		
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrantTable.TABLE_NAME, cv, WarrantTable.ID_EXTERNAL + "=" + idWarr, null) == 1;
		return result;
	}	

	public boolean setWarrantIdState(long idWarr, int IdState) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(WarrantTable.IDSTATE, IdState);	
		cv.put(WarrantTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrantTable.TABLE_NAME, cv, WarrantTable.ID_EXTERNAL + "=" + idWarr, null) == 1;
		return result;
	}	

	public boolean setWarrantSigned(long idWarr, long IdUserSign) {
		boolean result = false;	
		java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
		
		ContentValues cv = new ContentValues();
		cv.put(WarrantTable.ID_USER_SIGN, IdUserSign);
		cv.put(WarrantTable.D_SIGN, dateFormat.format(sqlDate));
		cv.put(WarrantTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrantTable.TABLE_NAME, cv, WarrantTable.ID_EXTERNAL + "=" + idWarr, null) == 1;
		return result;
	}	

	public boolean setWarrantModifiedState(long idWarr, boolean Modified) {
		boolean result = false;
		int fModified = (Modified) ? 1 : 0;
		ContentValues cv = new ContentValues();
		cv.put(WarrantTable.MODIFIED, fModified);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrantTable.TABLE_NAME, cv, WarrantTable.ID_EXTERNAL + "=" + idWarr, null) == 1;
		return result;
	}	
	
	public boolean setWarrantModifiedState(long idWarr) {
		boolean result = false;
		int fModified = (isWarrantModified(idWarr)) ? 1 : 0;
		ContentValues cv = new ContentValues();
		cv.put(WarrantTable.MODIFIED, fModified);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrantTable.TABLE_NAME, cv, WarrantTable.ID_EXTERNAL + "=" + idWarr, null) == 1;
		return result;
	}	
	
	public String[] getWarrantNums() {
    	Cursor c = null, ce = null;
		String[] nums = null;
		String wNum;
		int colid;
		String query = "SELECT " + WarrantTable.ID_EXTERNAL + ", " + WarrantTable.NUM +
    			" FROM " + WarrantTable.TABLE_NAME + " ORDER BY " + WarrantTable.ID_EXTERNAL + " DESC";
		String query_emp = "SELECT DISTINCT " + EmpWorkTimeTable.ID_WARRANT + " FROM " + EmpWorkTimeTable.TABLE_NAME +
				" WHERE " + EmpWorkTimeTable.ID_WARRANT + " NOT IN (" +
    			"SELECT " + WarrantTable.ID_EXTERNAL +
    			" FROM " + WarrantTable.TABLE_NAME + ")";
		SQLiteDatabase sd = getWritableDatabase();
		try {
			int cntr = 0;
	    	c = sd.rawQuery(query, null);
	    	ce = sd.rawQuery(query_emp, null);
	    	nums = new String[c.getCount() + ce.getCount()];
	    	while (c.moveToNext()) {
	    		colid = c.getColumnIndex(WarrantTable.NUM);
	    		wNum = c.getString(colid);
	    		nums[cntr] = wNum;
	    		cntr++;
	    	}
	    	while (ce.moveToNext()) {
	    		colid = ce.getColumnIndex(EmpWorkTimeTable.ID_WARRANT);
	    		wNum = ce.getString(colid);
	    		nums[cntr] = wNum;
	    		cntr++;
	    	}
	    	return nums;
		} catch(Exception e) {
			if (D) Log.e(TAG, "getWarrantNums exception = " + e.getMessage());
        } finally {
            if (c != null)
                c.close();
            if (ce != null)
                ce.close();
		}
    	return nums;
	}	

	public int getWarrantLocalState(long idWarr) {
		String query = "SELECT COUNT("+ WarrantTable.LOCAL_STATE +") FROM " + WarrantTable.TABLE_NAME +
				" WHERE " + WarrantTable.ID_EXTERNAL + " = " + idWarr;
		return (int) getTableCount(query);
	}	

	public long getWarrantCount() {
		String query = "SELECT COUNT("+ WarrantTable.ID +") FROM " + WarrantTable.TABLE_NAME;
		return getTableCount(query);
	}

	public long getModifiedWarrantCount() {
		String query = "SELECT COUNT("+ WarrantTable.ID +") FROM " + WarrantTable.TABLE_NAME +
				" WHERE " + WarrantTable.MODIFIED + " > 0";
		return getTableCount(query);
	}

	public long getMaxWarrantId() {
		String query = "SELECT MAX("+ WarrantTable.ID_EXTERNAL +") FROM " + WarrantTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public String getWarrantMaxDateChange() {
		String query = "SELECT MAX("+ WarrantTable.DATE_CHANGE +") FROM " + WarrantTable.TABLE_NAME +
				" WHERE " + WarrantTable.ID_EXTERNAL + "> 0";
//		query += " AND " + WarrantTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		return getTableStringField(query);
	}	

	public long getWarrantContCount(long idExt) {
		String query = "SELECT COUNT("+ WarrContTable.ID +") FROM " + WarrContTable.TABLE_NAME +
				" WHERE " + WarrContTable.ID_WARRANT + " = " + idExt + " AND " + 
				WarrContTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		if (idExt > 0) return getTableCount(query);		
		else return 0;
	}

	public long getWarrContUnitCount(long id) {
		String query = "SELECT COUNT("+ WarrUnitTable.ID +") FROM " + WarrUnitTable.TABLE_NAME +
				" WHERE " + WarrUnitTable.ID_WARR_CONT + " = " + id + " AND "+
						WarrUnitTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		if (id > 0) return getTableCount(query);		
		else return 0;
	}

	public long getWarrUnitRepairCount(long id) {
		String query = "SELECT COUNT("+ WarrRepairTable.ID +") FROM " + WarrRepairTable.TABLE_NAME +
				" WHERE " + WarrRepairTable.ID_WARR_UNIT + " = " + id + " AND "+
				WarrRepairTable.REC_STAT + DBSchemaHelper.REC_STAT_NODEL_CONDITION;
		if (id > 0) return getTableCount(query);		
		else return 0;
	}

	public long getWarrantNoLocalContractCount() {
		String query = "SELECT COUNT(w."+ WarrantTable.ID +") FROM " + WarrantTable.TABLE_NAME +
				" w WHERE (SELECT c." + ContractTable.ID + " FROM " + ContractTable.TABLE_NAME + 
					" c WHERE c." + ContractTable.ID_EXTERNAL + "=w." + WarrantTable.ID_CONTRACT +") IS NULL";
//		query = "SELECT "+ WarrantTable.ID_EXTERNAL +" FROM " + WarrantTable.TABLE_NAME +
//				" WHERE (SELECT " + ContractTable.ID + " FROM " + ContractTable.TABLE_NAME +
//					" WHERE " + ContractTable.ID_EXTERNAL + "=" + WarrantTable.ID_CONTRACT +") IS NULL";
		return getTableCount(query);
	}

	
	/* ****************************************** */
	private long getWarrContLocalId(long idExt) {
		String query = "SELECT "+ WarrContTable.ID +" FROM " + 
    		WarrContTable.TABLE_NAME + " WHERE " + WarrContTable.ID_EXTERNAL + " = " + idExt;			
		return getTableCount(query);
	}
	
	public boolean isWarrContSpecsIdPresent(long idSpec, long w_id, boolean only_check) {
		Boolean result = true;
		if (idSpec > 0) {
			SQLiteDatabase sd = getWritableDatabase();
	    	Cursor c = sd.rawQuery("SELECT "+ WarrContTable.ID +", "+ WarrContTable.REC_STAT +" FROM " + 
	    			WarrContTable.TABLE_NAME + " WHERE " + WarrContTable.EQ_SPEC + " = " + idSpec +
	    			" AND " + WarrContTable.ID_WARRANT + " = " + w_id, null);
			result = c.moveToNext();
			if (result) {  // если запись найдена, проверяем не была ли она удалена ранее
				int colid = c.getColumnIndex(WarrContTable.REC_STAT);
				int recStat = c.getInt(colid);
				colid = c.getColumnIndex(WarrContTable.ID);
				long recId = c.getLong(colid);
				if (recStat == 2) { // если была удалена - восстанавливаем
					if (!only_check) {
						ContentValues cv = new ContentValues();
						cv.put(WarrContTable.REC_STAT, 1);		
						cv.put(WarrContTable.MODIFIED, 1);
						int upd_result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + recId, null);
					} else {
						result = false;
					}
				}	
			}
            if (c != null)
                c.close();
		}
		return result;
	}
	
	public long getWarrContSpecsId(long idSpec, long w_id) {
		long result = 0;
		if (idSpec > 0) {
			SQLiteDatabase sd = getWritableDatabase();
	    	Cursor c = sd.rawQuery("SELECT "+ WarrContTable.ID +", "+ WarrContTable.REC_STAT +" FROM " + 
	    			WarrContTable.TABLE_NAME + " WHERE " + WarrContTable.EQ_SPEC + " = " + idSpec +
	    			" AND " + WarrContTable.ID_WARRANT + " = " + w_id, null);
			if (c.moveToNext()) {  // если запись найдена, проверяем не была ли она удалена ранее
				int colid = c.getColumnIndex(WarrContTable.REC_STAT);
				int recStat = c.getInt(colid);
				colid = c.getColumnIndex(WarrContTable.ID);
				result = c.getLong(colid);
				if (recStat == 2) { // если была удалена - восстанавливаем
					ContentValues cv = new ContentValues();
					cv.put(WarrContTable.REC_STAT, 1);		
					cv.put(WarrContTable.MODIFIED, 1);
					int upd_result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + result, null);
				}	
			}
            if (c != null)
                c.close();
		}
		return result;
	}
	
	public long _addWarrContItem(boolean ins, long idExt, long idWarrant, int idEq, int eqStatus, long eqSpec, 
			String eqSrl, String eqInv,  
			int idJob, int jobStatus, int tblMode, String rem) {
		long result = getWarrContLocalId(idExt);
		if (result == 0) {
			result = getWarrContSpecsId(eqSpec, idWarrant); 
		}
		SQLiteDatabase sd = getWritableDatabase();		
		ContentValues cv = new ContentValues();
		cv.put(WarrContTable.ID_EQ, idEq);
		cv.put(WarrContTable.ID_JOB, idJob);
		cv.put(WarrContTable.JOB_STATUS, jobStatus);
		cv.put(WarrContTable.EQ_STATUS, eqStatus);
		cv.put(WarrContTable.EQ_SPEC, eqSpec);
		cv.put(WarrContTable.EQ_SRL, eqSrl);
		cv.put(WarrContTable.EQ_INV, eqInv);
		cv.put(WarrContTable.TBL_MODE, tblMode);
		cv.put(WarrContTable.REMARK, rem);
		cv.put(WarrContTable.ID_EXTERNAL, idExt);
		cv.put(WarrContTable.MODIFIED, 0);
		if (ins || result == 0) {
			cv.put(WarrContTable.ID_WARRANT, idWarrant);
			result = sd.insert(WarrContTable.TABLE_NAME, WarrContTable.ID_EXTERNAL, cv);			
		} else {
			sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + result, null);
		}
		return result;
	}
	
	public long addWarrContItem(long idExt, long idWarrant, int idEq, int eqStatus, long eqSpec, 
			String eqSrl, String eqInv, int idJob, int jobStatus, int tblMode, String rem) {
		long result = 0;
		if (idExt > 0) {
			result = _addWarrContItem(false, idExt, idWarrant, idEq, eqStatus, eqSpec, eqSrl, eqInv, 
					idJob, jobStatus, tblMode, rem);
		} else {
			if (!isWarrContSpecsIdPresent(eqSpec, idWarrant, false)) {
				result = _addWarrContItem(true, idExt, idWarrant, idEq, eqStatus, eqSpec, eqSrl, eqInv, 
						idJob, jobStatus, tblMode, rem);				
			}
		}
		if (result < 0) Log.d(TAG, "addWarrContItem - Error");
		return result;
	}
	
	public int delWarrContItem(long idExt) {
		SQLiteDatabase sd = getWritableDatabase();
		return sd.delete(WarrContTable.TABLE_NAME, WarrContTable.ID_EXTERNAL + "=" + idExt, null);
	}
	
	public boolean changeWarrContEqSrl(long idWarrCont, long eqSpecNew, int idEq, String eqSrl, String eqInv) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(WarrContTable.ID_EQ, idEq);
		cv.put(WarrContTable.EQ_SPEC, eqSpecNew);
		cv.put(WarrContTable.EQ_SRL, eqSrl);
		cv.put(WarrContTable.EQ_INV, eqInv);
		cv.put(WarrContTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();		
		result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + idWarrCont, null) == 1;		
		return result;
	}
	
	public boolean changeWarrContEqJob(long idWarrCont, long idJobNew) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(WarrContTable.ID_JOB, idJobNew);
		cv.put(WarrContTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();		
		result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + idWarrCont, null) == 1;		
		return result;
	}
	
	public boolean removeWarrContSpecsId(int id, long id_ext, long spec_id) {
		boolean result = false;
		if (spec_id > 0) {
			SQLiteDatabase sd = getWritableDatabase();
			if (id_ext == 0) {
				result = sd.delete(WarrContTable.TABLE_NAME, WarrContTable.EQ_SPEC + "="+ spec_id + " AND " + 
						WarrContTable.ID + "=" + id, null) > 0;
			} else {  // update rec_stat
				ContentValues cv = new ContentValues();
				cv.put(WarrContTable.MODIFIED, 1);
				cv.put(WarrContTable.REC_STAT, 2);
				result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + id, null) == 1;				
			} 
		}
		return result;
	}		
			
	public long getWarrContCount() {
		String query = "SELECT COUNT("+ WarrContTable.ID +") FROM " + WarrContTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	private long getWarrContJobInProgressCount(long idWarr) {
		String query = "SELECT COUNT("+ WarrContTable.ID +") FROM " + WarrContTable.TABLE_NAME +
				" WHERE "+ WarrContTable.JOB_STATUS +" = "+ StateRecord.JOB_STATE_TODO +
				" AND " + WarrContTable.ID_WARRANT + " = " + idWarr;
		return getTableCount(query);
	}
	
	public boolean setWarrContJobEqState(long idWarr, long idWarrCont, int jobState, int eqState) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(WarrContTable.JOB_STATUS, jobState);		
		cv.put(WarrContTable.EQ_STATUS, eqState);
		cv.put(WarrContTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + idWarrCont, null) == 1;
		long jobCntr = getWarrContJobInProgressCount(idWarr);
		int wState = (jobCntr == 0) ? WarrantRecord.W_DONE : WarrantRecord.W_IN_PROGRESS;
		setWarrantLocalState(idWarr, wState);
		setWarrantModifiedState(idWarr);
		return result;
	}	

	public boolean setWarrContModifiedState(long idWarrCont) {
		boolean result = false;
		int fModified = (isWarrContModified(idWarrCont)) ? 1 : 0;

		ContentValues cv = new ContentValues();
		cv.put(WarrContTable.MODIFIED, fModified);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + idWarrCont, null) == 1;
		return result;
	}	
	
	public boolean updateWarrContRemark(int id, String rem) {
		Boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(WarrContTable.REMARK, rem);		
		cv.put(WarrContTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.ID + "=" + id, null) == 1;
		return result;		
	}
	
	public boolean updateWarrContInventoryNum(int id_specs, String inv) {
		Boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(WarrContTable.EQ_INV, inv);		
		//cv.put(WarrContTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrContTable.TABLE_NAME, cv, WarrContTable.EQ_SPEC + "=" + id_specs, null) >= 1;
		return result;		
	}
	
	public boolean isWarrContModified(long idWarrCont) {
		boolean result = false;		
		String query = "SELECT "+ WarrUnitTable.ID +" FROM " + WarrUnitTable.TABLE_NAME + " WHERE (" + 
				WarrUnitTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrUnitTable.MODIFIED + " > 0" +
				") AND " + WarrUnitTable.ID_WARR_CONT + "="+ idWarrCont;
		result = isRecordPresent(query);
		if (!result) {
			String r_query = "SELECT "+ WarrRepairTable.ID +" FROM " + WarrRepairTable.TABLE_NAME + " WHERE (" + 
					WarrRepairTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrRepairTable.MODIFIED + " > 0" +
					") AND " + WarrRepairTable.ID_WARR_UNIT + " IN ("+ query + ")";
			result = isRecordPresent(r_query);
		}	
		return result;
	}		
	
	public int getWarrantNumFromCont(long idWarrCont) {
		int result = 0;
		String query = "SELECT "+ WarrantTable.NUM +" FROM " + WarrantTable.TABLE_NAME + " WHERE " + 
				WarrantTable.ID_EXTERNAL + " = (SELECT " + WarrContTable.ID_WARRANT + " FROM " + WarrContTable.TABLE_NAME +
				" WHERE " + WarrContTable.ID_EXTERNAL + "="+ idWarrCont + ")";
		result = (int) getTableCount(query);
		if (result == 0) {
			query = "SELECT " + WarrContTable.ID_WARRANT + " FROM " + WarrContTable.TABLE_NAME +
					" WHERE " + WarrContTable.ID_EXTERNAL + "="+ idWarrCont;
			result = (int) getTableCount(query);			
		}
		return result;
	}

	
	/* ****************************************** */
	private WarrUnitRecord findWarrUnitJob(long idWarrCont, int jobId) {
		WarrUnitRecord wuRecord = null;
		if (jobId > 0) {
			SQLiteDatabase sd = getWritableDatabase();
	    	Cursor c = sd.rawQuery("SELECT * FROM " + 
	    			WarrUnitTable.TABLE_NAME + " WHERE " + WarrUnitTable.ID_JOB + " = " + jobId +
	    			" AND " + WarrUnitTable.ID_WARR_CONT + " = " + idWarrCont, null);
			if (c.moveToNext()) {
				wuRecord = new WarrUnitRecord(c);
			}
            if (c != null)
                c.close();
		}
		return wuRecord;
	}

	private Boolean isWarrUnitJobPresent(long idWarrCont, int jobId) {
		boolean result = true;
		if (jobId > 0) {
			SQLiteDatabase sd = getWritableDatabase();
	    	Cursor c = sd.rawQuery("SELECT "+ WarrUnitTable.ID +", "+ WarrUnitTable.REC_STAT +" FROM " + 
	    			WarrUnitTable.TABLE_NAME + " WHERE " + WarrUnitTable.ID_JOB + " = " + jobId +
	    			" AND " + WarrUnitTable.ID_WARR_CONT + " = " + idWarrCont, null);
			result = c.moveToNext();
			if (result) {  // если запись найдена, проверяем не была ли она удалена ранее
				int colid = c.getColumnIndex(WarrUnitTable.REC_STAT);
				int recStat = c.getInt(colid);
				colid = c.getColumnIndex(WarrUnitTable.ID);
				long recId = c.getLong(colid);
				if (recStat == 2) { // если была удалена - восстанавливаем
					ContentValues cv = new ContentValues();
					cv.put(WarrUnitTable.REC_STAT, 1);		
					cv.put(WarrUnitTable.MODIFIED, 1);
					int upd_result = sd.update(WarrUnitTable.TABLE_NAME, cv, WarrUnitTable.ID + "=" + recId, null);				
				}	
			}
            if (c != null)
                c.close();
		}
		return result;
	}

	private long getWarrUnitJob(long idWarrCont, int jobId) {
		long result = 0;
		if (jobId > 0) {
			SQLiteDatabase sd = getWritableDatabase();
	    	Cursor c = sd.rawQuery("SELECT "+ WarrUnitTable.ID +", "+ WarrUnitTable.REC_STAT +" FROM " + 
	    			WarrUnitTable.TABLE_NAME + " WHERE " + WarrUnitTable.ID_JOB + " = " + jobId +
	    			" AND " + WarrUnitTable.ID_WARR_CONT + " = " + idWarrCont, null);
			if (c.moveToNext()) {  // если запись найдена, проверяем не была ли она удалена ранее
				int colid = c.getColumnIndex(WarrUnitTable.REC_STAT);
				int recStat = c.getInt(colid);
				colid = c.getColumnIndex(WarrUnitTable.ID);
				result = c.getLong(colid);
				if (recStat == 2) { // если была удалена - восстанавливаем
					ContentValues cv = new ContentValues();
					cv.put(WarrUnitTable.REC_STAT, 1);		
					cv.put(WarrUnitTable.MODIFIED, 1);
					int upd_result = sd.update(WarrUnitTable.TABLE_NAME, cv, WarrUnitTable.ID + "=" + result, null);				
				}	
			}
            if (c != null)
                c.close();
		}
		return result;
	}

	private long getWarrUnitLocalId(long idExt) {
		String query = "SELECT "+ WarrUnitTable.ID +" FROM " + 
    		WarrUnitTable.TABLE_NAME + " WHERE " + WarrUnitTable.ID_EXTERNAL + " = " + idExt;			
		return getTableCount(query);
	}
	
	public long addWarrUnitItem(int idExt, int idWarrCont, int eqStatus,   
			int idJob, int jobStatus, String rem) {
		long result = getWarrUnitLocalId(idExt);
		if (result == 0) {
			// CREATE A CONTENTVALUE OBJECT		
//			DateFormat dateF = DateFormat.getDateTimeInstance();
			ContentValues cv = new ContentValues();
			cv.put(WarrUnitTable.ID_EXTERNAL, idExt);
			cv.put(WarrUnitTable.ID_WARR_CONT, idWarrCont);
			cv.put(WarrUnitTable.ID_JOB, idJob);
			cv.put(WarrUnitTable.JOB_STATUS, jobStatus);
			cv.put(WarrUnitTable.EQ_STATUS, eqStatus);
			cv.put(WarrUnitTable.REMARK, rem);
			// RETRIEVE WRITEABLE DATABASE AND INSERT
			SQLiteDatabase sd = getWritableDatabase();
			
			result = sd.insert(WarrUnitTable.TABLE_NAME, WarrUnitTable.ID_EXTERNAL, cv);
		} else {
			// update
		}
		if (result < 0) Log.d(TAG, "addWarrUnitItem - Error");
		return result;
	}
	
	private long _addWarrUnitItem(boolean ins, long idExt, long idWarrCont, int idJob, int tblMode, String rem) {
		long result = getWarrUnitLocalId(idExt);
		if (result == 0) {
			result = getWarrUnitJob(idWarrCont, idJob); 
		}
		if (idWarrCont > 0 && idJob > 0) {
			SQLiteDatabase sd = getWritableDatabase();	
			ContentValues cv = new ContentValues();
			cv.put(WarrUnitTable.ID_JOB, idJob);
			cv.put(WarrUnitTable.TBL_MODE, tblMode);
			cv.put(WarrUnitTable.REMARK, rem);
			cv.put(WarrUnitTable.ID_EXTERNAL, idExt);
			cv.put(WarrUnitTable.MODIFIED, 0);
			if (ins || result == 0) {			
				cv.put(WarrUnitTable.ID_WARR_CONT, idWarrCont);
				result = sd.insert(WarrUnitTable.TABLE_NAME, WarrUnitTable.ID_EXTERNAL, cv);
			} else {
				sd.update(WarrUnitTable.TABLE_NAME, cv, WarrUnitTable.ID + "=" + result, null);
			}
		}
		return result;
	}
	
	public long addWarrUnitItem(long idExt, long idWarrCont, int idJob, int tblMode, String rem) {
		long result = 0;
		if (idExt > 0) {
			result = _addWarrUnitItem(false, idExt, idWarrCont, idJob, tblMode, rem);
		} else if (!isWarrUnitJobPresent(idWarrCont, idJob)) { 
			result = _addWarrUnitItem(true, idExt, idWarrCont, idJob, tblMode, rem);			
		}
		return result;
	}
	
	public int delWarrUnitItem(long idExt) {
		SQLiteDatabase sd = getWritableDatabase();
		return sd.delete(WarrUnitTable.TABLE_NAME, WarrUnitTable.ID_EXTERNAL + "=" + idExt, null);
	}
		
	public long getWarrUnitCount() {
		String query = "SELECT COUNT("+ WarrUnitTable.ID +") FROM " + WarrUnitTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public boolean updateWarrUnitRemark(int id, String rem) {
		Boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(WarrUnitTable.REMARK, rem);		
		cv.put(WarrUnitTable.MODIFIED, 1);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(WarrUnitTable.TABLE_NAME, cv, WarrUnitTable.ID + "=" + id, null) == 1;
		return result;		
	}
	
	private boolean removeWarrUnitWarrRepair(int id) {
		boolean result = false;
		SQLiteDatabase sd = getWritableDatabase();
		result = sd.delete(WarrRepairTable.TABLE_NAME, WarrRepairTable.ID_WARR_UNIT + "="+ id + " AND " + 
					WarrRepairTable.ID_EXTERNAL + ZERO_CONDITION, null) > 0;

		ContentValues cv = new ContentValues();
		cv.put(WarrRepairTable.MODIFIED, 1);
		cv.put(WarrRepairTable.REC_STAT, 2);
		result = sd.update(WarrRepairTable.TABLE_NAME, cv, WarrRepairTable.ID_WARR_UNIT + "=" + id +
				" AND "+ WarrRepairTable.ID_EXTERNAL + NOT_ZERO_CONDITION, null) > 0;				
		
		return result;
	}
	
	public boolean removeWarrUnitJobId(int id, long idExt, int idJob) {
		boolean result = false;
		if (idJob > 0) {
			SQLiteDatabase sd = getWritableDatabase();
			if (idExt == 0) {
				result = sd.delete(WarrUnitTable.TABLE_NAME, WarrUnitTable.ID_JOB + "="+ idJob + 
					" AND " + WarrUnitTable.ID + "=" + id, null) > 0;
			} else {  // update rec_stat
//				removeWarrUnitWarrRepair(id);  // пока требуем ручного удаления з/частей
				ContentValues cv = new ContentValues();
				cv.put(WarrUnitTable.MODIFIED, 1);
				cv.put(WarrUnitTable.REC_STAT, 2);
				result = sd.update(WarrUnitTable.TABLE_NAME, cv, WarrUnitTable.ID + "=" + id, null) == 1;				
			} 
		}
		return result;
	}
	
	public boolean setWarrUnitModifiedState(int id) {
		boolean result = false;
		int fModified = (isWarrUnitModified(id)) ? 1 : 0;
		String query = "SELECT "+ WarrUnitTable.ID_WARR_CONT +" FROM " + 
	    			WarrUnitTable.TABLE_NAME + " WHERE " + WarrUnitTable.ID + " = " + id;
		SQLiteDatabase sd = getWritableDatabase();
		if (id > 0) {
			ContentValues cv = new ContentValues();
			cv.put(WarrUnitTable.MODIFIED, fModified);				
			result = sd.update(WarrUnitTable.TABLE_NAME, cv, WarrUnitTable.ID + "=" + id, null) == 1;
		} else
			result = true;
		if (result) {
			Cursor c = sd.rawQuery(query, null);
			if (c.moveToNext()) {
				int colid = c.getColumnIndex(WarrUnitTable.ID_WARR_CONT);
				int idWarrCont = c.getInt(colid);				
				setWarrContModifiedState(idWarrCont);
			}
            if (c != null)
                c.close();
		}
		return result;
	}	

	public boolean isWarrUnitModified(int idWarrUnit) {
		boolean result = false;
		String query = "SELECT "+ WarrRepairTable.ID +" FROM " + WarrRepairTable.TABLE_NAME + " WHERE (" + 
				WarrRepairTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrRepairTable.MODIFIED + " > 0" +
				") AND " + WarrRepairTable.ID_WARR_UNIT + "=" + idWarrUnit;
		result = isRecordPresent(query);
		return result;
	}			

	
	/* ****************************************** */
	private long isWarrRepairIdPresent(long idExt) {
		long result = 0;
		if (idExt > 0) {
			SQLiteDatabase sd = getWritableDatabase();
	    	Cursor c = sd.rawQuery("SELECT "+ WarrRepairTable.ID +" FROM " + 
	    			WarrRepairTable.TABLE_NAME + " WHERE " + WarrRepairTable.ID_EXTERNAL + " = " + idExt, null);
			if (c.moveToNext()) {
				int colid = c.getColumnIndex(WarrRepairTable.ID);
				if (colid >= 0) 
					result = c.getInt(colid);				
			}
            if (c != null)
                c.close();
		}
		return result;
	}

	private double getWarrRepairIdQuantity(long id) {
		double result = 0;
		if (id > 0) {
			SQLiteDatabase sd = getWritableDatabase();
	    	Cursor c = sd.rawQuery("SELECT "+ WarrRepairTable.QUANT + ", "+ WarrRepairTable.REC_STAT +" FROM " + 
	    			WarrRepairTable.TABLE_NAME + " WHERE " + WarrRepairTable.ID + " = " + id, null);
			if (c.moveToNext()) {
				int colid = c.getColumnIndex(WarrRepairTable.REC_STAT);
				int recStat = c.getInt(colid);
				if (recStat == 2) {
					result = 0;
				} else {
					colid = c.getColumnIndex(WarrRepairTable.QUANT);
					if (colid >= 0) 
						result = c.getInt(colid);
				}
			}
            if (c != null)
                c.close();
		}
		return result;
	}
	
	private long isWarrRepairHWPresent(long idWarrUnit, int idRepair) {
		long result = 0;
		if (idRepair > 0) {
			SQLiteDatabase sd = getReadableDatabase();
	    	Cursor c = sd.rawQuery("SELECT "+ WarrRepairTable.ID +" FROM " + 
	    			WarrRepairTable.TABLE_NAME + " WHERE " + WarrRepairTable.ID_REPAIR + " = " + idRepair+
	    			" AND " + WarrRepairTable.ID_WARR_UNIT + " = " + idWarrUnit, null);	    			
			if (c.moveToNext()) {
				int colid = c.getColumnIndex(WarrRepairTable.ID);
				if (colid >= 0) 
					result = c.getInt(colid);				
			}
            if (c != null)
                c.close();
		}
		return result;
	}
	
	public long _addWarrRepairItem(long idExt, long idWarrant, long idWarrUnit, int idRepair, int tblMode,  
			double cost, double quantity) {
		long result = 0;
		ContentValues cv = new ContentValues();
		cv.put(WarrRepairTable.ID_EXTERNAL, idExt);
		cv.put(WarrRepairTable.ID_WARR_UNIT, idWarrUnit);
		cv.put(WarrRepairTable.ID_WARRANT, idWarrant);
		cv.put(WarrRepairTable.ID_REPAIR, idRepair);
		cv.put(WarrRepairTable.TBL_MODE, tblMode);
		cv.put(WarrRepairTable.COST, cost);
		cv.put(WarrRepairTable.QUANT, quantity);
		// RETRIEVE WRITEABLE DATABASE AND INSERT
		SQLiteDatabase sd = getWritableDatabase();
		
		result = sd.insert(WarrRepairTable.TABLE_NAME, WarrRepairTable.ID_EXTERNAL, cv);
		return result;
	}

	public long addWarrRepairItem(long idExt, long idWarrant, long idWarrUnit, int idRepair, int tblMode,  
			double cost, double quantity) {
		long result = 0;
		long warrRepairId = 0;
		if (idExt > 0) { 
			warrRepairId = isWarrRepairIdPresent(idExt);
			if (warrRepairId == 0)
				warrRepairId = isWarrRepairHWPresent(idWarrUnit, idRepair);
		} else
			warrRepairId = isWarrRepairHWPresent(idWarrUnit, idRepair);
		
		if (warrRepairId == 0) {
			result = _addWarrRepairItem(idExt, idWarrant, idWarrUnit, idRepair, tblMode, cost, quantity);
		} else { // update
			ContentValues cv = new ContentValues();
			double preQuant = 0;
			if (idExt == 0) { // добавляем из программы
				preQuant = getWarrRepairIdQuantity(warrRepairId);
				preQuant += quantity;
				cv.put(WarrRepairTable.MODIFIED, 1);
			} else {
				preQuant = quantity;
				cv.put(WarrRepairTable.MODIFIED, 0);
			}
			cv.put(WarrRepairTable.ID_EXTERNAL, idExt);
			cv.put(WarrRepairTable.COST, cost);
			cv.put(WarrRepairTable.QUANT, preQuant);
			cv.put(WarrRepairTable.REC_STAT, 1);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.update(WarrRepairTable.TABLE_NAME, cv, WarrRepairTable.ID + "=" + warrRepairId, null);
		}		
		if (result < 0) Log.d(TAG, "addWarrRepairItem - Error");
		return result;
	}
	
	public int delWarrRepairItem(long idExt) {
		SQLiteDatabase sd = getWritableDatabase();
		return sd.delete(WarrRepairTable.TABLE_NAME, WarrRepairTable.ID_EXTERNAL + "=" + idExt, null);
	}
			
	public long getWarrRepairCount() {
		String query = "SELECT COUNT("+ WarrRepairTable.ID +") FROM " + WarrRepairTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public Boolean removeWarrRepairRepairId(long id, long id_ext, int idGds) {
		Boolean result = false;
		if (id > 0 && idGds > 0) {
			SQLiteDatabase sd = getWritableDatabase();
			if (id_ext == 0) {
				result = sd.delete(WarrRepairTable.TABLE_NAME, WarrRepairTable.ID_REPAIR + "="+ idGds +
						" AND " + WarrRepairTable.ID + "=" + id, null) > 0;
			} else {  // update rec_stat
				ContentValues cv = new ContentValues();
				cv.put(WarrRepairTable.MODIFIED, 1);
				cv.put(WarrRepairTable.REC_STAT, 2);
				result = sd.update(WarrRepairTable.TABLE_NAME, cv, WarrRepairTable.ID + "=" + id, null) == 1;				
			}
		}
		return result;
	}

	
	/* ****************************************** */
	private Boolean isEmpHWIdPresent(int idGds) {
		Boolean result = true;
		if (idGds > 0) {
			String query = "SELECT "+ EmpHardwareTable.ID +" FROM " + 
	    		EmpHardwareTable.TABLE_NAME + " WHERE " + EmpHardwareTable.ID_GDS + " = " + idGds;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addEmpHWItem(int idGds, double cntr) {
		long result = 0;
		if (!isEmpHWIdPresent(idGds)) {
			// CREATE A CONTENTVALUE OBJECT		
			ContentValues cv = new ContentValues();
			cv.put(EmpHardwareTable.ID_GDS, idGds);
			cv.put(EmpHardwareTable.COUNT, cntr);
			// RETRIEVE WRITEABLE DATABASE AND INSERT
			SQLiteDatabase sd = getWritableDatabase();
			
			result = sd.insert(EmpHardwareTable.TABLE_NAME, EmpHardwareTable.ID_GDS, cv);
		}
		if (result < 0) Log.d(TAG, "addEmpHWItem - Error");
		return result;
	}

	/* ****************************************** */
	private Boolean isSealIdPresent(int id) {
		Boolean result = true;
		String query = "SELECT "+ SealTable.ID +" FROM " + 
			SealTable.TABLE_NAME + " WHERE " + SealTable.ID_EXTERNAL + " = " + id;
		result = isRecordPresent(query);
		return result;
	}
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addSealItem(int idExt, String srl, String num, String iNum, String dateCh, String dateInvCh) {
		long result = 0;
		if (!isSealIdPresent(idExt)) {
			// CREATE A CONTENTVALUE OBJECT		
			ContentValues cv = new ContentValues();
			cv.put(SealTable.ID_EXTERNAL, idExt);
			cv.put(SealTable.SRL, srl);
			cv.put(SealTable.NUM, num);
			cv.put(SealTable.INUM, iNum);
			cv.put(SealTable.CHANGE_DATE, dateCh);
			cv.put(SealTable.I_CHANGE_DATE, dateInvCh);
			// RETRIEVE WRITEABLE DATABASE AND INSERT
			SQLiteDatabase sd = getWritableDatabase();
			
			result = sd.insert(SealTable.TABLE_NAME, SealTable.NUM, cv);
		}
		if (result < 0) Log.d(TAG, "addSealItem - Error");
		return result;
	}

	/* ****************************************** */
	private Boolean isSealCertIdPresent(int id) {
		Boolean result = true;
		String query = "SELECT "+ SealCertTable.ID +" FROM " + 
			SealCertTable.TABLE_NAME + " WHERE " + SealCertTable.ID_EXTERNAL + " = " + id;
		result = isRecordPresent(query);
		return result;
	}
	
	private long getSealCertId(long idExt) {
		long result = 0;
		if (idExt > 0) {
			String query = "SELECT "+ SealCertTable.ID +" FROM " + 
				SealCertTable.TABLE_NAME + " WHERE " + SealCertTable.ID_EXTERNAL + " = " + idExt;
			result = getTableCount(query);
		}
		return result;
	}	
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addSealCertItem(long idSpecs, int idReason, int idWarrCont, int fSeal, String defect, 
			String dateCh, String dateBegin, String dateEnd, double ppoMoney) {
		long result = 0;
		// CREATE A CONTENTVALUE OBJECT		
		ContentValues cv = new ContentValues();
//		cv.put(SealCertTable.ID_EXTERNAL, idExt);
		cv.put(SealCertTable.ID_WARR_CONT, idWarrCont);
		cv.put(SealCertTable.ID_SPECS, idSpecs);
		cv.put(SealCertTable.ID_REASON, idReason);
		cv.put(SealCertTable.FACTORY_SEAL, fSeal);
		cv.put(SealCertTable.CHANGE_DATE, dateCh);
		cv.put(SealCertTable.DEFECT, defect);
		cv.put(SealCertTable.BEGIN_DATE, dateBegin);
		cv.put(SealCertTable.END_DATE, dateEnd);
		cv.put(SealCertTable.PPO_MONEY, ppoMoney);
//		cv.put(SealCertTable.MODIFIED, dateInvCh);
//		cv.put(SealCertTable.ID_EMP, dateInvCh);
//		cv.put(SealCertTable.ID_OWNER, dateInvCh);
		// RETRIEVE WRITEABLE DATABASE AND INSERT
		SQLiteDatabase sd = getWritableDatabase();
		result = sd.insert(SealCertTable.TABLE_NAME, SealCertTable.ID_WARR_CONT, cv);
		if (result < 0) Log.d(TAG, "addSealCertItem - Error");
		return result;
	}
	
	public long updateSealCertItem(long id, int idReason, int idWarrCont, int fSeal, String defect, 
			String dateCh, String dateBegin, String dateEnd, double ppoMoney) {
		long result = 0;
		// CREATE A CONTENTVALUE OBJECT		
		ContentValues cv = new ContentValues();
//		cv.put(SealCertTable.ID_EXTERNAL, idExt);
		cv.put(SealCertTable.ID_WARR_CONT, idWarrCont);
		cv.put(SealCertTable.ID_REASON, idReason);
		cv.put(SealCertTable.FACTORY_SEAL, fSeal);
		cv.put(SealCertTable.CHANGE_DATE, dateCh);
		cv.put(SealCertTable.DEFECT, defect);
		cv.put(SealCertTable.BEGIN_DATE, dateBegin);
		cv.put(SealCertTable.END_DATE, dateEnd);
		cv.put(SealCertTable.PPO_MONEY, ppoMoney);
//		cv.put(SealCertTable.MODIFIED, dateInvCh);
//		cv.put(SealCertTable.ID_EMP, dateInvCh);
//		cv.put(SealCertTable.ID_OWNER, dateInvCh);
		// RETRIEVE WRITEABLE DATABASE AND INSERT
		SQLiteDatabase sd = getWritableDatabase();

		sd.update(SealCertTable.TABLE_NAME, cv, SealCertTable.ID + "=" + id, null);
		result = id;
		if (result < 0) Log.d(TAG, "addSealCertItem - Error");
		return result;
	}
	
	public boolean updateSealCertModified(long id, int modified) {
		boolean res = false;
		ContentValues cv = new ContentValues();
		cv.put(SealCertTable.MODIFIED, modified);
		SQLiteDatabase sd = getWritableDatabase();
		res = sd.update(SealCertTable.TABLE_NAME, cv, SealCertTable.ID + "=" + id, null) > 0;
		return res;
	}
	
	public boolean updateSealCertIdExt(long id, long idExt) {
		boolean res = false;
		ContentValues cv = new ContentValues();
		cv.put(SealCertTable.ID_EXTERNAL, idExt);
		cv.put(SealCertTable.MODIFIED, 0);
		SQLiteDatabase sd = getWritableDatabase();
		res = sd.update(SealCertTable.TABLE_NAME, cv, SealCertTable.ID + "=" + id, null) > 0;
		return res;
	}
	
	public boolean isWarrContInSealCert(long idWarrCont) {
		String query = "SELECT " + SealCertTable.ID +" FROM " + SealCertTable.TABLE_NAME +
				" WHERE " + SealCertTable.ID_WARR_CONT + " = " + idWarrCont;
		return isRecordPresent(query);
	}
	
	public int getSealCertSealsCount(long id) {
		String query = "SELECT COUNT("+ SealCertSealsTable.ID +") FROM " + SealCertSealsTable.TABLE_NAME +
				" WHERE " + SealCertSealsTable.ID_SEAL_CERT + " = " + id;
		if (id > 0) return (int) getTableCount(query);		
		else return 0;
	}
	
	public boolean removeSealCert(long id) {
		Boolean result = false;
		try {
			if (id > 0) {
				if (removeAllSealCertSeals(id)) {
					SQLiteDatabase sd = getWritableDatabase();
					result = sd.delete(SealCertTable.TABLE_NAME, SealCertTable.ID + " = " + id, null) > 0;
				}
			}
		} catch (Exception e) {
			if (D) Log.e(TAG, "removeSealCert exception = " + e.getMessage());
		}
		return result;
	}
	
	public Boolean removeAllSealCertSeals(long id) {
		Boolean result = false;
		try {
			if (id > 0) {
				SQLiteDatabase sd = getWritableDatabase();
				result = sd.delete(SealCertSealsTable.TABLE_NAME, SealCertSealsTable.ID_SEAL_CERT + " = " + id, null) >= 0;
			}
		} catch (Exception e) {
			if (D) Log.e(TAG, "removeAllSealCertSeals exception = " + e.getMessage());
		}
		return result;
	}
	

	/* ****************************************** */
	private Boolean isSealInCertPresent(int idSeal) {
		Boolean result = true;
		String query = "SELECT "+ SealCertSealsTable.ID +" FROM " + 
				SealCertSealsTable.TABLE_NAME + " WHERE " + SealCertSealsTable.ID_SEAL_EXTERNAL + " = " + idSeal;
		result = isRecordPresent(query);
		return result;
	}
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addSealInSealCert(int idSealCert, int idSeal, String info) {
		long result = 0;
		if (!isSealInCertPresent(idSeal)) {
			ContentValues cv = new ContentValues();
			cv.put(SealCertSealsTable.ID_SEAL_EXTERNAL, idSeal);
			cv.put(SealCertSealsTable.ID_SEAL_CERT, idSealCert);
			cv.put(SealCertSealsTable.SEAL_INFO, info);
			cv.put(SealCertSealsTable.MODIFIED, 1);
			SQLiteDatabase sd = getWritableDatabase();			
			result = sd.insert(SealCertSealsTable.TABLE_NAME, SealCertSealsTable.ID_SEAL_CERT, cv);
		}
		if (result < 0) Log.d(TAG, "addSealCertSealsItem - Error");
		return result;
	}

	public Boolean removeSealCertSeal(long id) {
		Boolean result = false;
		try {
			if (id > 0) {
				SQLiteDatabase sd = getWritableDatabase();
				result = sd.delete(SealCertSealsTable.TABLE_NAME, SealCertSealsTable.ID_SEAL_EXTERNAL + " = " + id, null) >= 0;
			}
		} catch (Exception e) {
			if (D) Log.e(TAG, "removeSealCertSeals exception = " + e.getMessage());
		}
		return result;
	}
	
    public String getSealCertSeals(long sealCertId) {
    	String res = "";
    	SQLiteDatabase sd = getWritableDatabase();
    	Cursor c = null; 
    	String query;   	
    	query = "SELECT * FROM " + SealCertSealsTable.TABLE_NAME + 
    			" WHERE " + SealCertSealsTable.ID_SEAL_CERT + " = " + sealCertId;
		try {
			SealCertSealsRecord sealRec;
			c = sd.rawQuery(query, null);
			int cntr = 0;
	    	while (c.moveToNext()) {    		
	    		sealRec = new SealCertSealsRecord(c);
	    		cntr++;
	    		if (cntr == 1)
	    			res += sealRec.toString();
	    		else
	    			res += "; " + sealRec.toString();
	    	}    
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
	    	if (c != null) c.close();
		}
		return res;
    }
	
	
	
	/* ****************************************** */
	public String getModifiedWarrants() {
		String result = "";
		Cursor cursorWarr = null;
		Cursor cursorWCont = null;
		Cursor cursorWUnit = null;
		Cursor cursorWRepair = null;
		WarrantRecord warr;
		WarrContRecord warrCont;
		WarrUnitRecord warrUnit;
		WarrRepairRecord warrRepair;
		String queryWarr = "SELECT * FROM " + WarrantTable.TABLE_NAME + " WHERE " + WarrantTable.MODIFIED + " > 0";
		String queryWCont = "SELECT * FROM " + WarrContTable.TABLE_NAME + " WHERE (" + 
				WarrContTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrContTable.MODIFIED + " > 0) AND " +
				WarrContTable.ID_WARRANT + " = ";
		String queryWUnit = "SELECT * FROM " + WarrUnitTable.TABLE_NAME + " WHERE (" + 
				WarrUnitTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrUnitTable.MODIFIED + " > 0) AND " +
				WarrUnitTable.ID_WARR_CONT + " = ";
		String queryWRepair = "SELECT * FROM " + WarrRepairTable.TABLE_NAME + " WHERE (" + 
				WarrRepairTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrRepairTable.MODIFIED + " > 0) AND "+
				WarrRepairTable.ID_WARR_UNIT + " = "; 

		SQLiteDatabase sd = getWritableDatabase();		
		JSONArray warrants = new JSONArray();		
		try {						
			cursorWarr = sd.rawQuery(queryWarr, null);
			while (cursorWarr.moveToNext()) {	    		
	    		warr = new WarrantRecord(cursorWarr);
	    		warr.jobMarkStarted = getEmpWorkLastMark(warr.id_external) == 0;
	    		warr.jobMarkModified = getEmpWorkLastMarkModified(warr.id_external) > 0;
	    		if (/*!warr.jobMarkModified && !warr.jobMarkStarted*/
	    				true
	    				/*!(warr.localState == WarrantRecord.W_DONE && warr.to_sign > 0 && warr.id_user_sign == 0)*/) {
	    			JSONObject jsonWarrObj = new JSONObject();
					jsonWarrObj.put("id", warr.id_external);
					jsonWarrObj.put("id_state", warr.id_state);
					jsonWarrObj.put("report", (warr.reported)?1:0);
					JSONArray warrConts = new JSONArray();
					cursorWCont = sd.rawQuery(queryWCont + warr.id_external, null);
			    	while (cursorWCont.moveToNext()) {
			    		JSONObject jsonWContObj = new JSONObject();		    		
			    		warrCont = new WarrContRecord(cursorWCont);
						jsonWContObj.put("id", warrCont.id_external);
						//jsonWContObj.put("id_warr", warrCont.id_warrant);
						jsonWContObj.put("eq_spec", warrCont.eq_spec);
						jsonWContObj.put("id_eq", warrCont.id_eq);
						jsonWContObj.put("eq_s", warrCont.eq_status);
						jsonWContObj.put("id_job", warrCont.id_job);
						jsonWContObj.put("job_s", warrCont.job_status);
						jsonWContObj.put("rec_stat", warrCont.rec_stat);
						jsonWContObj.put("tbl", warrCont.tbl_mode);
						jsonWContObj.put("rem", warrCont.remark);
			    	    		
						JSONArray warrUnits = new JSONArray();	    		
			    		cursorWUnit = sd.rawQuery(queryWUnit + warrCont.id, null);
			    		while (cursorWUnit.moveToNext()) {	    			
			    			warrUnit = new WarrUnitRecord(cursorWUnit);
			    			JSONObject jsonWUnitObj = new JSONObject();
		    				jsonWUnitObj.put("id", warrUnit.id_external);
		    				jsonWUnitObj.put("id_job", warrUnit.id_job);
		    				jsonWUnitObj.put("tbl", warrUnit.tbl_mode);
		    				jsonWUnitObj.put("rec_stat", warrUnit.rec_stat);
		    				jsonWUnitObj.put("rem", warrUnit.remark);
			    			
		    				JSONArray warrRepairs = new JSONArray();	    			
			    			cursorWRepair = sd.rawQuery(queryWRepair + warrUnit.id, null);
			    			while (cursorWRepair.moveToNext()) {
			    				warrRepair = new WarrRepairRecord(cursorWRepair);
			    				JSONObject jsonWRepairObj = new JSONObject();
			    				jsonWRepairObj.put("id", warrRepair.id_external);
			    				jsonWRepairObj.put("id_repair", warrRepair.id_repair);
			    				jsonWRepairObj.put("tbl", warrRepair.tbl_mode);
			    				jsonWRepairObj.put("quant", warrRepair.quant);
			    				jsonWRepairObj.put("rec_stat", warrRepair.rec_stat);
			    				warrRepairs.put(jsonWRepairObj);
			    			}	    			
			    			jsonWUnitObj.put("warr_repair", warrRepairs);
			    			warrUnits.put(jsonWUnitObj);
			    		}
			    		jsonWContObj.put("warr_unit", warrUnits);
			    		warrConts.put(jsonWContObj);
			    	}
			    	jsonWarrObj.put("warr_cont", warrConts);
			    	warrants.put(jsonWarrObj);
	    		}
			}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (warrants.length() > 0) {
        		result = warrants.toString();
        	} else {
        		result = "";
        	}
            if (cursorWarr != null)
                cursorWarr.close();
            if (cursorWCont != null)
                cursorWCont.close();
            if (cursorWUnit != null)
                cursorWUnit.close();
            if (cursorWRepair != null)
                cursorWRepair.close();
        }
		return result;
	}

	public boolean clearModifiedWarrants() {  // for debug
		boolean result = false;
		Cursor cursorWCont = null;
		Cursor cursorWUnit = null;
		Cursor cursorWRepair = null;
		WarrContRecord warrCont;
		WarrUnitRecord warrUnit;
		WarrRepairRecord warrRepair;
		String queryWCont = "SELECT * FROM " + WarrContTable.TABLE_NAME + " WHERE " + 
				WarrContTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrContTable.MODIFIED + " > 0";
		String queryWUnit = "SELECT "+ WarrUnitTable.ID +" FROM " + WarrUnitTable.TABLE_NAME + " WHERE (" + 
				WarrUnitTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrUnitTable.MODIFIED + " > 0) AND " +
				WarrUnitTable.ID_WARR_CONT + " = ";
		String queryWRepair = "SELECT "+ WarrRepairTable.ID +" FROM " + WarrRepairTable.TABLE_NAME + " WHERE (" + 
				WarrRepairTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + WarrRepairTable.MODIFIED + " > 0) AND "+
				WarrRepairTable.ID_WARR_UNIT + " = "; 

		SQLiteDatabase sd = getWritableDatabase();
		
		cursorWCont = sd.rawQuery(queryWCont, null);
    	while (cursorWCont.moveToNext()) {
    		warrCont = new WarrContRecord(cursorWCont);

    		setWarrContModifiedState(warrCont.id_external);
    	}
        if (cursorWCont != null)
            cursorWCont.close();
        if (cursorWUnit != null)
            cursorWUnit.close();
        if (cursorWRepair != null)
            cursorWRepair.close();
		return result;
	}
	
	public boolean clearModifiedMsgs() {  
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(MessagesTable.MODIFIED, 0);
		SQLiteDatabase sd = getWritableDatabase();	
		result = sd.update(MessagesTable.TABLE_NAME, cv, MessagesTable.MODIFIED + "> 0", null) > 0;
//		result = result || sd.delete(MessagesTable.TABLE_NAME, MessagesTable.ID_EXTERNAL + ZERO_CONDITION, null) > 0;
		return result;
	}
	
	public String getModifiedMsgs() {
		String result = "", photo;
		Cursor cursorMsgs = null;
		MessagesRecord msg;
		String queryMsgs = "SELECT * FROM " + MessagesTable.TABLE_NAME + " WHERE " + 
				MessagesTable.ID_EXTERNAL + ZERO_CONDITION + " OR " + MessagesTable.MODIFIED + " > 0";

		SQLiteDatabase sd = getWritableDatabase();		
		JSONArray warrants = new JSONArray();		
		try {						
			cursorMsgs = sd.rawQuery(queryMsgs, null);
	    	while (cursorMsgs.moveToNext()) {
	    		JSONObject jsonMsgObj = new JSONObject();
	    		msg = new MessagesRecord(cursorMsgs);
	    		photo = "";
				jsonMsgObj.put("id_loc", msg.id);
				jsonMsgObj.put("id", msg.id_external);
				jsonMsgObj.put("id_sender", msg.id_sender);
				jsonMsgObj.put("id_recip", msg.id_recipient);
				jsonMsgObj.put("id_state", msg.id_state);
				jsonMsgObj.put("status", msg.status);
				jsonMsgObj.put("rec_stat", msg.rec_stat);
	    		if (msg.id_external > 0) {
					jsonMsgObj.put("subj", "");
					jsonMsgObj.put("msg", "");
	    		} else {
					jsonMsgObj.put("subj", msg.subj);
					jsonMsgObj.put("msg", msg.msg);
					if (msg.attachment != null) {
						photo = Base64.encodeToString(msg.attachment, Base64.DEFAULT);
					}
	    		}
	    		jsonMsgObj.put("photo", photo);
	    		jsonMsgObj.put("a_nm", msg.a_nm);
	    		jsonMsgObj.put("a_sz", msg.a_size);
				warrants.put(jsonMsgObj);
	    	}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (warrants.length() > 0) {
        		result = warrants.toString();
        	} else {
        		result = "";
        	}
            if (cursorMsgs != null)
                cursorMsgs.close();
        }
		return result;
	}

	public String getEmpWorkTimeRecords() {
		String result = "";
		Cursor cursorWorkTime = null;
		EmpWorkTimeRecord workTime;
		String markDate;
		String floatStr;
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//		String queryMsgs = "SELECT * FROM " + EmpWorkTimeTable.TABLE_NAME + 
//				" WHERE " + EmpWorkTimeTable.MODIFIED + "= 1"+//NOT_ZERO_CONDITION +
//				" ORDER BY " + EmpWorkTimeTable.DTBE;
		
		String queryMsgs = "SELECT * FROM " + EmpWorkTimeTable.TABLE_NAME + 
				" WHERE " + EmpWorkTimeTable.ID_EXTERNAL + ZERO_CONDITION +
				" ORDER BY " + EmpWorkTimeTable.DTBE;		
		
		//checkEmpWorkClosedIntervals(); // не закрывать интервалы
		SQLiteDatabase sd = getWritableDatabase();		
		JSONArray warrants = new JSONArray();		
		try {						
			cursorWorkTime = sd.rawQuery(queryMsgs, null);
	    	while (cursorWorkTime.moveToNext()) {
	    		JSONObject jsonMsgObj = new JSONObject();
	    		workTime = new EmpWorkTimeRecord(cursorWorkTime);
	    		jsonMsgObj.put("id", workTime.id);
				jsonMsgObj.put("id_warr", workTime.id_warrant);
				jsonMsgObj.put("id_ent", workTime.id_ent);
				jsonMsgObj.put("be", workTime.be);
				markDate = sDateFormat.format(workTime.dtbe);
				jsonMsgObj.put("dtbe", markDate);
				
				floatStr = Double.toString(workTime.lat);
				floatStr = floatStr.replace(".", ",");
				jsonMsgObj.put("lat", floatStr);
				floatStr = Double.toString(workTime.lng);
				floatStr = floatStr.replace(".", ",");
				jsonMsgObj.put("lng", floatStr);
				
				warrants.put(jsonMsgObj);
	    	}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (warrants.length() > 0) {
        		result = warrants.toString();
        		setEmpWorkMarksModified(2, 1);// помечаем как передающиеся
        	} else {
        		setEmpWorkMarksModified(0, 1);// помечаем как переданные, т.к. ид уже установлен
        		result = "";
        	}
            if (cursorWorkTime != null)
                cursorWorkTime.close();
        }
		return result;
	}
	public String getEmpLocationRecords() {
		String result = "";
		Cursor cursorLocation = null;
		LocationRecord location;
		String markDate;
		String floatStr;
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String queryMsgs = "SELECT * FROM " + LocationTable.TABLE_NAME + 
				" WHERE " + LocationTable.MODIFIED + NOT_ZERO_CONDITION +
				" ORDER BY " + LocationTable.L_DATE;

		SQLiteDatabase sd = getWritableDatabase();		
		JSONArray warrants = new JSONArray();		
		try {						
			cursorLocation = sd.rawQuery(queryMsgs, null);
	    	while (cursorLocation.moveToNext()) {
	    		JSONObject jsonMsgObj = new JSONObject();
	    		location = new LocationRecord(cursorLocation);
				floatStr = Double.toString(location.lat);
				floatStr = floatStr.replace(".", ",");
				jsonMsgObj.put("lat", floatStr /*location.lat*/);
				floatStr = Double.toString(location.lng);
				floatStr = floatStr.replace(".", ",");
				jsonMsgObj.put("lng", floatStr /*location.lng*/);
				markDate = sDateFormat.format(location.l_date);
				jsonMsgObj.put("l_date", markDate);
				warrants.put(jsonMsgObj);
	    	}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (warrants.length() > 0) {
        		result = warrants.toString();
        	} else {
        		result = "";
        	}
            if (cursorLocation != null)
                cursorLocation.close();
        }
		return result;
	}
	
	public String getSpecsInvRecords() {
		String result = "";
		Cursor cursorSpecs = null;
		SpecsRecord spec;
		String idStr, invStr, numStr;
		String querySpecs = "SELECT * FROM " + SpecsTable.TABLE_NAME + 
				" WHERE " + SpecsTable.MODIFIED + NOT_ZERO_CONDITION+ " OR " + SpecsTable.MODIFIED + " > 0";;

		SQLiteDatabase sd = getWritableDatabase();		
		JSONArray warrants = new JSONArray();		
		try {						
			cursorSpecs = sd.rawQuery(querySpecs, null);
	    	while (cursorSpecs.moveToNext()) {
	    		JSONObject jsonMsgObj = new JSONObject();
	    		spec = new SpecsRecord(cursorSpecs);
				idStr = Long.toString(spec.id_external);
				jsonMsgObj.put("id", idStr);
				invStr = spec.inv;
				jsonMsgObj.put("inv", invStr);
				if (spec.numCash >= 0)
					numStr = "" + spec.numCash;
				else
					numStr = "";
				jsonMsgObj.put("num_cash", numStr);
				jsonMsgObj.put("ip_cash", spec.ipCash);
				jsonMsgObj.put("ip_mdm", spec.ipModem);
				jsonMsgObj.put("f_seal", spec.factorySeal);
				warrants.put(jsonMsgObj);
	    	}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (warrants.length() > 0) {
        		result = warrants.toString();
        	} else {
        		result = "";
        	}
            if (cursorSpecs != null)
                cursorSpecs.close();
        }
		return result;		
	}
	
	/* ****************************************** */
	public String getReady2SendSealCerts() {
		String result = "";
		Cursor cursorSealCert = null;
		Cursor cursorSeals = null;
		String floatStr;
		SealCertRecord sealCert;
		SealCertSealsRecord seal;
		String querySealCert = "SELECT * FROM " + SealCertTable.TABLE_NAME + " WHERE " + SealCertTable.MODIFIED + 
				" = " + SealCertListActivity.SEAL_CERT_READY_TO_SEND;
		String querySeals = "SELECT * FROM " + SealCertSealsTable.TABLE_NAME + " WHERE " + 
				SealCertSealsTable.ID_SEAL_CERT + " = ";

		SQLiteDatabase sd = getWritableDatabase();		
		JSONArray sealCertArray = new JSONArray();		
		try {						
			cursorSealCert = sd.rawQuery(querySealCert, null);
			while (cursorSealCert.moveToNext()) {	    		
	    		sealCert = new SealCertRecord(cursorSealCert);
    			JSONObject jsonSealCertObj = new JSONObject();
    			jsonSealCertObj.put("id", sealCert.id);
				jsonSealCertObj.put("idReason", sealCert.idReason);
				jsonSealCertObj.put("id_specs", sealCert.idSpecs);
				jsonSealCertObj.put("id_wc", sealCert.idWarrCont);
				jsonSealCertObj.put("dt_c", sealCert.dateChange);
				jsonSealCertObj.put("dt_b", sealCert.dateBegin);
				jsonSealCertObj.put("dt_e", sealCert.dateEnd);
				jsonSealCertObj.put("defect", sealCert.defect);
				
//				floatStr = Double.toString(sealCert.ppoMoney);
//				floatStr = floatStr.replace(".", ",");
//				jsonSealCertObj.put("money", floatStr /*sealCert.ppoMoney*/);
				jsonSealCertObj.put("money", sealCert.ppoMoney);
				
				jsonSealCertObj.put("fSeal", sealCert.factorySeal);
				JSONArray sealsArray = new JSONArray();
				cursorSeals = sd.rawQuery(querySeals + sealCert.id, null);
		    	while (cursorSeals.moveToNext()) {
		    		JSONObject jsonSealCertSealObj = new JSONObject();		    		
		    		seal = new SealCertSealsRecord(cursorSeals);
					jsonSealCertSealObj.put("id", seal.idSeals);
		    		sealsArray.put(jsonSealCertSealObj);
		    	}
		    	jsonSealCertObj.put("seals", sealsArray);
		    	sealCertArray.put(jsonSealCertObj);
			}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (sealCertArray.length() > 0) {
        		result = sealCertArray.toString();
        	} else {
        		result = "";
        	}
            if (cursorSealCert != null)
                cursorSealCert.close();
            if (cursorSeals != null)
                cursorSeals.close();
        }
		return result;
	}

	
	
}
