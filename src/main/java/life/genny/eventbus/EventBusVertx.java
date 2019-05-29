package life.genny.eventbus;

import io.vertx.rxjava.core.eventbus.EventBus;
import life.genny.channel.Producer;
import life.genny.cluster.CurrentVtxCtx;
import life.genny.qwanda.entity.BaseEntity;

public class EventBusVertx implements EventBusInterface {
	private static final String WEBCMDS = "webcmds";
	EventBus eventBus = null;

	public EventBusVertx() {
		eventBus = CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus();
	}

	public EventBusVertx(EventBus eventBus) {
		this.eventBus = eventBus;
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
		default:
			log.error("Channel does not exist: " + channel);
		}
	}
}