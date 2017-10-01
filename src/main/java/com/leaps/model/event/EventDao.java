package com.leaps.model.event;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.google.gson.JsonObject;
import com.leaps.interfaces.IEventDao;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.rate.Rate;
import com.leaps.model.token.Token;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.LeapsUtils;

public class EventDao implements IEventDao {
	
	private static EventDao instance = null;

	protected EventDao() {
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
			int priceFrom, int freeSlots, long date, long timeFrom, long timeTo, List<String> tags, long ownerId) {
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

	public List<Tag> getAllUserSpecialties(long userId) {

		List<Tag> tags = DBUserDao.getInstance().getAllUserSpecialtiesFromDb(userId);
		
		return tags;
	}

	public JsonObject followEvent(long token, int eventId) throws UserException, EventException {
		Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(token);
		
		if (cachedUser == null) {
			throw new UserNotFoundException(Configuration.NO_USER_FOUND);
		}
		
		User user = null;
		
		for (Map.Entry<Token, User> map : cachedUser.entrySet()) {
			user = map.getValue();
		}
		
		DBUserDao.getInstance().followEvent(user.getUserId(), eventId);
		
		Event event = DBUserDao.getInstance().getEventById(eventId);
		
		return LeapsUtils.generateJsonEvent(event);
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
	
	public void rate(JsonObject requestData, long token) throws InvalidInputParamsException, EventException, UserNotFoundException {
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
		
		Rate rate = new Rate(requestData.get("event_id").getAsLong(), requestData.get("rating").getAsInt(), requestData.get("comment").getAsString(), requestData.get("date_created").getAsLong());

		if (rate.getRating() < Configuration.MIN_RATING || rate.getRating() > Configuration.MAX_RATING) {
			throw new InvalidInputParamsException(Configuration.INVALID_RATING_VALUE);
		}

		// check if event exist
		DBUserDao.getInstance().getEventById(rate.getEventId());
		
		DBUserDao.getInstance().rateEvent(rate, userId);
	}
}
