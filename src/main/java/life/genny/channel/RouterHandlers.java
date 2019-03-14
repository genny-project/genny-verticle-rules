package life.genny.channel;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.utils.VertxUtils;



public class RouterHandlers {


	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static CorsHandler cors() {
		return CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.OPTIONS).allowedHeader("X-PINGARUNER").allowedHeader("Content-Type")
				.allowedHeader("X-Requested-With");
	}
	



	 public static void apiMapPutHandler(final RoutingContext context) {
		    
	    
		    //handle the body here and assign it to wifiPayload to process the data 
		    final HttpServerRequest req = context.request().bodyHandler(boddy -> {
		    	String realm = GennySettings.mainrealm;
				String token = context.request().getParam("token");

		   //   log.info(boddy.toJsonObject());
		    	  JsonObject wifiPayload = boddy.toJsonObject();
		      if (wifiPayload == null) {
		    	  context.request().response().headers().set("Content-Type", "application/json");
		          JsonObject err = new JsonObject().put("status", "error");
		          context.request().response().headers().set("Content-Type", "application/json");
		          context.request().response().end(err.encode());
		        } 
		      else {
		          // a JsonObject wraps a map and it exposes type-aware getters
		          String param1 = wifiPayload.getString("key");
					if (token == null) {
						MultiMap headerMap = context.request().headers();
						token = headerMap.get("Authorization");
						if (token == null) {
							log.error("NULL TOKEN!");
						} else {
							token = token.substring(7); // To remove initial [Bearer ]
						}
					} 
					
				
					try {
						realm = KeycloakUtils.getDecodedToken(token).getString("azp");
					} catch (JSONException  | NullPointerException e) {  // TODO, should always be a token ....
						realm = GennySettings.mainrealm;
						log.error("token not decoded:["+token+"] using realm "+realm+" and key "+param1);
						
					}

					// TODO hack
					if ("genny".equalsIgnoreCase(realm)) {
						realm = GennySettings.mainrealm;
					}
	         
		          String param2 = wifiPayload.getString("json");
		          VertxUtils.writeCachedJson(realm,param1, param2);
		         
		                  JsonObject ret = new JsonObject().put("status", "ok");
		                  context.request().response().headers().set("Content-Type", "application/json");
		                  context.request().response().end(ret.encode());

		        }
		    });

		    
		    

		  }

	 public static void apiMapPutHandlerArray(final RoutingContext context) {
		    
		    
		    //handle the body here and assign it to wifiPayload to process the data 
		    final HttpServerRequest req = context.request().bodyHandler(boddy -> {
		    	String realm = GennySettings.mainrealm;
				String token = context.request().getParam("token");

		   //   log.info(boddy.toJsonObject());
		    	  JsonObject wifiPayload = boddy.toJsonObject();
		      if (wifiPayload == null) {
		    	  context.request().response().headers().set("Content-Type", "application/json");
		          JsonObject err = new JsonObject().put("status", "error");
		          context.request().response().headers().set("Content-Type", "application/json");
		          context.request().response().end(err.encode());
		        } 
		      else {
		          // a JsonObject wraps a map and it exposes type-aware getters
		          String param2 = wifiPayload.getString("json");
		          QDataBaseEntityMessage msg = JsonUtils.fromJson(param2,QDataBaseEntityMessage.class);
		          log.info("Writing a batch of "+msg.getItems().length+" to cache");
					if (token == null) {
						MultiMap headerMap = context.request().headers();
						token = headerMap.get("Authorization");
						if (token == null) {
							log.error("NULL TOKEN!");
						} else {
							token = token.substring(7); // To remove initial [Bearer ]
						}
					} 
					try {
						realm = KeycloakUtils.getDecodedToken(token).getString("azp");
					} catch (JSONException  | NullPointerException e) {  // TODO, should always be a token ....
						realm = GennySettings.mainrealm;
						log.error("token not decoded:["+token+"] using realm "+realm);
						
					}

					// TODO hack
					if ("genny".equalsIgnoreCase(realm)) {
						realm = GennySettings.mainrealm;
					}
	
					
		          long start = System.nanoTime();
		          for (BaseEntity be : msg.getItems()) {
		        	  VertxUtils.writeCachedJson(realm,be.getCode(), JsonUtils.toJson(be));
		          }
		          long end = System.nanoTime();
		          double dif = (end - start)/1e6;
		          log.info("Finished writing to cache in "+dif+"ms");
		                  JsonObject ret = new JsonObject().put("status", "ok");
		                  context.request().response().headers().set("Content-Type", "application/json");
		                  context.request().response().end(ret.encode());

		        }
		    });

		    
		    

}
	
	  public static void apiMapGetHandlerRealm(final RoutingContext context) {
		    final HttpServerRequest req = context.request();
		    String param1 = req.getParam("param1").toUpperCase();
		    String realm = req.getParam("realm");
		    JsonObject json = null;
		    
		    json = VertxUtils.readCachedJson(realm,param1);
		      if (json.getString("status").equals("error")) {
		        JsonObject err = new JsonObject().put("status", "error");
		        req.response().headers().set("Content-Type", "application/json");
		        req.response().end(err.encode());
		      } else {
		    	    JsonObject valueJson = new JsonObject(json.getString("value"));
		    	    json.put("value", valueJson);

		            req.response().headers().set("Content-Type", "application/json");
		            req.response().end(json.encode());
		          }

		  }

	 
		  public static void apiMapGetHandler(final RoutingContext context) {
		    final HttpServerRequest req = context.request();
		    String param1 = req.getParam("param1");
	    	String realm = GennySettings.mainrealm;
			String token = context.request().getParam("token");


		    JsonObject json = null;
		    
		    if (!StringUtils.isBlank(param1)) {
		    	param1 = param1.toUpperCase();
				if (token == null) {
					MultiMap headerMap = context.request().headers();
					token = headerMap.get("Authorization");
					if (token == null) {
						log.error("NULL TOKEN!");
					} else {
						token = token.substring(7); // To remove initial [Bearer ]
					}
					
				} 
				try {
					 log.info("Cache read token = "+token);
					realm = KeycloakUtils.getDecodedToken(token).getString("azp");
				} catch (JSONException  | NullPointerException e) {  // TODO, should always be a token ....
					realm = GennySettings.mainrealm;
					log.error("token not decoded:["+token+"] using realm "+realm+" and parm "+param1);
					
				}

				// TODO hack
				if ("genny".equalsIgnoreCase(realm)) {
					realm = GennySettings.mainrealm;
				}
				
		    json = VertxUtils.readCachedJson(realm,param1);
		      if (json.getString("status").equals("error")) {
		        JsonObject err = new JsonObject().put("status", "error");
		        req.response().headers().set("Content-Type", "application/json");
		        req.response().end(err.encode());
		      } else {
		    	    JsonObject valueJson = new JsonObject(json.getString("value"));
		    	    json.put("value", valueJson);
		            req.response().headers().set("Content-Type", "application/json");
		            req.response().end(json.encode());
		          }

		    } else {
		    	req.response().headers().set("Content-Type", "application/json");
		    	req.response().end();
		    }
		  }

		  public static void apiClearGetHandler(final RoutingContext context) {
			    final HttpServerRequest req = context.request();

			    VertxUtils.clearDDT(GennySettings.dynamicRealm());
				            req.response().headers().set("Content-Type", "application/json");
			            req.response().end();

			  }


}
