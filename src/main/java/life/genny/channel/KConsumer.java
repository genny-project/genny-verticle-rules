package life.genny.channel;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;



public class KConsumer {
  

  /** 
   * Instantiate a instance of a consumer object by passing the config which includes the url to 
   *   Kafka server
   * Pass a topic name to subscribe to a topic from a producer 
   */
	public static KafkaConsumer<String, String> singleConsumer(String topicName) {
    Map<String, String> config = KConfig.initializeConfigurations();
    Thread.currentThread().setContextClassLoader(null);
		// use consumer for interacting with Apache Kafka
		KafkaConsumer<String, String> consumer = new KafkaConsumer(config);
    consumer.subscribe(Arrays.asList(topicName));
    return consumer;
	}
  /**
   * Use handler to register to poll the messages from a producer topic  
   * timeMillis time in milliseconds for frequency poll of messages
   * cosumer an instance of a consumer 
   * recordsHandler a java consumer function which accepts the message 
   *   polled from an instant of the message polled 
   * */
  public static void handler(
      int timeMillis,
      KafkaConsumer<String, String> consumer,
      Consumer<ConsumerRecords<String, String>> records) {

    while(true){
      ConsumerRecords<String, String> res = consumer.poll(Duration.ofMillis(timeMillis));
      records.accept(res);
    }

  }

  static Consumer<ConsumerRecords<String, String>> recordsFunc = polledResult -> {
    for (ConsumerRecord<String, String> s : polledResult)
      System.out.println("here is the result " + s.value());
  };

  public static void sampleTest(){
    String topicName = "test";
    KafkaConsumer<String, String> consumer = singleConsumer(topicName);
    handler(100, consumer,recordsFunc);
  }
}

