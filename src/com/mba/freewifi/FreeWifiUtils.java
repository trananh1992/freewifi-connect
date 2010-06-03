package com.mba.freewifi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class FreeWifiUtils {
	
	private static final String TAG = "FreeWifiConnector";
	private static final FreeWifiUtils instance = new FreeWifiUtils();
	public static final int CONNECT_OK = 0, CONNECT_ERROR_UNKNOWN = 1, CONNECT_ERROR_CERTIF = 2, CONNECT_ERROR_TIMEOUT = 3;
	
	private FreeWifiUtils() {
	}
	
	public static FreeWifiUtils getInstance() {
		return instance;
	}
	
	public synchronized int doConnect(String user, String password) {
		
		try {	
			int connection_Timeout = 7000; // = 7 sec

			HttpParams my_httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(my_httpParams, connection_Timeout);
			HttpConnectionParams.setSoTimeout(my_httpParams, connection_Timeout); 
		
			DefaultHttpClient httpclient = new DefaultHttpClient(my_httpParams);
			TrustAllSSLSocketFactory tasslf = new TrustAllSSLSocketFactory();
	        Scheme sch = new Scheme("https", tasslf, 443);
			httpclient.getConnectionManager().getSchemeRegistry().register(sch);

			HttpGet get = null;
			HttpResponse response = null;
			HttpEntity responseEntity = null;
			BufferedReader br = null;
			String s = null;

//			// Free met maintenant un identifiant dans la page ...
//			HttpGet get = new HttpGet("https://wifi.free.fr/");
//			
//			HttpResponse response = httpclient.execute(get);
//
//			HttpEntity responseEntity = response.getEntity();
//			BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
//			String priv = null;
//			while ( (s=br.readLine())!=null ) {
//				if (s.indexOf("<input name=\"priv\"")>-1) {
//					priv = s.substring(s.indexOf("value=")+7);
//					priv = priv.substring(0, priv.indexOf('"'));
//				}
//			}
			
		    ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
		    values.add(new BasicNameValuePair("login", user));
		    values.add(new BasicNameValuePair("password", password));
		    //values.add(new BasicNameValuePair("priv", priv));
		    values.add(new BasicNameValuePair("url", "http://www.google.fr/"));
		    
			HttpPost post = new HttpPost("https://wifi.free.fr/Auth");
			post.setEntity(new UrlEncodedFormEntity(values));
			response = httpclient.execute(post);
			responseEntity = response.getEntity();
						
			br = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
			s = null;
			while ( (s=br.readLine())!=null ) {
				if (s.indexOf("CONNEXION AU SERVICE REUSSI")>-1) {
					return CONNECT_OK;
				}
			}
		} catch (javax.net.ssl.SSLException jne) {
			Log.e(TAG, "", jne);
			return CONNECT_ERROR_CERTIF;
		} catch (java.net.SocketTimeoutException st) {
			Log.e(TAG, "", st);
			return CONNECT_ERROR_TIMEOUT;
		} catch (Throwable t) {
			Log.e(TAG, "", t);
		}
		
		return CONNECT_ERROR_UNKNOWN;
	}

	public boolean onFreewifi(Context c) {
		ConnectivityManager cManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
	    if (networkInfo != null) {
	      if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
	        WifiManager wManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
	        WifiInfo wInfo = wManager.getConnectionInfo();
	        return "freewifi".equalsIgnoreCase(wInfo.getSSID());
	      }
	    }
	    return false;
	}
	
}
