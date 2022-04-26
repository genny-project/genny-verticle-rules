package life.genny.eventbus;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import life.genny.models.GennyToken;
import life.genny.qwandautils.GennyCacheInterface;

public class MockCache implements GennyCacheInterface {
	
	private static final Logger log = LoggerFactory
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private static Map<String,Map<String,String>> realmCacheMap = new ConcurrentHashMap<String,Map<String,String>>();

	@Override

	public Object readCache(String realm, final String key, final GennyToken token) {
	//	log.info("MockCache read:"+realm+":"+key);

		if (!realmCacheMap.containsKey(realm)) {
			realmCacheMap.put(realm, new ConcurrentHashMap<String,String>());
		}
		return realmCacheMap.get(realm).get(key);
	}

	@Override
	public void writeCache(String realm, final String key, final String value, final GennyToken token,long ttl_seconds) {

		if (!realmCacheMap.containsKey(realm)) {
			realmCacheMap.put(realm, new ConcurrentHashMap<String,String>());
		}

		if (key == null) {
			throw new IllegalArgumentException("Key is null. Provided with value:[" + value + "]");
		}
		if (value == null) {
			realmCacheMap.get(realm).remove(key);
		} else {
			realmCacheMap.get(realm).put(key, value); // ignore expiry
		}
	}

	@Override
	public void clear(String realm) {

		realmCacheMap.remove(realm);
		realmCacheMap.put(realm, new ConcurrentHashMap<String,String>());

		
	}




}
