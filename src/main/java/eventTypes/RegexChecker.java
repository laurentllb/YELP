package eventTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonDeserializer;

import eventEngine.Event;
import eventEngine.SimpleEventDataModel;
import eventEngine.EventTypeFactory;
import eventEngine.SimpleEventInterface;
import eventEngine.Event.Priority;
import logParser1.Line;
import logParser1.RegexHelper;

public class RegexChecker implements SimpleEventInterface {

	private static final Logger logger =
	        Logger.getLogger(EventTypeFactory.class.getName());
	
	
	private SimpleEventDataModel config;
	private boolean captureMetadata;
	String triggerText;
	Line triggerLine;
	Integer readAheadCount;

	public RegexChecker() {
		captureMetadata = false;

	}

	public RegexChecker(SimpleEventDataModel config) {
		this.captureMetadata = false;
		this.config = config;
		this.readAheadCount = 0;

		if (this.config.linesToCapture == null)
			this.config.linesToCapture = new Integer(0);

		if (this.config.readBehind == null)
			this.config.readBehind = new Integer(0);
		
		this.config.priority=Event.Priority.INFO;

	}

	
	private Event captureMetadata(ArrayList<Line> buffer) {
		HashMap<String, String> map = new HashMap<String, String>();

		Event event = new Event(triggerLine, this, config.priority);
		event.setEventMetaData(map);
		this.captureMetadata = false;

		// logger.log(Level.INFO,"Found error:");
		StringBuffer rawData = new StringBuffer() ;
		Integer startIndex = buffer.size() - this.readAheadCount - this.config.readBehind;
		if (startIndex < 0)
			startIndex = 0;
		logger.log(Level.INFO,"startIndex:" + startIndex);
		for (Line line : buffer.subList(startIndex, buffer.size() - 1)) {
			rawData.append(line.getRawData() + "\\r\\n");
			if (config.captureRegexes != null) {
				for (RegexHelper regex : config.captureRegexes) {
					map.putAll(regex.matchGroups(line.getRawData()));
				}
			}
			map.putAll(config.triggerRegex.matchGroups(line.getRawData()));
			
			if (config.endCaptureRegex != null)
				map.putAll(config.endCaptureRegex.matchGroups(line.getRawData()));
			// System.out.println(line.getLineNo()+":"+line.getRawData());

		}
		map.put("triggerText", this.triggerText);
		map.put("rawData", rawData.toString());

		readAheadCount = 0;
		return event;

	}

	@Override
	public ArrayList<Event> checkLine(Line myLine, ArrayList<Line> buffer) {
		// logger.log(Level.INFO,"Found error"+myLine.getRawData());

		Matcher triggerMatcher = this.config.triggerRegex.getPattern().matcher(myLine.getRawData());
		
		if (triggerMatcher.find()) {
			this.triggerLine = myLine;
			this.triggerText = triggerMatcher.group(0);
			if (this.config.excludeRegex!=null)
			{
				
			Matcher excludeMatcher = this.config.excludeRegex.getPattern().matcher(triggerMatcher.group(0));
			if (excludeMatcher.find())
			{
				logger.log(Level.FINE,"Checking for exclude: ["+excludeMatcher.group(0)+"]");
				return null;
			}
			}
			
		
			logger.log(Level.FINE,"CheckerType: ["+this.getEventName()+"] fired:" + this.triggerText);
			if (config.linesToCapture != 0 || config.endCaptureRegex != null) {
				if (!this.captureMetadata) {
					this.captureMetadata = true;
				}
			} else {
				HashMap<String, String> map = new HashMap<String, String>();
				Event event = new Event(triggerLine, this, config.priority);
				map.putAll(config.triggerRegex.matchGroups(myLine.getRawData()));
				map.put("triggerText", this.triggerText);
				event.setEventMetaData(map);
				return new ArrayList<Event>(Arrays.asList(event));
			}
		}

		if (this.captureMetadata) {
			Matcher endTriggerMatcher = null;
			if (this.config.endCaptureRegex != null) {
				endTriggerMatcher = this.config.endCaptureRegex.getPattern().matcher(myLine.getRawData());
			}

			if (endTriggerMatcher != null && config.linesToCapture == 0)
				config.linesToCapture = buffer.size()-1;

			// Read the rest of the data
			if (readAheadCount < (config.linesToCapture - config.readBehind)
					&& (endTriggerMatcher != null && !endTriggerMatcher.find())) {
				readAheadCount += 1;
			} else {
				return new ArrayList<Event>(Arrays.asList(captureMetadata(buffer)));
			}
		}

		return null;

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
		return config.eventName;
	}

}
