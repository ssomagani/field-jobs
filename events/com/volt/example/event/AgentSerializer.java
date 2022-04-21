package com.volt.example.event;

public class AgentSerializer implements org.apache.kafka.common.serialization.Serializer<Agent> {

	@Override
    public byte[] serialize(String arg0, Agent agent) {
        byte[] retVal = null;
        try {
            retVal = (agent.id + "," + agent.name + "," + agent.zone).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
}
