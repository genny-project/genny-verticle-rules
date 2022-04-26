package life.genny.eventbus;

import java.lang.invoke.MethodHandles;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import life.genny.channel.DistMap;
import life.genny.models.GennyToken;
import life.genny.qwandautils.GennyCacheInterface;

public class VertxCache implements GennyCacheInterface {
	
	private static final Logger log = LoggerFactory
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


	@Override

	public Object readCache(String realm, final String key, final GennyToken token) {
	//	log.info("VertxCache read:"+realm+":"+key);
		return DistMap.getMapBaseEntitys(realm).get(key);
	}

	@Override
	public void writeCache(String realm, final String key, final String value, final GennyToken token,long ttl_seconds) {

		
		if (key == null) {
			throw new IllegalArgumentException("Key is null. Provided with value:[" + value + "]");
		}
		if (value == null) {
			DistMap.getMapBaseEntitys(realm).remove(key);
		} else {
			DistMap.getMapBaseEntitys(realm).put(key, value);
		}

	}

	@Override
	public void clear(String realm) {

		DistMap.clear(realm);

		
	}
}
