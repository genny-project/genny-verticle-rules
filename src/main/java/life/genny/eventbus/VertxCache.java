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
	
	private static final Logger logger = LoggerFactory
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


	@Override
	public Object readCache(String key, String token) {
//		JSONObject decodedTokenJson = KeycloakUtils.getDecodedToken(token);
//		String realm = decodedTokenJson.getString("azp");

		return DistMap.getDistBE().get(key);
	}

	@Override
	public void writeCache(String key, String value, String token,long ttl_seconds) {
	//	JSONObject decodedTokenJson = KeycloakUtils.getDecodedToken(token);
	//	String realm = decodedTokenJson.getString("azp");

		if (key == null) {
			throw new IllegalArgumentException("Key is null");
		}
		if (value == null) {
			DistMap.getDistBE().delete(key);
		} else {
			if (key == null) {
				logger.error("Null Key provided! with value=["+value+"]");
				
			} else {
				if (value.contains("SBE_NEW_ITEMS")) {
					//logger.info("Write Cache value=["+value+"]");
				}
				DistMap.getDistBE().put(key, value, ttl_seconds,TimeUnit.SECONDS);
			}
		}

	}

	@Override
	public void clear() {

		DistMap.clear();
		
	}




}
