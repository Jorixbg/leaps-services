package com.leaps.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="EVENTS")
public class Event {

    @Id
    @Column(name="event_id")
    private long eventId;

    @Column(name="address")
    private String address;

    @Column(name="coord_lat")
    private Double coordLat;

    @Column(name="coord_lnt")
    private Double coordLnt;

    @Column(name="date")
    private Long date;

    @Column(name="date_created")
    private Long dateCreated;

    @Column(name="description")
    private String description;

    @Column(name="event_image_url")
    private String eventImageUrl;

    @Column(name="firebase_topic")
    private String firebaseTopic;

    @Column(name="free_slots")
    private Integer freeSlots;

    @Column(name="owner_id")
    private Long ownerId;

    @Column(name="price_from")
    private BigDecimal priceFrom;

    @Column(name="time_from")
    private Long timeFrom;

    @Column(name="time_to")
    private Long timeTo;

    @Column(name="title")
    private String title;

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getCoordLat() {
        return coordLat;
    }

    public void setCoordLat(Double coordLat) {
        this.coordLat = coordLat;
    }

    public Double getCoordLnt() {
        return coordLnt;
    }

    public void setCoordLnt(Double coordLnt) {
        this.coordLnt = coordLnt;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventImageUrl() {
        return eventImageUrl;
    }

    public void setEventImageUrl(String eventImageUrl) {
        this.eventImageUrl = eventImageUrl;
    }

    public String getFirebaseTopic() {
        return firebaseTopic;
    }

    public void setFirebaseTopic(String firebaseTopic) {
        this.firebaseTopic = firebaseTopic;
    }

    public Integer getFreeSlots() {
        return freeSlots;
    }

    public void setFreeSlots(Integer freeSlots) {
        this.freeSlots = freeSlots;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public BigDecimal getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(BigDecimal priceFrom) {
        this.priceFrom = priceFrom;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
