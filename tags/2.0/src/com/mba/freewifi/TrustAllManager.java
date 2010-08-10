package com.mba.freewifi;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class TrustAllManager implements X509TrustManager {

	private static final String KEY_FREE = "modulus: f0580d80d552a0ed801bfc0bbf05d227abae142635e7570b833b68d02188d01c23695483902d83177a16d93df206547183520b723d2dec1bd06b532a78d9b0ecf809c8838753b6bfe5d48dd03d0f2b87216fb783c37837a32ac5409c3d42fa82b1be7f0c0fbda7d80d2c008693f622bed04e0791ed930140e39da7eaa152f9e2d3f248f2ef1c496af5cfff01940c069480573afdf825ae77b2fe838f1416bf8f55dbfd76382f23254d6d58025a185c1ebf46e304173fba39a6db6e04d48bdd4e9a71e96e80fadf6e4e599b72ccd2b339a6b5dac0a2acaba2e3bd47d4465ad30a711b55609a22d59224bc800aa838dad3cb247ea86f1371e50dad25449c0d9a53";

	public void checkClientTrusted(X509Certificate[] cert, String authType) throws CertificateException { 
    }
    public void checkServerTrusted(X509Certificate[] cert, String authType) throws CertificateException { 
    	for (X509Certificate x : cert) {
    		if (x.getSubjectDN()!=null && x.getSubjectDN().getName().startsWith("CN=*.free.fr")) {
    			// Certificat trouve. Est-ce que c'est le bon ?
    			if (x.getPublicKey().toString().contains(KEY_FREE)) {
    				// OK ...
    				return;
    			}
    		}
    	}
    	
    	throw new CertificateException("Ce n'est pas le certificat de Free !");
    }
    public X509Certificate[] getAcceptedIssuers() { 
    	return null; 
    }
} 