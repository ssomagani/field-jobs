package com.volt.example;

import java.io.IOException;

import org.voltdb.VoltTable;
import org.voltdb.client.Client2;
import org.voltdb.client.Client2Config;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

public class DetailedReportTest {

	private static Client2 client;

	public static void main(String[] args) throws IOException, ProcCallException {
		Client2Config config = new Client2Config();
		client = ClientFactory.createClient(config);

		client.connectSync("localhost");

		Object[] procArgs = new Object[] {1};
		ClientResponse resp = client.callProcedureSync("DetailedReport", procArgs);
		VoltTable table = resp.getResults()[0];
		while(table.advanceRow()) {
			System.out.println(table.getLong(0) + ", " 
					+ table.getLong(1) + "," 
					+ table.getLong(2) + ", " 
					+ table.getLong(3) + ", " 
					+ table.getTimestampAsTimestamp(4) 
					+ ", " + table.getTimestampAsTimestamp(5));
		}
	}
}
