package com.leaps.events;

import com.leaps.web.entities.EventsRequest;
import com.leaps.web.entities.EventsResponse;
import org.springframework.stereotype.Service;

@Service
public interface EventsService {

    EventsResponse getOwnerEvents(long ownerId);
}
