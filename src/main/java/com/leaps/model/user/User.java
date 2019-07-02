package com.leaps.model.user;

import java.util.List;

import com.leaps.model.image.Image;

public class User {
	private Long userId;
	private String username;
	private String email;
	private String pass;
	private String firstName;
	private String lastName;
	private int age;
	private String gender;
	private String location;
	private int maxDistanceSetting;
	private Long birthday;
	private String description;
	private String profileImageUrl;
	private String googleId;
	private String facebookId;
	private boolean isTrainer;
	private String phoneNumber;
	private int yearsOfTraining;
	private int sessionPrice;
	private String longDescription;
	private String firebaseToken;
	private List<Image> userImages;

	public User () {
		super();
	}
	
	User (Long userId, String username, String email, String pass, String firstName, String lastName, int age, String gender, String location,
			int maxDistanceSetting, Long birthday, String description, String profileImageUrl, boolean isTrainer) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.pass = pass;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.gender = gender;
		this.location = location;
		this.maxDistanceSetting = maxDistanceSetting;
		this.birthday = birthday;
		this.description = description;
		this.profileImageUrl = profileImageUrl;
		this.isTrainer = isTrainer;
	}
	
	User (Long userId, String username, String email, String pass, String firstName, String lastName, int age, String gender, String location,
			int maxDistanceSetting, Long birthday, String description, String profileImageUrl, boolean isTrainer, String phoneNumber,
			int yearsOfTraining, int sessionPrice, String longDescription) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.pass = pass;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.gender = gender;
		this.location = location;
		this.maxDistanceSetting = maxDistanceSetting;
		this.birthday = birthday;
		this.description = description;
		this.profileImageUrl = profileImageUrl;
		this.isTrainer = isTrainer;
		this.phoneNumber = phoneNumber;
		this.yearsOfTraining = yearsOfTraining;
		this.sessionPrice = sessionPrice;
		this.longDescription = longDescription;
	}
	
	User (Long userId, String username, String email, String firstName, String lastName, int age, String gender, String location,
			int maxDistanceSetting, Long birthday, String description, String profileImageUrl, boolean isTrainer, String facebookId, 
			String googleId, String phoneNumber, int yearsOfTraining, int sessionPrice, String longDescription, String firebaseToken) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.gender = gender;
		this.location = location;
		this.maxDistanceSetting = maxDistanceSetting;
		this.birthday = birthday;
		this.description = description;
		this.profileImageUrl = profileImageUrl;
		this.isTrainer = isTrainer;
		this.facebookId = facebookId;
		this.googleId = googleId;
		this.phoneNumber = phoneNumber;
		this.yearsOfTraining = yearsOfTraining;
		this.sessionPrice = sessionPrice;
		this.longDescription = longDescription;
		this.firebaseToken = firebaseToken;
	}

	User (Long userId, String username, String pass, String email, String firstName, String lastName, Long birthday, String facebookId, String googleId, int age, String firebaseToken) {
		this.userId = userId;
		this.username = username;
		this.pass = pass;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = birthday;
		this.facebookId = facebookId;
		this.googleId = googleId;
		this.age = age;
		this.firebaseToken = firebaseToken;
	}

	// getters and setters
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getMaxDistanceSetting() {
		return maxDistanceSetting;
	}

	public void setMaxDistanceSetting(int maxDistanceSetting) {
		this.maxDistanceSetting = maxDistanceSetting;
	}

	public Long getBirthday() {
		return birthday;
	}

	public void setBirthday(Long birthday) {
		this.birthday = birthday;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public boolean isTrainer() {
		return isTrainer;
	}
	
	public void setIsTrainer(boolean isTrainer) {
		this.isTrainer = isTrainer;
	}

	public String getGoogleId() {
		return googleId;
	}

	public void setGoogleId(String googleId) {
		this.googleId = googleId;
	}

	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public int getYearsOfTraining() {
		return yearsOfTraining;
	}

	public void setYearsOfTraining(int yearsOfTraining) {
		this.yearsOfTraining = yearsOfTraining;
	}

	public int getSessionPrice() {
		return sessionPrice;
	}

	public void setSessionPrice(int sessionPrice) {
		this.sessionPrice = sessionPrice;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public void setTrainer(boolean isTrainer) {
		this.isTrainer = isTrainer;
	}

	public List<Image> getUserImages() {
		return userImages;
	}

	public void setUserImages(List<Image> userImages) {
		this.userImages = userImages;
	}

	public String getFirebaseToken() {
		return firebaseToken;
	}

	public void setFirebaseToken(String firebaseToken) {
		this.firebaseToken = firebaseToken;
	}
}
