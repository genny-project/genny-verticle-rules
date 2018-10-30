package life.genny.channel;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import life.genny.cluster.ClusterConfig;

public class DistMap {
   
  private static IMap distBE;
  private static IMap distPontoonBE;  

  /**
   * @return the distBE
   */
  public static IMap getDistBE() {
    return distBE;
  }

  /**
   * @param distBE the distBE to set
   */
  public static void setDistBE(IMap distBE) {
 
    DistMap.distBE = distBE;
  }
  
  /**
   * @return the distPontoonBE
   */
  public static IMap getDistPontoonBE() {
    return distPontoonBE;
  }

  /**
   * @param distBE the distPontoonBE to set
   */
  public static void setDistPontoonBE(IMap distPontoonBE) {
 
    DistMap.distPontoonBE = distPontoonBE;
  }
  
  public static void registerDataStructure(HazelcastInstance haInst, final String realm) {
    setDistBE(haInst.getMap(realm));
    setDistPontoonBE(haInst.getMap("pontoon:"+realm));
    
  }

  public static void clear()
  {
	  DistMap.distBE.clear();
	  DistMap.distPontoonBE.clear();
  }
  
  public static void clearDistBE()
  {
	  DistMap.distBE.clear();
  }
  
  public static void clearDistPontoonBE()
  {
	  DistMap.distPontoonBE.clear();
  }
  
}
