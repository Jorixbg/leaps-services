package com.leaps.model.event;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.leaps.interfaces.IEventDao;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.exceptions.InvalidCredentialsException;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.InvalidParametersException;
import com.leaps.model.exceptions.TagException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.rate.Rate;
import com.leaps.model.rate.RateDao;
import com.leaps.model.rate.RateDao.RateDaoEnum;
import com.leaps.model.token.Token;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.LeapsUtils;

public class EventDao implements IEventDao {
	
	private static EventDao instance = null;
	private RateDaoEnum rateDao;
	
	protected EventDao() {
		rateDao = RateDao.RateDaoEnum.INSTANCE;
	}

	public static EventDao getInstance() {
		if (instance == null) {
			instance = new EventDao();
		}
		return instance;
	}
	
	public Tag createNewTag(int specialityId, String name) {
		return new Tag(specialityId, name);
	}
	
	private long getUserId(long token) {
		Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(token);
		long userId = -1;
		if (cachedUser == null) {
			throw new UserNotFoundException(Configuration.USER_DOES_NOT_EXIST);
		}
		
		for (Map.Entry<Token, User> map : cachedUser.entrySet()) {
			userId = map.getValue().getUserId();
		}
		
		return userId;
	}
	
	public Tag createNewTag(int specialityId, String name, long ownerId) {
		Tag tag = new Tag(specialityId, name);
		tag.setOwnerId(ownerId);
		return tag;
	}
	
	public Event generateNewEvent(long eventId, String title, String description, long date, long timeFrom, long timeTo, long ownerId, String eventImageUrl, 
			double latitude, double longitute, int priceFrom, String address, int freeSlots,  long dateCreated) {
		return new Event(eventId, title, description, date, timeFrom, timeTo, ownerId, eventImageUrl, latitude, longitute, priceFrom, address, freeSlots, dateCreated);
	}
	
	public Event createNewEvent(String title, String description, String address, double latitude, double longitute,
			int priceFrom, int freeSlots, long date, long timeFrom, long timeTo, List<String> tags, long ownerId) throws EventException {
		Event event = null;
		
		Long dateCreated = System.currentTimeMillis();

		// add the event to the DB and return its id
		long eventId = DBUserDao.getInstance().createNewEvent(title, description, date, timeFrom, timeTo, ownerId, latitude, longitute, priceFrom, address, freeSlots, dateCreated);
		
		if (eventId >= 0) {
			boolean tagsInsert = true;
			
			if (tags != null && !tags.isEmpty()) {
				tagsInsert = DBUserDao.getInstance().addTagsToTheDB(tags, eventId);
			}
			
			if (tagsInsert) {
				event = new Event(eventId, title, description, date, timeFrom, timeTo, ownerId, null, latitude, longitute, priceFrom, address, freeSlots, dateCreated);
			}
		}
		
		return event;
	}

	public List<Tag> getAllUserSpecialties(long userId) throws TagException {
		return DBUserDao.getInstance().getAllUserSpecialtiesFromDb(userId);
	}

	public JsonObject followEvent(long token, int eventId) throws UserException, EventException, ImageException, TagException {
		Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(token);
		
		if (cachedUser == null) {
			throw new UserNotFoundException(Configuration.NO_USER_FOUND);
		}
		
		User user = null;
		
		for (Map.Entry<Token, User> map : cachedUser.entrySet()) {
			user = map.getValue();
		}
		
		DBUserDao.getInstance().followEvent(user.getUserId(), eventId);
		
		return LeapsUtils.generateJsonEvent(DBUserDao.getInstance().getEventById(eventId), user.getUserId());
	}

	public void unfollowEvent(long token, int eventId) throws EventException {
		Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(token);
		
		if (cachedUser == null) {
			throw new UserNotFoundException(Configuration.NO_USER_FOUND);
		}
		
		User user = null;
		
		for (Map.Entry<Token, User> map : cachedUser.entrySet()) {
			user = map.getValue();
		}
		
		DBUserDao.getInstance().unfollowEvent(user.getUserId(), eventId);
	}
	
	public JsonObject rate(JsonObject requestData, long token) throws InvalidInputParamsException, EventException, UserNotFoundException {
		long userId = -1;

		Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(token);
		
		if (cachedUser == null) {
			throw new UserNotFoundException(Configuration.USER_DOES_NOT_EXIST);
		}
		
		for (Map.Entry<Token, User> map : cachedUser.entrySet()) {
			userId = map.getValue().getUserId();
		}
		
		if (requestData.get("event_id") == null || requestData.get("rating") == null || requestData.get("comment") == null || requestData.get("date_created") == null) {
			throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
		}
		
		Rate rate = rateDao.createRate(requestData.get("event_id").getAsLong(), requestData.get("rating").getAsInt(), 
									   userId, requestData.get("comment").getAsString(), requestData.get("date_created").getAsLong());

		if (rate.getRating() < Configuration.MIN_RATING || rate.getRating() > Configuration.MAX_RATING) {
			throw new InvalidInputParamsException(Configuration.INVALID_RATING_VALUE);
		}
		
		// check if event exist
		DBUserDao.getInstance().getEventById(rate.getEventId());
		long rateId = DBUserDao.getInstance().rateEvent(rate);
		
		JsonObject response = new JsonObject();
		response.addProperty("rate_id", rateId);
		
		return response;
	}

	public Event getEvent(String eventId) throws InvalidParametersException, EventException {
		if (eventId == null || !LeapsUtils.isNumber(eventId)) {
			throw new InvalidParametersException(Configuration.INVALID_EVENT_ID);
		}
		
		Event event = DBUserDao.getInstance().getEventById(Long.valueOf(eventId));
		
		if (event == null) {
			throw new EventException(Configuration.EVENT_DOES_NOT_EXIST_OR_CANNOT_BE_RETREIVED);
		}
		
		return event;
	}

	public void deleteEvent(JsonObject obj) throws InvalidInputParamsException, EventException {
		if (obj.get("event_id") == null || !LeapsUtils.isNumber(obj.get("event_id").getAsString())) {
			throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
		}
		
		long eventId = obj.get("event_id").getAsLong();
		DBUserDao.getInstance().deleteEvent(eventId);
	}

	public JsonArray getFollowed(long token, boolean isFuture) throws UserException, EventException, ImageException, TagException {
		long userId = getUserId(token);
		
		List<Event> events = DBUserDao.getInstance().getAllFollowedEvents(userId, isFuture);
		
		JsonArray response = new JsonArray();
		for (int i = 0; i < events.size(); i++) {
			response.add(LeapsUtils.generateJsonEvent(events.get(i), token));
		}
		
		return response;
	}

	public JsonArray getComments(long eventId, int page, int limit) throws EventException, UserException, InvalidParametersException {
		int pageLimit = Configuration.DEFAULT_PAGE_COMMENTS_LIMIT;
		JsonArray rateJson = new JsonArray();
		
		if (page == 0) {
			throw new InvalidParametersException(Configuration.INVALID_PAGE_NUMBER);
		}
		
		if (limit != 0) {
			pageLimit = limit;
		}
		
		List<Rate> rates = DBUserDao.getInstance().getRatesForEvent(eventId, page, pageLimit);
		
		for (int i = 0; i < rates.size(); i++) {
			JsonObject obj = new JsonObject();
			Rate rate = rates.get(i);
			User user = UserDao.getInstance().getUserFromDbOrCacheById(rate.getUserId());
			obj.addProperty("comment_id", rate.getCommentId());
			obj.addProperty("user_id", rate.getUserId());
			obj.addProperty("rating", rate.getRating());
			obj.addProperty("event_owner_name", (user.getFirstName() + " " + user.getLastName()).trim());
			obj.addProperty("event_owner_image", user.getProfileImageUrl());
			obj.addProperty("comment", rate.getComment());
			obj.addProperty("date_created", rate.getDateCreated());
			obj.addProperty("comment_image", rate.getImageUrl());
			rateJson.add(obj);
		}
		
		return rateJson;
	}

	public void update(JsonObject requestData, long token) throws InvalidParametersException, EventException {
		
		if (requestData.get("event_id") == null) {
			throw new InvalidParametersException(Configuration.EVENT_ID_PARAM_IS_REQUIRED);
		}
		
		Map<String, Map<String, Object>> params = new HashMap<String, Map<String, Object>>();
		List<String> tags = new ArrayList<String>();
		long eventId = requestData.get("event_id").getAsLong();
		
		if (requestData.get("title") != null) {
			params.put("title", new HashMap<String, Object>());
			params.get("title").put("string", requestData.get("title").getAsString());
		}
		
		if (requestData.get("description") != null) {
			params.put("description", new HashMap<String, Object>());
			params.get("description").put("string", requestData.get("description").getAsString());
		}
		
		if (requestData.get("date") != null) {
			params.put("date", new HashMap<String, Object>());
			params.get("date").put("string", requestData.get("date").getAsString());
		}
		
		if (requestData.get("time_from") != null) {
			params.put("time_from", new HashMap<String, Object>());
			params.get("time_from").put("string", requestData.get("time_from").getAsString());
		}
		
		if (requestData.get("time_to") != null) {
			params.put("time_to", new HashMap<String, Object>());
			params.get("time_to").put("string", requestData.get("time_to").getAsString());
		}
		
		if (requestData.get("coord_lat") != null) {
			params.put("coord_lat", new HashMap<String, Object>());
			params.get("coord_lat").put("string", requestData.get("coord_lat").getAsString());
		}
		
		if (requestData.get("coord_lnt") != null) {
			params.put("coord_lnt", new HashMap<String, Object>());
			params.get("coord_lnt").put("string", requestData.get("coord_lnt").getAsString());
		}
		
		if (requestData.get("price_from") != null) {
			params.put("price_from", new HashMap<String, Object>());
			params.get("price_from").put("string", requestData.get("price_from").getAsString());
		}
		
		if (requestData.get("address") != null) {
			params.put("address", new HashMap<String, Object>());
			params.get("address").put("string", requestData.get("address").getAsString());
		}
		
		if (requestData.get("free_slots") != null) {
			params.put("free_slots", new HashMap<String, Object>());
			params.get("free_slots").put("string", requestData.get("free_slots").getAsString());
		}
		
		if (requestData.get("tags") != null) {
			JsonElement jsonTags = requestData.get("tags");
			Type listType = new TypeToken<List<String>>() {}.getType();

			tags = new Gson().fromJson(jsonTags, listType);
		}
		
		DBUserDao.getInstance().updateEvent(params, eventId);
		
		DBUserDao.getInstance().removeEventTagsFromDb(eventId);
		DBUserDao.getInstance().addTagsToTheDB(tags, eventId);
	}

	public JsonObject getAllAttendees(String eventId, String token) throws InvalidInputParamsException, InvalidParametersException, EventException, UserException {
		Event event = EventDao.getInstance().getEvent(eventId);
		
		Long checker = null;
		if (token != null && LeapsUtils.isNumber(token)) {
			checker = Long.valueOf(token);
		}
		
		Map<String, Object> attendees = EventDao.getInstance().getAllEventAttendees(event, checker);
		
		return (JsonObject) attendees.get("json");
	}

	public Map<String, Object> getAllEventAttendees(Event event, Long token) throws EventException, UserException {
		Map<String, Object> response = new HashMap<String, Object>();
		JsonObject attendingJsonObject = new JsonObject();
		JsonArray followed = new JsonArray();
		JsonArray others = new JsonArray();
				
		List<User> attending = DBUserDao.getInstance().getAllAttendingUsersForEvent(event.getEventId());
		if (attending.size() > 0) {
			User currentUser = null;
			List<Long> followedUsers = null;
			
			if (token != null) {
				Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(token);
				if (cachedUser != null) {
					for(Map.Entry<Token, User> map : cachedUser.entrySet()) {
						currentUser = map.getValue();
					}
				}
			}
			
			if (currentUser != null) {
				followedUsers = DBUserDao.getInstance().getEventFollowingUsers(currentUser.getUserId(), attending);
			} else {
				followedUsers = new ArrayList<Long>();
			}

			for (int i = 0; i < attending.size(); i++) {
				boolean other = true;
				JsonObject tempUser = new JsonObject();
				tempUser.addProperty("user_id", attending.get(i).getUserId());
				tempUser.addProperty("user_name", attending.get(i).getFirstName() + " " + attending.get(i).getLastName());
				tempUser.addProperty("user_image_url", attending.get(i).getProfileImageUrl());
				
				
				for (int k = 0; k < followedUsers.size(); k++) {
					long tempValue = followedUsers.get(k);
					if (attending.get(i).getUserId() == tempValue) {
						other = false;
						break;
					}
				}
				
				if (other) {
					others.add(tempUser);
				} else {
					followed.add(tempUser);
				}
			}
		}
		
		attendingJsonObject.add("followed", followed);
		attendingJsonObject.add("others", others);
		
		response.put("json", attendingJsonObject);
		response.put("attendees", attending);
		
		return response;
	}

	public JsonObject getComment(long commentId) throws InvalidInputParamsException, EventException, UserException, TagException {
		return LeapsUtils.generateJsonComment(commentId);
	}

	public JsonObject createEvent(long token, JsonObject obj) throws AuthorizationException, EventException {
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
					
		if (UserDao.getInstance().getUserFromCache(token) == null) {
			throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
		}
		
		Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(token);

		if (cachedUser == null || cachedUser.isEmpty()) {
			throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
		}
		
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
			throw new EventException(Configuration.CANNOT_CREATE_NEW_EVENT);
		}
		
		JsonObject response = new JsonObject();
		response.addProperty("event_id", event.getEventId());
		
		return response;
	}

	public JsonObject getFeedEvents(String latFirstParam, String latSecondParam, String lngFirstParam, String lngSecondParam, long token) throws UserException, EventException, InvalidParametersException, ImageException, TagException {
		// default value for feed
		final int page = 1;
		int limit = Configuration.EVENTS_RETURN_SIZE_IN_FEED;
		
		JsonObject response = new JsonObject();

		JsonArray popular = new JsonArray();
		List<Event> popularEvents = DBUserDao.getInstance().getMostPopularEvents(page, limit);
		for (int i = 0; i < popularEvents.size(); i++) {
			popular.add(LeapsUtils.generateJsonEvent(popularEvents.get(i), token));
		}
		
		response.add("popular", popular);

		JsonArray nearby = new JsonArray();
		
		double latitude;
		double longitude;
		
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
				nearby.add(LeapsUtils.generateJsonEvent(responseEvents.get(i), token));
			}
		}
		
		
		response.add("nearby", nearby);
		
		// TODO: currently using the most popular events logic
		// TODO: revise in later stages
		JsonArray suited = new JsonArray();
		for (int i = 0; i < popularEvents.size(); i++) {
			suited.add(LeapsUtils.generateJsonEvent(popularEvents.get(i), token));
		}
		response.add("suited", suited);
		
		return response;
	}
}
