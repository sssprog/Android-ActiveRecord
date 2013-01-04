package com.sssprog.activerecord.helpers;

import com.sssprog.activerecord.ActiveRecord;
import com.sssprog.activerecord.ActiveRecordCursor;

import android.widget.BaseAdapter;

public abstract class ActiveRecordCursorAdapter<T extends ActiveRecord> extends BaseAdapter {
	
	private ActiveRecordCursor<T> mCursor;
	
	public ActiveRecordCursorAdapter(ActiveRecordCursor<T> cursor) {
		mCursor = cursor;
	}

	@Override
	public int getCount() {
		return mCursor == null ? 0 : mCursor.getCount();
	}

	@Override
	public T getItem(int position) {
		mCursor.moveToPosition(position);
		return mCursor.getCurrentRowAsModel();
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getID();
	}
	
	public ActiveRecordCursor<T> swapCursor(ActiveRecordCursor<T> cursor) {
		ActiveRecordCursor<T> old = mCursor;
		mCursor = cursor;
		notifyDataSetChanged();
		return old;
	}
	
}
