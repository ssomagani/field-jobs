package com.volt.example;

import java.util.ArrayList;
import java.util.HashSet;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.VoltTableRow;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;

public class RegionalManagerReport extends VoltCompoundProcedure {

	private int managerId;
	private HashSet<Integer> regionSet;
	private VoltTable results;
	
	public VoltTable run(int managerId) {
		regionSet = new HashSet<>();
		this.managerId = managerId;
		newStageList(this::lookupRegions)
		.then(this::lookupZones)
		.then(this::lookupJobs)
		.then(this::finish)
		.build();
		return this.results;
	}
	
	private void lookupRegions(ClientResponse[] none) {
		queueProcedureCall("GetRegionsUnderManager", managerId);
	}
	
	private void lookupZones(ClientResponse[] resp) {
		VoltTable regions = resp[0].getResults()[0];
		int x = 0;
		while(regions.advanceRow()) {
			if(x++ > 9)
				break;
			regionSet.add((int) regions.getLong(0));
			queueProcedureCall("GetZonesUnderRegion", regions.getLong(0));

		}
	}
	
	private void lookupJobs(ClientResponse[] resp) {
		int x = 0;
		for(int i=0; i<2; i++) {
			VoltTable zones = resp[0].getResults()[0];
			while (zones.advanceRow()) {
				if(x++ > 9)
					break;
				queueProcedureCall("GetJobsUnderZone", zones.getLong(0));
			}
		}
	}
	
	private void finish(ClientResponse[] resp) {
		
		ArrayList<Integer> regionsList = new ArrayList<>(regionSet);
		VoltTable.ColumnInfo[] columns = new VoltTable.ColumnInfo[4];
		columns[0] = new VoltTable.ColumnInfo("JOB_COUNT", VoltType.INTEGER);
		columns[1] = new VoltTable.ColumnInfo("Zone", VoltType.INTEGER);
		columns[2] = new VoltTable.ColumnInfo("Region", VoltType.INTEGER);
		columns[3] = new VoltTable.ColumnInfo("Manager", VoltType.INTEGER);
		results = new VoltTable(columns);
		int x=0;
		for(int i=0; i<resp.length; i++) {
			VoltTable jobCountTable = resp[i].getResults()[0];
			if(jobCountTable.advanceRow()) {
				int jobCount = (int) jobCountTable.getLong(0);
				int zone = (int) jobCountTable.getLong(1);
				results.addRow(jobCount, zone, regionsList.get(x++), managerId);
			}
		}
		this.completeProcedure(results);
	}
}
