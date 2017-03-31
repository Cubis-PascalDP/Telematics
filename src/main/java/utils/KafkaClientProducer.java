package utils;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static telematics.GetTelematicsData.ta;

/**
 * Manages the messaging of the api and the Kafka broker.
 *
 * @author  Pascal De Poorter
 * @version 1.0
 * @since   22/3/2017
 */
public class KafkaClientProducer {

    private Producer<String, String> kafkaProducer = null;
    /**
     * Initialize the Kafka Producer for the given topic.
     */
    public KafkaClientProducer() {
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ta.getKafkaBootstrapServer());
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        kafkaProducer = new KafkaProducer<>(configProperties);

    }

    /**
     * Sent a message to the Kafka server for a specific topic
     */
    void sendMessage(String topic, String id, String message) {
        ProducerRecord<String, String> rec = new ProducerRecord<>(topic, message);
        boolean processed = false;
        while(!processed) {
            try {
                kafkaProducer.send(rec).get();
                ta.setLastEventID(Integer.parseInt(id));
                System.out.println("Event sent to kafka: " + id);
                processed = true;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Event not sent to Kafka broker: " + id);
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the Kafka Producer
     */
    public void kafkaClose(){
        kafkaProducer.close();
    }

}

