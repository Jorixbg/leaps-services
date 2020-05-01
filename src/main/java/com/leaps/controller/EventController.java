package com.leaps.controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.leaps.events.EventsService;
import com.leaps.web.entities.EventsRequest;
import com.leaps.web.entities.EventsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.InvalidParametersException;
import com.leaps.model.exceptions.TagException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.token.Token;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.DebuggingManager;
import com.leaps.model.utils.LeapsUtils;

@RestController
@RequestMapping("/event")
@MultipartConfig
public class EventController {
	
	private final Logger logger = LoggerFactory.getLogger(EventController.class);

	@Autowired
	EventsService eventsService;
	
	/**
	 * Create event
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/create")
	public String createEvent(HttpServletRequest req, HttpServletResponse resp) {
		try {
			return EventDao.getInstance().createEvent(LeapsUtils.checkToken(req.getHeader("Authorization")), LeapsUtils.getRequestData(req)).toString();
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
	 * Create repeating event
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/create/repeat")
	public String createRepeatingEvent(HttpServletRequest req, HttpServletResponse resp) {
		try {
			return EventDao.getInstance().createRepeatingEvent(LeapsUtils.checkToken(req.getHeader("Authorization")), LeapsUtils.getRequestData(req)).toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
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
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (EventException ee) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ee.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}  catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
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
	 * Get all feed events
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/feed/{lat_first}.{lat_second}/{lng_first}.{lng_second}")
	public String getMultipleEvents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("lat_first") String latFirstParam,
			 @PathVariable("lat_second") String latSecondParam, @PathVariable("lng_first") String lngFirstParam, @PathVariable("lng_second") String lngSecondParam) {
		try {
			return EventDao.getInstance().getFeedEvents(latFirstParam, latSecondParam, lngFirstParam, lngSecondParam, UserDao.getInstance().checkIfTokenIsValid(req.getHeader("Authorization"))).toString();
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
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ipe.getMessage());
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
	 * Get event by owner
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/owner/{ownerId}")
	@ResponseBody
	public ResponseEntity<EventsResponse> getOwnerEvents(@PathVariable("ownerId") long ownerId) {
		return ResponseEntity.ok(eventsService.getOwnerEvents(ownerId));
	}
	
	/**
	 * Get event method
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{event_id}")
	public String getAdditionalEvents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("event_id") String eventId) {
		try {
			long token = UserDao.getInstance().checkIfTokenIsValid(req.getHeader("Authorization"));
			
			JsonArray response = new JsonArray();
			response.add(LeapsUtils.generateJsonEvent(EventDao.getInstance().getEvent(eventId), token, null, null));
			
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
	 * Get the 15 most popular tags method
	 * 
	 * GET order
	 * 
	 * TOKEN is NOT required
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/tags")
	public String getEventTags(HttpServletRequest req, HttpServletResponse resp) {
		
		try {
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
			
			JsonObject response = LeapsUtils.generateJsonEvent(event, null, null, null);
			
			if (Configuration.debugMode) {
				logger.info("Order: event/attend");
				logger.info(response.toString());
			}
			
			LeapsUtils.attendEventInFirebase(user, event);
			
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
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
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
		String maxStartingDateString = null;
		int page = 1;
		int limit = Configuration.FILTER_EVENTS_DEFAULT_PAGE_LIMITATION;
		
		if (Configuration.debugMode) {
			DebuggingManager.logRequestHeaders(req, logger);
			logger.info("--------------- event/filter---------------");
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
			
			if (obj.get("all_events") != null && obj.get("all_events").getAsBoolean() == Boolean.TRUE) {
				minStartingDate = System.currentTimeMillis();
			} else if (obj.get("min_start_date") != null) {
				minStartingDate = obj.get("min_start_date").getAsLong();
			} else {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS + ": starting date");
			}
			
			if (obj.get("all_events") != null && obj.get("all_events").getAsBoolean() == Boolean.TRUE) {
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

			JsonObject response = new JsonObject();
			JsonArray filteredEventsJson = new JsonArray();
			List<Event> filteredEvents = null;
			
			if (latitude != 0.0 && longitude != 0.0) {
				filteredEvents = DBUserDao.getInstance().getFilteredEventsWithCoordinates(keyWord, latitude, longitude, distance, tags, minStartingDate, maxStartingDate, maxStartingDateString, page, limit);
				response.addProperty("total_results", DBUserDao.getInstance().getFilteredEventsCountWithCoordinates(keyWord, latitude, longitude, distance, tags, minStartingDate, maxStartingDate, page, limit));
			} else {
				filteredEvents = DBUserDao.getInstance().getFilteredEvents(keyWord, distance, tags, minStartingDate, maxStartingDate, page, limit);
				response.addProperty("total_results", DBUserDao.getInstance().getFilteredEventsCount(keyWord, distance, tags, minStartingDate, maxStartingDate, page, limit));
			}
			
			for (int i = 0; i < filteredEvents.size(); i++) {
				filteredEventsJson.add(LeapsUtils.generateJsonEvent(filteredEvents.get(i), null, latitude, longitude));
			}

			response.add("events", filteredEventsJson);
			
			if (Configuration.debugMode) {
				logger.info("------------------------------------------");
				logger.info("Order Response:");
				logger.info(response.toString());
				logger.info("------------------END---------------------");
			}
			
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
				response.add(LeapsUtils.generateJsonEvent(popularEvents.get(i), null, null, null));
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
				response.add(LeapsUtils.generateJsonEvent(responseEvents.get(i), null, latitude, longitude));
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
	 * Unfollow event
	 * 
	 * {
	 *   "event_id" : 123
	 * }
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/unfollow")
	public String unfollow(HttpServletRequest req, HttpServletResponse resp) throws AuthorizationException, InvalidInputParamsException, IOException {
		try {
			long token = LeapsUtils.checkToken(req.getHeader("Authorization"));
			JsonObject obj = LeapsUtils.getRequestData(req);
			
			if (obj.get("event_id") == null) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			EventDao.getInstance().unfollowEvent(token, obj.get("event_id").getAsInt());
			
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
		}catch (IOException ioe) {
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
			
			return EventDao.getInstance().rate(LeapsUtils.getRequestData(req), token).toString();
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
	
	// TODO: do the implementation of delete event
	@RequestMapping(method = RequestMethod.DELETE, value = "/delete")
	public String delete(HttpServletRequest req, HttpServletResponse resp) {
		try {
			LeapsUtils.checkToken(req.getHeader("Authorization"));
			EventDao.getInstance().deleteEvent(LeapsUtils.getRequestData(req));
			
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
				resp.sendError(HttpServletResponse.SC_CONFLICT, iipe.getMessage());
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
		}
	}
	
	/**
	 * Get following users for future event
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/following/future")
	public String getFutureFollowing(HttpServletRequest req, HttpServletResponse resp) {
		try {
			long token = LeapsUtils.checkToken(req.getHeader("Authorization"));
			
			boolean isFutureEvent = true;
			
			JsonArray response = EventDao.getInstance().getFollowed(token, isFutureEvent);
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
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
	 * Get following users for past event
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/following/past")
	public String getPastFollowing(HttpServletRequest req, HttpServletResponse resp) {
		try {
			long token = LeapsUtils.checkToken(req.getHeader("Authorization"));
			
			boolean isFutureEvent = false;
			
			JsonArray response = EventDao.getInstance().getFollowed(token, isFutureEvent);
			
			return response.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
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
	 * Get all comments of an event
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/comments/{event_id}/{page}/{limit}")
	public String getComments(HttpServletRequest req, HttpServletResponse resp, @PathVariable("event_id") long eventId, 
							  @PathVariable("page") int page, @PathVariable("limit") int limit) throws EventException {
		try {
			return EventDao.getInstance().getComments(eventId, page, limit).toString();
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, iipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}

	/**
	 * Update event
	 * 
	 * {
     *   "title" : "trenirovchitsa",
     *   "description" : "Nai-ultra mega huper yakata trenirovka ever izmislyana", 
     *   "date" : "6226623422532",
     *   "time_from" : "6226623422532",
     *   "time_to" : "6226623422532",
     *   "coord_lat" : 42.693351,
     *   "coord_lnt" : 23.340381,
     *   "price_from" : 10,
     *   "address" : "ул. „Шипка“ 34-36, 1504 София",
     *   "free_slots" : 50,
     *   "date_created" : "1365475684564",
     *   "tags" : [ "tag1", "tag2", "tag3" ... ]
	 * }
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/update")
	public String update(HttpServletRequest req, HttpServletResponse resp) {
		try {
			long token = LeapsUtils.checkToken(req.getHeader("Authorization"));
			EventDao.getInstance().update(LeapsUtils.getRequestData(req), token);

			return HttpStatus.OK.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
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
		} catch (InvalidParametersException iipe) {
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
		}
	}
	
	/**
	 * Get all event attendees
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/attendees/{event_id}")
	public String getAllAtendees(HttpServletRequest req, HttpServletResponse resp, @PathVariable("event_id") String event_id) {
		try {
			return EventDao.getInstance().getAllAttendees(event_id, req.getHeader("Authorization")).toString();
		} catch (InvalidParametersException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, iipe.getMessage());
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
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Get a comment
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/comment/{comment_id}")
	public String getComment(HttpServletRequest req, HttpServletResponse resp, @PathVariable("comment_id") long comment_id) {
		try {
			return EventDao.getInstance().getComment(comment_id).toString();
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
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (TagException te) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, te.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
}
