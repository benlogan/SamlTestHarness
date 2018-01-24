package com.loganbe;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.loganbe.crm.CrmResponse;
import com.loganbe.crm.CrmServiceHandler;

public class CrmAuthTest {

	@BeforeClass
	public static void setup() {
		SamlTestController.CRM_AUTH_HEADER = System.getProperty("CRM_AUTH_HEADER");
	}
	
	@Test
	public void hasAccess() {
		CrmServiceHandler serviceHandlder = new CrmServiceHandler();
		
		String crmProxyUrl = SamlTestController.CRM_SERVICE_URL + "ben.logan@exanebnpparibas.com" + "/";
		
		CrmResponse response = serviceHandlder.callCrmService(crmProxyUrl);
		
		assertTrue(response.isExaneResearchAccess());
	}
	
	@Test
	public void noAccess() {
		CrmServiceHandler serviceHandlder = new CrmServiceHandler();
		
		String crmProxyUrl = SamlTestController.CRM_SERVICE_URL + "ben.logan1@exanebnpparibas.com" + "/";
		
		CrmResponse response = serviceHandlder.callCrmService(crmProxyUrl);
		
		assertFalse(response.isExaneResearchAccess());
	}

}