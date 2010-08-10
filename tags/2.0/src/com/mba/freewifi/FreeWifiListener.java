package com.mba.freewifi;

import java.util.List;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class FreeWifiListener extends BroadcastReceiver {
	
	private static int ID = R.drawable.icon;
	private static final String KEY_USEOTHER = "usother";
	
	@Override
	public void onReceive(final Context context, Intent intent) {

		boolean cancel = true;
		
		NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		if (ni!=null && ni.getState()==State.CONNECTED) {
			WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wi = wm.getConnectionInfo();
			
			SharedPreferences sp = context.getSharedPreferences(FreeWifiConnect.KEY_PREFS, Context.MODE_PRIVATE);
			String useOther = sp.getString(KEY_USEOTHER, null);
			boolean prefer = sp.getBoolean(FreeWifiConnect.KEY_PREFER, true);
			
			if ("freewifi".equalsIgnoreCase(wi.getSSID())) {
				System.err.println("************* CONNECTE A FREEWIFI. prefer="+prefer+", useOther="+useOther);
				int otherFound = -1;
				if (useOther==null && prefer) {
					List<WifiConfiguration> configured = wm.getConfiguredNetworks();				
					for (ScanResult sc : wm.getScanResults()) {
						if (!"freewifi".equalsIgnoreCase(sc.SSID)) {
							for (WifiConfiguration wc : configured) {
								String otherSSID = wc.SSID;
								if (otherSSID.startsWith("\"") && otherSSID.length()>3) {
									otherSSID=otherSSID.substring(1, otherSSID.length()-1);
								}
								System.err.println("************* "+sc.SSID+" / "+otherSSID);
								if (otherSSID.equalsIgnoreCase(sc.SSID)) {
									otherFound=wc.networkId;
								}
								if (otherFound>-1) { break; }
							}									
						}
						if (otherFound>-1) { break; }
					}
				}
				System.err.println("*********** OTHERFOUND = "+otherFound);
				if (otherFound>-1) {
					wm.disconnect();
					wm.enableNetwork(otherFound, true);
					Editor e = sp.edit();
					e.putString(KEY_USEOTHER, "true");
					e.commit();
				} else {
					System.err.println("************************************** OK ...");
					Intent svcintent = new Intent(context, FreeWifiService.class);
					PendingIntent pendingIntent = PendingIntent.getService(context, 0, svcintent, 0);
					AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
					String delaiStr = sp.getString(FreeWifiConnect.KEY_DELAI, "0");
					int delai = 0;
					try {
						delai = Integer.parseInt(delaiStr);
					} catch (Throwable t) {
					}
					alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ((delai<3?3:delai) * 1000), pendingIntent);
					cancel=false;
				}
			}
			if (useOther!=null) {
				System.err.println("************************************** REACTIVATION");
				//wm.enableNetwork(Integer.parseInt(useOther), false);
				Editor e = sp.edit();
				e.remove(KEY_USEOTHER);
				e.commit();
				// On reactive les autres réseaux
				for (WifiConfiguration wc : wm.getConfiguredNetworks()) {
					wm.enableNetwork(wc.networkId, false);
				}
			}				
		}
		
		if (cancel) {
			// Enlever la notification
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(ID);
		}
	}


}
