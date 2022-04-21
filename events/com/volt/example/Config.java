package com.volt.example;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public final class Config {

	public static final int JOB_COUNT = 10000000; // 10 million
	public static final int AGENT_COUNT = 100000;
	public static final int ZONE_COUNT = 5000;
	public static final int REGION_COUNT = 100;
	public static Properties COMMON_PROPS = new Properties();

	static {

		COMMON_PROPS.put("bootstrap.servers", "localhost:9092");
		COMMON_PROPS.put("acks", "all");
		COMMON_PROPS.put("retries", 0);
		COMMON_PROPS.put("batch.size", 16384);
		COMMON_PROPS.put("linger.ms", 1);
		COMMON_PROPS.put("buffer.memory", 33554432);
		COMMON_PROPS.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
		COMMON_PROPS.put("partitioner.class", "org.voltdb.client.topics.VoltDBKafkaPartitioner");

	}

	public static class IntRangeDistribution {

		public static IntFunction<IntStream> sequential(Integer domainCount, Integer rangeCount) {
			final int multiplier = rangeCount/domainCount;
			return new IntFunction<IntStream>() {

				@Override
				public IntStream apply(int domain) {

					int start = domain * multiplier;	// assuming 0 index
					int end = start + multiplier;
					return IntStream.range(start, end);
				}};
		}

		public static IntFunction<IntStream> random(Integer domainCount, Integer rangeCount) {
			final int multiplier = rangeCount/domainCount;
			return new IntFunction<IntStream>() {

				@Override
				public IntStream apply(int domain) {

					int start = domain * multiplier;	// assuming 0 index
					int end = start + multiplier;
					return ThreadLocalRandom.current().ints(start, end);
				}};
		}

	}

	public static void main(String[] args) {
		IntRangeDistribution.sequential(10, 100).apply(0).forEach(x -> System.out.println(Integer.toString(x)));
		IntRangeDistribution.random(10, 100).apply(0).forEach(x -> System.out.println(Integer.toString(x)));
	}

}
