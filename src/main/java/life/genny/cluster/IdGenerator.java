package life.genny.cluster;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class IdGenerator {
	static public Long uniqueId() {
    HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance();

  
    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
    com.hazelcast.core.IdGenerator idGen =  hazelcastInstance.getIdGenerator( "newId" );
    
    return idGen.newId();
	}
}
