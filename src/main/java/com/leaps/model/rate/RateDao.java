package com.leaps.model.rate;

public class RateDao {
	
	public static enum RateDaoEnum {
		INSTANCE;
		
		public Rate createRate(long eventId, int rating, long userId, String comment, long dateCreated) {
			return new Rate(eventId, rating, userId, comment, dateCreated);
		}
		
		public Rate createRate(long commentId, long eventId, int rating, long userId, String comment, long dateCreated) {
			return new Rate(commentId, eventId, rating, userId, comment, dateCreated);
		}
		
		public Rate createRate(long commentId, long eventId, int rating, long userId, String comment, long dateCreated, String imageUrl) {
			return new Rate(commentId, eventId, rating, userId, comment, dateCreated, imageUrl);
		}
	}
}
