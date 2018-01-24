package com.loganbe.crm;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.loganbe.SamlTestController;

public class CrmServiceHandler {

	public CrmResponse callCrmService(String crmProxyUrl) {
		
		// https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-restclient
		RestTemplate restTemplate = new RestTemplate();
		// we can't do this, as we need to set the auth/header
        //CheckUser checkUser = restTemplate.getForObject("https://cube.exane.com/service/vendors/checkuser/ludovic.tenant@exane.com/", CheckUser.class);
        
		HttpHeaders headers = new HttpHeaders();
		//headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
		//headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Authorization", SamlTestController.CRM_AUTH_HEADER);

		//HttpEntity<RestRequest> entityReq = new HttpEntity<RestRequest>(request, headers);
		HttpEntity<String> entityReq = new HttpEntity<String>("parameters", headers);
		
        ResponseEntity<CrmResponse> respEntity = restTemplate
        	    .exchange(crmProxyUrl, HttpMethod.GET, entityReq, CrmResponse.class);
        
        System.out.println("CRM Response (RAW) : " + respEntity.toString());
    	
        return respEntity.getBody();
	}
	
}