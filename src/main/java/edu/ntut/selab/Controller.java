package edu.ntut.selab;

import edu.ntut.selab.algorithm.CrawlingAlgorithm;
import edu.ntut.selab.algorithm.DFSAlgorithm;
import edu.ntut.selab.algorithm.NFSAlgorithm;
import edu.ntut.selab.equivalent.Equivalent;
import edu.ntut.selab.generator.LogGenerator;
import edu.ntut.selab.log.LoggerStore;
import edu.ntut.selab.util.Config;

import java.io.IOException;

public class Controller {
	protected LogGenerator logGenerator = null;
	protected Equivalent equivalentRule = null;
	protected CrawlingAlgorithm algorithm = null;
	protected LoggerStore loggerStore = null;
	
	public Controller() {
		// TODO Auto-generated constructor stub
		try{
			if(Config.CRAWLING_ALGORITHM.equals("NFS"))
				algorithm = new NFSAlgorithm();
			else if(Config.CRAWLING_ALGORITHM.equals("DFS"))
				algorithm = new DFSAlgorithm();
			else
				throw new Exception();
		}
		catch(Exception e) {
			System.out.println("Wrong algorithm!");
		}
		
		loggerStore = new LoggerStore();
		algorithm.setLoggerStore(loggerStore);
	}
	
	public void executeController () throws IOException, InterruptedException, Exception {
		long startTime = 0, endTime = 0;
		startTime = System.currentTimeMillis();

		StateGraph stateGraph = new StateGraph();
		algorithm.setStateGraph(stateGraph);
		algorithm.execute();
		stateGraph.setLoggerStore(loggerStore);
		
		endTime = System.currentTimeMillis();
		stateGraph.buildReport();
//		logGenerator = new LogGenerator(algorithm.getTimeString(), stateGraph);
		logGenerator.generateLog();
	}
}
