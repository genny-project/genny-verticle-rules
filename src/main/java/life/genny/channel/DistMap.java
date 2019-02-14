package life.genny.channel;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import life.genny.cluster.ClusterConfig;

public class DistMap {
   
  private static HazelcastInstance instance;	
  

  public static IMap getMapBaseEntitys(final String realm) {
	    return  instance.getMap(realm);
	  }

	
 

  /**
   * @return the distBE
   */
  public static IMap getDistBE(final String realm) {
    return getMapBaseEntitys(realm);
  }

 
  
  /**
   * @return the distPontoonBE
   */
  public static IMap getDistPontoonBE(final String realm) {
	  return getMapBaseEntitys("PONTOON:"+realm);
  }

 
  
  public static void registerDataStructure(HazelcastInstance haInst) {
	instance = haInst;
     
  }

  public static void clear(final String realm)
  {
	  clearDistBE(realm);
	  clearDistPontoonBE(realm);
  }
  
  public static void clearDistBE(final String realm)
  {
	  DistMap.getMapBaseEntitys(realm).clear();
  }
  
  public static void clearDistPontoonBE(final String realm)
  {
	  DistMap.getMapBaseEntitys("PONTOON:"+realm).clear();
  }
  
}
