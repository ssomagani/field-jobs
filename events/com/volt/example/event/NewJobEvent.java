package com.volt.example.event;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import static com.volt.example.Config.*;

public class NewJobEvent {
	
	public int id;
	public short type;
	public int zone;
	public int agentId;

	static Properties props = (Properties) COMMON_PROPS.clone();
    protected static String topicName = "NewJob";
    protected static Producer<Integer, NewJobEvent> producer;
    
	static {
		props.put("value.serializer", "com.volt.example.event.NewJobEventSerializer");
		producer = new KafkaProducer<Integer, NewJobEvent>(props);
	};
	
	public NewJobEvent(int id, int type, int zone, int agentId) {
		this.id = id;
		this.type = (short) type;
		this.zone = zone;
		this.agentId = agentId;
	}
	
	public void publish() {
		producer.send(new ProducerRecord<Integer, NewJobEvent>(topicName, this));
	}
	
	public String toString() {
		return id + "," + type + "," + zone + "," + agentId;
	}
}
