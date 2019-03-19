package life.genny.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;

import io.vertx.core.json.DecodeException;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import life.genny.qwandautils.GennySettings;


public class SecureResources {
	
	  protected static final Logger log = org.apache.logging.log4j.LogManager
		      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


  /**
   * @return the keycloakJsonMap
   */
  public static Map<String, String> getKeycloakJsonMap() {
	  return keycloakJsonMap;
  }

  public static Map<String, String> keycloakJsonMap = new ConcurrentHashMap<String, String>();

  	public static String fetchRealms() {
  		String ret = "";
  		for (String keycloakRealmKey : keycloakJsonMap.keySet()) {
  			ret += keycloakRealmKey + ":" + keycloakJsonMap.get(keycloakRealmKey) + "\n";
  		}
  		return ret;
  	}

  /**
   * @param keycloakJsonMap the keycloakJsonMap to set
   * @return
   */
  public static Future<Void> setKeycloakJsonMap() {
  	  
    final Future<Void> fut = Future.future();
    Vertx.currentContext().owner().executeBlocking(exec -> {
    	String keycloakJson = "{\n" + 
      	  		"  \"realm\": \"" + System.getenv("PROJECT_REALM") + "\",\n" + 
      	  		"  \"auth-server-url\": \"" + System.getenv("KEYCLOAKURL") + "\",\n" + 
      	  		"  \"ssl-required\": \"none\",\n" + 
      	  		"  \"resource\": \"" + System.getenv("PROJECT_REALM") + "\",\n" + 
      	  		"  \"credentials\": {\n" + 
      	  		"    \"secret\": \"" + System.getenv("KEYCLOAK_SECRET") + "\" \n" + 
      	  		"  },\n" + 
      	  		"  \"policy-enforcer\": {}\n" + 
      	  		"}";
            
      	  keycloakJsonMap.put("keycloak.json", keycloakJson);
      fut.complete();
    }, res -> {
    });
    return fut;
  }


  public static void clear()
  {
	  if (keycloakJsonMap != null) {
		  keycloakJsonMap.clear();
	  }
  }
}
