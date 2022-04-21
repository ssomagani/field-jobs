package com.volt.example;

import java.util.ArrayList;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;

public class ExportReport extends VoltCompoundProcedure {

	
	private int iteration = 0;
	private ArrayList<Long> agentList;
	private int region;

	public long run(int region) {
		this.region = region;
		
		this.newStageList(this::getAgents)
		.then(this::setStages)
		.build();
		
		return 0;
	}
	
	private void getAgents(ClientResponse[] none) {
		queueProcedureCall("GetAgentsByRegion", region);
	}
	
	private void setStages(ClientResponse[] resp) {
		agentList = new ArrayList<>();
		VoltTable agentsTable = resp[0].getResults()[0];
		while(agentsTable.advanceRow())
			agentList.add(agentsTable.getLong(0));
		
		StageListBuilder stageList = this.newStageList((x) -> {});
		agentList.forEach((x) -> {
			stageList.then(this::lookupJobs);
		});
		
		stageList
		.then(this::finish)
		.build();
		
	}
	
	private void lookupJobs(ClientResponse[] resp) {
		queueProcedureCall("InsertJobsIntoStream", agentList.get(iteration++));
	}
	
	private void finish(ClientResponse[] resp) {
		this.completeProcedure(0l);
	}

}
