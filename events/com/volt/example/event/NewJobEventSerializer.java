package com.volt.example.event;

public class NewJobEventSerializer implements org.apache.kafka.common.serialization.Serializer<NewJobEvent> {

	
	@Override
    public byte[] serialize(String arg0, NewJobEvent newJobEvent) {
        byte[] retVal = null;
        try {
            retVal = (newJobEvent.id + "," + newJobEvent.type + "," + newJobEvent.zone + "," + newJobEvent.agentId).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
}
