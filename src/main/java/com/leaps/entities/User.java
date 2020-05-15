package com.leaps.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="USERS")
public class User {
	@Id
	@Column(name="user_id")
	private Long userId;

	@Column(name="username")
	private String username;

	@Column(name="email_address")
	private String email;

	@Column(name="password")
	private String pass;

	@Column(name="first_name")
	private String firstName;

	@Column(name="last_name")
	private String lastName;

	@Column(name="age")
	private int age;

	@Column(name="gender")
	private String gender;

	@Column(name="location")
	private String location;

	@Column(name="max_distance_setting")
	private int maxDistanceSetting;

	@Column(name="birthday")
	private Long birthday;

	@Column(name="description")
	private String description;

	@Column(name="profile_image_url")
	private String profileImageUrl;

	@Column(name="google_id")
	private String googleId;

	@Column(name="facebook_id")
	private String facebookId;

	@Column(name="is_trainer")
	private boolean isTrainer;

	@Column(name="phone_number")
	private String phoneNumber;

	@Column(name="years_of_training")
	private int yearsOfTraining;

	@Column(name="session_price")
	private int sessionPrice;

	@Column(name="long_description")
	private String longDescription;

	@Column(name="firebase_token")
	private String firebaseToken;

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

	public String getFirebaseToken() {
		return firebaseToken;
	}

	public void setFirebaseToken(String firebaseToken) {
		this.firebaseToken = firebaseToken;
	}
}
