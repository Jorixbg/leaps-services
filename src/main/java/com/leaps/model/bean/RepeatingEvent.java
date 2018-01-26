package com.leaps.model.bean;

public class RepeatingEvent {
	long id;
	long eventId;
	long parentEventId;
	long startTime;
	long endTime;
	boolean exist;

	public RepeatingEvent(long startTime, long endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public RepeatingEvent(long startTime, long endTime, boolean exist) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.exist = exist;
	}
	
	public RepeatingEvent(long parentEventId, long startTime, long endTime, boolean exist, long id) {
		this.parentEventId = parentEventId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.exist = exist;
		this.id = id;
	}
	
	public RepeatingEvent(long parentEventId, long startTime, long endTime, boolean exist) {
		this.parentEventId = parentEventId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.exist = exist;
	}
	
	public RepeatingEvent(long eventId, long parentEventId, long startTime, long endTime, boolean exist) {
		this.eventId = eventId;
		this.parentEventId = parentEventId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.exist = exist;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getEventId() {
		return eventId;
	}
	
	public long getParentEventId() {
		return parentEventId;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	public boolean isExist() {
		return exist;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public void setParentEventId(long parentEventId) {
		this.parentEventId = parentEventId;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}
}
