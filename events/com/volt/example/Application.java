package com.volt.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.voltdb.client.Client2;
import org.voltdb.client.Client2CallOptions;
import org.voltdb.client.Client2Config;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

import com.github.javafaker.Faker;
import com.volt.example.Config.IntRangeDistribution;
import com.volt.example.event.Agent;
import com.volt.example.event.NewJobEvent;
import com.volt.example.event.Region;
import com.volt.example.event.Zone;

import static com.volt.example.Config.*;

public class Application {

	private static Client2 client;
	private static Client2CallOptions options = new Client2CallOptions();

	private static void preloadJobsAndAgents() {

		IntStream.range(0, REGION_COUNT)
		.mapToObj(
				regionId -> {
					return new Region(regionId, ThreadLocalRandom.current().nextInt(REGION_COUNT/20));
				})
		.peek (
				region -> region.publish() 
				)
		.forEach (
				region -> {
					IntRangeDistribution.sequential(REGION_COUNT, ZONE_COUNT).apply(region.id)
					.mapToObj(
							zoneId -> {
								return new Zone(zoneId, region.id, ThreadLocalRandom.current().nextInt(ZONE_COUNT/20));
							})
					.peek(
							zone -> zone.publish()
							)
					.forEach (
							zone -> {

								IntRangeDistribution.sequential(ZONE_COUNT, AGENT_COUNT).apply(zone.id)
								.mapToObj(
										agentId -> {
											return new Agent(agentId, new Faker().name().fullName(), zone.id);
										})
								.peek(
										agent -> agent.publish()
										)
								.forEach(
										agent -> IntRangeDistribution.sequential(AGENT_COUNT, JOB_COUNT).apply(agent.id).parallel()
										.mapToObj(
												jobId -> {
													return new NewJobEvent(jobId, ThreadLocalRandom.current().nextInt(4), zone.id, agent.id);
												})
										.forEach(
												job -> {
													job.publish();
												}
												));
							});
				});
	}

	private static void preload() throws IOException, ProcCallException {
		preloadJobsAndAgents();
	}
	
	private static void runLoad() {
		IntStream.range(0, AGENT_COUNT).forEach((x) -> {
			CompletableFuture<ClientResponse> future = client.callProcedureAsync("FinishJobs", x);
			try {
				Thread.currentThread().sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			futures.add(future);
		});
	}
	
	static ArrayList<CompletableFuture<ClientResponse>> futures = new ArrayList<>();

	
	static ScheduledExecutorService EXEC = Executors.newScheduledThreadPool(10);
	
	public static void main(String[] args) throws IOException, ProcCallException {
		
		Client2Config config = new Client2Config();
		config.transactionRateLimit(20000);
		config.responseExecutorService(EXEC, true);
		client = ClientFactory.createClient(config);
		client.connectSync("localhost");
//		preload();
		runLoad();
		futures.forEach((x) -> {
			try {
				x.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
	}

}
