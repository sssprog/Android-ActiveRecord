package com.sssprog.activerecord;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.support.v4.util.LruCache;

/**
 * Caches items inflated from cursor in getCurrentRowAsModel() method according
 * to specified cache strategy
 */
public class CacheableActiveRecordCursor<Table extends ActiveRecord> extends ActiveRecordCursor<Table> {
	
	private Map<Long, Table> mCache;
	private LruCache<Long, Table> mLruCache;
	
	CacheableActiveRecordCursor(Class<Table> tableClass, Cursor cursor, int maxItemsNumberInCache) {
		super(tableClass, cursor);
		if (maxItemsNumberInCache > 0)
			mLruCache = new LruCache<Long, Table>(maxItemsNumberInCache);
		else
			mCache = new HashMap<Long, Table>();
	}

	/**
	 * @param tableClass
	 * @param cursor
	 * @param maxItemsNumberInCache if 0 then all items will be cached
	 */
	public CacheableActiveRecordCursor(Class<Table> tableClass, ActiveRecordCursor<Table> cursor, int maxItemsNumberInCache) {
		this(tableClass, cursor.getCursor(), maxItemsNumberInCache);
	}
	
	@Override
	public Table getCurrentRowAsModel() {
		long id = getCurrentRowID();
		Table res;
		if (mLruCache != null) {
			res = mLruCache.get(id);
			if (res == null) {
				res = super.getCurrentRowAsModel();
				mLruCache.put(id, res);
			} else {
//				ARLog.i("from cache " + id);
			}
		} else {
			res = mCache.get(id);
			if (res == null) {
				res = super.getCurrentRowAsModel();
				mCache.put(id, res);
			} else {
//				ARLog.i("from cache " + id);
			}
		}
		return res;
	}

}
