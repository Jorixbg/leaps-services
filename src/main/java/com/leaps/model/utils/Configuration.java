package com.leaps.model.utils;

public class Configuration {

	public static final String FIREBASE_SERVER_KEY = "AAAAupXoEEc:APA91bE1CGVtuCT1kkgY3TByf9SQzWKgnJKXFOQMdJ3yb2D8fbgqRwEhv_koX2-LfiernysTwGkFUGfGLFOT73ECnavgH6MgJr5Gp_EYL5kVVitqPhrY8sgX0LNdI9iEO91r2nmC1iUc";
	public static final String FIREBASE_API_URL = "https://fcm.googleapis.com/fcm/send";
	
	public enum RepeatOptions {
		never, every_day, every_weekdays, every_weekends, every_week, every_two_weeks, every_month, custom;
	}

	public enum FrequencyOptions {
		daily, weekly;
	}
	
	public enum WeekDays {
		monday, tuesday, wednesday, thursday, friday, saturday, sunday, everyday
	}
	
	public static final int MAX_DAYS_OF_WEEK = 7;
	
	public static final String THIRTY_MINUTES = "1";
	
	public static final String MONDAY = "monday";
	public static final String TUESDAY = "tuesday";
	public static final String WEDNESDAY = "wednesday";
	public static final String THURSDAY = "thursday";
	public static final String FRIDAY = "friday";
	public static final String SATURDAY = "saturday";
	public static final String SUNDAY = "sunday";
	
	// Debug mode Configuration for logging
	public static final boolean debugMode = true;
	
	// Tag variables
	public static final int TAG_SELECT_LIMIT = 15;
	
	// Leaps json variables
	public static final String USER_ID = "user_id";
	
	public static final String FAR_FAR_AWAY_TIME = "2900000000000";


	public static final String LEAPS_GEOMAP_KEY = "AIzaSyBPoBOLpB51WtS9mYTNTrAVj0x642ZjA6g";
	
	// Exception messages
	public static final String WRONG_USERNAME_PASSWORD_MESSAGE = "Wrong username or password!";
	public static final String NO_USER_FOUND = "No user was found";
	public static final String INVALID_OR_EXPIRED_TOKEN = "Invalid or expired token";
	public static final String INVALID_USER_ID = "Invalid user id";
	public static final String INVALID_EVENT_ID = "Invalid event id";
	public static final String INVALID_EMAIL = "Invalid email address";
	public static final String INVALID_INPUT_PAREMETERS = "Invalid input parameters";
	public static final String FACEBOOK_LOGIN_ERROR = "A problem occured while trying to login with facebook.";
	public static final String GOOGLE_LOGIN_ERROR = "A problem occured while trying to login with google.";
	public static final String USER_ALREADY_EXIST = "User already exist";
	public static final String ERROR_WHILE_CREATING_NEW_USER = "Something went wrong while trying to create a new user.";
	public static final String ERROR_WHILE_CREATING_NEW_EVENT = "Something went wrong while trying to create a new event.";
	public static final String EVENT_DOES_NOT_EXIST_OR_CANNOT_BE_RETREIVED = "Event does not exist or cannot be retreived.";
	public static final String ERROR_WHILE_UPDATING_THE_USER = "Error while updating the user";
	public static final String USER_ALREADY_ATTENDS_TO_THE_EVENT = "The user already is attending the event.";
	
	// Events config
	public static final int EVENTS_MAX_TAG_SIZE = 10;
	public static final int USER_MAX_PICTURE_SIZE = 5;
	public static final int EVENT_MAX_PICTURE_SIZE = 5;
	public static final int TOKEN_MAX_VALUE = 1000000001;
	public static final int TOKEN_SIZE = 10;
	public static final int TOKEN_SIZE_FOR_CREATE_EVENT = 3;
	public static final String EVENT_ATTENDEE_LIMIT_IS_REACHED = "The event's attendee limit is reached.";
	public static final String CANNOT_REMOVE_USER_FROM_EVENT = "Cannot unattend the user from the event.";
	
	
	// TEST TOKEN
	public static final int ACCEPTED_TOKEN = 1111177777;
	
	// TEST USER DATA
	public static final String USER_USERNAME = "mityo_krika";
	public static final String USER_EMAIL = "krika_original@mail.bg";
	
	// Images configuration
	public static final String FILE_LOCATION = "C:/Users/Blacksmith/Desktop/IMAGES/";
	public static final String SERVER_EVENTS_FILE_LOCATION = "/home/ec2-user/leaps/Images/Events/";
	public static final String SERVER_USERS_FILE_LOCATION = "/home/ec2-user/leaps/Images/Users/";
	
//	public static final String IMAGE_START_PATH = "/home/ec2-user/leaps/";
	public static final String IMAGE_START_PATH = "/root/leaps/";
	public static final String USER_IMAGE_PATH = "Images/Users/";
	public static final String EVENT_IMAGE_PATH = "Images/Events/";
	public static final String RATE_IMAGE_PATH = "Images/Rate/";
	public static final String IMAGE_DOES_NOT_EXISTS = "Image does not exist";
	public static final String IMAGE_LIMIT_FOR_USER_IS_REACHED = "Image limit for the user is reached";
	public static final String IMAGE_LIMIT_FOR_EVENT_IS_REACHED = "Image limit for the event is reached";
	
	public static final String CANNOT_INSERT_IMAGE_INTO_DATABASE = "Cannot insert the image into the database";
	public static final String CANNOT_REMOVE_IMAGE_FROM_DB = "Cannot remove the image from the database";
	public static final String CANNOT_REMOVE_IMAGE_FROM_SERVER = "Cannot remove the image from the server";
	public static final String CANNOT_RETRIEVE_TAGS_FROM_SERVER = "Cannot return the tags from the server.";

	public static final int MAX_USER_IMAGE_COUNT = 5;
	public static final int MAX_EVENT_IMAGE_COUNT = 5;

	public static final int USER_DEFAULT_MAX_DISTANCE_SETTING = 20;
	public static final int FILTER_EVENTS_DEFAULT_PAGE_LIMITATION = 20;
	public static final int EVENTS_RETURN_SIZE_IN_FEED = 3;
	public static final int TRAINER_FEED_LIMIT_SIZE = 20;
	public static final int MOST_POPULAR_EVENTS_RETURN_SIZE = 20;
	public static final int NEARBY_EVENTS_RETURN_SIZE = 20;

	public static final Object NOT_AVAILABLE_PARAM_FOR_NEARBY_EVENTS = "na";
	
	
	// Three hours in millisecconds
	// TODO: fix the timezone
	public static final long THREE_HOURS_IN_MS = 10800000;

	public static final String USER_IS_ALREADY_FOLLOWED = "User is already followed";
	public static final String USER_DOES_NOT_EXIST = "User does not exist";
	public static final String ERROR_RETREIVING_THE_USERS = "Cannot retrieve the users from db";
	public static final String EVENT_IS_ALREADY_FOLLOWED = "Event is already followed";
	public static final String EVENT_DOES_NOT_EXIST = "Event does not exist";

	public static final String CANNOT_RATE_CURRENT_EVENT = "Cannot rate current event.";

	public static final int MIN_RATING = 1;
	public static final int MAX_RATING = 5;

	public static final String INVALID_RATING_VALUE = "Rating cannot be less than " + MIN_RATING + " or greater than " + MAX_RATING;

	public static final String CANNOT_RETRIEVE_EVENT_RATINGS = "Cannot retrieve event ratings";

	public static final String CANNOT_RETRIEVE_EVENT_FOLLOWERS = "Cannot retrieve event followers";

	public static final String CANNOT_RETRIEVE_IMAGE_FROM_DB = "Cannot retrieve image from db";

	public static final String CANNOT_RETRIEVE_FOLLOWED_EVENTS = "Cannot retrieve followed events";
	
	public static final int DEFAULT_PAGE_COMMENTS_LIMIT = 20;

	public static final String INVALID_PAGE_NUMBER = "The selected page cannot be 0";

	public static final String EVENT_ID_PARAM_IS_REQUIRED = "event_id parameter is mandatory.";

	public static final String CANNOT_UPDATE_EVENT = "Cannot update event";

	public static final String CANNOT_INSERT_TAGS_IN_THE_DB = "Cannot insert tags in the db";

	public static final String CANNOT_DELETE_TAGS_FOR_EVENT = "Cannot delete tags for event";

	public static final String ERROR_READING_DATA_FROM_GEOLOCATOR = "There was an error while trying to read the data from geolocator";

	public static final String CANNOT_DELETE_EVENT = "Cannot delete event";

	public static final String CANNOT_RETRIEVE_RATE = "Cannot retrieve rate";

	public static final String CANNOT_CREATE_NEW_EVENT = "Cannot create new event";

	public static final String CANNOT_RETRIEVE_FOLLOWING_COUNT = "Cannot retrieve following count";

	public static final String CANNOT_RETRIEVE_FOLLOWERS_COUNT = "Cannot retrieve followers count";

	public static final String CANNOT_INSERT_USER_INTO_DB = "Cannot insert user into DB";

	public static final String INVALID_TAG = "Invalid Tag";

	public static final String CANNOT_RETRIEVE_USER_SPECIALTIES = "Cannot retrieve user specialties";

	public static final String CANNOT_RETRIEVE_ATTENDING_EVENT_USERS = "Cannot retrieve attending event users";

	public static final String CANNOT_UPDATE_USER = "Cannot update user";

	public static final String CANNOT_RETRIEVE_ATTENDING_EVENTS_FOR_USER = "Cannot retrieve attending events for user";

	public static final String CANNOT_RETRIEVE_HOSTING_EVENTS_FOR_USER = "Cannot retrieve hosting events for user";

	public static final String CANNOT_RETRIEVE_EVENT_ATTENDEES = "Cannot retrieve event attendees";

	public static final String CANNOT_UNNATEND_FROM_EVENT = "Cannot unattend from event";

	public static final String CANNOT_RETRIEVE_NUMBER_OF_PAST_ATTENDING_EVENTS = "Cannot retrieve number of past attending events";

	public static final String CANNOT_CHECK_IF_USER_ATTENDS_EVENT = "Cannot check if user already attends an event";

	public static final String CANNOT_RETRIEVE_USERS_FROM_DB = "Cannot retrieve users from db";

	public static final String CANNOT_RETRIEVE_MOST_POPULAR_EVENTS = "Cannot retrieve most popular events";

	public static final String CANNOT_RETRIEVE_UPCOMMING_EVENTS = "Cannot retrieve upcomming events";

	public static final String CANNOT_RETRIEVE_NEARBY_UPCOMMING_EVENTS = "Cannot retrieve nearby upcomming events";

	public static final String CANNOT_RETRIEVE_FILTERED_EVENTS = "Cannot retrieve filtered events";

	public static final String CANNOT_RETRIEVE_FILTERED_TRAINERS = "Cannot retrieve filtered trainers";

	public static final String CANNOT_RETRIEVE_TAGS_FROM_DB = "Cannot retrieve tags from db";

	public static final String CANNOT_DELETE_SPECIALTIES_FOR_USER = "Cannot delete specialties for user";

	public static final String CANNOT_INSERT_SPECIALTIES_IN_THE_DB = "Cannot insert specialties into the db";
	public static final String ERROR_WHILE_SENDING_MESSAGE_TO_FIREBASE_TOPIC = "Error while sending message to firebase topic";
	public static final String ERROR_UPDATING_FIREBASE_TOKEN = "Error while updating the user's firebase token";
}