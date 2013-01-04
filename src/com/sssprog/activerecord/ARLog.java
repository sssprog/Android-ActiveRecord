package com.sssprog.activerecord;

import android.util.Log;

public class ARLog {
	
	private static final String DEFAULT_TAG = "ActiveRecord";
	
	public static void i(String tag, String message) {
		if (Database.debugMode)
			Log.i(tag, message);
	}

	public static void d(String tag, String message) {
		if (Database.debugMode)
			Log.d(tag, message);
	}

	public static void w(String tag, String message) {
		if (Database.debugMode)
			Log.w(tag, message);
	}

	public static void e(String tag, String message) {
		if (Database.debugMode)
			Log.e(tag, message);
	}
	
	public static void i( String message) {
		if (Database.debugMode)
			Log.i(DEFAULT_TAG, message);
	}

	public static void d( String message) {
		if (Database.debugMode)
			Log.d(DEFAULT_TAG, message);
	}

	public static void w(String message) {
		if (Database.debugMode)
			Log.w(DEFAULT_TAG, message);
	}

	public static void e(String message) {
		if (Database.debugMode)
			Log.e(DEFAULT_TAG, message);
	}

}
