package com.leaps.model.event;

import java.util.ArrayList;
import java.util.List;

import com.leaps.model.image.Image;

/**
{
        "title" : "trenirovchitsa",
        "description" : "Nai-ultra mega huper yakata trenirovka ever izmislyana", 
        "date" : "6226623422532",
        "time_from" : "6226623422532",
        "time_to" : "6226623422532",
        "owner_id" : 1,
        "owner_name" : "Mityo Krika",
        "owner_image_url" : "nyakakvo url",
        "specialities" : [ "yoga", "running", "jogging" ],
        "event_image_url" : "some url",
        "coord_lat" : 42.693351,
        "coord_lnt" : 23.340381,
        "price_from" : 10,
        "address" : "ул. „Шипка“ 34-36, 1504 София",
        "free_slots" : 50,
        "date_created" : "1365475684564",
        "tags" : [ "tag1", "tag2", "tag3" ... ]
}
**/

public class Event {
	private long eventId;
	private String title;
	private String description;
	private Long date;
	private Long timeFrom;
	private Long timeTo;
	private long ownerId;
	private String eventImageUrl;
	private double coordLatitude;
	private double coordLongitude;
	private int priceFrom;
	private String address;
	private int freeSlots;
	private Long dateCreated;
	private String firebaseTopic;
	private List<Tag> tags;
	private List<Image> images;
	
	Event(long eventId, String title, String description, Long date, Long timeFrom, Long timeTo, long ownerId, String eventImageUrl,
			double coordLatitude, double coordLongitude, int priceFrom, String address, int freeSlots, Long dateCreated, String firebaseTopic) {
		this.eventId = eventId;
		this.title = title;
		this.description = description;
		this.date = date;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
		this.ownerId = ownerId;
		this.eventImageUrl = eventImageUrl;
		this.coordLatitude = coordLatitude;
		this.coordLongitude = coordLongitude;
		this.priceFrom = priceFrom;
		this.address = address;
		this.freeSlots = freeSlots;
		this.dateCreated = dateCreated;
		tags = new ArrayList<Tag>();
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public Long getTimeFrom() {
		return timeFrom;
	}

	public void setTimeFrom(Long timeFrom) {
		this.timeFrom = timeFrom;
	}

	public Long getTimeTo() {
		return timeTo;
	}

	public void setTimeTo(Long timeTo) {
		this.timeTo = timeTo;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public double getCoordLatitude() {
		return coordLatitude;
	}

	public void setCoordLatitude(double coordLatitude) {
		this.coordLatitude = coordLatitude;
	}

	public double getCoordLongitude() {
		return coordLongitude;
	}

	public void setCoordLongitude(double coordLongitude) {
		this.coordLongitude = coordLongitude;
	}

	public int getPriceFrom() {
		return priceFrom;
	}

	public void setPriceFrom(int priceFrom) {
		this.priceFrom = priceFrom;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getFreeSlots() {
		return freeSlots;
	}

	public void setFreeSlots(int freeSlots) {
		this.freeSlots = freeSlots;
	}

	public Long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Long dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getEventImageUrl() {
		return eventImageUrl;
	}

	public void setEventImageUrl(String eventImageUrl) {
		this.eventImageUrl = eventImageUrl;
	}

	public List<Tag> getTags() {
		return tags;
	}
	
	public void addTag(Tag tag) {
		getTags().add(tag);
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public String getFirebaseTopic() {
		return firebaseTopic;
	}

	public void setFirebaseTopic(String firebaseTopic) {
		this.firebaseTopic = firebaseTopic;
	}
}
