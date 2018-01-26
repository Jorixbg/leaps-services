package com.leaps.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leaps.interfaces.IDBUserDao;
import com.leaps.model.bean.RepeatingEvent;
import com.leaps.model.event.Event;
import com.leaps.model.event.EventDao;
import com.leaps.model.event.Tag;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.exceptions.TagException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.image.Image;
import com.leaps.model.image.ImageDao;
import com.leaps.model.rate.Rate;
import com.leaps.model.rate.RateDao;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;

public class DBUserDao implements IDBUserDao {

	private static final Logger logger = LoggerFactory.getLogger(DBUserDao.class);
	
	private static DBUserDao instance;
	
	private DBUserDao() {}
	
	public static DBUserDao getInstance(){
		if(instance == null)
			instance = new DBUserDao();
		return instance;
	}

	private User retrieveUser(ResultSet rs) throws SQLException {
		return UserDao.getInstance().createNewUser(rs.getInt("user_id"), rs.getString("username"), rs.getString("email_address"), rs.getInt("age"), rs.getString("gender"),
				   rs.getString("location"), rs.getInt("max_distance_setting"), rs.getString("first_name"), rs.getString("last_name"), 
				   rs.getLong("birthday"), rs.getString("description"), rs.getString("profile_image_url"), rs.getBoolean("is_trainer"), 
				   rs.getString("facebook_id"), rs.getString("google_id"), rs.getString("phone_number"),
				   rs.getInt("session_price"), rs.getString("long_description"), rs.getInt("years_of_training"), rs.getString("firebase_token"));
	}
	
	// close all connections method
	private void closeResources(PreparedStatement preparedStatement, ResultSet rs, Connection dbConnection) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {}
		}
		
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {}
		}
		
		// currently disabled
//		if (dbConnection != null) {
//			try {
//				dbConnection.close();
//			} catch (SQLException e) {}
//		}
	}
	
	public boolean checkIfUserExistInDB(String userData) {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		String selectSQL = "SELECT user_id FROM leaps.users WHERE email_address = ? OR username = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, userData);
			preparedStatement.setString(2, userData);
			preparedStatement.execute();
			
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				return true;
			}
			
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			return true;
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
		
		return false;
	}

	public boolean checkIfUserExistInDBByEmail(String email) {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		String selectSQL = "SELECT user_id FROM leaps.users WHERE email_address = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				return true;
			}
			
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			return true;
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
		
		return false;
	}
	
	public User getUserFromDb(String userData, String pass, String facebookId, String googleId) throws UserException {
		User user = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		String selectSQL = "SELECT * FROM leaps.users WHERE (username = ? OR email_address = ?) AND password = ?";
		
		if (facebookId != null) {
			selectSQL = "SELECT * FROM leaps.users WHERE facebook_id = ?";
		} else if (googleId != null) {
			selectSQL = "SELECT * FROM leaps.users WHERE google_id = ?";
		}
	
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			if (facebookId != null) {
				preparedStatement.setString(1, facebookId);
			} else if (googleId != null) {
				preparedStatement.setString(1, googleId);
			} else {
				preparedStatement.setString(1, userData);
				preparedStatement.setString(2, userData);
				preparedStatement.setString(3, pass);
			}

			preparedStatement.execute();
			
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				user = retrieveUser(rs);
			}
			
			return user;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}
	
	public Long insertUserIntoDB(String username, String pass, String email, String firstName, String lastName,
								 Long birthday, String facebookId, String gogleId, int age, String firebaseToken) throws UserException {
		Long userId = -1L;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet generatedKeys = null;
		String selectSQL = "INSERT INTO leaps.users (username, password, email_address, first_name, last_name, birthday, facebook_id, google_id, age, max_distance_setting, firebase_token) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? )";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, pass);
			preparedStatement.setString(3, email);
			preparedStatement.setString(4, firstName);
			preparedStatement.setString(5, lastName);
			preparedStatement.setLong(6, birthday);
			preparedStatement.setString(7, facebookId);
			preparedStatement.setString(8, gogleId);
			preparedStatement.setInt(9, age);
			
			// set default max distance setting on creation of a new user
			preparedStatement.setInt(10, Configuration.USER_DEFAULT_MAX_DISTANCE_SETTING);
			preparedStatement.setString(11, firebaseToken);
			
			preparedStatement.execute();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	userId = generatedKeys.getLong(1);
            }
            
            return userId;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new UserException(Configuration.CANNOT_INSERT_USER_INTO_DB);
		} finally {
			closeResources(preparedStatement, generatedKeys, dbConnection);
		}
	}
	
	public List<String> findSimilarUsernamesFromDB(String username) throws UserException {
		List<String> returnedData = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet dbUsernames = null;
		String selectSQL = "SELECT username FROM leaps.users WHERE username LIKE ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, username);
			
			dbUsernames = preparedStatement.executeQuery();
            if (dbUsernames.next()) {
            	returnedData.add(dbUsernames.getString(1));
            }
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, dbUsernames, dbConnection);
		}
		
		return returnedData;
	}

	public boolean resetUserPassword(String email, String pass) throws UserException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String selectSQL = "update leaps.users set password = ? WHERE email_address = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, pass);
			preparedStatement.setString(2, email);
			preparedStatement.execute();
			
			return true;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	// TODO: unused order
//	public List<Tag> getUserTokens(long ownerId, int tokenSizeForCreateEvent) {
//		List<Tag> tags = new ArrayList<Tag>();
//		Connection dbConnection = null;
//		PreparedStatement preparedStatement = null;
//		String selectSQL = "SELECT s.name FROM leaps.specialties s "
//						 + "LEFT JOIN leaps.users u ON s.user_id = u.user_id "
//						 + "WHERE s.user_id = ? LIMIT ?";
//		
//		try {
//			dbConnection = DBManager.getInstance().getConnection();
//			preparedStatement = dbConnection.prepareStatement(selectSQL);
//			preparedStatement.setLong(1, ownerId);
//			preparedStatement.setInt(2, tokenSizeForCreateEvent);
//
//			if (Configuration.debugMode) {
//				logger.info("SQL Statement: " + preparedStatement.toString());
//			}
//			
//			ResultSet rs = preparedStatement.executeQuery();
//            if (rs.next()) {
//            	tags.add(EventDao.getInstance().createNewTag(rs.getInt("specialty_id"), rs.getString("name"), rs.getInt("user_id")));
//            }
//            
//			if (Configuration.debugMode) {
//				LeapsUtils.logRetrievedTagsFromTheDB(tags);
//			}
//			
//			return tags;
//		} catch (Exception e) {
//			if (Configuration.debugMode) {
//				logger.error(e.getMessage());
//			}
//			
//			throw new UserException(Configuration.INVALID_TOKEN);
//		} finally {
//			closeResources(preparedStatement, null, dbConnection);
//		}
//		
//		return tags;
//	}

	public long createNewEvent(String title, String description, long date, long timeFrom, long timeTo, long ownerId, double latitude, 
							   double longitute, int priceFrom, String address, int freeSlots, Long dateCreated, String eventFirebaseTopic) throws EventException {
		long eventId = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet generatedKeys = null;
		
		String selectSQL = "INSERT INTO leaps.events (title, description, date, time_from, time_to, owner_id, coord_lat, coord_lnt, "
												   + "price_from, address, free_slots, date_created, event_image_url, firebase_topic)"
												   + "VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?)";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, title);
			preparedStatement.setString(2, description);
			preparedStatement.setLong(3, date);
			preparedStatement.setLong(4, timeFrom);
			preparedStatement.setLong(5, timeTo);
			preparedStatement.setLong(6, ownerId);
			preparedStatement.setDouble(7, latitude);
			preparedStatement.setDouble(8, longitute);
			preparedStatement.setInt(9, priceFrom);
			preparedStatement.setString(10, address);
			preparedStatement.setInt(11, freeSlots);
			preparedStatement.setLong(12, dateCreated);
			preparedStatement.setString(13, null);
			preparedStatement.setString(14, eventFirebaseTopic);
			
			preparedStatement.execute();

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	eventId = generatedKeys.getLong(1);
            }
            
            return eventId;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new EventException(Configuration.CANNOT_CREATE_NEW_EVENT);
		} finally {
			closeResources(preparedStatement, generatedKeys, dbConnection);
		}
	}

	public boolean addTagsToTheDB(List<String> tags, long eventId) throws EventException {
		boolean success = true;
		List<Tag> dbTags = new ArrayList<Tag>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		PreparedStatement secondPreparedStatement = null;
		PreparedStatement statement = null;
		ResultSet generatedKeys = null;
		ResultSet rs = null;
		
		StringBuilder selectTagsStatement = new StringBuilder("Select tag_id, name FROM leaps.tags WHERE name IN (");
		for (int i = 0; i < tags.size(); i++) {
			selectTagsStatement.append("?");
			if (i + 1 < tags.size()) {
				selectTagsStatement.append(", ");
			} else {
				selectTagsStatement.append(")");
			}
		}

		// check if some of the tags already exist in the database 'tags' schema and retreive their ids
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectTagsStatement.toString());
			for (int i = 0; i < tags.size(); i++) {
				preparedStatement.setString(i+1, tags.get(i));
			}
			
        	generatedKeys = preparedStatement.executeQuery();
        	while (generatedKeys.next()) {
            	dbTags.add(EventDao.getInstance().createNewTag(generatedKeys.getInt(1), generatedKeys.getString(2)));
        	}

			// remove the tags from the tags array that ids were found
			List<String> tempTags = new ArrayList<String>();
			for (int i = 0; i < tags.size(); i++) {
				for (int k = 0; k < dbTags.size(); k++) {
					if (tags.get(i).equals(dbTags.get(k).getName())) {
						break;
					}
					
					if (k + 1 >= dbTags.size()) {
						tempTags.add(tags.get(i));
					}
				}
			}
			
			if (dbTags.size() == 0) {
				tempTags = tags;
			}
		
			if (tempTags.size() != 0) {
				StringBuilder insertTagsStatement = new StringBuilder("INSERT INTO leaps.tags (name) VALUES ");
				for (int i = 0; i < tempTags.size(); i++) {
					insertTagsStatement.append("( ? )");
					if (i + 1 < tempTags.size()) {
						insertTagsStatement.append(", ");
					}
				}
				
				// add the tags that are not registered in the db in 'tags' schema and get their ids
				int index = 0;
				secondPreparedStatement = dbConnection.prepareStatement(insertTagsStatement.toString(), Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < tempTags.size(); i++) {
					secondPreparedStatement.setString(i + 1, tempTags.get(i));
				}
				secondPreparedStatement.execute();
				
				rs = secondPreparedStatement.getGeneratedKeys();
	            while (rs.next()) {
	            	dbTags.add(EventDao.getInstance().createNewTag(rs.getInt(1), tags.get(index++)));
	            }
				
				return true;
			}

			// add all tags in 'event_has_tags' schema
			StringBuilder insertEventTagsStatement =  new StringBuilder("INSERT INTO leaps.event_has_tags ( tag_id, event_id ) VALUES");
			for (int i = 0; i < dbTags.size(); i++) {
				insertEventTagsStatement.append(" ( ?, ? )" );
				if (i + 1 < dbTags.size()) {
					insertEventTagsStatement.append(",");
				}
			}

			statement = dbConnection.prepareStatement(insertEventTagsStatement.toString());
			int temp = 1;
			for (int i = 0; i < dbTags.size(); i++) {
				statement.setInt(temp++, dbTags.get(i).getTagId());
				statement.setInt(temp++, (int) eventId);
			}
			statement.execute();
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new EventException(Configuration.CANNOT_INSERT_TAGS_IN_THE_DB);
		} finally {
			closeResources(preparedStatement, generatedKeys, dbConnection);
			closeResources(secondPreparedStatement, rs, null);
			closeResources(statement, null, null);
		}
		
		return success;
	}

	public List<String> getMostPopularTags() throws TagException {
		List<String> tags = new ArrayList<String>();
				
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT t.name, count(eht.tag_id) FROM leaps.event_has_tags eht LEFT JOIN leaps.tags t ON t.tag_id = eht.tag_id GROUP BY eht.tag_id ORDER BY eht.tag_id ASC LIMIT ?";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, Configuration.TAG_SELECT_LIMIT);

			rs = preparedStatement.executeQuery();
            while (rs.next()) {
            	tags.add(rs.getString("name"));
            }
            
			return tags;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
		
			throw new TagException(Configuration.INVALID_TAG);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}
	
	public User getUserFromDbById(long userId) throws UserException {
		User user = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSql = "Select * from leaps.users WHERE user_id = ?";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
            while (rs.next()) {
            	user = retrieveUser(rs);
            }
    		
    		return user;
        } catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new UserException(Configuration.ERROR_RETREIVING_THE_USERS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Tag> getAllUserSpecialtiesFromDb(long userId) throws TagException {
		List<Tag> specialties = new ArrayList<Tag>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSql = "Select specialty_id, name from leaps.specialties WHERE user_id = ?";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				specialties.add(EventDao.getInstance().createNewTag(rs.getInt("specialty_id"),rs.getString("name"), userId));
			}
		    
			return specialties;
	    } catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new TagException(Configuration.CANNOT_RETRIEVE_USER_SPECIALTIES);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public Event getEventById(long eventId) throws EventException {
		Event event = null;
		Connection dbConnection = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT * FROM leaps.events WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				event = EventDao.getInstance().generateNewEvent(eventId, rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"),
								 rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), rs.getString("address"),
								 rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic"));
			}
			
			return event;
	    } catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.EVENT_DOES_NOT_EXIST);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<User> getAllAttendingUsersForEvent(long eventId) throws UserException {
		List<User> users = new ArrayList<User>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "select * from leaps.users u WHERE u.user_id IN (SELECT uae.user_id FROM leaps.users_attend_events uae WHERE uae.event_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				users.add(retrieveUser(rs));
			}
			
			return users;
	    } catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_RETRIEVE_ATTENDING_EVENT_USERS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public Map<String, String> getAllEventImages(long eventId) throws ImageException {
		Map<String, String> images = new HashMap<String, String>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT image_id, file_name FROM leaps.event_images WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				images.put(rs.getString("image_id"), rs.getString("file_name"));
			}
			
			return images;
	    } catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_RETRIEVE_IMAGE_FROM_DB);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Tag> getAllEventTagsFromDb(long eventId) throws TagException {
		List<Tag> tags = new ArrayList<Tag>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT t.name, t.tag_id FROM leaps.tags t WHERE t.tag_id IN (SELECT eht.tag_id FROM leaps.event_has_tags eht WHERE eht.event_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, (int)eventId);
						
			rs = preparedStatement.executeQuery();			
			while (rs.next()) {
				tags.add(EventDao.getInstance().createNewTag(rs.getInt("tag_id"), rs.getString("name")));
			}
			
			return tags;
	    } catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new TagException(Configuration.CANNOT_RETRIEVE_TAGS_FROM_SERVER);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public boolean updateUser(Map<String, Map<String, Object>> params, long userId) throws UserException {
		boolean success = true;
		
		if (!params.isEmpty()) {
			Connection dbConnection = null;
			PreparedStatement preparedStatement = null;
			
			StringBuilder selectSQL = new StringBuilder("update leaps.users set ");
			int mapSize = params.size();
			int counter = 0;
			for (Map.Entry<String, Map<String, Object>> map : params.entrySet()) {
				String key = map.getKey();
				selectSQL.append(key + " = ?");
				counter++;
				if (counter < mapSize) {
					selectSQL.append(", ");
				} else {
					selectSQL.append(" WHERE user_id = ?");
				}
			}
			
			try {
				dbConnection = DBManager.getInstance().getConnection();
				preparedStatement = dbConnection.prepareStatement(selectSQL.toString());
				
				int statementCounter = 1;
				for (Map.Entry<String, Map<String, Object>> map : params.entrySet()) {
					for (Map.Entry<String, Object> innerMap : map.getValue().entrySet()) {
						if (innerMap.getKey().equals("string")) {
							preparedStatement.setString(statementCounter++, String.valueOf(innerMap.getValue()));
						} else if (innerMap.getKey().equals("int")) {
							preparedStatement.setInt(statementCounter++, Integer.valueOf(String.valueOf(innerMap.getValue())));
						} else if (innerMap.getKey().equals("long")) {
							preparedStatement.setLong(statementCounter++, Long.valueOf(String.valueOf(innerMap.getValue())));
						} else if (innerMap.getKey().equals("boolean")) {
							preparedStatement.setBoolean(statementCounter++, Boolean.valueOf(String.valueOf(innerMap.getValue())));
						}
					}
				}
				preparedStatement.setLong(statementCounter, userId);
				preparedStatement.executeUpdate();
				
				return success;
		    } catch (Exception e) {
		    	if (Configuration.debugMode) {
					logger.error(e.getMessage());
				}
		    	
				throw new UserException(Configuration.CANNOT_UPDATE_USER);
			} finally {
				closeResources(preparedStatement, null, dbConnection);
			}
		} else {
			return success;
		}
	}
	
	public List<Image> getAllUserImages(long userId) throws ImageException {
		List<Image> images = new ArrayList<Image>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT image_id, file_name FROM leaps.user_images WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
						
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				images.add(ImageDao.getInstance().createNewImage(rs.getLong("image_id"), userId, rs.getString("file_name")));
			}			

			return images;
	    } catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_RETRIEVE_IMAGE_FROM_DB);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getAllAttendingEventsForUser(long userId) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT e.* FROM leaps.events e WHERE e.event_id in (SELECT uae.event_id FROM leaps.users_attend_events uae WHERE uae.user_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_ATTENDING_EVENTS_FOR_USER);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getAllHostingEventsForUser(long userId) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.events WHERE owner_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}			

			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_HOSTING_EVENTS_FOR_USER);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int getEventAttendeesNumber(long eventId) throws UserException {
		int attendees = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "select count(user_id) as 'attendees' from leaps.users_attend_events where event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				attendees = rs.getInt("attendees");
			}
			
			return attendees;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_RETRIEVE_EVENT_ATTENDEES);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public void addAttendeeForEvent(long userId, long eventId) throws UserException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "INSERT INTO leaps.users_attend_events (user_id, event_id) VALUES ( ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, eventId);
			
			preparedStatement.executeUpdate();
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_INSERT_USER_INTO_DB);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public boolean unattendUserFromEvent(long userId, long eventId) throws UserException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.users_attend_events WHERE user_id = ? AND event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, eventId);
			
			preparedStatement.executeUpdate();
			
			return true;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_UNNATEND_FROM_EVENT);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public List<Event> getAllPastHostingEventsForUser(int userId, int limit, int page) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.events e WHERE e.owner_id = ? AND e.time_from < ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, (System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS));
			preparedStatement.setInt(3, (page - 1) * limit);
			preparedStatement.setInt(4, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}			

			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_HOSTING_EVENTS_FOR_USER);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getAllFutureHostingEventsForUser(int userId, int limit, int page) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.events WHERE owner_id = ? AND time_from >= ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, (System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS));
			preparedStatement.setInt(3, (page - 1) * limit);
			preparedStatement.setInt(4, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_HOSTING_EVENTS_FOR_USER);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getAllPastAttendingEventsForUser(int userId, int limit, int page) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT e.* FROM leaps.events e WHERE e.event_id in (SELECT uae.event_id FROM leaps.users_attend_events uae WHERE uae.user_id = ?) AND e.time_from < ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, (System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS));
			preparedStatement.setInt(3, (page - 1) * limit);
			preparedStatement.setInt(4, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_ATTENDING_EVENTS_FOR_USER);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getAllFutureAttendingEventsForUser(int userId, int limit, int page) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT e.* FROM leaps.events e	WHERE e.event_id IN  (SELECT uae.event_id FROM leaps.users_attend_events uae WHERE uae.user_id = ?) AND e.time_from >= ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, (System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS));
			preparedStatement.setInt(3, (page - 1) * limit);
			preparedStatement.setInt(4, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_ATTENDING_EVENTS_FOR_USER);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int getTheTotalNumberOfPastAttendingEvents(int userId) throws EventException {
		int counter = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT count(e.event_id) as 'total_number' FROM leaps.events e WHERE e.event_id IN (SELECT uae.event_id FROM leaps.users_attend_events uae WHERE uae.user_id = ?) AND e.time_from < ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				counter = rs.getInt("total_number");
			}			
			
			return counter;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_NUMBER_OF_PAST_ATTENDING_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int insertUserImageIntoDB(long userId, String fileName) throws ImageException {
		int imageId = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet generatedKeys = null;
		
		String selectSQL = "INSERT INTO leaps.user_images (user_id, file_name) VALUES ( ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setLong(1, userId);
			preparedStatement.setString(2, fileName);
			
			preparedStatement.execute();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	imageId = generatedKeys.getInt(1);
            }
    		
    		return imageId;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		} finally {
			closeResources(preparedStatement, generatedKeys, dbConnection);
		}
	}

	public boolean removeUserImageFromDB(int imageId) throws ImageException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.user_images where image_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, imageId);
			
			preparedStatement.execute();
			
			return true;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_REMOVE_IMAGE_FROM_DB);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public String getUserImageNameById(int imageId) throws ImageException {
		String imageName = null;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		String selectSQL = "SELECT file_name FROM leaps.user_images WHERE image_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, imageId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				imageName = rs.getString("file_name");
			}
			
			return imageName;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_RETRIEVE_IMAGE_FROM_DB);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}
	
	public int insertEventImageIntoDB(long eventId, String fileName) throws ImageException {
		int imageId = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet generatedKeys = null;
		
		String selectSQL = "INSERT INTO leaps.event_images (event_id, file_name) VALUES ( ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setLong(1, eventId);
			preparedStatement.setString(2, fileName);
			
			preparedStatement.execute();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	imageId = generatedKeys.getInt(1);
            }
    		
    		return imageId;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		} finally {
			closeResources(preparedStatement, generatedKeys, dbConnection);
		}
	}

	public String getEventImageNameById(int imageId) throws ImageException {
		String imageName = null;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT file_name FROM leaps.event_images WHERE image_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, imageId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				imageName = rs.getString("file_name");
			}
			
			return imageName;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_RETRIEVE_IMAGE_FROM_DB);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public boolean removeEventImageFromDB(int imageId) throws ImageException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.event_images where image_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, imageId);
			
			preparedStatement.execute();
			
			return true;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_REMOVE_IMAGE_FROM_DB);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public boolean insertUserMainImageIntoDB(long userId, String fileName) throws ImageException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "UPDATE leaps.users SET profile_image_url = ? WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, fileName);
			preparedStatement.setLong(2, userId);
			
			preparedStatement.executeUpdate();
			
			return true;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public boolean insertEventMainImageIntoDB(long eventId, String fileName) throws ImageException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "UPDATE leaps.events SET event_image_url = ? WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, fileName);
			preparedStatement.setLong(2, eventId);
			
			preparedStatement.executeUpdate();
			
			return true;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public boolean checkIfUserAlreadyAttendsAnEvent(long userId, long eventId) throws UserException {
		boolean success = false;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT user_id FROM leaps.users_attend_events WHERE user_id = ? AND event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, eventId);
			
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				success = true;
			}

			return success;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_CHECK_IF_USER_ATTENDS_EVENT);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int getAllEventCountThatUserHasAttended(long userId) throws EventException {
		int counter = 0;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "select count(event_id) as 'attends' FROM leaps.users_attend_events WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				counter = rs.getInt("attends");
			}

			return counter;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_EVENT_ATTENDEES);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<User> getAllTrainersWithMostEventsCreated() throws UserException {
		List<User> users = new ArrayList<User>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT u.user_id, u.username, u.email_address, u.password, u.age, u.gender, u.location, u.max_distance_setting, u.first_name, u.last_name, u.birthday,"
				+ " u.description, u.profile_image_url, u.is_trainer, u.facebook_id, u.google_id, u.phone_number, u.years_of_training, u.session_price, u.long_description, u.firebase_token"
				+ " FROM leaps.events e"
				+ " LEFT JOIN leaps.users u"
				+ " ON u.user_id = e.owner_id"
				+ " WHERE u.is_trainer = true"
				+ " GROUP BY e.owner_id"
				+ " ORDER BY count(e.owner_id)"
				+ " DESC LIMIT ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, Configuration.TRAINER_FEED_LIMIT_SIZE);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				users.add(retrieveUser(rs));
			}
			
			return users;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_RETRIEVE_USERS_FROM_DB);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getMostPopularEvents(int page, int limit) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		long currentTime = System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT *"
						 + " FROM leaps.events e"
						 + " LEFT JOIN leaps.users_attend_events uae"
						 + " ON uae.event_id = e.event_id"
						 + " WHERE e.time_from >= ?"
						 + " GROUP BY uae.event_id"
						 + " HAVING count(uae.user_id) > 0"
						 + " ORDER BY count(uae.event_id)"
						 + " DESC LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, currentTime);
			preparedStatement.setInt(2, (page - 1) * limit);
			preparedStatement.setInt(3, limit);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_MOST_POPULAR_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getAllEUpcommingEvents() throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.events WHERE time_from >= ?";
		
		long currentTime = System.currentTimeMillis();
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, currentTime);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_UPCOMMING_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getNearbyUpcommingEvents(double latitude, double longitude,int page, int limit) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		long currentTime = System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.events WHERE time_from >= ? ORDER BY ABS(ABS(coord_lat - ?) + ABS(coord_lnt - ?)) ASC LIMIT ?, ?";
				
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, currentTime);
			preparedStatement.setDouble(2, latitude);
			preparedStatement.setDouble(3, longitude);
			preparedStatement.setInt(4, (page - 1) * limit);
			preparedStatement.setInt(5, limit);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_NEARBY_UPCOMMING_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getFilteredEventsWithCoordinates(String keyWord, double latitude, double longitude, int distance,
		List<String> tags, long minStartingDate, long maxStartingDate, String maxStartingDateString, int page, int limit) throws EventException {
		List<Event> events = new ArrayList<Event>();
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();		
		
		StringBuilder selectStatement = new StringBuilder("SELECT e.*, (ABS(ABS(e.coord_lat - ?) + ABS(e.coord_lnt - ?))) as 'distance' FROM leaps.events e WHERE e.time_from >= ? AND e.time_from <= ?");
		params.add(latitude);
		types.add("double");
		params.add(longitude);
		types.add("double");
		params.add(minStartingDate);
		types.add("long");
		params.add(maxStartingDate);
		types.add("long");
		
		if (keyWord != null) {
			selectStatement.append(" AND (e.title LIKE ? OR e.description LIKE ? OR e.address LIKE ?)");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
		}
		
		if (!tags.isEmpty()) {
			selectStatement.append(" AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN (SELECT t.tag_id FROM leaps.tags t WHERE");
			for (int i = 0; i < tags.size(); i++) {
				selectStatement.append(" t.name = ?");
				params.add(tags.get(i));
				types.add("string");
				
				if (i + 1 < tags.size()) {
					selectStatement.append(" OR");
				}
			}
			selectStatement.append("))");
		}
		
		selectStatement.append(" HAVING distance < ? ORDER BY distance ASC LIMIT ?, ?");
		params.add(distance);
		types.add("int");
		params.add((page - 1) * limit);
		types.add("int");
		params.add(limit);
		types.add("int");
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectStatement.toString());
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i).equals("string")) {
					preparedStatement.setString(i+1, String.valueOf(params.get(i)));
				} else if (types.get(i).equals("int")) {
					preparedStatement.setInt(i+1, Integer.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("long")) {
					preparedStatement.setLong(i+1, Long.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("double")) {
					preparedStatement.setDouble(i+1, Double.valueOf(String.valueOf(params.get(i))));
				}
			}
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_FILTERED_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getFilteredEvents(String keyWord, int distance, List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws EventException {
		List<Event> events = new ArrayList<Event>();
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
				
		StringBuilder selectStatement = new StringBuilder("SELECT e.* FROM leaps.events e WHERE e.time_from >= ? AND e.time_from <= ?");
		params.add(minStartingDate);
		types.add("long");
		params.add(maxStartingDate);
		types.add("long");
		
		if (keyWord != null) {
			selectStatement.append(" AND (e.title LIKE ? OR e.description LIKE ? OR e.address LIKE ?)");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
		}
		
		if (!tags.isEmpty()) {
			selectStatement.append(" AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN (SELECT t.tag_id FROM leaps.tags t WHERE");
			for (int i = 0; i < tags.size(); i++) {
				selectStatement.append(" t.name = ?");
				params.add(tags.get(i));
				types.add("string");
				
				if (i + 1 < tags.size()) {
					selectStatement.append(" OR");
				}
			}
			selectStatement.append("))");
		}
		
		selectStatement.append(" ORDER BY e.event_id ASC LIMIT ?, ?");
		params.add((page - 1) * limit);
		types.add("int");
		params.add(limit);
		types.add("int");
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectStatement.toString());
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i).equals("string")) {
					preparedStatement.setString(i+1, String.valueOf(params.get(i)));
				} else if (types.get(i).equals("int")) {
					preparedStatement.setInt(i+1, Integer.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("long")) {
					preparedStatement.setLong(i+1, Long.valueOf(String.valueOf(params.get(i))));
				}
			}
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}			

			return events;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_FILTERED_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<User> getFilteredTrainersByMostEventsWithCoordinates(String keyWord, double latitude, double longitude,
			int distance, List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws UserException {
		List<User> trainers = new ArrayList<User>();
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();	
		ResultSet rs = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT u.*, (ABS(ABS(e.coord_lat - ?) + ABS(e.coord_lnt - ?))) as 'distance' FROM leaps.users u"
				+ " LEFT JOIN leaps.events e ON u.user_id = e.owner_id WHERE e.time_from >= ? AND e.time_from <= ?");
		params.add(latitude);
		types.add("double");
		params.add(longitude);
		types.add("double");
		params.add(minStartingDate);
		types.add("long");
		params.add(maxStartingDate);
		types.add("long");
		
		if (keyWord != null) {
			selectStatement.append(" AND (u.first_name LIKE ? OR u.last_name LIKE ? OR u.description LIKE ? OR u.long_description LIKE ?)");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
		}
		
		if (!tags.isEmpty()) {
			selectStatement.append(" AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN (SELECT t.tag_id FROM leaps.tags t WHERE");
			for (int i = 0; i < tags.size(); i++) {
				selectStatement.append(" t.name = ?");
				params.add(tags.get(i));
				types.add("string");
				
				if (i + 1 < tags.size()) {
					selectStatement.append(" OR");
				}
			}
			selectStatement.append("))");
		}
		
		selectStatement.append(" GROUP BY u.user_id HAVING distance < ? ORDER BY count(e.owner_id) DESC LIMIT ?, ?");
		params.add(distance);
		types.add("int");
		params.add((page - 1) * limit);
		types.add("int");
		params.add(limit);
		types.add("int");
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
				
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectStatement.toString());
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i).equals("string")) {
					preparedStatement.setString(i+1, String.valueOf(params.get(i)));
				} else if (types.get(i).equals("int")) {
					preparedStatement.setInt(i+1, Integer.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("long")) {
					preparedStatement.setLong(i+1, Long.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("double")) {
					preparedStatement.setDouble(i+1, Double.valueOf(String.valueOf(params.get(i))));
				}
			}
			
			if (Configuration.debugMode) {
				logger.info("------------------------------------");
				logger.info("DB statement:");
				logger.info(preparedStatement.toString());
				logger.info("------------------------------------");
			}
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				trainers.add(retrieveUser(rs));
			}
			
			return trainers;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_RETRIEVE_FILTERED_TRAINERS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<User> getFilteredTrainersByMostEvents(String keyWord, int distance, List<String> tags,
			long minStartingDate, long maxStartingDate, int page, int limit) throws UserException {
		List<User> trainers = new ArrayList<User>();
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT u.* FROM leaps.users u LEFT JOIN leaps.events e ON u.user_id = e.owner_id WHERE e.time_from >= ? AND e.time_from <= ?");
		params.add(minStartingDate);
		types.add("long");
		params.add(maxStartingDate);
		types.add("long");
		
		if (keyWord != null) {
			selectStatement.append(" AND (u.first_name LIKE ? OR u.last_name LIKE ? OR u.description LIKE ? OR u.long_description LIKE ?)");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
		}
		
		if (!tags.isEmpty()) {
			selectStatement.append(" AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN (SELECT t.tag_id FROM leaps.tags t WHERE");
			for (int i = 0; i < tags.size(); i++) {
				selectStatement.append(" t.name = ?");
				params.add(tags.get(i));
				types.add("string");
				
				if (i + 1 < tags.size()) {
					selectStatement.append(" OR");
				}
			}
			selectStatement.append("))");
		}
		
		selectStatement.append(" GROUP BY u.user_id ORDER BY count(e.owner_id) DESC LIMIT ?, ?");
		params.add((page - 1) * limit);
		types.add("int");
		params.add(limit);
		types.add("int");
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectStatement.toString());
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i).equals("string")) {
					preparedStatement.setString(i+1, String.valueOf(params.get(i)));
				} else if (types.get(i).equals("int")) {
					preparedStatement.setInt(i+1, Integer.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("long")) {
					preparedStatement.setLong(i+1, Long.valueOf(String.valueOf(params.get(i))));
				}
			}
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				trainers.add(retrieveUser(rs));
			}
			
			return trainers;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_RETRIEVE_FILTERED_TRAINERS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int countFilteredTrainersByMostEventsWithCoordinates(String keyWord, double latitude, double longitude,
			int distance, List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws UserException {
		int count = 0;
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();	
		ResultSet rs = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT COUNT(*) FROM (SELECT u.*, (ABS(ABS(e.coord_lat - ?) + ABS(e.coord_lnt - ?))) as 'distance' FROM leaps.users u"
				+ " LEFT JOIN leaps.events e ON u.user_id = e.owner_id WHERE e.time_from >= ? AND e.time_from <= ?");
		params.add(latitude);
		types.add("double");
		params.add(longitude);
		types.add("double");
		params.add(minStartingDate);
		types.add("long");
		params.add(maxStartingDate);
		types.add("long");
		
		if (keyWord != null) {
			selectStatement.append(" AND (u.first_name LIKE ? OR u.last_name LIKE ? OR u.description LIKE ? OR u.long_description LIKE ?)");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
		}
		
		if (!tags.isEmpty()) {
			selectStatement.append(" AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN (SELECT t.tag_id FROM leaps.tags t WHERE");
			for (int i = 0; i < tags.size(); i++) {
				selectStatement.append(" t.name = ?");
				params.add(tags.get(i));
				types.add("string");
				
				if (i + 1 < tags.size()) {
					selectStatement.append(" OR");
				}
			}
			selectStatement.append("))");
		}
		
		selectStatement.append(" GROUP BY u.user_id HAVING distance < ? ORDER BY count(e.owner_id) DESC LIMIT ?, ?) AS temp");
		params.add(distance);
		types.add("int");
		params.add((page - 1) * limit);
		types.add("int");
		params.add(limit);
		types.add("int");
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
				
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectStatement.toString());
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i).equals("string")) {
					preparedStatement.setString(i+1, String.valueOf(params.get(i)));
				} else if (types.get(i).equals("int")) {
					preparedStatement.setInt(i+1, Integer.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("long")) {
					preparedStatement.setLong(i+1, Long.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("double")) {
					preparedStatement.setDouble(i+1, Double.valueOf(String.valueOf(params.get(i))));
				}
			}
			
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			
			return count;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_RETRIEVE_FILTERED_TRAINERS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int countFilteredTrainersByTheirEvents(String keyWord, int distance, List<String> tags,
			long minStartingDate, long maxStartingDate, int page, int limit) throws UserException {
		int count = 0;
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT COUNT(*) FROM (SELECT u.* FROM leaps.users u LEFT JOIN leaps.events e ON u.user_id = e.owner_id WHERE e.time_from >= ? AND e.time_from <= ?");
		params.add(minStartingDate);
		types.add("long");
		params.add(maxStartingDate);
		types.add("long");
		
		if (keyWord != null) {
			selectStatement.append(" AND (u.first_name LIKE ? OR u.last_name LIKE ? OR u.description LIKE ? OR u.long_description LIKE ?)");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
		}
		
		if (!tags.isEmpty()) {
			selectStatement.append(" AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN (SELECT t.tag_id FROM leaps.tags t WHERE");
			for (int i = 0; i < tags.size(); i++) {
				selectStatement.append(" t.name = ?");
				params.add(tags.get(i));
				types.add("string");
				
				if (i + 1 < tags.size()) {
					selectStatement.append(" OR");
				}
			}
			selectStatement.append("))");
		}
		
		selectStatement.append(" GROUP BY u.user_id ORDER BY count(e.owner_id) DESC LIMIT ?, ?) AS temp");
		params.add((page - 1) * limit);
		types.add("int");
		params.add(limit);
		types.add("int");
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectStatement.toString());
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i).equals("string")) {
					preparedStatement.setString(i+1, String.valueOf(params.get(i)));
				} else if (types.get(i).equals("int")) {
					preparedStatement.setInt(i+1, Integer.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("long")) {
					preparedStatement.setLong(i+1, Long.valueOf(String.valueOf(params.get(i))));
				}
			}
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt(1);
			}
			
			return count;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new UserException(Configuration.CANNOT_RETRIEVE_FILTERED_TRAINERS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int getFilteredEventsCountWithCoordinates(String keyWord, double latitude, double longitude, int distance,
			List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws EventException {
		int count = 0;
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();		
		ResultSet rs = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT COUNT(*) FROM (SELECT e.*, (ABS(ABS(e.coord_lat - ?) + ABS(e.coord_lnt - ?))) as 'distance' FROM leaps.events e WHERE e.time_from >= ? AND e.time_from <= ?");
		params.add(latitude);
		types.add("double");
		params.add(longitude);
		types.add("double");
		params.add(minStartingDate);
		types.add("long");
		params.add(maxStartingDate);
		types.add("long");
		
		if (keyWord != null) {
			selectStatement.append(" AND (e.title LIKE ? OR e.description LIKE ? OR e.address LIKE ?)");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
		}
		
		if (!tags.isEmpty()) {
			selectStatement.append(" AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN (SELECT t.tag_id FROM leaps.tags t WHERE");
			for (int i = 0; i < tags.size(); i++) {
				selectStatement.append(" t.name = ?");
				params.add(tags.get(i));
				types.add("string");
				
				if (i + 1 < tags.size()) {
					selectStatement.append(" OR");
				}
			}
			selectStatement.append("))");
		}
		
		selectStatement.append(" HAVING distance < ? ORDER BY distance ASC LIMIT ?, ?) AS temp");
		params.add(distance);
		types.add("int");
		params.add((page - 1) * limit);
		types.add("int");
		params.add(limit);
		types.add("int");
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
				
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectStatement.toString());
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i).equals("string")) {
					preparedStatement.setString(i+1, String.valueOf(params.get(i)));
				} else if (types.get(i).equals("int")) {
					preparedStatement.setInt(i+1, Integer.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("long")) {
					preparedStatement.setLong(i+1, Long.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("double")) {
					preparedStatement.setDouble(i+1, Double.valueOf(String.valueOf(params.get(i))));
				}
			}
			
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			
			return count;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_FILTERED_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int getFilteredEventsCount(String keyWord, int distance, List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws EventException {
		int count = 0;
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT COUNT(*) FROM (SELECT e.* FROM leaps.events e WHERE e.time_from >= ? AND e.time_from <= ?");
		params.add(minStartingDate);
		types.add("long");
		params.add(maxStartingDate);
		types.add("long");
		
		if (keyWord != null) {
			selectStatement.append(" AND (e.title LIKE ? OR e.description LIKE ? OR e.address LIKE ?)");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
			params.add("%"+keyWord+"%");
			types.add("string");
		}
		
		if (!tags.isEmpty()) {
			selectStatement.append(" AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN (SELECT t.tag_id FROM leaps.tags t WHERE");
			for (int i = 0; i < tags.size(); i++) {
				selectStatement.append(" t.name = ?");
				params.add(tags.get(i));
				types.add("string");
				
				if (i + 1 < tags.size()) {
					selectStatement.append(" OR");
				}
			}
			selectStatement.append("))");
		}
		
		selectStatement.append(" ORDER BY e.event_id ASC LIMIT ?, ?) AS temp");
		params.add((page - 1) * limit);
		types.add("int");
		params.add(limit);
		types.add("int");
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectStatement.toString());
			for (int i = 0; i < params.size(); i++) {
				if (types.get(i).equals("string")) {
					preparedStatement.setString(i+1, String.valueOf(params.get(i)));
				} else if (types.get(i).equals("int")) {
					preparedStatement.setInt(i+1, Integer.valueOf(String.valueOf(params.get(i))));
				} else if (types.get(i).equals("long")) {
					preparedStatement.setLong(i+1, Long.valueOf(String.valueOf(params.get(i))));
				}
			}
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt(1);
			}
			
			return count;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new EventException(Configuration.CANNOT_RETRIEVE_FILTERED_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<String> getTagsFromTheDB() throws TagException {
		List<String> tags = null;
				
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT name FROM leaps.tags";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			
			rs = preparedStatement.executeQuery();
			tags = new ArrayList<String>();
            while (rs.next()) {
            	tags.add(rs.getString("name"));
            }
    		
    		return tags;
		} catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
	    	
			throw new TagException(Configuration.CANNOT_RETRIEVE_TAGS_FROM_DB);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}
	
	public void followUser(long follower, int followed) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "INSERT INTO leaps.user_followers (follower, followed) VALUES ( ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, follower);
			preparedStatement.setLong(2, followed);
			preparedStatement.execute();
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.USER_IS_ALREADY_FOLLOWED);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public void unfollowUser(Long follower, int followed) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.user_followers WHERE follower = ? AND followed = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, follower);
			preparedStatement.setLong(2, followed);
			preparedStatement.execute();
			
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.USER_DOES_NOT_EXIST);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public Map<String, List<Long>> getFollowingUsers(Long userId) throws EventException {
		Map<String, List<Long>> tempUsers = new HashMap<String, List<Long>>();
		tempUsers.put("follower", new ArrayList<Long>());
		tempUsers.put("followed", new ArrayList<Long>());
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.user_followers WHERE followed = ? OR follower = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, userId);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				if (rs.getLong("follower") == userId && rs.getLong("followed") == userId) {
					continue;
				} else if (rs.getLong("follower") == userId) {
					tempUsers.get("followed").add(rs.getLong("followed"));
				} else if (rs.getLong("followed") == userId) {
					tempUsers.get("follower").add(rs.getLong("follower"));
				}
			}
			
			return tempUsers;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.ERROR_RETREIVING_THE_USERS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public void followEvent(Long userId, int eventId) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "INSERT INTO leaps.event_followers (user_id, event_id) VALUES ( ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, eventId);
			preparedStatement.execute();
			
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.EVENT_IS_ALREADY_FOLLOWED);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public void unfollowEvent(Long userId, int eventId) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.event_followers WHERE user_id = ? AND event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, eventId);
			preparedStatement.execute();
			
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.EVENT_DOES_NOT_EXIST);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public long rateEvent(Rate rate) throws EventException {
		long rateId = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet generatedKeys = null;
		
		String selectSQL = "INSERT INTO leaps.event_rating (event_id, user_id, rating, comment, date_created) VALUES ( ? , ? , ? , ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setLong(1, rate.getEventId());
			preparedStatement.setLong(2, rate.getUserId());
			preparedStatement.setInt(3, rate.getRating());
			preparedStatement.setString(4, rate.getComment());
			preparedStatement.setLong(5, rate.getDateCreated());
			preparedStatement.execute();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	rateId = generatedKeys.getLong(1);
            } else {
            	throw new EventException(Configuration.CANNOT_RATE_CURRENT_EVENT);
            }
			
			return rateId;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RATE_CURRENT_EVENT);
		} finally {
			closeResources(preparedStatement, generatedKeys, dbConnection);
		}
	}

	public List<Rate> getRatesForEvent(long eventId) throws EventException {
		List<Rate> rating = new ArrayList<Rate>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.event_rating WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				rating.add(RateDao.RateDaoEnum.INSTANCE.createRate(rs.getLong("id"), rs.getLong("event_id"), rs.getInt("rating"), rs.getLong("user_id"), rs.getString("comment"), 
																   rs.getLong("date_created"), rs.getString("rating_image_url")));
			}
			
			return rating;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_EVENT_RATINGS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Rate> getRatesForEvent(long eventId, int page, int limit) throws EventException {
		List<Rate> rating = new ArrayList<Rate>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.event_rating WHERE event_id = ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			preparedStatement.setInt(2, (page - 1) * limit);
			preparedStatement.setInt(3, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				rating.add(RateDao.RateDaoEnum.INSTANCE.createRate(rs.getLong("id"), rs.getLong("event_id"), rs.getInt("rating"), rs.getLong("user_id"), rs.getString("comment"), 
																   rs.getLong("date_created"), rs.getString("rating_image_url")));
			}
			
			return rating;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_EVENT_RATINGS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Long> getEventFollowingUsers(long follower, List<User> attending) throws EventException {
		List<Long> followers = new ArrayList<Long>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		StringBuilder selectSQL = new StringBuilder("SELECT * FROM leaps.user_followers WHERE follower = ? AND followed IN (");
		for (int i = 0; i < attending.size(); i++) {
			if (i + 1 < attending.size()) {
				selectSQL.append(" ? ,");
			} else {
				selectSQL.append(" ? )");
			}
		}
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL.toString());
			preparedStatement.setLong(1, follower);
			
			for (int i = 0; i < attending.size(); i++) {
				preparedStatement.setLong(i+2, attending.get(i).getUserId());
			}
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				if (rs.getLong("followed") == follower && rs.getLong("follower") == follower) {
					continue;
				}
				followers.add(rs.getLong("followed"));
			}
			
			return followers;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_EVENT_FOLLOWERS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public void insertRateImageIntoDB(long rateId, String fileName) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "update leaps.event_rating set rating_image_url = ? WHERE id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, fileName);
			preparedStatement.setLong(2, rateId);
			preparedStatement.execute();
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new EventException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public String getRateImageNameById(int id) throws EventException {
		String imageName = null;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT rating_image_url FROM leaps.event_rating WHERE id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, id);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				imageName = rs.getString("rating_image_url");
			}
			
			return imageName;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_IMAGE_FROM_DB);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Event> getAllFollowedEvents(long userId, boolean isFuture) throws EventException {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		long time = System.currentTimeMillis();
		
		String selectSQL = " select e.* from leaps.events e where e.time_from " + (isFuture ? ">=" : "<=") + " ? and event_id in (select ef.event_id from leaps.event_followers ef where ef.user_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, time);
			preparedStatement.setLong(2, userId);
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date"), rs.getString("firebase_topic")));
			}
			
			return events;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_FOLLOWED_EVENTS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}
	
	
	public void updateEvent(Map<String, Map<String, Object>> params, long eventId) throws EventException {		
		if (!params.isEmpty()) {
			Connection dbConnection = null;
			PreparedStatement preparedStatement = null;
			
			StringBuilder selectSQL = new StringBuilder("update leaps.events set ");
			int mapSize = params.size();
			int counter = 0;
			for (Map.Entry<String, Map<String, Object>> map : params.entrySet()) {
				String key = map.getKey();
				selectSQL.append(key + " = ?");
				counter++;
				if (counter < mapSize) {
					selectSQL.append(", ");
				} else {
					selectSQL.append(" WHERE event_id = ?");
				}
			}
			
			try {
				dbConnection = DBManager.getInstance().getConnection();
				preparedStatement = dbConnection.prepareStatement(selectSQL.toString());
				
				int statementCounter = 1;
				for (Map.Entry<String, Map<String, Object>> map : params.entrySet()) {
					for (Map.Entry<String, Object> innerMap : map.getValue().entrySet()) {
						if (innerMap.getKey().equals("string")) {
							preparedStatement.setString(statementCounter++, String.valueOf(innerMap.getValue()));
						} else if (innerMap.getKey().equals("int")) {
							preparedStatement.setInt(statementCounter++, Integer.valueOf(String.valueOf(innerMap.getValue())));
						} else if (innerMap.getKey().equals("long")) {
							preparedStatement.setLong(statementCounter++, Long.valueOf(String.valueOf(innerMap.getValue())));
						} else if (innerMap.getKey().equals("boolean")) {
							preparedStatement.setBoolean(statementCounter++, Boolean.valueOf(String.valueOf(innerMap.getValue())));
						}
					}
				}
				
				preparedStatement.setLong(statementCounter, eventId);				
				preparedStatement.executeUpdate();
		    } catch (Exception e) {
				if (Configuration.debugMode) {
					logger.info("SQL Statement: " + preparedStatement.toString());
				}
				throw new EventException(Configuration.CANNOT_UPDATE_EVENT);
			} finally {
				closeResources(preparedStatement, null, dbConnection);
			}
		}
	}
	
	public void removeEventTagsFromDb(long eventId) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.event_has_tags WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			throw new EventException(Configuration.CANNOT_DELETE_TAGS_FOR_EVENT);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public void deleteEvent(long eventId) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.events WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			throw new EventException(Configuration.CANNOT_DELETE_EVENT);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public Rate getRate(long commentId) throws EventException {
		Rate rate = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT * FROM leaps.event_rating WHERE id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, commentId);
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				rate = RateDao.RateDaoEnum.INSTANCE.createRate(commentId, rs.getLong("event_id"), rs.getInt("rating"), rs.getLong("user_id"), 
															   rs.getString("comment"), rs.getLong("date_created"), rs.getString("rating_image_url"));
			}
			
			return rate;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_RATE);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Long> getRatesForTrainer(long userId, int page, int limit) throws EventException {
		List<Long> rating = new ArrayList<Long>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT id FROM leaps.event_rating WHERE user_id = ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setInt(2, (page - 1) * limit);
			preparedStatement.setInt(3, limit);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				rating.add(rs.getLong("id"));
			}
			
			return rating;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_RATE);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public int getFollowingCount(Long userId) throws EventException {
		int count = -1;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		String selectSQL = "SELECT COUNT(*) FROM leaps.user_followers WHERE follower = ? AND follower != followed";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt(1);
			}
			
			// default 0
			return count >= 0 ? count : 0;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_FOLLOWING_COUNT);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int getFollowersCount(Long userId) throws EventException {
		int count = -1;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT COUNT(*) FROM leaps.user_followers WHERE followed = ? AND follower != followed";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt(1);
			}
			
			// default 0
			return count >= 0 ? count : 0;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_FOLLOWERS_COUNT);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<Integer> getAllUserRatings(Long userId) throws EventException {
		List<Integer> rating = new ArrayList<Integer>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSQL = "SELECT rating FROM leaps.event_rating WHERE event_id IN (SELECT event_id FROM leaps.events WHERE owner_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				rating.add(rs.getInt("rating"));
			}
			
			return rating;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RETRIEVE_EVENT_RATINGS);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public boolean canRate(Long userId, long eventId) throws UserException {
		boolean canRate = true;

		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;

		String selectSql = "SELECT u.user_id FROM leaps.users u WHERE u.user_id IN (SELECT uae.user_id FROM leaps.users_attend_events uae WHERE uae.user_id = ? AND uae.event_id = ?) AND u.user_id IN (SELECT er.user_id FROM leaps.event_rating er WHERE er.user_id = ? AND er.event_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, eventId);
			preparedStatement.setLong(3, userId);
			preparedStatement.setLong(4, eventId);
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				canRate = false;
			}

			return canRate;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<User> getFilteredTrainersByText(String keyWord, int page, int limit) throws UserException {
		List<User> trainers = new ArrayList<User>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSql = "SELECT distinct u.* FROM leaps.users as u WHERE u.is_trainer = 1 AND "
					+ "(u.username LIKE ? OR u.description LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?) LIMIT ?, ?";
		String likeKeyword = "%" + keyWord + "%";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql);
			preparedStatement.setString(1, likeKeyword);
			preparedStatement.setString(2, likeKeyword);
			preparedStatement.setString(3, likeKeyword);
			preparedStatement.setString(4, likeKeyword);
			preparedStatement.setInt(5, (page - 1) * limit);
			preparedStatement.setInt(6, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				trainers.add(retrieveUser(rs));
			}
			
			return trainers;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int countFilteredTrainersByText(String keyWord) throws UserException {
		int count = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSql = "SELECT count(*) as 'count' FROM leaps.users as u WHERE u.is_trainer = 1 AND "
					+ "(u.username LIKE ? OR u.description LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?)";
		String likeKeyword = "%" + keyWord + "%";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql);
			preparedStatement.setString(1, likeKeyword);
			preparedStatement.setString(2, likeKeyword);
			preparedStatement.setString(3, likeKeyword);
			preparedStatement.setString(4, likeKeyword);
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt("count");
			}
			
			return count;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<User> getFilteredTrainersByTags(List<String> tags, int page, int limit) throws UserException {
		List<User> trainers = new ArrayList<User>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		StringBuilder selectSql = new StringBuilder("SELECT distinct u.* FROM leaps.users as u JOIN leaps.events as e ON e.owner_id = u.user_id "
										   + "JOIN leaps.event_has_tags as et ON e.event_id = et.event_id JOIN leaps.tags as t ON et.tag_id = t.tag_id "
										   + "WHERE u.is_trainer = 1 AND (");
		for (int i = 0; i < tags.size(); i++) {
			selectSql.append("t.name = ?");
			if (i + 1 < tags.size()) {
				selectSql.append(" OR ");
			}
		}
		selectSql.append(") UNION SELECT distinct u.* "
						 + "FROM leaps.users as u LEFT JOIN leaps.events as e ON e.owner_id = u.user_id WHERE e.owner_id IS NULL LIMIT ?, ?");
		
		int counter = 1;
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql.toString());
			for (int i = 0; i < tags.size(); i++) {
				preparedStatement.setString(counter++, tags.get(i));
			}
			preparedStatement.setInt(counter++, (page - 1) * limit);
			preparedStatement.setInt(counter++, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				trainers.add(retrieveUser(rs));
			}
			
			return trainers;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int countFilteredTrainersByTags(List<String> tags) throws UserException {
		int result = 0;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		StringBuilder selectSql = new StringBuilder("SELECT distinct u.* FROM leaps.users as u JOIN leaps.events as e ON e.owner_id = u.user_id "
										   + "JOIN leaps.event_has_tags as et ON e.event_id = et.event_id JOIN leaps.tags as t ON et.tag_id = t.tag_id "
										   + "WHERE u.is_trainer = 1 AND (");
		for (int i = 0; i < tags.size(); i++) {
			selectSql.append("t.name = ?");
			if (i + 1 < tags.size()) {
				selectSql.append(" OR ");
			}
		}
		selectSql.append(") UNION SELECT distinct u.* "
						 + "FROM leaps.users as u LEFT JOIN leaps.events as e ON e.owner_id = u.user_id WHERE e.owner_id IS NULL");
		
		int counter = 1;
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql.toString());
			for (int i = 0; i < tags.size(); i++) {
				preparedStatement.setString(counter++, tags.get(i));
			}
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				result++;
			}
			
			return result;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	
	public List<User> getAllFilteredTrainers(int page, int limit) throws UserException {
		List<User> trainers = new ArrayList<User>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSql = "SELECT * FROM leaps.users WHERE is_trainer = TRUE LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql);
			preparedStatement.setInt(1, (page - 1) * limit);
			preparedStatement.setInt(2, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				trainers.add(retrieveUser(rs));
			}
			
			return trainers;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	
	
	public int countAllFilteredTrainers() throws UserException {
		int count = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String selectSql = "SELECT count(user_id) as 'count' FROM leaps.users WHERE is_trainer = TRUE";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql);
			
			rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt("count");
			}
			
			return count;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public List<User> getFilteredTrainersByTextAndTags(String keyWord, List<String> tags, int page, int limit) throws UserException {
		List<User> trainers = new ArrayList<User>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		StringBuilder selectSql = new StringBuilder("SELECT distinct u.* FROM leaps.users as u JOIN leaps.events as e "
												  + "ON e.owner_id = u.user_id JOIN leaps.event_has_tags as et ON e.event_id = et.event_id "
												  + "JOIN leaps.tags as t ON et.tag_id = t.tag_id WHERE u.is_trainer = 1  AND (");
		for (int i = 0; i < tags.size(); i++) {
			selectSql.append("t.name = ?");
			if (i + 1 < tags.size()) {
				selectSql.append(" OR ");
			}
		}
		selectSql.append(") AND (u.username LIKE ? OR u.description LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?) "
					   + "UNION SELECT distinct u.* FROM leaps.users as u LEFT JOIN leaps.events as e ON e.owner_id = u.user_id WHERE e.owner_id IS NULL "
					   + "AND (u.username LIKE ? OR u.description LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?) LIMIT ?, ?");
		
		String likeKeyword = "%" + keyWord + "%";
		int counter = 1;
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql.toString());
			for (int i = 0; i < tags.size(); i++) {
				preparedStatement.setString(counter++, tags.get(i));
			}
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setInt(counter++, (page - 1) * limit);
			preparedStatement.setInt(counter++, limit);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				trainers.add(retrieveUser(rs));
			}
			
			return trainers;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public int countFilteredTrainersByTextAndTags(String keyWord, List<String> tags) throws UserException {
		int result = 0;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		StringBuilder selectSql = new StringBuilder("SELECT distinct u.* FROM leaps.users as u JOIN leaps.events as e "
												  + "ON e.owner_id = u.user_id JOIN leaps.event_has_tags as et ON e.event_id = et.event_id "
												  + "JOIN leaps.tags as t ON et.tag_id = t.tag_id WHERE u.is_trainer = 1  AND (");
		for (int i = 0; i < tags.size(); i++) {
			selectSql.append("t.name = ?");
			if (i + 1 < tags.size()) {
				selectSql.append(" OR ");
			}
		}
		selectSql.append(") AND (u.username LIKE ? OR u.description LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?) "
					   + "UNION SELECT distinct u.* FROM leaps.users as u LEFT JOIN leaps.events as e ON e.owner_id = u.user_id WHERE e.owner_id IS NULL "
					   + "AND (u.username LIKE ? OR u.description LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?)");
		
		String likeKeyword = "%" + keyWord + "%";
		int counter = 1;
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql.toString());
			for (int i = 0; i < tags.size(); i++) {
				preparedStatement.setString(counter++, tags.get(i));
			}
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			preparedStatement.setString(counter++, likeKeyword);
			
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				result++;
			}
			
			return result;
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new UserException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public void removeUserSpecialtiesFromDb(Long userId) throws UserException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.specialties WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			throw new UserException(Configuration.CANNOT_DELETE_SPECIALTIES_FOR_USER);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public void addUserSpecialtiesToTheDB(List<String> specialties, Long userId) throws UserException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		StringBuilder selectTagsStatement = new StringBuilder("INSERT INTO leaps.specialties ( user_id , name ) VALUES ");
		for (int i = 0; i < specialties.size(); i++) {
			selectTagsStatement.append("( ? , ? )");
			if (i + 1 < specialties.size()) {
				selectTagsStatement.append(", ");
			}
		}

		int counter = 1;
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectTagsStatement.toString());
			for (int i = 0; i < specialties.size(); i++) {
				preparedStatement.setLong(counter++, userId);
				preparedStatement.setString(counter++, specialties.get(i));
			}
			
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new UserException(Configuration.CANNOT_INSERT_SPECIALTIES_IN_THE_DB);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public long createRepeatingEvent(RepeatingEvent repeatingEvents, long parentId) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet generatedKeys = null;
		long id = -1;
		
		String selectSQL = "INSERT INTO leaps.repeating_events ( parent_event_id , event_start_time , event_end_time , exist) VALUES ( ? , ? , ? , ? )";

		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setLong(1, parentId);
			preparedStatement.setLong(2, repeatingEvents.getStartTime());
			preparedStatement.setLong(3, repeatingEvents.getEndTime());
			preparedStatement.setBoolean(4, repeatingEvents.isExist());
			
			preparedStatement.execute();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	id = generatedKeys.getLong(1);
            }
            
            return id;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new EventException(Configuration.CANNOT_INSERT_SPECIALTIES_IN_THE_DB);
		} finally {
			closeResources(preparedStatement, generatedKeys, dbConnection);
		}
	}

	public List<RepeatingEvent> getScheduledRepeatingEvents() throws EventException {
		List<RepeatingEvent> events = new ArrayList<RepeatingEvent>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		long currentTime = System.currentTimeMillis();
		
		String selectSQL = "SELECT * FROM leaps.repeating_events WHERE event_start_time < ? AND exist = false";

		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, currentTime);
			rs = preparedStatement.executeQuery();
			
			while(rs.next()) {
				events.add(new RepeatingEvent(rs.getLong("parent_event_id"), rs.getLong("event_start_time"), rs.getLong("event_end_time"), rs.getBoolean("exist"), rs.getLong("id")));
			}
			
			return events;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new EventException(Configuration.CANNOT_INSERT_SPECIALTIES_IN_THE_DB);
		} finally {
			closeResources(preparedStatement, rs, dbConnection);
		}
	}

	public boolean updateRepeatingEvent(long id, long parentId) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String selectSQL = "update leaps.repeating_events set event_id = ?, exist = ? WHERE id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, parentId);
			preparedStatement.setBoolean(2, true);
			preparedStatement.setLong(3, id);
			preparedStatement.execute();
			
			return true;
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new EventException(Configuration.NO_USER_FOUND);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}

	public void updateUserFirebaseToken(long userId, String firebaseToken) throws UserException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String selectSQL = "UPDATE leaps.users SET firebase_token = ? WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, firebaseToken);
			preparedStatement.setLong(2, userId);
			preparedStatement.execute();
		} catch (Exception e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			
			throw new UserException(Configuration.ERROR_UPDATING_FIREBASE_TOKEN);
		} finally {
			closeResources(preparedStatement, null, dbConnection);
		}
	}
}
