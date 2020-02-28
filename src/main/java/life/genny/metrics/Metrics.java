package life.genny.metrics;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.google.gdata.data.dublincore.Format;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
// import io.vertx.rxjava.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.rxjava.core.Vertx;
// import io.vertx.rxjava.ext.dropwizard.MetricsService;
import io.vertx.rxjava.ext.dropwizard.MetricsService;
import io.vertx.rxjava.ext.web.RoutingContext;

public class Metrics {

	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


  public static void main(String... strings) {

    log.info("vertx.http.servers.0.0.0.0:8088.open-websockets".replaceAll("[.-]", "_"));
  }

  public static void metrics(final RoutingContext routingContext) {

    StringBuilder stringBuilder = new StringBuilder();

    MetricsService metricsService = MetricsService.create(Vertx.currentContext().owner());
    JsonObject metrics = metricsService.getMetricsSnapshot(Vertx.currentContext().owner());
    log.info("msdfkljsdflksdjflsdjflkjdslfjld "+metrics);
    // log.info("this is metrics " + metrics);

    // log.info(metrics.getJsonObject("handlers"));
    // log.info(metrics.getJsonObject("vertx.eventbus.handlers"));

//    Set<String> metricsNames = metricsService.metricsNames();

//    for (String metricName : metricsNames) {
//      JsonObject ob = metrics.getJsonObject(metricName);
//      String type = ob.getString("type");
//      log.info(ob.getMap());
//      // log.info("Known metrics name " + metricsName);
//      // System.out.printf("%s-%s\n","bridge", metricsName);
//      stringBuilder.append(prometheusFormat("bridge", metricName, type, ob.getMap()));
//
//    }

    routingContext.response().putHeader("Content-Type", "application/json");
    // routingContext.response().setChunked(true);
//    routingContext.response().end(stringBuilder.toString());
    routingContext.response().end(metrics.toString());
  }

  public static String prometheusFormat(String domain, String metricName, String type, Map map) {
    String str = metricName.replaceAll("[.-]", "_");
    map.values().stream().forEach(val -> log.info(val));
    map.entrySet().stream().forEach(oo -> log.info(oo));
    
    String sss = map.keySet().stream().map(entry -> {
      return String.format("%s=\"%s\"", entry, map.get(entry));
    }).reduce((str1, str2) -> {
      return String.format("%s,%s", str1.toString(), str2.toString());
    }).get().toString();
    
    return String.format("%s_%s_%s{%s}\n", domain, metricName, type, sss);
  }

}
