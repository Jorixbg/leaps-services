package com.leaps.model.user;

class UserImage {
	private int imageId;
	private User user;
	private String fileName;
	
	UserImage(int imageId, User user, String fileName) {
		this.imageId = imageId;
		this.user = user;
		this.fileName = fileName;
	}

	
	// getters and setters
	int getImageId() {
		return imageId;
	}

	void setImageId(int imageId) {
		this.imageId = imageId;
	}

	User getUser() {
		return user;
	}

	void setUser(User user) {
		this.user = user;
	}

	String getFileName() {
		return fileName;
	}

	void setFileName(String fileName) {
		this.fileName = fileName;
	}
}