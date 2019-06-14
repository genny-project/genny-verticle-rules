package life.genny.eventbus;

import javax.naming.NamingException;

import io.vertx.rxjava.core.eventbus.EventBus;
import life.genny.channel.Producer;
import life.genny.cluster.CurrentVtxCtx;
import life.genny.qwanda.entity.BaseEntity;

public class EventBusVertx implements EventBusInterface {
	private static final String WEBCMDS = "webcmds";
	private static final String CMDS = "cmds";
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