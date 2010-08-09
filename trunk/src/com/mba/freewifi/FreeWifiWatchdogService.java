package com.mba.freewifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

public class FreeWifiWatchdogService extends Service {
	
	public static final long WATCHDOG_DELAY = 120*1000;
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		watch();
		stopSelf();
	}
		
	public void watch() {
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wi = wm.getConnectionInfo();
		if ("freewifi".equalsIgnoreCase(wi.getSSID())) {
			// Prolonger
			SharedPreferences sp = getSharedPreferences(FreeWifiConnect.KEY_PREFS, Context.MODE_PRIVATE);
			final String user = sp.getString(FreeWifiConnect.KEY_USER, null);
			final String password = sp.getString(FreeWifiConnect.KEY_PASSWORD, null);
			if (user==null || user.trim().length()==0 || password==null || password.trim().length()==0) {
				return;
			}
			
			try {
				System.err.println("****************** RECONNECT");
				FreeWifiUtils.getInstance().doConnect(user, password);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// re-declencher le watchdog
			Intent svcintent = new Intent(this, FreeWifiWatchdogService.class);
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, svcintent, 0);
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + WATCHDOG_DELAY, pendingIntent);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
