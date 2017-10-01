package com.leaps.controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.InvalidCredentialsException;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.InvalidParametersException;
import com.leaps.model.exceptions.TagException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.token.Token;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.LeapsUtils;

@RestController
@RequestMapping("/event")
@MultipartConfig
public class EventController {
	
	private final Logger logger = LoggerFactory.getLogger(EventController.class);

	/**
	 * Event create method
	 * 
		{
		    "title" : "trenirovchitsa",
		    "description" : "Nai-ultra mega huper yakata trenirovka ever izmislyana", 
		    "event_image_url" : "some url",
		    "address" : "ул. „Шипка“ 34-36, 1504 София",
		    "coord_lat" : 42.693351,
		    "coord_lnt" : 23.340381,
		    "price_from" : 10,
		    "free_slots" : 50,
		    "date" : "6226623422532",
		    "time_from" : "6226623422532",
		    "time_to" : "6226623422532",
		    
		    "owner_id" : 1,
		    "owner_name" : "Mityo Krika",
		    "owner_image_url" : "nyakakvo url",
		    "specialities" : [ "yoga", "running", "jogging" ],
		    
		    "date_created" : "1365475684564"
		}
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/create")
	public String createEvent(HttpServletRequest req, HttpServletResponse resp) {
		String title;
		String description;
		String address;
		double latitude;
		double longitute;
		int priceFrom;
		int freeSlots;
		long date;
		long timeFrom;
		long timeTo;
		List<String> tags = new ArrayList<String>();
		
		long ownerId = -1;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException();
			}
			
			long checkedToken = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checkedToken) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(checkedToken);

			if (cachedUser == null || cachedUser.isEmpty()) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}

			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			
			String requestData = sb.toString();

			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("title") == null || obj.get("description") == null || obj.get("address") == null || obj.get("coord_lat") == null ||
					obj.get("coord_lnt") == null || obj.get("price_from") == null || obj.get("free_slots") == null || obj.get("date") == null ||
					obj.get("time_from") == null || obj.get("time_to") == null || obj.get("owner_id") == null) {
				throw new InvalidCredentialsException(Configuration.ERROR_WHILE_CREATING_NEW_EVENT);
			}
			
			Iterator<Map.Entry<Token, User>> it = cachedUser.entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<Token, User> pair = it.next();
			    ownerId = pair.getValue().getUserId();
			}
			
			if (ownerId != obj.get("owner_id").getAsLong()) {
				throw new InvalidCredentialsException("The owner's id does not match with the one on the server!");
			}
			
			title = obj.get("title").getAsString();
			description = obj.get("description").getAsString();
			address = obj.get("address").getAsString();
			latitude = obj.get("coord_lat").getAsDouble();
			longitute = obj.get("coord_lnt").getAsDouble();
			priceFrom = obj.get("price_from").getAsInt();
			freeSlots = obj.get("free_slots").getAsInt();
			date = obj.get("date").getAsLong();
			timeFrom = obj.get("time_from").getAsLong();
			timeTo = obj.get("time_to").getAsLong();
			
			
			if (obj.get("tags") != null) {
				JsonElement jsonTags = obj.get("tags");
				Type listType = new TypeToken<List<String>>() {}.getType();

				tags = new Gson().fromJson(jsonTags, listType);
			}
			
			Event event = EventDao.getInstance().createNewEvent(title, description, address, latitude, longitute, priceFrom, freeSlots, date, 
																timeFrom, timeTo, tags, ownerId);
			
			if (event == null) {
				// TODO: write proper exception here
				throw new EventException();
			}
			
			JsonObject response = new JsonObject();
			response.addProperty("event_id", event.getEventId());
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (EventException ex) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
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
	 * Get all feed events method
	 * 
	 * GET order
	 * 
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/feed/{lat_first}.{lat_second}/{lng_first}.{lng_second}")
	public String getMultipleEvents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("lat_first") String latFirstParam,
			 @PathVariable("lat_second") String latSecondParam, @PathVariable("lng_first") String lngFirstParam, @PathVariable("lng_second") String lngSecondParam) {
		
		try {
			// default value for feed
			final int page = 1;
			int limit = Configuration.EVENTS_RETURN_SIZE_IN_FEED;
			
			JsonObject response = new JsonObject();
	
			JsonArray popular = new JsonArray();
			List<Event> popularEvents = DBUserDao.getInstance().getMostPopularEvents(page, limit);
			for (int i = 0; i < popularEvents.size(); i++) {
				popular.add(LeapsUtils.generateJsonEvent(popularEvents.get(i)));
			}
			
			response.add("popular", popular);
	
			JsonArray nearby = new JsonArray();
			
			double latitude;
			double longitude;
			
			try {
				if (latFirstParam == null || latFirstParam.isEmpty() || latSecondParam == null || latSecondParam.isEmpty() ||
					lngFirstParam == null || lngFirstParam.isEmpty() || lngSecondParam == null || lngSecondParam.isEmpty()) {
					throw new InvalidParametersException(Configuration.INVALID_INPUT_PAREMETERS);
				}
				
				// TODO: If some of the coordinate values equals 'na' string parameter, then we assume that they will not be presented so we return the popular events instead
				// TODO: revise in the following stages
				if (!latFirstParam.equals(Configuration.NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS) || !latSecondParam.equals(Configuration.NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS) ||
					!lngFirstParam.equals(Configuration.NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS) || !lngSecondParam.equals(Configuration.NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS)) {
					
					latitude = Double.valueOf(latFirstParam + "." + latSecondParam);
					longitude = Double.valueOf(lngFirstParam + "." + lngSecondParam);
					
					List<Event> responseEvents = DBUserDao.getInstance().getNearbyUpcommingEvents(latitude, longitude, page, limit);
					
					for (int i = 0; i < responseEvents.size(); i++) {
						nearby.add(LeapsUtils.generateJsonEvent(responseEvents.get(i)));
					}
				}
			} catch (InvalidParametersException ipe) {
				try {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ipe.getMessage());
					return null;
				} catch (IOException ioe) {
					return null;
				}
			}
			
			
			response.add("nearby", nearby);
			
			// TODO: currently using the most popular events logic
			// TODO: revise in later stages
			JsonArray suited = new JsonArray();
			for (int i = 0; i < popularEvents.size(); i++) {
				suited.add(LeapsUtils.generateJsonEvent(popularEvents.get(i)));
			}
			response.add("suited", suited);
			
			if (Configuration.debugMode) {
				logger.info("Order: /feed/{lat_first}.{lat_second}/{lng_first}.{lng_second}");
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
		}
	}
	
	/**
	 * 
	 * Get event method
	 * 
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{event_id}")
	public String getAdditionalEvents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("event_id") String eventId) {
		try {
			JsonArray response = new JsonArray();
			response.add(LeapsUtils.generateJsonEvent(EventDao.getInstance().getEvent(eventId)));
			
			if (Configuration.debugMode) {
				logger.info("Order: /{event_id}");
				logger.info(response.toString());
			}
			
			return response.toString();
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (EventException ex) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
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
		}
	}
	
	/**
	 * Get the 15 most popular tags method
	 * 
	 * GET order
	 * 
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/tags")
	public String getEventTags(HttpServletRequest req, HttpServletResponse resp) {
		
		List<String> tags = DBUserDao.getInstance().getMostPopularTags();
		List<String> unusedTags = DBUserDao.getInstance().getTagsFromTheDB();
		
		if ((tags == null || tags.isEmpty()) && (unusedTags == null || unusedTags.isEmpty())) {
			try {
				throw new TagException(Configuration.CANNOT_RETRIEVE_TAGS_FROM_SERVER);
			} catch (TagException te) {
				try {
					resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
					return null;
				} catch (IOException e1) {
					return null;
				}
			}
		}

		JsonArray response = new JsonArray();
		
		if (tags.size() < Configuration.TAG_SELECT_LIMIT) {
			for (int i = 0; i < unusedTags.size(); i++) {
				if (!tags.contains(unusedTags.get(i))) {
					for (int o = 0; o < tags.size(); o++) {
						if (tags.get(o).equalsIgnoreCase(unusedTags.get(i))) {
							break;
						}
						
						if (o + 1 == tags.size()) {
							tags.add(unusedTags.get(i));
						}
					}
				}
				
				if (tags.size() == Configuration.TAG_SELECT_LIMIT) {
					break;
				}
			}
		}
		
		for (int i = 0; i < tags.size(); i++) {
			response.add(tags.get(i));
		}

		return response.toString();
	}
	
	/**
	 * Attend event method
	 * 
	 * { "user_id" : 13, "event_id" : 341 }
	 * 
	 * TOKEN IS required
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/attend")
	public String attendEvent(HttpServletRequest req, HttpServletResponse resp) {
		long userId;
		long eventId;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			String token = req.getHeader("Authorization");
			User user = null;
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}

			long checker = Long.valueOf(token);
			
			Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(checker);
			
			if (cachedUser == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}

			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();

			if (obj.get("user_id") == null || obj.get("event_id") == null) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}

			userId = obj.get("user_id").getAsLong();
			eventId = obj.get("event_id").getAsLong();
			
			for(Map.Entry<Token, User> map : cachedUser.entrySet()) {
				if (map.getValue().getUserId() != userId) {
					throw new InvalidInputParamsException(Configuration.NO_USER_FOUND);
				} else {
					user = map.getValue();
					break;
				}
			}
			
			Event event = DBUserDao.getInstance().getEventById(eventId);
			if (event == null) {
				throw new EventException(Configuration.INVALID_EVENT_ID);
			}
			
			int eventAttendees = DBUserDao.getInstance().getEventAttendeesNumber(eventId);
			
			if (eventAttendees < 0 || event.getFreeSlots() <= eventAttendees) {
				throw new EventException(Configuration.EVENT_ATTENDEE_LIMIT_IS_REACHED);
			}
			
			if (DBUserDao.getInstance().checkIfUserAlreadyAttendsAnEvent(userId, eventId)) {
				throw new EventException(Configuration.USER_ALREADY_ATTENDS_TO_THE_EVENT);
			}
			
			DBUserDao.getInstance().addAttendeeForEvent(userId, eventId);
			
			JsonObject response = LeapsUtils.generateJsonEvent(event);
			
			if (Configuration.debugMode) {
				logger.info("Order: event/attend");
				logger.info(response.toString());
			}
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ipe.getMessage());
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
	 * Unattend event method
	 * 
	 * { "user_id" : 13, "event_id" : 341 }
	 * 
	 * TOKEN IS required
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/unattend")
	public String dummyUnattendEvent(HttpServletRequest req, HttpServletResponse resp) {
		long userId;
		long eventId;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			String token = req.getHeader("Authorization");

			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}

			long checker = Long.valueOf(token);

			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String requestData = sb.toString();

			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();

			if (obj.get("user_id") == null || obj.get("event_id") == null) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}

			userId = obj.get("user_id").getAsLong();
			eventId = obj.get("event_id").getAsLong();

			if (!DBUserDao.getInstance().unattendUserFromEvent(userId, eventId)) {
				throw new EventException(Configuration.CANNOT_REMOVE_USER_FROM_EVENT);
			}
			
			return HttpStatus.OK.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ipe.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ee.getMessage());
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
	 * Filter Events method
	 * 
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter")
	public String filterEvents(HttpServletRequest req, HttpServletResponse resp) {
		String keyWord = null;
		double latitude = 0.0;
		double longitude = 0.0;
		int distance = Configuration.USER_DEFAULT_MAX_DISTANCE_SETTING;
		List<String> tags = new ArrayList<String>();
		long minStartingDate = -1;
		long maxStartingDate = -1;
		int page = 1;
		int limit = Configuration.FILTER_EVENTS_DEFAULT_PAGE_LIMITATION;
				
		try (Scanner sc = new Scanner(req.getInputStream())) {
			
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}

			String requestData = sb.toString();

			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("key_words") != null) {
				keyWord = obj.get("key_words").getAsString();
			}
			
			if (obj.get("my_lnt") != null && obj.get("my_lat") != null) {
				latitude = obj.get("my_lat").getAsDouble();
				longitude = obj.get("my_lnt").getAsDouble();
			}
			
			if (obj.get("distance") != null) {
				distance = obj.get("distance").getAsInt();
			}
			
			if (obj.get("tags") != null) {
				JsonElement jsonTags = obj.get("tags");
				Type listType = new TypeToken<List<String>>() {}.getType();

				tags = new Gson().fromJson(jsonTags, listType);
			}
			
			if (obj.get("min_start_date") != null) {
				minStartingDate = obj.get("min_start_date").getAsLong();
			} else {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS + ": starting date");
			}
			
			if (obj.get("max_start_date") != null) {
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

			JsonObject response = new JsonObject();
			JsonArray filteredEventsJson = new JsonArray();
			List<Event> filteredEvents = null;
			
			if (latitude != 0.0 && longitude != 0.0) {
				filteredEvents = DBUserDao.getInstance().getFilteredEventsWithCoordinates(keyWord, latitude, longitude, distance, tags, minStartingDate, maxStartingDate, page, limit);
				response.addProperty("total_results", DBUserDao.getInstance().getFilteredEventsCountWithCoordinates(keyWord, latitude, longitude, distance, tags, minStartingDate, maxStartingDate, page, limit));
			} else {
				filteredEvents = DBUserDao.getInstance().getFilteredEvents(keyWord, distance, tags, minStartingDate, maxStartingDate, page, limit);
				response.addProperty("total_results", DBUserDao.getInstance().getFilteredEventsCount(keyWord, distance, tags, minStartingDate, maxStartingDate, page, limit));
			}
			
			for (int i = 0; i < filteredEvents.size(); i++) {
				filteredEventsJson.add(LeapsUtils.generateJsonEvent(filteredEvents.get(i)));
			}

			response.add("events", filteredEventsJson);

			return response.toString();
		} catch (InvalidInputParamsException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ipe.getMessage());
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
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
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
	 * Get most popular events method
	 *  
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.GET, value = "popular/{page}")
	public String getPopularEvents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("page") String pageNumber) {
		int page;
		int limit = Configuration.MOST_POPULAR_EVENTS_RETURN_SIZE;
		
		try {

			if (pageNumber == null || pageNumber.isEmpty() || !LeapsUtils.isNumber(pageNumber)) {
				throw new InvalidParametersException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			page = Integer.valueOf(pageNumber);
			
			List<Event> popularEvents = DBUserDao.getInstance().getMostPopularEvents(page, limit);
			
			JsonArray response = new JsonArray();
			
			for (int i = 0; i < popularEvents.size(); i++) {
				response.add(LeapsUtils.generateJsonEvent(popularEvents.get(i)));
			}

			return response.toString();
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ipe.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		}
	}
		
	/**
	 * Get suited events method
	 * 
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.GET, value = "suited/{page}")
	public String getSuitedEvents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("page") String pageNumber) {
		try {
			
			if (pageNumber == null || pageNumber.isEmpty() || !LeapsUtils.isNumber(pageNumber)) {
				throw new InvalidParametersException(Configuration.INVALID_INPUT_PAREMETERS);
			}

			// TODO: revise in next stages
			String tempSuitedEvents = getPopularEvents(req, resp, pageNumber);
			
			return tempSuitedEvents;
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ipe.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		}
	}
	
	/**
	 * Get nearby events method
	 * 
	 * GET order
	 * 
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.GET, value = "nearby/{page}/{lat_first}.{lat_second}/{lng_first}.{lng_second}")
	public String getNearbyEvents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("page") String pageNumber, @PathVariable("lat_first") String latFirstParam,
								 @PathVariable("lat_second") String latSecondParam, @PathVariable("lng_first") String lngFirstParam, @PathVariable("lng_second") String lngSecondParam) {
		int page;
		int limit = Configuration.NEARBY_EVENTS_RETURN_SIZE;
		double latitude;
		double longitude;
		
		try {
			
			if (pageNumber == null || pageNumber.isEmpty() || !LeapsUtils.isNumber(pageNumber)) {
				throw new InvalidParametersException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			page = Integer.valueOf(pageNumber);
			
			if (latFirstParam == null || latFirstParam.isEmpty() || latSecondParam == null || latSecondParam.isEmpty() ||
				lngFirstParam == null || lngFirstParam.isEmpty() || lngSecondParam == null || lngSecondParam.isEmpty()) {
				throw new InvalidParametersException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			// TODO: If some of the coordinate values equals 'na' string parameter, then we assume that they will not be presented so we return the popular events instead
			// TODO: revise in the following stages
			if (latFirstParam.equals(Configuration.NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS) || latSecondParam.equals(Configuration.NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS) ||
				lngFirstParam.equals(Configuration.NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS) || lngSecondParam.equals(Configuration.NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS)) {
				return getPopularEvents(req, resp, pageNumber);
			}
			
			latitude = Double.valueOf(latFirstParam + "." + latSecondParam);
			longitude = Double.valueOf(lngFirstParam + "." + lngSecondParam);
			
			List<Event> responseEvents = DBUserDao.getInstance().getNearbyUpcommingEvents(latitude, longitude, page, limit);

			JsonArray response = new JsonArray();
			
			for (int i = 0; i < responseEvents.size(); i++) {
				response.add(LeapsUtils.generateJsonEvent(responseEvents.get(i)));
			}
			
			return response.toString();
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ipe.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		}
	}
	
	/**
	 * 
	 * Follow event
	 * 
	 * {
	 *   "event_id" : 123
	 * }
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/follow")
	public String follow(HttpServletRequest req, HttpServletResponse resp) throws AuthorizationException, InvalidInputParamsException, IOException {
		try {
			long token = LeapsUtils.checkToken(req.getHeader("Authorization"));
			JsonObject obj = LeapsUtils.getRequestData(req);
			
			if (obj.get("event_id") == null) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			JsonObject response = EventDao.getInstance().followEvent(token, obj.get("event_id").getAsInt());
			
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
	 * Rate event
	 *
	 *   {
  	 *		"event_id" : 20,
  	 *		"rating" : 5,
  	 *		"comment" : "This is an awsome training",
  	 *		"date_created" : 1505241432428
	 *	 }
	 *
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/rate")
	public String rate(HttpServletRequest req, HttpServletResponse resp) {
		try {
			long token = LeapsUtils.checkToken(req.getHeader("Authorization"));
			EventDao.getInstance().rate(LeapsUtils.getRequestData(req), token);
			
			return HttpStatus.OK.toString();
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
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ee.getMessage());
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
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
}
