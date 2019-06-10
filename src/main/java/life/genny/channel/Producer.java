package life.genny.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.MessageProducer;

public class Producer {

	private static MessageProducer<Object> toClientOutbound;
	private static MessageProducer<Object> toEvents;
	private static MessageProducer<Object> toData;
	private static MessageProducer<Object> toWebData;
	private static MessageProducer<Object> toMessages;
	private static MessageProducer<Object> toCmds;
	private static MessageProducer<Object> toWebCmds;
	private static MessageProducer<Object> toServices;
	private static MessageProducer<Object> toSocial;
	private static MessageProducer<Object> toHealth;
	private static MessageProducer<Object> toSignals;
	private static MessageProducer<Object> toStatefulMessages;

	public static MessageProducer<Object> getToSocial() {
		return toSocial;
	}

	public static void setToSocial(MessageProducer<Object> toSocial) {
		Producer.toSocial = toSocial;
	}

	private static Map<String, MessageProducer<JsonObject>> channelSessionList = new HashMap<String, MessageProducer<JsonObject>>();

	private static Map<String, Set<MessageProducer<JsonObject>>> userSessionMap = new HashMap<String, Set<MessageProducer<JsonObject>>>();

	/**
	 * @return the userSessionMap
	 */
	public static Map<String, Set<MessageProducer<JsonObject>>> getUserSessionMap() {
		return userSessionMap;
	}

	/**
	 * @param userSessionMap
	 *            the userSessionMap to set
	 */
	public static void setUserSessionMap(Map<String, Set<MessageProducer<JsonObject>>> userSessionMap) {
		Producer.userSessionMap = userSessionMap;
	}

	/**
	 * @return the channelSessionList
	 */
	public static Map<String, MessageProducer<JsonObject>> getChannelSessionList() {
		return channelSessionList;
	}

	/**
	 * @param channelSessionList
	 *            the channelSessionList to set
	 */
	public static void setChannelSessionList(Map<String, MessageProducer<JsonObject>> channelSessionList) {
		Producer.channelSessionList = channelSessionList;
	}

	/**
	 * @return the toClientOutbount
	 */
	public static MessageProducer<Object> getToClientOutbound() {
		return toClientOutbound;
	}

	/**
	 * @param toClientOutbount
	 *            the toClientOutbount to set
	 */
	public static void setToClientOutbound(MessageProducer<Object> toClientOutbount) {
		Producer.toClientOutbound = toClientOutbount;
	}

	/**
	 * @return the toEvents
	 */
	public static MessageProducer<Object> getToEvents() {
		return toEvents;
	}

	/**
	 * @return the toData
	 */
	public static MessageProducer<Object> getToData() {
		return toData;
	}

	/**
	 * @param toData
	 *            the toData to set
	 */
	public static void setToData(MessageProducer<Object> toData) {
		Producer.toData = toData;
	}

	/**
	 * @param toEvents
	 *            the toEvents to set
	 */
	public static void setToEvents(MessageProducer<Object> toEvents) {
		Producer.toEvents = toEvents;
	}

	public static MessageProducer<Object> getToMessages() {
		return toMessages;
	}

	public static void setToMessages(MessageProducer<Object> toMessages) {
		Producer.toMessages = toMessages;
	}

	public static MessageProducer<Object> getToCmds() {
		return toCmds;
	}

	public static void setToCmds(MessageProducer<Object> toCmds) {
		Producer.toCmds = toCmds;
	}

	/**
	 * @return the toServices
	 */
	public static MessageProducer<Object> getToServices() {
		return toServices;
	}

	/**
	 * @param toServices
	 *            the toServices to set
	 */
	public static void setToServices(MessageProducer<Object> toServices) {
		Producer.toServices = toServices;
	}

	/**
	 * @return the toWebCmds
	 */
	public static MessageProducer<Object> getToWebCmds() {
		return toWebCmds;
	}

	/**
	 * @param toWebCmds
	 *            the toWebCmds to set
	 */
	public static void setToWebCmds(MessageProducer<Object> toWebCmds) {
		Producer.toWebCmds = toWebCmds;
	}

	/**
	 * @return the toWebData
	 */
	public static MessageProducer<Object> getToWebData() {
		return toWebData;
	}

	/**
	 * @param toWebData
	 *            the toWebData to set
	 */
	public static void setToWebData(MessageProducer<Object> toWebData) {
		Producer.toWebData = toWebData;
	}

	/**
	 * @return the toHealth
	 */
	public static MessageProducer<Object> getToHealth() {
		return toHealth;
	}

	/**
	 * @param toWebData
	 *            the toHealth to set
	 */
	public static void setToHealth(MessageProducer<Object> toHealth) {
		Producer.toHealth = toHealth;
	}
	
	
	
	
	public static MessageProducer<Object> getToSignals() {
		return toSignals;
	}

	public static void setToSignals(MessageProducer<Object> toSignals) {
		Producer.toSignals = toSignals;
	}

	public static MessageProducer<Object> getToStatefulMessages() {
		return toStatefulMessages;
	}

	public static void setToStatefulMessages(MessageProducer<Object> toStatefulMessages) {
		Producer.toStatefulMessages = toStatefulMessages;
	}

	public static void registerAllProducers(EventBus eb) {
		setToEvents(eb.publisher("events"));
		setToData(eb.publisher("data"));
		setToWebData(eb.publisher("webdata"));
		setToCmds(eb.publisher("cmds"));
		setToWebCmds(eb.publisher("webcmds"));
		setToServices(eb.publisher("services"));
		setToMessages(eb.publisher("messages"));
		setToSocial(eb.publisher("social"));
		setToHealth(eb.publisher("health"));
		setToSignals(eb.publisher("signals"));
		setToStatefulMessages(eb.publisher("statefulmessages"));
	}

}
