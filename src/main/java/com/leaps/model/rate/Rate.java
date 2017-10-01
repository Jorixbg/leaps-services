package com.leaps.model.rate;

public class Rate {
	private long eventId;
	private int rating;
	private String comment;
	private long dateCreated;
	private String imageUrl;
	
	public Rate(long eventId, int rating, String comment, long dateCreated) {
		this.eventId = eventId;
		this.rating = rating;
		this.comment = comment;
		this.dateCreated = dateCreated;
	}
	
	public Rate(long eventId, int rating, String comment, long dateCreated, String imageUrl) {
		this.eventId = eventId;
		this.rating = rating;
		this.comment = comment;
		this.dateCreated = dateCreated;
		this.imageUrl = imageUrl;
	}
	
	
	public long getEventId() {
		return eventId;
	}
	
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	
	public int getRating() {
		return rating;
	}
	
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public long getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
