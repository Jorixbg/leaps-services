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

import com.leaps.controller.UserController;
import com.leaps.interfaces.IDBUserDao;
import com.leaps.model.event.Event;
import com.leaps.model.event.EventDao;
import com.leaps.model.event.Tag;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.image.Image;
import com.leaps.model.image.ImageDao;
import com.leaps.model.rate.Rate;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.LeapsUtils;

public class DBUserDao implements IDBUserDao {

	private static final Logger logger = LoggerFactory.getLogger(DBUserDao.class);
	
	private static DBUserDao instance;
	private DBManager manager;
	
	private DBUserDao() {
		manager = DBManager.getInstance();
	}
	
	public static DBUserDao getInstance(){
		if(instance == null)
			instance = new DBUserDao();
		return instance;
	}
	
	public boolean checkIfUserExistInDB(String userData) {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
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
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				return true;
			}
			
		} catch (Exception e) {
			// TODO
			return true;
		}
		return false;
	}
	
	public boolean checkIfUserExistInDBByEmail(String email) {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String selectSQL = "SELECT user_id FROM leaps.users WHERE email_address = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				return true;
			}
			
		} catch (Exception e) {
			// TODO
			return true;
		}
		return false;
	}
	
	public User getUserFromDb(String userData, String pass, String facebookId, String googleId) {
		User user = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
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
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				user = UserDao.getInstance().createNewUser(rs.getInt("user_id"), rs.getString("username"), rs.getString("email_address"), rs.getInt("age"), rs.getString("gender"),
						   rs.getString("location"), rs.getInt("max_distance_setting"), rs.getString("first_name"), rs.getString("last_name"), 
						   rs.getLong("birthday"), rs.getString("description"), rs.getString("profile_image_url"), rs.getBoolean("is_trainer"), 
						   rs.getString("facebook_id"), rs.getString("google_id"), rs.getString("phone_number"),
						   rs.getInt("session_price"), rs.getString("long_description"), rs.getInt("years_of_training"));
			}
			
			return user;
		} catch (Exception e) {
			// TODO
		}
		
		return null;
	}
	
	public Long insertUserIntoDB(String username, String pass, String email, String firstName, String lastName, Long birthday, String facebookId, String gogleId, int age) throws UserException {
		Long userId = -1L;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String selectSQL = "INSERT INTO leaps.users (username, password, email_address, first_name, last_name, birthday, facebook_id, google_id, age, max_distance_setting) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ?)";
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
			
			preparedStatement.execute();
			
			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	userId = generatedKeys.getLong(1);
    			if (Configuration.debugMode) {
    				logger.info("User Id: " + userId);
    			}
            }
            
            return userId;
		} catch (Exception e) {
			throw new UserException(e.getMessage());
		} finally {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<String> findSimilarUsernamesFromDB(String username) {
		List<String> returnedData = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String selectSQL = "SELECT username FROM leaps.users WHERE username LIKE ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, username);

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			ResultSet dbUsernames = preparedStatement.executeQuery();
            if (dbUsernames.next()) {
            	returnedData.add(dbUsernames.getString(1));
            	if (Configuration.debugMode) {
    				logger.info("Username: " + dbUsernames.getString(1));
    			}
            }
		} catch (Exception e) {
			System.out.println(e.getMessage());
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

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			return true;
		} catch (Exception e) {
			throw new UserException(e.getMessage());
		}
	}

	public List<Tag> getUserTokens(long ownerId, int tokenSizeForCreateEvent) {
		List<Tag> tags = new ArrayList<Tag>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		String selectSQL = "SELECT s.name FROM leaps.specialties s "
						 + "LEFT JOIN leaps.users u ON s.user_id = u.user_id "
						 + "WHERE s.user_id = ? LIMIT ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, ownerId);
			preparedStatement.setInt(2, tokenSizeForCreateEvent);

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
            	tags.add(EventDao.getInstance().createNewTag(rs.getInt("specialty_id"), rs.getString("name"), rs.getInt("user_id")));
            }
            
			if (Configuration.debugMode) {
				LeapsUtils.logRetrievedTagsFromTheDB(tags);
			}
			
			return tags;
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		return tags;
	}

	public long createNewEvent(String title, String description, long date, long timeFrom, long timeTo, long ownerId, double latitude, 
							   double longitute, int priceFrom, String address, int freeSlots, Long dateCreated) {
		long eventId = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "INSERT INTO leaps.events (title, description, date, time_from, time_to, owner_id, coord_lat, coord_lnt, "
												   + "price_from, address, free_slots, date_created, event_image_url)"
												   + "VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?)";
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
			
			preparedStatement.execute();

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	eventId = generatedKeys.getLong(1);
            }
            
            return eventId;
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return eventId;
	}

	public boolean addTagsToTheDB(List<String> tags, long eventId) {
		boolean success = true;
		List<Tag> dbTags = new ArrayList<Tag>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
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
			
        	ResultSet generatedKeys = preparedStatement.executeQuery();

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
        	while (generatedKeys.next()) {
            	dbTags.add(EventDao.getInstance().createNewTag(generatedKeys.getInt(1), generatedKeys.getString(2)));
        	}
            
			if (Configuration.debugMode) {
				logger.info("check if some of the tags already exist in the database 'tags' schema and retreive their ids");
				LeapsUtils.logRetrievedTagsFromTheDB(dbTags);
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
			return false;
		}
		
		List<String> tempTags = new ArrayList<String>();
		
		// remove the tags from the tags array that ids were found 
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
			try {
				int index = 0;
				preparedStatement = dbConnection.prepareStatement(insertTagsStatement.toString(), Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < tempTags.size(); i++) {
					preparedStatement.setString(i + 1, tempTags.get(i));
				}
				preparedStatement.execute();

				if (Configuration.debugMode) {
					logger.info("SQL Statement: " + preparedStatement.toString());
				}
				
				ResultSet rs = preparedStatement.getGeneratedKeys();
	            while (rs.next()) {
	            	dbTags.add(EventDao.getInstance().createNewTag(rs.getInt(1), tags.get(index++)));
	            }
				
				if (Configuration.debugMode) {
					logger.info("add the tags that are not registered in the db in 'tags' schema and get their ids");
					LeapsUtils.logRetrievedTagsFromTheDB(dbTags);
				}
			} catch (Exception e) {
				// TODO: proper exception
				System.out.println(e.getMessage());
				return false;
			}
		}
		
		
		// add all tags in 'event_has_tags' schema
		StringBuilder insertEventTagsStatement =  new StringBuilder("INSERT INTO leaps.event_has_tags ( tag_id, event_id ) VALUES");
		for (int i = 0; i < dbTags.size(); i++) {
			insertEventTagsStatement.append(" ( ?, ? )" );
			if (i + 1 < dbTags.size()) {
				insertEventTagsStatement.append(",");
			}
		}
		
		try {
			preparedStatement = dbConnection.prepareStatement(insertEventTagsStatement.toString());
			int temp = 1;
			for (int i = 0; i < dbTags.size(); i++) {
				preparedStatement.setInt(temp++, dbTags.get(i).getTagId());
				preparedStatement.setInt(temp++, (int) eventId);
			}
			
			preparedStatement.execute();

			if (Configuration.debugMode) {
				logger.info("add all tags in 'event_has_tags' schema");
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
			return false;
		}
		
		return success;
	}

	public List<String> getMostPopularTags() {
		List<String> tags = new ArrayList<String>();
				
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT t.name, count(eht.tag_id) FROM leaps.event_has_tags eht LEFT JOIN leaps.tags t ON t.tag_id = eht.tag_id GROUP BY eht.tag_id ORDER BY eht.tag_id ASC LIMIT ?";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, Configuration.TAG_SELECT_LIMIT);

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
            	tags.add(rs.getString("name"));
            }

			if (Configuration.debugMode) {
				LeapsUtils.logRetrievedTagsFromTheDBAsString(tags);
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return tags;
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

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
            while (rs.next()) {
            	user = UserDao.getInstance().createNewUser(userId, rs.getString("username"), rs.getString("email_address"), rs.getInt("age"), rs.getString("gender"), rs.getString("location"), 
            								rs.getInt("max_distance_setting"), rs.getString("first_name"), rs.getString("last_name"), rs.getLong("birthday"), rs.getString("description"), 
            								rs.getString("profile_image_url"), rs.getBoolean("is_trainer"), rs.getString("facebook_id"), rs.getString("google_id"), rs.getString("phone_number"), 
            								rs.getInt("session_price"), rs.getString("long_description"), rs.getInt("years_of_training"));
            }
        } catch (Exception e) {
			throw new UserException(Configuration.ERROR_RETREIVING_THE_USERS);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return user;
	}

	public List<Tag> getAllUserSpecialtiesFromDb(long userId) {
		List<Tag> specialties = new ArrayList<Tag>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSql = "Select specialty_id, name from leaps.specialties WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSql);
			preparedStatement.setLong(1, userId);
			
			ResultSet rs = preparedStatement.executeQuery();

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			while (rs.next()) {
				specialties.add(EventDao.getInstance().createNewTag(rs.getInt("specialty_id"),rs.getString("name"), userId));
			}


			if (Configuration.debugMode) {
				LeapsUtils.logRetrievedTagsFromTheDB(specialties);
			}
	    } catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
	    
		return specialties;
	}

	public Event getEventById(long eventId) throws EventException {
		Event event = null;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT title, description, date, time_from, time_to, owner_id, coord_lat, coord_lnt, "
				   + "price_from, address, free_slots, event_image_url FROM leaps.events WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			ResultSet rs = preparedStatement.executeQuery();

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			while (rs.next()) {
				event = EventDao.getInstance().generateNewEvent(eventId, rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"),
								 rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), rs.getString("address"),
						 rs.getInt("free_slots"), rs.getLong("date"));
				if (Configuration.debugMode) {
					LeapsUtils.logRetrievedEventsFromTheDB(event);
				}
			}
			
			return event;
	    } catch (Exception e) {
	    	if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.EVENT_DOES_NOT_EXIST);
		}
	}

	public List<User> getAllAttendingUsersForEvent(long eventId) {
		List<User> users = new ArrayList<User>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "select u.user_id, u.username, u.email_address, u.password, u.age, u.gender, u.location, u.max_distance_setting, u.first_name, u.last_name, u.birthday, u.description,"
						 + " u.profile_image_url, u.is_trainer, u.facebook_id, u.google_id, u.phone_number, u.years_of_training, u.session_price, u.long_description"
						 + " from leaps.users u WHERE u.user_id IN (SELECT uae.user_id FROM leaps.users_attend_events uae WHERE uae.event_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			ResultSet rs = preparedStatement.executeQuery();

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			while (rs.next()) {
				users.add(UserDao.getInstance().createNewUser(rs.getInt("user_id"), rs.getString("username"), rs.getString("email_address"), rs.getInt("age"), rs.getString("gender"),
						   rs.getString("location"), rs.getInt("max_distance_setting"), rs.getString("first_name"), rs.getString("last_name"), 
						   rs.getLong("birthday"), rs.getString("description"), rs.getString("profile_image_url"), rs.getBoolean("is_trainer"), 
						   rs.getString("facebook_id"), rs.getString("google_id"), rs.getString("phone_number"),
						   rs.getInt("session_price"), rs.getString("long_description"), rs.getInt("years_of_training")));
			}
	    } catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return users;
	}

	public Map<String, String> getAllEventImages(long eventId) {
		Map<String, String> images = new HashMap<String, String>();
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT image_id, file_name FROM leaps.event_images WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			ResultSet rs = preparedStatement.executeQuery();

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			while (rs.next()) {
				images.put(rs.getString("image_id"), rs.getString("file_name"));
			}

			if (Configuration.debugMode) {
				for (Map.Entry<String, String> map : images.entrySet()) {
					LeapsUtils.logRetrievedImageFromTheDB(map.getKey(), map.getValue());
				}
			}
	    } catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return images;
	}

	public List<Tag> getAllEventTagsFromDb(long eventId) {
		List<Tag> tags = new ArrayList<Tag>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT t.name, t.tag_id FROM leaps.tags t WHERE t.tag_id IN (SELECT eht.tag_id FROM leaps.event_has_tags eht WHERE eht.event_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, (int)eventId);
						
			ResultSet rs = preparedStatement.executeQuery();

			if (Configuration.debugMode) {
				logger.info("SQL Statement: " + preparedStatement.toString());
			}
			
			while (rs.next()) {
				tags.add(EventDao.getInstance().createNewTag(rs.getInt("tag_id"), rs.getString("name")));
			}
			
			if (Configuration.debugMode) {
				List<String> tagNames = new ArrayList<String>();
				for (int i = 0; i < tags.size(); i++) {
					tagNames.add(tags.get(i).getName());
				}
				LeapsUtils.logRetrievedTagsFromTheDBAsString(tagNames);
			}
	    } catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return tags;
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

				if (Configuration.debugMode) {
					logger.info("SQL Statement: " + preparedStatement.toString());
				}
				
				preparedStatement.executeUpdate();
		    } catch (Exception e) {
				// TODO: proper exception
		    	success = false;
				throw new UserException(e.getMessage());
			}
		}
		
		return success;
	}
	
	public List<Image> getAllUserImages(long userId) {
		List<Image> images = new ArrayList<Image>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT image_id, file_name FROM leaps.user_images WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
						
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				images.add(ImageDao.getInstance().createNewImage(rs.getLong("image_id"), userId, rs.getString("file_name")));
			}			
			
	    } catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return images;
	}

	public List<Event> getAllAttendingEventsForUser(long userId) {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT e.event_id, e.title, e.description, e.date, e.time_from, e.time_to, e.owner_id, e.coord_lat, e.coord_lnt, e.price_from, e.address, e.free_slots, e.date_created, e.event_image_url"
						+ "	FROM leaps.events e"
						+ "	WHERE e.event_id in"
						+ " (SELECT uae.event_id FROM leaps.users_attend_events uae WHERE uae.user_id = ?)";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date")));
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public List<Event> getAllHostingEventsForUser(long userId) {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT e.event_id, e.title, e.description, e.date, e.time_from, e.time_to, e.owner_id, e.coord_lat, e.coord_lnt, e.price_from, e.address, e.free_slots, e.date_created, e.event_image_url"
						 + " FROM leaps.events e WHERE e.owner_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date")));
			}			
			
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public int getEventAttendeesNumber(long eventId) {
		int attendees = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "select count(user_id) as 'attendees' from leaps.users_attend_events where event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, eventId);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				attendees = rs.getInt("attendees");
			}			
			
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return attendees;
	}

	public void addAttendeeForEvent(long userId, long eventId) {
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
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
	}

	public boolean unattendUserFromEvent(long userId, long eventId) {
		boolean success = true;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.users_attend_events WHERE user_id = ? AND event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, eventId);
			
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
			success = false;
		}
		
		return success;
	}

	public List<Event> getAllPastHostingEventsForUser(int userId, int limit, int page) {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT e.event_id, e.title, e.description, e.date, e.time_from, e.time_to, e.owner_id, e.coord_lat, e.coord_lnt, e.price_from, e.address, e.free_slots, e.date_created, e.event_image_url"
						 + " FROM leaps.events e WHERE e.owner_id = ? AND e.date < ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, (System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS));
			preparedStatement.setInt(3, (page - 1) * limit);
			preparedStatement.setInt(4, limit);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date")));
			}			
			
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public List<Event> getAllFutureHostingEventsForUser(int userId, int limit, int page) {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT e.event_id, e.title, e.description, e.date, e.time_from, e.time_to, e.owner_id, e.coord_lat, e.coord_lnt, e.price_from, e.address, e.free_slots, e.date_created, e.event_image_url"
						 + " FROM leaps.events e WHERE e.owner_id = ? AND e.time_from > ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, (System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS));
			preparedStatement.setInt(3, (page - 1) * limit);
			preparedStatement.setInt(4, limit);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date")));
			}			
			
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public List<Event> getAllPastAttendingEventsForUser(int userId, int limit, int page) {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT e.event_id, e.title, e.description, e.date, e.time_from, e.time_to, e.owner_id, e.coord_lat, e.coord_lnt, e.price_from, e.address, e.free_slots, e.date_created, e.event_image_url"
				+ "	FROM leaps.events e"
				+ "	WHERE e.event_id in"
				+ " (SELECT uae.event_id FROM leaps.users_attend_events uae WHERE uae.user_id = ?)"
				+ " AND e.time_from < ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, (System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS));
			preparedStatement.setInt(3, (page - 1) * limit);
			preparedStatement.setInt(4, limit);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						userId, rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date")));
			}			
			
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public List<Event> getAllFutureAttendingEventsForUser(int userId, int limit, int page) {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT e.event_id, e.title, e.description, e.date, e.time_from, e.time_to, e.owner_id, e.coord_lat, e.coord_lnt, e.price_from, e.address, e.free_slots, e.date_created, e.event_image_url"
				+ "	FROM leaps.events e"
				+ "	WHERE e.event_id in"
				+ " (SELECT uae.event_id FROM leaps.users_attend_events uae WHERE uae.user_id = ?)"
				+ " AND e.time_from > ? LIMIT ?, ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, (System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS));
			preparedStatement.setInt(3, (page - 1) * limit);
			preparedStatement.setInt(4, limit);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), rs.getLong("time_to"), 
						rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"), rs.getInt("price_from"), rs.getString("address"),
						rs.getInt("free_slots"), rs.getLong("date")));
			}			
			
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public int getTheTotalNumberOfPastAttendingEvents(int userId) {
		int counter = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT count(e.event_id) as 'total_number' FROM leaps.events e WHERE e.event_id IN (SELECT uae.event_id FROM leaps.users_attend_events uae WHERE uae.user_id = ?) AND e.time_from < ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				counter = rs.getInt("total_number");
			}			
			
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return counter;
	}

	public int insertUserImageIntoDB(long userId, String fileName) {
		int imageId = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "INSERT INTO leaps.user_images (user_id, file_name) VALUES ( ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setLong(1, userId);
			preparedStatement.setString(2, fileName);
			
			preparedStatement.execute();
			
			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	imageId = generatedKeys.getInt(1);
            }
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return imageId;
	}

	public boolean removeUserImageFromDB(int imageId) {
		boolean success = true;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.user_images where image_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, imageId);
			
			preparedStatement.execute();
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
			success = false;
		}
		
		return success;
	}

	public String getUserImageNameById(int imageId) {
		String imageName = null;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT file_name FROM leaps.user_images WHERE image_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, imageId);
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				imageName = rs.getString("file_name");
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return imageName;
	}
	
	public int insertEventImageIntoDB(long eventId, String fileName) {
		int imageId = -1;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "INSERT INTO leaps.event_images (event_id, file_name) VALUES ( ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setLong(1, eventId);
			preparedStatement.setString(2, fileName);
			
			preparedStatement.execute();
			
			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
            	imageId = generatedKeys.getInt(1);
            }
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return imageId;
	}

	public String getEventImageNameById(int imageId) {
		String imageName = null;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT file_name FROM leaps.event_images WHERE image_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, imageId);
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				imageName = rs.getString("file_name");
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return imageName;
	}

	public boolean removeEventImageFromDB(int imageId) {
		boolean success = true;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "DELETE FROM leaps.event_images where image_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, imageId);
			
			preparedStatement.execute();
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
			success = false;
		}
		
		return success;
	}

	public boolean insertUserMainImageIntoDB(long userId, String fileName) {
		boolean success = true;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "UPDATE leaps.users SET profile_image_url = ? WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, fileName);
			preparedStatement.setLong(2, userId);
			
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
			success = false;
		}
		
		return success;
	}

	public boolean insertEventMainImageIntoDB(long eventId, String fileName) {
		boolean success = true;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "UPDATE leaps.events SET event_image_url = ? WHERE event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setString(1, fileName);
			preparedStatement.setLong(2, eventId);
			
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
			success = false;
		}
		
		return success;
	}

	public boolean checkIfUserAlreadyAttendsAnEvent(long userId, long eventId) {
		boolean success = false;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT user_id FROM leaps.users_attend_events WHERE user_id = ? AND event_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, eventId);
			
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				success = true;
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
			success = true;
		}
		
		return success;
	}

	public int getAllEventCountThatUserHasAttended(long userId) {
		int counter = 0;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "select count(event_id) as 'attends' FROM leaps.users_attend_events WHERE user_id = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				counter = rs.getInt("attends");
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		return counter;
	}

	public List<User> getAllTrainersWithMostEventsCreated() {
		List<User> users = new ArrayList<User>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT u.user_id, u.username, u.email_address, u.password, u.age, u.gender, u.location, u.max_distance_setting, u.first_name, u.last_name, u.birthday,"
				+ " u.description, u.profile_image_url, u.is_trainer, u.facebook_id, u.google_id, u.phone_number, u.years_of_training, u.session_price, u.long_description"
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
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				users.add(UserDao.getInstance().createNewUser(rs.getLong("user_id"), rs.getString("username"), rs.getString("email_address"), rs.getInt("age"), rs.getString("gender"), rs.getString("location"), 
						rs.getInt("max_distance_setting"), rs.getString("first_name"), rs.getString("last_name"), rs.getLong("birthday"), rs.getString("description"), 
						rs.getString("profile_image_url"), rs.getBoolean("is_trainer"), rs.getString("facebook_id"), rs.getString("google_id"), rs.getString("phone_number"), 
						rs.getInt("session_price"), rs.getString("long_description"), rs.getInt("years_of_training")));
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return users;
	}

	public List<Event> getMostPopularEvents(int page, int limit) {
		List<Event> events = new ArrayList<Event>();
		
		long currentTime = System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT e.event_id, e.title, e.description, e.date, e.time_from, e.time_to, e.owner_id, e.coord_lat, e.coord_lnt, e.price_from, e.address, e.free_slots, e.event_image_url"
						 + " FROM leaps.events e"
						 + " LEFT JOIN leaps.users_attend_events uae"
						 + " ON uae.event_id = e.event_id"
						 + " WHERE e.time_from > ?"
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
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date")));
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public List<Event> getAllEUpcommingEvents() {
		List<Event> events = new ArrayList<Event>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT * FROM leaps.events WHERE time_from > ?";
		
		long currentTime = System.currentTimeMillis();
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, currentTime);
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date")));
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public List<Event> getNearbyUpcommingEvents(double latitude, double longitude,int page, int limit) {
		List<Event> events = new ArrayList<Event>();
		
		long currentTime = System.currentTimeMillis() + Configuration.THREE_HOURS_IN_MS;
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT * FROM leaps.events WHERE time_from > ? ORDER BY ABS(ABS(coord_lat - ?) + ABS(coord_lnt - ?)) ASC LIMIT ?, ?";
				
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, currentTime);
			preparedStatement.setDouble(2, latitude);
			preparedStatement.setDouble(3, longitude);
			preparedStatement.setInt(4, (page - 1) * limit);
			preparedStatement.setInt(5, limit);
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date")));
			}
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return events;
	}

	public List<Event> getFilteredEventsWithCoordinates(String keyWord, double latitude, double longitude, int distance,
			List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws EventException {
		List<Event> events = new ArrayList<Event>();
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();		
		
		StringBuilder selectStatement = new StringBuilder("SELECT e.*, (ABS(ABS(e.coord_lat - ?) + ABS(e.coord_lnt - ?))) as 'distance' FROM leaps.events e WHERE e.time_from > ? AND e.time_to < ?");
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
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date")));
			}
		} catch (Exception e) {
			throw new EventException(e.getMessage());
		}
		
		return events;
	}

	public List<Event> getFilteredEvents(String keyWord, int distance, List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws EventException {
		List<Event> events = new ArrayList<Event>();
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT e.* FROM leaps.events e WHERE e.time_from > ? AND e.time_to < ?");
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
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				events.add(EventDao.getInstance().generateNewEvent(rs.getLong("event_id"), rs.getString("title"), rs.getString("description"), rs.getLong("date"), rs.getLong("time_from"), 
						rs.getLong("time_to"), rs.getLong("owner_id"), rs.getString("event_image_url"), rs.getDouble("coord_lat"), rs.getDouble("coord_lnt"),rs.getInt("price_from"), 
						rs.getString("address"), rs.getInt("free_slots"), rs.getLong("date")));
			}			
			
		} catch (Exception e) {
			throw new EventException(e.getMessage());
		}
		
		return events;
	}

	public List<User> getFilteredTrainersByMostEventsWithCoordinates(String keyWord, double latitude, double longitude,
			int distance, List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws UserException {
		List<User> trainers = new ArrayList<User>();
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();	
		
		StringBuilder selectStatement = new StringBuilder("SELECT u.*, (ABS(ABS(e.coord_lat - ?) + ABS(e.coord_lnt - ?))) as 'distance' FROM leaps.users u"
				+ " LEFT JOIN leaps.events e ON u.user_id = e.owner_id WHERE e.time_from > ? AND e.time_to < ?");
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
			
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				trainers.add(UserDao.getInstance().createNewUser(rs.getLong("user_id"), rs.getString("username"), rs.getString("email_address"), rs.getInt("age"), rs.getString("gender"), rs.getString("location"), 
						rs.getInt("max_distance_setting"), rs.getString("first_name"), rs.getString("last_name"), rs.getLong("birthday"), rs.getString("description"), 
						rs.getString("profile_image_url"), rs.getBoolean("is_trainer"), rs.getString("facebook_id"), rs.getString("google_id"), rs.getString("phone_number"), 
						rs.getInt("session_price"), rs.getString("long_description"), rs.getInt("years_of_training")));
			}
		} catch (Exception e) {
			throw new UserException(e.getMessage());
		}
		
		return trainers;
	}

	public List<User> getFilteredTrainersByMostEvents(String keyWord, int distance, List<String> tags,
			long minStartingDate, long maxStartingDate, int page, int limit) throws UserException {
		List<User> trainers = new ArrayList<User>();
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
//		SELECT u.*
//		FROM leaps.users u
//		LEFT JOIN leaps.events e
//		ON u.user_id = e.owner_id 
//		WHERE e.time_from > 101978506756
//		AND e.time_to < 9226623422532
//		AND (u.first_name LIKE '%test%' OR u.last_name LIKE '%test%' OR u.description LIKE '%test%' OR u.long_description LIKE '%test%')
//		AND e.event_id IN (SELECT eht.event_id FROM leaps.event_has_tags eht WHERE eht.tag_id IN 
//		(SELECT t.tag_id FROM leaps.tags t WHERE t.name = 'yoga' OR t.name = 'fafla'))
		
//		GROUP BY u.user_id
//		ORDER BY count(e.owner_id)
//		DESC LIMIT 0, 20;
		
		StringBuilder selectStatement = new StringBuilder("SELECT u.* FROM leaps.users u LEFT JOIN leaps.events e ON u.user_id = e.owner_id WHERE e.time_from > ? AND e.time_to < ?");
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
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				trainers.add(UserDao.getInstance().createNewUser(rs.getLong("user_id"), rs.getString("username"), rs.getString("email_address"), rs.getInt("age"), rs.getString("gender"), rs.getString("location"), 
						rs.getInt("max_distance_setting"), rs.getString("first_name"), rs.getString("last_name"), rs.getLong("birthday"), rs.getString("description"), 
						rs.getString("profile_image_url"), rs.getBoolean("is_trainer"), rs.getString("facebook_id"), rs.getString("google_id"), rs.getString("phone_number"), 
						rs.getInt("session_price"), rs.getString("long_description"), rs.getInt("years_of_training")));
			}			
			
		} catch (Exception e) {
			throw new UserException(e.getMessage());
		}
		
		return trainers;
	}

	public int countFilteredTrainersByMostEventsWithCoordinates(String keyWord, double latitude, double longitude,
			int distance, List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws UserException {
		int count = 0;
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();	
		
		StringBuilder selectStatement = new StringBuilder("SELECT COUNT(*) FROM (SELECT u.*, (ABS(ABS(e.coord_lat - ?) + ABS(e.coord_lnt - ?))) as 'distance' FROM leaps.users u"
				+ " LEFT JOIN leaps.events e ON u.user_id = e.owner_id WHERE e.time_from > ? AND e.time_to < ?");
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
			
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			throw new UserException(e.getMessage());
		}
		
		return count;
	}

	public int countFilteredTrainersByTheirEvents(String keyWord, int distance, List<String> tags,
			long minStartingDate, long maxStartingDate, int page, int limit) throws UserException {
		int count = 0;
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT COUNT(*) FROM (SELECT u.* FROM leaps.users u LEFT JOIN leaps.events e ON u.user_id = e.owner_id WHERE e.time_from > ? AND e.time_to < ?");
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
			
			ResultSet rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt(1);
			}			
			
		} catch (Exception e) {
			throw new UserException(e.getMessage());
		}
		
		return count;
	}

	public int getFilteredEventsCountWithCoordinates(String keyWord, double latitude, double longitude, int distance,
			List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws EventException {
		int count = 0;
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();		
		
		StringBuilder selectStatement = new StringBuilder("SELECT COUNT(*) FROM (SELECT e.*, (ABS(ABS(e.coord_lat - ?) + ABS(e.coord_lnt - ?))) as 'distance' FROM leaps.events e WHERE e.time_from > ? AND e.time_to < ?");
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
			
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			throw new EventException(e.getMessage());
		}
		
		return count;
	}

	public int getFilteredEventsCount(String keyWord, int distance, List<String> tags, long minStartingDate, long maxStartingDate, int page, int limit) throws EventException {
		int count = 0;
		List<Object> params = new ArrayList<Object>();
		List<String> types = new ArrayList<String>();
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		StringBuilder selectStatement = new StringBuilder("SELECT COUNT(*) FROM (SELECT e.* FROM leaps.events e WHERE e.time_from > ? AND e.time_to < ?");
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
			
			ResultSet rs = preparedStatement.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt(1);
			}			
			
		} catch (Exception e) {
			throw new EventException(e.getMessage());
		}
		
		return count;
	}

	public List<String> getTagsFromTheDB() {
		List<String> tags = null;
				
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT name FROM leaps.tags";
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			
			ResultSet rs = preparedStatement.executeQuery();
			tags = new ArrayList<String>();
            while (rs.next()) {
            	tags.add(rs.getString("name"));
            }
            
		} catch (Exception e) {
			// TODO: proper exception
			System.out.println(e.getMessage());
		}
		
		return tags;
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
		}
	}

	public Map<String, List<Long>> getFollowingUsers(Long userId) throws EventException {
		Map<String, List<Long>> tempUsers = new HashMap<String, List<Long>>();
		tempUsers.put("follower", new ArrayList<Long>());
		tempUsers.put("followed", new ArrayList<Long>());
		
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "SELECT * FROM leaps.user_followers WHERE followed = ? OR follower = ?";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, userId);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				if (rs.getLong("follower") == userId && rs.getLong("followed") == userId) {
					continue;
				} else if (rs.getLong("follower") == userId) {
					tempUsers.get("follower").add(rs.getLong("followed"));
				} else if (rs.getLong("followed") == userId) {
					tempUsers.get("followed").add(rs.getLong("follower"));
				}
			}
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.ERROR_RETREIVING_THE_USERS);
		}
		
		return tempUsers;
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
		}
	}

	public void rateEvent(Rate rate, long userId) throws EventException {
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
		
		String selectSQL = "INSERT INTO leaps.event_rating (event_id, user_id, rating, comment, date_created) VALUES ( ? , ? , ? , ? , ? )";
		
		try {
			dbConnection = DBManager.getInstance().getConnection();
			preparedStatement = dbConnection.prepareStatement(selectSQL);
			preparedStatement.setLong(1, rate.getEventId());
			preparedStatement.setLong(2, userId);
			preparedStatement.setInt(3, rate.getRating());
			preparedStatement.setString(4, rate.getComment());
			preparedStatement.setLong(5, rate.getDateCreated());
			preparedStatement.execute();
			
		} catch (SQLException e) {
			if (Configuration.debugMode) {
				logger.error(e.getMessage());
			}
			throw new EventException(Configuration.CANNOT_RATE_CURRENT_EVENT);
		}
	}
}
