package life.genny.channel;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;
import org.mortbay.log.Log;

import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import life.genny.qwandautils.GennySettings;
import rx.Observable;

public class Consumer {

	  protected static final Logger log =
		      org.apache.logging.log4j.LogManager.getLogger(
		          MethodHandles.lookup().lookupClass().getCanonicalName());

	private static Observable<Message<Object>> fromWebCmds;
	private static Observable<Message<Object>> fromWebData;
	private static Observable<Message<Object>> fromCmds;
	private static Observable<Message<Object>> fromData;
	private static Observable<Message<Object>> fromMessages;
	private static Observable<Message<Object>> fromServices;

	private static Observable<Message<Object>> fromEvents;
	private static Observable<Message<Object>> fromSocial;
	
	private static Observable<Message<Object>> fromHealth;
	
	private static Observable<Message<Object>> fromDirect;
	
	public static String directIP = "";

	/**
	 * @return the fromCmds
	 */
	public static Observable<Message<Object>> getFromCmds() {
		return fromCmds;
	}

	/**
	 * @param fromCmds
	 *            the fromCmds to set
	 */
	private static void setFromCmds(Observable<Message<Object>> fromCmds) {
		Consumer.fromCmds = fromCmds;
	}

	/**
	 * @return the fromData
	 */
	public static Observable<Message<Object>> getFromData() {
		return fromData;
	}

	/**
	 * @param fromData
	 *            the fromData to set
	 */
	public static void setFromData(Observable<Message<Object>> fromData) {
		Consumer.fromData = fromData;
	}

	/**
	 * @return the fromServices
	 */
	public static Observable<Message<Object>> getFromServices() {
		return fromServices;
	}

	/**
	 * @param fromServices
	 *            the fromServices to set
	 */
	public static void setFromServices(Observable<Message<Object>> fromServices) {
		Consumer.fromServices = fromServices;
	}

	/**
	 * @return the events
	 */
	public static Observable<Message<Object>> getFromEvents() {
		return fromEvents;
	}

	/**
	 * @param events
	 *            the events to set
	 */
	public static void setFromSocial(Observable<Message<Object>> social) {
		Consumer.fromSocial = social;
	}

	/**
	 * @return the data
	 */
	public static Observable<Message<Object>> getFromSocial() {
		return fromSocial;
	}

	/**
	 * @param events
	 *            the events to set
	 */
	public static void setFromEvents(Observable<Message<Object>> events) {
		Consumer.fromEvents = events;
	}

	/**
	 * @return the fromMessages
	 */
	public static Observable<Message<Object>> getFromMessages() {
		return fromMessages;
	}

	/**
	 * @param fromMessages
	 *            the fromMessages to set
	 */
	public static void setFromMessages(Observable<Message<Object>> fromMessages) {
		Consumer.fromMessages = fromMessages;
	}

	/**
	 * @return the fromWebCmds
	 */
	public static Observable<Message<Object>> getFromWebCmds() {
		return fromWebCmds;
	}

	/**
	 * @param fromWebCmds
	 *            the fromWebCmds to set
	 */
	public static void setFromWebCmds(Observable<Message<Object>> fromWebCmds) {
		Consumer.fromWebCmds = fromWebCmds;
	}

	/**
	 * @return the fromWebData
	 */
	public static Observable<Message<Object>> getFromWebData() {
		return fromWebData;
	}

	/**
	 * @param fromWebData
	 *            the fromWebData to set
	 */
	public static void setFromWebData(Observable<Message<Object>> fromWebData) {
		Consumer.fromWebData = fromWebData;
	}

	/**
	 * @return the fromWebData
	 */
	public static Observable<Message<Object>> getFromHealth() {
		return fromHealth;
	}

	/**
	 * @param fromWebData
	 *            the fromWebData to set
	 */
	public static void setFromHealth(Observable<Message<Object>> fromHealth) {
		Consumer.fromHealth = fromHealth;
	}
	
	

	public static Observable<Message<Object>> getFromDirect() {
		return fromDirect;
	}

	public static void setFromDirect(Observable<Message<Object>> fromDirect) {
		Consumer.fromDirect = fromDirect;
	}

	public static void registerAllConsumer(EventBus eb) {
		setFromWebCmds(eb.consumer("webcmds").toObservable());
		setFromWebData(eb.consumer("webdata").toObservable());
		setFromCmds(eb.consumer("cmds").toObservable());
		setFromData(eb.consumer("data").toObservable());
		setFromServices(eb.consumer("services").toObservable());
		setFromEvents(eb.consumer("events").toObservable());
		setFromSocial(eb.consumer("social").toObservable());
		setFromMessages(eb.consumer("messages").toObservable());
		setFromHealth(eb.consumer("health").toObservable());
		
	//	myip = "mytest";
		setFromDirect(eb.consumer(GennySettings.myIP).toObservable());
		log.info("This Verticle is listening directly on "+GennySettings.myIP);
		directIP = GennySettings.myIP;  // make available to others
	}

}
