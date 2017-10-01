package com.leaps.model.image;

public class Image {
	private long imageId;
	private long ownerTypeId;
	private String imageName;
		
	public Image(long imageId, long ownerTypeId, String imageName) {
		this.imageId = imageId;
		this.ownerTypeId = ownerTypeId;
		this.imageName = imageName;
	}
	
	public long getImageId() {
		return imageId;
	}
	
	public void setImageId(long imageId) {
		this.imageId = imageId;
	}
	
	public long getEventId() {
		return ownerTypeId;
	}
	
	public void setEventId(long eventId) {
		this.ownerTypeId = eventId;
	}
	
	public String getImageName() {
		return imageName;
	}
	
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
}
