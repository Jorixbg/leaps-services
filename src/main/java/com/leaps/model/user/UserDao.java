package com.leaps.model.user;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leaps.interfaces.IUserDao;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.event.Event;
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.TagException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.token.Token;
import com.leaps.model.token.TokenManager;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.LeapsUtils;

public class UserDao implements IUserDao {
	{
		cachedUsers = new ConcurrentHashMap<Token, User>();
	}

	private static ConcurrentHashMap<Token, User> cachedUsers;

	private static UserDao instance = null;

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
			String facebookId, String googleId, String phoneNumber, int sessionPrice, String longDescription, int yearsOfTraining) {
		return new User(userId, username, email, firstName, lastName, age, gender, location, maxDistanceSetting, birthday, description, profileImageUrl, isTrainer,
				facebookId, googleId, phoneNumber, yearsOfTraining, sessionPrice, longDescription);
	}
	
	public Map<Token, User> registerNewUser(String pass, String email, String firstName, String lastName, Long birthday, String facebookId, String googleId) throws UserException {
		Map<Token, User> returnedData = new HashMap<Token, User>();
		
		int age = LeapsUtils.generateAgeFromBirthday(birthday);
		User user = createNewUser(pass, email, firstName, lastName, birthday, facebookId, googleId, age);

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
			if (mapToken.getId().equals(token)) {
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
	
	private User createNewUser(String pass, String email, String firstName, String lastName, Long birthday, String facebookId, String googleId, int age) throws UserException {
		User user = null;

		String username = generateUsername(email);
		// if an error occurs, the returned userId will be set to -1
		Long userId = DBUserDao.getInstance().insertUserIntoDB(username, pass, email, firstName, lastName, birthday, facebookId, googleId, age);
		if (userId >= 0) {
			user = new User(userId, username, pass, email, firstName, lastName, birthday, facebookId, googleId, age);
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
		
		if (DBUserDao.getInstance().checkIfUserExistInDBByEmail(email)) {
			String pass = LeapsUtils.generateRandomPass();
			String hashedPass = LeapsUtils.convertToMd5(pass);
			boolean resetPassword = DBUserDao.getInstance().resetUserPassword(email, hashedPass);
			if (resetPassword) {
				LeapsUtils.resetPassword(email, pass);
				success = true;
			}
		}
		
		return success;
	}
	
	public User getUserFromDbOrCacheById(long userId) throws UserException {
		User user = getUserFromCacheById(userId);
		if (user == null) {
			user = DBUserDao.getInstance().getUserFromDbById(userId);
		}
		
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
		
		return LeapsUtils.generateJsonUser(followedUser, token);
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
		
		return LeapsUtils.generateJsonUser(followedUser, token);
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
		User user = null;
		
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
}
