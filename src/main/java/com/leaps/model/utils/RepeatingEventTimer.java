package com.leaps.model.utils;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leaps.model.bean.RepeatingEvent;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.event.Event;
import com.leaps.model.exceptions.EventException;

public class RepeatingEventTimer implements Job {
	private static final Logger logger = LoggerFactory.getLogger(RepeatingEventTimer.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<RepeatingEvent> repeatingEvents = null;
		
		try {
			repeatingEvents = DBUserDao.getInstance().getScheduledRepeatingEvents();
			
			if (!repeatingEvents.isEmpty()) {
				for(int i = 0; i < repeatingEvents.size(); i++) {
					RepeatingEvent currentEvent = repeatingEvents.get(i);
					
					// first get the parent event
					Event parentEvent = DBUserDao.getInstance().getEventById(currentEvent.getParentEventId());
					
					// second create the new repeating event
					long repeatingEventId = DBUserDao.getInstance().createNewEvent(parentEvent.getTitle(), parentEvent.getDescription(), currentEvent.getStartTime(), currentEvent.getStartTime(),
							currentEvent.getEndTime(), parentEvent.getOwnerId(), parentEvent.getCoordLatitude(), parentEvent.getCoordLongitude(), parentEvent.getPriceFrom(),
							parentEvent.getAddress(), parentEvent.getFreeSlots(), System.currentTimeMillis(), parentEvent.getFirebaseTopic());
					
					// third update the repeating event in the db
					DBUserDao.getInstance().updateRepeatingEvent(repeatingEvents.get(i).getId(), repeatingEventId);
				}
			}
		} catch (EventException e) {
			logger.info("An error occured in the cron schedule. " + e.getMessage());
		}
	}
}
