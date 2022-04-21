package com.volt.example.event;

import static com.volt.example.Config.COMMON_PROPS;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class Region {

	public final int id;
	public final int manager;

	public Region(int id, int manager) {
		this.id = id;
		this.manager = manager;
	}
	
	static Properties props = (Properties) COMMON_PROPS.clone();
    protected static String topicName = "NewRegion";
    protected static Producer<Integer, Region> producer;
    
	static {
		props.put("value.serializer", "com.volt.example.event.RegionSerializer");
		producer = new KafkaProducer<Integer, Region>(props);
	};
	
	public void publish() {
		producer.send(new ProducerRecord<Integer, Region>(topicName, this));
	}
}
