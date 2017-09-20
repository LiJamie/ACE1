package edu.ntut.selab.iterator;


import edu.ntut.selab.AndroidCrawler;
import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.StateGraph;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.exception.ClickTypeErrorException;
import edu.ntut.selab.exception.IteratorTypeErrorException;
import edu.ntut.selab.exception.MultipleListOrGridException;
import edu.ntut.selab.exception.NullPackageNameException;
import edu.ntut.selab.util.Config;

import static org.junit.Assert.*;

import edu.ntut.selab.util.Utility;
import org.dom4j.DocumentException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class iteratorFactoryTest {
    private  StateGraph stateGraph;
    private Device device;
    private AndroidCrawler androidCrawler;
    private String deviceSerialNum;

    @Before
    public void setup() throws InterruptedException, ExecuteCommandErrorException, ClickTypeErrorException, DocumentException, IOException, NullPackageNameException, MultipleListOrGridException {
        Config config = new Config();
        this.stateGraph = new StateGraph();
        this.deviceSerialNum  = config.getDeviceSerialNum();
        this.device = new Device(this.deviceSerialNum);
        this.androidCrawler = new AndroidCrawler(device, stateGraph);
    }

    @Test
    public void testCreateIterator() throws IteratorTypeErrorException {
        Iterator expected = new NFSIterator(stateGraph,androidCrawler);
        IteratorFactory iteratorFactory = new IteratorFactory();
        Iterator iterator = iteratorFactory.createIterator(IteratorFactory.NFS, stateGraph, androidCrawler);
        assertNotNull(iterator);
        assertTrue(iterator instanceof NFSIterator);
    }

    @After
    public void teardown() throws NoSuchFieldException, IllegalAccessException {
        File directory = new File(Utility.getReportPath());
        if(directory.isDirectory())
            deleteFilesInDirectory(directory);
        directory.delete();
    }

    private void deleteFilesInDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory())
                this.deleteFilesInDirectory(file);
            file.delete();
        }
    }
}
