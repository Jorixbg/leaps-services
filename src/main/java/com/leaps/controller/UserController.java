package com.leaps.controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.event.Event;
import com.leaps.model.event.EventDao;
import com.leaps.model.event.Tag;
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.exceptions.InvalidCredentialsException;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.InvalidParametersException;
import com.leaps.model.exceptions.TagException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.image.Image;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.DebuggingManager;
import com.leaps.model.utils.EmailSender;
import com.leaps.model.utils.LeapsUtils;

@RestController
@RequestMapping("/user")
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
		
	/**
	 * User update method
	 * 
	 * RAPID
	 * 
	 * check if the method is working properly
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/update")
	public String updateUser(HttpServletRequest req, HttpServletResponse resp) {
		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		Map<String, Object> mapForCacheUpdate = new HashMap<String, Object>();
		Long userId;
		String username;
		String email;
		String pass;
		String gender;
		String location;
		String firstName;
		String lastName;
		int maxDistanceSetting;
		boolean isTrainer;
		Long birthday;
		String description;
		String profileImageUrl;
		String phoneNumber;
		int yearsOfTraining;
		int sessionPrice;
		String longDescription;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException();
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
						
			if (obj.get("user_id") == null) {
				throw new UserException(Configuration.INVALID_USER_ID);
			}
			
			userId = obj.get("user_id").getAsLong();
			
			if (obj.get("username") != null) {
				username = obj.get("username").getAsString();
				map.put("username", new HashMap<String, Object>());
				map.get("username").put("string", username);
			}
			
			if (obj.get("email_address") != null) {
				email = obj.get("email_address").getAsString();
				map.put("email_address", new HashMap<String, Object>());
				map.get("email_address").put("string", email);
			}
			
			if (obj.get("password") != null) {
				pass = LeapsUtils.convertToMd5(obj.get("password").getAsString());
				map.put("password", new HashMap<String, Object>());
				map.get("password").put("string", pass);
			}
			
			if (obj.get("gender") != null) {
				gender = obj.get("gender").getAsString();
				map.put("gender", new HashMap<String, Object>());
				map.get("gender").put("string", gender);
			}
			
			if (obj.get("location") != null) {
				location = obj.get("location").getAsString();
				map.put("location", new HashMap<String, Object>());
				map.get("location").put("string", location);
			}
			
			if (obj.get("first_name") != null) {
				firstName = obj.get("first_name").getAsString();
				map.put("first_name", new HashMap<String, Object>());
				map.get("first_name").put("string", firstName);
			}
			
			if (obj.get("last_name") != null) {
				lastName = obj.get("last_name").getAsString();
				map.put("last_name", new HashMap<String, Object>());
				map.get("last_name").put("string", lastName);
			}
			
			if (obj.get("max_distance_setting") != null) {
				maxDistanceSetting = obj.get("max_distance_setting").getAsInt();
				map.put("max_distance_setting", new HashMap<String, Object>());
				map.get("max_distance_setting").put("int", maxDistanceSetting);
			}
			
			if (obj.get("birthday") != null) {
				birthday = obj.get("birthday").getAsLong();
				map.put("birthday", new HashMap<String, Object>());
				map.get("birthday").put("long", birthday);
				
				int age = LeapsUtils.generateAgeFromBirthday(birthday);
				map.put("age", new HashMap<String, Object>());
				map.get("age").put("int", age);
			}
			
			if (obj.get("description") != null) {
				description = obj.get("description").getAsString();
				map.put("description", new HashMap<String, Object>());
				map.get("description").put("string", description);
			}
			
			if (obj.get("is_trainer") != null) {
				isTrainer = obj.get("is_trainer").getAsBoolean();
				map.put("is_trainer", new HashMap<String, Object>());
				map.get("is_trainer").put("boolean", isTrainer);
			}
			
			if (obj.get("phone_number") != null) {
				phoneNumber = obj.get("phone_number").getAsString();
				map.put("phone_number", new HashMap<String, Object>());
				map.get("phone_number").put("string", phoneNumber);
			}
			
			if (obj.get("years_of_training") != null) {
				yearsOfTraining = obj.get("years_of_training").getAsInt();
				map.put("years_of_training", new HashMap<String, Object>());
				map.get("years_of_training").put("int", yearsOfTraining);
			}
			
			if (obj.get("price_for_session") != null) {
				sessionPrice = obj.get("price_for_session").getAsInt();
				map.put("session_price", new HashMap<String, Object>());
				map.get("session_price").put("int", sessionPrice);
			}
			
			if (obj.get("long_description") != null) {
				longDescription = obj.get("long_description").getAsString();
				map.put("long_description", new HashMap<String, Object>());
				map.get("long_description").put("string", longDescription);
			}
			
			if (!DBUserDao.getInstance().updateUser(map, userId)) {
				throw new UserException(Configuration.ERROR_WHILE_UPDATING_THE_USER);
			}
			
			User user = DBUserDao.getInstance().getUserFromDbById(userId);
			if (user != null) {
				UserDao.getInstance().updateUserInCache(user, checker);
			}
			return HttpStatus.OK.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException e) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Get user method
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{user_id}")
	@ResponseBody
	public Object getUser(HttpServletRequest req, HttpServletResponse resp, @PathVariable("user_id") int user_id) {
		if (Configuration.debugMode) {
			DebuggingManager.logRequestHeaders(req, logger);
		}
		
		long token = UserDao.getInstance().checkIfTokenIsValid(req.getHeader("Authorization"));
		
		try {
			if(Long.valueOf(user_id) == null) {
				throw new InvalidParametersException(Configuration.INVALID_USER_ID);
			}
			
			User user = UserDao.getInstance().getUserFromDbOrCacheById(user_id);
			
			if (user == null) {
				throw new UserException(Configuration.INVALID_USER_ID);
			}			

			JsonObject response = LeapsUtils.generateJsonUser(user, token);
			
			if (Configuration.debugMode) {
				DebuggingManager.logResponseJson(response, logger);
			}
			
			return response.toString();
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ipe.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		}
	}
	
	/**
	 * Filter trainers method
	 * 
		{
		  "key_words" : "test",
		  "my_lnt": 13.1342331,
		  "my_lat": 12.12311,
		  "distance": 1000000,
		  "tags": [
		    "yoga",
		    "fafla"
		  ],
		  "min_start_date": "101978506756",
		  "max_start_date": "9226623422532",
		  "limit" : 20,
		  "page" : 1
		}
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/trainer/filter")
	public String getMultipleTrainers(HttpServletRequest req, HttpServletResponse resp) {
		String userFullName;
		String keyWord = null;
		double latitude = 0.0;
		double longitude = 0.0;
		int distance = Configuration.USER_DEFAULT_MAX_DISTANCE_SETTING;
		List<String> tags = new ArrayList<String>();
		int page = 1;
		int limit = Configuration.FILTER_EVENTS_DEFAULT_PAGE_LIMITATION;
		long minStartingDate = -1;
		long maxStartingDate = -1;

		if (Configuration.debugMode) {
			DebuggingManager.logRequestHeaders(req, logger);
			logger.info("---------- user/trainer/filter------------");
		}
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String requestData = sb.toString();
			
			if (Configuration.debugMode) {
				logger.info("------------------------------------------");
				logger.info("Request Data:");
				logger.info(requestData);
				logger.info("------------------------------------------");
			}
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("key_words") != null) {
				keyWord = obj.get("key_words").getAsString();
			}
			
			// commenting the longitude and latitude values as the users should not be searched by those criterias
//			if (obj.get("my_lnt") != null && obj.get("my_lat") != null) {
//				latitude = obj.get("my_lat").getAsDouble();
//				longitude = obj.get("my_lnt").getAsDouble();
//			}
			
			if (obj.get("distance") != null) {
				distance = obj.get("distance").getAsInt();
			}
			
			if (obj.get("tags") != null) {
				JsonElement jsonTags = obj.get("tags");
				Type listType = new TypeToken<List<String>>() {}.getType();

				tags = new Gson().fromJson(jsonTags, listType);
			}
			
			if (obj.get("all_trainers") != null && obj.get("all_trainers").getAsBoolean() == Boolean.TRUE) {
				minStartingDate = System.currentTimeMillis();
			} else if (obj.get("min_start_date") != null) {
				minStartingDate = obj.get("min_start_date").getAsLong();
			} else {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS + ": starting date");
			}
			
			if (obj.get("all_trainers") != null && obj.get("all_trainers").getAsBoolean() == Boolean.TRUE) {
				maxStartingDate = Long.valueOf(Configuration.FAR_FAR_AWAY_TIME);
			} else if (obj.get("max_start_date") != null) {
				maxStartingDate = obj.get("max_start_date").getAsLong();
			} else {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS + ": end date");
			}
			
			if (obj.get("limit") != null) {
				limit = obj.get("limit").getAsInt();
			}
			
			if (obj.get("page") != null && obj.get("page").getAsInt() > 0) {
				page = obj.get("page").getAsInt();
			}
			
			List<User> filteredTrainersByTheirEvents = null;

			JsonObject orderResponse = new JsonObject();
			JsonArray filteredEventsJson = new JsonArray();
			
			if (latitude != 0.0 && longitude != 0.0) {
				filteredTrainersByTheirEvents = DBUserDao.getInstance().getFilteredTrainersByMostEventsWithCoordinates(keyWord, latitude, longitude, distance, tags, minStartingDate, maxStartingDate, page, limit);
				orderResponse.addProperty("total_results", DBUserDao.getInstance().countFilteredTrainersByMostEventsWithCoordinates(keyWord, latitude, longitude, distance, tags, minStartingDate, maxStartingDate, page, limit));
			} else {
				filteredTrainersByTheirEvents = DBUserDao.getInstance().getFilteredTrainersByMostEvents(keyWord, distance, tags, minStartingDate, maxStartingDate, page, limit);
				orderResponse.addProperty("total_results", DBUserDao.getInstance().countFilteredTrainersByTheirEvents(keyWord, distance, tags, minStartingDate, maxStartingDate, page, limit));
			}
			
			for (int k = 0; k < filteredTrainersByTheirEvents.size(); k++) {
				JsonObject response = new JsonObject();
				User user = filteredTrainersByTheirEvents.get(k);
				response.addProperty("user_id", user.getUserId());
				response.addProperty("username", user.getUsername());
				
				int userAttendedEvents = DBUserDao.getInstance().getAllEventCountThatUserHasAttended(user.getUserId());
				response.addProperty("attended_events", userAttendedEvents);
				
				response.addProperty("description", user.getDescription());
				response.addProperty("email_address", user.getEmail());
				response.addProperty("age", user.getAge());
				response.addProperty("gender", user.getGender());
				response.addProperty("location", user.getLocation());
				response.addProperty("max_distance_setting", user.getMaxDistanceSetting());
				response.addProperty("first_name", user.getFirstName());
				response.addProperty("last_name", user.getLastName());
				response.addProperty("birthday", user.getBirthday());
				response.addProperty("profile_image_url", user.getProfileImageUrl());
				response.addProperty("is_trainer", user.isTrainer());
				response.addProperty("long_description", user.isTrainer() ? user.getLongDescription() : null);
				response.addProperty("years_of_training", user.isTrainer() ? user.getYearsOfTraining() : null);
				response.addProperty("session_price", user.isTrainer() ? user.getSessionPrice() : null);

				// TODO: get followed by - in stage 2
				JsonArray followedByJson = new JsonArray();
				response.add("followed_by", followedByJson);
				
				List<Event> attendingEvents = DBUserDao.getInstance().getAllAttendingEventsForUser(user.getUserId());
				JsonArray attendingEventsJson = new JsonArray();
				
				userFullName = user.getFirstName() + " " + user.getLastName();
				
				for (int i = 0; i < attendingEvents.size(); i++) {
					attendingEventsJson.add(LeapsUtils.generateJsonEvent(attendingEvents.get(i), null));
				}
				response.add("attending_events", attendingEventsJson);
				
				List<Event> hostingEvents = DBUserDao.getInstance().getAllHostingEventsForUser(user.getUserId());
				JsonArray hostingEventsJson = new JsonArray();
				for (int i = 0; i < hostingEvents.size(); i++) {
					hostingEventsJson.add(LeapsUtils.generateJsonEvent(hostingEvents.get(i), null));
				}
				response.add("hosting_events", hostingEventsJson);
				
				JsonArray specialtiesJson = new JsonArray();
				List<Tag> specialties = EventDao.getInstance().getAllUserSpecialties(user.getUserId());
				for (int i = 0; i < specialties.size(); i++) {
					specialtiesJson.add(specialties.get(i).getName());
				}
				response.add("specialties", specialtiesJson);
				
				List<Image> userImages = DBUserDao.getInstance().getAllUserImages(user.getUserId());
				JsonArray userImagesJson = new JsonArray();
				
				for (int i = 0; i < userImages.size(); i++) {
					JsonObject tempObj = new JsonObject();
					obj.addProperty("image_id", userImages.get(i).getImageId());
					obj.addProperty("image_url", userImages.get(i).getImageName());
					userImagesJson.add(tempObj);
				}
				response.add("images", userImagesJson);
				
				filteredEventsJson.add(response);
			}
			
			orderResponse.add("trainers", filteredEventsJson);

			if (Configuration.debugMode) {
				logger.info("------------------------------------------");
				logger.info("Order Response:");
				logger.info(orderResponse.toString());
				logger.info("------------------END---------------------");
			}
			return orderResponse.toString();
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Configuration.INVALID_INPUT_PAREMETERS);
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException e) {
			return e.toString();
		}
	}
	
	/**
	 * Dummy reset password method
	 * 
		{
		    "email_address" : gosho@abv.bg
		}
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/resetPassword/dummy")
	public String dummyResetPassword(HttpServletRequest req, HttpServletResponse resp) {
		String email;
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();

			if (obj.get("email_address") == null) {
				throw new InvalidInputParamsException();
			}
			
			return HttpStatus.OK.toString();
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Configuration.INVALID_INPUT_PAREMETERS);
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (IOException e) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}

	/**
	 * Reset password method
	 * 
		{
		    "email_address" : gosho@abv.bg
		}
	 * @throws Exception 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/resetPassword")
	public String resetPassword(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String email;
		try (Scanner sc = new Scanner(req.getInputStream())) {
//			StringBuilder sb = new StringBuilder();
//			while (sc.hasNext()) {
//				sb.append(sc.nextLine());
//			}
//			
//			String requestData = sb.toString();
//			
//			JsonParser parser = new JsonParser();
//			JsonObject obj = parser.parse(requestData).getAsJsonObject();
//
//			if (obj.get("email_address") == null) {
//				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
//			}
//			
//			email = obj.get("email_address").getAsString();
//			
//			if(!UserDao.getInstance().resetPassword(email)) {
//				// TODO: remove the unauthorized exception
//				// in all cases it will return ok status at the moment
////				throw new UserException(Configuration.INVALID_EMAIL);
//			}
			
			EmailSender.sendEmail();
			return HttpStatus.OK.toString();
//		} catch (InvalidInputParamsException iip) {
//			try {
//				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
//				return null;
//			} catch (IOException ioe2) {
//				return null;
//			}
//		} catch (UserException ue) {
//			try {
//				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
//				return null;
//			} catch (IOException ioe1) {
//				return null;
//			}
//		} catch (IOException ioe) {
//			try {
//				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
//				return null;
//			} catch (IOException ioe2) {
//				return null;
//			}
		} catch (Exception e) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
		
	/**
	 * Get all feed trainers method
	 * 
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/trainer/feed")
	public String getFeedTrainers(HttpServletRequest req, HttpServletResponse resp) {
		
		try {
			long token = UserDao.getInstance().checkIfTokenIsValid(req.getHeader("Authorization"));
			// TODO: add try catch clauses at least for the DB
			
			List<User> trainersWithMostEventsCreated = DBUserDao.getInstance().getAllTrainersWithMostEventsCreated();
	
			JsonArray response = new JsonArray();
			
			for (int i = 0; i < trainersWithMostEventsCreated.size(); i++) {
				response.add(LeapsUtils.generateJsonUser(trainersWithMostEventsCreated.get(i), token));
			}
	
			if (Configuration.debugMode) {
				logger.info("Order: /trainer/feed");
				logger.info(response.toString());
			}
			
			return response.toString();
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		}
	}
		
	/**
	 * Get user attending events method

		{
			"user_id" : 123, 
			"limit" : 20, 
			"page" : 1	
		}

	 */
	@RequestMapping(method = RequestMethod.POST, value = "/events/attending")
	public String getUserAttendingEvents(HttpServletRequest req, HttpServletResponse resp) {
		int userId = 0;
		int limit = 0;
		int page = 0;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException();
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("user_id") == null || obj.get("limit") == null || obj.get("page") == null) {
				throw new InvalidInputParamsException();
			}
			
			userId = obj.get("user_id").getAsInt();
			limit = obj.get("limit").getAsInt();
			page = obj.get("page").getAsInt();
			
			User user = DBUserDao.getInstance().getUserFromDbById(userId);
			if (user == null) {
				throw new UserException(Configuration.INVALID_USER_ID);
			}

			List<Event> pastEvents = DBUserDao.getInstance().getAllPastAttendingEventsForUser(userId, limit, page);
			List<Event> futureEvents = DBUserDao.getInstance().getAllFutureAttendingEventsForUser(userId, limit, page);
			JsonObject response = new JsonObject();

			JsonArray pastJson = new JsonArray();
			if (pastEvents != null) {
				for (int i = 0; i < pastEvents.size(); i++) {
					pastJson.add(LeapsUtils.generateJsonEvent(pastEvents.get(i), null));
				}
			}
			
			
			JsonArray futureJson = new JsonArray();
			for (int i = 0; i < futureEvents.size(); i++) {
				futureJson.add(LeapsUtils.generateJsonEvent(futureEvents.get(i), null));
			}
			
			response.add("past", pastJson);
			response.add("future", futureJson);
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, Configuration.INVALID_OR_EXPIRED_TOKEN);
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Configuration.INVALID_INPUT_PAREMETERS);
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException e) {
			return e.toString();
		}
	}
		
	/**
	 * Get user past attending events method

		{
			"user_id" : 123, 
			"limit" : 20, 
			"page" : 1	
		}

	 */
	@RequestMapping(method = RequestMethod.POST, value = "/events/attending/past")
	public String getUserAttendingPastEvents(HttpServletRequest req, HttpServletResponse resp) {
		int userId = 0;
		int limit = 0;
		int page = 0;
		String userFullName;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException();
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("user_id") == null || obj.get("limit") == null || obj.get("page") == null) {
				throw new InvalidInputParamsException();
			}
			
			userId = obj.get("user_id").getAsInt();
			limit = obj.get("limit").getAsInt();
			page = obj.get("page").getAsInt();
			
			User user = DBUserDao.getInstance().getUserFromDbById(userId);
			if (user == null) {
				throw new UserException(Configuration.INVALID_USER_ID);
			}

			List<Event> pastEvents = DBUserDao.getInstance().getAllPastAttendingEventsForUser(userId, limit, page);			
			
			JsonArray response = new JsonArray();
			if (pastEvents != null) {
				for (int i = 0; i < pastEvents.size(); i++) {
					response.add(LeapsUtils.generateJsonEvent(pastEvents.get(i), null));
				}
			}
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, Configuration.INVALID_OR_EXPIRED_TOKEN);
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Configuration.INVALID_INPUT_PAREMETERS);
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException e) {
			return e.toString();
		}
	}
	
	/**
	 * Get future user attending events method

		{
			"user_id" : 123, 
			"limit" : 20, 
			"page" : 1	
		}

	 */
	@RequestMapping(method = RequestMethod.POST, value = "/events/attending/future")
	public String getUserAttendingFutureEvents(HttpServletRequest req, HttpServletResponse resp) {
		int userId = 0;
		int limit = 0;
		int page = 0;
		String userFullName;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException();
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("user_id") == null || obj.get("limit") == null || obj.get("page") == null) {
				throw new InvalidInputParamsException();
			}
			
			userId = obj.get("user_id").getAsInt();
			limit = obj.get("limit").getAsInt();
			page = obj.get("page").getAsInt();
			
			User user = DBUserDao.getInstance().getUserFromDbById(userId);
			if (user == null) {
				throw new UserException(Configuration.INVALID_USER_ID);
			}
			
			List<Event> futureEvents = DBUserDao.getInstance().getAllFutureAttendingEventsForUser(userId, limit, page);
			
			JsonArray response = new JsonArray();
			for (int i = 0; i < futureEvents.size(); i++) {
				response.add(LeapsUtils.generateJsonEvent(futureEvents.get(i), null));
			}

			if (Configuration.debugMode) {
				logger.info("Order: /events/attending/future");
				logger.info(response.toString());
			}
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, Configuration.INVALID_OR_EXPIRED_TOKEN);
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Configuration.INVALID_INPUT_PAREMETERS);
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException e) {
			return e.toString();
		}
	}
		
	/**
	 * Get user hosting events method

		{
			"user_id" : 123, 
			"limit" : 20, 
			"page" : 1	
		}
		
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/events/hosting")
	public String getUserHostingEvents(HttpServletRequest req, HttpServletResponse resp) {
		int userId = 0;
		int limit = 0;
		int page = 0;
		String userFullName;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException();
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("user_id") == null || obj.get("limit") == null || obj.get("page") == null) {
				throw new InvalidInputParamsException();
			}
			
			userId = obj.get("user_id").getAsInt();
			limit = obj.get("limit").getAsInt();
			page = obj.get("page").getAsInt();
			
			User user = DBUserDao.getInstance().getUserFromDbById(userId);
			if (user == null) {
				throw new UserException(Configuration.INVALID_USER_ID);
			}
			
			userFullName = user.getFirstName() + " " + user.getLastName();
			
			List<Event> pastEvents = DBUserDao.getInstance().getAllPastHostingEventsForUser(userId, limit, page);
			
			List<Event> futureEvents = DBUserDao.getInstance().getAllFutureHostingEventsForUser(userId, limit, page);
			
			
			JsonObject response = new JsonObject();

			JsonArray pastJson = new JsonArray();
			for (int i = 0; i < pastEvents.size(); i++) {
				pastJson.add(LeapsUtils.generateJsonEvent(pastEvents.get(i), null));
			}
			
			JsonArray futureJson = new JsonArray();
			for (int i = 0; i < futureEvents.size(); i++) {
				futureJson.add(LeapsUtils.generateJsonEvent(futureEvents.get(i), null));
			}
			
			
			response.add("past", pastJson);
			response.add("future", futureJson);
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, Configuration.INVALID_OR_EXPIRED_TOKEN);
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Configuration.INVALID_INPUT_PAREMETERS);
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException e) {
			return e.toString();
		}
	}
		
	/**
	 * Get past user hosting events method

		{
			"user_id" : 123, 
			"limit" : 20, 
			"page" : 1	
		}

	 */
	@RequestMapping(method = RequestMethod.POST, value = "/events/hosting/past")
	public String getUserHostingPastEvents(HttpServletRequest req, HttpServletResponse resp) {
		int userId = 0;
		int limit = 0;
		int page = 0;
		String userFullName;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException();
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("user_id") == null || obj.get("limit") == null || obj.get("page") == null) {
				throw new InvalidInputParamsException();
			}
			
			userId = obj.get("user_id").getAsInt();
			limit = obj.get("limit").getAsInt();
			page = obj.get("page").getAsInt();
			
			User user = DBUserDao.getInstance().getUserFromDbById(userId);
			if (user == null) {
				throw new UserException(Configuration.INVALID_USER_ID);
			}
			
			userFullName = user.getFirstName() + " " + user.getLastName();
			
			List<Event> pastEvents = DBUserDao.getInstance().getAllPastHostingEventsForUser(userId, limit, page);
			
			JsonArray response = new JsonArray();
			for (int i = 0; i < pastEvents.size(); i++) {
				response.add(LeapsUtils.generateJsonEvent(pastEvents.get(i), null));
			}
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, Configuration.INVALID_OR_EXPIRED_TOKEN);
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Configuration.INVALID_INPUT_PAREMETERS);
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException e) {
			return e.toString();
		}
	}
		
	/**
	 * Get future user hosting events method

		{
			"user_id" : 123, 
			"limit" : 20, 
			"page" : 1	
		}

	 */
	@RequestMapping(method = RequestMethod.POST, value = "/events/hosting/future")
	public String getyUserHostingFutureEvents(HttpServletRequest req, HttpServletResponse resp) {
		int userId = 0;
		int limit = 0;
		int page = 0;
		String userFullName;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException();
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("user_id") == null || obj.get("limit") == null || obj.get("page") == null) {
				throw new InvalidInputParamsException();
			}
			
			userId = obj.get("user_id").getAsInt();
			limit = obj.get("limit").getAsInt();
			page = obj.get("page").getAsInt();
			
			User user = DBUserDao.getInstance().getUserFromDbById(userId);
			if (user == null) {
				throw new UserException(Configuration.INVALID_USER_ID);
			}
			
			userFullName = user.getFirstName() + " " + user.getLastName();
			
			List<Event> futureEvents = DBUserDao.getInstance().getAllFutureHostingEventsForUser(userId, limit, page);
			
			JsonArray response = new JsonArray();
			for (int i = 0; i < futureEvents.size(); i++) {
				response.add(LeapsUtils.generateJsonEvent(futureEvents.get(i), null));
			}
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, Configuration.INVALID_OR_EXPIRED_TOKEN);
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Configuration.INVALID_INPUT_PAREMETERS);
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException e) {
			return e.toString();
		}
	}
		
	/**
	 * 
	 * Follow user
	 * 
	 * {
	 *   "user_id" : 123
	 * }
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/follow")
	public String follow(HttpServletRequest req, HttpServletResponse resp) throws AuthorizationException, InvalidInputParamsException, IOException {
		try {
			long token = LeapsUtils.checkToken(req.getHeader("Authorization"));
			JsonObject obj = LeapsUtils.getRequestData(req);
			
			if (obj.get("user_id") == null) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			JsonObject response = UserDao.getInstance().followUser(token, obj.get("user_id").getAsInt());
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidInputParamsException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, iipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserNotFoundException unfe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, unfe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * 
	 * Unfollow user
	 * 
	 * {
	 *   "user_id" : 123
	 * }
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/unfollow")
	public String unfollow(HttpServletRequest req, HttpServletResponse resp) throws AuthorizationException, InvalidInputParamsException, IOException {
		try {
			long token = LeapsUtils.checkToken(req.getHeader("Authorization"));
			JsonObject obj = LeapsUtils.getRequestData(req);
			
			if (obj.get("user_id") == null) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			return UserDao.getInstance().unfollowUser(token, obj.get("user_id").getAsInt()).toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
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
		} catch (UserNotFoundException unfe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, unfe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Get all user followers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/followers/{user_id}")
	public String getFollowers(HttpServletRequest req, HttpServletResponse resp, @PathVariable("user_id") String user_id) {
		try {
			return UserDao.getInstance().getAllFollowers(user_id).toString();
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserNotFoundException unfe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, unfe.getMessage());
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
		}
	}

	
	/**
	 * Get all comments for a specific trainer
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/comments/{user_id}/{page}/{limit}")
	public String getTrainerComments(HttpServletRequest req, HttpServletResponse resp, @PathVariable("user_id") long user_id, 
									 @PathVariable("page") int page, @PathVariable("limit") int limit) {
		try {
			return UserDao.getInstance().getTrainerComments(user_id, page, limit).toString();
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		}
	}
}
