package com.loganbe.saml;

import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.signature.SignatureValidator;

public class ExaneSignatureValidator {

	public static boolean validateSignature(Signature signature, BasicX509Credential credential) {
		try {
			SignatureValidator validator = new SignatureValidator(credential);
		
			validator.validate(signature);
			return true;
		} catch (ValidationException e) {
			System.err.println("ValidationException when processing signature!");
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			System.err.println("Exception when processing signature!");
			e.printStackTrace();
			return false;
		}
	}
	
}