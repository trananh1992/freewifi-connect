package com.mba.freewifi;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class FreeWifiConnect extends PreferenceActivity implements OnSharedPreferenceChangeListener, ServiceConnection {
    
	private static final String TAG = "FreeWifi-Connect";
	//private static final String CARTE = "lien_carte";
	
	protected static final String KEY_PREFS		= "freewifi";
	
	protected static final String KEY_ONOFF		= "onoff";
	protected static final String KEY_USER		= "user";
	protected static final String KEY_PASSWORD	= "password";
	protected static final String KEY_NOTIF		= "notifications";
	protected static final String KEY_MAXTRIES	= "maxtries";
	protected static final String KEY_PREFERKNOWN	= "prefererknown";
	protected static final String KEY_DELAI		= "delai";
	protected static final String KEY_WATCHDOG	= "watchdog";
	protected static final String KEY_PREFER	= "prefer";
	
	private IFreeWifiControl freeWifiControl = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		addPreferencesFromResource(R.xml.prefs);
		
		// Initialiser d'apres les prefs actuelle
		updateLibelle(KEY_USER);
		updateLibelle(KEY_PASSWORD);
		updateLibelle(KEY_MAXTRIES);
		updateLibelle(KEY_DELAI);
		
//		Display d = getWindowManager().getDefaultDisplay(); 
//        final int width = d.getWidth(); 
//        final int height = d.getHeight(); 

//        findPreference(CARTE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//			public boolean onPreferenceClick(Preference preference) {
//				Intent i = new Intent(Intent.ACTION_VIEW);				
//				i.setData(Uri.parse("http://m.sensorly.com/maps/android.php?sdk_version="+Build.VERSION.SDK+"&width="+width+"&height="+height));
//				startActivity(i);
//				return true;
//			}
//		});

		Stats.start(this);
		
		Intent svc = new Intent(this, FreeWifiService.class);
		bindService(svc, this, Context.BIND_AUTO_CREATE);
    }

    private SharedPreferences getSharedPreferences() {
		return super.getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
	} 
    
    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
		return getSharedPreferences();
	}
    
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		updateLibelle(key);
	}
	
	private boolean inflateOK = false;
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!inflateOK) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.menu, menu);
	        inflateOK=true;
		}
        return true;
    }
	
	private void updateLibelle(String key) {
		if (KEY_ONOFF.equals(key) || KEY_NOTIF.equals(key) || KEY_PREFER.equals(key) || KEY_WATCHDOG.equals(key)) {
			return;
		}
		String summary = null;
		String value = notEmpty(getSharedPreferences().getString(key, null));
		if (KEY_USER.equals(key)) {
			summary = "Actuellement: "+(value==null?"Non renseigné":value);
		} else
		if (KEY_PASSWORD.equals(key)) {
			summary = "Actuellement: "+(value==null?"Non renseigné":"Renseigné.");
		} else
		if (KEY_MAXTRIES.equals(key)) {
			summary = "Actuellement: "+(value==null?"3":value);
		} else
		if (KEY_DELAI.equals(key)) {
			summary = "Actuellement: "+(value==null||"0".equals(value)?"Immédiat":value);
		}
		if (summary!=null) {
			PreferenceScreen ps = getPreferenceScreen();
			if (ps!=null) {
				Preference p = ps.findPreference(key);
				if (p!=null) {
					p.setSummary(summary);
				} 
			}
		}
	}
	
	private String notEmpty(String s) {
		if (s!=null && s.trim().length()==0) {
			return null;
		}
		return s;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId()==R.id.Connecter) {
			if (!FreeWifiUtils.getInstance().onFreewifi(this)) {
				Toast.makeText(this, "Je ne peux me connecter que sur le ssid FreeWifi ...", Toast.LENGTH_LONG).show();
				return true;
			}
			
			final String user = notEmpty(getSharedPreferences().getString(KEY_USER, null));
			final String password = notEmpty(getSharedPreferences().getString(KEY_PASSWORD, null));
			
			if (user==null || password==null) {
				Toast.makeText(this, "Renseignez d'abord vos utilisateurs / mot de passe", Toast.LENGTH_LONG).show();
			} else {
				
				if (freeWifiControl!=null) {
					new Thread() {
						public void run() {
							try {
								freeWifiControl.connect(true);
							} catch (RemoteException e) {
								e.printStackTrace();
							}							
						}
					}.start();
				}
				Toast.makeText(this, "En cours ...", Toast.LENGTH_LONG).show();
			}
			
			return true;
		} else
		if (item.getItemId()==R.id.aide) {
			AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle(null);
			d.setMessage(
					"Pour résumer, "+
					"allez sur https://wifi.free.fr/ pour vous inscrire au service "+
					"free puis renseignez ici vos identifiants.\n"+
					"PS: Je ne peux rien contre les problème de connection aléatoires de certains HTC avec la freebox ..."
			);
			d.setButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			d.show();	
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onServiceConnected(ComponentName cn, IBinder binder) {
		freeWifiControl=(IFreeWifiControl)binder;
	}


	@Override
	public void onServiceDisconnected(ComponentName cn) {
		freeWifiControl=null;
	}
	
	@Override
	protected void onStop() {
		Stats.end(this);
		super.onStop();
	}
}