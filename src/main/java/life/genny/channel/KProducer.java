package life.genny.channel;

import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KProducer {
  
  static KafkaProducer <String, String> producer = null;

	public static KafkaProducer <String, String> singleProducer() {
    Map<String, String> config = KConfig.initializeConfigurations();
    Thread.currentThread().setContextClassLoader(null);
    return Optional.ofNullable(producer).orElse(new KafkaProducer(config));
	}

  public static void sendr(){
    while(true){
      try {
        Thread.sleep(10000);
        for (int i = 0; i < 5; i++) {
          // only topic and message value are specified, round robin on destination partitions
          ProducerRecord<String, String> record = new ProducerRecord("test", "message_" + i);
          singleProducer().send(record);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
