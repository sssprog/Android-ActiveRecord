package com.sssprog.activerecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import com.sssprog.activerecord.helpers.ARUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseOpenHelper extends SQLiteOpenHelper {

	private DatabaseInfo mInfo;
	
	public DatabaseOpenHelper(Context context, DatabaseInfo dbInfo) {
		super(context, dbInfo.config.name, null, dbInfo.config.version);
		mInfo = dbInfo;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db, false);
		updateIndexes(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		deleteOldTables(db);
		createTables(db, true);
		updateFields(db);
		updateIndexes(db);
	}
	
	private void createTables(SQLiteDatabase db, boolean ifNotExists) {
		for (Class<? extends ActiveRecord> table: mInfo.config.getModelClasses()) {
			String sql = getCreateTableStatement(table, ifNotExists);
			ARLog.d(sql);
			// Don't catch exceptions here, letting user of the library to see exceptions
			db.execSQL(sql);
		}
	}
	
	private String getCreateTableStatement(Class<? extends ActiveRecord> table, boolean addIfNotExists) {
		StringBuilder res = new StringBuilder();
		res.append("CREATE TABLE ");
		if (addIfNotExists)
			res.append("IF NOT EXISTS ");
		res.append(ARUtils.toSqlName(table.getSimpleName()))
			.append('(');
		List<FieldInfo> fields = mInfo.fieldsInfo.get(table);
		for (FieldInfo field: fields) {
			if (field.field.getName().equals("_id"))
				res.append(field.sqlName).append(" INTEGER PRIMARY KEY AUTOINCREMENT").append(", ");
			else
				res.append(field.sqlName).append(' ').append(DatabaseInfo.sSqlTypes.get(field.field.getType()).sqlType).append(", ");
		}
		// Remove last comma
		if (!fields.isEmpty())
			res.delete(res.length() - 2, res.length());
		res.append(")");
		
		return res.toString();
	}
	
	private void deleteOldTables(SQLiteDatabase db) {
//		Cursor c = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table';", null);
//		while (c.moveToNext()) {
//			String name = c.getString(c.getColumnIndex("name"));
//			if (!mInfo.containsTable(name)) {
//				String sql = "DROP TABLE " + name;
//				db.execSQL(sql);
//				AALog.d(sql);
//			}
//		}
	}
	
	private void updateFields(SQLiteDatabase db) {
		for (Entry<Class<? extends ActiveRecord>, List<FieldInfo>> entry : mInfo.fieldsInfo.entrySet()) {
			Class<? extends ActiveRecord> table = entry.getKey();
			String tableName = ARUtils.toSqlName(table.getSimpleName());
			// Find new fields
			Cursor c = db.rawQuery("PRAGMA table_info(" + tableName + ");", null);
			ArrayList<FieldInfo> fields = new ArrayList<FieldInfo>(entry.getValue());
			while (c.moveToNext()) {
				String fieldName = c.getString(c.getColumnIndex("name"));
				for (Iterator<FieldInfo> i = fields.iterator(); i.hasNext(); ) {
					FieldInfo f = i.next();
					if (f.sqlName.equals(fieldName)) {
						i.remove();
						break;
					}
				}
			}
			
			// Add new fields
			for (FieldInfo field: fields) {
				String sql = String.format(Locale.US, "ALTER TABLE %s ADD COLUMN %s %s", tableName, field.sqlName, 
						DatabaseInfo.sSqlTypes.get(field.field.getType()).sqlType);
				db.execSQL(sql);
				ARLog.d(sql);
			}
		}
	}
	
	private void updateIndexes(SQLiteDatabase db) {
		// Delete not existing indexes, and find new ones
		List<DatabaseIndex> list = new ArrayList<DatabaseIndex>(mInfo.config.getIndexes());
		Cursor c = db.rawQuery("SELECT * FROM sqlite_master WHERE type='index';", null);
		while (c.moveToNext()) {
			String name = c.getString(c.getColumnIndex("name"));
			boolean exists = false;
			for (Iterator<DatabaseIndex> i = list.iterator(); i.hasNext(); ) {
				DatabaseIndex index = i.next();
				if (name.equals(index.indexName)) {
					exists = true;
					i.remove();
					break;
				}
			}
			if (!exists) {
				String sql = "DROP INDEX " + name;
				db.execSQL(sql);
				ARLog.d(sql);
			}
		}
		// Add new indexes
		for (DatabaseIndex index: list) {
			String fields = "";
			for (String f: index.columns)
				fields += ", " + ARUtils.toSqlName(f);
			fields = fields.substring(2);
			String sql = String.format(Locale.US, "CREATE INDEX %s ON %s (%s)", 
					index.indexName, ARUtils.toSqlName(index.modelClass.getSimpleName()), fields);
			db.execSQL(sql);
			ARLog.d(sql);
		}
	}

}
