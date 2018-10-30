package life.genny.eventbus;

import io.vertx.rxjava.core.eventbus.EventBus;
import life.genny.channel.Producer;
import life.genny.cluster.CurrentVtxCtx;
import life.genny.qwanda.entity.BaseEntity;

public class EventBusVertx  implements EventBusInterface
{
	EventBus eventBus = CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus();
	
	public EventBusVertx ()
	{
		
	}
	
	public EventBusVertx(EventBus eventBus)
	{
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
			payload = EventBusInterface.privacyFilter(user, payload,filterAttributes);
			Producer.getToWebData().write(payload).end();
			;
			break;
		case "cmds":
			payload = EventBusInterface.privacyFilter(user, payload,filterAttributes);
			Producer.getToWebCmds().write(payload);
			break;
		case "services":
			Producer.getToServices().write(payload);
			break;
		case "messages":
			Producer.getToMessages().write(payload);
			break;
		default:
			log.error("Channel does not exist: " + channel);
		}
	}
}