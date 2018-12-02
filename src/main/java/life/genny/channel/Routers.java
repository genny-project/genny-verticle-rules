package life.genny.channel;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import life.genny.qwandautils.GennySettings;

public class Routers {


	static private Router router = null;

	public static Router routers(final Vertx vertx) {
		if (router == null) {
			router = Router.router(vertx);
		}

		router.route().handler(RouterHandlers.cors());
		router.route(HttpMethod.POST, "/write").handler(RouterHandlers::apiMapPutHandler);
		router.route(HttpMethod.POST, "/writearray").handler(RouterHandlers::apiMapPutHandlerArray);
		router.route(HttpMethod.GET, "/read/:param1").handler(RouterHandlers::apiMapGetHandler);
		router.route(HttpMethod.GET, "/version").handler(VersionHandler::apiGetVersionHandler);
		router.route(HttpMethod.GET, "/clear").handler(RouterHandlers::apiClearGetHandler);
		return router;
	}
	
	public static Router getRouter(final Vertx vertx)
	{
		if (router == null) {
			routers(vertx);
		}
		return router;
	}

	public static void activate(final Vertx vertx) {
		System.out.println("Activating cache Routes on port "+GennySettings.CACHE_API_PORT+" given ["+GennySettings.CACHE_API_PORT+"]");
		HttpServerOptions serverOptions = new HttpServerOptions();
		  serverOptions.setUsePooledBuffers(true);
		  serverOptions.setCompressionSupported(true);
		  serverOptions.setCompressionLevel(3);
		  serverOptions.setUseAlpn(true);
		vertx.createHttpServer(/* serverOptions*/).requestHandler(router::accept).listen(Integer.parseInt(GennySettings.CACHE_API_PORT));
	}

}
