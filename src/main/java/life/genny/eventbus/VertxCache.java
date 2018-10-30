package life.genny.eventbus;

import java.util.concurrent.TimeUnit;

import life.genny.channel.DistMap;
import life.genny.qwandautils.GennyCacheInterface;

public class VertxCache implements GennyCacheInterface {

	@Override
	public Object readCache(String key, String token) {
		return DistMap.getDistBE().get(key);
	}

	@Override
	public void writeCache(String key, String value, String token,long ttl_seconds) {
		if (value == null) {
			DistMap.getDistBE().delete(key);
		} else {
			DistMap.getDistBE().put(key, value, ttl_seconds,TimeUnit.SECONDS);
		}

	}

	@Override
	public void clear() {
		DistMap.clear();
		
	}




}
