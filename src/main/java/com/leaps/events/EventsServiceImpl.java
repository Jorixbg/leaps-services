package com.leaps.events;

import com.leaps.entities.Event;
import com.leaps.repositories.EventRepository;
import com.leaps.web.entities.EventsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventsServiceImpl implements EventsService {

    @Autowired
    private EventRepository eventRepository;

    @Override
    public EventsResponse getOwnerEvents(long ownerId) {
        EventsResponse response = new EventsResponse();
        List<String> descriptions = new ArrayList<>();
        List<Event> events = eventRepository.findByOwnerId(ownerId);
        System.out.println(events);
        events.forEach(event -> {
            descriptions.add(event.getDescription());
        });
        response.setDescriptions(descriptions);
        return response;
    }

}
