package com.utis.warrants;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.utis.warrants.tables.LocationTable;
import com.utis.warrants.tables.LogsTable;

public class WarrantsContentProvider extends ContentProvider {
	private static final String cUri = "com.utis.warrants.dbprovider";
	public static final Uri CONTENT_LOG_URI = Uri.parse("content://"+ cUri +"/logs");
	public static final Uri CONTENT_LOCATION_URI = Uri.parse("content://"+ cUri +"/locations");
	// Create the constants used to differentiate between 
	// the different URI requests.
	private static final int ALLROWS_LOG = 1;
	private static final int SINGLE_ROW_LOG = 2;
	private static final int ALLROWS_LOCATION = 3;
	private static final int SINGLE_ROW_LOCATION = 4;

	private static final UriMatcher uriMatcher;
	// Populate the UriMatcher object, where a URI ending 
	// in 'elements' will correspond to a request for all
	// items, and 'elements/[rowID]' represents a single row.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(cUri, "logs", ALLROWS_LOG);
		uriMatcher.addURI(cUri, "logs/#", SINGLE_ROW_LOG);
		uriMatcher.addURI(cUri, "locations", ALLROWS_LOCATION);
		uriMatcher.addURI(cUri, "locations/#", SINGLE_ROW_LOCATION);
	}	
	// The index (key) column name for use in where clauses.
	public static final String KEY_ID = "_id";
	// The name and column index of each column in your database.
	// These should be descriptive.
	public static final String LOG_LEVEL_COLUMN = LogsTable.LEVEL;
	public static final String LOG_DATE_COLUMN = LogsTable.MSG_DATE;
	public static final String LOG_MSG_COLUMN = LogsTable.MSG;
	public static final String LOG_MODIF_COLUMN = LogsTable.MODIFIED;

	public static final String LOCATION_LAT_COLUMN = LocationTable.LAT;
	public static final String LOCATION_LNG_COLUMN = LocationTable.LNG;
	public static final String LOCATION_DATE_COLUMN = LocationTable.L_DATE;
	public static final String LOCATION_MODIF_COLUMN = LocationTable.MODIFIED;
		
	// TODO: Create public field for each column in your table.
	// SQLite Open Helper variable
	private DBSchemaHelper dbSch;
	
	@Override
	public boolean onCreate() {
		// Construct the underlying database.
		// Defer opening the database until you need to perform
		// a query or transaction.
		dbSch = DBSchemaHelper.getInstance(getContext());		
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		// Return a string that identifies the MIME type
		// for a Content Provider URI
		switch (uriMatcher.match(uri)) {
			case ALLROWS_LOG: 
				return "vnd.android.cursor.dir/vnd.utis.log";
			case SINGLE_ROW_LOG: 
				return "vnd.android.cursor.item/vnd.utis.log";
			case ALLROWS_LOCATION: 
				return "vnd.android.cursor.dir/vnd.utis.location";
			case SINGLE_ROW_LOCATION: 
				return "vnd.android.cursor.item/vnd.utis.location";
			default: 
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Open the database.
		SQLiteDatabase db = dbSch.getWritableDatabase();
		// Replace these with valid SQL statements if necessary.
		String groupBy = null;
		String having = null;
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		String tblName = getTable(uri);
		queryBuilder.setTables(tblName);
		
		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
			case SINGLE_ROW_LOG :
			case SINGLE_ROW_LOCATION:
				String rowID = uri.getPathSegments().get(1);
				queryBuilder.appendWhere(KEY_ID + "=" + rowID);
			default: break;
		}
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, groupBy, having, sortOrder);
		return cursor;	
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Open a read / write database to support the transaction.
		SQLiteDatabase db = dbSch.getWritableDatabase();
		String tblName = getTable(uri);

		// If this is a row URI, limit the deletion to the specified row.
		switch (uriMatcher.match(uri)) {
			case SINGLE_ROW_LOG : 
			case SINGLE_ROW_LOCATION:
				String rowID = uri.getPathSegments().get(1);
				selection = KEY_ID + "=" + rowID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			default: break;
		}
		// To return the number of deleted items, you must specify a where
		// clause. To delete all rows and return a value, pass in '1'.
		if (selection == null)
			selection = "1";
		// Execute the deletion.
		int deleteCount = db.delete(tblName, selection, selectionArgs);
		// Notify any observers of the change in the data set.
		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Open a read / write database to support the transaction.
		SQLiteDatabase db = dbSch.getWritableDatabase();
		String tableName = "";
		Uri currentUri = CONTENT_LOG_URI;
		switch (uriMatcher.match(uri)) {
			case SINGLE_ROW_LOG:
			case ALLROWS_LOG:
				tableName = LogsTable.TABLE_NAME;
				currentUri = CONTENT_LOG_URI;
				break;
			case SINGLE_ROW_LOCATION:
			case ALLROWS_LOCATION:
				tableName = LocationTable.TABLE_NAME;
				currentUri = CONTENT_LOCATION_URI;
				break;
		}
		
		// To add empty rows to your database by passing in an empty 
		// Content Values object, you must use the null column hack 
		// parameter to specify the name of the column that can be 
		// set to null.
		String nullColumnHack = null;
		// Insert the values into the table
		long id = db.insert(tableName, nullColumnHack, values);
		if (id > -1) {
			// Construct and return the URI of the newly inserted row.
			Uri insertedId = ContentUris.withAppendedId(currentUri, id);
			// Notify any observers of the change in the data set.
			getContext().getContentResolver().notifyChange(insertedId, null);
			return insertedId;
		} else
			return null;		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// Open a read / write database to support the transaction.
		SQLiteDatabase db = dbSch.getWritableDatabase();
		String tblName = getTable(uri);
		
		// If this is a row URI, limit the deletion to the specified row.
		switch (uriMatcher.match(uri)) {
			case SINGLE_ROW_LOG : 
			case SINGLE_ROW_LOCATION:
				String rowID = uri.getPathSegments().get(1);
				selection = KEY_ID + "=" + rowID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			default: break;
		}
		// Perform the update.
		int updateCount = db.update(tblName, values, selection, selectionArgs);
		// Notify any observers of the change in the data set.
		getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;	
	}

	private String getTable(Uri uri) {
		String tblName = "";
		switch (uriMatcher.match(uri)) {
			case SINGLE_ROW_LOG:
			case ALLROWS_LOG:
				tblName = LogsTable.TABLE_NAME;
				break;
			case SINGLE_ROW_LOCATION:
			case ALLROWS_LOCATION:
				tblName = LocationTable.TABLE_NAME;
				break;
		}
		return tblName;
	}
}
