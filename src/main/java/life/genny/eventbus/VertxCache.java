package life.genny.eventbus;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import life.genny.channel.DistMap;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.KeycloakUtils;
import org.json.JSONObject;

public class VertxCache implements GennyCacheInterface {
	
	private static final Logger log = LoggerFactory
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


	@Override

	public Object readCache(String realm, final String key, final String token) {
	//	log.info("VertxCache read:"+realm+":"+key);
		return DistMap.getMapBaseEntitys(realm).get(key);
	}

	@Override
	public void writeCache(String realm, final String key, final String value, final String token,long ttl_seconds) {

		
		if (key == null) {
			throw new IllegalArgumentException("Key is null");
		}
		if (value == null) {
			DistMap.getMapBaseEntitys(realm).remove(key);
		} else {
			if (key == null) {
				log.error("Null Key provided! with value=["+value+"]");
				
			} else {
				DistMap.getMapBaseEntitys(realm).put(key, value);
			}
		}

	}

	@Override
	public void clear(String realm) {

		DistMap.clear(realm);

		
	}




}
