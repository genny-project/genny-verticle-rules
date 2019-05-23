package life.genny.eventbus;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang.StringUtils;
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
		String payloadStr = (String)payload.toString();
		payloadStr = StringUtils.abbreviateMiddle(payloadStr, "...", 80);
		switch (channel) {
		case "event":
		case "events":
			System.out.println(channel.toUpperCase()+":"+payloadStr);
			;
			break;
		case "data":
			System.out.println(channel.toUpperCase()+":"+payloadStr);
			break;

		case "webdata":
			payload = EventBusInterface.privacyFilter(user, payload,filterAttributes);
			payloadStr = (String)payload.toString();
			payloadStr = StringUtils.abbreviateMiddle(payloadStr, "...", 80);

			System.out.println(channel.toUpperCase()+":"+payloadStr);
			break;
		case "cmds":
		case "webcmds":
			payload = EventBusInterface.privacyFilter(user, payload,filterAttributes);
			payloadStr = (String)payload.toString();
			payloadStr = StringUtils.abbreviateMiddle(payloadStr, "...", 80);
			System.out.println(channel.toUpperCase()+":"+payloadStr);
			break;
		case "services":
			System.out.println(channel.toUpperCase()+":"+payloadStr);
			break;
		case "messages":
			System.out.println(channel.toUpperCase()+":"+payloadStr);
			break;
		default:
			log.error("Channel does not exist: " + channel);
		}
	}
}