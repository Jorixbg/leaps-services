package com.leaps.model.event;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.leaps.interfaces.IEventDao;
import com.leaps.model.bean.RepeatingEvent;
import com.leaps.model.bean.RepeatingEventTime;
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
			double latitude, double longitute, int priceFrom, String address, int freeSlots,  long dateCreated, String eventFirebaseTopic) {
		return new Event(eventId, title, description, date, timeFrom, timeTo, ownerId, eventImageUrl, latitude, longitute, priceFrom, address, freeSlots, dateCreated, eventFirebaseTopic);
	}
	
	public Event createNewEvent(String title, String description, String address, double latitude, double longitute,
			int priceFrom, int freeSlots, long date, long timeFrom, long timeTo, List<String> tags, long ownerId, 
			String eventFirebaseTopic) throws EventException {
		Event event = null;
		
		Long dateCreated = System.currentTimeMillis();

		// add the event to the DB and return its id
		long eventId = DBUserDao.getInstance().createNewEvent(title, description, date, timeFrom, timeTo, ownerId, latitude, longitute, priceFrom, address, freeSlots, dateCreated, eventFirebaseTopic);
		
		if (eventId >= 0) {
			boolean tagsInsert = true;
			
			if (tags != null && !tags.isEmpty()) {
				tagsInsert = DBUserDao.getInstance().addTagsToTheDB(tags, eventId);
			}
			
			if (tagsInsert) {
				event = new Event(eventId, title, description, date, timeFrom, timeTo, ownerId, null, latitude, longitute, priceFrom, address, freeSlots, dateCreated, eventFirebaseTopic);
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
		
		return LeapsUtils.generateJsonEvent(DBUserDao.getInstance().getEventById(eventId), user.getUserId(), null, null);
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

	public void deleteEvent(JsonObject obj) throws InvalidInputParamsException, EventException, UserException {
		if (obj.get("event_id") == null || !LeapsUtils.isNumber(obj.get("event_id").getAsString())) {
			throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
		}
		
		long eventId = obj.get("event_id").getAsLong();
		
		Event event = DBUserDao.getInstance().getEventById(eventId);
		
		String eventName = event.getTitle();
		long ownerId = event.getOwnerId();
		User owner = DBUserDao.getInstance().getUserFromDbById(ownerId);
		String ownerName = owner.getFirstName() + " " + owner.getLastName();
		
		DBUserDao.getInstance().deleteEvent(eventId);
			
		String message = "The event " +  eventName + " with trainer " + ownerName + " was canceled.";
		LeapsUtils.sendMessageToFirebaseTopic(message, eventId);
		
	}

	public JsonArray getFollowed(long token, boolean isFuture) throws UserException, EventException, ImageException, TagException {
		long userId = getUserId(token);
		
		List<Event> events = DBUserDao.getInstance().getAllFollowedEvents(userId, isFuture);
		
		JsonArray response = new JsonArray();
		for (int i = 0; i < events.size(); i++) {
			response.add(LeapsUtils.generateJsonEvent(events.get(i), token, null, null));
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
		
		if (requestData.get("time_from") != null || requestData.get("time_to") != null) {
			Event event = DBUserDao.getInstance().getEventById(eventId);
			String message = "The time of " +  event.getTitle() + " has changed. It will start at " + event.getTimeFrom() + " and end at " + event.getTimeTo();
			LeapsUtils.sendMessageToFirebaseTopic(message, eventId);
		}
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
		String eventFirebaseTopic = null;
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
				obj.get("time_from") == null || obj.get("time_to") == null || obj.get("owner_id") == null || obj.get("firebase_topic") == null) {
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
		eventFirebaseTopic = obj.get("firebase_topic").getAsString();
		
		if (obj.get("tags") != null) {
			JsonElement jsonTags = obj.get("tags");
			Type listType = new TypeToken<List<String>>() {}.getType();

			tags = new Gson().fromJson(jsonTags, listType);
		}
		
		Event event = EventDao.getInstance().createNewEvent(title, description, address, latitude, longitute, priceFrom, freeSlots, date, 
															timeFrom, timeTo, tags, ownerId, eventFirebaseTopic);
		
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
		
		double latitude = -999.999;
		double longitude = -999.999;

		JsonArray nearby = new JsonArray();
		
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
				nearby.add(LeapsUtils.generateJsonEvent(responseEvents.get(i), token, latitude, longitude));
			}
		}

		response.add("nearby", nearby);

		JsonArray popular = new JsonArray();
		List<Event> popularEvents = DBUserDao.getInstance().getMostPopularEvents(page, limit);
		for (int i = 0; i < popularEvents.size(); i++) {
			popular.add(LeapsUtils.generateJsonEvent(popularEvents.get(i), token, latitude, longitude));
		}
		
		response.add("popular", popular);
		
		// TODO: currently using the most popular events logic
		// TODO: revise in later stages
		JsonArray suited = new JsonArray();
		for (int i = 0; i < popularEvents.size(); i++) {
			suited.add(LeapsUtils.generateJsonEvent(popularEvents.get(i), token, latitude, longitude));
		}
		response.add("suited", suited);
		
		return response;
	}

	public JsonObject createRepeatingEvent(long token, JsonObject requestData) throws InvalidParametersException, EventException, UserException, ImageException, TagException {
		List<RepeatingEventTime> dates = new ArrayList<RepeatingEventTime>();
		List<RepeatingEvent> repeatingEvents = new ArrayList<RepeatingEvent>();
		String period;
		String startTime;
		String endTime;
		boolean repeat;
		long startDate;
		long endDate;
		String frequency;
		String eventFirebaseTopic = null;
		long parentId;
		
		String title;
		String description;
		String address;
		double latitude;
		double longitude;
		int priceFrom;
		int freeSlots;
		List<String> tags = new ArrayList<String>();
		
		if (requestData.get("start") == null || requestData.get("end") == null || requestData.get("repeat") == null || requestData.get("frequency") == null
				|| requestData.get("dates") == null || requestData.get("title") == null || requestData.get("description") == null || requestData.get("address") == null
				 || requestData.get("coord_lat") == null || requestData.get("coord_lnt") == null || requestData.get("price_from") == null || requestData.get("free_slots") == null) {
			throw new InvalidParametersException(Configuration.INVALID_INPUT_PAREMETERS);
		}
		
		// TODO: update if the topic is required - it most probably is
		if (requestData.get("firebase_topic") != null) {
			eventFirebaseTopic = requestData.get("firebase_topic").getAsString();
		}
		// end TODO
		
		startDate = requestData.get("start").getAsLong();
		endDate = requestData.get("end").getAsLong();
		repeat = requestData.get("repeat").getAsBoolean();
		frequency = requestData.get("frequency").getAsString();
		title = requestData.get("title").getAsString();
		description = requestData.get("description").getAsString();
		address = requestData.get("address").getAsString();
		latitude = requestData.get("coord_lat").getAsDouble();
		longitude = requestData.get("coord_lnt").getAsDouble();
		priceFrom = requestData.get("price_from").getAsInt();
		freeSlots = requestData.get("free_slots").getAsInt();
		
		if (requestData.get("tags") != null) {
			JsonElement jsonTags = requestData.get("tags");
			Type listType = new TypeToken<List<String>>() {}.getType();

			tags = new Gson().fromJson(jsonTags, listType);
		}
		
		JsonArray datesJson = requestData.get("dates").getAsJsonArray();
		for (int i = 0; i < datesJson.size(); i++) {
			String jsonPeriod = datesJson.get(i).getAsJsonObject().get("period").getAsString();
			startTime = datesJson.get(i).getAsJsonObject().get("start").getAsString();
			endTime = datesJson.get(i).getAsJsonObject().get("end").getAsString();
			
			// check if the 'repeat' value is correct
			if (!LeapsUtils.checkIfValueIsValid(jsonPeriod, Configuration.WeekDays.values())) {
				throw new InvalidParametersException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			dates.add(new RepeatingEventTime(jsonPeriod, startTime, endTime));
		}
		
		LocalDateTime startDateInMillis = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDateTime();
		int startDay = startDateInMillis.getDayOfMonth();
		int startMonth = startDateInMillis.getMonthValue();
		int startYear = startDateInMillis.getYear();
		
		LocalDateTime endDateInMillis = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDateTime();
		
		for (int i = 0; i < dates.size(); i++) {
			RepeatingEventTime currentEvent = dates.get(i);
			int startMinutes = Integer.parseInt(currentEvent.getStart().substring(2, 4)); //(currentEvent.getStart() % 100);
			int startHours = Integer.parseInt(currentEvent.getStart().substring(0, 2)); //(currentEvent.getStart() / 100);
			int endMinutes = Integer.parseInt(currentEvent.getEnd().substring(2, 4)); //(currentEvent.getEnd() % 100);
			int endHours = Integer.parseInt(currentEvent.getEnd().substring(0, 2)); //(currentEvent.getEnd() / 100);

			LocalDateTime eventStartTime = LocalDateTime.of(startYear, startMonth, startDay, startHours, startMinutes, 0);
			LocalDateTime eventEndTime = LocalDateTime.of(startYear, startMonth, startDay, endHours, endMinutes, 0);
			
			if (currentEvent.getPeriod().equals(Configuration.WeekDays.everyday.toString())) {
				Period timePeriod = Period.between(eventStartTime.toLocalDate(), endDateInMillis.toLocalDate());
				
				while (timePeriod.getDays() > 0) {
					timePeriod = Period.between(eventStartTime.toLocalDate(), endDateInMillis.toLocalDate());
					
					// here we have the event's start and end time
					long currentEventStartTime = eventStartTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
					long currentEventEndTime = eventEndTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
					
					repeatingEvents.add(new RepeatingEvent(currentEventStartTime, currentEventEndTime, false));
					
					eventStartTime = eventStartTime.plusDays(1);
					eventEndTime = eventEndTime.plusDays(1);
				}
			} else {
				DayOfWeek startDayOfWeek = startDateInMillis.getDayOfWeek();
				int dayOfWeekIntValue = startDayOfWeek.getValue();
				int eventDateForCreation = -1;
				
				switch(currentEvent.getPeriod()) {
					case Configuration.MONDAY:
						eventDateForCreation = 1;
						break;
					case Configuration.TUESDAY:
						eventDateForCreation = 2;
						break;
					case Configuration.WEDNESDAY:
						eventDateForCreation = 3;
						break;
					case Configuration.THURSDAY:
						eventDateForCreation = 4;
						break;
					case Configuration.FRIDAY:
						eventDateForCreation = 5;
						break;
					case Configuration.SATURDAY:
						eventDateForCreation = 6;
						break;
					case Configuration.SUNDAY:
						eventDateForCreation = 7;
						break;
					default:
						// you shouldn't be able to get here ...
				}
				
				// checks if the event for creation has bigger day than the day when we start creating the events
				if (eventDateForCreation > dayOfWeekIntValue) {
					int dayDifference = eventDateForCreation - dayOfWeekIntValue;
					eventStartTime = eventStartTime.plusDays(dayDifference);
					eventEndTime = eventEndTime.plusDays(dayDifference);
				} else { // eventDateForCreation > dayOfWeekIntValue
					int dayDifference = (Configuration.MAX_DAYS_OF_WEEK + eventDateForCreation) - dayOfWeekIntValue;
					eventStartTime = eventStartTime.plusDays(dayDifference);
					eventEndTime = eventEndTime.plusDays(dayDifference);
				}
				
				// add the first event
				long currentEventStartTime = eventStartTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
				long currentEventEndTime = eventEndTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
				repeatingEvents.add(new RepeatingEvent(currentEventStartTime, currentEventEndTime));
				// add one week
				eventStartTime = eventStartTime.plusDays(Configuration.MAX_DAYS_OF_WEEK);
				eventEndTime = eventEndTime.plusDays(Configuration.MAX_DAYS_OF_WEEK);
				
				Period timePeriod = Period.between(eventStartTime.toLocalDate(), endDateInMillis.toLocalDate());
				
				// then continue with the ones after 1 week
				while (timePeriod.getDays() > 0) {
					currentEventStartTime = eventStartTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
					currentEventEndTime = eventEndTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
					
					repeatingEvents.add(new RepeatingEvent(currentEventStartTime, currentEventEndTime, false));					

					eventStartTime = eventStartTime.plusDays(Configuration.MAX_DAYS_OF_WEEK);
					eventEndTime = eventEndTime.plusDays(Configuration.MAX_DAYS_OF_WEEK);
					
					timePeriod = Period.between(eventStartTime.toLocalDate(), endDateInMillis.toLocalDate());
				}
			}
		}
		
		// find the most closest event to our date
		int index = 0;
		long soonestEvent = repeatingEvents.get(0).getStartTime();
		for (int i = 1; i < repeatingEvents.size(); i++) {
			long currentEventTime = repeatingEvents.get(i).getStartTime();
			
			if (currentEventTime < soonestEvent) {
				index = i;
				soonestEvent = currentEventTime;
			}
		}
		
		repeatingEvents.get(index).setExist(true);
		
		long userId = getUserId(token);
		// create this event and prepare to send it as an response
		Event event = EventDao.getInstance().createNewEvent(title, description, address, latitude, longitude, priceFrom, freeSlots, repeatingEvents.get(index).getStartTime(), 
				repeatingEvents.get(index).getStartTime(), repeatingEvents.get(index).getEndTime(), tags, userId, eventFirebaseTopic);
		
		if (event == null) {
			throw new EventException(Configuration.CANNOT_CREATE_NEW_EVENT);
		}
		
		parentId = event.getEventId();
		
		// add all repeating events into the db
		for (int i = 0; i < repeatingEvents.size(); i++) {
			long id = DBUserDao.getInstance().createRepeatingEvent(repeatingEvents.get(i), parentId);
			
			// update the parent event with its proper event_id fk
			if (i == index) {
				DBUserDao.getInstance().updateRepeatingEvent(id, parentId);
			}
		}
		
		JsonObject response = LeapsUtils.generateJsonEvent(event, token, null, null);
		
		return response;
	}
}
