package edu.ntut.selab.event;

import static org.junit.Assert.*;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.data.Point;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.util.Config;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class CheckEventTest {
    private AndroidEvent checkEvent;
    private String tempLabel;
    private Point point;

    @Before
    public void setUp() throws Exception {
        this.point = new Point(100, 100);
        EventData eventData = new EventData("[0,0][200,200]", this.tempLabel);
        this.checkEvent = new ClickEvent(eventData, ClickEvent.Type.Check);
        testGetName();
        testGetReportLabel();
    }

    @Test
    public void testGetCommand() {
        String[] expect = new String[]{"shell", "input", "tap", "100", "100"};
        String[] actual = this.checkEvent.getCommand();
        assertArrayEquals(expect, actual);
    }

    @Test
    public void testExecuteOn() throws InterruptedException, IOException, ExecuteCommandErrorException {
        Config config = new Config();
        Device device = new Device(config.getDeviceSerialNum());
        this.checkEvent.executeOn(device);
    }


    @Test
    public void testGetReportLabel() {
        String expect = "check(\\\"" + this.tempLabel + "\\\")";
        String actual = this.checkEvent.getReportLabel();
        assertEquals(expect, actual);
    }

    @Test
    public void testGetName() {
        String expect = "Check Event";
        String actual = this.checkEvent.getName();
        assertEquals(expect, actual);
    }
}
