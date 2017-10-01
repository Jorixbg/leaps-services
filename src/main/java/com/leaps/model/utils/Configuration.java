package com.leaps.model.utils;

public class Configuration {
	
	// Tag variables
	public static final int TAG_SELECT_LIMIT = 15;
	
	// Leaps json variables
	public static final String USER_ID = "user_id";
	
	// Exception messages
	public static final String WRONG_USERNAME_PASSWORD_MESSAGE = "Wrong username or password!";
	public static final String NO_USER_FOUND = "No user was found with the presented token.";
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
	
	public static final String IMAGE_START_PATH = "/home/ec2-user/leaps/";
	public static final String USER_IMAGE_PATH = "Images/Users/";
	public static final String EVENT_IMAGE_PATH = "Images/Events/";
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
	
	// Debug mode Configuration for logging
	public static final boolean debugMode = true;
	
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
}