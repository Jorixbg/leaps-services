package com.leaps.repositories;

import java.util.List;

import com.leaps.entities.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public interface EventRepository extends CrudRepository<Event, Long> {

    List<Event> findByOwnerId(long ownerId);

    Event findByEventId(long id);
}