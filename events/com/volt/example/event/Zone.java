package com.volt.example.event;

import static com.volt.example.Config.COMMON_PROPS;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class Zone {

	public final int id;
	public final int region;
	public final int manager;

	public Zone(int id, int region, int manager) {
		this.id = id;
		this.region = region;
		this.manager = manager;
	}
	
	static Properties props = (Properties) COMMON_PROPS.clone();
    protected static String topicName = "NewZone";
    protected static Producer<Integer, Zone> producer;
    
	static {
		props.put("value.serializer", "com.volt.example.event.ZoneSerializer");
		producer = new KafkaProducer<Integer, Zone>(props);
	};
	
	public void publish() {
		producer.send(new ProducerRecord<Integer, Zone>(topicName, this));
	}
}
