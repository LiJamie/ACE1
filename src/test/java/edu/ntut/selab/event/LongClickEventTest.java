package edu.ntut.selab.event;

import static org.junit.Assert.*;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.data.Point;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.util.Config;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class LongClickEventTest {
    private AndroidEvent longClickEvent;
    private String tempLabel;
    private Point point;

    @Before
    public void setUp() throws Exception {
        this.point = new Point(100, 100);
        this.tempLabel = "testLongClickLabel";
        EventData eventData = new EventData("[0,0][200,200]", this.tempLabel);
        this.longClickEvent = new LongClickEvent(eventData);
        testGetName();
        testGetReportLabel();
    }

    @Test
    public void testGetCommand() {
        String[] expect = new String[] {"shell", "input", "swipe", "100", "100", "100", "100", "1500"};
        this.longClickEvent.getCommand();
    }

    @Test
    public void testExecuteOn() throws InterruptedException, IOException, ExecuteCommandErrorException {
        Config config = new Config();
        Device device = new Device(config.getDeviceSerialNum());
        this.longClickEvent.executeOn(device);
    }

    @Test
    public void testGetReportLabel() {
        String expect = "longClick(\\\"" + this.tempLabel + "[" + point.x() + "," + point.y() + "]" + "\\\")";
        String actual = this.longClickEvent.getReportLabel();
        assertEquals(expect, actual);
    }

    @Test
    public void testGetName() {
        String expect = "LongClick Event";
        String actual = this.longClickEvent.getName();
        assertEquals(expect, actual);
    }
}
