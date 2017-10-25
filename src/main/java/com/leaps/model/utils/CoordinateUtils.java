package com.leaps.model.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.InvalidParametersException;

public class CoordinateUtils {

	private static final Logger logger = LoggerFactory.getLogger(CoordinateUtils.class);

    public enum CoordinatesEnum {
    	INSTANCE;
    	
    	public JsonObject getCoordinates(JsonObject requestData) throws InvalidInputParamsException, InvalidParametersException {
    		if (requestData.get("address") == null) {
        		throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
        	}
    		
    		String address = "Bulgaria, " + requestData.get("address").getAsString();
    		JsonObject coords = new JsonObject();
            StringBuffer query = new StringBuffer();
            String[] split = address.split(" ");
            String queryResult = null;
    		
            query.append("https://maps.googleapis.com/maps/api/geocode/json?address=");
            
            if (split.length == 0) {
                return null;
            }

            for (int i = 0; i < split.length; i++) {
                query.append(split[i]);
                if (i < (split.length - 1)) {
                    query.append("+");
                }
            }
            
            query.append("&key=" + Configuration.LEAPS_GEOMAP_KEY);
            
            if (Configuration.debugMode) {
                logger.debug("Query:" + query);
            }

            try {
                queryResult = getRequest(query.toString());
            } catch (Exception e) {
                if (Configuration.debugMode) {
                	logger.error("Error when trying to get data with the following query " + query);
                }
            }
            
            if (queryResult == null) {
                return null;
            }
            
    		JsonParser parser = new JsonParser();
    		
			JsonObject obj = parser.parse(queryResult).getAsJsonObject();
			
			if (obj == null) {
				throw new InvalidParametersException(Configuration.ERROR_READING_DATA_FROM_GEOLOCATOR);
			}
			
			if (Configuration.debugMode) {
				logger.debug("obj=" + obj);
			}
			
			JsonArray results = obj.get("results").getAsJsonArray();
			JsonObject geometry = results.get(0).getAsJsonObject().get("geometry").getAsJsonObject();
			JsonObject location = geometry.get("location").getAsJsonObject();
			
			coords.addProperty("coord_lat", location.get("lat").getAsDouble());
			coords.addProperty("coord_lnt", location.get("lng").getAsDouble());
            
    		return coords;
    	}
    	
        private String getRequest(String url) throws Exception {

            final URL obj = new URL(url);
            final HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            if (con.getResponseCode() != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }

		public JsonObject getAddress(JsonObject requestData) throws InvalidInputParamsException, InvalidParametersException {
			if (requestData.get("coord_lat") == null || requestData.get("coord_lnt") == null) {
        		throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
        	}
			
			
			
			double latitude = requestData.get("coord_lat").getAsDouble();
			double longitude = requestData.get("coord_lnt").getAsDouble();
    		StringBuffer query = new StringBuffer();
            String queryResult = null;
			JsonObject address = new JsonObject();
			
            query.append("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + Configuration.LEAPS_GEOMAP_KEY);
			
            try {
                queryResult = getRequest(query.toString());
            } catch (Exception e) {
                if (Configuration.debugMode) {
                	logger.error("Error when trying to get data with the following query " + query);
                }
            }
            
            if (queryResult == null) {
                return null;
            }
            
    		JsonParser parser = new JsonParser();
    		
			JsonObject obj = parser.parse(queryResult).getAsJsonObject();
			
			if (obj == null) {
				throw new InvalidParametersException(Configuration.ERROR_READING_DATA_FROM_GEOLOCATOR);
			}
			
			if (Configuration.debugMode) {
				logger.debug("obj=" + obj);
			}

			JsonArray results = obj.get("results").getAsJsonArray();
			String formattedAddress = results.get(0).getAsJsonObject().get("formatted_address").getAsString();
			address.addProperty("address", formattedAddress);
			
			return address;
		}
    }
}