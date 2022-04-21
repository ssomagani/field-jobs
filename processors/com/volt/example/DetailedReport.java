package com.volt.example;

import java.util.ArrayList;
import java.util.Arrays;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;

public class DetailedReport extends VoltCompoundProcedure {
	
	private int iteration = 0;
	private ArrayList<Long> agentList;
	private int region;
	VoltTable results;

	public VoltTable run(int region) {
		this.region = region;
		
		VoltTable.ColumnInfo[] columns = new VoltTable.ColumnInfo[6];
		columns[0] = new VoltTable.ColumnInfo("ID", VoltType.INTEGER);
		columns[1] = new VoltTable.ColumnInfo("TYPE", VoltType.INTEGER);
		columns[2] = new VoltTable.ColumnInfo("ZONE", VoltType.INTEGER);
		columns[3] = new VoltTable.ColumnInfo("AGENT", VoltType.INTEGER);
		columns[4] = new VoltTable.ColumnInfo("CREATED", VoltType.TIMESTAMP);
		columns[5] = new VoltTable.ColumnInfo("FINISHED", VoltType.TIMESTAMP);
		results = new VoltTable(columns);
		
		this.newStageList(this::getAgents)
		.then(this::setStages)
		.build();
		
		return results;
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
		for(int i=0; i<resp.length; i++) {
			VoltTable jobCountTable = resp[i].getResults()[0];
			while(jobCountTable.advanceRow()) {
				results.add(jobCountTable);
			}
		}
		queueProcedureCall("GetEachJobByAgent", agentList.get(iteration++));
	}
	
	private void finish(ClientResponse[] resp) {
		for(int i=0; i<resp.length; i++) {
			VoltTable jobCountTable = resp[i].getResults()[0];
			while(jobCountTable.advanceRow()) {
				results.add(jobCountTable);
			}
		}
		
		System.out.println("Row count = " + results.getRowCount());
		this.completeProcedure(results);
	}
}
