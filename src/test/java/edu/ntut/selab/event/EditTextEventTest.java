package edu.ntut.selab.event;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.util.Config;
import org.junit.Before;
import org.junit.Test;

import edu.ntut.selab.PointHelper;
import edu.ntut.selab.data.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditTextEventTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        //fail("Not yet implemented");
    }

    @Test
    public void testInputText() throws ExecuteCommandErrorException, InterruptedException, IOException {
        EventData eventData = new EventData("[0,110][185,159]", "editText");
        eventData.setValue("test56");
        List<EventData> eventDatas = new ArrayList<>();
        eventDatas.add(eventData);
        EditTextEvent edit = new EditTextEvent(eventDatas);
        EventData eventData1 = new EventData("[0,169][720,1280]", "editText1");
        eventData1.setValue("1234");
        List<EventData> eventDatas1 = new ArrayList<>();
        eventDatas1.add(eventData1);
        EditTextEvent edit1 = new EditTextEvent(eventDatas1);
        Config config = new Config();
        Device device = new Device(config.getDeviceSerialNum());
        edit.executeOn(device);
        edit1.executeOn(device);

    }
}
