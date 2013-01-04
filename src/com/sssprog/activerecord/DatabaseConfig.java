package com.sssprog.activerecord;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConfig {
	public int version;
	public String name;
	private List<Class<? extends ActiveRecord>> mClasses = new ArrayList<Class<? extends ActiveRecord>>();
	private List<DatabaseIndex> mIndexes = new ArrayList<DatabaseIndex>();
	
	public void addModelClass(Class<? extends ActiveRecord> modelClass) {
		mClasses.add(modelClass);
	}
	
	List<Class<? extends ActiveRecord>> getModelClasses() {
		return mClasses;
	}
	
	public void addIndex(DatabaseIndex index) {
		mIndexes.add(index);
	}
	
	List<DatabaseIndex> getIndexes() {
		return mIndexes;
	}
}
