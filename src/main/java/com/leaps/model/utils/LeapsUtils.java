package com.leaps.model.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

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
import com.leaps.model.db.DBUserDao;
import com.leaps.model.event.Event;
import com.leaps.model.event.EventDao;
import com.leaps.model.event.Tag;
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.exceptions.TagException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.image.Image;
import com.leaps.model.rate.Rate;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;

public class LeapsUtils {

	private static final Logger logger = LoggerFactory.getLogger(LeapsUtils.class);

	// TODO: test
	public static boolean checkIfValueIsValid(String checkedValue, Object[] values) {
		boolean flag = false;
		
		if (checkedValue != null) {
			for (int i = 0; i < values.length; i++) {
				String enumValue = values[i].toString();
				if (checkedValue.equalsIgnoreCase(enumValue)) {
					flag = true;
					break;
				}
			}
		}
		
		return flag;
	}
	
	public static void runScheduler() {
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler scheduler = sf.getScheduler();
			
			JobDetail job = JobBuilder.newJob(RepeatingEventTimer.class).withIdentity("dummyJobName", "group1").build();
			
			Date startTime = DateBuilder.nextGivenSecondDate(null, 10);
			
			// run every 30 minutes infinite loop
			CronTrigger crontrigger = TriggerBuilder
				.newTrigger()
				.withIdentity("RepeatingEvents", "group1")
				.startAt(startTime)
				// startNow()
				.withSchedule(CronScheduleBuilder.cronSchedule(Configuration.THIRTY_MINUTES + " * * * * ?"))
				.build();
			
			scheduler.start();
			scheduler.scheduleJob(job, crontrigger);
			
			// scheduler.shutdown();
		} catch (SchedulerException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
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
		double dist = round(earthRadius * c, 2);
		
		return dist;
	}
	
	public static int generateAgeFromBirthday(long birthday) {
		Long time= System.currentTimeMillis() / 1000 - birthday / 1000;
		int years = Math.round(time) / 31536000;
		System.out.println(years);
		return years;
	}
	
	/**
	 * Util method for generating a Json Event from defined input parameters
	 * @throws UserException 
	 * @throws EventException 
	 * @throws ImageException 
	 * @throws TagException 
	 */
	public static JsonObject generateJsonEvent(Event event, Long token, Double requestingUserLatitude, 
			Double requestingUserLongitude) throws UserException, EventException, ImageException, TagException {
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
		tempJson.addProperty("distance", (requestingUserLatitude != null && requestingUserLongitude != null) ? 
							calculateDistance(requestingUserLatitude, requestingUserLongitude, event) : null);
		tempJson.addProperty("can_rate", token != null ? UserDao.getInstance().canRate(event, token) : false);
		
		List<Rate> rate = DBUserDao.getInstance().getRatesForEvent(event.getEventId());
		
		tempJson.addProperty("rating", getEventRate(rate));
		tempJson.addProperty("reviews", rate.size());
		
		JsonArray specialitiesJson = new JsonArray();
		
		List<Tag> specialities = DBUserDao.getInstance().getAllEventTagsFromDb(event.getEventId());
		
		for (int i = 0; i < specialities.size(); i++) {
			specialitiesJson.add(specialities.get(i).getName());
		}
		
		tempJson.add("specialities", specialitiesJson);
		
		Map<String, Object> attendees = EventDao.getInstance().getAllEventAttendees(event, token);
		JsonObject attendingJsonObject = (JsonObject) attendees.get("json");
		List<User> attending = (List<User>) attendees.get("attendees");
		
		tempJson.add("attending", attendingJsonObject);
		
		tempJson.addProperty("event_image_url", event.getEventImageUrl());
		tempJson.addProperty("coord_lat", event.getCoordLatitude());
		tempJson.addProperty("coord_lnt", event.getCoordLongitude());
		tempJson.addProperty("price_from", event.getPriceFrom());
		tempJson.addProperty("address", event.getAddress());
		tempJson.addProperty("free_slots", event.getFreeSlots() - attending.size());
		tempJson.addProperty("date_created", event.getDateCreated());
		tempJson.addProperty("firebase_topic", event.getFirebaseTopic());
		
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
	
	private static double calculateDistance(Double requestingUserLatitude, Double requestingUserLongitude, Event event) {
		return distFrom(requestingUserLatitude, requestingUserLongitude, event.getCoordLatitude(), event.getCoordLongitude());
	}

	/**
	 * Utility method for getting average rate from a list of rates
	 */
	public static double getEventRate(List<Rate> rate) {
		double approxRate = 0;
		double eventRate = 0;
		
		if (rate.size() > 0) {
			for (int i = 0; i < rate.size(); i++) {
				approxRate += rate.get(i).getRating();
			}
			
			eventRate = round(approxRate / rate.size(), 2);
		}
			
		return eventRate;
	}

	/**
	 * Util method for generating a Json User
	 * @throws ImageException 
	 * @throws TagException 
	 */
	public static JsonObject generateJsonUser(User user, Long token, Double lat, Double lng) throws EventException, UserException, ImageException, TagException {
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
		response.addProperty("firebase_token", user.getFirebaseToken());
		response.addProperty("is_trainer", user.isTrainer());
		response.addProperty("long_description", user.isTrainer() ? user.getLongDescription() : null);
		response.addProperty("years_of_training", user.isTrainer() ? user.getYearsOfTraining() : null);
		response.addProperty("session_price", user.isTrainer() ? user.getSessionPrice() : null);
		
		List<Integer> rating = UserDao.getInstance().getAllUserRatings(user.getUserId());		
		double approxRate = 0;
		double userRate = 0;
		
		if (rating.size() > 0) {
			for (int i = 0; i < rating.size(); i++) {
				approxRate += rating.get(i);
			}
			
			userRate = round(approxRate / rating.size(), 2);
		}

		response.addProperty("rating", userRate);
		response.addProperty("reviews", rating.size());
		
		response.addProperty("following_count", UserDao.getInstance().getFollowingCount(user.getUserId()));
		response.addProperty("followers_count", UserDao.getInstance().getFollowersCount(user.getUserId()));
		
		response.add("followed_by", UserDao.getInstance().getAllUserFollowers(user));
		
		List<Event> attendingEvents = DBUserDao.getInstance().getAllAttendingEventsForUser(user.getUserId());
		JsonArray attendingEventsJson = new JsonArray();
		
		for (int i = 0; i < attendingEvents.size(); i++) {
			attendingEventsJson.add(LeapsUtils.generateJsonEvent(attendingEvents.get(i), token, lat, lng));
		}
		response.add("attending_events", attendingEventsJson);
		
		List<Event> hostingEvents = DBUserDao.getInstance().getAllHostingEventsForUser(user.getUserId());
		JsonArray hostingEventsJson = new JsonArray();
		for (int i = 0; i < hostingEvents.size(); i++) {			
			hostingEventsJson.add(LeapsUtils.generateJsonEvent(hostingEvents.get(i), token, lat, lng));
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
	 * Util method for generating a Json Comment
	 * @throws TagException 
	 */
	public static JsonObject generateJsonComment(long commentId) throws EventException, UserException, TagException {
		JsonObject response = new JsonObject();
		Rate rate = DBUserDao.getInstance().getRate(commentId);
		User user = DBUserDao.getInstance().getUserFromDbById(rate.getUserId());
		Event event = DBUserDao.getInstance().getEventById(rate.getEventId());

		response.addProperty("event_id", rate.getEventId());
		response.addProperty("event_title", event.getTitle());
		response.addProperty("event_image_url", event.getEventImageUrl());
		
		List<Rate> eventRate = DBUserDao.getInstance().getRatesForEvent(event.getEventId());
		
		response.addProperty("event_rating", getEventRate(eventRate));
		
		List<Tag> tags = DBUserDao.getInstance().getAllEventTagsFromDb(rate.getEventId());
		JsonArray jsonTags = new JsonArray();
		for (int i = 0; i < tags.size(); i++) {
			jsonTags.add(tags.get(i).getName());
		}
		response.add("tags", jsonTags);

		response.addProperty("user_id", rate.getUserId());
		response.addProperty("user_profile_image_url", user.getProfileImageUrl());
		
		String name = null;
		if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
			name = user.getFirstName();
		} else if (user.getLastName() != null && !user.getLastName().isEmpty()) {
			name = user.getLastName();
		} else if (user.getUsername() != null && !user.getUsername().isEmpty()) {
			name = user.getUsername();
		}
		response.addProperty("user_name", name);
		
		response.addProperty("comment_rating", rate.getRating());
		response.addProperty("comment", rate.getComment());
		response.addProperty("date_created", rate.getDateCreated());
		response.addProperty("comment_image", rate.getImageUrl());
		
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
	
	/**
	 * Rounding method
	 */
	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	/****************************/
	/** BELOW ARE DUMMY METHODS */
	/****************************/
	
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
		  Regions AWS_REGION = Regions.EU_WEST_1;           // Choose the AWS region of the Amazon SES endpoint you want to connect to. Note that your sandbox 
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

	// TODO: not used atm
	public static void attendEventInFirebase(User user, Event event) {
		String eventFirebaseTopic = event.getFirebaseTopic();
		String userFirebaseToken = user.getFirebaseToken();
		
		// TODO: write logic
	}

	// TODO: not used atm
	public static void unattendEventInFirebase(User user, Event event) {
		String eventFirebaseTopic = event.getFirebaseTopic();
		String userFirebaseToken = user.getFirebaseToken();
		
		// TODO: write logic
	}
	
	public static HttpStatus sendMessageToFirebaseTopic(String message, long eventId) throws EventException {		
		String title = "Event change";
		
		JsonObject body = new JsonObject();
		// the firebase topic is a string formatted event id
		body.addProperty("to", "/topics/" + String.valueOf(eventId));
		body.addProperty("priority", "high");
		
		JsonObject notification = new JsonObject();
		notification.addProperty("title", title);
		notification.addProperty("body", message);
		
		body.add("notification", notification);
		
		HttpEntity<String> request = new HttpEntity<>(body.toString());
		
		CompletableFuture<String> pushNotification = pushNotifications(request);
		CompletableFuture.allOf(pushNotification).join();
		
		try {
			pushNotification.get();
			return HttpStatus.OK;
		} catch (InterruptedException ie) {
			logger.info(ie.getMessage());
			throw new EventException(Configuration.ERROR_WHILE_SENDING_MESSAGE_TO_FIREBASE_TOPIC);
		} catch (ExecutionException ee) {
			logger.info(ee.getMessage());
			throw new EventException(Configuration.ERROR_WHILE_SENDING_MESSAGE_TO_FIREBASE_TOPIC);
		}
	}
	
	public static CompletableFuture<String> pushNotifications(HttpEntity<String> entity) {
		RestTemplate restTemplate = new RestTemplate();
		
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + Configuration.FIREBASE_SERVER_KEY));
		interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
		restTemplate.setInterceptors(interceptors);
 
		String firebaseResponse = restTemplate.postForObject(Configuration.FIREBASE_API_URL, entity, String.class);
 
		return CompletableFuture.completedFuture(firebaseResponse);
	}
}
