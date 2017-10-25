package com.leaps.model.rate;

public class Rate {
	private long commentId;
	private long eventId;
	private int rating;
	private long userId;
	private String comment;
	private long dateCreated;
	private String imageUrl;
	
	public Rate(long eventId, int rating, long userId, String comment, long dateCreated) {
		this.eventId = eventId;
		this.rating = rating;
		this.userId = userId;
		this.comment = comment;
		this.dateCreated = dateCreated;
	}
	
	public Rate(long commentId, long eventId, int rating, long userId, String comment, long dateCreated) {
		this.commentId = commentId;
		this.eventId = eventId;
		this.rating = rating;
		this.userId = userId;
		this.comment = comment;
		this.dateCreated = dateCreated;
	}
	
	public Rate(long commentId, long eventId, int rating, long userId, String comment, long dateCreated, String imageUrl) {
		this.commentId = commentId;
		this.eventId = eventId;
		this.rating = rating;
		this.userId = userId;
		this.comment = comment;
		this.dateCreated = dateCreated;
		this.imageUrl = imageUrl;
	}
	
	
	public long getEventId() {
		return eventId;
	}
	
	public int getRating() {
		return rating;
	}
	
	public String getComment() {
		return comment;
	}
	
	public long getDateCreated() {
		return dateCreated;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public long getUserId() {
		return userId;
	}

	public long getCommentId() {
		return commentId;
	}
}
