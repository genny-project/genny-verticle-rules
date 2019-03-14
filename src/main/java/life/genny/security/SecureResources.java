package life.genny.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.json.DecodeException;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import life.genny.qwandautils.GennySettings;


public class SecureResources {
	


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
      	  		"  \"realm\": \"genny\",\n" + 
      	  		"  \"auth-server-url\": \"http://keycloak.genny.life:8180/auth\",\n" + 
      	  		"  \"ssl-required\": \"none\",\n" + 
      	  		"  \"resource\": \"genny\",\n" + 
      	  		"  \"credentials\": {\n" + 
      	  		"    \"secret\": \"056b73c1-7078-411d-80ec-87d41c55c3b4\"\n" + 
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
