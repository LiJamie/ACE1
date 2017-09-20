package edu.ntut.selab;


import edu.ntut.selab.controller.SingleDeviceController;
import edu.ntut.selab.generator.CoverageGenerator;
import edu.ntut.selab.util.Config;

import java.io.File;

public class TestCaseMain {
    final static String SERIALIZATION_PATH = System.getProperty("user.dir") + "/gui_pages/" + Config.SERIALIZATION_DATA_PATH + "/serialData";
    final static String SERIALIZATION_FILE = "serializeStateGraphData.ser";
    final static String SERIALIZATION_ANDROIDCRAWLER_PATH = System.getProperty("user.dir") + "/gui_pages/" + Config.SERIALIZATION_DATA_PATH + "/serialData";
    final static String SERIALIZATION_ANDROIDCRAWLER_FILE = "serializeAndroidCrawlerData.ser";

    public static void main(String[] args) throws Exception {
        SingleDeviceController singleDeviceController = new SingleDeviceController();
        File stateGraphFile = new File(SERIALIZATION_PATH + "/" + SERIALIZATION_FILE);
        File androidCrawlerFile = new File(SERIALIZATION_ANDROIDCRAWLER_PATH + "/" + SERIALIZATION_ANDROIDCRAWLER_FILE);
        StateGraph stateGraph = (StateGraph) singleDeviceController.deSerializeFile(stateGraphFile);
        AndroidCrawler androidCrawler = (AndroidCrawler) singleDeviceController.deSerializeFile(androidCrawlerFile);
        CoverageGenerator coverageGenerator = new CoverageGenerator(stateGraph,androidCrawler);
        coverageGenerator.generateStateCoveragePath();
    }
}