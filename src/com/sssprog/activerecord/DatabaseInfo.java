package com.sssprog.activerecord;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sssprog.activerecord.FieldInfo;
import com.sssprog.activerecord.helpers.ARUtils;

class DatabaseInfo {
	
	static enum FieldType {
		ByteField,
		ShortField,
		IntField,
		LongField,
		FloatField,
		DoubleField,
		CharField,
		BooleanField,
		StringField
	}
	
	static class FieldTypeInfo {
		public final String sqlType;
		public final FieldType fieldType;
		
		public FieldTypeInfo(String sqlType, FieldType fieldType) {
			super();
			this.sqlType = sqlType;
			this.fieldType = fieldType;
		}
		
	}
	
	final DatabaseConfig config;
	final Map<Class<? extends ActiveRecord>, List<FieldInfo>> fieldsInfo;
	static final Map<Class<?>, FieldTypeInfo> sSqlTypes = new HashMap<Class<?>, FieldTypeInfo>();
	static {
		sSqlTypes.put(byte.class, new FieldTypeInfo("INTEGER", FieldType.ByteField));
		sSqlTypes.put(short.class, new FieldTypeInfo("INTEGER", FieldType.ShortField));
		sSqlTypes.put(int.class, new FieldTypeInfo("INTEGER", FieldType.IntField));
		sSqlTypes.put(long.class, new FieldTypeInfo("INTEGER", FieldType.LongField));
		sSqlTypes.put(float.class, new FieldTypeInfo("REAL", FieldType.FloatField));
		sSqlTypes.put(double.class, new FieldTypeInfo("REAL", FieldType.DoubleField));
		sSqlTypes.put(char.class, new FieldTypeInfo("TEXT", FieldType.CharField));
		sSqlTypes.put(boolean.class, new FieldTypeInfo("INTEGER", FieldType.BooleanField));
		sSqlTypes.put(Byte.class, new FieldTypeInfo("INTEGER", FieldType.ByteField));
		sSqlTypes.put(Short.class, new FieldTypeInfo("INTEGER", FieldType.ShortField));
		sSqlTypes.put(Integer.class, new FieldTypeInfo("INTEGER", FieldType.IntField));
		sSqlTypes.put(Long.class, new FieldTypeInfo("INTEGER", FieldType.LongField));
		sSqlTypes.put(Float.class, new FieldTypeInfo("REAL", FieldType.FloatField));
		sSqlTypes.put(Double.class, new FieldTypeInfo("REAL", FieldType.DoubleField));
		sSqlTypes.put(Boolean.class, new FieldTypeInfo("INTEGER", FieldType.BooleanField));
		sSqlTypes.put(String.class, new FieldTypeInfo("TEXT", FieldType.StringField));
	}
	
	DatabaseInfo(DatabaseConfig config) {
		this.config = config;
		fieldsInfo = getFieldsInfo();
	}
	
	private Map<Class<? extends ActiveRecord>, List<FieldInfo>> getFieldsInfo() {
		Map<Class<? extends ActiveRecord>, List<FieldInfo>> res = new  HashMap<Class<? extends ActiveRecord>, List<FieldInfo>>();
		for (Class<? extends ActiveRecord> table: config.getModelClasses()) {
			res.put(table, getTableInfo(table));
		}
		return res;
	}
	
	private List<FieldInfo> getTableInfo(Class<? extends ActiveRecord> table) {
		// Going through all super classes to collect all fields with any visibility
		Class<?> cls = table;
		List<FieldInfo> res = new ArrayList<FieldInfo>();
		do {
			for (Field field: cls.getDeclaredFields()) {
				if (!field.getName().startsWith("t_") && fieldWithRightModifiers(field)) {
					FieldTypeInfo st = sSqlTypes.get(field.getType());
					if (st == null) {
						throw new IllegalArgumentException("Model " + table.getSimpleName() + 
								" contains field '" + field.getName() + "' with unsupported type '" + field.getType() + "'" + 
								"\nMake field name to start with 't_' to not persist it in DB");
					}
					FieldInfo i = new FieldInfo(field, ARUtils.toSqlName(field.getName()), st.fieldType);
					field.setAccessible(true);
					res.add(i);
				}
			}
			cls = cls.getSuperclass();
		} while (ActiveRecord.class.isAssignableFrom(cls));
		return res;
	}
	
	private boolean fieldWithRightModifiers(Field field) {
		int m = field.getModifiers();
		return !Modifier.isFinal(m) &&
				!Modifier.isStatic(m);
	}
	
//	boolean containsTable(String tableName) {
//		for (Class<? extends ActiveRecord> table: config.getModelClasses()) {
//			if (tableName.equals(AAUtils.toSqlName(table.getSimpleName())))
//				return true;
//		}
//		return false;
//	}
	
//	boolean containsIndex(String indexName) {
//		for (DatabaseIndex index: config.getIndexes()) {
//			if (indexName.equals(index.indexName))
//				return true;
//		}
//		return false;
//	}

}
