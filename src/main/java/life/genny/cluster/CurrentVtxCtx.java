package life.genny.cluster;

import io.vertx.rxjava.core.Vertx;

public class CurrentVtxCtx {
  private static CurrentVtxCtx currentVtx;
  
  private  Vertx clusterVtx;
  
  /**
   * @return the bus
   */

  /**
   * @param bus the bus to set
   */

  /**
   * @return the clusterVtx
   */
  public Vertx getClusterVtx() {
    return clusterVtx;
  }

  public static CurrentVtxCtx getCurrentCtx() {
    if(currentVtx == null) {
      currentVtx = new CurrentVtxCtx();
      return currentVtx;
    }
    else {
      return currentVtx; 
    }
  }
  
  /**
   * @param clusterVtx the clusterVtx to set
   */
  public void setClusterVtx(Vertx clusterVtx) {
    this.clusterVtx = clusterVtx;
  } 

  

}
