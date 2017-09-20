package edu.ntut.selab.builder;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.data.Point;
import edu.ntut.selab.event.AndroidEvent;
import edu.ntut.selab.event.BackKeyEvent;
import edu.ntut.selab.event.ClickEvent;
import edu.ntut.selab.event.EventData;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Utility;
import edu.ntut.selab.StateGraph;

import org.dom4j.tree.DefaultDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ATDBuilderTest {
    private final String PATH = Utility.getReportPath();
    private ReportBuilder builder;
    private File testFolder;

    @Before
    public void setup() throws FileNotFoundException, UnsupportedEncodingException {
        List<GUIState> states = new ArrayList<>();
        List<String> mockList = new ArrayList<>();
        this.testFolder = new File(PATH + "/Dot");
        this.testFolder.mkdirs();
        this.builder = new ATDBuilder(new StateGraph());
    }

    @Test
    public void testDisplayOrderBackKeyEvent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final boolean displayEventExcutionOrder = Config.DISPLAY_EVENT_EXCUTION_ORDER;
        AndroidEvent backKeyEvent = new BackKeyEvent();
        Method method = ReportBuilder.class.getDeclaredMethod("displayOrder", AndroidEvent.class);
        method.setAccessible(true);
        String actual = (String) method.invoke(this.builder, backKeyEvent);
        if (displayEventExcutionOrder) {
            System.out.println(actual);
            assertEquals(backKeyEvent.getReportLabel() + ": " + backKeyEvent.getOrder(), actual);
        } else {
            assertEquals(backKeyEvent.getReportLabel(), actual);
        }
    }

    @Test
    public void testDisplayOrderCheckEvent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final boolean displayEventExcutionOrder = Config.DISPLAY_EVENT_EXCUTION_ORDER;
        EventData eventData = new EventData("[0,0][100,100]", "test");
        AndroidEvent checkEvent = new ClickEvent(eventData, ClickEvent.Type.Check);
        Method method = ReportBuilder.class.getDeclaredMethod("displayOrder", AndroidEvent.class);
        method.setAccessible(true);
        String actual = (String) method.invoke(this.builder, checkEvent);
        if (displayEventExcutionOrder) {
            System.out.println(actual);
            assertEquals(checkEvent.getReportLabel() + ": " + checkEvent.getOrder(), actual);
        } else {
            assertEquals(checkEvent.getReportLabel(), actual);
        }
    }

    @Test
    public void testCreateSVGFile() throws IOException, InterruptedException, ExecuteCommandErrorException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String dotFilePath = "test_gui_pages/StateTransitionDiagram.dot";
        final File file = new File(dotFilePath);
        assertTrue("test dot file is not exist", file.exists());
        final String outputDir = "test_gui_pages/ActivitySubstateDiagram/StateTransitionDiagram.svg";
        Method method = ReportBuilder.class.getDeclaredMethod("createSVGFile", File.class, String.class);
        method.setAccessible(true);
        method.invoke(this.builder, file, outputDir);
        File svgFile = new File(outputDir);
        assertTrue(svgFile.exists());
        svgFile.delete();
    }

    @Test
    public void testWriteStateConnectWithBackKeyEvent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // prepare test data
        final String mockFromStateActivity = "formStateActivity";
        final String mockToStateActivity = "toStateActivity";
        final int mockFromStateId = 0;
        final int mockToStateId = 1;
        GUIState mockFromState = this.createMockState(mockFromStateActivity, mockFromStateId);
        GUIState mockToState = this.createMockState(mockToStateActivity, mockToStateId);
        AndroidEvent backKeyEvent = new BackKeyEvent();
        backKeyEvent.setToState(mockToState);
        // test
        Method method = ATDBuilder.class.getDeclaredMethod("writeStateConnect", GUIState.class, GUIState.class, AndroidEvent.class);
        method.setAccessible(true);
        String expect = "";
        if (Config.DISPLAY_EVENT_EXCUTION_ORDER)
            expect = mockFromStateActivity + " -> " + mockToStateActivity + " [label = \"  <" + mockFromStateActivity +
                    ".s" + mockFromStateId + "," + backKeyEvent.getReportLabel() + ": " + backKeyEvent.getOrder() + "," + mockToStateActivity +
                    ".s" + mockToStateId + ">  \" ] ;";
        else
            expect = mockFromStateActivity + " -> " + mockToStateActivity + " [label = \"  <" + mockFromStateActivity +
                    ".s" + mockFromStateId + "," + backKeyEvent.getReportLabel() + "," + mockToStateActivity +
                    ".s" + mockToStateId + ">  \" ] ;";
        String actual = (String) method.invoke(this.builder, mockFromState, mockToState, backKeyEvent);
        assertEquals(expect, actual);
    }

    @Test
    public void testWriteStateConnectAggregation() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // prepare test data
        final String mockFromStateActivity = "formStateActivity";
        final String mockToStateActivity = "toStateActivity";
        final int mockFromStateId = 0;
        final int mockToStateId = 1;
        GUIState mockFromState = this.createMockState(mockFromStateActivity, mockFromStateId);
        GUIState mockToState = this.createMockState(mockToStateActivity, mockToStateId);
        List<AndroidEvent> events = new ArrayList<>();
        AndroidEvent backKeyEvent = new BackKeyEvent();
        EventData eventData = new EventData("[0,0][100,100]", "test");
        AndroidEvent checkEvent = new ClickEvent(eventData, ClickEvent.Type.Check);
        backKeyEvent.setToState(mockToState);
        checkEvent.setToState(mockToState);
        events.add(backKeyEvent);
        events.add(checkEvent);
        // test
        Method method = ATDBuilder.class.getDeclaredMethod("writeStateConnectAggregation", GUIState.class, GUIState.class, List.class);
        method.setAccessible(true);
        String expect = "";
        if (Config.DISPLAY_EVENT_EXCUTION_ORDER)
            expect = "  <" + mockFromStateActivity + ".s" + mockFromStateId + "," + backKeyEvent.getReportLabel() + ": " + backKeyEvent.getOrder() + "," + mockToStateActivity +
                    ".s" + mockToStateId + ">  \n  <" + mockFromStateActivity + ".s" + mockFromStateId + "," + checkEvent.getReportLabel() + ": " + backKeyEvent.getOrder() + "," + mockToStateActivity +
                    ".s" + mockToStateId + ">  ";
        else
            expect = "  <" + mockFromStateActivity + ".s" + mockFromStateId + "," + backKeyEvent.getReportLabel() + "," + mockToStateActivity +
                    ".s" + mockToStateId + ">  \n  <" + mockFromStateActivity + ".s" + mockFromStateId + "," + checkEvent.getReportLabel() + "," + mockToStateActivity +
                    ".s" + mockToStateId + ">  ";
        String actual = (String) method.invoke(this.builder, mockFromState, mockToState, events);
        assertEquals(expect, actual);
    }

    @After
    public void teardown() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.closeATDBuilderWriter();
        File[] contents = this.testFolder.listFiles();
        for (File f : contents) {
            f.delete();
        }
        this.testFolder.delete();
        new File(PATH).delete();
    }

    // in windows, if not close writer, can not delete folder
    private void closeATDBuilderWriter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = ReportBuilder.class.getDeclaredMethod("closeWriter");
        method.setAccessible(true);
        method.invoke(this.builder);
    }

    private GUIState createMockState(String activity, int id) {
        ArrayList<AndroidEvent> androidEvents = new ArrayList<>();
        GUIState mockState = new GUIState(new DefaultDocument(), androidEvents);
        mockState.setActivityName(activity);
        mockState.setId(id);
        return mockState;
    }
}
