package life.genny.cluster;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
// import life.genny.channels.EBCHandlers;
// import life.genny.channels.EBConsumers;
// import life.genny.channels.EBProducers;
// import life.genny.utils.VertxUtils;
import rx.functions.Action1;
import life.genny.channel.Consumer;
import life.genny.channel.Producer;;

public class Cluster {
	  protected static final Logger log = org.apache.logging.log4j.LogManager
		      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  private static final Future<Void> fut = Future.future();

//  static Action1<? super Vertx> registerAllChannels = vertx -> {
//    EventBus eb = vertx.eventBus();
//    Consumer.registerAllConsumer(eb);
//    Producer.registerAllProducers(eb);
//    CurrentVtxCtx.getCurrentCtx().setClusterVtx(vertx);
//  
//    fut.complete();
//  };

  static Action1<Throwable> clusterError = error -> {
    log.error("error in the cluster: " + error.getMessage());
  };

  public static Future<Void> joinCluster() {

    Vertx.clusteredVertx(ClusterConfig.configCluster(), vertx -> {
      initializeResource(vertx.result());
    });
    return fut;
  }

  public static void initializeResource(Vertx vertx) {
    EventBus eb = vertx.eventBus();
    Consumer.registerAllConsumer(eb);
    Producer.registerAllProducers(eb);
    CurrentVtxCtx.getCurrentCtx().setClusterVtx(vertx);
    fut.complete();
    
  }
}
