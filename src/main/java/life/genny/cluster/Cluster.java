package life.genny.cluster;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;

import com.hazelcast.core.ISemaphore;

import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;


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
  static Action1<? super Vertx> registerAllChannels = vertx -> {
    EventBus eb = vertx.eventBus();
    Consumer.registerAllConsumer(eb);
    Producer.registerAllProducers(eb);
    CurrentVtxCtx.getCurrentCtx().setClusterVtx(vertx);
  
    fut.complete();
  };

  static Action1<Throwable> clusterError = error -> {
    log.error("error in the cluster: " + error.getMessage());
  };

  public static Future<Void> joinCluster() {
    Vertx.currentContext().owner().rxClusteredVertx(ClusterConfig.configCluster())
        .subscribe(registerAllChannels, clusterError);
    return fut;
  }

}
