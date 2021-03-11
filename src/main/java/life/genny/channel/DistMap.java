package life.genny.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.HazelcastInstance;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin;

import life.genny.qwandautils.GennySettings;



public class DistMap {

  private static HazelcastInstance instance;	

  private static  Set<String> realms = new HashSet<String>();
  private static RemoteCacheManager cacheManager ;

  static {
    ConfigurationBuilder builder = new ConfigurationBuilder();
    builder.addServer()
      .host(GennySettings.cacheServerName)
      .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
      .security().authentication()
      //Add user credentials.
      .username(System.getenv("INFINISPAN_USERNAME"))
      .password(System.getenv("INFINISPAN_PASSWORD"))
      .realm("default")
      .saslMechanism("DIGEST-MD5");
    cacheManager = new RemoteCacheManager(builder.build());

  }
  private static Map<String, RemoteCache> caches= new HashMap<>();

  public static RemoteCache<String, String> getMapBaseEntitys(final String realm) {
    if(realms.contains(realm)){
      return caches.get(realm); 
    }else{
      cacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE).getOrCreateCache(realm, DefaultTemplate.DIST_SYNC);
      realms.add(realm);
      caches.put(realm,cacheManager.getCache(realm));
      return caches.get(realm); 
    }
  }


  public static void getRealms() {
    cacheManager.getCacheNames().forEach(System.out::println);
  }


  /**
   * @return the distBE
   */
  public static RemoteCache<String, String> getDistBE(final String realm) {
    return getMapBaseEntitys(realm);
  }



  /**
   * @return the distPontoonBE
   */
  public static RemoteCache<String, String> getDistPontoonBE(final String realm) {
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
