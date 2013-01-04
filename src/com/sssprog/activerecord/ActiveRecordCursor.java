package com.sssprog.activerecord;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

public class ActiveRecordCursor<Table extends ActiveRecord> {
	
	private Cursor mCursor;
	private Class<Table> mTableClass;
	
	ActiveRecordCursor(Class<Table> tableClass, Cursor cursor) {
		mCursor = cursor;
		mTableClass = tableClass;
	}
	
	public int getCount() {
		return mCursor.getCount();
	}
	
	public boolean moveToFirst() {
		return mCursor.moveToFirst();
	}
	
	public boolean moveToLast() {
		return mCursor.moveToLast();
	}
	
	public boolean moveToNext() {
		return mCursor.moveToNext();
	}
	
	public boolean moveToPrevious() {
		return mCursor.moveToPrevious();
	}
	
	public boolean moveToPosition(int position) {
		return mCursor.moveToPosition(position);
	}
	
	public void close() {
		mCursor.close();
	}
	
	public boolean isClosed() {
		return mCursor.isClosed();
	}
	
	public boolean isBeforeFirst() {
		return mCursor.isBeforeFirst();
	}
	
	public boolean isAfterLast() {
		return mCursor.isAfterLast();
	}
	
	public Cursor getCursor() {
		return mCursor;
	}
	
	public Table getCurrentRowAsModel() {
		checkCursor();
		return ActiveRecord.toModel(mTableClass, mCursor);
	}
	
	public long getCurrentRowID() {
		checkCursor();
		return mCursor.getLong(mCursor.getColumnIndex("_id"));
	}
	
	private void checkCursor() {
		if (mCursor.isClosed())
			throw new IllegalStateException("cursor is closed");
		if (mCursor.isBeforeFirst())
			throw new IllegalStateException("cursor is before first row");
		if (mCursor.isAfterLast())
			throw new IllegalStateException("cursor is after last row");
	}
	
	public List<Table> getAll() {
		if (mCursor.isClosed())
			throw new IllegalStateException("cursor is closed");
		List<Table> res = new ArrayList<Table>();
		if (mCursor.moveToFirst()) {
			do {
				res.add(ActiveRecord.toModel(mTableClass, mCursor));
			} while (mCursor.moveToNext());
		}
		mCursor.close();
		return res;
	}

}
