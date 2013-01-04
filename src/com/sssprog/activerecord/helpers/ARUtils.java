package com.sssprog.activerecord.helpers;

import com.sssprog.activerecord.ActiveRecord;

public class ARUtils {
	
	public static String toSqlName(String javaName) {
		return javaName;
	}
	
	public static String getTableName(Class<? extends ActiveRecord> tableClass) {
		return toSqlName(tableClass.getSimpleName());
	}
	
}
