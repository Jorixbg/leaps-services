package com.leaps.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.utils.LeapsUtils;
import com.leaps.payment.PaymentSessionRequest;

@RestController
@RequestMapping("/payments")
public class PaymentsController {
	
	private final Logger logger = LoggerFactory.getLogger(PaymentsController.class);
	
	/**
	 * Create payment session
	 * @throws AuthorizationException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/paymentSession")
	public String createPaymentSession(HttpServletRequest req, HttpServletResponse resp) throws AuthorizationException, ClientProtocolException, IOException {
		
		LeapsUtils.checkToken(req.getHeader("Authorization"));
		
		ObjectMapper mapperObj = new ObjectMapper();
		
		PaymentSessionRequest request = new PaymentSessionRequest(LeapsUtils.getRequestData(req));
		
	    CloseableHttpClient client = HttpClients.createDefault();
	    HttpPost httpPost = new HttpPost("https://checkout-test.adyen.com/v37/paymentSession"); // TODO hardocded, create properties
	 
	    StringEntity entity = new StringEntity(mapperObj.writeValueAsString(request));
	    httpPost.setEntity(entity);
	    httpPost.setHeader("Accept", "application/json");
	    httpPost.setHeader("Content-type", "application/json");
	 
	    CloseableHttpResponse response = client.execute(httpPost);
	    client.close();
	    
	    return response.toString();
	}
	
}
