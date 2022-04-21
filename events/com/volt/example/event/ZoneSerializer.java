package com.volt.example.event;

public class ZoneSerializer implements org.apache.kafka.common.serialization.Serializer<Zone> {

	@Override
    public byte[] serialize(String arg0, Zone zone) {
        byte[] retVal = null;
        try {
            retVal = (zone.id + "," + zone.region + "," + zone.manager).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
}
