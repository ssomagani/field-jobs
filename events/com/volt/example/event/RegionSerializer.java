package com.volt.example.event;

public class RegionSerializer implements org.apache.kafka.common.serialization.Serializer<Region> {

	@Override
    public byte[] serialize(String arg0, Region region) {
        byte[] retVal = null;
        try {
            retVal = (region.id + ","  + region.manager).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
}
