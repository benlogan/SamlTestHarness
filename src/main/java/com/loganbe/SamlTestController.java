package com.loganbe;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;

import com.loganbe.crm.CrmResponse;
import com.loganbe.crm.CrmServiceHandler;
import com.loganbe.saml.CertUtilities;
import com.loganbe.saml.ExaneSignatureValidator;
import com.loganbe.saml.SamlUtilities;
import com.loganbe.xml.XmlUtilities;

/**
 * Spring Boot Controller for the SAML Test App
 *
 * This is the Test Exane Assertion Consumer Service (ACS)
 */
@Controller
@EnableAutoConfiguration
public class SamlTestController {

	public static final String CRM_SERVICE_URL = "https://cube.exane.com/service/vendors/checkuser/";
	public static String CRM_AUTH_HEADER; // don't put this in src control!

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "SAML Test App. Nothing here! Try /AuthoriseUser ...";
    }

    private static final String SAML_DESTINATION = "http://www.exane.com";
    // FIXME map!
    private static final String SAML_ISSUER = "http://www.reddeer.com";

    //@RequestMapping("/AuthoriseUser")
    @RequestMapping(value="/AuthoriseUser", method=RequestMethod.POST, consumes=MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    ResponseEntity<?> authenticateAndAuthorise(HttpServletRequest request, String relayState, @RequestBody String payload) throws URISyntaxException {

    	// System.out.println("POST payload : " + payload);

    	// validation? FIXME
    	// https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-validation

    	String userEmailAddress = null;

		// SAML security checks
		try {
			DefaultBootstrap.bootstrap();

			Document document = XmlUtilities.parseStringToXml(payload);
			if(document.getDocumentElement().getNodeName() != "samlp:Response") {
				return authenticationErrorResponse("Not a valid SAML response");
			} else {
				Response response = SamlUtilities.parseResponse(document.getDocumentElement());

				if(!StatusCode.SUCCESS_URI.equals(response.getStatus().getStatusCode().getValue())) {
					return authenticationErrorResponse("SAML Response - Status Code Mismatch");
				}

				// this should be our service, otherwise it maybe wasn't meant for us!
				if(!response.getDestination().equals(SAML_DESTINATION)) {
					return authenticationErrorResponse("SAML Response - Destination Mismatch");
				}

				// check the issuer against our list
				if(!response.getIssuer().getValue().equals(SAML_ISSUER)) {
					return authenticationErrorResponse("SAML Response - Issuer Mismatch");
				}

				Assertion assertion = response.getAssertions().get(0);

				// extract the subject from the SAML (not a request param)
				userEmailAddress = assertion.getSubject().getNameID().getValue();
				System.out.println("SAML SUBJECT : " + userEmailAddress);
				if(userEmailAddress == null || userEmailAddress.trim().length() <= 0) {
					return authenticationErrorResponse("SAML Response - no subject email address");
				}

		    	Signature signature = assertion.getSignature(); // or response. ?

		    	// FIXME obviously need to load the cert associated with the issuer
		    	BasicX509Credential credential = CertUtilities.credentialFromFile("src/main/resources/reddeer_pk.cer");
		    	if(!ExaneSignatureValidator.validateSignature(signature, credential)) {
		    		return authenticationErrorResponse("Signature Validation Failed");
		    	}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return authenticationErrorResponse("Unhandled Exception");
		}

    	// PROXY to https://cube.exane.com/service/vendors/checkuser/EMAIL/
    	String crmProxyUrl = CRM_SERVICE_URL + userEmailAddress + "/";
    	System.out.println("Proxying Authorisation : " + crmProxyUrl);

    	// CRM REST Service (authorise client contact)
		CrmResponse crmResponse = new CrmServiceHandler().callCrmService(crmProxyUrl);
        //System.out.println("CRM Response (JSON) : " + new Gson().toJson(crmResponse));

        // SETUP RESPONSE
        if(crmResponse.isExaneResearchAccess()) {
        	// redirect to target destination
        	//System.out.println("Authorisation Successful. Redirecting to target : " + relayState);

        	HttpHeaders httpHeaders = new HttpHeaders();
        	String location = "http://" + request.getServerName() + "/" + relayState; // stay on same domain for testing!
        	System.out.println("Authorisation Successful. Redirecting to target : " + location);
        	httpHeaders.setLocation(new URI(location));
        	//httpHeaders.setLocation(new URI(relayState));
            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER); // HTTP 303
            // forces a GET request to the new URL even if original request was POST
            // https://www.w3.org/TR/cors/#redirect-steps NOT WIDELY SUPPORED - TRIGGERS CROSS ORIGIN ERRORS USUALLY
        } else {
        	// 403 - authorization fail
        	return new ResponseEntity<String>("Authorisation (CRM) Failed. Access Denied.", HttpStatus.FORBIDDEN);
        }
    }

	// 401 - authentication fail
    public static ResponseEntity<String> authenticationErrorResponse(String errorMessage) {
    	System.err.println("SAML Auth Problem : " + errorMessage);
    	return new ResponseEntity<String>("Authentication (SAML) Failed. Access Denied. ERROR : " + errorMessage, HttpStatus.UNAUTHORIZED);
    }

	public static void main(String[] args) {
		System.out.println("SAML Test Harness (STARTING)");

		//CRM_AUTH_HEADER = args[0];
		CRM_AUTH_HEADER = System.getenv("CRM_AUTH_HEADER");

		SpringApplication.run(SamlTestController.class, args);

        System.out.println("SAML Test Harness (STARTED)");
	}

}