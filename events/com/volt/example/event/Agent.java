package com.volt.example.event;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import static com.volt.example.Config.*;

public class Agent {

	public final int id;
	public final int zone;
	public final String name;
	
	static Properties props = (Properties) COMMON_PROPS.clone();
    protected static String topicName = "NewAgent";
    protected static Producer<Integer, Agent> producer;
    
	static {
		props.put("value.serializer", "com.volt.example.event.AgentSerializer");
		producer = new KafkaProducer<Integer, Agent>(props);
	};
	
	public Agent(int id, String name, int zone) {
		this.id = id;
		this.name = name;
		this.zone = zone;
	}

	public void publish() {
		producer.send(new ProducerRecord<Integer, Agent>(topicName, this));
	}
}
