package com.sssprog.activerecord;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.sssprog.activerecord.helpers.ARUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class Database {

	/**
	 * Set to true to enable logging
	 */
	public static volatile boolean debugMode = false;
	
	private static volatile SQLiteDatabase mDatabase;
	private static volatile DatabaseOpenHelper mDbHelper;
	private static volatile Context mContext;
	static volatile DatabaseConfig dbConfig;
	static volatile DatabaseInfo dbInfo;
	

	private Database(){}
	
	public static void initialize(Context context, DatabaseConfig config) {
		if (context == null)
			throw new IllegalArgumentException("context can't be null");
		if (config == null)
			throw new IllegalArgumentException("config can't be null");
		mContext = context;
		dbConfig = config;
		dbInfo = new DatabaseInfo(config);
		mDbHelper = new DatabaseOpenHelper(mContext, dbInfo);
	}
	
	/**
	 * Opens writable SQLite database
	 */
	public static void open() {
		if (mDatabase == null || !mDatabase.isOpen()) {
			mDatabase = mDbHelper.getWritableDatabase();
		}
	}

	/**
	 * Closes database, if it previously was opened
	 */
	public static void close() {
		if (mDatabase != null && !mDatabase.isOpen()) {
			mDatabase.close();
		}
	}

	public static boolean isOpen() {
		if (mDatabase != null && mDatabase.isOpen())
			return true;
		else
			return false;
	}
	
	public static void beginTransaction() {
		checkDbIsOpened();
		mDatabase.beginTransaction();
	}
	
	public static void endTransaction() {
		checkDbIsOpened();
		mDatabase.endTransaction();
	}
	
	public static void setTransactionSuccessful() {
		checkDbIsOpened();
		mDatabase.setTransactionSuccessful();
	}
	
	public static boolean inTransaction() {
		checkDbIsOpened();
		return mDatabase.inTransaction();
	}
	
	private static void checkDbIsOpened() {
		if (mDatabase == null || !mDatabase.isOpen())
			throw new IllegalStateException("database isn't opened");
	}
	
	/**
	 * 
	 * @param table
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return the number of rows affected
	 */
	public static int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		checkDbIsOpened();
		return mDatabase.update(table, values, whereClause, whereArgs);
	}
	
	/**
	 * 
	 * @param table
	 * @param nullColumnHack
	 * @param values
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public static long insert(String table, String nullColumnHack, ContentValues values) {
		checkDbIsOpened();
		return mDatabase.insert(table, nullColumnHack, values);
	}
	
	/**
	 * 
	 * @param table
	 * @param whereClause
	 * @param whereArgs
	 * @return the number of rows affected if a whereClause is passed in, 0
	 *         otherwise. To remove all rows and get a count pass "1" as the
	 *         whereClause.
	 */
	public static int delete(String table, String whereClause, String[] whereArgs) {
		return mDatabase.delete(table, whereClause, whereArgs);
	}

	public static Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		checkDbIsOpened();
		return mDatabase.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}
	
	public static Cursor rawQuery(String sql, String[] selectionArgs) {
		checkDbIsOpened();
		return mDatabase.rawQuery(sql, selectionArgs);
	}
	
	public static <Table extends ActiveRecord> ActiveRecordCursor<Table> rawQuery(Class<Table> tableClass, String sql, String[] selectionArgs) {
		Cursor cursor = rawQuery(sql, selectionArgs);
		return new ActiveRecordCursor<Table>(tableClass, cursor);
	}
	
	public static <Table extends ActiveRecord> int delete(Class<Table> tableClass, String whereClause, String[] whereArgs) {
		return delete(ARUtils.getTableName(tableClass), whereClause, whereArgs);
	}
	
	public static <Table extends ActiveRecord> ActiveRecordCursor<Table> query(
			boolean distinct, Class<Table> tableClass, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit) {
		checkDbIsOpened();
		Cursor cursor = query(distinct, ARUtils.getTableName(tableClass),
				columns, selection, selectionArgs, groupBy, having, orderBy,
				limit);
		return new ActiveRecordCursor<Table>(tableClass, cursor);
	}
	
	public static <Table extends ActiveRecord> ActiveRecordCursor<Table> findAll(Class<Table> tableClass) {
		return query(false, tableClass, null, null, null, null, null, null, null);
	}
	
	public static <Table extends ActiveRecord> ActiveRecordCursor<Table> findByIds(Class<Table> tableClass, Collection<Long> ids) {
		return query(false, tableClass, null, "_id IN (" + TextUtils.join(",", ids) + ")", null, null, null, null, null);
	}
	
	/**
	 * @param tableClass
	 * @param id
	 * @return record with specified id, or null if there is no such record in DB
	 */
	public static <Table extends ActiveRecord> Table findById(Class<Table> tableClass, long id) {
		List<Table> list = findByIds(tableClass, Arrays.asList(id)).getAll();
		if (list.isEmpty())
			return null;
		else
			return list.get(0);
	}
	
	public static <Table extends ActiveRecord> ActiveRecordCursor<Table> findByColumnValue(Class<Table> tableClass, String columnName, String value) {
		return query(false, tableClass, null, columnName + "=?", new String[] {value}, null, null, null, null);
	}


}
