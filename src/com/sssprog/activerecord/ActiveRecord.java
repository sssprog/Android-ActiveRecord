package com.sssprog.activerecord;

import java.io.Serializable;
import java.util.List;

import com.sssprog.activerecord.helpers.ARUtils;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Base class for models. Implements Serializable, so models can be put in to Bundle
 */
public class ActiveRecord implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long _id = 0;
	
	public long getID() {
		return _id;
	}
	
	static <T extends ActiveRecord> T toModel(Class<T> modelClass, Cursor cursor) {
		try {
			T res = modelClass.newInstance();
			res.loadDataFromCursor(cursor);
			return res;
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Can't instantiate " + modelClass.getName());
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Can't instantiate " + modelClass.getName());
		}
	}
	
	void loadDataFromCursor(Cursor cursor) {
		List<FieldInfo> fields = Database.dbInfo.fieldsInfo.get(getClass());
		for (FieldInfo field: fields) {
			Object value = null;
			int columnIndex = cursor.getColumnIndex(field.sqlName);
			if (cursor.isNull(columnIndex))
				value = null;
			else {
				switch (field.fieldType) {
				case ByteField:
					value = cursor.getInt(columnIndex);
					break;
					
				case ShortField:
					value = cursor.getShort(columnIndex);
					break;
					
				case IntField:
					value = cursor.getInt(columnIndex);
					break;
					
				case LongField:
					value = cursor.getLong(columnIndex);
					break;
					
				case FloatField:
					value = cursor.getFloat(columnIndex);
					break;
					
				case DoubleField:
					value = cursor.getDouble(columnIndex);
					break;
					
				case BooleanField:
					value = cursor.getInt(columnIndex) != 0;
					break;
					
				case CharField:
					value = cursor.getString(columnIndex).charAt(0);
					break;
					
				case StringField:
					value = cursor.getString(columnIndex);
					break;
				}
			}
			try {
				field.field.set(this, value);
			} catch (IllegalArgumentException e) {
				ARLog.w(e.toString());
			} catch (IllegalAccessException e) {
				ARLog.w(e.toString());
			}
		}
	}
	
	void loadToValues(ContentValues values) {
		List<FieldInfo> fields = Database.dbInfo.fieldsInfo.get(getClass());
		for (FieldInfo field: fields) {
			if (field.sqlName.equals("_id"))
				continue;
			Object value = null;
			String name = field.sqlName;
			try {
				value = field.field.get(this);
			} catch (IllegalArgumentException e) {
				ARLog.w(e.toString());
			} catch (IllegalAccessException e) {
				ARLog.w(e.toString());
			}
			if (value == null)
				values.putNull(name);
			else {
				switch (field.fieldType) {
				case ByteField:
					values.put(name, (Integer) value);
					break;
					
				case ShortField:
					values.put(name, (Short) value);
					break;
					
				case IntField:
					values.put(name, (Integer) value);
					break;
					
				case LongField:
					values.put(name, (Long) value);
					break;
					
				case FloatField:
					values.put(name, (Float) value);
					break;
					
				case DoubleField:
					values.put(name, (Double) value);
					break;
					
				case BooleanField:
					values.put(name, (Boolean) value);
					break;
					
				case CharField:
					values.put(name, value.toString());
					break;
					
				case StringField:
					values.put(name, (String) value);
					break;
					
				}
			}
		}
	}
	
	ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		loadToValues(values);
		return values;
	}
	
	public void save() {
		if (_id > 0) {
			Database.update(ARUtils.getTableName(getClass()), toContentValues(), "_id = " + _id, null);
		} else {
			_id = Database.insert(ARUtils.getTableName(getClass()), null, toContentValues());
		}
	}
	
	public void delete() {
		if (_id > 0)
			Database.delete(ARUtils.getTableName(getClass()), "_id = " + _id, null);
	}
	
	/**
	 * Returns true if two objects represent the same row in DB
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ActiveRecord) || _id <= 0)
			return false;
		ActiveRecord a = (ActiveRecord) obj;
		if (a.getClass() != getClass())
			return false;
		return a._id > 0 && a._id == _id;
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		List<FieldInfo> fields = Database.dbInfo.fieldsInfo.get(getClass());
		res.append("{");
		for (FieldInfo field: fields) {
			Object value = null;
			try {
				value = field.field.get(this);
			} catch (IllegalArgumentException e) {
				ARLog.w(e.toString());
			} catch (IllegalAccessException e) {
				ARLog.w(e.toString());
			}
			res.append(field.field.getName()).append(": ").append(value).append(", ");
		}
		if (!fields.isEmpty())
			res.delete(res.length() - 2, res.length());
		res.append("}");
		return res.toString();
	}

}
