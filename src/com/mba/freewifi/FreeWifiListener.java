package com.mba.freewifi;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class FreeWifiListener extends BroadcastReceiver {
	
	private static int ID = R.drawable.icon;

	@Override
	public void onReceive(final Context context, Intent intent) {

		System.err.println("************************************** APPEL ONRECEIVE");
		boolean cancel = true;
		
		NetworkInfo ni = (NetworkInfo)(NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		System.err.println("************************************** "+ni+" / "+(ni==null?"":ni.getState()));
		if (ni!=null && ni.getState()==State.CONNECTED) {
			WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wi = wm.getConnectionInfo();
			System.err.println("************************************** SSID="+wi.getSSID());
			
			if ("freewifi".equalsIgnoreCase(wi.getSSID())) {
				System.err.println("************************************** OK ...");
				Intent svcintent = new Intent(context, FreeWifiService.class);
				PendingIntent pendingIntent = PendingIntent.getService(context, 0, svcintent, 0);
				AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (3 * 1000), pendingIntent);
				cancel=false;
			}
		}
		
		if (cancel) {
			// Enlever la notification
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(ID);
		}
	}


}
