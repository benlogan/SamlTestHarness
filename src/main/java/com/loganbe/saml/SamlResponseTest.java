package com.loganbe.saml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.loganbe.xml.XmlUtilities;

/**
 * Integration test with real messages for the end-to-end SAML check
 *
 * Loosely based on some notes here;
 * http://sureshatt.blogspot.fr/2012/11/how-to-read-saml-20-response-with.html
 *
 * This is the Exane Assertion Consumer Service (ACS)
 */
public class SamlResponseTest {

	static final String TEST_FILE_PATH = "src/main/resources/testResponseSigned.xml";

	static final String TEST_CERT_PATH = "src/main/resources/test_unit_pk.cer";

	static final String ASSERTION_SUBJECT = "_ce3d2948b4cf20146dee0a0b3dd6f69b6cf86f62d7";

	static final String ASSERTION_ISSUER = "http://idp.example.com/metadata.php";

	static final String ASSERTION_AUDIENCE = "http://sp.example.com/demo1/metadata.php";

	private static Element rootElement;
	private static Response response;
	private static Assertion assertion;

	@BeforeClass
	public static void setup() {

		try {
			DefaultBootstrap.bootstrap();

			Document doc = XmlUtilities.parseFileToXml(TEST_FILE_PATH);

			//System.out.println("Has Children? " + doc.hasChildNodes());
			//System.out.println("Has Attributes? " + doc.hasAttributes());

			//System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());

			rootElement = doc.getDocumentElement();

			response = SamlUtilities.parseResponse(rootElement);

			assertion = response.getAssertions().get(0); // we only expect one assertion
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception & Stack Trace!");
		}
	}

	@Test
	public void testResponseType() {
		System.out.println("ROOT ELEMENT : " + rootElement.getNodeName());
		assertEquals(rootElement.getNodeName(), "samlp:Response");
	}

	@Test
	public void testResponseStatus() {
		String statusCode = response.getStatus().getStatusCode().getValue();
		System.out.println("STATUS : " + statusCode);
		assertEquals(statusCode, "urn:oasis:names:tc:SAML:2.0:status:Success");
	}

	@Test
	public void testSignature() {
		// it's the assertion that is signed, not the response itself? not necessarily!
		Signature signature = response.getSignature();

		if(signature != null) {
			System.out.println("SIGNATURE : " + signature.toString());
		} else {
			System.err.println("SIGNATURE IS NULL");
		}

		assertTrue(validateSignature(signature));
	}

	//@Test
	public void testAssertionSignature() {
		// it's the assertion that is signed, not the response itself? not necessarily!
		Signature signature = assertion.getSignature();

		if(signature != null) {
			System.out.println("SIGNATURE : " + signature.toString());
		} else {
			System.err.println("SIGNATURE IS NULL");
		}

		assertTrue(validateSignature(signature));
	}

	@Test
	public void testAssertion() {
		// reading the subject name (what was authenticated at the IDP)
		String subject = assertion.getSubject().getNameID().getValue();
		System.out.println("SUBJECT : " + subject);
		assertEquals(subject, ASSERTION_SUBJECT);

		// reading the issuer (the IDP who issued the response object)
		String issuer = assertion.getIssuer().getValue();
		System.out.println("ISSUER : " + issuer);
		assertEquals(issuer, ASSERTION_ISSUER);

		// reading the audience (to whom the response was issued)
		String audience = assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getAudienceURI();
		System.out.println("AUDIENCE : " + audience);
		assertEquals(audience, ASSERTION_AUDIENCE);
	}

	// FIXME properly source public key!
	private boolean validateSignature(Signature signature) {
		// from the response (don't do this in prod!)
		//X509Certificate cert = (X509Certificate)signature.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0);
		//String certFromMessage = cert.getValue();
		//System.out.println("CERT MESSAGE : " + certFromMessage);

		// from string FIXME doesn't work!
		//String certString = "-----BEGIN CERTIFICATE-----\\nMIICajCCAdOgAwIBAgIBADANBgkqhkiG9w0BAQ0FADBSMQswCQYDVQQGEwJ1czETMBEGA1UECAwKQ2FsaWZvcm5pYTEVMBMGA1UECgwMT25lbG9naW4gSW5jMRcwFQYDVQQDDA5zcC5leGFtcGxlLmNvbTAeFw0xNDA3MTcxNDEyNTZaFw0xNTA3MTcxNDEyNTZaMFIxCzAJBgNVBAYTAnVzMRMwEQYDVQQIDApDYWxpZm9ybmlhMRUwEwYDVQQKDAxPbmVsb2dpbiBJbmMxFzAVBgNVBAMMDnNwLmV4YW1wbGUuY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDZx+ON4IUoIWxgukTb1tOiX3bMYzYQiwWPUNMp+Fq82xoNogso2bykZG0yiJm5o8zv/sd6pGouayMgkx/2FSOdc36T0jGbCHuRSbtia0PEzNIRtmViMrt3AeoWBidRXmZsxCNLwgIV6dn2WpuE5Az0bHgpZnQxTKFek0BMKU/d8wIDAQABo1AwTjAdBgNVHQ4EFgQUGHxYqZYyX7cTxKVODVgZwSTdCnwwHwYDVR0jBBgwFoAUGHxYqZYyX7cTxKVODVgZwSTdCnwwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQ0FAAOBgQByFOl+hMFICbd3DJfnp2Rgd/dqttsZG/tyhILWvErbio/DEe98mXpowhTkC04ENprOyXi7ZbUqiicF89uAGyt1oqgTUCD1VsLahqIcmrzgumNyTwLGWo17WDAa1/usDhetWAMhgzF/Cnf5ek0nK00m0YZGyc4LzgD0CROMASTWNg==\\n-----END CERTIFICATE-----";
		//System.out.println("CERT STRING : " + certString);
		//java.security.cert.X509Certificate cert = new java.security.cert.X509Certificate();
		//cert.setValue(certString);

		//assertEquals(certFromMessage, certString);

		BasicX509Credential credential = CertUtilities.credentialFromFile(TEST_CERT_PATH);

		return ExaneSignatureValidator.validateSignature(signature, credential);
	}

	// http://travistidwell.com/jsencrypt/demo/
	// FIXME but I'm not passing the key, I'm passing the whole PEM encoded CERT!
	/*
	public static PublicKey getKey(String key) {
		try {
			byte[] byteKey = Base64.getDecoder().decode(key);
			//byte[] byteKey = key.getBytes(); // definitely needs decoding!

			X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(X509publicKey);

			System.out.println("PUBLIC KEY FROM STRING...");
			System.out.println(pk);

			return pk;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	*/

}