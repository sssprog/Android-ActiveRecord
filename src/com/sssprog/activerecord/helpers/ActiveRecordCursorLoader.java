package com.sssprog.activerecord.helpers;

import com.sssprog.activerecord.ARLog;
import com.sssprog.activerecord.ActiveRecord;
import com.sssprog.activerecord.ActiveRecordCursor;
import com.sssprog.activerecord.CacheableActiveRecordCursor;
import com.sssprog.activerecord.Database;

import android.content.Context;
import android.database.ContentObserver;
import android.support.v4.content.AsyncTaskLoader;

public class ActiveRecordCursorLoader<T extends ActiveRecord> extends AsyncTaskLoader<ActiveRecordCursor<T>> {

	private boolean distinct;
	private Class<T> tableClass;
	private String[] columns;
	private String selection;
	private String[] selectionArgs;
	private String groupBy;
	private String having;
	private String orderBy;
	private String limit;
	private ActiveRecordCursor<T> mCursor;
	private String sql;
	
	private boolean mUseCacheableCursor;
	private int mMaxItemsNumberInCache;
	
	private final ForceLoadContentObserver mObserver;

	public ActiveRecordCursorLoader(Context context, boolean distinct, Class<T> tableClass, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit) {
		super(context);
		mObserver = new ForceLoadContentObserver();
		this.distinct = distinct;
		this.tableClass = tableClass;
		this.columns = columns;
		this.selection = selection;
		this.selectionArgs = selectionArgs;
		this.groupBy = groupBy;
		this.having = having;
		this.orderBy = orderBy;
		this.limit = limit;
	}
	
	public ActiveRecordCursorLoader(Context context, Class<T> tableClass, String sql, String[] selectionArgs) {
		super(context);
		mObserver = new ForceLoadContentObserver();
		this.tableClass = tableClass;
		this.sql = sql;
		this.selectionArgs = selectionArgs;
	}
	
	public void setUseCacheableCursor(boolean userCacheable) {
		mUseCacheableCursor = userCacheable;
	}
	
	public void setMaxItemsNumberInCache(int maxItemsNumberInCache) {
		mMaxItemsNumberInCache = maxItemsNumberInCache;
	}
	
	void registerContentObserver(ActiveRecordCursor<T> cursor, ContentObserver observer) {
        cursor.getCursor().registerContentObserver(mObserver);
    }

	@Override
	public ActiveRecordCursor<T> loadInBackground() {
		while (!Database.isOpen()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				ARLog.w(e.toString());
			}
		}
		ActiveRecordCursor<T> cursor;
		if (sql != null)
			cursor = Database.rawQuery(tableClass, sql, selectionArgs);
		else
			cursor = Database.query(distinct, tableClass, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		if (mUseCacheableCursor) {
			cursor = new CacheableActiveRecordCursor<T>(tableClass, cursor, mMaxItemsNumberInCache);
		}
		// Ensure the cursor window is filled
		cursor.getCount();
		registerContentObserver(cursor, mObserver);
		return cursor;
	}

	@Override
	public void deliverResult(ActiveRecordCursor<T> cursor) {
		if (isReset()) {
			// An async query came in while the loader is stopped
			if (cursor != null) {
				cursor.close();
			}
			return;
		}
		ActiveRecordCursor<T> oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted()) {
			super.deliverResult(cursor);
		}

		if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
			oldCursor.close();
		}
	}

	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			deliverResult(mCursor);
		}
		if (takeContentChanged() || mCursor == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	public void onCanceled(ActiveRecordCursor<T> cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		mCursor = null;
	}

}
