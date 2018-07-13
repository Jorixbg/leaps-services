package com.leaps.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.InvalidParametersException;
import com.leaps.model.utils.CoordinateUtils;
import com.leaps.model.utils.CoordinateUtils.CoordinatesEnum;
import com.leaps.model.utils.LeapsUtils;

@RestController
@RequestMapping("/utils")
@MultipartConfig
public class UtilsController {

	private final Logger logger = LoggerFactory.getLogger(UtilsController.class);
	private CoordinatesEnum coordinateUtils = CoordinateUtils.CoordinatesEnum.INSTANCE;
	
	
	/**
	 * Test notification order
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/test")
	public String test(HttpServletRequest req, HttpServletResponse resp) {
		
		JsonObject requestData;
		try {
			
			requestData = LeapsUtils.getRequestData(req);
			
		} catch (IOException e) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
		// TODO: topic-a e string format na event id-to
		
		JsonObject body = new JsonObject();
		body.addProperty("to", "/topics/" + requestData.get("event_id").getAsString());
		body.addProperty("priority", "high");
 
		JsonObject notification = new JsonObject();
		notification.addProperty("title", "yakoto zaglavie");
		notification.addProperty("body", "Vanka, da pocherpish bira!!!");
 
		body.add("notification", notification);
		
		HttpEntity<String> request = new HttpEntity<>(body.toString());
		
		CompletableFuture<String> pushNotification = LeapsUtils.pushNotifications(request);
		CompletableFuture.allOf(pushNotification).join();
		
		try {
			pushNotification.get();
			return HttpStatus.OK.toString();
		} catch (InterruptedException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ie.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (ExecutionException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ee.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	
	/**
	 * Get coordinates from given address
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/coordinates")
	public String getCoordinates(HttpServletRequest req, HttpServletResponse resp) {
		
		try {
			return coordinateUtils.getCoordinates(LeapsUtils.getRequestData(req)).toString();
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidInputParamsException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, iipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Get address from given coordinates
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/address")
	public String getAddress(HttpServletRequest req, HttpServletResponse resp) {
		try {
			return coordinateUtils.getAddress(LeapsUtils.getRequestData(req)).toString();
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidInputParamsException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, iipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Get address from given coordinates
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/full_address")
	public String getFullAddress(HttpServletRequest req, HttpServletResponse resp) {
		try {
			return coordinateUtils.getFullAddress(LeapsUtils.getRequestData(req)).toString();
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidInputParamsException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, iipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
}
