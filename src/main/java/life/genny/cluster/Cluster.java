package life.genny.cluster;

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
  private static final Future<Void> fut = Future.future();
  static Action1<? super Vertx> registerAllChannels = vertx -> {
    EventBus eb = vertx.eventBus();
    Consumer.registerAllConsumer(eb);
    Producer.registerAllProducers(eb);
    CurrentVtxCtx.getCurrentCtx().setClusterVtx(vertx);
  
    fut.complete();
  };

  static Action1<Throwable> clusterError = error -> {
    System.out.println("error in the cluster: " + error.getMessage());
  };

  public static Future<Void> joinCluster() {
    Vertx.currentContext().owner().rxClusteredVertx(ClusterConfig.configCluster())
        .subscribe(registerAllChannels, clusterError);
    return fut;
  }

}
