package life.genny.cluster;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.SemaphoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
// import life.genny.security.Hazel;
import life.genny.channel.DistMap;
import life.genny.qwandautils.GennySettings;

/**
 * @author Adam Crow
 * @author Byron Aguirre
 */
public class ClusterConfig {

	private static final Logger log = LoggerFactory.getLogger(ClusterConfig.class);

	private static final int portEBCluster = 15701;

	/**
	 * @param toClientOutbount
	 *            the toClientOutbount to set
	 */
	public static EventBusOptions configEBCluster() {
		final EventBusOptions eb = new EventBusOptions();
		eb.setClustered(true);
		eb.setIdleTimeout(0);
		eb.setReconnectAttempts(20);
		eb.setReconnectInterval(5);

		if (GennySettings.devMode) {
			System.out.println("IN DEV MODE on "+GennySettings.defaultLocalIP);
			eb.setClusterPublicHost(GennySettings.defaultLocalIP);
			eb.setHost(GennySettings.defaultLocalIP);

		} else {
			System.out.println("NOT IN DEV MODE , MYIP=[" + GennySettings.myIP + "]");
			if (!GennySettings.defaultLocalIP.equalsIgnoreCase(GennySettings.myIP)) {
				System.out.println("Production Mode");
				if (GennySettings.hostIP != null) {
					eb.setPort(portEBCluster).setHost(GennySettings.myIP);
				}
			} else {
				System.out.println("Local Docker Mode");
				eb.setPort(portEBCluster);
			}
		}
		return eb;
	}

	/**
	 * @param toClientOutbount
	 *            the toClientOutbount to set
	 */
	public static VertxOptions configCluster() {
		HazelcastInstance haInst = hazelcastInstance();
		if (GennySettings.isDdtHost) {
			DistMap.registerDataStructure(haInst, GennySettings.mainrealm); // TODO, get all realms
		}
		final VertxOptions options = new VertxOptions();
		final ClusterManager mgr = new HazelcastClusterManager(haInst);
		options.setClusterManager(mgr);
		options.setEventBusOptions(configEBCluster());
		options.setClustered(true);
		options.setClusterManager(mgr);

		if (GennySettings.devMode) {
			options.setMaxEventLoopExecuteTime(1000000000L * 600); // TODO, this is really for debugging
			options.setBlockedThreadCheckInterval(1000000000L * 600);
			options.setMaxWorkerExecuteTime(1000000000L * 600);
		} else {
			options.setBlockedThreadCheckInterval(1000);
			options.setMaxEventLoopExecuteTime(2000000000L);
			options.setMaxWorkerExecuteTime(60000000000L);
		}

		options.setEventLoopPoolSize(16);
		options.setInternalBlockingPoolSize(20);

		options.setWorkerPoolSize(20);
		// options.setQuorumSize(1);

		return options;
	}

	public static Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("hazelcast.shutdownhook.enabled", "false");
		return properties;
	}

	public static MultiMapConfig getMultiMapCfg() {
		MultiMapConfig multiMapCfg = new MultiMapConfig();
		multiMapCfg.setName("__vertx.subs");
		multiMapCfg.setBackupCount(1);
		return multiMapCfg;
	}

	public static MapConfig getMapCfg() {
		MapConfig mapCfg = new MapConfig();
		mapCfg.setName("__vertx.haInfo");
		mapCfg.setTimeToLiveSeconds(0);
		mapCfg.setMaxIdleSeconds(0);
		mapCfg.setEvictionPolicy(EvictionPolicy.NONE);
		MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
		maxSizeConfig.setMaxSizePolicy(MaxSizePolicy.PER_NODE);
		maxSizeConfig.setSize(0);
		mapCfg.setMaxSizeConfig(maxSizeConfig);
		mapCfg.setEvictionPercentage(25);
		mapCfg.setMergePolicy("com.hazelcast.map.merge.LatestUpdateMapMergePolicy");
		return mapCfg;
	}

	public static SemaphoreConfig getSemaphoreConfig() {
		SemaphoreConfig semaphoreConfig = new SemaphoreConfig();
		semaphoreConfig.setName("__vertx.*");
		semaphoreConfig.setInitialPermits(1);
		return semaphoreConfig;
	}

	public static SemaphoreConfig getSemaphoreConfigStartupManager() {
		SemaphoreConfig semaphoreConfig = new SemaphoreConfig();
		semaphoreConfig.setName("startupManager");
		semaphoreConfig.setInitialPermits(1);
		return semaphoreConfig;
	}

	public static HazelcastInstance hazelcastInstance() {
		Config cfg = new Config();
		cfg.getGroupConfig().setName(GennySettings.username);
		cfg.getGroupConfig().setPassword(GennySettings.username);

		cfg.setProperties(getProperties());
		cfg.addMultiMapConfig(getMultiMapCfg());
		cfg.addMapConfig(getMapCfg());
		cfg.addSemaphoreConfig(getSemaphoreConfig());
		cfg.addSemaphoreConfig(getSemaphoreConfigStartupManager());

		return Hazelcast.newHazelcastInstance(cfg);
	}

	// public static HazelcastInstance hazelcastInstance() {
	// Config config = new Config();
	// if (memberDev == null) {
	// GroupConfig groupCfg = new GroupConfig();
	// groupCfg.setName(memberDev);
	// config.setGroupConfig(groupCfg);
	// }
	// ReplicatedMapConfig repCfg = new ReplicatedMapConfig();
	// Map<String, ReplicatedMapConfig> mapRepCfg = new HashMap<String,
	// ReplicatedMapConfig>();
	// config.setReplicatedMapConfigs(mapRepCfg);
	// ReliableTopicConfig topic = new ReliableTopicConfig();
	// config.addReliableTopicConfig(topic);
	// QuorumListenerConfig listenerConfig = new QuorumListenerConfig();
	// listenerConfig.setImplementation(quorumEvent -> {
	// });
	// QuorumConfig quorumConfig = new QuorumConfig();
	// quorumConfig.setName("quorumRuleWithThreeMembers");
	// quorumConfig.setEnabled(true);
	// quorumConfig.setSize(2);
	// quorumConfig.addListenerConfig(listenerConfig);
	// MapConfig mapConfig = new MapConfig();
	// mapConfig.setQuorumName("quorumRuleWithThreeMembers");
	// config.addQuorumConfig(quorumConfig);
	// config.addMapConfig(mapConfig);
	// HotRestartPersistenceConfig hotrescfg = new HotRestartPersistenceConfig();
	// File f = new File(System.getProperty("user.home"));
	// hotrescfg.setBackupDir(f);
	// config.setHotRestartPersistenceConfig(hotrescfg);
	// return Hazelcast.newHazelcastInstance(config);
	// }

	// public void programaticConf() {
	// MemberAttributeConfig fourCore = new MemberAttributeConfig();
	// fourCore.setIntAttribute( "CPU_CORE_COUNT", 4 );
	// config.setMemberAttributeConfig(fourCore);
	// config.setInstanceName("Instance for this config");
	// config.setNetworkConfig(1)
	// MapConfig mapCfg = new MapConfig();
	// Map<String, MapConfig> hasMapMapCfg = new HashMap<String, MapConfig>();
	// hasMapMapCfg.put("Map1", mapCfg);
	// config.setMapConfigs(hasMapMapCfg);
	// }

}
