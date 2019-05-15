package life.genny.eventbus;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;

import io.vertx.rxjava.core.eventbus.EventBus;
import life.genny.qwanda.entity.BaseEntity;

public class EventBusMock  implements EventBusInterface
{	
	private static final Logger log = org.apache.logging.log4j.LogManager
		.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	
	public EventBusMock ()
	{
		
	}
	
	public EventBusMock(EventBus eventBus)
	{
		
	}

	@Override
	public void publish(BaseEntity user, String channel, Object payload, final String[] filterAttributes) {
		// Actually Send ....
		switch (channel) {
		case "event":
		case "events":
			log.info(channel.toUpperCase()+":"+payload);
			;
			break;
		case "data":
			log.info(channel.toUpperCase()+":"+payload);
			break;

		case "webdata":
			payload = EventBusInterface.privacyFilter(user, payload,filterAttributes);
			log.info(channel.toUpperCase()+":"+payload);
			break;
		case "cmds":
		case "webcmds":
			payload = EventBusInterface.privacyFilter(user, payload,filterAttributes);
			log.info(channel.toUpperCase()+":"+payload);
			break;
		case "services":
			log.info(channel.toUpperCase()+":"+payload);
			break;
		case "messages":
			log.info(channel.toUpperCase()+":"+payload);
			break;
		default:
			log.error("Channel does not exist: " + channel);
		}
	}
}