package com.sssprog.activerecord;

import java.lang.reflect.Field;

import com.sssprog.activerecord.DatabaseInfo.FieldType;

class FieldInfo {

	public final Field field;
	public final String sqlName;
	public final FieldType fieldType;
	
	public FieldInfo(Field field, String sqlName, FieldType fieldType) {
		super();
		this.field = field;
		this.sqlName = sqlName;
		this.fieldType = fieldType;
	}
	
}
