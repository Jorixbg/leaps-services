package com.leaps.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.utils.LeapsUtils;
import com.leaps.payment.PaymentSessionRequest;

@RestController
@RequestMapping("/payments")
public class PaymentsController {
	
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(PaymentsController.class);
	
	private RestTemplate restTemplate;
	
	/**
	 * Create payment session
	 * @throws AuthorizationException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/paymentSession")
	public String createPaymentSession(HttpServletRequest req, HttpServletResponse resp) throws AuthorizationException, ClientProtocolException, IOException {
		
		restTemplate = new RestTemplate();
//		LeapsUtils.checkToken(req.getHeader("Authorization"));
		
		ObjectMapper mapperObj = new ObjectMapper();
		
		PaymentSessionRequest request = new PaymentSessionRequest(LeapsUtils.getRequestData(req));
		
	    String url = "https://checkout-test.adyen.com/v37/paymentSession"; // TODO hardocded, create properties
	 
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("X-API-Key", "AQEqhmfuXNWTK0Qc+iScl2UotMWYS4RYA4cYDDfhOOiB09PxEfVmghV8BGCdEMFdWw2+5HzctViMSCJMYAc=-801n9bVAvOEh07mZH7rUK6vM3yIRBGGxELWcfNpN9Sg=-4exfen7P82cAhbxz"); // TODO
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    
	    HttpEntity<String> entity = new HttpEntity<String>(mapperObj.writeValueAsString(request), headers);
	    
	    ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
	    
	    return response.getBody();
	}
	
}
