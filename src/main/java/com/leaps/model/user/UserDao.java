package com.leaps.model.user;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.leaps.interfaces.IUserDao;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.event.Event;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.TagException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.token.Token;
import com.leaps.model.token.TokenManager;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.DebuggingManager;
import com.leaps.model.utils.LeapsUtils;

public class UserDao implements IUserDao {
	{
		cachedUsers = new ConcurrentHashMap<Token, User>();
	}

	private static ConcurrentHashMap<Token, User> cachedUsers;
	private static UserDao instance = null;
	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

	protected UserDao() {
	}

	public static UserDao getInstance() {
		if (instance == null) {
			instance = new UserDao();
		}
		return instance;
	}

	public User createNewUser(long userId, String username, String email, int age, String gender, String location,
			int maxDistanceSetting, String firstName, String lastName, long birthday, String description, String profileImageUrl, boolean isTrainer,
			String facebookId, String googleId, String phoneNumber, int sessionPrice, String longDescription, int yearsOfTraining, String firebaseToken) {
		return new User(userId, username, email, firstName, lastName, age, gender, location, maxDistanceSetting, birthday, description, profileImageUrl, isTrainer,
				facebookId, googleId, phoneNumber, yearsOfTraining, sessionPrice, longDescription, firebaseToken);
	}
	
	public Map<Token, User> registerNewUser(String pass, String email, String firstName, String lastName, Long birthday, 
											String facebookId, String googleId, String firebaseToken) throws UserException {
		Map<Token, User> returnedData = new HashMap<Token, User>();
		
		int age = LeapsUtils.generateAgeFromBirthday(birthday);
		User user = createNewUser(pass, email, firstName, lastName, birthday, facebookId, googleId, age, firebaseToken);

		if (user != null) {
			Token token = TokenManager.getInstance().generateToken();
			cacheUser(token, user);
			returnedData.put(token, user);
		}

		return returnedData;
	}
	
	public void updateUserInCache(User user, long token) {
		for (Entry<Token, User> map : cachedUsers.entrySet()) {
			// temprorary make a reference of the token taken from the cache for further use
			Token mapToken = map.getKey();
			long mapTokenId = mapToken.getId();
			if (mapTokenId == token) {
				// first remove the current key-value pair that token's id = the input token id
				cachedUsers.remove(mapToken);
				// update the temprorary token reference taken from the cache before removal
				TokenManager.getInstance().updateTokenTime(mapToken);
				// add the updated token reference and put an updated user into the map
				cachedUsers.put(mapToken, user);
				break;
			}
		}
	}
	
	public Map<Token,User> getUserFromDbOrCache(String userData, String pass, String facebookId, String googleId) throws UserException {
		Map<Token, User> cachedUser = getUserFromCache(userData, pass, facebookId, googleId);
		
		if (cachedUser == null || cachedUser.isEmpty()) {
			Map<Token, User> returnedData = new HashMap<Token, User>();
			User user = DBUserDao.getInstance().getUserFromDb(userData, pass, facebookId, googleId);
			if (user != null) {
				Token token = TokenManager.getInstance().generateToken();
				cacheUser(token, user);
				returnedData.put(token, user);
			}
			
			return returnedData;
		}
		
		Token token = getTokenIfUserExistInCache(userData, facebookId, googleId);
		TokenManager.getInstance().updateTokenTime(token);
		
		return cachedUser;
	}

	public boolean checkIfUserExistInDbOrCache(String userData) {
		if (checkIfUserExistInCache(userData)) {
			return true;
		}
		return DBUserDao.getInstance().checkIfUserExistInDB(userData);
	}

	private boolean checkIfUserExistInCache(String userData) {
		for (Map.Entry<Token, User> users : cachedUsers.entrySet()) {
			if (users.getValue() != null) {
				if ((users.getValue().getEmail() != null && users.getValue().getEmail().equals(userData)) || (users.getValue().getUsername() != null && users.getValue().getUsername().equals(userData))) {
					return true;
				}
			}
		}

		return false;
	}
	
	private Token getTokenIfUserExistInCache(String userData, String facebookId, String googleId) {
		for (Map.Entry<Token, User> users : cachedUsers.entrySet()) {
			if (!cachedUsers.isEmpty()) {
				if (users.getValue() != null && users.getValue().getFacebookId() != null && users.getValue().getFacebookId().equals(facebookId)) {
					return users.getKey();
				} else if (users.getValue() != null && users.getValue().getGoogleId() != null && users.getValue().getGoogleId().equals(googleId)) {
					return users.getKey();
				} else if (users.getValue() != null && users.getValue().getUsername() != null && (users.getValue()).getUsername().equals(userData)) {
					return users.getKey();
				} else if (users.getValue() != null && (users.getValue()).getEmail() != null && (users.getValue()).getEmail().equals(userData)) {
					return users.getKey();
				}
			}
		}

		return null;
	}

	private void cacheUser(Token Token, User user) {
		cachedUsers.put(Token, user);
	}
	
	private User createNewUser(String pass, String email, String firstName, String lastName, Long birthday, 
							   String facebookId, String googleId, int age, String firebaseToken) throws UserException {
		User user = null;

		String username = generateUsername(email);
		// if an error occurs, the returned userId will be set to -1
		Long userId = DBUserDao.getInstance().insertUserIntoDB(username, pass, email, firstName, lastName, birthday, facebookId, googleId, age, firebaseToken);
		if (userId >= 0) {
			user = new User(userId, username, pass, email, firstName, lastName, birthday, facebookId, googleId, age, firebaseToken);
		}

		return user;
	}

	private String generateUsername(String email) throws UserException {
		Random rand = new Random();
		
		String tempUsername = email;
		int index = tempUsername.indexOf('@');
		tempUsername = tempUsername.substring(0, index);
		
		List<String> dbUsers = DBUserDao.getInstance().findSimilarUsernamesFromDB(tempUsername);
		
		if (dbUsers.isEmpty()) {
			return tempUsername;
		}
		
		StringBuilder sb = new StringBuilder(tempUsername);
		
		for (int i = 0; i < dbUsers.size(); i++) {
			if (dbUsers.get(i).equals(sb.toString())) {
				sb.append(rand.nextInt(10));
				i = 0;
			}
			
			if ((i+1) == dbUsers.size() && !sb.toString().equals(dbUsers.get(i))) {
				tempUsername = sb.toString();
			}
		}
		
		return tempUsername;
	}

	private Map<Token, User> getUserFromCache(String userData, String pass, String facebookId, String googleId) {
		for (Map.Entry<Token, User> users : cachedUsers.entrySet()) {
			if (!cachedUsers.isEmpty()) {
				if (users.getValue() != null && users.getValue().getFacebookId() != null && users.getValue().getFacebookId().equals(facebookId)) {
					Map<Token, User> user = new HashMap<Token, User>();
					user.put(users.getKey(), users.getValue());
					return user;
				} else if (users.getValue() != null && users.getValue().getGoogleId() != null && users.getValue().getGoogleId().equals(googleId)) {
					Map<Token, User> user = new HashMap<Token, User>();
					user.put(users.getKey(), users.getValue());
					return user;
				} else if ((users.getValue() != null && users.getValue().getUsername() != null && (users.getValue()).getUsername().equals(userData)) && (users.getValue().getPass() != null && (users.getValue()).getPass().equals(pass))) {
					Map<Token, User> user = new HashMap<Token, User>();
					user.put(users.getKey(), users.getValue());
					return user;
				} else if ((users.getValue() != null && (users.getValue()).getEmail() != null && (users.getValue()).getEmail().equals(userData)) && (users.getValue().getPass() != null && (users.getValue()).getPass().equals(pass))) {
					Map<Token, User> user = new HashMap<Token, User>();
					user.put(users.getKey(), users.getValue());
					return user;
				}
			}
		}

		return null;
	}

	public Map<Token, User> getUserFromCache(long token) {
		// Debug
		long id = 131313;
		User testUser = new User();
		testUser.setUserId(131313l);
		cachedUsers.put(new Token(id),testUser);
		for (Map.Entry<Token, User> users : cachedUsers.entrySet()) {
			if (!cachedUsers.isEmpty()) {
				if (users.getKey() != null && users.getKey().getId() != null && users.getKey().getId().equals(token)) {
					Map<Token, User> user = new HashMap<Token, User>();
					user.put(users.getKey(), users.getValue());
					return user;
				}
			}
		}

		return null;
	}

	public boolean resetPassword(String email) throws Exception {
		boolean success = false;
		User user = DBUserDao.getInstance().getUserFromDbByEmail(email);
		String oldPass = user.getPass();
		
		if (user != null) {
			String pass = LeapsUtils.generateRandomPass();
			String hashedPass = LeapsUtils.convertToMd5(pass);
			boolean resetPassword = DBUserDao.getInstance().resetUserPassword(email, hashedPass);
			if (resetPassword) {
				boolean reset = LeapsUtils.sendMailToUser(email, pass);
				if (reset) {
					success = true;
				} else {
					Map<String, Map<String, Object>> params = new HashMap<String, Map<String, Object>>();
					params.put("password", new HashMap<String, Object>());
					params.get("password").put("string", oldPass);
					
					DBUserDao.getInstance().updateUser(params, user.getUserId());
				}
			}
		}
		
		return success;
	}
	
	public User getUserFromDbOrCacheById(long userId) throws UserException {
//		User user = getUserFromCacheById(userId);
//		if (user == null) {
		// remove usage of cache
		User user = DBUserDao.getInstance().getUserFromDbById(userId);
//		}
		
		return user;
	}
	
	private User getUserFromCacheById(long userId) {
		for (Map.Entry<Token, User> users : cachedUsers.entrySet()) {
			if (!cachedUsers.isEmpty()) {
				if (users.getValue() != null && users.getValue().getUserId() != null && users.getValue().getUserId().equals(userId)) {
					return users.getValue();
				}
			}
		}

		return null;
	}
	
	public JsonObject followUser(long token, int followedUserId) throws UserNotFoundException, EventException, UserException, ImageException, TagException {
		Map<Token, User> cachedUser = getUserFromCache(token);
		
		if (cachedUser == null) {
			throw new UserNotFoundException(Configuration.NO_USER_FOUND);
		}
		
		User user = null;
		
		for (Map.Entry<Token, User> map : cachedUser.entrySet()) {
			user = map.getValue();
		}
		
		DBUserDao.getInstance().followUser(user.getUserId(), followedUserId);
		// we return the followed user
		User followedUser = DBUserDao.getInstance().getUserFromDbById(followedUserId);
		
		return LeapsUtils.generateJsonUser(followedUser, token, null, null);
	}

	public JsonObject unfollowUser(long token, int followedUserId) throws UserNotFoundException, EventException, UserException, ImageException, TagException {
		Map<Token, User> cachedUser = getUserFromCache(token);
		
		if (cachedUser == null) {
			throw new UserNotFoundException(Configuration.NO_USER_FOUND);
		}
		
		User user = null;
		
		for (Map.Entry<Token, User> map : cachedUser.entrySet()) {
			user = map.getValue();
		}
		
		DBUserDao.getInstance().unfollowUser(user.getUserId(), followedUserId);

		// we return the unfollowed user
		User followedUser = DBUserDao.getInstance().getUserFromDbById(followedUserId);
		
		return LeapsUtils.generateJsonUser(followedUser, token, null, null);
	}

	public long checkIfTokenIsValid(String token) {
		if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
			return 0;
		}

		long checker = Long.valueOf(token);
		
		return UserDao.getInstance().getUserFromCache(checker) != null ? checker : 0;
	}

	public JsonObject getAllFollowers(String id) throws EventException, UserNotFoundException, UserException, InvalidInputParamsException {
		if (id == null || id.isEmpty() || !LeapsUtils.isNumber(id)) {
			throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
		}
		
		User user = getUserFromDbOrCacheById(Long.valueOf(id));
		if (user == null) {
			throw new UserNotFoundException(Configuration.USER_DOES_NOT_EXIST);
		}
		
		return getAllUserFollowers(user);
	}

	public JsonObject getAllUserFollowers(User user) throws EventException, UserException {
		JsonObject followedByJson = new JsonObject();
		JsonArray followingJson = new JsonArray();
		JsonArray othersJson = new JsonArray();
		
		Map<String, List<Long>> users = DBUserDao.getInstance().getFollowingUsers(user.getUserId());
		List<Long> follower = null;
		List<Long> followed = null;
		for (Map.Entry<String, List<Long>> temp : users.entrySet()) {
			if (temp.getKey().equals("follower")) {
				follower = temp.getValue();
			} else {
				followed = temp.getValue();
			}
		}
		
		for (int fod = 0; fod < follower.size(); fod++) {
//			boolean match = false;
			User tempUser = DBUserDao.getInstance().getUserFromDbById(follower.get(fod));
			JsonObject jsonUser = new JsonObject();
			jsonUser.addProperty("user_id", tempUser.getUserId());
			jsonUser.addProperty("user_name", (tempUser.getFirstName() + " " + tempUser.getLastName()).trim());
			jsonUser.addProperty("user_image_url", tempUser.getProfileImageUrl());
			
			othersJson.add(jsonUser);
//			
//			for (int fol = 0; fol < followed.size(); fol++) {
//				if (follower.get(fod) == followed.get(fol)) {
//					follower.remove(fod);
//					fod--;
//					match = true;
//					break;
//				}
//			}
//			
//			if (!match) {
//				othersJson.add(jsonUser);
//			}
		}
		
		for (int fol = 0; fol < followed.size(); fol++) {
			JsonObject jsonUser = new JsonObject();
			User tempUser = DBUserDao.getInstance().getUserFromDbById(followed.get(fol));
			jsonUser.addProperty("user_id", tempUser.getUserId());
			jsonUser.addProperty("user_name", (tempUser.getFirstName() + " " + tempUser.getLastName()).trim());
			jsonUser.addProperty("user_image_url", tempUser.getProfileImageUrl());
			followingJson.add(jsonUser);
		}
		
		
		followedByJson.add("following", followingJson);		
		followedByJson.add("others", othersJson);
		
		return followedByJson;
	}

	public JsonArray getTrainerComments(long userId, int page, int limit) throws EventException, UserException, TagException {
		JsonArray response = new JsonArray();
		
		List<Long> rateIds = DBUserDao.getInstance().getRatesForTrainer(userId, page, limit);
		for (int i = 0; i < rateIds.size(); i++) {
			response.add(LeapsUtils.generateJsonComment(rateIds.get(i)));
		}
		
		return response;
	}

	public int getFollowingCount(Long userId) throws EventException {
		return DBUserDao.getInstance().getFollowingCount(userId);
	}

	public int getFollowersCount(Long userId) throws EventException {
		return DBUserDao.getInstance().getFollowersCount(userId);
	}

	public List<Integer> getAllUserRatings(Long userId) throws EventException {
		return DBUserDao.getInstance().getAllUserRatings(userId);
	}

	public boolean canRate(Event event, Long token) throws UserException {
		boolean canRate = true;
		
		if (token != null) {
			Map<Token, User> cachedUser = UserDao.getInstance().getUserFromCache(token);
			if (cachedUser != null) {
				for(Map.Entry<Token, User> map : cachedUser.entrySet()) {
					canRate = DBUserDao.getInstance().canRate(map.getValue().getUserId(), event.getEventId());
				}
			}
		}
		
		return canRate;
	}

	public JsonObject filterTrainers(HttpServletRequest req) throws EventException, IOException, InvalidInputParamsException, UserException, ImageException, TagException {
		// TODO: leaving some unused attributes untill further discussion. 27.10.2017
		String keyWord = null;
		double latitude = 0.0;
		double longitude = 0.0;
		int distance = Configuration.USER_DEFAULT_MAX_DISTANCE_SETTING;
		List<String> tags = new ArrayList<String>();
		int page = 1;
		int limit = Configuration.FILTER_EVENTS_DEFAULT_PAGE_LIMITATION;
		long minStartingDate = -1;
		long maxStartingDate = -1;
		
		long token = UserDao.getInstance().checkIfTokenIsValid(req.getHeader("Authorization"));
		
		if (Configuration.debugMode) {
			DebuggingManager.logRequestHeaders(req, logger);
			logger.info("---------- user/trainer/filter------------");
		}
		
		JsonObject obj = LeapsUtils.getRequestData(req);
		if (Configuration.debugMode) {
			logger.info("------------------------------------------");
			logger.info("Request Data:");
			logger.info(obj.toString());
			logger.info("------------------------------------------");
		}
		
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
		
		if (keyWord != null && tags != null && !tags.isEmpty()) {
			filteredTrainersByTheirEvents = DBUserDao.getInstance().getFilteredTrainersByTextAndTags(keyWord, tags, page, limit);
			orderResponse.addProperty("total_results", DBUserDao.getInstance().countFilteredTrainersByTextAndTags(keyWord, tags));
		} else if (keyWord != null) {
			filteredTrainersByTheirEvents = DBUserDao.getInstance().getFilteredTrainersByText(keyWord, page, limit);
			orderResponse.addProperty("total_results", DBUserDao.getInstance().countFilteredTrainersByText(keyWord));
		} else if (tags != null && !tags.isEmpty()) {
			filteredTrainersByTheirEvents = DBUserDao.getInstance().getFilteredTrainersByTags(tags, page, limit);
			orderResponse.addProperty("total_results", DBUserDao.getInstance().countFilteredTrainersByTags(tags));
		} else {
			filteredTrainersByTheirEvents = DBUserDao.getInstance().getAllFilteredTrainers(page, limit);
			orderResponse.addProperty("total_results", DBUserDao.getInstance().countAllFilteredTrainers());
		}
		
		for (int k = 0; k < filteredTrainersByTheirEvents.size(); k++) {
//			JsonObject response = new JsonObject();
			User user = filteredTrainersByTheirEvents.get(k);
			
			JsonObject response = LeapsUtils.generateJsonUser(user, token != 0 ? token : null, latitude, longitude);
			
//			response.addProperty("user_id", user.getUserId());
//			response.addProperty("username", user.getUsername());
//			
//			int userAttendedEvents = DBUserDao.getInstance().getAllEventCountThatUserHasAttended(user.getUserId());
//			response.addProperty("attended_events", userAttendedEvents);
//			
//			response.addProperty("description", user.getDescription());
//			response.addProperty("email_address", user.getEmail());
//			response.addProperty("age", user.getAge());
//			response.addProperty("gender", user.getGender());
//			response.addProperty("location", user.getLocation());
//			response.addProperty("max_distance_setting", user.getMaxDistanceSetting());
//			response.addProperty("first_name", user.getFirstName());
//			response.addProperty("last_name", user.getLastName());
//			response.addProperty("birthday", user.getBirthday());
//			response.addProperty("profile_image_url", user.getProfileImageUrl());
//			response.addProperty("is_trainer", user.isTrainer());
//			response.addProperty("long_description", user.isTrainer() ? user.getLongDescription() : null);
//			response.addProperty("years_of_training", user.isTrainer() ? user.getYearsOfTraining() : null);
//			response.addProperty("session_price", user.isTrainer() ? user.getSessionPrice() : null);
//
//			// TODO: get followed by - in stage 2
//			JsonArray followedByJson = new JsonArray();
//			response.add("followed_by", followedByJson);
//			
//			List<Event> attendingEvents = DBUserDao.getInstance().getAllAttendingEventsForUser(user.getUserId());
//			JsonArray attendingEventsJson = new JsonArray();
//			
//			for (int i = 0; i < attendingEvents.size(); i++) {
//				attendingEventsJson.add(LeapsUtils.generateJsonEvent(attendingEvents.get(i), null, latitude, longitude));
//			}
//			response.add("attending_events", attendingEventsJson);
//			
//			List<Event> hostingEvents = DBUserDao.getInstance().getAllHostingEventsForUser(user.getUserId());
//			JsonArray hostingEventsJson = new JsonArray();
//			for (int i = 0; i < hostingEvents.size(); i++) {
//				hostingEventsJson.add(LeapsUtils.generateJsonEvent(hostingEvents.get(i), null, latitude, longitude));
//			}
//			response.add("hosting_events", hostingEventsJson);
//			
//			JsonArray specialtiesJson = new JsonArray();
//			List<Tag> specialties = EventDao.getInstance().getAllUserSpecialties(user.getUserId());
//			for (int i = 0; i < specialties.size(); i++) {
//				specialtiesJson.add(specialties.get(i).getName());
//			}
//			response.add("specialties", specialtiesJson);
//			
//			List<Image> userImages = DBUserDao.getInstance().getAllUserImages(user.getUserId());
//			JsonArray userImagesJson = new JsonArray();
//			
//			for (int i = 0; i < userImages.size(); i++) {
//				JsonObject tempObj = new JsonObject();
//				obj.addProperty("image_id", userImages.get(i).getImageId());
//				obj.addProperty("image_url", userImages.get(i).getImageName());
//				userImagesJson.add(tempObj);
//			}
//			response.add("images", userImagesJson);
			
			filteredEventsJson.add(response);
			
			orderResponse.add("trainers", filteredEventsJson);

		}
		

		if (Configuration.debugMode) {
			logger.info("------------------------------------------");
			logger.info("Order Response:");
			logger.info(orderResponse.toString());
			logger.info("------------------END---------------------");
		}
		
		return orderResponse;
	}

	public HttpStatus updateFirebaseToken(long token, JsonObject requestData) throws InvalidInputParamsException, UserException {
		String firebaseToken = null;
		long userId = -1;
		
		if (requestData.get("firebase_token") == null) {
			throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
		}
		
		firebaseToken = requestData.get("firebase_token").getAsString();
		Map<Token, User> userData = getUserFromCache(token);
		
		if (userData == null || (userData != null && userData.isEmpty())) {
			throw new UserException(Configuration.WRONG_USERNAME_PASSWORD_MESSAGE);
		}
		
		Iterator<Map.Entry<Token, User>> it = userData.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry<Token, User> pair = it.next();
		    userId = pair.getValue().getUserId();
		}
		
		DBUserDao.getInstance().updateUserFirebaseToken(userId, firebaseToken);
		
		// update the cache
		User user = DBUserDao.getInstance().getUserFromDbById(userId);
		UserDao.getInstance().updateUserInCache(user, token);
		
		return HttpStatus.OK;
	}
}
