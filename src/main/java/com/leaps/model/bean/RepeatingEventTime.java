package com.leaps.model.bean;

public class RepeatingEventTime {
	private String period;
	private String start;
	private String end;
	
	public RepeatingEventTime (String period, String start, String end) {
		this.period = period;
		this.start = start;
		this.end = end;
	}
	
	public String getPeriod() {
		return period;
	}
	
	public String getStart() {
		return start;
	}
	
	public String getEnd() {
		return end;
	}
}
