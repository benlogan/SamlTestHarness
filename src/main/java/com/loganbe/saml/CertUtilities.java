package com.loganbe.saml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.opensaml.xml.security.x509.BasicX509Credential;

public class CertUtilities {

	public static BasicX509Credential credentialFromFile(String filePath) {
		// from file
		FileInputStream fin;
		try {
			fin = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException when processing certificate!");
			e.printStackTrace();
			return null;
		}
		CertificateFactory cf;
		Certificate certificate;
		try {
			cf = CertificateFactory.getInstance("X.509");
			certificate = cf.generateCertificate(fin);
		} catch (CertificateException e) {
			System.err.println("CertificateException when processing certificate!");
			e.printStackTrace();
			return null;
		}

		PublicKey pk = certificate.getPublicKey();
		System.out.println("PK FROM FILE : " + pk.toString());

		// vendor public key
		BasicX509Credential credential = new BasicX509Credential(); 
		credential.setPublicKey(pk);
		
		return credential;
	}
	
}