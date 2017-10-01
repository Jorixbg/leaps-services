package com.leaps.model.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// These are from the AWS SDK for Java, which you can download at https://aws.amazon.com/sdk-for-java.
// Be sure to include the AWS SDK for Java library in your project.
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leaps.controller.UserController;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.event.Event;
import com.leaps.model.event.EventDao;
import com.leaps.model.event.Tag;
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.image.Image;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;

public class LeapsUtils {

	private static final Logger logger = LoggerFactory.getLogger(LeapsUtils.class);
	
	/**
	 * Util method to check whenever a given string is a number or not.
	 */
	public static boolean isNumber(String text) {
		for (int i = 0; i < text.length(); i++) {
		    if (!Character.isDigit(text.charAt(i))) {
		    	return false;
		    }
		}
		return true;
	}
	
	/**
	 * Util method for random password generation.
	 */
	public static String generateRandomPass() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
	}
	
	/**
	 * Util method for hashing a password
	 */
	public static String convertToMd5(final String md5) throws UnsupportedEncodingException {
        StringBuffer sb = null;
        try {
            final java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            final byte[] array = md.digest(md5.getBytes("UTF-8"));
            sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (final java.security.NoSuchAlgorithmException e) {
        	
        }
        return sb.toString();
    }
	
	/**
	 * Util method for getting the suffux of String (file name)
	 */
	public static String getFileExtension(String fileName) {
		String suffix = null;		
		int dotPosition = -1;
		for (int i = fileName.length() - 1; i >= 0; i--) {
			if (fileName.charAt(i) == '.') {
				dotPosition = i;
				break;
			}
		}
		
		if (dotPosition > 0) {
			suffix = fileName.substring(dotPosition);
		}
		
		return suffix;
	}
	
	/**
	 * Util method for calculating the distance between 2 coordinates
	 */
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371.0; // kilometers
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;
		
		return dist;
	}
	
	public static int generateAgeFromBirthday(long birthday) {
		Long time= System.currentTimeMillis() / 1000 - birthday / 1000;
		int years = Math.round(time) / 31536000;
		System.out.println(years);
		return years;
	}
	
//	/**
//	 * Util method for generating a Json Event from defined input parameters
//	 */
//	public static JsonObject generateJsonEvent(long eventId, String title, String description, long date, long timeFrom, long timeTo, long ownerId, String ownerName, String ownerImageUrl,
//										   List<Tag> specialities, List<User> attending, String eventImageUrl, double latitude, double longitude, int priceFrom, String address,
//										   int freeSlots, long dateCreated, Map<String, String> images) {
//		JsonObject tempJson = new JsonObject();
//		tempJson.addProperty("event_id", eventId);
//		tempJson.addProperty("title", title);
//		tempJson.addProperty("description", description);
//		tempJson.addProperty("date", date);
//		tempJson.addProperty("time_from", timeFrom);
//		tempJson.addProperty("time_to", timeTo);
//		tempJson.addProperty("owner_id", ownerId);
//		tempJson.addProperty("owner_name", ownerName);
//		tempJson.addProperty("owner_image_url", ownerImageUrl);
//		
//		JsonArray specialitiesJson = new JsonArray();
//		
//		for (int i = 0; i < specialities.size(); i++) {
//			specialitiesJson.add(specialities.get(i).getName());
//		}
//		
//		tempJson.add("specialities", specialitiesJson);
//		
//		JsonArray attendingJson = new JsonArray();
//		
//		for (int i = 0; i < attending.size(); i++) {
//			JsonObject tempUser = new JsonObject();
//			tempUser.addProperty("user_id", attending.get(i).getUserId());
//			tempUser.addProperty("user_name", attending.get(i).getFirstName() + " " + attending.get(i).getLastName());
//			tempUser.addProperty("user_image_url", attending.get(i).getProfileImageUrl());
//			attendingJson.add(tempUser);
//		}
//
//		tempJson.add("attending", attendingJson);
//
//		tempJson.addProperty("event_image_url", eventImageUrl);
//		tempJson.addProperty("coord_lat", latitude);
//		tempJson.addProperty("coord_lnt", longitude);
//		tempJson.addProperty("price_from", priceFrom);
//		tempJson.addProperty("address", address);
//		tempJson.addProperty("free_slots", freeSlots - attending.size());
//		tempJson.addProperty("date_created", dateCreated);
//		
//		
//		JsonArray imagesJson = new JsonArray();
//		
//		for (Map.Entry<String, String> map : images.entrySet()) {
//			JsonObject eventImage = new JsonObject();
//			eventImage.addProperty("image_id", Integer.valueOf(map.getKey()));
//			eventImage.addProperty("image_url", map.getValue());
//			imagesJson.add(eventImage);
//		}
//
//		tempJson.add("images", imagesJson);
//		
//		return tempJson;
//	}
	
	/**
	 * Util method for generating a Json Event from defined input parameters
	 * @throws UserException 
	 */
	public static JsonObject generateJsonEvent(Event event) throws UserException {
		User eventOwner = UserDao.getInstance().getUserFromDbOrCacheById(event.getOwnerId());
		
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("event_id", event.getEventId());
		tempJson.addProperty("title", event.getTitle());
		tempJson.addProperty("description", event.getDescription());
		tempJson.addProperty("date", event.getDate());
		tempJson.addProperty("time_from", event.getTimeFrom());
		tempJson.addProperty("time_to", event.getTimeTo());
		tempJson.addProperty("owner_id", event.getOwnerId());
		tempJson.addProperty("owner_name", (eventOwner.getFirstName() + " " + eventOwner.getLastName().trim()));
		tempJson.addProperty("owner_image_url", eventOwner.getProfileImageUrl());
		
		JsonArray specialitiesJson = new JsonArray();
		
		List<Tag> specialities = DBUserDao.getInstance().getAllEventTagsFromDb(event.getEventId());
		
		for (int i = 0; i < specialities.size(); i++) {
			specialitiesJson.add(specialities.get(i).getName());
		}
		
		tempJson.add("specialities", specialitiesJson);
		
		
		// TODO: refactor to return followed and others
		JsonArray attendingJson = new JsonArray();
		List<User> attending = DBUserDao.getInstance().getAllAttendingUsersForEvent(event.getEventId());
		
		for (int i = 0; i < attending.size(); i++) {
			JsonObject tempUser = new JsonObject();
			tempUser.addProperty("user_id", attending.get(i).getUserId());
			tempUser.addProperty("user_name", attending.get(i).getFirstName() + " " + attending.get(i).getLastName());
			tempUser.addProperty("user_image_url", attending.get(i).getProfileImageUrl());
			attendingJson.add(tempUser);
		}

		tempJson.add("attending", attendingJson);
		// end TODO
		
		tempJson.addProperty("event_image_url", event.getEventImageUrl());
		tempJson.addProperty("coord_lat", event.getCoordLatitude());
		tempJson.addProperty("coord_lnt", event.getCoordLongitude());
		tempJson.addProperty("price_from", event.getPriceFrom());
		tempJson.addProperty("address", event.getAddress());
		tempJson.addProperty("free_slots", event.getFreeSlots() - attending.size());
		tempJson.addProperty("date_created", event.getDateCreated());
		
		
		JsonArray imagesJson = new JsonArray();

		Map<String, String> eventImages = DBUserDao.getInstance().getAllEventImages(event.getEventId());
		
		for (Map.Entry<String, String> map : eventImages.entrySet()) {
			JsonObject eventImage = new JsonObject();
			eventImage.addProperty("image_id", Integer.valueOf(map.getKey()));
			eventImage.addProperty("image_url", map.getValue());
			imagesJson.add(eventImage);
		}

		tempJson.add("images", imagesJson);
		
		return tempJson;
	}
	
	/**
	 * Util method for generating a Json User
	 * @throws EventException 
	 * @throws UserException 
	 */
	public static JsonObject generateJsonUser(User user) throws EventException, UserException {
		JsonObject response = new JsonObject();
		
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
		
		// no need for null chech as if an error is thrown - it will be in the DBUserDao class and then it will be rethrown
		for (int fod = 0; fod < followed.size(); fod++) {
			User tempUser = DBUserDao.getInstance().getUserFromDbById(followed.get(fod));
			JsonObject jsonUser = new JsonObject();
			jsonUser.addProperty("user_id", tempUser.getUserId());
			jsonUser.addProperty("full_name", (tempUser.getFirstName() + " " + tempUser.getLastName()).trim());
			jsonUser.addProperty("user_profile_picture", tempUser.getProfileImageUrl());
			
			boolean match = false;
			
			for (int fol = 0; fol < follower.size(); fol++) {
				if (followed.get(fod) == follower.get(fol)) {
					followingJson.add(jsonUser);
					match = true;
					break;
				}
			}
			
			if (!match) {
				othersJson.add(jsonUser);
			}
		}
		
		followedByJson.add("following", followingJson);		
		followedByJson.add("others", othersJson);
		
		response.add("followed_by", followedByJson);
		
		List<Event> attendingEvents = DBUserDao.getInstance().getAllAttendingEventsForUser(user.getUserId());
		JsonArray attendingEventsJson = new JsonArray();
		
		for (int i = 0; i < attendingEvents.size(); i++) {
			attendingEventsJson.add(LeapsUtils.generateJsonEvent(attendingEvents.get(i)));
		}
		response.add("attending_events", attendingEventsJson);
		
		List<Event> hostingEvents = DBUserDao.getInstance().getAllHostingEventsForUser(user.getUserId());
		JsonArray hostingEventsJson = new JsonArray();
		for (int i = 0; i < hostingEvents.size(); i++) {			
			attendingEventsJson.add(LeapsUtils.generateJsonEvent(attendingEvents.get(i)));
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
			JsonObject obj = new JsonObject();
			obj.addProperty("image_id", userImages.get(i).getImageId());
			obj.addProperty("image_url", userImages.get(i).getImageName());
			userImagesJson.add(obj);
		}
		response.add("images", userImagesJson);
		
		return response;
	}
	
	/**
	 * Util method that checks if a given token is valid and exist at the same time
	 */
	public static long checkToken(String token) throws AuthorizationException {
		if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
			throw new AuthorizationException(Configuration.INVALID_INPUT_PAREMETERS);
		}

		long checker = Long.valueOf(token);
					
		if (UserDao.getInstance().getUserFromCache(checker) == null) {
			throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
		}
		
		return checker;
	}

	/**
	 * Util method that reads the request data and returns the given Json Object
	 */
	public static JsonObject getRequestData(HttpServletRequest req) throws IOException {
		Scanner scanner = null;
		try {
			scanner = new Scanner(req.getInputStream());
			StringBuilder sb = new StringBuilder();
			while (scanner.hasNext()) {
				sb.append(scanner.nextLine());
			}

			String requestData = sb.toString();
			JsonParser parser = new JsonParser();
			
			return parser.parse(requestData).getAsJsonObject();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}
	
	
	/****************************/
	/** BELOW ARE DUMMY METHODS */
	/****************************/
	
	
	/**
	 * An util method for generating a JsonObject of class User
	 * @param paramOne -> username
	 * @param paramTwo -> pass
	 * @return JsonObject
	 */
	public static JsonObject generateJsonUser(String paramOne, String paramTwo) {
		User tempUser = User.getDummyUser(paramOne, paramTwo);
		
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("user_id", tempUser.getUserId());
		tempJson.addProperty("username", tempUser.getUsername());
		tempJson.addProperty("email_address", tempUser.getEmail());
		tempJson.addProperty("age", tempUser.getAge());
		tempJson.addProperty("gender", tempUser.getGender());
		tempJson.addProperty("location", tempUser.getLocation());
		tempJson.addProperty("max_distance_setting", tempUser.getMaxDistanceSetting());
		tempJson.addProperty("first_name", tempUser.getFirstName());
		tempJson.addProperty("last_name", tempUser.getLastName());
		tempJson.addProperty("profile_image_url", tempUser.getProfileImageUrl());
		tempJson.addProperty("birthday", tempUser.getBirthday());
		tempJson.addProperty("description", tempUser.getDescription());
		tempJson.addProperty("is_trainer", tempUser.isTrainer());
		
		return tempJson;
	}
	
	/**
	 * An util method for generating a JsonObject of class Trainer
	 * @param paramOne -> username
	 * @param paramTwo -> pass
	 * @return JsonObject
	 */
	public static Object generateJsonTrainer(String paramOne, String paramTwo) {
		User tempTrainer = User.getDummyTrainer(paramOne, paramTwo);
		
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("user_id", tempTrainer.getUserId());
		tempJson.addProperty("username", tempTrainer.getUsername());
		tempJson.addProperty("email_address", tempTrainer.getEmail());
		tempJson.addProperty("age", tempTrainer.getAge());
		tempJson.addProperty("gender", tempTrainer.getGender());
		tempJson.addProperty("location", tempTrainer.getLocation());
		tempJson.addProperty("max_distance_setting", tempTrainer.getMaxDistanceSetting());
		tempJson.addProperty("first_name", tempTrainer.getFirstName());
		tempJson.addProperty("last_name", tempTrainer.getLastName());
		tempJson.addProperty("profile_image_url", "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png");
		tempJson.addProperty("birthday", tempTrainer.getBirthday());
		tempJson.addProperty("description", tempTrainer.getDescription());
		tempJson.addProperty("is_trainer", tempTrainer.isTrainer());
		tempJson.addProperty("phone_number", tempTrainer.getPhoneNumber());
		tempJson.addProperty("years_of_training", tempTrainer.getYearsOfTraining());
		tempJson.addProperty("session_price", tempTrainer.getSessionPrice());
		tempJson.addProperty("long_description", tempTrainer.getLongDescription());
		tempJson.addProperty("attended_events", 5);
		
		JsonArray images = new JsonArray();
		JsonObject imgOne = new JsonObject();
		imgOne.addProperty("image_id", 3);
		imgOne.addProperty("image_url", "https://img.sportal.bg/uploads/news/2016_18/images/00602496.jpg?20161012233310");
		JsonObject imgTwo = new JsonObject();
		imgTwo.addProperty("image_id", 5);
		imgTwo.addProperty("image_url", "https://img2.sportal.bg/uploads/news/2016_24/images/00609449.jpg?20161017185312");
		images.add(imgOne);
		images.add(imgTwo);
		tempJson.add("images", images);
		
		JsonArray specialties = new JsonArray();
		specialties.add("yoga");
		specialties.add("fitness");
		specialties.add("cardio");
		tempJson.add("specialties", specialties);
		
		JsonArray followedBy = new JsonArray();
		JsonObject followerOne = new JsonObject();
		followerOne.addProperty("user_id", 1);
		followerOne.addProperty("user_first_name", "Krasi");
		followerOne.addProperty("user_profile_picture", "/uploads/pictures/pic1.jpg");
		JsonObject followerTwo = new JsonObject();
		followerTwo.addProperty("user_id", 2);
		followerTwo.addProperty("user_first_name", "Zaiko");
		followerTwo.addProperty("user_profile_picture", "/uploads/pictures/pic2.jpg");
		JsonObject followerThree = new JsonObject();
		followerThree.addProperty("user_id", 3);
		followerThree.addProperty("user_first_name", "Sasho");
		followerThree.addProperty("user_profile_picture", "/uploads/pictures/pic3.jpg");
		followedBy.add(followerOne);
		followedBy.add(followerTwo);
		followedBy.add(followerThree);
		tempJson.add("followed_by", followedBy);
		
		JsonArray attendingEvents = new JsonArray();
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		attendingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		tempJson.add("attending_events", attendingEvents);
		
		JsonArray hostingEvents = new JsonArray();
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInThePast());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		hostingEvents.add((JsonObject)LeapsUtils.generateJsonEventMoreInTheFuture());
		tempJson.add("hosting_events", hostingEvents);
		
		return tempJson;
	}
	
	public static Object generateJsonEvent() {
		
		/**
		{
			"event_id" : 1,
			"title" : "trenirovchitsa",
			"description" : "Nai-ultra mega huper yakata trenirovka ever izmislyana", 
			"date" : "6226623422532",
			"time_from" : "6226623422532",
			"time_to" : "6226623422532",
			"event_main_image" : "some url",
			"owner_id" : 1,
			"owner_name" : "Mityo Krika",
			"owner_image_url" : "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png",
			"specialities" : [ "yoga", "running", "jogging" ],
			"attending" : [
				{
					"user_id" : 1,
					"user_name" : "zayo bayo",
					"user_image_url" : "https://i49.vbox7.com/o/47c/47cb43c49a0.jpg"
				},
				{
					"user_id" : 41,
					"user_name" : "Kumcho Vulcho",
					"user_image_url" : "https://images.webcafe.bg/2012/08/29/%E2%FA%EB%EA/618x464.jpg"
				},
				{
					"user_id" : 23,
					"user_name" : "100 kila",
					"user_image_url" : "http://senzacia.net/wp-content/uploads/2013/05/100-kila-1.jpg"
				}
			],
			"event_image_url" : "some url",
			"coord_lat" : 42.693351,
			"coord_lnt" : 23.340381,
			"price_from" : 10,
			"address" : "ул. „Шипка“ 34-36, 1504 София",
			"free_slots" : 50,
			"date_created" : "1365475684564"
		}
	**/
		
		Event tempEvent = Event.createDummyEvent();
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("event_id", tempEvent.getEventId());
		tempJson.addProperty("title", tempEvent.getTitle());
		tempJson.addProperty("description", tempEvent.getDescription());
		tempJson.addProperty("date", tempEvent.getDate());
		tempJson.addProperty("time_from", tempEvent.getTimeFrom());
		tempJson.addProperty("time_to", tempEvent.getTimeTo());
		tempJson.addProperty("event_main_image", tempEvent.getEventImageUrl());
		tempJson.addProperty("owner_id", 1);
		
		tempJson.addProperty("owner_name", "Mityo Krika");
		tempJson.addProperty("owner_image_url", "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png");
		
		JsonArray specialities = new JsonArray();
		specialities.add("yoga");
		specialities.add("running");
		specialities.add("jogging");
		
		tempJson.add("specialities", specialities);
		
		JsonArray attending = new JsonArray();
		JsonObject tempUser = new JsonObject();
		tempUser.addProperty("user_id", 1);
		tempUser.addProperty("user_name", "zayo bayo");
		tempUser.addProperty("user_image_url", "https://i49.vbox7.com/o/47c/47cb43c49a0.jpg");
		
		JsonObject tempUser2 = new JsonObject();
		tempUser2.addProperty("user_id", 41);
		tempUser2.addProperty("user_name", "Kumcho Vulcho");
		tempUser2.addProperty("user_image_url", "https://images.webcafe.bg/2012/08/29/%E2%FA%EB%EA/618x464.jpg");

		JsonObject tempUser3 = new JsonObject();
		tempUser3.addProperty("user_id", 23);
		tempUser3.addProperty("user_name", "100 kila");
		tempUser3.addProperty("user_image_url", "http://senzacia.net/wp-content/uploads/2013/05/100-kila-1.jpg");
		
		attending.add(tempUser);
		attending.add(tempUser2);
		attending.add(tempUser3);
		
		tempJson.add("attending", attending);
		
		tempJson.addProperty("event_image_url", tempEvent.getEventImageUrl());
		tempJson.addProperty("coord_lat", tempEvent.getCoordLatitude());
		tempJson.addProperty("coord_lnt", tempEvent.getCoordLongitude());
		tempJson.addProperty("price_from", 10);
		tempJson.addProperty("address", tempEvent.getAddress());
		tempJson.addProperty("free_slots", tempEvent.getFreeSlots());
		tempJson.addProperty("date_created", tempEvent.getDateCreated());
		
		JsonArray images = new JsonArray();
		JsonObject eventImageOne = new JsonObject();
		eventImageOne.addProperty("image_id", 1);
		eventImageOne.addProperty("image_url", "http://sportteam.co.za/wp-content/uploads/2017/02/istock_000051598656_large.jpg__1600x475_q85_crop_subject_location-2438626_upscale-1024x304.jpg");
		
		JsonObject eventImageTwo = new JsonObject();
		eventImageTwo.addProperty("image_id", 2);
		eventImageTwo.addProperty("image_url", "http://www.limontasport.com/wp-content/uploads/2016/03/limonta-sport-box-referenze-600x456.jpg");
		
		JsonObject eventImageThree = new JsonObject();
		eventImageThree.addProperty("image_id", 3);
		eventImageThree.addProperty("image_url", "https://cnnespanol2.files.wordpress.com/2017/03/170313194633-25-what-a-shot-0314-super-169.jpg?quality=90&strip=all");
		
		images.add(eventImageOne);
		images.add(eventImageTwo);
		images.add(eventImageThree);

		tempJson.add("images", images);
		
		return tempJson;
	}
	
	public static Object generateJsonEventWithoutPrice() {
		
		/**
		{
			"event_id" : 1,
			"title" : "trenirovchitsa",
			"description" : "Nai-ultra mega huper yakata trenirovka ever izmislyana", 
			"date" : "6226623422532",
			"time_from" : "6226623422532",
			"time_to" : "6226623422532",
			"owner_id" : 1,
			"owner_name" : "Mityo Krika",
			"owner_image_url" : "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png",
			"specialities" : [ "yoga", "running", "jogging" ],
			"attending" : [
				{
					"user_id" : 1,
					"user_name" : "zayo bayo",
					"user_image_url" : "https://i49.vbox7.com/o/47c/47cb43c49a0.jpg"
				},
				{
					"user_id" : 41,
					"user_name" : "Kumcho Vulcho",
					"user_image_url" : "https://images.webcafe.bg/2012/08/29/%E2%FA%EB%EA/618x464.jpg"
				},
				{
					"user_id" : 23,
					"user_name" : "100 kila",
					"user_image_url" : "http://senzacia.net/wp-content/uploads/2013/05/100-kila-1.jpg"
				}
			],
			"event_image_url" : "some url",
			"coord_lat" : 42.693351,
			"coord_lnt" : 23.340381,
			"address" : "ул. „Шипка“ 34-36, 1504 София",
			"free_slots" : 50,
			"date_created" : "1365475684564"
		}
	**/
		
		Event tempEvent = Event.createDummyEvent();
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("event_id", tempEvent.getEventId());
		tempJson.addProperty("title", tempEvent.getTitle());
		tempJson.addProperty("description", tempEvent.getDescription());
		tempJson.addProperty("date", tempEvent.getDate());
		tempJson.addProperty("time_from", tempEvent.getTimeFrom());
		tempJson.addProperty("time_to", tempEvent.getTimeTo());
		tempJson.addProperty("owner_id", 1);
		
		tempJson.addProperty("owner_name", "Mityo Krika");
		tempJson.addProperty("owner_image_url", "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png");
		
		JsonArray specialities = new JsonArray();
		specialities.add("yoga");
		specialities.add("running");
		specialities.add("jogging");
		
		tempJson.add("specialities", specialities);
		
		JsonArray attending = new JsonArray();
		JsonObject tempUser = new JsonObject();
		tempUser.addProperty("user_id", 1);
		tempUser.addProperty("user_name", "zayo bayo");
		tempUser.addProperty("user_image_url", "https://i49.vbox7.com/o/47c/47cb43c49a0.jpg");
		
		JsonObject tempUser2 = new JsonObject();
		tempUser2.addProperty("user_id", 41);
		tempUser2.addProperty("user_name", "Kumcho Vulcho");
		tempUser2.addProperty("user_image_url", "https://images.webcafe.bg/2012/08/29/%E2%FA%EB%EA/618x464.jpg");

		JsonObject tempUser3 = new JsonObject();
		tempUser3.addProperty("user_id", 23);
		tempUser3.addProperty("user_name", "100 kila");
		tempUser3.addProperty("user_image_url", "http://senzacia.net/wp-content/uploads/2013/05/100-kila-1.jpg");
		
		attending.add(tempUser);
		attending.add(tempUser2);
		attending.add(tempUser3);
		
		tempJson.add("attending", attending);
		
		tempJson.addProperty("event_image_url", tempEvent.getEventImageUrl());
		tempJson.addProperty("coord_lat", tempEvent.getCoordLatitude());
		tempJson.addProperty("coord_lnt", tempEvent.getCoordLongitude());
		tempJson.addProperty("address", tempEvent.getAddress());
		tempJson.addProperty("free_slots", tempEvent.getFreeSlots());
		tempJson.addProperty("date_created", tempEvent.getDateCreated());
		
		JsonArray images = new JsonArray();
		JsonObject eventImageOne = new JsonObject();
		eventImageOne.addProperty("image_id", 1);
		eventImageOne.addProperty("image_url", "http://sportteam.co.za/wp-content/uploads/2017/02/istock_000051598656_large.jpg__1600x475_q85_crop_subject_location-2438626_upscale-1024x304.jpg");
		
		JsonObject eventImageTwo = new JsonObject();
		eventImageTwo.addProperty("image_id", 2);
		eventImageTwo.addProperty("image_url", "http://www.limontasport.com/wp-content/uploads/2016/03/limonta-sport-box-referenze-600x456.jpg");
		
		JsonObject eventImageThree = new JsonObject();
		eventImageThree.addProperty("image_id", 3);
		eventImageThree.addProperty("image_url", "https://cnnespanol2.files.wordpress.com/2017/03/170313194633-25-what-a-shot-0314-super-169.jpg?quality=90&strip=all");
		
		images.add(eventImageOne);
		images.add(eventImageTwo);
		images.add(eventImageThree);

		tempJson.add("images", images);
		
		return tempJson;
	}
	
	/**
	 * An util method for generating a JsonObject of a dummy picture
	 * @return JsonObject
	 */
	public static Object generateJsonPicture() {
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("image_id", 1324113);
		tempJson.addProperty("url", "/event/13/image/1324113");
		return tempJson;
	}
	
	
	/**
		EVENT IN THE PAST
	**/
	public static Object generateJsonEventInThePast() {
		Event tempEvent = Event.createDummyEvent();
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("event_id", tempEvent.getEventId());
		tempJson.addProperty("title", tempEvent.getTitle());
		tempJson.addProperty("description", tempEvent.getDescription());
		tempJson.addProperty("date", (tempEvent.getDate() - (86400000*7) ));
		tempJson.addProperty("time_from", (tempEvent.getTimeFrom() - (86400000*7)));
		tempJson.addProperty("time_to", (tempEvent.getTimeTo() - (86400000*7)));
		tempJson.addProperty("owner_id", 1);
		
		tempJson.addProperty("owner_name", "Mityo Krika");
		tempJson.addProperty("owner_image_url", "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png");
		
		JsonArray specialities = new JsonArray();
		specialities.add("yoga");
		specialities.add("running");
		specialities.add("jogging");
		
		tempJson.add("specialities", specialities);
		
		JsonArray attending = new JsonArray();
		JsonObject tempUser = new JsonObject();
		tempUser.addProperty("user_id", 1);
		tempUser.addProperty("user_name", "zayo bayo");
		tempUser.addProperty("user_image_url", "https://i49.vbox7.com/o/47c/47cb43c49a0.jpg");
		
		JsonObject tempUser2 = new JsonObject();
		tempUser2.addProperty("user_id", 41);
		tempUser2.addProperty("user_name", "Kumcho Vulcho");
		tempUser2.addProperty("user_image_url", "https://images.webcafe.bg/2012/08/29/%E2%FA%EB%EA/618x464.jpg");
	
		JsonObject tempUser3 = new JsonObject();
		tempUser3.addProperty("user_id", 23);
		tempUser3.addProperty("user_name", "100 kila");
		tempUser3.addProperty("user_image_url", "http://senzacia.net/wp-content/uploads/2013/05/100-kila-1.jpg");
		
		attending.add(tempUser);
		attending.add(tempUser2);
		attending.add(tempUser3);
		
		tempJson.add("attending", attending);
		
		tempJson.addProperty("event_image_url", tempEvent.getEventImageUrl());
		tempJson.addProperty("coord_lat", tempEvent.getCoordLatitude());
		tempJson.addProperty("coord_lnt", tempEvent.getCoordLongitude());
		tempJson.addProperty("price_from", 10);
		tempJson.addProperty("address", tempEvent.getAddress());
		tempJson.addProperty("free_slots", tempEvent.getFreeSlots());
		tempJson.addProperty("date_created", tempEvent.getDateCreated());
		
		JsonArray images = new JsonArray();
		JsonObject eventImageOne = new JsonObject();
		eventImageOne.addProperty("image_id", 1);
		eventImageOne.addProperty("image_url", "http://sportteam.co.za/wp-content/uploads/2017/02/istock_000051598656_large.jpg__1600x475_q85_crop_subject_location-2438626_upscale-1024x304.jpg");
		
		JsonObject eventImageTwo = new JsonObject();
		eventImageTwo.addProperty("image_id", 2);
		eventImageTwo.addProperty("image_url", "http://www.limontasport.com/wp-content/uploads/2016/03/limonta-sport-box-referenze-600x456.jpg");
		
		JsonObject eventImageThree = new JsonObject();
		eventImageThree.addProperty("image_id", 3);
		eventImageThree.addProperty("image_url", "https://cnnespanol2.files.wordpress.com/2017/03/170313194633-25-what-a-shot-0314-super-169.jpg?quality=90&strip=all");
		
		images.add(eventImageOne);
		images.add(eventImageTwo);
		images.add(eventImageThree);

		tempJson.add("images", images);
		
		return tempJson;
	}

	/**
		EVENT IN THE PAST
	**/
	public static Object generateJsonEventMoreInThePast() {
		Event tempEvent = Event.createDummyEvent();
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("event_id", tempEvent.getEventId());
		tempJson.addProperty("title", tempEvent.getTitle());
		tempJson.addProperty("description", tempEvent.getDescription());
		tempJson.addProperty("date", (tempEvent.getDate() - (86400000*10) ));
		tempJson.addProperty("time_from", (tempEvent.getTimeFrom() - (86400000*10)));
		tempJson.addProperty("time_to", (tempEvent.getTimeTo() - (86400000*10)));
		tempJson.addProperty("owner_id", 1);
		
		tempJson.addProperty("owner_name", "Mityo Krika");
		tempJson.addProperty("owner_image_url", "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png");
		
		JsonArray specialities = new JsonArray();
		specialities.add("yoga");
		specialities.add("running");
		specialities.add("jogging");
		
		tempJson.add("specialities", specialities);
		
		JsonArray attending = new JsonArray();
		JsonObject tempUser = new JsonObject();
		tempUser.addProperty("user_id", 1);
		tempUser.addProperty("user_name", "zayo bayo");
		tempUser.addProperty("user_image_url", "https://i49.vbox7.com/o/47c/47cb43c49a0.jpg");
		
		JsonObject tempUser2 = new JsonObject();
		tempUser2.addProperty("user_id", 41);
		tempUser2.addProperty("user_name", "Kumcho Vulcho");
		tempUser2.addProperty("user_image_url", "https://images.webcafe.bg/2012/08/29/%E2%FA%EB%EA/618x464.jpg");

		JsonObject tempUser3 = new JsonObject();
		tempUser3.addProperty("user_id", 23);
		tempUser3.addProperty("user_name", "100 kila");
		tempUser3.addProperty("user_image_url", "http://senzacia.net/wp-content/uploads/2013/05/100-kila-1.jpg");
		
		attending.add(tempUser);
		attending.add(tempUser2);
		attending.add(tempUser3);
		
		tempJson.add("attending", attending);
		
		tempJson.addProperty("event_image_url", tempEvent.getEventImageUrl());
		tempJson.addProperty("coord_lat", tempEvent.getCoordLatitude());
		tempJson.addProperty("coord_lnt", tempEvent.getCoordLongitude());
		tempJson.addProperty("price_from", 10);
		tempJson.addProperty("address", tempEvent.getAddress());
		tempJson.addProperty("free_slots", tempEvent.getFreeSlots());
		tempJson.addProperty("date_created", tempEvent.getDateCreated());
		
		JsonArray images = new JsonArray();
		JsonObject eventImageOne = new JsonObject();
		eventImageOne.addProperty("image_id", 1);
		eventImageOne.addProperty("image_url", "http://sportteam.co.za/wp-content/uploads/2017/02/istock_000051598656_large.jpg__1600x475_q85_crop_subject_location-2438626_upscale-1024x304.jpg");
		
		JsonObject eventImageTwo = new JsonObject();
		eventImageTwo.addProperty("image_id", 2);
		eventImageTwo.addProperty("image_url", "http://www.limontasport.com/wp-content/uploads/2016/03/limonta-sport-box-referenze-600x456.jpg");
		
		JsonObject eventImageThree = new JsonObject();
		eventImageThree.addProperty("image_id", 3);
		eventImageThree.addProperty("image_url", "https://cnnespanol2.files.wordpress.com/2017/03/170313194633-25-what-a-shot-0314-super-169.jpg?quality=90&strip=all");
		
		images.add(eventImageOne);
		images.add(eventImageTwo);
		images.add(eventImageThree);

		tempJson.add("images", images);
		
		return tempJson;
	}
	
	
	/**
		EVENT IN THE FUTURE
	 **/
	public static Object generateJsonEventInTheFuture() {
		Event tempEvent = Event.createDummyEvent();
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("event_id", tempEvent.getEventId());
		tempJson.addProperty("title", tempEvent.getTitle());
		tempJson.addProperty("description", tempEvent.getDescription());
		tempJson.addProperty("date", (tempEvent.getDate() + (86400000*20) ));
		tempJson.addProperty("time_from", (tempEvent.getTimeFrom() + (86400000*20)));
		tempJson.addProperty("time_to", (tempEvent.getTimeTo() + (86400000*20)));
		tempJson.addProperty("owner_id", 1);
		
		tempJson.addProperty("owner_name", "Mityo Krika");
		tempJson.addProperty("owner_image_url", "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png");
		
		JsonArray specialities = new JsonArray();
		specialities.add("yoga");
		specialities.add("running");
		specialities.add("jogging");
		
		tempJson.add("specialities", specialities);
		
		JsonArray attending = new JsonArray();
		JsonObject tempUser = new JsonObject();
		tempUser.addProperty("user_id", 1);
		tempUser.addProperty("user_name", "zayo bayo");
		tempUser.addProperty("user_image_url", "https://i49.vbox7.com/o/47c/47cb43c49a0.jpg");
		
		JsonObject tempUser2 = new JsonObject();
		tempUser2.addProperty("user_id", 41);
		tempUser2.addProperty("user_name", "Kumcho Vulcho");
		tempUser2.addProperty("user_image_url", "https://images.webcafe.bg/2012/08/29/%E2%FA%EB%EA/618x464.jpg");

		JsonObject tempUser3 = new JsonObject();
		tempUser3.addProperty("user_id", 23);
		tempUser3.addProperty("user_name", "100 kila");
		tempUser3.addProperty("user_image_url", "http://senzacia.net/wp-content/uploads/2013/05/100-kila-1.jpg");
		
		attending.add(tempUser);
		attending.add(tempUser2);
		attending.add(tempUser3);
		
		tempJson.add("attending", attending);
		
		tempJson.addProperty("event_image_url", tempEvent.getEventImageUrl());
		tempJson.addProperty("coord_lat", tempEvent.getCoordLatitude());
		tempJson.addProperty("coord_lnt", tempEvent.getCoordLongitude());
		tempJson.addProperty("price_from", 10);
		tempJson.addProperty("address", tempEvent.getAddress());
		tempJson.addProperty("free_slots", tempEvent.getFreeSlots());
		tempJson.addProperty("date_created", tempEvent.getDateCreated());
		
		JsonArray images = new JsonArray();
		JsonObject eventImageOne = new JsonObject();
		eventImageOne.addProperty("image_id", 1);
		eventImageOne.addProperty("image_url", "http://sportteam.co.za/wp-content/uploads/2017/02/istock_000051598656_large.jpg__1600x475_q85_crop_subject_location-2438626_upscale-1024x304.jpg");
		
		JsonObject eventImageTwo = new JsonObject();
		eventImageTwo.addProperty("image_id", 2);
		eventImageTwo.addProperty("image_url", "http://www.limontasport.com/wp-content/uploads/2016/03/limonta-sport-box-referenze-600x456.jpg");
		
		JsonObject eventImageThree = new JsonObject();
		eventImageThree.addProperty("image_id", 3);
		eventImageThree.addProperty("image_url", "https://cnnespanol2.files.wordpress.com/2017/03/170313194633-25-what-a-shot-0314-super-169.jpg?quality=90&strip=all");
		
		images.add(eventImageOne);
		images.add(eventImageTwo);
		images.add(eventImageThree);

		tempJson.add("images", images);
		
		return tempJson;
	}
	
	/**
	EVENT IN THE FUTURE
	 **/
	public static Object generateJsonEventMoreInTheFuture() {
		Event tempEvent = Event.createDummyEvent();
		JsonObject tempJson = new JsonObject();
		tempJson.addProperty("event_id", tempEvent.getEventId());
		tempJson.addProperty("title", tempEvent.getTitle());
		tempJson.addProperty("description", tempEvent.getDescription());
		tempJson.addProperty("date", (tempEvent.getDate() + (86400000*30) ));
		tempJson.addProperty("time_from", (tempEvent.getTimeFrom() + (86400000*30)));
		tempJson.addProperty("time_to", (tempEvent.getTimeTo() + (86400000*30)));
		tempJson.addProperty("owner_id", 1);
		
		tempJson.addProperty("owner_name", "Mityo Krika");
		tempJson.addProperty("owner_image_url", "https://www.novini.bg/uploads/news_pictures/2016-35/big/mitio-krika-spuka-10-dini-s-glavata-si-398375.png");
		
		JsonArray specialities = new JsonArray();
		specialities.add("yoga");
		specialities.add("running");
		specialities.add("jogging");
		
		tempJson.add("specialities", specialities);
		
		JsonArray attending = new JsonArray();
		JsonObject tempUser = new JsonObject();
		tempUser.addProperty("user_id", 1);
		tempUser.addProperty("user_name", "zayo bayo");
		tempUser.addProperty("user_image_url", "https://i49.vbox7.com/o/47c/47cb43c49a0.jpg");
		
		JsonObject tempUser2 = new JsonObject();
		tempUser2.addProperty("user_id", 41);
		tempUser2.addProperty("user_name", "Kumcho Vulcho");
		tempUser2.addProperty("user_image_url", "https://images.webcafe.bg/2012/08/29/%E2%FA%EB%EA/618x464.jpg");
	
		JsonObject tempUser3 = new JsonObject();
		tempUser3.addProperty("user_id", 23);
		tempUser3.addProperty("user_name", "100 kila");
		tempUser3.addProperty("user_image_url", "http://senzacia.net/wp-content/uploads/2013/05/100-kila-1.jpg");
		
		attending.add(tempUser);
		attending.add(tempUser2);
		attending.add(tempUser3);
		
		tempJson.add("attending", attending);
		
		tempJson.addProperty("event_image_url", tempEvent.getEventImageUrl());
		tempJson.addProperty("coord_lat", tempEvent.getCoordLatitude());
		tempJson.addProperty("coord_lnt", tempEvent.getCoordLongitude());
		tempJson.addProperty("price_from", 10);
		tempJson.addProperty("address", tempEvent.getAddress());
		tempJson.addProperty("free_slots", tempEvent.getFreeSlots());
		tempJson.addProperty("date_created", tempEvent.getDateCreated());
		
		JsonArray images = new JsonArray();
		JsonObject eventImageOne = new JsonObject();
		eventImageOne.addProperty("image_id", 1);
		eventImageOne.addProperty("image_url", "http://sportteam.co.za/wp-content/uploads/2017/02/istock_000051598656_large.jpg__1600x475_q85_crop_subject_location-2438626_upscale-1024x304.jpg");
		
		JsonObject eventImageTwo = new JsonObject();
		eventImageTwo.addProperty("image_id", 2);
		eventImageTwo.addProperty("image_url", "http://www.limontasport.com/wp-content/uploads/2016/03/limonta-sport-box-referenze-600x456.jpg");
		
		JsonObject eventImageThree = new JsonObject();
		eventImageThree.addProperty("image_id", 3);
		eventImageThree.addProperty("image_url", "https://cnnespanol2.files.wordpress.com/2017/03/170313194633-25-what-a-shot-0314-super-169.jpg?quality=90&strip=all");
		
		images.add(eventImageOne);
		images.add(eventImageTwo);
		images.add(eventImageThree);
	
		tempJson.add("images", images);
		
		return tempJson;
	}

	public static void resetPassword(String email, String pass) throws MessagingException {
		sendMail();
//		final String username = "leaps.dev@gmail.com";
//		final String password = "leaps.dev1";
//
//		Properties props = new Properties();
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.ssl.enable", "true");
//		props.put("mail.transport.protocol", "smtps");
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.port", "465");
//
//		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(username, password);
//			}
//		  });
//		
//		System.out.println("Pass: " + pass);
//		System.out.println("Email: " + email);
//		
//		try {
//
//			Message message = new MimeMessage(session);
//			message.setFrom(new InternetAddress("from-email@gmail.com"));
//			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
//			message.setSubject("Reset Leaps password");
//			message.setText("Testing,"
//				+ "\n\n Your new pass is: " + pass);
//
//			Transport.send(message);
//		} catch (MessagingException e) {
//			throw new RuntimeException(e);
//		}
	}
	
	
	
	public static void sendMail() throws MessagingException {
	
		// IMPORTANT: To successfully send an email, you must replace the values of the strings below with your own values.   
		  String EMAIL_FROM = "leaps.dev@gmail.com";    // Replace with the sender's address. This address must be verified with Amazon SES.
//		  String EMAIL_REPLY_TO  = "REPLY-TO@EXAMPLE.COM";  // Replace with the address replies should go to. This address must be verified with Amazon SES. 
		  String EMAIL_RECIPIENT = "noexile@gmail.com"; // Replace with a recipient address. If your account is still in the sandbox,
		                                                   // this address must be verified with Amazon SES.  
		  String EMAIL_ATTACHMENTS = "ATTACHMENT-FILE-NAME-WITH-PATH"; // Replace with the path of an attachment. Must be a valid path or this project will not build.
		                                                              // Remember to use two slashes in place of each slash.
		  
		  // IMPORTANT: Ensure that the region selected below is the one in which your identities are verified.  
		  Regions AWS_REGION = Regions.US_WEST_2;           // Choose the AWS region of the Amazon SES endpoint you want to connect to. Note that your sandbox 
		                                                   // status, sending limits, and Amazon SES identity-related settings are specific to a given AWS 
		                                                   // region, so be sure to select an AWS region in which you set up Amazon SES. Here, we are using 
		                                                   // the US West (Oregon) region. Examples of other regions that Amazon SES supports are US_EAST_1 
		                                                   // and EU_WEST_1. For a complete list, see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html 
		  
		String EMAIL_SUBJECT   = "Amazon SES email test";
		String EMAIL_BODY_TEXT = "This MIME email was sent through Amazon SES using SendRawEmail.";
		  
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);
		message.setSubject(EMAIL_SUBJECT, "UTF-8");
		
		message.setFrom(new InternetAddress(EMAIL_FROM));
//		message.setReplyTo(new Address[]{new InternetAddress(EMAIL_REPLY_TO)});
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_RECIPIENT));
		
		// Cover wrap
		MimeBodyPart wrap = new MimeBodyPart();
		
		// Alternative TEXT/HTML content
		MimeMultipart cover = new MimeMultipart("alternative");
		MimeBodyPart html = new MimeBodyPart();
		cover.addBodyPart(html);
		
		wrap.setContent(cover);
		
		MimeMultipart content = new MimeMultipart("related");
		message.setContent(content);
		content.addBodyPart(wrap);
		
		String[] attachmentsFiles = new String[]{
		    EMAIL_ATTACHMENTS
		};
		
		// This is just for testing HTML embedding of different type of attachments.
		StringBuilder sb = new StringBuilder();
		
		for (String attachmentFileName : attachmentsFiles) {
		    String id = UUID.randomUUID().toString();
		    sb.append("<img src=\"cid:");
		sb.append(id);
		sb.append("\" alt=\"ATTACHMENT\"/>\n");
		
		MimeBodyPart attachment = new MimeBodyPart();
		
		DataSource fds = new FileDataSource(attachmentFileName);
		attachment.setDataHandler(new DataHandler(fds));
		attachment.setHeader("Content-ID", "<" + id + ">");
		    attachment.setFileName(fds.getName());
		
		    content.addBodyPart(attachment);
		}
		
		html.setContent("<html><body><h1>HTML</h1>\n" + EMAIL_BODY_TEXT + "</body></html>", "text/html");
		
		try {
		    System.out.println("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");
		
		/*
		 * The ProfileCredentialsProvider will return your [default]
		 * credential profile by reading from the credentials file located at
		 * (~/.aws/credentials).
		 *
		 * TransferManager manages a pool of threads, so we create a
		 * single instance and share it throughout our application.
		 */
		AWSCredentials credentials = null;
		try {
		    credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
		    throw new AmazonClientException(
		            "Cannot load the credentials from the credential profiles file. " +
		"Please make sure that your credentials file is at the correct " +
		"location (~/.aws/credentials), and is in valid format.",
		            e);
		}
		
		// Instantiate an Amazon SES client, which will make the service call with the supplied AWS credentials.
		AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(credentials);
		Region REGION = Region.getRegion(AWS_REGION);
		client.setRegion(REGION);
		
		// Print the raw email content on the console
		PrintStream out = System.out;
		message.writeTo(out);
		
		// Send the email.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		message.writeTo(outputStream);
		RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
		
		SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
		client.sendRawEmail(rawEmailRequest);
		System.out.println("Email sent!");
		
		} catch (Exception ex) {
		  System.out.println("Email Failed");
		System.err.println("Error message: " + ex.getMessage());
		        ex.printStackTrace();
		    }
		}

	public static void logRetrievedUserFromTheDB(User user) {
		logger.info("User details: \n User Id: " + user.getUserId() + ", Username: " + user.getUsername() + ", Email: " + user.getEmail() + ", Age: " + user.getAge()
					+ ", Gender: " + user.getGender() + ", " + user.getLocation() + ", Max Distance Setting: " + user.getMaxDistanceSetting() + ", First Name: " + user.getFirstName()
					+ ", Last Name: " + user.getLastName() + ", Birthday: " + user.getBirthday() + ", Description: " + user.getDescription() + ", Profile Image Url: " + user.getProfileImageUrl()
					+ ", Is Trainer: " + user.isTrainer() + ", Facebook Id: " + user.getFacebookId() + ", Google Id: " + user.getGoogleId() + ", Phone Number: " + user.getPhoneNumber()
		 			+ ", Session Price: " + user.getSessionPrice() + ", Long Description: " + user.getLongDescription() + ", Years of Training: " + user.getYearsOfTraining());
	}

	public static void logRetrievedEventsFromTheDB(Event event) {
		logger.info("Event details: \n Event Id: " + event.getEventId() + ", Title: " + event.getTitle() + ", Description: " + event.getDescription() + ", Date: " + event.getDate()
					+ ", Time From: " + event.getTimeFrom() + ", Time To: " + event.getTimeTo() + ", Owner Id: " + event.getOwnerId() + ", Event Image Url: " + event.getEventImageUrl()
					+ ", Coordinates Latitude: " + event.getCoordLatitude() + ", Coordinates Longitude: " + event.getCoordLongitude() + ", Price From: " + event.getPriceFrom()
					+ ", Address: " + event.getAddress() + ", Free Slots: " + event.getFreeSlots());
	}

	public static void logRetrievedImageFromTheDB(String imageId, String fileName) {
		logger.info("Image Details: \n Image Id: " + imageId + ", File Name: " + fileName);
	}

	public static void logRetrievedTagsFromTheDB(List<Tag> tags) {
		StringBuilder loggedTags = new StringBuilder("Tags: ");
		for (int i = 0; i < tags.size(); i++) {
			loggedTags.append(tags.get(i).getName());
			if (i + 1 < tags.size()) {
				loggedTags.append(", ");
			}
		}
		logger.info(loggedTags.toString());
	}

	public static void logRetrievedTagsFromTheDBAsString(List<String> tags) {
		StringBuilder loggedTags = new StringBuilder("Tags: ");
		for (int i = 0; i < tags.size(); i++) {
			loggedTags.append(tags.get(i));
			if (i + 1 < tags.size()) {
				loggedTags.append(", ");
			}
		}
		logger.info(loggedTags.toString());
	}
}
