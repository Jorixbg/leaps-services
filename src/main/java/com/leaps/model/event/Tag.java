package com.leaps.model.event;

public class Tag {
	private int tagId;
	private String name;
	private long ownerId;
	
	public Tag(int tagId, String name) {
		this.tagId = tagId;
		this.name = name;
	}

	
	// getters and setters
	public int getTagId() {
		return tagId;
	}

	public void setTagId(int tagId) {
		this.tagId = tagId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}
}
