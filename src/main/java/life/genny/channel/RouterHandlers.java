package life.genny.channel;

import java.lang.invoke.MethodHandles;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import life.genny.models.GennyToken;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.security.TokenIntrospection;
import life.genny.utils.VertxUtils;

public class RouterHandlers {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static CorsHandler cors() {
		return CorsHandler.create(
        "http://localhost:\\d\\d|"+
        "https://localhost:\\d\\d|"+
        "http://localhost:\\d\\d\\d\\d|"+
        "https://localhost:\\d\\d\\d\\d|"+
        "https://.*.genny.life|https://.*.gada.io|"+
        GennySettings.projectUrl).allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.PUT)
				.allowedMethod(HttpMethod.OPTIONS).allowedHeader("X-PINGARUNER").allowedHeader("Content-Type").allowedHeader("Authorization")
				.allowedHeader("Accept")
				.allowedHeader("X-Requested-With");
	}

	public static Vertx avertx;

	private static final List<String> roles;
	static {
		roles = TokenIntrospection.setRoles("dev", "test", "service");
	}

	public static void apiMapPutHandler(final RoutingContext context) {
		String paramToken = context.request().getParam("token");

		if (paramToken == null) {
			MultiMap headerMap = context.request().headers();
			paramToken = headerMap.get("Authorization");
			if (paramToken == null) {
				log.error("NULL TOKEN!");
			} else {
				paramToken = paramToken.substring(7); // To remove initial [Bearer ]
			}

		}

		if (paramToken != null /* && TokenIntrospection.checkAuthForRoles(avertx,roles, paramToken)*/ ) { // do not allow
																										// empty tokens

			GennyToken userToken = new GennyToken(paramToken);

			// handle the body here and assign it to payload to process the data
			final HttpServerRequest req = context.request().bodyHandler(boddy -> {
				JsonObject payload = boddy.toJsonObject();

				if (userToken.hasRole("test") || userToken.hasRole("dev")) {

					try {
						// a JsonObject wraps a map and it exposes type-aware getters
						String key = payload.getString("key");
						String value = payload.getString("json");
						Long expirySecs = Long.decode(payload.getString("ttl"));
						VertxUtils.writeCachedJson(userToken.getRealm(), key, value, userToken, expirySecs);

						JsonObject ret = new JsonObject().put("status", "ok");
						context.request().response().headers().set("Content-Type", "application/json");
						context.request().response().end(ret.encode());

					} catch (Exception e) {
						JsonObject err = new JsonObject().put("status", "error");
						context.request().response().headers().set("Content-Type", "application/json");
						context.request().response().end(err.encode());

					}

				}
			});
		}
	}

	public static void apiMapPutHandlerArray(final RoutingContext context) {



	}

	public static void apiMapGetHandlerRealm(final RoutingContext context) {
		apiMapGetHandler(context);
	}

	public static void apiMapGetHandler(final RoutingContext context) {
		final HttpServerRequest req = context.request();
		String key = req.getParam("key");
		String realm = req.getParam("realm");
		String tokenStr = context.request().getParam("token");

		if (tokenStr == null) {
			MultiMap headerMap = context.request().headers();
			tokenStr = headerMap.get("Authorization");
			if (tokenStr == null) {
				log.error("NULL TOKEN!");
			} else {
				tokenStr = tokenStr.substring(7); // To remove initial [Bearer ]
			}
		}

		GennyToken token = new GennyToken(tokenStr);
		if (token != null /* && TokenIntrospection.checkAuthForRoles(avertx,roles, token)*/ ) { // do not allow empty
																								// tokens
			if ("DUMMY".equals(token.getToken())) {
				realm = "jenny"; // force
			} else {
				JSONObject tokenJSON = KeycloakUtils.getDecodedToken(token);
				if (realm == null) {
					realm = tokenJSON.getString("aud");
				}
			}

			// for testig and debugging, if a user has a role test then put the token into a
			// cache entry so that the test can access it
			//// JSONObject realm_access = tokenJSON.getJSONObject("realm_access");
			// JSONArray roles = realm_access.getJSONArray("roles");
			// List<Object> roleList = roles.toList();

			// if ((roleList.contains("test")) || (roleList.contains("dev"))) {

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
		} else {
			log.warn("TOKEN NOT GOOD");
		}
		// }

	}
	


	public static void apiClearGetHandler(final RoutingContext context) {
		final HttpServerRequest req = context.request();
		String token = context.request().getParam("token");
		if (token != null /*&& TokenIntrospection.checkAuthForRoles(avertx,roles, token)*/ ) { // do not allow empty
			// tokens
			GennyToken gt = new GennyToken(token);
			VertxUtils.clearDDT(gt.getRealm());
		}
		req.response().headers().set("Content-Type", "application/json");
		req.response().end();

	}

}
