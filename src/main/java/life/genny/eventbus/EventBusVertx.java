package life.genny.eventbus;

import javax.naming.NamingException;

import io.vertx.rxjava.core.eventbus.EventBus;
import life.genny.channel.Producer;
import life.genny.cluster.CurrentVtxCtx;
import life.genny.qwanda.entity.BaseEntity;

public class EventBusVertx implements EventBusInterface {
	private static final String WEBCMDS = "webcmds";
	private static final String CMDS = "cmds";
	private static final String WEBDATA = "webdata";
	private static final String DATA = "data";
	private static final String EVENTS = "events";
	private static final String EVENT = "event";
	private static final String SIGNALS = "signals";
	private static final String MESSAGES = "messages";
	private static final String STATEFULMESSAGES = "statefulmessages";
	private static final String SERVICES = "services";
	
	EventBus eventBus = null;

	public EventBusVertx() {
		eventBus = CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus();
	}

	public EventBusVertx(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public  void write(final String channel, final Object payload) throws NamingException 
	{
		switch (channel) {
		case EVENT:
		case EVENTS:
			if (Producer.getToServices().writeQueueFull()) {
				log.error("EVENTS >> producer data is full hence message cannot be sent");
				Producer.setToEvents(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(EVENTS));
				Producer.getToEvents().send(payload);
			} else {
				Producer.getToEvents().send(payload);
			}
			break;
		case DATA:
			Producer.getToData().write(payload).end();
			if (Producer.getToData().writeQueueFull()) {
				log.error("DATA >> producer data is full hence message cannot be sent");
				Producer.setToData(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(DATA));
				Producer.getToData().write(payload).end();
			} else {
				Producer.getToData().write(payload).end();
			}
			break;

		case WEBDATA:
			if (Producer.getToWebData().writeQueueFull()) {
				log.error("WEBDATA >> producer data is full hence message cannot be sent");
				Producer.setToWebData(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(WEBDATA));
				Producer.getToWebData().write(payload).end();
			} else {
				Producer.getToWebData().write(payload).end();
			}
			break;
		case CMDS:
			if (Producer.getToCmds().writeQueueFull()) {
				log.error("CMDS >> producer data is full hence message cannot be sent");
				Producer.setToCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(CMDS));
				Producer.getToCmds().send(payload).end();
			} else {
				Producer.getToCmds().send(payload).end();
			}
			break;

		case WEBCMDS:
			if (Producer.getToWebCmds().writeQueueFull()) {
				log.error("WEBCMDS >> producer data is full hence message cannot be sent");
				Producer.setToWebCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(WEBCMDS));
				Producer.getToWebCmds().send(payload).end();
			} else {
				Producer.getToWebCmds().send(payload).end();
			}
			break;
		case SERVICES:
			if (Producer.getToServices().writeQueueFull()) {
				log.error("SERVICES >> producer data is full hence message cannot be sent");
				Producer.setToServices(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(SERVICES));
				Producer.getToServices().send(payload).end();
			} else {
				Producer.getToServices().send(payload).end();
			}
			break;

		case MESSAGES:
			if (Producer.getToMessages().writeQueueFull()) {
				log.error("MESSAGES >> producer data is full hence message cannot be sent");
				Producer.setToMessages(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(MESSAGES));
				Producer.getToMessages().send(payload).end();
			} else {
				Producer.getToMessages().send(payload).end();
			}

			break;
		case STATEFULMESSAGES:
			if (Producer.getToStatefulMessages().writeQueueFull()) {
				log.error("STATEFULMESSAGES >> producer data is full hence message cannot be sent");
				Producer.setToStatefulMessages(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(STATEFULMESSAGES));
				Producer.getToStatefulMessages().send(payload).end();
			} else {
				Producer.getToStatefulMessages().send(payload).end();
			}
			break;
		case SIGNALS:
			if (Producer.getToSignals().writeQueueFull()) {
				log.error("SIGNALS >> producer data is full hence message cannot be sent");
				Producer.setToSignals(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(SIGNALS));
				Producer.getToSignals().write(payload).end();
			} else {
				Producer.getToSignals().write(payload).end();
			}
			break;
		default:
			log.error("Channel does not exist: " + channel);
		}	
	}
	
	public  void send(final String channel, final Object payload) throws NamingException 
	{
		switch (channel) {
		case "event":
		case "events":
			Producer.getToEvents().send(payload).end();
			;
			break;
		case "data":
			Producer.getToData().write(payload).end();
			break;

		case "webdata":
			Producer.getToWebData().write(payload).end();
			break;
		case CMDS:
			if (Producer.getToCmds().writeQueueFull()) {
				log.error("WEBSOCKET EVT >> producer data is full hence message cannot be sent");
				Producer.setToCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(CMDS));
				Producer.getToCmds().send(payload);

			} else {
				Producer.getToCmds().send(payload);
			}
			break;

		case WEBCMDS:
			if (Producer.getToWebCmds().writeQueueFull()) {
				log.error("WEBSOCKET EVT >> producer data is full hence message cannot be sent");
				Producer.setToWebCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(WEBCMDS));
				Producer.getToWebCmds().send(payload);

			} else {
				Producer.getToWebCmds().send(payload);
			}
			break;
		case "services":
			Producer.getToServices().write(payload);
			break;
		case "messages":
			Producer.getToMessages().send(payload);
			break;
		case "statefulmessages":
			Producer.getToStatefulMessages().send(payload);
			break;
		case "signals":
			Producer.getToSignals().write(payload);
			break;
		default:
			log.error("Channel does not exist: " + channel);
		}	
	}
	
	@Override
	public void publish(BaseEntity user, String channel, Object payload, final String[] filterAttributes) {
		// Actually Send ....
		switch (channel) {
		case "event":
		case "events":
			Producer.getToEvents().send(payload).end();
			;
			break;
		case "data":
			Producer.getToData().write(payload).end();
			break;

		case "webdata":
			payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
			Producer.getToWebData().write(payload).end();
			break;
		case "cmds":
		case "webcmds":
			payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
			if (Producer.getToWebCmds().writeQueueFull()) {
				log.error("WEBSOCKET EVT >> producer data is full hence message cannot be sent");
				Producer.setToWebCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(WEBCMDS));
				Producer.getToWebCmds().send(payload);

			} else {
				Producer.getToWebCmds().send(payload);
			}
			break;
		case "services":
			Producer.getToServices().write(payload);
			break;
		case "messages":
			Producer.getToMessages().send(payload);
			break;
		case "statefulmessages":
			Producer.getToStatefulMessages().send(payload);
			break;
		case "signals":
			Producer.getToSignals().write(payload);
			break;
		default:
			log.error("Channel does not exist: " + channel);
		}
	}
}