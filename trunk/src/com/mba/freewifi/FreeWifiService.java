package com.mba.freewifi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;

public class FreeWifiService extends Service {
	
	private static int ID = R.drawable.icon;
	private int bindCount = 0;
	
	@Override
	public void onStart(Intent intent, int startId) {
		// Cette methode est appelee uniquement lors de startService() et pas lors de bindService().
		super.onStart(intent, startId);
		doconnect(true);
		if (bindCount==0) {
			stopSelf();
		}
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		bindCount--;
		if (bindCount==0) {
			stopSelf();
		}
		return super.onUnbind(intent);
	}
	
	public void doconnect(boolean force) {

		setForeground(true);
		try {
			Stats.start(this);
			SharedPreferences sp = getSharedPreferences(FreeWifiConnect.KEY_PREFS, Context.MODE_PRIVATE);
			boolean autoconnect = sp.getBoolean(FreeWifiConnect.KEY_ONOFF, true);
			if (!autoconnect && !force) {
				// Dommage
				return;
			}
			final boolean notify = sp.getBoolean(FreeWifiConnect.KEY_NOTIF, true);
			final Intent notificationIntent = notify?new Intent(FreeWifiService.this, FreeWifiConnect.class):null;
			final PendingIntent contentIntent = notify?PendingIntent.getActivity(FreeWifiService.this, 0, notificationIntent, 0):null;
			final NotificationManager mNotificationManager = notify?(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE):null;
			
			final String user = sp.getString(FreeWifiConnect.KEY_USER, null);
			final String password = sp.getString(FreeWifiConnect.KEY_PASSWORD, null);
			if (user==null || user.trim().length()==0 || password==null || password.trim().length()==0) {
				if (notify) {
					Notification notification = new Notification(R.drawable.icon, "Réseau FreeWifi à proximité", System.currentTimeMillis());
					notification.setLatestEventInfo(FreeWifiService.this, "FreeWifi", "Mais vous n'avez pas renseigné vos identifiants !", contentIntent);
					
					mNotificationManager.notify(ID, notification);
				}
				return;
			}
			
			int maxtries = Integer.parseInt(sp.getString(FreeWifiConnect.KEY_MAXTRIES, "3"));
			
			boolean retry = true;
			int code = -1;
			int essais= 1;
			for (; essais<=maxtries && code!=FreeWifiUtils.CONNECT_OK && retry; essais++) {
				int icon = R.drawable.icon;
				try {
					if (notify) {
						Notification notification = new Notification(icon, "Identification FreeWifi en cours ...", System.currentTimeMillis());
						notification.setLatestEventInfo(FreeWifiService.this, "FreeWifi", "Identification FreeWifi en cours ...", contentIntent);
						mNotificationManager.notify(ID, notification);
					}
					code = FreeWifiUtils.getInstance().doConnect(user, password);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				if (notify) {
					String msg1 = null, msg2 = null;
					if (code==FreeWifiUtils.CONNECT_OK) {
						msg1 = "Identification FreeWifi Ok.";
						msg2 = "Vous avez été correctement identifié.";
					} else {
						msg1 = "Identification FreeWifi en erreur ("+essais+"/"+maxtries+") ...";
						icon = R.drawable.iconerror;
						if (essais<3) {
							msg2 = "Tentative de connection FreeWifi numéro "+essais+"/"+maxtries+" en erreur. Nouvel essai.";			
							try { Thread.sleep(4000); } catch (Throwable t) { t.printStackTrace(); }
						} else {
							if (code==FreeWifiUtils.CONNECT_ERROR_TIMEOUT) {
								msg2="Essayez une connection manuelle depuis le menu de freewifi-connect.";
							} else {
								msg2 = "Problème d'identifiant ?";
							}
						}
					}
					Notification notification = new Notification(icon, msg1, System.currentTimeMillis());							
					notification.setLatestEventInfo(FreeWifiService.this, "FreeWifi", msg2, contentIntent);
					
					mNotificationManager.notify(ID, notification);
				}
			}
			Stats.event("Connection:"+(code==FreeWifiUtils.CONNECT_OK)+",Essais:"+essais);
		} finally {
			Stats.end(this);
			setForeground(false);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		bindCount++;
		return new IFreeWifiControl.Stub() {

			@Override
			public void connect(boolean force) throws RemoteException {
				doconnect(force);
			}
			
			
			
		};
	}

}
