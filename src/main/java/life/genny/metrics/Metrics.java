package life.genny.metrics;

import java.util.Map;
import java.util.Set;
import com.google.gdata.data.dublincore.Format;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
// import io.vertx.rxjava.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.rxjava.core.Vertx;
// import io.vertx.rxjava.ext.dropwizard.MetricsService;
import io.vertx.rxjava.ext.dropwizard.MetricsService;
import io.vertx.rxjava.ext.web.RoutingContext;

public class Metrics {



  public static void main(String... strings) {

    System.out.println("vertx.http.servers.0.0.0.0:8088.open-websockets".replaceAll("[.-]", "_"));
  }

  public static void metrics(final RoutingContext routingContext) {

    StringBuilder stringBuilder = new StringBuilder();

    MetricsService metricsService = MetricsService.create(Vertx.currentContext().owner());
    JsonObject metrics = metricsService.getMetricsSnapshot(Vertx.currentContext().owner());
    System.out.println("msdfkljsdflksdjflsdjflkjdslfjld "+metrics);
    // System.out.println("this is metrics " + metrics);

    // System.out.println(metrics.getJsonObject("handlers"));
    // System.out.println(metrics.getJsonObject("vertx.eventbus.handlers"));

//    Set<String> metricsNames = metricsService.metricsNames();

//    for (String metricName : metricsNames) {
//      JsonObject ob = metrics.getJsonObject(metricName);
//      String type = ob.getString("type");
//      System.out.println(ob.getMap());
//      // System.out.println("Known metrics name " + metricsName);
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
    map.values().stream().forEach(val -> System.out.println(val));
    map.entrySet().stream().forEach(oo -> System.out.println(oo));
    
    String sss = map.keySet().stream().map(entry -> {
      return String.format("%s=\"%s\"", entry, map.get(entry));
    }).reduce((str1, str2) -> {
      return String.format("%s,%s", str1.toString(), str2.toString());
    }).get().toString();
    
    return String.format("%s_%s_%s{%s}\n", domain, metricName, type, sss);
  }

}
