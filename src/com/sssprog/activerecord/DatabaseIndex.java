package com.sssprog.activerecord;

import java.util.Arrays;
import java.util.List;

import com.sssprog.activerecord.helpers.ARUtils;

public class DatabaseIndex {
	public final Class<? extends ActiveRecord> modelClass;
	public final String indexName;
	public final List<String> columns;
	
	public DatabaseIndex(Class<? extends ActiveRecord> modelClass, String indexName, List<String> columns) {
		super();
		this.modelClass = modelClass;
		this.indexName = indexName != null ? indexName : getDefaultName(modelClass, columns);
		this.columns = columns;
	}
	
	public DatabaseIndex(Class<? extends ActiveRecord> modelClass, String indexName, String... columns) {
		this(modelClass, indexName, Arrays.asList(columns));
	}
	
	private static String getDefaultName(Class<? extends ActiveRecord> modelClass, List<String> columns) {
		String res = "IDX_" + ARUtils.toSqlName(modelClass.getSimpleName());
		for (String c: columns) {
			res += "_" + ARUtils.toSqlName(c);
		}
		return res;
	}
}