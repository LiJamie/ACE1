package edu.ntut.selab.controller;

import edu.ntut.selab.AndroidCrawler;
import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.equivalent.EquivalentStateStrategy;
import edu.ntut.selab.equivalent.EquivalentStateStrategyFactory;
import edu.ntut.selab.exception.*;
import edu.ntut.selab.generator.CoverageGenerator;
import edu.ntut.selab.generator.LogGenerator;
import edu.ntut.selab.iterator.Iterator;
import edu.ntut.selab.iterator.IteratorFactory;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;
import org.dom4j.DocumentException;

import java.io.*;


public class SingleDeviceController {
    private final String LAST_DATA_PATH = System.getProperty("user.dir") + "/gui_pages/" + Config.SERIALIZATION_DATA_PATH;
    private final String SERIALIZATION_PATH = LAST_DATA_PATH + "/serialData";
    private AndroidCrawler mainCrawler;
    private StateGraph stateGraph;

    public void execute() throws InterruptedException, ExecuteCommandErrorException, ClickTypeErrorException, DocumentException, IOException, IteratorTypeErrorException, EventFromStateErrorException, CrawlerControllerInitialErrorException, CannotReachTargetStateException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ClassNotFoundException, ProgressBarTimeoutException {
        this.createStateGraph();
        this.createAndroidCrawler();
        IteratorFactory iteratorFactory = new IteratorFactory();
        Iterator iterator = null;
        final String algorithm = Config.CRAWLING_ALGORITHM.toLowerCase();
        if (algorithm.equals("nfs"))
            iterator = iteratorFactory.createIterator(IteratorFactory.NFS, stateGraph, mainCrawler);
        else if (algorithm.equals("dfs"))
            iterator = iteratorFactory.createIterator(IteratorFactory.DFS, stateGraph, mainCrawler);
        else if (algorithm.equals("random_walk"))
            iterator = iteratorFactory.createIterator(IteratorFactory.RANDOM_WALK, stateGraph, mainCrawler);
        this.mainCrawler.exploreAllStates(iterator);
        this.stateGraph.buildReport();
        LogGenerator logGenerator = new LogGenerator(this.mainCrawler, stateGraph);
        logGenerator.generateLog();
        CoverageGenerator coverageGenerator = new CoverageGenerator(stateGraph, mainCrawler);
        coverageGenerator.generateStateCoveragePath();
    }

    private void createAndroidCrawler() throws IOException, ClassNotFoundException, InterruptedException, ClickTypeErrorException, MultipleListOrGridException, NullPackageNameException, DocumentException, ExecuteCommandErrorException {
        final String SERIALIZATION_FILE = "serializeAndroidCrawlerData.ser";
        File serializationFile = new File(SERIALIZATION_PATH + "/" + SERIALIZATION_FILE);
        if (Config.IMPORT_SERIALIZATION_FILE && serializationFile.exists()) {
            this.mainCrawler = (AndroidCrawler) this.deSerializeFile(serializationFile);
            this.mainCrawler.setStateGraph(this.stateGraph);
            this.copyStatesAndCoverage();
        } else {
            Config config = new Config();
            Device device = new Device(config.getDeviceSerialNum());
            this.mainCrawler = new AndroidCrawler(device, this.stateGraph);
            EquivalentStateStrategy equivalentStateStrategy = new EquivalentStateStrategyFactory().createStrategy();
            this.stateGraph.setEquivalentStragegy(equivalentStateStrategy);
        }
    }

    private void createStateGraph() throws IOException, ClassNotFoundException, DocumentException {
        final String SERIALIZATION_FILE = "serializeStateGraphData.ser";
        File serializationFile = new File(SERIALIZATION_PATH + "/" + SERIALIZATION_FILE);
        if (Config.IMPORT_SERIALIZATION_FILE && serializationFile.exists()) {
            this.stateGraph = (StateGraph) this.deSerializeFile(serializationFile);
            this.stateGraph.guideEvents();
        } else {
            this.stateGraph = new StateGraph();
            EquivalentStateStrategy equivalentStateStrategy = new EquivalentStateStrategyFactory().createStrategy();
            this.stateGraph.setEquivalentStragegy(equivalentStateStrategy);
        }
    }

    public void copyStatesAndCoverage() throws IOException {
        final String LAST_STATES_PATH = LAST_DATA_PATH + "/States";
        final String LAST_COVERAGES_PATH = LAST_DATA_PATH + "/coverages";
        final String NEW_STATES_PATH = this.mainCrawler.getReportPath() + "/States";
        final String NEW_COVERAGE_PATH = this.mainCrawler.getReportPath() + "/coverages";
        File stateDirectory = new File(LAST_STATES_PATH);
        if (stateDirectory.exists() && stateDirectory.listFiles().length > 0)
            Utility.copyFolder(LAST_STATES_PATH, NEW_STATES_PATH);
        File coverageDirectory = new File(LAST_COVERAGES_PATH);
        if (coverageDirectory.exists() && coverageDirectory.listFiles().length > 0)
            Utility.copyFolder(LAST_COVERAGES_PATH, NEW_COVERAGE_PATH);
    }

    public Object deSerializeFile(File file) throws IOException, ClassNotFoundException {
        Object serializedObject;
        FileInputStream inFile = new FileInputStream(file);
        ObjectInputStream inObject = new ObjectInputStream(inFile);
        serializedObject = inObject.readObject();
        inObject.close();
        inFile.close();
        return serializedObject;
    }
}
