package edu.ntut.selab.event;

import static org.junit.Assert.*;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.TimeHelper;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.util.Config;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class MenuKeyEventTest {
private AndroidEvent menuKeyEvent;

    @Before
    public void setUp() throws Exception {
        this.menuKeyEvent = new MenuKeyEvent();
        testGetName();
        testGetReportLabel();
    }

    @Test
    public void testGetCommand() {
        String[] expect = new String[]{"shell", "input", "keyevent", "KEYCODE_MENU"};
        String[] actual = this.menuKeyEvent.getCommand();
        assertArrayEquals(expect, actual);
    }

    @Test
    public void testExecuteOn() throws InterruptedException, IOException, ExecuteCommandErrorException {
        Config config = new Config();
        Device device = new Device(config.getDeviceSerialNum());
        this.menuKeyEvent.executeOn(device);
    }

    @Test
    public void testGetReportLabel() {
        String expect = "press(\\\"MenuKey\\\")";
        String actual = this.menuKeyEvent.getReportLabel();
        assertEquals(expect, actual);
    }

    @Test
    public void testGetName() {
        String expect = "MenuKey Event";
        String actual = this.menuKeyEvent.getName();
        assertEquals(expect, actual);
    }
}
