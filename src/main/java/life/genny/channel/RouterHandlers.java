package life.genny.channel;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
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
		   //   System.out.println(boddy.toJsonObject());
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
		          log.info("CACHE KEY:"+param1);
		          String param2 = wifiPayload.getString("json");
		          VertxUtils.writeCachedJson(GennySettings.dynamicRealm(),param1, param2);
		         
		                  JsonObject ret = new JsonObject().put("status", "ok");
		                  context.request().response().headers().set("Content-Type", "application/json");
		                  context.request().response().end(ret.encode());

		        }
		    });

		    
		    

		  }

	 public static void apiMapPutHandlerArray(final RoutingContext context) {
		    
		    
		    //handle the body here and assign it to wifiPayload to process the data 
		    final HttpServerRequest req = context.request().bodyHandler(boddy -> {
		   //   System.out.println(boddy.toJsonObject());
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
		          long start = System.nanoTime();
		          for (BaseEntity be : msg.getItems()) {
		        	  VertxUtils.writeCachedJson(GennySettings.dynamicRealm(),be.getCode(), JsonUtils.toJson(be));
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
		    String param1 = req.getParam("param1");
		    String realm = req.getParam("realm");
		    JsonObject json = null;
		    
		    json = VertxUtils.readCachedJson(realm,param1);
		      if (json.getString("status").equals("error")) {
		        JsonObject err = new JsonObject().put("status", "error");
		        req.response().headers().set("Content-Type", "application/json");
		        req.response().end(err.encode());
		      } else {
		            req.response().headers().set("Content-Type", "application/json");
		            req.response().end(json.encode());
		          }

		  }

	 
		  public static void apiMapGetHandler(final RoutingContext context) {
		    final HttpServerRequest req = context.request();
		    String param1 = req.getParam("param1");

		    JsonObject json = null;
		    
		    if (StringUtils.isBlank(param1)) {
		    	param1 = param1.toUpperCase();
		    json = VertxUtils.readCachedJson(GennySettings.dynamicRealm(),param1);
		      if (json.getString("status").equals("error")) {
		        JsonObject err = new JsonObject().put("status", "error");
		        req.response().headers().set("Content-Type", "application/json");
		        req.response().end(err.encode());
		      } else {
		            req.response().headers().set("Content-Type", "application/json");
		            req.response().end(json.encode());
		          }

		    }
		    req.response().headers().set("Content-Type", "application/json");
		    req.response().end();
		  }

		  public static void apiClearGetHandler(final RoutingContext context) {
			    final HttpServerRequest req = context.request();

			    VertxUtils.clearDDT(GennySettings.dynamicRealm());
				            req.response().headers().set("Content-Type", "application/json");
			            req.response().end();

			  }


}
