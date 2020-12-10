package life.genny.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class KConfig {
	static Map<String, String> config;

  public static Map<String, String>  initializeConfigurations(){
    config = new HashMap<>();
    String bootstrapServer = Optional.ofNullable(System.getenv("BOOTSTRAP_SERVER")).orElse("kafka:9092");
    String groupID = Optional.ofNullable(System.getenv("GROUP_ID")).orElse("my_group");
    // URL of Kafka server
		config.put("bootstrap.servers", bootstrapServer);
		config.put("group.id", groupID);
    config.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
    config.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
		config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		config.put("auto.offset.reset", "earliest");
		config.put("enable.auto.commit", "false");
    return config;
  }

}
