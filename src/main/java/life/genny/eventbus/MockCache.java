package life.genny.eventbus;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import life.genny.channel.DistMap;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.KeycloakUtils;
import org.json.JSONObject;

public class MockCache implements GennyCacheInterface {
	
	private static final Logger logger = LoggerFactory
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private static Map<String,Map<String,String>> realmCacheMap = new ConcurrentHashMap<String,Map<String,String>>();

	@Override

	public Object readCache(String realm, final String key, final String token) {

		if (!realmCacheMap.containsKey(realm)) {
			realmCacheMap.put(realm, new ConcurrentHashMap<String,String>());
		}
		return realmCacheMap.get(realm).get(key);
	}

	@Override
	public void writeCache(String realm, final String key, final String value, final String token,long ttl_seconds) {

		if (!realmCacheMap.containsKey(realm)) {
			realmCacheMap.put(realm, new ConcurrentHashMap<String,String>());
		}

		if (key == null) {
			throw new IllegalArgumentException("Key is null");
		}
		if (value == null) {
			realmCacheMap.get(realm).remove(key);
		} else {
			if (key == null) {
				logger.error("Null Key provided! with value=["+value+"]");
				
			} else {
				realmCacheMap.get(realm).put(key, value); // ignore expiry
			}
		}

	}

	@Override
	public void clear(String realm) {

		realmCacheMap.remove(realm);
		realmCacheMap.put(realm, new ConcurrentHashMap<String,String>());

		
	}




}
