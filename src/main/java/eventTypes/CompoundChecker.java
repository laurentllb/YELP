package eventTypes;

import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

import eventEngine.CompoundEvent;
import eventEngine.CompoundEventDataModel;
import eventEngine.CompoundEventInterface;
import eventEngine.Event;

import java.util.ArrayList;
import java.util.HashMap;

public class CompoundChecker implements CompoundEventInterface {

	private static final Logger logger = Logger.getLogger(CompoundChecker.class.getName());

	CompoundEventDataModel config;

	HashMap<String, String> currentState;
	ArrayList<String> description;
	HashMap<String, ArrayList<Event>> eventList;

	private CompoundChecker() {

	}

	public CompoundChecker(CompoundEventDataModel config) {

		this.config = config;
		if (this.config.eventKey == null)
			this.config.eventKey = new String("defaultKey");
		currentState = new HashMap<String, String>();
		eventList = new HashMap<String, ArrayList<Event>>();
	}

	private boolean checkCondition(String keyValue, Event event, CompoundEventDataModel.Transitions transition) {
		
		if (transition.condition == null || transition.condition.key == null
				|| transition.condition.value == null || transition.condition.operation == null)
			return true;

		logger.log(Level.INFO, "We're doing a comparison!!!!!");
		

		String key = transition.condition.key;
		if (event.getEventMetaData() != null && !event.getEventMetaData().containsKey(key))
			return false;
		String eventValue=event.getEventMetaData().get(key);
				
		logger.log(Level.INFO, "We're still doing a comparison!!!!!"+event.getEventMetaData().get(key));
		
		if (transition.condition.operation.contentEquals("equals"))
		{
			return transition.condition.value.contentEquals(eventValue);
		}
		else if (transition.condition.operation.contentEquals("not"))
		{
			return !transition.condition.value.contentEquals(eventValue);
		}	
		else
		{
			return false;
		}
			
			
	}

	public ArrayList<CompoundEvent> processState(Event event) {

		ArrayList<CompoundEvent> newEvents=new ArrayList<CompoundEvent>();
		logger.log(Level.FINEST, "Process Event: " + event.getEventType().getEventName());

		ArrayList<String> keyValues = new ArrayList<String>();

		if (event.getEventType().getEventName().contentEquals("end_of_file")) {
			keyValues.addAll(currentState.keySet());
		} else {
			if (event.getEventMetaData() == null || event.getEventMetaData().get(this.config.eventKey) == null)
				return newEvents;
			keyValues.add(event.getEventMetaData().get(this.config.eventKey));
		}

		for (String keyValue : keyValues) {
			logger.log(Level.FINEST,
					"Process Event: " + event.getEventType().getEventName() + " keyValue[" + keyValue + "]");

			if (currentState.get(keyValue) == null) {
				currentState.put(keyValue, "start");
				logger.log(Level.FINEST, "Process Event: " + event.getEventType().getEventName()
						+ " New keyValue found:[" + keyValue + "] [" + this.config.eventKey + "]");
				eventList.put(keyValue, new ArrayList<Event>());
			}

			if (currentState.get(keyValue).contentEquals("finished"))
				continue;

			for (CompoundEventDataModel.Transitions transition : config.transitions) {

				logger.log(Level.FINEST,
						"Current State:" + currentState.get(keyValue) + " Looking at transition:" + transition.event);
				if (transition.state.contentEquals(currentState.get(keyValue))
						&& transition.event.contentEquals(event.getEventType().getEventName())) {

					if (!checkCondition(keyValue, event, transition))
						break;

					logger.log(Level.FINE, "Found transition to:[" + transition.next_state + "] with event ["
							+ event.getEventType().getEventName() + "] for key [" + keyValue + "]");

					currentState.put(keyValue, transition.next_state);
					eventList.get(keyValue).add(event);
					break;
				}
			}

			if (currentState.get(keyValue).contentEquals("display")) {
				
				HashMap<String, String> map = new HashMap<String, String>();
				CompoundEvent newEvent = new CompoundEvent(eventList.get(keyValue).get(0).getLine(), this, this.config.priority,eventList.get(keyValue));
				newEvent.setEventMetaData(map);
				newEvents.add(newEvent);
				
				System.out.println("AWOOOGA ["+newEvent.getSummary()+"]"+this.config.priority);
			//	System.out.println("This was caused by the following events:");
			//	for (Event savedEvent : eventList.get(keyValue)) {
			//		System.out.println("	Event:" + savedEvent.getSummary());
			//	}
				currentState.put(keyValue, "finished");
			}
		}
		
		return newEvents;
	}
	
	@Override
	public ArrayList<String> getDescription() {
		return config.description;
	}

	@Override
	public String getSummary() {
		return config.summary;
	}

	@Override
	public String getEventName() {
		// TODO Auto-generated method stub
		return config.checkerName;
	}

	
	
}