package life.genny.channel;

import java.lang.invoke.MethodHandles;
import org.apache.logging.log4j.Logger;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import life.genny.qwandautils.GennySettings;

public class Consumer {

	  protected static final Logger log =
		      org.apache.logging.log4j.LogManager.getLogger(
		          MethodHandles.lookup().lookupClass().getCanonicalName());

	private static MessageConsumer<Object> fromWebCmds;
	private static MessageConsumer<Object> fromWebData;
	private static MessageConsumer<Object> fromCmds;
	private static MessageConsumer<Object> fromData;
	private static MessageConsumer<Object> fromMessages;
	private static MessageConsumer<Object> fromServices;

	private static MessageConsumer<Object> fromEvents;
	private static MessageConsumer<Object> fromSocial;
	private static MessageConsumer<Object> fromHealth;
	private static MessageConsumer<Object> fromDirect;
	
	public static String directIP = "";

	/**
	 * @return the fromCmds
	 */
	public static MessageConsumer<Object> getFromCmds() {
		return fromCmds;
	}

	/**
	 * @param fromCmds
	 *            the fromCmds to set
	 */
	private static void setFromCmds(MessageConsumer<Object> fromCmds) {
		Consumer.fromCmds = fromCmds;
	}

	/**
	 * @return the fromData
	 */
	public static MessageConsumer<Object> getFromData() {
		return fromData;
	}

	/**
	 * @param fromData
	 *            the fromData to set
	 */
	public static void setFromData(MessageConsumer<Object> fromData) {
		Consumer.fromData = fromData;
	}

	/**
	 * @return the fromServices
	 */
	public static MessageConsumer<Object> getFromServices() {
		return fromServices;
	}

	/**
	 * @param fromServices
	 *            the fromServices to set
	 */
	public static void setFromServices(MessageConsumer<Object> fromServices) {
		Consumer.fromServices = fromServices;
	}

	/**
	 * @return the events
	 */
	public static MessageConsumer<Object> getFromEvents() {
		return fromEvents;
	}

	/**
	 * @param events
	 *            the events to set
	 */
	public static void setFromSocial(MessageConsumer<Object> social) {
		Consumer.fromSocial = social;
	}

	/**
	 * @return the data
	 */
	public static MessageConsumer<Object> getFromSocial() {
		return fromSocial;
	}

	/**
	 * @param events
	 *            the events to set
	 */
	public static void setFromEvents(MessageConsumer<Object> events) {
		Consumer.fromEvents = events;
	}

	/**
	 * @return the fromMessages
	 */
	public static MessageConsumer<Object> getFromMessages() {
		return fromMessages;
	}

	/**
	 * @param fromMessages
	 *            the fromMessages to set
	 */
	public static void setFromMessages(MessageConsumer<Object> fromMessages) {
		Consumer.fromMessages = fromMessages;
	}

	/**
	 * @return the fromWebCmds
	 */
	public static MessageConsumer<Object> getFromWebCmds() {
		return fromWebCmds;
	}

	/**
	 * @param fromWebCmds
	 *            the fromWebCmds to set
	 */
	public static void setFromWebCmds(MessageConsumer<Object> fromWebCmds) {
		Consumer.fromWebCmds = fromWebCmds;
	}

	/**
	 * @return the fromWebData
	 */
	public static MessageConsumer<Object> getFromWebData() {
		return fromWebData;
	}

	/**
	 * @param fromWebData
	 *            the fromWebData to set
	 */
	public static void setFromWebData(MessageConsumer<Object> fromWebData) {
		Consumer.fromWebData = fromWebData;
	}

	/**
	 * @return the fromWebData
	 */
	public static MessageConsumer<Object> getFromHealth() {
		return fromHealth;
	}

	/**
	 * @param fromWebData
	 *            the fromWebData to set
	 */
	public static void setFromHealth(MessageConsumer<Object> fromHealth) {
		Consumer.fromHealth = fromHealth;
	}
	
	

	public static MessageConsumer<Object> getFromDirect() {
		return fromDirect;
	}

	public static void setFromDirect(MessageConsumer<Object> fromDirect) {
		Consumer.fromDirect = fromDirect;
	}

	public static void registerAllConsumer(EventBus eb) {
		setFromWebCmds(eb.consumer("webcmds"));
		setFromWebData(eb.consumer("webdata"));
		setFromCmds(eb.consumer("cmds"));
		setFromData(eb.consumer("data"));
		setFromServices(eb.consumer("services"));
		setFromEvents(eb.consumer("events"));
		setFromSocial(eb.consumer("social"));
		setFromMessages(eb.consumer("messages"));
		setFromHealth(eb.consumer("health"));
		
	//	myip = "mytest";
		setFromDirect(eb.consumer(GennySettings.myIP));
		log.info("This Verticle is listening directly on "+GennySettings.myIP);
		directIP = GennySettings.myIP;  // make available to others
	}

}
