package life.genny.channel;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.security.TokenIntrospection;
import life.genny.utils.VertxUtils;

public class RouterHandlers {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static CorsHandler cors() {
		return CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.OPTIONS).allowedHeader("X-PINGARUNER").allowedHeader("Content-Type")
				.allowedHeader("X-Requested-With");
	}
	
	public static Vertx avertx;

	private static final List<String> roles;
	static {
		roles = TokenIntrospection.setRoles("dev", "test", "service");
	}

	public static void apiMapPutHandler(final RoutingContext context) {

		// handle the body here and assign it to payload to process the data
		final HttpServerRequest req = context.request().bodyHandler(boddy -> {
			String token = null;
			JsonObject payload = boddy.toJsonObject();

			if (payload != null) {
				token = payload.getJsonObject("headers").getString("Authorization").split("Bearer ")[1];
			}

			if (token != null && TokenIntrospection.checkAuthForRoles(avertx,roles, token)) { // do not allow empty tokens


				JSONObject tokenJSON = KeycloakUtils.getDecodedToken(token);
				String realm = tokenJSON.getString("aud");

				// for testig and debugging, if a user has a role test then put the token into a
				// cache entry so that the test can access it
//				JSONObject realm_access = tokenJSON.getJSONObject("realm_access");
//				JSONArray roles = realm_access.getJSONArray("roles");
//				List<Object> roleList = roles.toList();
//
//				if ((roleList.contains("test")) || (roleList.contains("dev"))) {

					try {
						// a JsonObject wraps a map and it exposes type-aware getters
						String key = payload.getString("key");
						String value = payload.getString("json");
						Long expirySecs = Long.decode(payload.getString("json"));
						VertxUtils.writeCachedJson(realm, key, value, token, expirySecs);

						JsonObject ret = new JsonObject().put("status", "ok");
						context.request().response().headers().set("Content-Type", "application/json");
						context.request().response().end(ret.encode());

					} catch (Exception e) {
						JsonObject err = new JsonObject().put("status", "error");
						context.request().response().headers().set("Content-Type", "application/json");
						context.request().response().end(err.encode());

					}
				} else {
					log.warn("TOKEN NOT ACCEPTED");
				}
//			}
		});

	}

	public static void apiMapPutHandlerArray(final RoutingContext context) {
		    
		    
//		    //handle the body here and assign it to wifiPayload to process the data 
//		    final HttpServerRequest req = context.request().bodyHandler(boddy -> {
//		    	String realm = GennySettings.mainrealm;
//
//		   //   log.info(boddy.toJsonObject());
//		    	  JsonObject payload = boddy.toJsonObject();
//		    	  
//					String token = payload.getJsonObject("headers").getString("Authorization").split("Bearer ")[1];
//
//					if (token != null && TokenIntrospection.checkAuthForRoles(roles, token)) { // do not allow empty tokens
//
//						log.info("Roles from this token are allow and authenticated "
//								+ TokenIntrospection.checkAuthForRoles(roles, token));
//						
//
//						JSONObject tokenJSON = KeycloakUtils.getDecodedToken(token);
//						String sessionState = tokenJSON.getString("session_state");
//						String realm = tokenJSON.getString("aud");
//						String uname = QwandaUtils.getNormalisedUsername(tokenJSON.getString("preferred_username"));
//						String userCode = "PER_" + uname.toUpperCase();
//
//						// for testig and debugging, if a user has a role test then put the token into a cache entry so that the test can access it
//						JSONObject realm_access = tokenJSON.getJSONObject("realm_access");
//						JSONArray roles = realm_access.getJSONArray("roles");
//						List<Object> roleList = roles.toList();
//						
//						if (roleList.contains("test")) {
//		    	  
//		    	  
//		    	  
//		    	  
//		    	  
//		      if (wifiPayload == null) {
//		    	  context.request().response().headers().set("Content-Type", "application/json");
//		          JsonObject err = new JsonObject().put("status", "error");
//		          context.request().response().headers().set("Content-Type", "application/json");
//		          context.request().response().end(err.encode());
//		        } 
//		      else {
//		          // a JsonObject wraps a map and it exposes type-aware getters
//		          String param2 = wifiPayload.getString("json");
//		          QDataBaseEntityMessage msg = JsonUtils.fromJson(param2,QDataBaseEntityMessage.class);
//		          log.info("Writing a batch of "+msg.getItems().length+" to cache");
//					if (token == null) {
//						MultiMap headerMap = context.request().headers();
//						token = headerMap.get("Authorization");
//						if (token == null) {
//							log.error("NULL TOKEN!");
//						} else {
//							token = token.substring(7); // To remove initial [Bearer ]
//						}
//					} 
//					try {
//						realm = KeycloakUtils.getDecodedToken(token).getString("azp");
//					} catch (JSONException  | NullPointerException e) {  // TODO, should always be a token ....
//						realm = GennySettings.mainrealm;
//						log.error("token not decoded:["+token+"] using realm "+realm);
//						
//					}
//
//					// TODO hack
//					if ("genny".equalsIgnoreCase(realm)) {
//						realm = GennySettings.mainrealm;
//					}
//	
//					
//		          long start = System.nanoTime();
//		          for (BaseEntity be : msg.getItems()) {
//		        	  VertxUtils.writeCachedJson(realm,be.getCode(), JsonUtils.toJson(be));
//		          }
//		          long end = System.nanoTime();
//		          double dif = (end - start)/1e6;
//		          log.info("Finished writing to cache in "+dif+"ms");
//		                  JsonObject ret = new JsonObject().put("status", "ok");
//		                  context.request().response().headers().set("Content-Type", "application/json");
//		                  context.request().response().end(ret.encode());
//
//		        }
//		    });
//
//		    
		    

}

	public static void apiMapGetHandlerRealm(final RoutingContext context) {
		apiMapGetHandler(context);
	}

	public static void apiMapGetHandler(final RoutingContext context) {
		final HttpServerRequest req = context.request();
		String key = req.getParam("key");
		String realm = req.getParam("realm");
		String token = context.request().getParam("token");

		if (token == null) {
			MultiMap headerMap = context.request().headers();
			token = headerMap.get("Authorization");
			if (token == null) {
				log.error("NULL TOKEN!");
			} else {
				token = token.substring(7); // To remove initial [Bearer ]
			}

		}

		if (token != null && TokenIntrospection.checkAuthForRoles(avertx,roles, token)) { // do not allow empty tokens

			log.info("Roles from this token are allow and authenticated "
					+ TokenIntrospection.checkAuthForRoles(avertx,roles, token));

			JSONObject tokenJSON = KeycloakUtils.getDecodedToken(token);
			if (realm == null) {
				realm = tokenJSON.getString("aud");
			}

			// for testig and debugging, if a user has a role test then put the token into a
			// cache entry so that the test can access it
	////		JSONObject realm_access = tokenJSON.getJSONObject("realm_access");
	//		JSONArray roles = realm_access.getJSONArray("roles");
	//		List<Object> roleList = roles.toList();

	//		if ((roleList.contains("test")) || (roleList.contains("dev"))) {

				try {
					// a JsonObject wraps a map and it exposes type-aware getters
					JsonObject value = VertxUtils.readCachedJson(realm, key, token);
					context.request().response().headers().set("Content-Type", "application/json");
					context.request().response().end(value.encode());

				} catch (Exception e) {
					JsonObject err = new JsonObject().put("status", "error");
					context.request().response().headers().set("Content-Type", "application/json");
					context.request().response().end(err.encode());

				}
			}
	//	}

	}

	public static void apiClearGetHandler(final RoutingContext context) {
		final HttpServerRequest req = context.request();

		VertxUtils.clearDDT(GennySettings.dynamicRealm());
		req.response().headers().set("Content-Type", "application/json");
		req.response().end();

	}

}
