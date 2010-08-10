package com.mba.freewifi;

import android.content.Context;

public class Stats {
	private static final String TAG = "freewifi-stats";
	private static final String FLURRY_CODE = "T643X9KF7YX1DC1HNZY3L6WW92124T";
	public static void start(Context c) {
//		try {
//			FlurryAgent.onStartSession(c, FLURRY_CODE);
//		} catch (Throwable t) {
//			Log.e(TAG, "", t);
//		}
	}
	public static void event(String s) {
//		try {
//			FlurryAgent.onEvent(s);
//		} catch (Throwable t) {
//			Log.e(TAG, "", t);
//		}
	}
	public static void end(Context c) {
//		try {
//			FlurryAgent.onEndSession(c);
//		} catch (Throwable t) {
//			Log.e(TAG, "", t);
//		}
	}
}
