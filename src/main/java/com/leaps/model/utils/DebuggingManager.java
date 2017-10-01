package com.leaps.model.utils;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import com.google.gson.JsonObject;

public class DebuggingManager {

	public static void logRequestHeaders(HttpServletRequest request, Logger logger) {
		logger.info("-------------------- BEGIN ORDER --------------------");
		logger.info("Request header:");
		Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            logger.info(key + " : " + value);
        }
	}

	public static void logResponseJson(JsonObject response, Logger logger) {
		logger.info("Response: ");
		logger.info(response.toString());
		logger.info("-------------------- END ORDER --------------------");
	}

}
